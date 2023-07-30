package com.armdoctor.service.impl;

import com.armdoctor.dto.requestdto.DoctorDTO;
import com.armdoctor.dto.responsedto.DoctorResponseDTO;
import com.armdoctor.enums.Status;
import com.armdoctor.exceptions.APIException;
import com.armdoctor.exceptions.ResourceAlreadyExistsException;
import com.armdoctor.exceptions.DoctorNotFoundException;
import com.armdoctor.exceptions.DoctorValidationException;
import com.armdoctor.model.DoctorEntity;
import com.armdoctor.model.HospitalEntity;
import com.armdoctor.repository.DoctorRepository;
import com.armdoctor.repository.HospitalRepository;
import com.armdoctor.service.DoctorService;
import com.armdoctor.util.ArmDoctorMailSender;
import com.armdoctor.util.TokenGenerate;
import com.armdoctor.util.DoctorValidation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {


    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ArmDoctorMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Override
    public DoctorEntity createUser(DoctorDTO dto) throws APIException {
        DoctorValidation.validateFields(dto);
        DoctorValidation.validatePassword(dto.getPassword());
        validateDuplicate(dto);

        String verifyCode = TokenGenerate.generateVerifyCode();

        DoctorEntity doctorEntity = new DoctorEntity();
        doctorEntity.setId(0);
        doctorEntity.setName(dto.getName());
        doctorEntity.setSurname(dto.getSurname());
        doctorEntity.setYear(dto.getYear());
        doctorEntity.setEmail(dto.getEmail());
        doctorEntity.setPassword(passwordEncoder.encode(dto.getPassword()));
        doctorEntity.setVerifyCode(verifyCode);
        doctorEntity.setStatus(Status.INACTIVE);
        doctorEntity.setRole(dto.getRole());
        doctorEntity.setProfession(dto.getProfession());
        doctorEntity.setWorkTime(dto.getWorkTime());

        Set<HospitalEntity> set = new HashSet<>();
        for (String x : dto.getHospitals()) {
            HospitalEntity hospital = hospitalRepository.getByName(x);
            set.add(hospital);
        }

        doctorEntity.setHospitals(set);

        try {
            doctorRepository.save(doctorEntity);
        } catch (Exception e) {
            throw new APIException("Problem during saving the user");
        }

        mailSender.sendEmail(dto.getEmail(), "Your verification code", "Your verification code is: " + verifyCode);
        return doctorEntity;
    }

    @Override
    public List<DoctorEntity> getByUsername(String email) throws APIException {
        List<DoctorEntity> entityList = null;
        try {
            entityList = doctorRepository.getByEmail(email);
        } catch (Exception e) {
            throw new APIException("Problem during getting the user");
        }
        return entityList;
    }

    @Override
    public List<DoctorResponseDTO> getByProfession(String profession) throws APIException {
        List<DoctorEntity> dtoList = null;
        try {
            dtoList = doctorRepository.getByProfession(profession);
        } catch (Exception e) {
            throw new APIException("Problem during getting Doctors");
        }
        if (dtoList.isEmpty()) {
            throw new DoctorNotFoundException("Doctor not found with given details");
        }

        return new ObjectMapper().convertValue(dtoList, new TypeReference<List<DoctorResponseDTO>>() {
        });
    }

    @Override
    public List<DoctorEntity> getAll() throws APIException {
        List<DoctorEntity> doctorEntities = null;
        try {
            doctorEntities = doctorRepository.findAll();
        } catch (Exception e) {
            throw new APIException("Problem during getting doctors");
        }
        return doctorEntities
                .stream()
                .filter(u -> u.getName() != null && !u.getName().equals(HospitalServiceImpl.NAME))
                .collect(Collectors.toList());
    }

    @Override
    public DoctorEntity verifyUser(String email, String verifyCode) throws APIException {
        DoctorEntity doctorEntity = null;
        try {
            doctorEntity = doctorRepository.getByEmailAndVerifyCode(email, verifyCode);
            if (doctorEntity == null) {
                throw new DoctorValidationException("Wrong verification code " + verifyCode);
            }
            doctorEntity.setStatus(Status.ACTIVE);
            doctorEntity.setVerifyCode(null);

            doctorRepository.save(doctorEntity);
        } catch (Exception e) {
            throw new APIException("Problem during verifying the user");
        }
        return doctorEntity;
    }

    @Override
    public DoctorEntity changePassword(String oldPassword, String newPassword, String confirmPassword, String email) throws APIException {
        DoctorEntity doctorEntity = null;

        DoctorValidation.validatePassword(newPassword);
        if (!newPassword.equals(confirmPassword)) {
            throw new DoctorValidationException("Passwords do not match!");
        }

        try {
            doctorEntity = doctorRepository.findByEmail(email);
        } catch (Exception e) {
            throw new APIException("Problem during changing of password");
        }

        if (!doctorEntity.getPassword().equals(passwordEncoder.encode(oldPassword))) {
            throw new DoctorValidationException("Wrong old password");
        }

        doctorEntity.setPassword(passwordEncoder.encode(newPassword));
        try {
            doctorRepository.save(doctorEntity);
        } catch (Exception e) {
            throw new APIException("Problem during changing password");
        }
        return doctorEntity;
    }

    @Override
    public DoctorEntity sendToken(String email) throws APIException {
        DoctorEntity doctorEntity = null;
        try {
            doctorEntity = doctorRepository.findByEmail(email);
        } catch (Exception e) {
            throw new APIException("Problem during sending email");
        }

        if (doctorEntity == null) {
            throw new DoctorNotFoundException("Wrong email: " + email);
        }

        String resetToken = TokenGenerate.generateResetToken();
        doctorEntity.setResetToken(resetToken);
        doctorRepository.save(doctorEntity);

        mailSender.sendEmail(doctorEntity.getEmail(), "Reset Token", "Your reset token: " + resetToken);

        return doctorEntity;
    }

    @Override
    public Boolean verifyToken(String email, String token) throws APIException {
        DoctorEntity doctorEntity = null;
        try {
            doctorEntity = doctorRepository.findByEmail(email);
        } catch (Exception e) {
            throw new APIException("Problem during verifying email");
        }

        if (!doctorEntity.getResetToken().equals(token)) {
            throw new DoctorValidationException("Wrong reset token: " + token);
        }
        return true;
    }

    @Override
    public DoctorEntity forgotPassword(String email, String password, String confirmPassword) throws APIException {
        DoctorEntity doctorEntity = null;
        DoctorValidation.validatePassword(password);
        if (!password.equals(confirmPassword)) {
            throw new DoctorValidationException("Passwords do not match");
        }

        try {
            doctorEntity = doctorRepository.findByEmail(email);
        } catch (Exception e) {
            throw new APIException("Problem during changing password");
        }

        if (doctorEntity.getResetToken() == null) {
            throw new APIException("Problem during changing password");
        }

        doctorEntity.setResetToken(null);
        doctorEntity.setPassword(passwordEncoder.encode(confirmPassword));
        doctorRepository.save(doctorEntity);

        return doctorEntity;
    }

    @Override
    public DoctorEntity update(DoctorDTO dto) throws APIException {
        validateDuplicate(dto);
        DoctorValidation.validateFields(dto);

        Optional<DoctorEntity> optionalUser = doctorRepository.findById(dto.getId());
        if (optionalUser.isEmpty()) {
            throw new DoctorNotFoundException("User not found with the given ID!");
        }

        DoctorEntity doctorEntity = optionalUser.get();
        doctorEntity.setName(dto.getName());
        doctorEntity.setSurname(dto.getSurname());
        doctorEntity.setYear(dto.getYear());
        doctorEntity.setEmail(dto.getEmail() == null ? doctorEntity.getEmail() : dto.getEmail());
        doctorEntity.setRole(dto.getRole());

        Set<HospitalEntity> hospitals = new HashSet<>();
        for (String x : dto.getHospitals()) {
            HospitalEntity hospital = hospitalRepository.getByName(x);
            hospitals.add(hospital);
        }

        doctorEntity.setHospitals(hospitals);
        try {
            doctorRepository.save(doctorEntity);
        } catch (Exception e) {
            throw new APIException("Problem during updating the user");
        }
        return doctorEntity;
    }

    @Override
    public DoctorEntity bookTime(Integer id, String time, boolean isCancelled) throws APIException {
        Optional<DoctorEntity> doctorEntity = doctorRepository.findById(id);
        if (doctorEntity.isEmpty()) {
            throw new DoctorNotFoundException("Doctor not found with given ID");
        }
        DoctorEntity entity = doctorEntity.get();
        int workTime = Integer.parseInt(entity.getWorkTime());
        int bookTime = Integer.parseInt(entity.getBookTime());
        int newTime = Integer.parseInt(time);

        if (bookTime + newTime > workTime) {
            throw new DoctorValidationException("Doctor is busy");
        }

        if (isCancelled) {
            entity.setBookTime(String.valueOf(bookTime - newTime));
        } else {
            entity.setBookTime(String.valueOf(bookTime + newTime));
        }

        try {
            doctorRepository.save(entity);
        } catch (Exception e) {
            throw new APIException("Problem during saving the booking time");
        }
        return entity;
    }

    @Override
    public void delete(Integer id) throws APIException {
        Optional<DoctorEntity> optionalUser = doctorRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new DoctorNotFoundException("User not found with given ID");
        }
        try {
            doctorRepository.deleteById(id);
        } catch (Exception e) {
            throw new APIException("Problem during deleting the user");
        }
    }

    private void validateDuplicate(DoctorDTO doctorDTO) {
        if (doctorDTO.getId() == null) {
            List<DoctorEntity> doctorEntityList = doctorRepository.getByEmail(doctorDTO.getEmail());
            if (!doctorEntityList.isEmpty()) {
                throw new ResourceAlreadyExistsException("User already exists");
            }
        } else {
            DoctorEntity doctorEntity = doctorRepository.getByEmailAndIdNot(doctorDTO.getEmail(), doctorDTO.getId());
            if (doctorEntity != null) {
                throw new ResourceAlreadyExistsException("User's email already exists");
            }
        }
    }
}
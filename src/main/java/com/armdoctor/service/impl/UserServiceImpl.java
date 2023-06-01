package com.armdoctor.service.impl;

import com.armdoctor.dto.requestdto.UserDTO;
import com.armdoctor.enums.Status;
import com.armdoctor.exceptions.APIException;
import com.armdoctor.exceptions.ResourceAlreadyExistException;
import com.armdoctor.exceptions.UserValidationException;
import com.armdoctor.model.UserEntity;
import com.armdoctor.repository.UserRepository;
import com.armdoctor.service.UserService;
import com.armdoctor.util.ArmDoctorMailSender;
import com.armdoctor.util.TokenGenerate;
import com.armdoctor.util.UserValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArmDoctorMailSender mailSender;

    @Override
    public UserEntity createUser(UserDTO dto) throws APIException {
        UserValidation.validateFields(dto);
        UserValidation.validatePassword(dto.getPassword());

        String verifyCode = TokenGenerate.generateVerifyCode();

        UserEntity userEntity = new UserEntity();
        userEntity.setId(0);
        userEntity.setName(dto.getName());
        userEntity.setSurname(dto.getSurname());
        userEntity.setYear(dto.getYear());
        userEntity.setEmail(dto.getEmail());
        userEntity.setPassword(passwordEncoder.encode(dto.getPassword()));
        userEntity.setVerifyCode(verifyCode);
        userEntity.setStatus(Status.INACTIVE);
        userEntity.setRole(dto.getRole());

        try {
            userRepository.save(userEntity);
        } catch (Exception e) {
            throw new APIException("Problem during saving the user");
        }

        mailSender.sendEmail(dto.getEmail(), "Your verification code", "Your verification code is: " + verifyCode);
        return userEntity;
    }

    @Override
    public List<UserEntity> getByUsername(String email) throws APIException {
        List<UserEntity > entityList = null;
        try {
            entityList = userRepository.getByEmail(email);
        } catch (Exception e) {
            throw new APIException("problem during getting of user");
        }
        return entityList;
    }

    @Override
    public UserEntity verifyUser(String email, String verifyCode) throws APIException{
        UserEntity user = null;
        try {
            user = userRepository.getByEmailAndVerifyCode(email,verifyCode);
            if (user == null) {
                throw new UserValidationException("wrong verify code" + verifyCode);
            }
            user.setStatus(Status.ACTIVE);
            user.setVerifyCode(null);
            userRepository.save(user);
        } catch (Exception e){
            throw new APIException("problem during verifying user");
        }
        return user;
    }
    private void validateDuplicate(UserDTO dto) {
        if (dto.getId() == null) {
            List<UserEntity> userEntity = userRepository.getByEmail(dto.getEmail());
            if (userEntity.isEmpty()) {
                throw new ResourceAlreadyExistException("User already exists");
            }
        } else {
            UserEntity UserE = userRepository.getByEmailAndIdNot(dto.getEmail(), dto.getId());
            if (UserE != null) {
                throw new ResourceAlreadyExistException("Email if User already exists");
            }
        }
    }
}

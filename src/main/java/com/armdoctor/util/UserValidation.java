package com.armdoctor.util;

import com.armdoctor.dto.requestdto.UserDTO;
import com.armdoctor.exceptions.UserValidationException;
import com.armdoctor.model.UserEntity;
import com.armdoctor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserValidation {
    public static void validateFields(UserDTO userDTO) {
        if (userDTO.getName() == null || userDTO.getName().isBlank()) {
            throw new UserValidationException("User's name cannot be null or empty");
        }

        if (userDTO.getSurname() == null || userDTO.getSurname().isBlank()) {
            throw new UserValidationException("User's surname cannot be null or empty");
        }

        if (userDTO.getYear() == null || userDTO.getYear() < 1910 || userDTO.getYear() > 2020) {
            throw new UserValidationException("User's age must be between 1910 - 2020");
        }
    }


    public static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new UserValidationException("Password cannot be null or empty");
        }
        if (password.length() < 6) {
            throw new UserValidationException("Password is short");
        }


        int countOfUppercase = 0;
        int countOfDigit = 0;

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isDigit(c)) {
                countOfDigit++;
            } else if (Character.isUpperCase(c)) {
                countOfUppercase++;
            }
        }

        if (countOfUppercase < 1) {
            throw new UserValidationException("Password must contain at least 1 uppercase letter");
        }

        if (countOfDigit < 2) {
            throw new UserValidationException("Password must contain at least 2 digits");
        }
    }

}

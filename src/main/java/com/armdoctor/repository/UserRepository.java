package com.armdoctor.repository;

import com.armdoctor.model.UserEntity;

import java.util.List;

public interface UserRepository {
    List<UserEntity> getByEMail(String s);

    void save(UserEntity userEntity);
}

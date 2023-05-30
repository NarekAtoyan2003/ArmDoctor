package com.armdoctor.controler;

import com.armdoctor.dto.requestdto.UserDTO;
import com.armdoctor.exceptions.APIException;
import com.armdoctor.model.UserEntity;
import com.armdoctor.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/creat-user")
    public UserEntity createUser (@RequestBody UserDTO userDTO) throws APIException {
        UserEntity user = userService.creatUser(userDTO);
        return user;
    }
    @GetMapping("/get-by-username")
    public List<UserEntity> getByUsername(@RequestParam String email) throws APIException {
        return userService.getByUsername(email);
    }
}

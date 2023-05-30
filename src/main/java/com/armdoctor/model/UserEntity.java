package com.armdoctor.model;

import com.armdoctor.dto.requestdto.UserDTO;
import com.armdoctor.enams.Role;
import com.armdoctor.enams.Status;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;

@Entity
@Table(name = "user")
@Data
public class UserEntity {
    @Id
    private Integer id;
    @Column(name = "first_name")
    private String name;
    @Column(name = "last_name")
    private String surname;
    private Integer year;
    private String email;
    private String password;
    @Column(name = "verification_code")
    private String verifyCode;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(name = "reset_token")
    private String resetToken;

}

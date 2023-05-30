package com.armdoctor.config;

import com.armdoctor.model.UserEntity;
import com.armdoctor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public class CustomUserDetails implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        UserEntity userEntity = null;
        try {
            List<UserEntity> userEntities = userRepository.getByEMail(s);
            userEntity = userEntities.get(0);
        } catch (Exception e) {
            throw new UsernameNotFoundException("Wrong email" + s);
        }
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        return new User(userEntity.getEmail(),userEntity.getPassword(),grantedAuthorities);
    }
}

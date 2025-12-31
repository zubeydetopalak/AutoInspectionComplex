package com.zubeyde.auto.service;

import com.zubeyde.auto.entity.Admin;
import com.zubeyde.auto.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {
    @Autowired
    private AuthRepository authRepository;

    public Admin authenticate(String username, String password) {
        return authRepository.findByUsernameandPassword(username, password);
    }
    public Admin register(Admin admin) {
        System.out.println(admin.toString());
        Admin adm = new Admin();
        adm.setUsername(admin.getUsername());
        adm.setPassword(admin.getPassword());
        adm.setRole(admin.getRole());
        System.out.println(adm.toString());
        return authRepository.save(adm);

}

    public List<Admin> getAllAdmins() {
        return authRepository.findAll();
    }
};

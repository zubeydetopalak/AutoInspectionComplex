package com.zubeyde.auto.controller;

import com.zubeyde.auto.entity.Admin;
import com.zubeyde.auto.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/api")
@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Admin login(@Valid @RequestBody Admin admin) {
        System.out.println(admin.toString());
        return authService.authenticate(admin.getUsername(), admin.getPassword());
    }
    @PostMapping("/register")
    public Admin register(@Valid @RequestBody Admin admin ) {
        System.out.println(admin.toString());
        return authService.register(admin);
    }
    @GetMapping
    public List<Admin> getAllAdmins() {
        return authService.getAllAdmins();
    }

}

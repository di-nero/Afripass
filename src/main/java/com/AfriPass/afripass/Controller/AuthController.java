package com.AfriPass.afripass.Controller;

import com.AfriPass.afripass.DTOs.LoginRequest;
import com.AfriPass.afripass.DTOs.LoginResponse;
import com.AfriPass.afripass.DTOs.RegisterRequest;
import com.AfriPass.afripass.Services.AuthSerevice;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthSerevice authSerevice;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request){
        System.out.println("REGISTER ENDPOINT HIT");
        System.out.println("Request: " + request);
        authSerevice.register(request);
        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid@RequestBody LoginRequest request){
        System.out.println("login hit");
        return ResponseEntity.ok(authSerevice.login(request));
    }

    @GetMapping("/test")
    public String test() {
        return "controller is working";
    }
}

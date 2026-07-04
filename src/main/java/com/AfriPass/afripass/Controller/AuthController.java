package com.AfriPass.afripass.Controller;

import com.AfriPass.afripass.DTOs.ApiResponse;
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
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authSerevice.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registered successfully" , null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {

        LoginResponse response = authSerevice.login(request);

        return ResponseEntity.ok(ApiResponse.success("Login successful" , response));
    }
}

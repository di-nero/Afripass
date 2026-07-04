package com.AfriPass.afripass.Services;


import com.AfriPass.afripass.Exception.ConflictException;
import com.AfriPass.afripass.Security.JwtUtil;
import com.AfriPass.afripass.DTOs.LoginRequest;
import com.AfriPass.afripass.DTOs.LoginResponse;
import com.AfriPass.afripass.DTOs.RegisterRequest;
import com.AfriPass.afripass.Enums.Role;
import com.AfriPass.afripass.Model.User;
import com.AfriPass.afripass.Repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthSerevice {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    public String register(RegisterRequest request){

        // check if user email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new ConflictException("Email already registered: " + request.getEmail());
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        log.info("New user registered: {}" , request.getEmail());
        return "Registration successful";
    }

    public LoginResponse login(LoginRequest request){

       authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail() , request.getPassword()));
        String token = jwtUtil.generateToken(request.getEmail());

        log.info("User logged in: {}" , request.getEmail());
        return new LoginResponse(token , request.getEmail());
    }

}

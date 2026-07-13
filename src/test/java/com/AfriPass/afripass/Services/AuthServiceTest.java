package com.AfriPass.afripass.Services;

import com.AfriPass.afripass.DTOs.LoginRequest;
import com.AfriPass.afripass.DTOs.LoginResponse;
import com.AfriPass.afripass.DTOs.RegisterRequest;
import com.AfriPass.afripass.Repositories.UserRepository;
import com.AfriPass.afripass.Security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldRegisterUser() {

        RegisterRequest request = new RegisterRequest();
        request.setName("Joseph");
        request.setEmail("joseph@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encoded-password");

        String result = authService.register(request);

        assertEquals("Registration successful", result);

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any());
    }

    @Test
    void login_shouldReturnJwtToken() {

        LoginRequest request = new LoginRequest();
        request.setEmail("joseph@gmail.com");
        request.setPassword("password123");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(jwtUtil.generateToken(request.getEmail()))
                .thenReturn("fake-jwt-token");

        LoginResponse response = authService.login(request);

        assertEquals("fake-jwt-token", response.getToken());
        assertEquals(request.getEmail(), response.getEmail());

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(jwtUtil)
                .generateToken(request.getEmail());
    }

}
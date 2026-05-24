package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.AuthDTO.AuthResponseDTO;
import com.att.tdp.issueflow.dto.AuthDTO.LoginRequestDTO;
import com.att.tdp.issueflow.dto.UserDTO.UserResponseDTO;
import com.att.tdp.issueflow.entities.Role;
import com.att.tdp.issueflow.entities.User;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.UserRepository;
import com.att.tdp.issueflow.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_success_returnsToken() {
        User user = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .fullName("Alice")
                .role(Role.DEVELOPER)
                .build();

        LoginRequestDTO dto = LoginRequestDTO.builder()
                .username("alice")
                .password("secret")
                .build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("jwt-token");

        AuthResponseDTO result = authService.login(dto);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(user);
    }

    @Test
    void login_badCredentials_throws() {
        LoginRequestDTO dto = LoginRequestDTO.builder()
                .username("alice")
                .password("wrong")
                .build();

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void login_userNotFoundAfterAuth_throws() {
        LoginRequestDTO dto = LoginRequestDTO.builder()
                .username("ghost")
                .password("secret")
                .build();

        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(dto));
    }

    @Test
    void getMe_success_returnsMappedDTO() {
        User user = User.builder()
                .id(2L)
                .username("bob")
                .email("bob@example.com")
                .fullName("Bob")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        UserResponseDTO result = authService.getMe("bob");

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("bob", result.getUsername());
        assertEquals("bob@example.com", result.getEmail());
        assertEquals("Bob", result.getFullName());
        assertEquals(Role.ADMIN, result.getRole());
    }

    @Test
    void getMe_userNotFound_throws() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getMe("nobody"));
    }
}

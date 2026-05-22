package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.UserDTO.AddUserDTO;
import com.att.tdp.issueflow.dto.UserDTO.UserResponseDTO;
import com.att.tdp.issueflow.entities.Role;
import com.att.tdp.issueflow.entities.User;
import com.att.tdp.issueflow.exception.BadRequestException;
import com.att.tdp.issueflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserSuccessfully() {
        AddUserDTO dto = AddUserDTO.builder()
                .username("newuser")
                .password("password123")
                .fullName("New User")
                .email("new@example.com")
                .role(Role.DEVELOPER)
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(userRepository.save(any())).thenReturn(
                User.builder()
                        .id(1L)
                        .username("newuser")
                        .email("new@example.com")
                        .fullName("New User")
                        .role(Role.DEVELOPER)
                        .build());

        UserResponseDTO result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        AddUserDTO dto = AddUserDTO.builder()
                .username("existinguser")
                .email("any@example.com")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.createUser(dto));

        assertTrue(ex.getMessage().contains("Username"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        AddUserDTO dto = AddUserDTO.builder()
                .username("newuser")
                .email("taken@example.com")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.createUser(dto));

        assertTrue(ex.getMessage().contains("Email"));
        verify(userRepository, never()).save(any());
    }
}

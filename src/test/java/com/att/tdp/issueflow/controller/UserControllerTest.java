package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.UserDTO.AddUserDTO;
import com.att.tdp.issueflow.dto.UserDTO.UpdateUserDTO;
import com.att.tdp.issueflow.dto.UserDTO.UserResponseDTO;
import com.att.tdp.issueflow.entities.Role;
import com.att.tdp.issueflow.security.CustomUserDetailsService;
import com.att.tdp.issueflow.security.JwtAuthenticationFilter;
import com.att.tdp.issueflow.security.JwtUtil;
import com.att.tdp.issueflow.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"null", "removal"})
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserResponseDTO sampleResponse() {
        return UserResponseDTO.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .fullName("Alice")
                .role(Role.DEVELOPER)
                .build();
    }

    @Test
    void createUser_success_returns200() throws Exception {
        AddUserDTO dto = AddUserDTO.builder()
                .username("alice")
                .password("password123")
                .fullName("Alice")
                .email("alice@example.com")
                .role(Role.DEVELOPER)
                .build();

        when(userService.createUser(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void createUser_blankUsername_returns400() throws Exception {
        AddUserDTO dto = AddUserDTO.builder()
                .username("")
                .password("password123")
                .fullName("Alice")
                .email("alice@example.com")
                .role(Role.DEVELOPER)
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_invalidEmail_returns400() throws Exception {
        AddUserDTO dto = AddUserDTO.builder()
                .username("alice")
                .password("password123")
                .fullName("Alice")
                .email("not-an-email")
                .role(Role.DEVELOPER)
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_missingRole_returns400() throws Exception {
        AddUserDTO dto = AddUserDTO.builder()
                .username("alice")
                .password("password123")
                .fullName("Alice")
                .email("alice@example.com")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUsers_returnsList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getUserById_returnsUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void updateUser_success_returns200() throws Exception {
        UpdateUserDTO dto = UpdateUserDTO.builder()
                .fullName("Alice Updated")
                .role(Role.DEVELOPER)
                .build();

        when(userService.updateUser(any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/users/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_blankFullName_returns400() throws Exception {
        UpdateUserDTO dto = UpdateUserDTO.builder()
                .fullName("")
                .role(Role.DEVELOPER)
                .build();

        mockMvc.perform(post("/users/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_returns200() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1L);
    }
}

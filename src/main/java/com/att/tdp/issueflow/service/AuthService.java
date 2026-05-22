package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.AuthDTO.AuthResponseDTO;
import com.att.tdp.issueflow.dto.AuthDTO.LoginRequestDTO;
import com.att.tdp.issueflow.dto.UserDTO.UserResponseDTO;
import com.att.tdp.issueflow.entities.User;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.UserRepository;
import com.att.tdp.issueflow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getUsername()));

        return AuthResponseDTO.builder()
                .token(jwtUtil.generateToken(user))
                .build();
    }

    public UserResponseDTO getMe(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}

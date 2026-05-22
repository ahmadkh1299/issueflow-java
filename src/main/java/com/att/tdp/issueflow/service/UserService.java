package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.UserDTO.AddUserDTO;
import com.att.tdp.issueflow.dto.UserDTO.UpdateUserDTO;
import com.att.tdp.issueflow.dto.UserDTO.UserResponseDTO;
import com.att.tdp.issueflow.entities.User;
import com.att.tdp.issueflow.exception.BadRequestException;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO createUser(AddUserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BadRequestException("Username '" + dto.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email '" + dto.getEmail() + "' is already registered.");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .role(dto.getRole())
                .build();

        return toResponseDTO(userRepository.save(user));
    }

    public UserResponseDTO getUserById(Long id) {
        return toResponseDTO(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)));
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public UserResponseDTO updateUser(Long id, UpdateUserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setFullName(dto.getFullName());
        user.setRole(dto.getRole());
        return toResponseDTO(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    private UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}

package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.UserDTO.AddUserDTO;
import com.att.tdp.issueflow.dto.UserDTO.UpdateUserDTO;
import com.att.tdp.issueflow.dto.UserDTO.UserResponseDTO;
import com.att.tdp.issueflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody AddUserDTO dto) {
        return ResponseEntity.ok(userService.createUser(dto));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PostMapping("/update/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserDTO dto) {
        return ResponseEntity.ok(userService.updateUser(userId, dto));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}

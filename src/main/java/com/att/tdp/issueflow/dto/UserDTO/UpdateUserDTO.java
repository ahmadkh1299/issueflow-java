package com.att.tdp.issueflow.dto.UserDTO;

import com.att.tdp.issueflow.entities.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @NotNull(message = "Role is required")
    private Role role;
}

package com.att.tdp.issueflow.dto.ProjectDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectDTO {

    @NotBlank(message = "Project name cannot be blank")
    private String name;

    private String description;
}

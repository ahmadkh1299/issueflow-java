package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.ProjectDTO.AddProjectDTO;
import com.att.tdp.issueflow.dto.ProjectDTO.ProjectResponseDTO;
import com.att.tdp.issueflow.dto.ProjectDTO.UpdateProjectDTO;
import com.att.tdp.issueflow.dto.ProjectDTO.WorkloadDTO;
import com.att.tdp.issueflow.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(@Valid @RequestBody AddProjectDTO dto) {
        return ResponseEntity.ok(projectService.createProject(dto));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectDTO dto) {
        return ResponseEntity.ok(projectService.updateProject(projectId, dto));
    }

    @GetMapping("/{projectId}/workload")
    public ResponseEntity<List<WorkloadDTO>> getWorkload(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getWorkload(projectId));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok().build();
    }
}

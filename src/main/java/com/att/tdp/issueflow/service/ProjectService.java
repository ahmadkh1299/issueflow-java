package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.ProjectDTO.AddProjectDTO;
import com.att.tdp.issueflow.dto.ProjectDTO.ProjectResponseDTO;
import com.att.tdp.issueflow.dto.ProjectDTO.UpdateProjectDTO;
import com.att.tdp.issueflow.dto.ProjectDTO.WorkloadDTO;
import com.att.tdp.issueflow.entities.Project;
import com.att.tdp.issueflow.entities.User;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.ProjectRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectResponseDTO createProject(AddProjectDTO dto) {
        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getOwnerId()));

        Project project = Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .owner(owner)
                .build();
        return toResponseDTO(projectRepository.save(project));
    }

    public ProjectResponseDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return toResponseDTO(project);
    }

    public List<ProjectResponseDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ProjectResponseDTO updateProject(Long id, UpdateProjectDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        return toResponseDTO(projectRepository.save(project));
    }

    public List<WorkloadDTO> getWorkload(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        return userRepository.findDeveloperWorkloadByProjectId(projectId);
    }

    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        // TODO: Replace with soft-delete once a `deleted` boolean is added to Project.
        //       Queries (findAll, findById) must filter deleted=true rows.
        //       Only users with ADMIN role should be able to restore a soft-deleted project.
        projectRepository.delete(project);
    }

    private ProjectResponseDTO toResponseDTO(Project project) {
        return ProjectResponseDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwner() != null ? project.getOwner().getId() : null)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}

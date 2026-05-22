package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.ProjectDTO.AddProjectDTO;
import com.att.tdp.issueflow.dto.ProjectDTO.ProjectResponseDTO;
import com.att.tdp.issueflow.entities.Project;
import com.att.tdp.issueflow.entities.User;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.ProjectRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void shouldCreateProjectSuccessfully() {
        AddProjectDTO dto = AddProjectDTO.builder()
                .name("New Project")
                .description("A test project")
                .ownerId(1L)
                .build();

        User owner = User.builder().id(1L).username("owner").build();

        // save() must return a project whose owner is set, because toResponseDTO reads owner.getId()
        Project savedProject = Project.builder()
                .id(1L)
                .name("New Project")
                .description("A test project")
                .owner(owner)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(projectRepository.save(any())).thenReturn(savedProject);

        ProjectResponseDTO result = projectService.createProject(dto);

        assertNotNull(result);
        assertEquals("New Project", result.getName());
        assertEquals(1L, result.getOwnerId());
        verify(projectRepository).save(any());
    }

    @Test
    void shouldThrowExceptionWhenOwnerNotFound() {
        AddProjectDTO dto = AddProjectDTO.builder()
                .name("New Project")
                .ownerId(99L)
                .build();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.createProject(dto));

        verify(projectRepository, never()).save(any());
    }
}

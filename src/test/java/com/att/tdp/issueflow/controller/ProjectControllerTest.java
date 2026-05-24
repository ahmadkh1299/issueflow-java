package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.ProjectDTO.AddProjectDTO;
import com.att.tdp.issueflow.dto.ProjectDTO.ProjectResponseDTO;
import com.att.tdp.issueflow.dto.ProjectDTO.UpdateProjectDTO;
import com.att.tdp.issueflow.dto.ProjectDTO.WorkloadDTO;
import com.att.tdp.issueflow.security.CustomUserDetailsService;
import com.att.tdp.issueflow.security.JwtAuthenticationFilter;
import com.att.tdp.issueflow.security.JwtUtil;
import com.att.tdp.issueflow.service.ProjectService;
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
@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ProjectResponseDTO sampleResponse() {
        return ProjectResponseDTO.builder()
                .id(1L)
                .name("Sample Project")
                .description("desc")
                .ownerId(5L)
                .build();
    }

    @Test
    void createProject_success_returns200() throws Exception {
        AddProjectDTO dto = AddProjectDTO.builder()
                .name("Sample Project")
                .description("desc")
                .ownerId(5L)
                .build();

        when(projectService.createProject(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Sample Project"));
    }

    @Test
    void createProject_blankName_returns400() throws Exception {
        AddProjectDTO dto = AddProjectDTO.builder()
                .name("")
                .ownerId(5L)
                .build();

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProject_missingOwnerId_returns400() throws Exception {
        AddProjectDTO dto = AddProjectDTO.builder()
                .name("Sample")
                .build();

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllProjects_returnsList() throws Exception {
        when(projectService.getAllProjects()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getProjectById_returnsProject() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateProject_success_returns200() throws Exception {
        UpdateProjectDTO dto = UpdateProjectDTO.builder()
                .name("Renamed")
                .description("new desc")
                .build();

        when(projectService.updateProject(any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(patch("/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateProject_blankName_returns400() throws Exception {
        UpdateProjectDTO dto = UpdateProjectDTO.builder()
                .name("")
                .build();

        mockMvc.perform(patch("/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWorkload_returnsWorkloadList() throws Exception {
        WorkloadDTO workload = WorkloadDTO.builder()
                .userId(2L)
                .username("dev")
                .openTicketCount(3L)
                .build();

        when(projectService.getWorkload(1L)).thenReturn(List.of(workload));

        mockMvc.perform(get("/projects/1/workload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(2L))
                .andExpect(jsonPath("$[0].openTicketCount").value(3L));
    }

    @Test
    void deleteProject_returns200() throws Exception {
        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isOk());

        verify(projectService).deleteProject(1L);
    }
}

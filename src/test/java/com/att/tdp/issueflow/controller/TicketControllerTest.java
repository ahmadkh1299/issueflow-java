package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.AuditLogDTO.AuditLogDTO;
import com.att.tdp.issueflow.dto.TicketDTO.AddTicketDTO;
import com.att.tdp.issueflow.dto.TicketDTO.TicketResponseDTO;
import com.att.tdp.issueflow.dto.TicketDTO.UpdateTicketDTO;
import com.att.tdp.issueflow.entities.Priority;
import com.att.tdp.issueflow.entities.Status;
import com.att.tdp.issueflow.entities.Type;
import com.att.tdp.issueflow.security.CustomUserDetailsService;
import com.att.tdp.issueflow.security.JwtAuthenticationFilter;
import com.att.tdp.issueflow.security.JwtUtil;
import com.att.tdp.issueflow.service.AuditLogService;
import com.att.tdp.issueflow.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"null", "removal"})
@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private TicketResponseDTO sampleResponse() {
        return TicketResponseDTO.builder()
                .id(1L)
                .title("Sample")
                .status(Status.TODO)
                .priority(Priority.MEDIUM)
                .type(Type.BUG)
                .projectId(10L)
                .build();
    }

    @Test
    void createTicket_success_returns200() throws Exception {
        AddTicketDTO dto = AddTicketDTO.builder()
                .title("New ticket")
                .projectId(10L)
                .priority(Priority.MEDIUM)
                .type(Type.BUG)
                .build();

        when(ticketService.createTicket(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Sample"));
    }

    @Test
    void createTicket_missingTitle_returns400() throws Exception {
        AddTicketDTO dto = AddTicketDTO.builder()
                .title("")
                .projectId(10L)
                .priority(Priority.MEDIUM)
                .type(Type.BUG)
                .build();

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTicket_missingProjectId_returns400() throws Exception {
        AddTicketDTO dto = AddTicketDTO.builder()
                .title("New")
                .priority(Priority.MEDIUM)
                .type(Type.BUG)
                .build();

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTickets_returnsAllWhenNoProjectId() throws Exception {
        when(ticketService.getAllTickets()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(ticketService).getAllTickets();
    }

    @Test
    void getTickets_filtersByProjectIdWhenProvided() throws Exception {
        when(ticketService.getTicketsByProject(10L)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/tickets").param("projectId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectId").value(10L));

        verify(ticketService).getTicketsByProject(10L);
    }

    @Test
    void getTicketById_returnsTicket() throws Exception {
        when(ticketService.getTicketById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateTicket_success_returns200() throws Exception {
        UpdateTicketDTO dto = UpdateTicketDTO.builder()
                .title("Updated")
                .status(Status.IN_PROGRESS)
                .build();

        when(ticketService.updateTicket(any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(patch("/tickets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateTicket_blankTitle_returns400() throws Exception {
        UpdateTicketDTO dto = UpdateTicketDTO.builder()
                .title("")
                .build();

        mockMvc.perform(patch("/tickets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addDependency_returns200() throws Exception {
        mockMvc.perform(post("/tickets/1/dependencies/2"))
                .andExpect(status().isOk());

        verify(ticketService).addDependency(1L, 2L);
    }

    @Test
    void removeDependency_returns204() throws Exception {
        mockMvc.perform(delete("/tickets/1/dependencies/2"))
                .andExpect(status().isNoContent());

        verify(ticketService).removeDependency(1L, 2L);
    }

    @Test
    void deleteTicket_returns200() throws Exception {
        mockMvc.perform(delete("/tickets/1"))
                .andExpect(status().isOk());

        verify(ticketService).deleteTicket(1L);
    }

    @Test
    void getAuditLog_returnsLogs() throws Exception {
        AuditLogDTO log = AuditLogDTO.builder()
                .id(1L)
                .ticketId(1L)
                .action("AUTO_ASSIGN")
                .performedBy("SYSTEM")
                .timestamp(LocalDateTime.now())
                .details("info")
                .build();

        when(auditLogService.getLogsForTicket(1L)).thenReturn(List.of(log));

        mockMvc.perform(get("/tickets/1/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("AUTO_ASSIGN"))
                .andExpect(jsonPath("$[0].performedBy").value("SYSTEM"));
    }
}

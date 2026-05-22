package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.ProjectDTO.WorkloadDTO;
import com.att.tdp.issueflow.dto.TicketDTO.AddTicketDTO;
import com.att.tdp.issueflow.dto.TicketDTO.TicketResponseDTO;
import com.att.tdp.issueflow.dto.TicketDTO.UpdateTicketDTO;
import com.att.tdp.issueflow.entities.Priority;
import com.att.tdp.issueflow.entities.Project;
import com.att.tdp.issueflow.entities.Role;
import com.att.tdp.issueflow.entities.Status;
import com.att.tdp.issueflow.entities.Ticket;
import com.att.tdp.issueflow.entities.Type;
import com.att.tdp.issueflow.entities.User;
import com.att.tdp.issueflow.exception.BadRequestException;
import com.att.tdp.issueflow.repository.ProjectRepository;
import com.att.tdp.issueflow.repository.TicketRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private TicketService ticketService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAssignToLeastLoadedDeveloper() {
        Long projectId = 1L;
        Long developerId = 10L;

        Project project = Project.builder().id(projectId).name("Test Project").build();
        User developer = User.builder()
                .id(developerId)
                .username("dev")
                .role(Role.DEVELOPER)
                .build();
        WorkloadDTO workload = WorkloadDTO.builder()
                .userId(developerId)
                .username("dev")
                .openTicketCount(2L)
                .build();

        // assigneeId intentionally omitted to trigger auto-assignment
        AddTicketDTO dto = AddTicketDTO.builder()
                .title("Test Ticket")
                .projectId(projectId)
                .priority(Priority.MEDIUM)
                .type(Type.BUG)
                .build();

        // save() must return a ticket with id so auditLogService receives a non-null ticketId
        Ticket savedTicket = Ticket.builder()
                .id(100L)
                .title("Test Ticket")
                .project(project)
                .assignee(developer)
                .priority(Priority.MEDIUM)
                .type(Type.BUG)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findDeveloperWorkloadByProjectId(projectId)).thenReturn(List.of(workload));
        when(userRepository.findById(developerId)).thenReturn(Optional.of(developer));
        when(ticketRepository.save(any())).thenReturn(savedTicket);

        TicketResponseDTO result = ticketService.createTicket(dto);

        assertEquals(developerId, result.getAssigneeId());
        verify(auditLogService).logAction(
                100L,
                "AUTO_ASSIGN",
                "SYSTEM",
                "Ticket automatically assigned to user ID: " + developerId);
    }

    @Test
    void shouldThrowErrorWhenClosingBlockedTicket() {
        // blocker is not DONE — it blocks closure of target
        Ticket blocker = Ticket.builder()
                .id(3L)
                .status(Status.TODO)
                .build();

        // Using the builder initialises dependsOn to an empty HashSet (@Builder.Default)
        Ticket target = Ticket.builder()
                .id(4L)
                .status(Status.IN_REVIEW)
                .build();
        target.getDependsOn().add(blocker);

        UpdateTicketDTO dto = UpdateTicketDTO.builder()
                .title("Update")
                .status(Status.DONE)
                .build();

        when(ticketRepository.findById(4L)).thenReturn(Optional.of(target));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> ticketService.updateTicket(4L, dto));

        assertTrue(ex.getMessage().contains("dependent tickets are not yet DONE"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldAllowClosingUnblockedTicket() {
        Project project = Project.builder().id(1L).name("Test Project").build();

        // blocker is DONE — no longer an obstacle
        Ticket blocker = Ticket.builder()
                .id(3L)
                .status(Status.DONE)
                .build();

        Ticket target = Ticket.builder()
                .id(4L)
                .status(Status.IN_REVIEW)
                .priority(Priority.MEDIUM)
                .type(Type.BUG)
                .project(project)
                .build();
        target.getDependsOn().add(blocker);

        UpdateTicketDTO dto = UpdateTicketDTO.builder()
                .title("Update")
                .status(Status.DONE)
                .build();

        // updateTicket reads the current user name from the security context for the audit log
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testUser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.findById(4L)).thenReturn(Optional.of(target));
        when(ticketRepository.save(any())).thenReturn(target);

        ticketService.updateTicket(4L, dto);

        assertEquals(Status.DONE, target.getStatus());
        verify(ticketRepository).save(target);
    }

    @Test
    void shouldThrowErrorOnInvalidStateTransition() {
        // Attempt to jump from TODO directly to DONE, skipping IN_PROGRESS and IN_REVIEW
        Ticket ticket = Ticket.builder()
                .id(1L)
                .status(Status.TODO)
                .build();

        UpdateTicketDTO dto = UpdateTicketDTO.builder()
                .title("Update")
                .status(Status.DONE)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> ticketService.updateTicket(1L, dto));

        assertTrue(ex.getMessage().contains("Invalid status transition"));
    }
}

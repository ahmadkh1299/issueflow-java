package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.TicketDTO.AddTicketDTO;
import com.att.tdp.issueflow.dto.TicketDTO.TicketResponseDTO;
import com.att.tdp.issueflow.dto.TicketDTO.UpdateTicketDTO;
import com.att.tdp.issueflow.entities.Project;
import com.att.tdp.issueflow.entities.Role;
import com.att.tdp.issueflow.entities.Status;
import com.att.tdp.issueflow.entities.Ticket;
import com.att.tdp.issueflow.entities.User;
import com.att.tdp.issueflow.exception.BadRequestException;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.ProjectRepository;
import com.att.tdp.issueflow.repository.TicketRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class TicketService {

    private static final List<Status> LIFECYCLE = List.of(
            Status.TODO, Status.IN_PROGRESS, Status.IN_REVIEW, Status.DONE);

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TicketResponseDTO createTicket(AddTicketDTO dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + dto.getProjectId()));

        User assignee = resolveAssignee(dto.getAssigneeId());

        Ticket ticket = Ticket.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .project(project)
                .status(Status.TODO)
                .priority(dto.getPriority())
                .type(dto.getType())
                .assignee(assignee)
                .build();

        return toResponseDTO(ticketRepository.save(ticket));
    }

    public TicketResponseDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        return toResponseDTO(ticket);
    }

    public List<TicketResponseDTO> getTicketsByProject(Long projectId) {
        return ticketRepository.findByProjectId(projectId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public TicketResponseDTO updateTicket(Long id, UpdateTicketDTO dto) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        if (ticket.getStatus() == Status.DONE) {
            throw new BadRequestException("Ticket with id " + id + " is DONE and cannot be updated.");
        }

        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());

        if (dto.getStatus() != null && dto.getStatus() != ticket.getStatus()) {
            validateStatusTransition(ticket.getStatus(), dto.getStatus());
            ticket.setStatus(dto.getStatus());
        }

        if (dto.getPriority() != null) {
            ticket.setPriority(dto.getPriority());
        }

        if (dto.getAssigneeId() != null) {
            ticket.setAssignee(resolveAssignee(dto.getAssigneeId()));
        }

        return toResponseDTO(ticketRepository.save(ticket));
    }

    public void deleteTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        // TODO (Section 3.5 – Soft Delete): replace with soft-delete flag instead of hard delete.
        //       Soft-deleted tickets must be hidden from standard responses and recoverable via
        //       POST /tickets/{id}/restore (ADMIN only).
        ticketRepository.delete(ticket);
    }

    // TODO (Section 3.8 – Auto Assignment):
    //   On createTicket, when assigneeId is absent, query all DEVELOPER users and select the one
    //   with the fewest non-DONE tickets in the same project (workload count).
    //   Ties are broken by earliest createdAt (oldest registrant first).
    //   If no DEVELOPER exists in the project, leave assignee null without error.
    //   Record each auto-assignment in the Audit Log with actor=SYSTEM, action=AUTO_ASSIGN.

    // TODO (Section 3.2 – Ticket Dependencies / checkDependenciesBeforeClose):
    //   In validateStatusTransition, when next==DONE, fetch the ticket's blocker list and verify
    //   every blocking ticket is already DONE.
    //   Throw: new BadRequestException("Cannot close ticket: blocking dependencies are still open.")

    private void validateStatusTransition(Status current, Status next) {
        int currentIdx = LIFECYCLE.indexOf(current);
        int nextIdx = LIFECYCLE.indexOf(next);
        if (nextIdx != currentIdx + 1) {
            throw new BadRequestException(
                    "Invalid status transition from " + current + " to " + next
                    + ". Lifecycle must advance one step at a time: TODO → IN_PROGRESS → IN_REVIEW → DONE.");
        }
    }

    private User resolveAssignee(Long assigneeId) {
        if (assigneeId == null) return null;
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + assigneeId));
        if (assignee.getRole() == Role.ADMIN) {
            throw new BadRequestException(
                    "User with id " + assigneeId + " is an ADMIN and cannot be assigned to tickets.");
        }
        return assignee;
    }

    private TicketResponseDTO toResponseDTO(Ticket ticket) {
        return TicketResponseDTO.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .type(ticket.getType())
                .projectId(ticket.getProject().getId())
                .assigneeId(ticket.getAssignee() != null ? ticket.getAssignee().getId() : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}

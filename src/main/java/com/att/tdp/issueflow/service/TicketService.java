package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.ProjectDTO.WorkloadDTO;
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
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AuditLogService auditLogService;

    public TicketResponseDTO createTicket(AddTicketDTO dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + dto.getProjectId()));

        User assignee;
        if (dto.getAssigneeId() != null) {
            assignee = resolveAssignee(dto.getAssigneeId());
        } else {
            List<WorkloadDTO> workload = userRepository.findDeveloperWorkloadByProjectId(dto.getProjectId());
            if (!workload.isEmpty()) {
                assignee = userRepository.findById(workload.get(0).getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found during auto-assignment"));
            } else {
                assignee = null;
            }
        }

        Ticket ticket = Ticket.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .project(project)
                .status(Status.TODO)
                .priority(dto.getPriority())
                .type(dto.getType())
                .assignee(assignee)
                .dueDate(dto.getDueDate())
                .build();

        Ticket saved = ticketRepository.save(ticket);

        if (dto.getAssigneeId() == null && assignee != null) {
            auditLogService.logAction(saved.getId(), "AUTO_ASSIGN", "SYSTEM",
                    "Ticket automatically assigned to user ID: " + assignee.getId());
        }

        return toResponseDTO(saved);
    }

    public TicketResponseDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        return toResponseDTO(ticket);
    }

    public List<TicketResponseDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
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
            if (dto.getStatus() == Status.DONE) {
                boolean hasOpenBlockers = ticket.getDependsOn().stream()
                        .anyMatch(dep -> dep.getStatus() != Status.DONE);
                if (hasOpenBlockers) {
                    throw new BadRequestException("Cannot close ticket: dependent tickets are not yet DONE.");
                }
            }
            Status previousStatus = ticket.getStatus();
            ticket.setStatus(dto.getStatus());
            String performer = SecurityContextHolder.getContext().getAuthentication().getName();
            auditLogService.logAction(id, "STATUS_CHANGE", performer,
                    "Status changed from " + previousStatus + " to " + dto.getStatus());
        }

        if (dto.getPriority() != null && dto.getPriority() != ticket.getPriority()) {
            ticket.setPriority(dto.getPriority());
            ticket.setOverdue(false);
        }

        if (dto.getAssigneeId() != null) {
            ticket.setAssignee(resolveAssignee(dto.getAssigneeId()));
        }

        if (dto.getDueDate() != null) {
            ticket.setDueDate(dto.getDueDate());
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

    public void addDependency(Long ticketId, Long dependsOnId) {
        if (ticketId.equals(dependsOnId)) {
            throw new BadRequestException("A ticket cannot depend on itself.");
        }
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));
        Ticket blocker = ticketRepository.findById(dependsOnId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + dependsOnId));
        if (!ticket.getProject().getId().equals(blocker.getProject().getId())) {
            throw new BadRequestException("Both tickets must belong to the same project.");
        }
        ticket.getDependsOn().add(blocker);
        ticketRepository.save(ticket);
    }

    public void removeDependency(Long ticketId, Long dependsOnId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));
        ticket.getDependsOn().removeIf(dep -> dep.getId().equals(dependsOnId));
        ticketRepository.save(ticket);
    }

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
                .dueDate(ticket.getDueDate())
                .isOverdue(ticket.isOverdue())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}

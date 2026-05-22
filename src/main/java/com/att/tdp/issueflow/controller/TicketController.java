package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.AuditLogDTO.AuditLogDTO;
import com.att.tdp.issueflow.dto.TicketDTO.AddTicketDTO;
import com.att.tdp.issueflow.dto.TicketDTO.TicketResponseDTO;
import com.att.tdp.issueflow.dto.TicketDTO.UpdateTicketDTO;
import com.att.tdp.issueflow.service.AuditLogService;
import com.att.tdp.issueflow.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<TicketResponseDTO> createTicket(@Valid @RequestBody AddTicketDTO dto) {
        return ResponseEntity.ok(ticketService.createTicket(dto));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getTickets(
            @RequestParam(required = false) Long projectId) {
        if (projectId != null) {
            return ResponseEntity.ok(ticketService.getTicketsByProject(projectId));
        }
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getTicketById(ticketId));
    }

    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketResponseDTO> updateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketDTO dto) {
        return ResponseEntity.ok(ticketService.updateTicket(ticketId, dto));
    }

    @PostMapping("/{ticketId}/dependencies/{dependsOnId}")
    public ResponseEntity<Void> addDependency(
            @PathVariable Long ticketId,
            @PathVariable Long dependsOnId) {
        ticketService.addDependency(ticketId, dependsOnId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{ticketId}/dependencies/{dependsOnId}")
    public ResponseEntity<Void> removeDependency(
            @PathVariable Long ticketId,
            @PathVariable Long dependsOnId) {
        ticketService.removeDependency(ticketId, dependsOnId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long ticketId) {
        ticketService.deleteTicket(ticketId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{ticketId}/audit")
    public ResponseEntity<List<AuditLogDTO>> getAuditLog(@PathVariable Long ticketId) {
        return ResponseEntity.ok(auditLogService.getLogsForTicket(ticketId));
    }
}

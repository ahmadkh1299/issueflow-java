package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.AuditLogDTO.AuditLogDTO;
import com.att.tdp.issueflow.entities.AuditLog;
import com.att.tdp.issueflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void logAction(Long ticketId, String action, String performedBy, String details) {
        AuditLog log = AuditLog.builder()
                .ticketId(ticketId)
                .action(action)
                .performedBy(performedBy)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLogDTO> getLogsForTicket(Long ticketId) {
        return auditLogRepository.findByTicketIdOrderByTimestampDesc(ticketId).stream()
                .map(this::toDTO)
                .toList();
    }

    private AuditLogDTO toDTO(AuditLog log) {
        return AuditLogDTO.builder()
                .id(log.getId())
                .ticketId(log.getTicketId())
                .action(log.getAction())
                .performedBy(log.getPerformedBy())
                .timestamp(log.getTimestamp())
                .details(log.getDetails())
                .build();
    }
}

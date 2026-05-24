package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.AuditLogDTO.AuditLogDTO;
import com.att.tdp.issueflow.entities.AuditLog;
import com.att.tdp.issueflow.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void logAction_savesAuditLogWithCorrectFields() {
        auditLogService.logAction(42L, "STATUS_CHANGE", "alice", "TODO → IN_PROGRESS");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals(42L, saved.getTicketId());
        assertEquals("STATUS_CHANGE", saved.getAction());
        assertEquals("alice", saved.getPerformedBy());
        assertEquals("TODO → IN_PROGRESS", saved.getDetails());
        assertNotNull(saved.getTimestamp());
    }

    @Test
    void getLogsForTicket_returnsMappedDTOs() {
        AuditLog log = AuditLog.builder()
                .id(1L)
                .ticketId(10L)
                .action("AUTO_ASSIGN")
                .performedBy("SYSTEM")
                .timestamp(LocalDateTime.of(2025, 1, 1, 12, 0))
                .details("Assigned to user 5")
                .build();

        when(auditLogRepository.findByTicketIdOrderByTimestampDesc(10L)).thenReturn(List.of(log));

        List<AuditLogDTO> result = auditLogService.getLogsForTicket(10L);

        assertEquals(1, result.size());
        AuditLogDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getTicketId());
        assertEquals("AUTO_ASSIGN", dto.getAction());
        assertEquals("SYSTEM", dto.getPerformedBy());
        assertEquals("Assigned to user 5", dto.getDetails());
    }

    @Test
    void getLogsForTicket_returnsEmptyListWhenNoLogs() {
        when(auditLogRepository.findByTicketIdOrderByTimestampDesc(99L)).thenReturn(List.of());

        List<AuditLogDTO> result = auditLogService.getLogsForTicket(99L);

        assertTrue(result.isEmpty());
    }
}

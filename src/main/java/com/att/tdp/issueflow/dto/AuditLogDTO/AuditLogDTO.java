package com.att.tdp.issueflow.dto.AuditLogDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {

    private Long id;
    private Long ticketId;
    private String action;
    private String performedBy;
    private LocalDateTime timestamp;
    private String details;
}

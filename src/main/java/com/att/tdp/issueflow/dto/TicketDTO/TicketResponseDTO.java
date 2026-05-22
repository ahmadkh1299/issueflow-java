package com.att.tdp.issueflow.dto.TicketDTO;

import com.att.tdp.issueflow.entities.Priority;
import com.att.tdp.issueflow.entities.Status;
import com.att.tdp.issueflow.entities.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {

    private Long id;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private Type type;
    private Long projectId;
    private Long assigneeId;
    private LocalDateTime dueDate;
    private boolean isOverdue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

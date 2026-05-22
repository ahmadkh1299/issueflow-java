package com.att.tdp.issueflow.dto.TicketDTO;

import com.att.tdp.issueflow.entities.Priority;
import com.att.tdp.issueflow.entities.Status;
import com.att.tdp.issueflow.entities.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTicketDTO {

    @NotBlank(message = "Ticket title cannot be blank")
    private String title;

    private String description;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @NotNull(message = "Type is required")
    private Type type;

    private Status status;

    private LocalDateTime dueDate;

    private Long assigneeId;
}

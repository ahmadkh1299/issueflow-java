package com.att.tdp.issueflow.dto.TicketDTO;

import com.att.tdp.issueflow.entities.Priority;
import com.att.tdp.issueflow.entities.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketDTO {

    @NotBlank(message = "Ticket title cannot be blank")
    private String title;

    private String description;

    private Status status;

    private Priority priority;

    private Long assigneeId;

    private LocalDateTime dueDate;
}

package com.att.tdp.issueflow.dto.ProjectDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadDTO {
    private Long userId;
    private String username;
    private Long openTicketCount;
}

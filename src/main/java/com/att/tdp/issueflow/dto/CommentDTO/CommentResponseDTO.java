package com.att.tdp.issueflow.dto.CommentDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {

    private Long id;
    private String content;
    private Long ticketId;
    private Long authorId;
    private List<MentionedUserDTO> mentionedUsers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.att.tdp.issueflow.dto.CommentDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentionedUserDTO {

    private Long id;
    private String username;
    private String fullName;
}

package com.att.tdp.issueflow.dto.CommentDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentDTO {

    @NotBlank(message = "Comment content cannot be blank")
    private String content;

    @NotNull(message = "Author ID is required")
    private Long authorId;
}

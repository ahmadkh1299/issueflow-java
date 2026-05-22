package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.CommentDTO.AddCommentDTO;
import com.att.tdp.issueflow.dto.CommentDTO.CommentResponseDTO;
import com.att.tdp.issueflow.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponseDTO> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddCommentDTO dto) {
        return ResponseEntity.ok(commentService.addComment(ticketId, dto));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByTicket(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(commentService.getCommentsByTicket(ticketId));
    }
}

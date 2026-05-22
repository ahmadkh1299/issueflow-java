package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.CommentDTO.AddCommentDTO;
import com.att.tdp.issueflow.dto.CommentDTO.CommentResponseDTO;
import com.att.tdp.issueflow.entities.Comment;
import com.att.tdp.issueflow.entities.Ticket;
import com.att.tdp.issueflow.entities.User;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.CommentRepository;
import com.att.tdp.issueflow.repository.TicketRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CommentResponseDTO addComment(Long ticketId, AddCommentDTO dto) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        User author = userRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getAuthorId()));

        // TODO (Section 2.10 – Mentions):
        //   Parse dto.getContent() for tokens matching the regex @\w+ .
        //   For each token, call userRepository.findByUsername(token.substring(1)).
        //   If the user exists, persist a mention record linking this comment to that user.

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .ticket(ticket)
                .author(author)
                .build();

        return toResponseDTO(commentRepository.save(comment));
    }

    public List<CommentResponseDTO> getCommentsByTicket(Long ticketId) {
        return commentRepository.findByTicketId(ticketId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private CommentResponseDTO toResponseDTO(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .ticketId(comment.getTicket().getId())
                .authorId(comment.getAuthor().getId())
                .mentionedUsers(List.of())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}

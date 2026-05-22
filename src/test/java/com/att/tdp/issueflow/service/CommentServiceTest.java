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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void shouldAddCommentSuccessfully() {
        AddCommentDTO dto = AddCommentDTO.builder()
                .content("This is a test comment")
                .authorId(1L)
                .build();

        Ticket ticket = Ticket.builder().id(10L).build();
        User author = User.builder().id(1L).username("dev").build();

        // toResponseDTO accesses comment.getTicket().getId() and comment.getAuthor().getId()
        // so the comment returned by save must have both associations set
        Comment savedComment = Comment.builder()
                .id(1L)
                .content("This is a test comment")
                .ticket(ticket)
                .author(author)
                .build();

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentRepository.save(any())).thenReturn(savedComment);

        CommentResponseDTO result = commentService.addComment(10L, dto);

        assertNotNull(result);
        assertEquals("This is a test comment", result.getContent());
        assertEquals(10L, result.getTicketId());
        assertEquals(1L, result.getAuthorId());
        verify(commentRepository).save(any());
    }

    @Test
    void shouldThrowExceptionWhenTicketNotFound() {
        AddCommentDTO dto = AddCommentDTO.builder()
                .content("Test comment")
                .authorId(1L)
                .build();

        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.addComment(99L, dto));

        verify(commentRepository, never()).save(any());
    }
}

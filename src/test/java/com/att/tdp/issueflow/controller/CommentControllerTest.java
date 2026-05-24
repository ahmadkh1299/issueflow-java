package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.CommentDTO.AddCommentDTO;
import com.att.tdp.issueflow.dto.CommentDTO.CommentResponseDTO;
import com.att.tdp.issueflow.security.CustomUserDetailsService;
import com.att.tdp.issueflow.security.JwtAuthenticationFilter;
import com.att.tdp.issueflow.security.JwtUtil;
import com.att.tdp.issueflow.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"null", "removal"})
@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private CommentResponseDTO sampleResponse() {
        return CommentResponseDTO.builder()
                .id(1L)
                .content("Hello")
                .ticketId(10L)
                .authorId(2L)
                .build();
    }

    @Test
    void addComment_success_returns200() throws Exception {
        AddCommentDTO dto = AddCommentDTO.builder()
                .content("Hello")
                .authorId(2L)
                .build();

        when(commentService.addComment(eq(10L), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/tickets/10/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Hello"));
    }

    @Test
    void addComment_blankContent_returns400() throws Exception {
        AddCommentDTO dto = AddCommentDTO.builder()
                .content("")
                .authorId(2L)
                .build();

        mockMvc.perform(post("/tickets/10/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_missingAuthorId_returns400() throws Exception {
        AddCommentDTO dto = AddCommentDTO.builder()
                .content("Hello")
                .build();

        mockMvc.perform(post("/tickets/10/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCommentsByTicket_returnsList() throws Exception {
        when(commentService.getCommentsByTicket(10L)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/tickets/10/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].content").value("Hello"));
    }
}

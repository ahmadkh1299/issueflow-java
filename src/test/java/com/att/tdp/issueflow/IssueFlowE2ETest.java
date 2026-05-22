package com.att.tdp.issueflow;

import com.att.tdp.issueflow.service.TicketEscalationScheduler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IssueFlowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketEscalationScheduler escalationScheduler;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String toJson(Map<String, Object> map) throws Exception {
        return objectMapper.writeValueAsString(map);
    }

    private long extractId(MvcResult result) throws Exception {
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private String extractToken(MvcResult result) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    // ── Golden Path ───────────────────────────────────────────────────────────

    @Test
    void shouldExecuteFullUserJourney() throws Exception {

        // ── Step 1: Registration ──────────────────────────────────────────────

        String userABody = toJson(Map.of(
                "username", "dev_alpha",
                "password", "password123",
                "fullName", "Dev Alpha",
                "email", "dev_alpha@example.com",
                "role", "DEVELOPER"
        ));

        long userAId = extractId(
                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userABody))
                        .andExpect(status().isOk())
                        .andReturn());

        // Duplicate username must be rejected
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userABody))
                .andExpect(status().isBadRequest());

        long userBId = extractId(
                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of(
                                        "username", "dev_beta",
                                        "password", "password123",
                                        "fullName", "Dev Beta",
                                        "email", "dev_beta@example.com",
                                        "role", "DEVELOPER"
                                ))))
                        .andExpect(status().isOk())
                        .andReturn());

        // ── Step 2: Login ─────────────────────────────────────────────────────

        String token = extractToken(
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of(
                                        "username", "dev_alpha",
                                        "password", "password123"
                                ))))
                        .andExpect(status().isOk())
                        .andReturn());

        // ── Step 3: Project Creation ──────────────────────────────────────────

        long projectId = extractId(
                mockMvc.perform(post("/projects")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of(
                                        "name", "E2E Test Project",
                                        "description", "Integration test project",
                                        "ownerId", userAId
                                ))))
                        .andExpect(status().isOk())
                        .andReturn());

        // ── Step 4: Auto-Assignment Setup ─────────────────────────────────────

        // Ticket 1: past dueDate + LOW priority, no assigneeId
        // → workload ties at 0; userA registered first (lower ID) → auto-assigned to userA
        long ticket1Id = extractId(
                mockMvc.perform(post("/tickets")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of(
                                        "title", "Ticket 1 - Blocker",
                                        "description", "Will block Ticket 2",
                                        "projectId", projectId,
                                        "priority", "LOW",
                                        "type", "BUG",
                                        "dueDate", "2020-01-01T00:00:00"
                                ))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.assigneeId").value(userAId))
                        .andReturn());

        // Ticket 2: no assigneeId → userA has 1 open ticket, userB has 0 → auto-assigned to userB
        long ticket2Id = extractId(
                mockMvc.perform(post("/tickets")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of(
                                        "title", "Ticket 2 - Target",
                                        "description", "Depends on Ticket 1",
                                        "projectId", projectId,
                                        "priority", "MEDIUM",
                                        "type", "FEATURE"
                                ))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.assigneeId").value(userBId))
                        .andReturn());

        // ── Step 5: Dependency Setup ──────────────────────────────────────────
        // Ticket 2 depends on (is blocked by) Ticket 1
        mockMvc.perform(post("/tickets/{t2}/dependencies/{t1}", ticket2Id, ticket1Id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // ── Step 6: Auto-Escalation ───────────────────────────────────────────
        // Ticket 1 is overdue (dueDate in 2020) and LOW → escalates to MEDIUM
        escalationScheduler.escalateOverdueTickets();

        mockMvc.perform(get("/tickets/{id}", ticket1Id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("MEDIUM"));

        // ── Step 7: Dependency Trap ───────────────────────────────────────────
        // Advance Ticket 2 to IN_REVIEW; closing it while Ticket 1 is still open must fail
        mockMvc.perform(patch("/tickets/{id}", ticket2Id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("title", "Ticket 2 - Target", "status", "IN_PROGRESS"))))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/tickets/{id}", ticket2Id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("title", "Ticket 2 - Target", "status", "IN_REVIEW"))))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/tickets/{id}", ticket2Id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("title", "Ticket 2 - Target", "status", "DONE"))))
                .andExpect(status().isBadRequest());

        // ── Step 8: Resolve Blocker ───────────────────────────────────────────
        // Walk Ticket 1 through the full lifecycle: TODO → IN_PROGRESS → IN_REVIEW → DONE
        for (String nextStatus : List.of("IN_PROGRESS", "IN_REVIEW", "DONE")) {
            mockMvc.perform(patch("/tickets/{id}", ticket1Id)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("title", "Ticket 1 - Blocker", "status", nextStatus))))
                    .andExpect(status().isOk());
        }

        // ── Step 9: Successful Closure ────────────────────────────────────────
        // Now that Ticket 1 is DONE, Ticket 2 can be closed
        mockMvc.perform(patch("/tickets/{id}", ticket2Id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("title", "Ticket 2 - Target", "status", "DONE"))))
                .andExpect(status().isOk());

        // ── Step 10: Audit Log Verification ──────────────────────────────────
        // AUTO_ASSIGN on creation + STATUS_CHANGE entries from the lifecycle walks
        mockMvc.perform(get("/tickets/{id}/audit", ticket1Id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));

        // ── Step 11: Comments Verification ───────────────────────────────────
        String commentText = "Resolved via E2E test run.";

        mockMvc.perform(post("/tickets/{id}/comments", ticket1Id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "content", commentText,
                                "authorId", userAId
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tickets/{id}/comments", ticket1Id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].content").value(commentText));
    }
}

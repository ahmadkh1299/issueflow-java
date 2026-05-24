package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.entities.Priority;
import com.att.tdp.issueflow.entities.Status;
import com.att.tdp.issueflow.entities.Ticket;
import com.att.tdp.issueflow.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class TicketEscalationSchedulerTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketEscalationScheduler scheduler;

    private Ticket overdueTicket(Priority priority) {
        return Ticket.builder()
                .id(1L)
                .priority(priority)
                .status(Status.IN_PROGRESS)
                .dueDate(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void lowPriorityEscalatesToMedium() {
        Ticket ticket = overdueTicket(Priority.LOW);
        when(ticketRepository.findByDueDateBeforeAndStatusNot(any(), eq(Status.DONE)))
                .thenReturn(List.of(ticket));

        scheduler.escalateOverdueTickets();

        assertEquals(Priority.MEDIUM, ticket.getPriority());
        verify(ticketRepository).saveAll(List.of(ticket));
    }

    @Test
    void mediumPriorityEscalatesToHigh() {
        Ticket ticket = overdueTicket(Priority.MEDIUM);
        when(ticketRepository.findByDueDateBeforeAndStatusNot(any(), eq(Status.DONE)))
                .thenReturn(List.of(ticket));

        scheduler.escalateOverdueTickets();

        assertEquals(Priority.HIGH, ticket.getPriority());
        verify(ticketRepository).saveAll(List.of(ticket));
    }

    @Test
    void highPriorityEscalatesToCritical() {
        Ticket ticket = overdueTicket(Priority.HIGH);
        when(ticketRepository.findByDueDateBeforeAndStatusNot(any(), eq(Status.DONE)))
                .thenReturn(List.of(ticket));

        scheduler.escalateOverdueTickets();

        assertEquals(Priority.CRITICAL, ticket.getPriority());
        verify(ticketRepository).saveAll(List.of(ticket));
    }

    @Test
    void criticalPriorityGetsMarkedOverdue() {
        Ticket ticket = overdueTicket(Priority.CRITICAL);
        assertFalse(ticket.isOverdue());

        when(ticketRepository.findByDueDateBeforeAndStatusNot(any(), eq(Status.DONE)))
                .thenReturn(List.of(ticket));

        scheduler.escalateOverdueTickets();

        assertTrue(ticket.isOverdue());
        verify(ticketRepository).saveAll(List.of(ticket));
    }

    @Test
    void criticalAlreadyOverdue_notModifiedAgain() {
        Ticket ticket = Ticket.builder()
                .id(1L)
                .priority(Priority.CRITICAL)
                .status(Status.IN_PROGRESS)
                .dueDate(LocalDateTime.now().minusDays(1))
                .isOverdue(true)
                .build();

        when(ticketRepository.findByDueDateBeforeAndStatusNot(any(), eq(Status.DONE)))
                .thenReturn(List.of(ticket));

        scheduler.escalateOverdueTickets();

        verify(ticketRepository, never()).saveAll(any());
    }

    @Test
    void noOverdueTickets_saveAllNotCalled() {
        when(ticketRepository.findByDueDateBeforeAndStatusNot(any(), eq(Status.DONE)))
                .thenReturn(List.of());

        scheduler.escalateOverdueTickets();

        verify(ticketRepository, never()).saveAll(any());
    }

    @Test
    void multipleTickets_allEscalated() {
        Ticket low = overdueTicket(Priority.LOW);
        low = Ticket.builder().id(1L).priority(Priority.LOW).status(Status.TODO)
                .dueDate(LocalDateTime.now().minusDays(1)).build();
        Ticket medium = Ticket.builder().id(2L).priority(Priority.MEDIUM).status(Status.IN_PROGRESS)
                .dueDate(LocalDateTime.now().minusDays(1)).build();

        when(ticketRepository.findByDueDateBeforeAndStatusNot(any(), eq(Status.DONE)))
                .thenReturn(List.of(low, medium));

        scheduler.escalateOverdueTickets();

        assertEquals(Priority.MEDIUM, low.getPriority());
        assertEquals(Priority.HIGH, medium.getPriority());
        verify(ticketRepository, times(1)).saveAll(any());
    }
}

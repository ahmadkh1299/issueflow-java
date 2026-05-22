package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.entities.Priority;
import com.att.tdp.issueflow.entities.Status;
import com.att.tdp.issueflow.entities.Ticket;
import com.att.tdp.issueflow.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketEscalationScheduler {

    private final TicketRepository ticketRepository;

    @Scheduled(cron = "0 * * * * *")
    public void escalateOverdueTickets() {
        List<Ticket> overdueTickets = ticketRepository
                .findByDueDateBeforeAndStatusNot(LocalDateTime.now(), Status.DONE);

        List<Ticket> modified = new ArrayList<>();
        for (Ticket ticket : overdueTickets) {
            switch (ticket.getPriority()) {
                case LOW      -> { ticket.setPriority(Priority.MEDIUM);   modified.add(ticket); }
                case MEDIUM   -> { ticket.setPriority(Priority.HIGH);     modified.add(ticket); }
                case HIGH     -> { ticket.setPriority(Priority.CRITICAL); modified.add(ticket); }
                case CRITICAL -> { if (!ticket.isOverdue()) { ticket.setOverdue(true); modified.add(ticket); } }
            }
        }

        if (!modified.isEmpty()) {
            ticketRepository.saveAll(modified);
        }
    }
}

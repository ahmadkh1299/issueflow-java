package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByTicketIdOrderByTimestampDesc(Long ticketId);
}

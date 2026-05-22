package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.dto.ProjectDTO.WorkloadDTO;
import com.att.tdp.issueflow.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT new com.att.tdp.issueflow.dto.ProjectDTO.WorkloadDTO(" +
           "u.id, u.username, COUNT(t.id)) " +
           "FROM User u " +
           "LEFT JOIN Ticket t ON (t.assignee = u AND t.project.id = :projectId AND t.status <> com.att.tdp.issueflow.entities.Status.DONE) " +
           "WHERE u.role = com.att.tdp.issueflow.entities.Role.DEVELOPER " +
           "GROUP BY u.id, u.username " +
           "ORDER BY COUNT(t.id) ASC, u.id ASC")
    List<WorkloadDTO> findDeveloperWorkloadByProjectId(@Param("projectId") Long projectId);
}

package com.vermeg.restapitestgenerator.repository;

import com.vermeg.restapitestgenerator.models.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository

public interface ExecutionRepository extends JpaRepository<Execution, Long> {

    @Query("SELECT COUNT(e) FROM Execution e WHERE e.version.project.user.id = :userId")
    Long countAllExecutions(Long userId);

    @Query("SELECT COUNT(e) FROM Execution e WHERE e.version.project.user.id = :userId AND e.createdAt >= :last14Days")
    Long countExecutionsLast14Days(Long userId, LocalDateTime last14Days);
}

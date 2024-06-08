package com.vermeg.restapitestgenerator.repository;

import com.vermeg.restapitestgenerator.models.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface ExecutionRepository extends JpaRepository<Execution, Long> {
}

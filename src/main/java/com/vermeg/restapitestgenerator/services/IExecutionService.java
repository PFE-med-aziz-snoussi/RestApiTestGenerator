package com.vermeg.restapitestgenerator.services;

import com.vermeg.restapitestgenerator.models.Execution;

import java.util.List;
import java.util.Optional;

public interface IExecutionService {

    Optional<Execution> getExecutionById(Long id);
    List<Execution> getAllExecutions();
    Execution createExecution(Execution execution);
    void deleteExecution(Long id);
    Execution updateExecution(Long id, Execution execution);
    void deleteMultipleExecutions(List<Long> ids);

    }

package com.vermeg.restapitestgenerator.services;

import com.vermeg.restapitestgenerator.models.Execution;

import java.util.Optional;

public interface IExecutionService {

    Optional<Execution> getExecutionById(Long id);
    void deleteExecution(Long id);
    Execution updateExecution(Long id, Execution execution);

}

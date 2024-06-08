package com.vermeg.restapitestgenerator.services;
import com.vermeg.restapitestgenerator.models.Execution;
import com.vermeg.restapitestgenerator.repository.ExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ExecutionServiceImpl implements IExecutionService {

    private final ExecutionRepository executionRepository;

    @Autowired
    public ExecutionServiceImpl(ExecutionRepository executionRepository) {
        this.executionRepository = executionRepository;
    }

    @Override
    public Optional<Execution> getExecutionById(Long id) {
        return executionRepository.findById(id);
    }

    @Override
    public void deleteExecution(Long id) {
        executionRepository.deleteById(id);
    }

    @Override
    public Execution updateExecution(Long id, Execution execution) {
        Optional<Execution> existingExecution = executionRepository.findById(id);
        if (existingExecution.isPresent()) {
            Execution updatedExecution = existingExecution.get();
            updatedExecution.setFichierResultCollection(execution.getFichierResultCollection());
            return executionRepository.save(updatedExecution);
        }
        return null;
    }

}

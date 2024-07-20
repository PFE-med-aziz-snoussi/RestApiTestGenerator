package com.vermeg.restapitestgenerator.controllers;

import com.vermeg.restapitestgenerator.models.ERole;
import com.vermeg.restapitestgenerator.models.User;
import com.vermeg.restapitestgenerator.repository.ExecutionRepository;
import com.vermeg.restapitestgenerator.repository.ProjectRepository;
import com.vermeg.restapitestgenerator.repository.UserRepository;
import com.vermeg.restapitestgenerator.repository.VersionRepository;
import com.vermeg.restapitestgenerator.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private UserRepository userRepo;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Map<String, Long> getStatistics() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> userOpt = userRepo.findByUsername(userDetails.getUsername());

        LocalDateTime last14Days = LocalDateTime.now().minusDays(14);
        Map<String, Long> statistics = new HashMap<>();

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (user.getRoles().stream().anyMatch(role -> role.getName() == ERole.ROLE_ADMIN)) {
                // Admin logic: Retrieve statistics for all users
                Long totalProjectsWithPostmanFiles = projectRepository.countAllProjectsWithPostmanFiles();
                Long totalProjectsWithPostmanFilesLast14Days = projectRepository.countAllProjectsWithPostmanFilesLast14Days(last14Days);
                Long totalProjectsWithResultFiles = projectRepository.countAllProjectsWithResultFiles();
                Long totalProjectsWithResultFilesLast14Days = projectRepository.countAllProjectsWithResultFilesLast14Days(last14Days);

                Long totalVersions = versionRepository.countAllVersions();
                Long totalVersionsLast14Days = versionRepository.countVersionsLast14Days(last14Days);

                Long totalExecutions = executionRepository.countAllExecutions();
                Long totalExecutionsLast14Days = executionRepository.countAllExecutionsLast14Days(last14Days);

                statistics.put("projectsWithPostmanFiles", totalProjectsWithPostmanFiles);
                statistics.put("projectsWithPostmanFilesLast14Days", totalProjectsWithPostmanFilesLast14Days);
                statistics.put("projectsWithResultFiles", totalProjectsWithResultFiles);
                statistics.put("projectsWithResultFilesLast14Days", totalProjectsWithResultFilesLast14Days);
                statistics.put("totalVersions", totalVersions);
                statistics.put("versionsLast14Days", totalVersionsLast14Days);
                statistics.put("totalExecutions", totalExecutions);
                statistics.put("executionsLast14Days", totalExecutionsLast14Days);

            } else {
                Long userId = user.getId();

                Long projectsWithPostmanFiles = projectRepository.countProjectsWithPostmanFiles(userId);
                Long projectsWithPostmanFilesLast14Days = projectRepository.countProjectsWithPostmanFilesLast14Days(userId, last14Days);
                Long projectsWithResultFiles = projectRepository.countProjectsWithResultFiles(userId);
                Long projectsWithResultFilesLast14Days = projectRepository.countProjectsWithResultFilesLast14Days(userId, last14Days);

                Long totalVersions = versionRepository.countUserVersions(userId);
                Long versionsLast14Days = versionRepository.countUserVersionsLast14Days(userId, last14Days);

                Long totalExecutions = executionRepository.countAllExecutions(userId);
                Long executionsLast14Days = executionRepository.countExecutionsLast14Days(userId, last14Days);

                statistics.put("projectsWithPostmanFiles", projectsWithPostmanFiles);
                statistics.put("projectsWithPostmanFilesLast14Days", projectsWithPostmanFilesLast14Days);
                statistics.put("projectsWithResultFiles", projectsWithResultFiles);
                statistics.put("projectsWithResultFilesLast14Days", projectsWithResultFilesLast14Days);
                statistics.put("totalVersions", totalVersions);
                statistics.put("versionsLast14Days", versionsLast14Days);
                statistics.put("totalExecutions", totalExecutions);
                statistics.put("executionsLast14Days", executionsLast14Days);
            }
        }

        return statistics;
    }

}

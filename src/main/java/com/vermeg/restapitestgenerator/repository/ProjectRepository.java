package com.vermeg.restapitestgenerator.repository;

import com.vermeg.restapitestgenerator.models.Project;
import com.vermeg.restapitestgenerator.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUser(User user);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.user.id = :userId AND EXISTS (SELECT v FROM Version v WHERE v.project.id = p.id AND v.fichierPostmanCollection IS NOT NULL)")
    Long countProjectsWithPostmanFiles(Long userId);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.user.id = :userId AND EXISTS (SELECT v FROM Version v WHERE v.project.id = p.id AND v.fichierPostmanCollection IS NOT NULL AND v.createdAt >= :last14Days)")
    Long countProjectsWithPostmanFilesLast14Days(Long userId, LocalDateTime last14Days);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.user.id = :userId AND EXISTS (SELECT v FROM Version v WHERE v.project.id = p.id AND EXISTS (SELECT e FROM Execution e WHERE e.version.id = v.id AND e.fichierResultCollection IS NOT NULL))")
    Long countProjectsWithResultFiles(Long userId);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.user.id = :userId AND EXISTS (SELECT v FROM Version v WHERE v.project.id = p.id AND EXISTS (SELECT e FROM Execution e WHERE e.version.id = v.id AND e.fichierResultCollection IS NOT NULL AND e.createdAt >= :last14Days))")
    Long countProjectsWithResultFilesLast14Days(Long userId, LocalDateTime last14Days);
}

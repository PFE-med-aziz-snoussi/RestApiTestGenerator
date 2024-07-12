package com.vermeg.restapitestgenerator.repository;

import com.vermeg.restapitestgenerator.models.Project;
import com.vermeg.restapitestgenerator.models.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {
    List<Version> findByProjectId(Long projectId);
    @Query("SELECT v.project FROM Version v WHERE v.id = :versionId")
    Project findProjectByVersionId(Long versionId);

    @Query("SELECT COUNT(v) FROM Version v WHERE v.project.user.id = :userId")
    Long countAllVersions(Long userId);

    @Query("SELECT COUNT(v) FROM Version v WHERE v.project.user.id = :userId AND v.createdAt >= :last14Days")
    Long countVersionsLast14Days(Long userId, LocalDateTime last14Days);
}

package com.vermeg.restapitestgenerator.repository;

import com.vermeg.restapitestgenerator.models.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {
    List<Version> findByProjectId(Long projectId);

}

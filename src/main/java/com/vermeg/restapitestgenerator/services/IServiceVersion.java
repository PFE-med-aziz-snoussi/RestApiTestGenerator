package com.vermeg.restapitestgenerator.services;

import com.vermeg.restapitestgenerator.models.Project;
import com.vermeg.restapitestgenerator.models.Version;

import java.util.List;
import java.util.Optional;

public interface IServiceVersion {
    Version createVersion(Version version);

    Optional<Version> getVersionById(Long id);

    List<Version> getAllVersions();

    Version updateVersion(Long id, Version version);

    void deleteVersion(Long id);
    List<Version> getVersionsByProjectId(Long projectId);

    Project findProjectByVersionId(Long versionId);

    }

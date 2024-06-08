package com.vermeg.restapitestgenerator.services;

import com.vermeg.restapitestgenerator.models.Version;
import com.vermeg.restapitestgenerator.repository.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VersionServiceImpl implements IServiceVersion {

    @Autowired
    private VersionRepository versionRepository;

    @Override
    public Version createVersion(Version version) {
        return versionRepository.save(version);
    }

    @Override
    public Optional<Version> getVersionById(Long id) {
        return versionRepository.findById(id);
    }

    @Override
    public List<Version> getAllVersions() {
        return versionRepository.findAll();
    }

    @Override
    public Version updateVersion(Long id, Version version) {
        Optional<Version> existingVersionOptional = versionRepository.findById(id);
        if (existingVersionOptional.isPresent()) {
            Version existingVersion = existingVersionOptional.get();
            existingVersion.setFichierOpenAPI(version.getFichierOpenAPI());
            existingVersion.setFichierPostmanCollection(version.getFichierPostmanCollection());
            return versionRepository.save(existingVersion);
        }
        return null;
    }

    @Override
    public void deleteVersion(Long id) {
        versionRepository.deleteById(id);
    }

    public List<Version> getVersionsByProjectId(Long projectId) {
        return versionRepository.findByProjectId(projectId);
    }
}


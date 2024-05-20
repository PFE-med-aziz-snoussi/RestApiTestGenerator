package com.vermeg.restapitestgenerator.services;

import com.vermeg.restapitestgenerator.models.Project;
import com.vermeg.restapitestgenerator.models.User;

import java.util.List;
import java.util.Optional;

public interface IProjectService {

    Project createProject(Project project);
    List<Project> getAllProjects();
    Optional<Project> getProjectById(Long id);
    Project updateProject(Long id, Project projectDetails);
    String deleteProject(Long id);
    public List<Project> getProjectsByUser(User user);

    }

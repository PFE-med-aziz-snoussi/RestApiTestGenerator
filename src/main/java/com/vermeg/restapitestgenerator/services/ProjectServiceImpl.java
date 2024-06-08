package com.vermeg.restapitestgenerator.services;

import com.vermeg.restapitestgenerator.models.User;
import com.vermeg.restapitestgenerator.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vermeg.restapitestgenerator.models.Project;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectServiceImpl implements IProjectService{
    @Autowired
    ProjectRepository projectRepository;

    @Override
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    @Override
    public Project updateProject(Long id, Project projectDetails) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        project.setNomDuProjet(projectDetails.getNomDuProjet());
        project.setUser(project.getUser());
        project.setVersions(project.getVersions());

        return projectRepository.save(project);
    }
    @Override
    public String deleteProject(Long id) {
        Optional<Project> projectOptional = projectRepository.findById(id);
        if (projectOptional.isPresent()) {
            projectRepository.deleteById(id);
            return "Project deleted successfully.";
        } else {
            return "Project not found.";
        }
    }

    @Override
    public List<Project> getProjectsByUser(User user) {
        return projectRepository.findByUser(user);
    }

}

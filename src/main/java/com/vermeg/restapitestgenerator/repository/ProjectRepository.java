package com.vermeg.restapitestgenerator.repository;

import com.vermeg.restapitestgenerator.models.Project;
import com.vermeg.restapitestgenerator.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUser(User user);
}

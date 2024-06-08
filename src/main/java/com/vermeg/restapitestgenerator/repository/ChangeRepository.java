package com.vermeg.restapitestgenerator.repository;

import com.vermeg.restapitestgenerator.models.Change;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChangeRepository extends JpaRepository<Change, Long> {
}

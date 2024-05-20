package com.vermeg.restapitestgenerator.repository;

import com.vermeg.restapitestgenerator.models.ERole;
import com.vermeg.restapitestgenerator.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);

}

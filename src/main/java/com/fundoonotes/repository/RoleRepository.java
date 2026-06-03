package com.fundoonotes.repository;

import com.fundoonotes.entity.Role;
import com.fundoonotes.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}

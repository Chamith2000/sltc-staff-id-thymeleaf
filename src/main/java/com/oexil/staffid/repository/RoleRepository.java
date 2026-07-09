package com.oexil.staffid.repository;

import com.oexil.staffid.enums.ERole;
import com.oexil.staffid.model.masters.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(ERole name);
}

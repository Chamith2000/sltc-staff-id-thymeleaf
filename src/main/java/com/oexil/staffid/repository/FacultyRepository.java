package com.oexil.staffid.repository;

import com.oexil.staffid.model.staff.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    
    boolean existsByNameIgnoreCase(String name);
    
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    
    Faculty findByNameIgnoreCase(String name);
    
}
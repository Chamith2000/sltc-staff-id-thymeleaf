package com.oexil.staffid.repository;

import com.oexil.staffid.model.staff.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    boolean existsByNameIgnoreCase(String name);
    
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    
    List<Department> findByStaffTypeId(Long staffTypeId);
    
    Department findByNameIgnoreCase(String name);
    
}
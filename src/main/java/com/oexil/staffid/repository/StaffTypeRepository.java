package com.oexil.staffid.repository;

import com.oexil.staffid.model.staff.StaffType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffTypeRepository extends JpaRepository<StaffType, Long> {
    
    boolean existsByTypeNameIgnoreCase(String typeName);
    
    boolean existsByTypeNameIgnoreCaseAndIdNot(String typeName, Long id);
    
    StaffType findByTypeNameIgnoreCase(String typeName);
    
}
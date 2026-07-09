package com.oexil.staffid.repository;

import com.oexil.staffid.model.staff.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    boolean existsByEmpNoOrPhoneNumber(String regNo, String phoneNumber);

    boolean existsByEmpNo(String empNo);
    
    // Methods for checking duplicates excluding current employee (for updates)
    boolean existsByEmpNoAndIdNot(String empNo, Long id);
    
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
    
    boolean existsByEmailAndIdNot(String email, Long id);
    
    boolean existsByEmpNoOrPhoneNumberAndIdNot(String empNo, String phoneNumber, Long id);
    
    boolean existsByEmail(String email);
    
    Employee findByEmpNo(String empNo);
    
    Employee findByEmail(String email);

    Page<Employee> findByActiveTrue(Pageable pageable);

    List<Employee> findByBatchIdAndActiveTrue(String batchId);
    
    @Query("SELECT e FROM Employee e WHERE e.active = true " +
           "AND (:staffTypeId IS NULL OR e.staffType.id = :staffTypeId) " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "AND (:facultyId IS NULL OR e.department.faculty.id = :facultyId) " +
           "AND (:designation IS NULL OR :designation = '' OR LOWER(e.designation) LIKE LOWER(CONCAT('%', :designation, '%')))")
    Page<Employee> findFilteredEmployees(@Param("staffTypeId") Long staffTypeId,
                                        @Param("departmentId") Long departmentId,
                                        @Param("facultyId") Long facultyId,
                                        @Param("designation") String designation,
                                        Pageable pageable);
}
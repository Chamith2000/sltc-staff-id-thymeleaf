package com.oexil.staffid.service;

import com.oexil.staffid.dto.staff.DepartmentDTO;
import com.oexil.staffid.dto.staff.EmployeeDTO;
import com.oexil.staffid.dto.staff.FacultyDTO;
import com.oexil.staffid.dto.staff.StaffTypeDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
public interface EmployeeService {

    void getAllEmployees(Model model, int page, int size);
    
    void getAllEmployees(Model model, int page, int size, Long staffTypeId, Long departmentId, Long facultyId, String designation);

    String registerStaff(@Valid EmployeeDTO studentDTO);

    String registerEmployeeBatch(MultipartFile file);
    
    String bulkRegisterEmployees(MultipartFile file);

    EmployeeDTO getEmployeeById(Long id);

    List<DepartmentDTO> getAllDepartments(Model model);

    List<DepartmentDTO> getDepartmentsByStaffType(Long staffTypeId);

    List<StaffTypeDTO> getStaffTypes(Model model);

    List<FacultyDTO> getFaculties(Model model);

    String updateEmployee(EmployeeDTO employeeDTO);

    EmployeeDTO getEmployeeESignatureDetails(Long id);

    Page<EmployeeDTO> getPrintedEmployees(int page, int size, String printLabel, String batchId);

    Page<EmployeeDTO> getPendingEmployees(int page, int size, String printLabel, String batchId);

    String markEmployeeAsPrinted(Long employeeId, String printLabel, String batchId);

    String markBatchAsPrinted(String batchId, String printLabel);

    String deleteEmployee(Long employeeId);
}
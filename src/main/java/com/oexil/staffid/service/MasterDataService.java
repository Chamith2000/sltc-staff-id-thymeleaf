package com.oexil.staffid.service;

import com.oexil.staffid.dto.staff.DepartmentDTO;
import com.oexil.staffid.dto.staff.FacultyDTO;
import com.oexil.staffid.dto.staff.StaffTypeDTO;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

@Service
public interface MasterDataService {

    // General master data operations
    void getAllMasterData(Model model);

    // Department operations
    List<DepartmentDTO> getAllDepartments(Model model);
    List<DepartmentDTO> getDepartmentsByStaffType(Long staffTypeId);
    DepartmentDTO getDepartmentById(Long id);
    String saveDepartment(DepartmentDTO departmentDTO);
    String updateDepartment(DepartmentDTO departmentDTO);
    String deleteDepartment(Long id);

    // Faculty operations
    List<FacultyDTO> getAllFaculties(Model model);
    FacultyDTO getFacultyById(Long id);
    String saveFaculty(FacultyDTO facultyDTO);
    String updateFaculty(FacultyDTO facultyDTO);
    String deleteFaculty(Long id);

    // Staff Type operations
    List<StaffTypeDTO> getAllStaffTypes(Model model);
    StaffTypeDTO getStaffTypeById(Long id);
    String saveStaffType(StaffTypeDTO staffTypeDTO);
    String updateStaffType(StaffTypeDTO staffTypeDTO);
    String deleteStaffType(Long id);
}
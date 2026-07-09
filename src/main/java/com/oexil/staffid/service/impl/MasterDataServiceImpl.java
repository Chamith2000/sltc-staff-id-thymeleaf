package com.oexil.staffid.service.impl;

import com.oexil.staffid.dto.staff.DepartmentDTO;
import com.oexil.staffid.dto.staff.FacultyDTO;
import com.oexil.staffid.dto.staff.StaffTypeDTO;
import com.oexil.staffid.model.staff.Department;
import com.oexil.staffid.model.staff.Faculty;
import com.oexil.staffid.model.staff.StaffType;
import com.oexil.staffid.repository.DepartmentRepository;
import com.oexil.staffid.repository.FacultyRepository;
import com.oexil.staffid.repository.StaffTypeRepository;
import com.oexil.staffid.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MasterDataServiceImpl implements MasterDataService {

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final StaffTypeRepository staffTypeRepository;
    private final ModelMapper modelMapper;

    @Override
    public void getAllMasterData(Model model) {
        getAllDepartments(model);
        getAllFaculties(model);
        getAllStaffTypes(model);
    }

    // Department operations
    @Override
    public List<DepartmentDTO> getAllDepartments(Model model) {
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentDTO> departmentDTOs = departments.stream()
                .map(this::convertToDepartmentDTO)
                .toList();
        model.addAttribute("departments", departmentDTOs);
        return departmentDTOs;
    }

    @Override
    public List<DepartmentDTO> getDepartmentsByStaffType(Long staffTypeId) {
        List<Department> departments;
        if (staffTypeId != null) {
            departments = departmentRepository.findByStaffTypeId(staffTypeId);
        } else {
            departments = departmentRepository.findAll();
        }
        
        return departments.stream()
                .map(this::convertToDepartmentDTO)
                .toList();
    }

    @Override
    public DepartmentDTO getDepartmentById(Long id) {
        Optional<Department> department = departmentRepository.findById(id);
        return department.map(this::convertToDepartmentDTO).orElse(null);
    }

    private DepartmentDTO convertToDepartmentDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDisplayName(department.getDisplayName());
        
        if (department.getStaffType() != null) {
            StaffTypeDTO staffTypeDTO = new StaffTypeDTO();
            staffTypeDTO.setId(department.getStaffType().getId());
            staffTypeDTO.setTypeName(department.getStaffType().getTypeName());
            staffTypeDTO.setDescription(department.getStaffType().getDescription());
            dto.setStaffType(staffTypeDTO);
        }
        
        if (department.getFaculty() != null) {
            FacultyDTO facultyDTO = new FacultyDTO();
            facultyDTO.setId(department.getFaculty().getId());
            facultyDTO.setName(department.getFaculty().getName());
            facultyDTO.setDisplayName(department.getFaculty().getDisplayName());
            dto.setFaculty(facultyDTO);
        }
        
        return dto;
    }

    @Override
    public String saveDepartment(DepartmentDTO departmentDTO) {
        try {
            // Check for duplicate name
            if (departmentRepository.existsByNameIgnoreCase(departmentDTO.getName())) {
                return "DUPLICATE_NAME";
            }

            Department department = new Department();
            department.setName(departmentDTO.getName());
            department.setDisplayName(departmentDTO.getDisplayName());

            if (departmentDTO.getStaffType() != null && departmentDTO.getStaffType().getId() != null) {
                Optional<StaffType> staffType = staffTypeRepository.findById(departmentDTO.getStaffType().getId());
                staffType.ifPresent(department::setStaffType);
            }

            if (departmentDTO.getFaculty() != null && departmentDTO.getFaculty().getId() != null) {
                Optional<Faculty> faculty = facultyRepository.findById(departmentDTO.getFaculty().getId());
                faculty.ifPresent(department::setFaculty);
            }

            departmentRepository.save(department);
            return "SUCCESS";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    public String updateDepartment(DepartmentDTO departmentDTO) {
        try {
            Optional<Department> existingDept = departmentRepository.findById(departmentDTO.getId());
            if (existingDept.isEmpty()) {
                return "NOT_FOUND";
            }

            // Check for duplicate name (excluding current department)
            if (departmentRepository.existsByNameIgnoreCaseAndIdNot(departmentDTO.getName(), departmentDTO.getId())) {
                return "DUPLICATE_NAME";
            }

            Department department = existingDept.get();
            department.setName(departmentDTO.getName());
            department.setDisplayName(departmentDTO.getDisplayName());

            if (departmentDTO.getStaffType() != null && departmentDTO.getStaffType().getId() != null) {
                Optional<StaffType> staffType = staffTypeRepository.findById(departmentDTO.getStaffType().getId());
                staffType.ifPresent(department::setStaffType);
            } else {
                department.setStaffType(null);
            }

            if (departmentDTO.getFaculty() != null && departmentDTO.getFaculty().getId() != null) {
                Optional<Faculty> faculty = facultyRepository.findById(departmentDTO.getFaculty().getId());
                faculty.ifPresent(department::setFaculty);
            } else {
                department.setFaculty(null);
            }

            departmentRepository.save(department);
            return "SUCCESS";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    public String deleteDepartment(Long id) {
        try {
            if (!departmentRepository.existsById(id)) {
                return "NOT_FOUND";
            }
            departmentRepository.deleteById(id);
            return "SUCCESS";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // Faculty operations
    @Override
    public List<FacultyDTO> getAllFaculties(Model model) {
        List<Faculty> faculties = facultyRepository.findAll();
        List<FacultyDTO> facultyDTOs = faculties.stream()
                .map(this::convertToFacultyDTO)
                .toList();
        model.addAttribute("faculties", facultyDTOs);
        return facultyDTOs;
    }

    @Override
    public FacultyDTO getFacultyById(Long id) {
        Optional<Faculty> faculty = facultyRepository.findById(id);
        return faculty.map(this::convertToFacultyDTO).orElse(null);
    }

    private FacultyDTO convertToFacultyDTO(Faculty faculty) {
        FacultyDTO dto = new FacultyDTO();
        dto.setId(faculty.getId());
        dto.setName(faculty.getName());
        dto.setDisplayName(faculty.getDisplayName());
        return dto;
    }

    @Override
    public String saveFaculty(FacultyDTO facultyDTO) {
        try {
            // Check for duplicate name
            if (facultyRepository.existsByNameIgnoreCase(facultyDTO.getName())) {
                return "DUPLICATE_NAME";
            }

            Faculty faculty = new Faculty();
            faculty.setName(facultyDTO.getName());
            faculty.setDisplayName(facultyDTO.getDisplayName());

            facultyRepository.save(faculty);
            return "SUCCESS";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    public String updateFaculty(FacultyDTO facultyDTO) {
        try {
            Optional<Faculty> existingFaculty = facultyRepository.findById(facultyDTO.getId());
            if (existingFaculty.isEmpty()) {
                return "NOT_FOUND";
            }

            // Check for duplicate name (excluding current faculty)
            if (facultyRepository.existsByNameIgnoreCaseAndIdNot(facultyDTO.getName(), facultyDTO.getId())) {
                return "DUPLICATE_NAME";
            }

            Faculty faculty = existingFaculty.get();
            faculty.setName(facultyDTO.getName());
            faculty.setDisplayName(facultyDTO.getDisplayName());

            facultyRepository.save(faculty);
            return "SUCCESS";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    public String deleteFaculty(Long id) {
        try {
            if (!facultyRepository.existsById(id)) {
                return "NOT_FOUND";
            }
            facultyRepository.deleteById(id);
            return "SUCCESS";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // Staff Type operations
    @Override
    public List<StaffTypeDTO> getAllStaffTypes(Model model) {
        List<StaffType> staffTypes = staffTypeRepository.findAll();
        List<StaffTypeDTO> staffTypeDTOs = staffTypes.stream()
                .map(this::convertToStaffTypeDTO)
                .toList();
        model.addAttribute("staffTypes", staffTypeDTOs);
        return staffTypeDTOs;
    }

    @Override
    public StaffTypeDTO getStaffTypeById(Long id) {
        Optional<StaffType> staffType = staffTypeRepository.findById(id);
        return staffType.map(this::convertToStaffTypeDTO).orElse(null);
    }

    private StaffTypeDTO convertToStaffTypeDTO(StaffType staffType) {
        StaffTypeDTO dto = new StaffTypeDTO();
        dto.setId(staffType.getId());
        dto.setTypeName(staffType.getTypeName());
        dto.setDescription(staffType.getDescription());
        return dto;
    }

    @Override
    public String saveStaffType(StaffTypeDTO staffTypeDTO) {
        try {
            // Check for duplicate type name
            if (staffTypeRepository.existsByTypeNameIgnoreCase(staffTypeDTO.getTypeName())) {
                return "DUPLICATE_TYPE_NAME";
            }

            StaffType staffType = new StaffType();
            staffType.setTypeName(staffTypeDTO.getTypeName());
            staffType.setDescription(staffTypeDTO.getDescription());

            staffTypeRepository.save(staffType);
            return "SUCCESS";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    public String updateStaffType(StaffTypeDTO staffTypeDTO) {
        try {
            Optional<StaffType> existingStaffType = staffTypeRepository.findById(staffTypeDTO.getId());
            if (existingStaffType.isEmpty()) {
                return "NOT_FOUND";
            }

            // Check for duplicate type name (excluding current staff type)
            if (staffTypeRepository.existsByTypeNameIgnoreCaseAndIdNot(staffTypeDTO.getTypeName(), staffTypeDTO.getId())) {
                return "DUPLICATE_TYPE_NAME";
            }

            StaffType staffType = existingStaffType.get();
            staffType.setTypeName(staffTypeDTO.getTypeName());
            staffType.setDescription(staffTypeDTO.getDescription());

            staffTypeRepository.save(staffType);
            return "SUCCESS";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    public String deleteStaffType(Long id) {
        try {
            if (!staffTypeRepository.existsById(id)) {
                return "NOT_FOUND";
            }
            staffTypeRepository.deleteById(id);
            return "SUCCESS";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
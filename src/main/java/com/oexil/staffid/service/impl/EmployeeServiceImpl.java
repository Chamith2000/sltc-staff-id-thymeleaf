package com.oexil.staffid.service.impl;

import com.oexil.staffid.dto.staff.DepartmentDTO;
import com.oexil.staffid.dto.staff.EmployeeDTO;
import com.oexil.staffid.dto.staff.FacultyDTO;
import com.oexil.staffid.dto.staff.StaffTypeDTO;
import com.oexil.staffid.model.staff.Department;
import com.oexil.staffid.model.staff.Employee;
import com.oexil.staffid.model.staff.Faculty;
import com.oexil.staffid.model.staff.StaffType;
import com.oexil.staffid.repository.DepartmentRepository;
import com.oexil.staffid.repository.EmployeeRepository;
import com.oexil.staffid.repository.FacultyRepository;
import com.oexil.staffid.repository.StaffTypeRepository;
import com.oexil.staffid.service.ImageUploadService;
import com.oexil.staffid.service.EmployeeService;
import com.oexil.staffid.service.ExcelService;
import com.oexil.staffid.utils.QRCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final StaffTypeRepository staffTypeRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final ImageUploadService imageUploadService;
    private final ModelMapper modelMapper;
    private final ExcelService excelService;

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) return null;

        EmployeeDTO employeeDTO = modelMapper.map(employee, EmployeeDTO.class);
        employeeDTO.setQr(QRCodeUtil.generateQRCode(employeeDTO.getEmpNo()));
        employeeDTO.setQrBase64(Base64.encodeBase64String(employeeDTO.getQr()));

        return employeeDTO;
    }

    @Override
    public void getAllEmployees(Model model, int page, int size) {
        getAllEmployees(model, page, size, null, null, null, null);
    }
    
    @Override
    public void getAllEmployees(Model model, int page, int size, Long staffTypeId, Long departmentId, Long facultyId, String designation) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employeePage;
        
        // Use filtering if any filter parameters are provided
        if (staffTypeId != null || departmentId != null || facultyId != null || 
            (designation != null && !designation.trim().isEmpty())) {
            employeePage = employeeRepository.findFilteredEmployees(staffTypeId, departmentId, facultyId, designation, pageable);
        } else {
            employeePage = employeeRepository.findByActiveTrue(pageable);
        }

        // Convert Page<Employee> to Page<EmployeeDTO> with QR codes
        Page<EmployeeDTO> employeeDTOPage = employeePage.map(employee -> {
            EmployeeDTO employeeDTO = modelMapper.map(employee, EmployeeDTO.class);
            try {
                String qrData = String.format(
                        "empNo : %s , name : %s , designation : %s",
                        employeeDTO.getEmpNo(),
                        employeeDTO.getFullName(),
                        employeeDTO.getDesignation()
                );
                byte[] qrCode = QRCodeUtil.generateQRCode(qrData);
                employeeDTO.setQr(qrCode);
                employeeDTO.setQrBase64(Base64.encodeBase64String(qrCode));
            } catch (Exception e) {
                // Handle QR code generation failure (e.g., log error, set default QR)
                employeeDTO.setQrBase64("");
            }
            return employeeDTO;
        });

        model.addAttribute("employees", employeeDTOPage);
    }

    @Override
    public String registerStaff(EmployeeDTO dto) {
        log.info("Registering staff member: {},{},{}", dto.getNic(), dto.getEmpNo(), dto.getPhoneNumber());
        boolean exists = employeeRepository.existsByEmpNoOrPhoneNumber(dto.getEmpNo(), dto.getPhoneNumber());

        if (exists) return "DUPLICATE";

        if (dto.getDepartmentId() == null) {
            throw new IllegalArgumentException("Department ID is required");
        }
        Department department = departmentRepository.findById(dto.getDepartmentId()).orElseThrow();
        Employee employee = new Employee();
        employee.setDepartment(department);
        
        // Set staff type if provided
        if (dto.getStaffTypeId() != null) {
            StaffType staffType = staffTypeRepository.findById(dto.getStaffTypeId()).orElseThrow();
            employee.setStaffType(staffType);
        }

        employee.setTitle(dto.getTitle());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmpNo(dto.getEmpNo());
        employee.setDesignation(dto.getDesignation());
        employee.setQualification(dto.getQualification());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setEmail(dto.getEmail());
        employee.setNic(dto.getNic());
        employee.setActive(true);
        employee.setCreateDate(new Date());

        String[] fileWriteResults = imageUploadService.getResultsOfFileWrite(dto.getImage());
        Optional.ofNullable(fileWriteResults).ifPresent(results -> {
            employee.setFilePath(results[1]);
            employee.setFileName(results[2]);
        });

        employeeRepository.save(employee);
        return "SUCCESS";
    }

    @Override
    @Transactional
    public String registerEmployeeBatch(MultipartFile file) {
        try {
            // Validate file
            if (!excelService.validateExcelFile(file)) {
                return "ERROR: Invalid file format. Please upload an Excel file (.xlsx or .xls)";
            }

            // Parse Excel data
            List<EmployeeDTO> employeeDTOs = excelService.parseEmployeeExcel(file);
            
            if (employeeDTOs.isEmpty()) {
                return "ERROR: No valid employee data found in the Excel file";
            }

            List<Employee> employeesToSave = new ArrayList<>();
            List<String> processedResults = new ArrayList<>();
            int insertCount = 0;
            int updateCount = 0;

            for (EmployeeDTO dto : employeeDTOs) {
                try {
                    Employee employee = processEmployeeForBatch(dto);
                    if (employee != null) {
                        employeesToSave.add(employee);
                        if (employee.getId() != null) {
                            updateCount++;
                            processedResults.add("Updated: " + dto.getEmpNo() + " - " + dto.getFullName());
                        } else {
                            insertCount++;
                            processedResults.add("Inserted: " + dto.getEmpNo() + " - " + dto.getFullName());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing employee {}: {}", dto.getEmpNo(), e.getMessage());
                    return "ERROR: Failed to process employee " + dto.getEmpNo() + ": " + e.getMessage();
                }
            }

            // Save all employees in batch
            employeeRepository.saveAll(employeesToSave);
            
            String resultMessage = String.format("SUCCESS: Processed %d employees (%d inserted, %d updated)", 
                                                insertCount + updateCount, insertCount, updateCount);
            log.info(resultMessage);
            
            return resultMessage;

        } catch (Exception e) {
            log.error("Error during bulk employee registration", e);
            return "ERROR: " + e.getMessage();
        }
    }

    private Employee processEmployeeForBatch(EmployeeDTO dto) throws Exception {
        Employee employee = null;
        boolean isUpdate = false;

        // Check for existing employee by Emp No
        Employee existingByEmpNo = employeeRepository.findByEmpNo(dto.getEmpNo());
        
        // Check for existing employee by Email (if email is provided)
        Employee existingByEmail = null;
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            existingByEmail = employeeRepository.findByEmail(dto.getEmail());
        }

        // Determine if we should update or insert
        if (existingByEmpNo != null) {
            employee = existingByEmpNo;
            isUpdate = true;
        } else if (existingByEmail != null) {
            employee = existingByEmail;
            isUpdate = true;
        } else {
            employee = new Employee();
            employee.setCreateDate(new Date());
            employee.setActive(true);
        }

        // Update employee fields
        employee.setTitle(dto.getTitle());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmpNo(dto.getEmpNo());
        employee.setQualification(dto.getQualification());
        employee.setDesignation(dto.getDesignation());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setEmail(dto.getEmail());

        // Set department if provided
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + dto.getDepartmentId()));
            employee.setDepartment(department);
        }

        // Set staff type if provided
        if (dto.getStaffTypeId() != null) {
            StaffType staffType = staffTypeRepository.findById(dto.getStaffTypeId())
                .orElseThrow(() -> new RuntimeException("Staff type not found with ID: " + dto.getStaffTypeId()));
            employee.setStaffType(staffType);
        }

        if (isUpdate) {
            employee.setUpdateDate(new Date());
        }

        return employee;
    }

    @Override
    @Transactional
    public String bulkRegisterEmployees(MultipartFile file) {
        try {
            // Validate file
            if (!excelService.validateExcelFile(file)) {
                return "ERROR: Invalid file format. Please upload an Excel file (.xlsx or .xls)";
            }

            // Parse Excel data using ID-based lookups
            List<EmployeeDTO> employeeDTOs = excelService.parseEmployeeExcelWithIds(file);
            
            if (employeeDTOs.isEmpty()) {
                return "ERROR: No valid employee data found in the Excel file";
            }

            List<Employee> employeesToSave = new ArrayList<>();
            List<String> processedResults = new ArrayList<>();
            int insertCount = 0;
            int updateCount = 0;

            for (EmployeeDTO dto : employeeDTOs) {
                try {
                    Employee employee = processEmployeeForBatch(dto);
                    if (employee != null) {
                        employeesToSave.add(employee);
                        if (employee.getId() != null) {
                            updateCount++;
                            processedResults.add("Updated: " + dto.getEmpNo() + " - " + dto.getFullName());
                        } else {
                            insertCount++;
                            processedResults.add("Inserted: " + dto.getEmpNo() + " - " + dto.getFullName());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing employee {}: {}", dto.getEmpNo(), e.getMessage());
                    return "ERROR: Failed to process employee " + dto.getEmpNo() + ": " + e.getMessage();
                }
            }

            // Save all employees in batch
            employeeRepository.saveAll(employeesToSave);
            
            String resultMessage = String.format("SUCCESS: Processed %d employees (%d inserted, %d updated)", 
                                                insertCount + updateCount, insertCount, updateCount);
            log.info(resultMessage);
            
            return resultMessage;

        } catch (Exception e) {
            log.error("Error during bulk employee registration", e);
            return "ERROR: " + e.getMessage();
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private Long getCellNumericValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            throw new IllegalArgumentException("Row " + (row.getRowNum() + 1) + ", Column " + (cellIndex + 1) + ": Cell is missing.");
        }
        try {
            return Math.round(cell.getNumericCellValue());
        } catch (Exception e) {
            throw new IllegalArgumentException("Row " + (row.getRowNum() + 1) + ", Column " + (cellIndex + 1) + ": Expected numeric value but found: " + cell.toString());
        }
    }


    @Override
    public List<DepartmentDTO> getAllDepartments(Model model) {
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentDTO> departmentDTOS = departments.stream()
                .map(this::convertToSimpleDepartmentDTO)
                .toList();
        model.addAttribute("departments", departmentDTOS);
        return departmentDTOS;
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
                .map(this::convertToSimpleDepartmentDTO)
                .toList();
    }

    private DepartmentDTO convertToSimpleDepartmentDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDisplayName(department.getDisplayName());
        
        // Only set basic info for staff type and faculty to avoid deep nesting
        if (department.getStaffType() != null) {
            StaffTypeDTO staffTypeDTO = new StaffTypeDTO();
            staffTypeDTO.setId(department.getStaffType().getId());
            staffTypeDTO.setTypeName(department.getStaffType().getTypeName());
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
    public List<StaffTypeDTO> getStaffTypes(Model model) {
        List<StaffType> staffTypes = staffTypeRepository.findAll();
        List<StaffTypeDTO> staffTypeDTOS = staffTypes.stream()
                .map(this::convertToStaffTypeDTO)
                .toList();
        model.addAttribute("staffTypes", staffTypeDTOS);
        return staffTypeDTOS;
    }

    private StaffTypeDTO convertToStaffTypeDTO(StaffType staffType) {
        StaffTypeDTO dto = new StaffTypeDTO();
        dto.setId(staffType.getId());
        dto.setTypeName(staffType.getTypeName());
        dto.setDescription(staffType.getDescription());
        return dto;
    }

    @Override
    public List<FacultyDTO> getFaculties(Model model) {
        List<Faculty> faculties = facultyRepository.findAll();
        List<FacultyDTO> facultyDTOS = faculties.stream()
                .map(this::convertToFacultyDTO)
                .toList();
        model.addAttribute("faculties", facultyDTOS);
        return facultyDTOS;
    }

    private FacultyDTO convertToFacultyDTO(Faculty faculty) {
        FacultyDTO dto = new FacultyDTO();
        dto.setId(faculty.getId());
        dto.setName(faculty.getName());
        dto.setDisplayName(faculty.getDisplayName());
        return dto;
    }

    @Override
    public String updateEmployee(EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.findById(employeeDTO.getId()).orElseThrow(() -> new RuntimeException("Employee not found"));

        // Only check for duplicates if critical fields have changed
        boolean empNoChanged = !Objects.equals(employee.getEmpNo(), employeeDTO.getEmpNo());
        boolean phoneChanged = !Objects.equals(employee.getPhoneNumber(), employeeDTO.getPhoneNumber());
        boolean emailChanged = !Objects.equals(employee.getEmail(), employeeDTO.getEmail());
        
        if (empNoChanged || phoneChanged || emailChanged) {
            // Check for duplicates only for the fields that have changed
            if (empNoChanged && employeeRepository.existsByEmpNoAndIdNot(employeeDTO.getEmpNo(), employeeDTO.getId())) {
                return "DUPLICATE";
            }
            
            if (phoneChanged && employeeRepository.existsByPhoneNumberAndIdNot(employeeDTO.getPhoneNumber(), employeeDTO.getId())) {
                return "DUPLICATE";
            }
            
            if (emailChanged && employeeRepository.existsByEmailAndIdNot(employeeDTO.getEmail(), employeeDTO.getId())) {
                return "DUPLICATE";
            }
        }

        // Update basic employee information
        employee.setTitle(employeeDTO.getTitle());
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setEmpNo(employeeDTO.getEmpNo());
        employee.setDesignation(employeeDTO.getDesignation());
        employee.setQualification(employeeDTO.getQualification());
        employee.setPhoneNumber(employeeDTO.getPhoneNumber());
        employee.setEmail(employeeDTO.getEmail());
        employee.setNic(employeeDTO.getNic());
        
        // Update department if provided
        if (employeeDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
            employee.setDepartment(department);
        }
        
        // Update staff type if provided
        if (employeeDTO.getStaffTypeId() != null) {
            StaffType staffType = staffTypeRepository.findById(employeeDTO.getStaffTypeId())
                .orElseThrow(() -> new RuntimeException("Staff type not found"));
            employee.setStaffType(staffType);
        }
        
        // Note: Faculty is managed through the department relationship, not directly on employee

        // Update image if provided
        if (employeeDTO.getImage() != null && !employeeDTO.getImage().isEmpty()) {
            String[] fileWriteResults = imageUploadService.getResultsOfFileWrite(employeeDTO.getImage());
            Optional.ofNullable(fileWriteResults).ifPresent(results -> {
                employee.setFilePath(results[1]);
                employee.setFileName(results[2]);
            });
        }

        // Set update timestamp
        employee.setUpdateDate(new Date());

        employeeRepository.save(employee);
        return "SUCCESS";
    }

    @Override
    public EmployeeDTO getEmployeeESignatureDetails(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + id));

        EmployeeDTO employeeDTO = modelMapper.map(employee, EmployeeDTO.class);

        // Set profile image as base64 if exists
        if (employee.getFileName() != null && !employee.getFileName().isEmpty()) {
            try {
                // Read image file and convert to base64 using fileName
                byte[] imageBytes = imageUploadService.readImageFile(employee.getFileName());
                if (imageBytes != null) {
                    String base64Image = Base64.encodeBase64String(imageBytes);
                    employeeDTO.setProfileImageBase64("data:image/jpeg;base64," + base64Image);
                }
            } catch (Exception e) {
                // Log error and set default image or empty
                System.err.println("Error setting profile image for employee ID: " + id + " - " + e.getMessage());
                employeeDTO.setProfileImageBase64("");
            }
        } else {
            employeeDTO.setProfileImageBase64("");
        }

        return employeeDTO;
    }

    @Override
    public Page<EmployeeDTO> getPrintedEmployees(int page, int size, String printLabel, String batchId) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Employee> spec = Specification.where(null);
        
        spec = spec.and((root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.isTrue(root.get("active")),
                criteriaBuilder.isTrue(root.get("printed"))
            ));
        
        if (printLabel != null && !printLabel.trim().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.like(criteriaBuilder.lower(root.get("printLabel")), 
                    "%" + printLabel.toLowerCase() + "%"));
        }
        
        if (batchId != null && !batchId.trim().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.like(criteriaBuilder.lower(root.get("batchId")), 
                    "%" + batchId.toLowerCase() + "%"));
        }
        
        Page<Employee> employeePage = employeeRepository.findAll(spec, pageable);
        return employeePage.map(employee -> {
            EmployeeDTO employeeDTO = modelMapper.map(employee, EmployeeDTO.class);
            try {
                String qrData = String.format(
                    "empNo : %s , name : %s , designation : %s",
                    employeeDTO.getEmpNo(),
                    employeeDTO.getDisplayName(),
                    employeeDTO.getDesignation()
                );
                byte[] qrCode = QRCodeUtil.generateQRCode(qrData);
                employeeDTO.setQr(qrCode);
                employeeDTO.setQrBase64(Base64.encodeBase64String(qrCode));
            } catch (Exception e) {
                employeeDTO.setQrBase64("");
            }
            return employeeDTO;
        });
    }

    @Override
    public Page<EmployeeDTO> getPendingEmployees(int page, int size, String printLabel, String batchId) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Employee> spec = Specification.where(null);
        
        spec = spec.and((root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.isTrue(root.get("active")),
                criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("printed")),
                    criteriaBuilder.isFalse(root.get("printed"))
                )
            ));
        
        if (printLabel != null && !printLabel.trim().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.like(criteriaBuilder.lower(root.get("printLabel")), 
                    "%" + printLabel.toLowerCase() + "%"));
        }
        
        if (batchId != null && !batchId.trim().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.like(criteriaBuilder.lower(root.get("batchId")), 
                    "%" + batchId.toLowerCase() + "%"));
        }
        
        Page<Employee> employeePage = employeeRepository.findAll(spec, pageable);
        return employeePage.map(employee -> {
            EmployeeDTO employeeDTO = modelMapper.map(employee, EmployeeDTO.class);
            try {
                String qrData = String.format(
                    "empNo : %s , name : %s , designation : %s",
                    employeeDTO.getEmpNo(),
                    employeeDTO.getDisplayName(),
                    employeeDTO.getDesignation()
                );
                byte[] qrCode = QRCodeUtil.generateQRCode(qrData);
                employeeDTO.setQr(qrCode);
                employeeDTO.setQrBase64(Base64.encodeBase64String(qrCode));
            } catch (Exception e) {
                employeeDTO.setQrBase64("");
            }
            return employeeDTO;
        });
    }

    @Override
    @Transactional
    public String markEmployeeAsPrinted(Long employeeId, String printLabel, String batchId) {
        try {
            Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
            
            employee.setPrinted(true);
            employee.setPrintedDate(new Date());
            employee.setPrintLabel(printLabel);
            employee.setBatchId(batchId);
            
            employeeRepository.save(employee);
            return "SUCCESS";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    @Transactional
    public String markBatchAsPrinted(String batchId, String printLabel) {
        try {
            List<Employee> employees = employeeRepository.findByBatchIdAndActiveTrue(batchId);
            
            for (Employee employee : employees) {
                employee.setPrinted(true);
                employee.setPrintedDate(new Date());
                employee.setPrintLabel(printLabel);
            }
            
            employeeRepository.saveAll(employees);
            return "SUCCESS";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    @Transactional
    public String deleteEmployee(Long employeeId) {
        try {
            Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
            
            // Soft delete - set employee as inactive instead of hard delete
            employee.setActive(false);
            employee.setUpdateDate(new Date());
            
            employeeRepository.save(employee);
            log.info("Employee with ID {} has been marked as inactive (soft deleted)", employeeId);
            return "SUCCESS";
        } catch (Exception e) {
            log.error("Error deleting employee with ID {}", employeeId, e);
            return "ERROR: " + e.getMessage();
        }
    }
}

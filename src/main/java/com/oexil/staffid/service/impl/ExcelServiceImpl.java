package com.oexil.staffid.service.impl;

import com.oexil.staffid.dto.staff.EmployeeDTO;
import com.oexil.staffid.model.staff.Department;
import com.oexil.staffid.model.staff.Faculty;
import com.oexil.staffid.model.staff.StaffType;
import com.oexil.staffid.repository.DepartmentRepository;
import com.oexil.staffid.repository.FacultyRepository;
import com.oexil.staffid.repository.StaffTypeRepository;
import com.oexil.staffid.service.ExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    private final StaffTypeRepository staffTypeRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;

    // Expected column headers in order
    private static final String[] EXPECTED_HEADERS = {
        "Title", "FirstName", "LastName", "Emp No", "Qualification", 
        "Designation", "Phone Number", "Email Address", "Staff Type", 
        "Faculty", "Department"
    };

    @Override
    public List<EmployeeDTO> parseEmployeeExcel(MultipartFile file) throws Exception {
        List<EmployeeDTO> employees = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            
            // Validate headers
            if (rows.hasNext()) {
                Row headerRow = rows.next();
                validateHeaders(headerRow);
            } else {
                throw new IllegalArgumentException("Excel file is empty");
            }

            int rowNumber = 1; // Start from 1 since 0 is header
            while (rows.hasNext()) {
                Row row = rows.next();
                rowNumber++;
                
                try {
                    EmployeeDTO employee = parseEmployeeRow(row, rowNumber);
                    if (employee != null) {
                        employees.add(employee);
                    }
                } catch (Exception e) {
                    throw new Exception("Error processing row " + rowNumber + ": " + e.getMessage(), e);
                }
            }
        }
        
        log.info("Successfully parsed {} employees from Excel file", employees.size());
        return employees;
    }

    @Override
    public List<EmployeeDTO> parseEmployeeExcelWithIds(MultipartFile file) throws Exception {
        List<EmployeeDTO> employees = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            
            // Validate headers
            if (rows.hasNext()) {
                Row headerRow = rows.next();
                validateHeaders(headerRow);
            } else {
                throw new IllegalArgumentException("Excel file is empty");
            }

            int rowNumber = 1; // Start from 1 since 0 is header
            while (rows.hasNext()) {
                Row row = rows.next();
                rowNumber++;
                
                try {
                    EmployeeDTO employee = parseEmployeeRowWithIds(row, rowNumber);
                    if (employee != null) {
                        employees.add(employee);
                    }
                } catch (Exception e) {
                    throw new Exception("Error processing row " + rowNumber + ": " + e.getMessage(), e);
                }
            }
        }
        
        log.info("Successfully parsed {} employees from Excel file with ID-based lookups", employees.size());
        return employees;
    }

    @Override
    public boolean validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        
        return filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls");
    }

    private void validateHeaders(Row headerRow) throws IllegalArgumentException {
        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            Cell cell = headerRow.getCell(i);
            String cellValue = getCellValue(cell).trim();
            
            if (!EXPECTED_HEADERS[i].equalsIgnoreCase(cellValue)) {
                throw new IllegalArgumentException(
                    String.format("Invalid header at column %d. Expected '%s' but found '%s'", 
                                i + 1, EXPECTED_HEADERS[i], cellValue));
            }
        }
    }

    private EmployeeDTO parseEmployeeRow(Row row, int rowNumber) throws Exception {
        // Skip empty rows
        if (isRowEmpty(row)) {
            return null;
        }

        EmployeeDTO employee = new EmployeeDTO();
        List<String> errors = new ArrayList<>();

        try {
            // Parse basic fields
            employee.setTitle(getCellValue(row.getCell(0)).trim());
            employee.setFirstName(getCellValue(row.getCell(1)).trim());
            employee.setLastName(getCellValue(row.getCell(2)).trim());
            employee.setEmpNo(getCellValue(row.getCell(3)).trim());
            employee.setQualification(getCellValue(row.getCell(4)).trim());
            employee.setDesignation(getCellValue(row.getCell(5)).trim());
            employee.setPhoneNumber(getCellValue(row.getCell(6)).trim());
            employee.setEmail(getCellValue(row.getCell(7)).trim());

            // Validate required fields
            if (employee.getEmpNo().isEmpty()) {
                errors.add("Employee No is required");
            }
            if (employee.getFirstName().isEmpty() && employee.getLastName().isEmpty()) {
                errors.add("At least First Name or Last Name is required");
            }
            if (employee.getDesignation().isEmpty()) {
                errors.add("Designation is required");
            }

            // Validate phone number - required and must be exactly 10 digits
            if (employee.getPhoneNumber().isEmpty()) {
                errors.add("Phone number is required");
            } else if (!employee.getPhoneNumber().matches("\\d{10}")) {
                errors.add("Phone number must be exactly 10 digits");
            }

            // Parse and validate Staff Type
            String staffTypeName = getCellValue(row.getCell(8)).trim();
            if (!staffTypeName.isEmpty()) {
                StaffType staffType = staffTypeRepository.findByTypeNameIgnoreCase(staffTypeName);
                if (staffType == null) {
                    errors.add("Invalid Staff Type: " + staffTypeName);
                } else {
                    employee.setStaffTypeId(staffType.getId());
                }
            }

            // Parse and validate Faculty
            String facultyName = getCellValue(row.getCell(9)).trim();
            if (!facultyName.isEmpty()) {
                Faculty faculty = facultyRepository.findByNameIgnoreCase(facultyName);
                if (faculty == null) {
                    errors.add("Invalid Faculty: " + facultyName);
                } else {
                    employee.setFacultyId(faculty.getId());
                }
            }

            // Parse and validate Department
            String departmentName = getCellValue(row.getCell(10)).trim();
            if (!departmentName.isEmpty()) {
                Department department = departmentRepository.findByNameIgnoreCase(departmentName);
                if (department == null) {
                    errors.add("Invalid Department: " + departmentName);
                } else {
                    employee.setDepartmentId(department.getId());
                }
            }

            if (!errors.isEmpty()) {
                throw new IllegalArgumentException("Validation errors: " + String.join(", ", errors));
            }

            return employee;

        } catch (Exception e) {
            throw new Exception("Row " + rowNumber + ": " + e.getMessage(), e);
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    // Handle numbers as strings to preserve format (like employee numbers)
                    double numericValue = cell.getNumericCellValue();
                    // If it's a whole number, return as integer string
                    if (numericValue == Math.floor(numericValue)) {
                        yield String.valueOf((long) numericValue);
                    } else {
                        yield String.valueOf(numericValue);
                    }
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf(cell.getStringCellValue());
                } catch (Exception e) {
                    try {
                        yield String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        yield "";
                    }
                }
            }
            default -> "";
        };
    }

    private EmployeeDTO parseEmployeeRowWithIds(Row row, int rowNumber) throws Exception {
        // Skip empty rows
        if (isRowEmpty(row)) {
            return null;
        }

        EmployeeDTO employee = new EmployeeDTO();
        List<String> errors = new ArrayList<>();

        try {
            // Parse basic fields
            employee.setTitle(getCellValue(row.getCell(0)).trim());
            employee.setFirstName(getCellValue(row.getCell(1)).trim());
            employee.setLastName(getCellValue(row.getCell(2)).trim());
            employee.setEmpNo(getCellValue(row.getCell(3)).trim());
            employee.setQualification(getCellValue(row.getCell(4)).trim());
            employee.setDesignation(getCellValue(row.getCell(5)).trim());
            employee.setPhoneNumber(getCellValue(row.getCell(6)).trim());
            employee.setEmail(getCellValue(row.getCell(7)).trim());

            // Validate required fields
            if (employee.getEmpNo().isEmpty()) {
                errors.add("Employee No is required");
            }
            if (employee.getFirstName().isEmpty() && employee.getLastName().isEmpty()) {
                errors.add("At least First Name or Last Name is required");
            }
            if (employee.getDesignation().isEmpty()) {
                errors.add("Designation is required");
            }

            // Validate phone number - required and must be exactly 10 digits
            if (employee.getPhoneNumber().isEmpty()) {
                errors.add("Phone number is required");
            } else if (!employee.getPhoneNumber().matches("\\d{10}")) {
                errors.add("Phone number must be exactly 10 digits");
            }

            // Parse and validate Staff Type ID
            String staffTypeIdStr = getCellValue(row.getCell(8)).trim();
            if (!staffTypeIdStr.isEmpty()) {
                try {
                    Long staffTypeId = Long.parseLong(staffTypeIdStr);
                    if (staffTypeRepository.existsById(staffTypeId)) {
                        employee.setStaffTypeId(staffTypeId);
                    } else {
                        errors.add("Invalid Staff Type ID: " + staffTypeId);
                    }
                } catch (NumberFormatException e) {
                    errors.add("Staff Type ID must be a valid number: " + staffTypeIdStr);
                }
            }

            // Parse and validate Faculty ID
            String facultyIdStr = getCellValue(row.getCell(9)).trim();
            if (!facultyIdStr.isEmpty()) {
                try {
                    Long facultyId = Long.parseLong(facultyIdStr);
                    if (facultyRepository.existsById(facultyId)) {
                        employee.setFacultyId(facultyId);
                    } else {
                        errors.add("Invalid Faculty ID: " + facultyId);
                    }
                } catch (NumberFormatException e) {
                    errors.add("Faculty ID must be a valid number: " + facultyIdStr);
                }
            }

            // Parse and validate Department ID
            String departmentIdStr = getCellValue(row.getCell(10)).trim();
            if (!departmentIdStr.isEmpty()) {
                try {
                    Long departmentId = Long.parseLong(departmentIdStr);
                    if (departmentRepository.existsById(departmentId)) {
                        employee.setDepartmentId(departmentId);
                    } else {
                        errors.add("Invalid Department ID: " + departmentId);
                    }
                } catch (NumberFormatException e) {
                    errors.add("Department ID must be a valid number: " + departmentIdStr);
                }
            }

            if (!errors.isEmpty()) {
                throw new IllegalArgumentException("Validation errors: " + String.join(", ", errors));
            }

            return employee;

        } catch (Exception e) {
            throw new Exception("Row " + rowNumber + ": " + e.getMessage(), e);
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        
        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !getCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
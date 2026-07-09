package com.oexil.staffid.service;

import com.oexil.staffid.dto.staff.EmployeeDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExcelService {
    
    /**
     * Parse Excel file and return list of EmployeeDTO objects
     * @param file Excel file containing staff data
     * @return List of parsed employee data
     * @throws Exception if file parsing fails
     */
    List<EmployeeDTO> parseEmployeeExcel(MultipartFile file) throws Exception;
    
    /**
     * Parse Excel file with ID-based lookups and return list of EmployeeDTO objects
     * @param file Excel file containing staff data with IDs for Staff Type, Faculty, Department
     * @return List of parsed employee data
     * @throws Exception if file parsing fails
     */
    List<EmployeeDTO> parseEmployeeExcelWithIds(MultipartFile file) throws Exception;
    
    /**
     * Validate Excel file format and structure
     * @param file Excel file to validate
     * @return true if file is valid, false otherwise
     */
    boolean validateExcelFile(MultipartFile file);
}
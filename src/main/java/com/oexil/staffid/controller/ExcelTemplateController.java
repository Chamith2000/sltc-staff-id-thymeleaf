package com.oexil.staffid.controller;

import com.oexil.staffid.repository.DepartmentRepository;
import com.oexil.staffid.repository.FacultyRepository;
import com.oexil.staffid.repository.StaffTypeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class ExcelTemplateController {

    private final StaffTypeRepository staffTypeRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;

    @GetMapping("/master-data-reference")
    @ResponseBody
    public ResponseEntity<String> getMasterDataReference() {
        StringBuilder sb = new StringBuilder();
        sb.append("MASTER DATA REFERENCE FOR BULK UPLOAD\n");
        sb.append("=====================================\n\n");
        
        sb.append("STAFF TYPES:\n");
        staffTypeRepository.findAll().forEach(st -> 
            sb.append(String.format("ID: %d - %s\n", st.getId(), st.getTypeName())));
        
        sb.append("\nFACULTIES:\n");
        facultyRepository.findAll().forEach(f -> 
            sb.append(String.format("ID: %d - %s\n", f.getId(), f.getName())));
        
        sb.append("\nDEPARTMENTS:\n");
        departmentRepository.findAll().forEach(d -> 
            sb.append(String.format("ID: %d - %s (Staff Type: %s, Faculty: %s)\n", 
                     d.getId(), d.getName(), 
                     d.getStaffType() != null ? d.getStaffType().getTypeName() : "N/A",
                     d.getFaculty() != null ? d.getFaculty().getName() : "N/A")));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "master_data_reference.txt");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(sb.toString());
    }

    @GetMapping("/download-template")
    public ResponseEntity<byte[]> downloadExcelTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Staff Data");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Title", "FirstName", "LastName", "Emp No", "Qualification", 
                "Designation", "Phone Number", "Email Address", "Staff Type", 
                "Faculty", "Department"
            };

            // Style for header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Add sample data row
            Row sampleRow = sheet.createRow(1);
            String[] sampleData = {
                "Dr.", "John", "Doe", "EMP001", "PhD Computer Science", 
                "Senior Lecturer", "0771234567", "john.doe@university.edu", "1", 
                "1", "1"
            };

            for (int i = 0; i < sampleData.length; i++) {
                Cell cell = sampleRow.createCell(i);
                cell.setCellValue(sampleData[i]);
            }

            // Add instructions row
            Row instructionsRow = sheet.createRow(2);
            String[] instructions = {
                "Title: Mr./Miss./Dr./Prof.", "Required field", "Required field", "Required unique identifier", "Education qualification", 
                "Job position/title", "Required: exactly 10 digits", "Valid email address", "Staff Type ID (from master data)", 
                "Faculty ID (from master data)", "Department ID (from master data)"
            };

            CellStyle instructionStyle = workbook.createCellStyle();
            Font instructionFont = workbook.createFont();
            instructionFont.setItalic(true);
            instructionFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            instructionStyle.setFont(instructionFont);

            for (int i = 0; i < instructions.length; i++) {
                Cell cell = instructionsRow.createCell(i);
                cell.setCellValue(instructions[i]);
                cell.setCellStyle(instructionStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            responseHeaders.setContentDispositionFormData("attachment", "staff_upload_template.xlsx");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(out.toByteArray());
        }
    }
}
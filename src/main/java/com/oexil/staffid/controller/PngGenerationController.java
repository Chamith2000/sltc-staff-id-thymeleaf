package com.oexil.staffid.controller;

import com.oexil.staffid.dto.staff.EmployeeDTO;
import com.oexil.staffid.service.EmployeeService;
import com.oexil.staffid.service.PngGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/png-generation")
@RequiredArgsConstructor
@Slf4j
public class PngGenerationController {

    private final PngGenerationService pngGenerationService;
    private final EmployeeService employeeService;

    @PostMapping("/generate-signature/{employeeId}")
    public ResponseEntity<Map<String, Object>> generateSignaturePng(@PathVariable Long employeeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get employee details
            EmployeeDTO employee = employeeService.getEmployeeById(employeeId);
            if (employee == null) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate employee data
            String printedName = employee.getPrintedName();
            String empNo = employee.getEmpNo();
            
            if (printedName == null || printedName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Employee name is missing or empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (empNo == null || empNo.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Employee number is missing or empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Generate PNG
            String outputPath = pngGenerationService.generateSignaturePngWithClipping(
                employeeId, 
                printedName.trim(), 
                empNo.trim()
            );
            
            response.put("success", true);
            response.put("message", "PNG generated successfully");
            response.put("filePath", outputPath);
            response.put("downloadUrl", "/api/png-generation/download/" + employeeId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error generating PNG for employee {}", employeeId, e);
            response.put("success", false);
            response.put("message", "Error generating PNG: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/download/{employeeId}")
    public ResponseEntity<Resource> downloadSignaturePng(@PathVariable Long employeeId) {
        try {
            // Get employee details for filename
            EmployeeDTO employee = employeeService.getEmployeeById(employeeId);
            if (employee == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Validate employee data and construct expected filename
            String printedName = employee.getPrintedName();
            String empNo = employee.getEmpNo();
            
            if (printedName == null || printedName.trim().isEmpty() || empNo == null || empNo.trim().isEmpty()) {
                log.error("Employee data incomplete for ID {}: name='{}', empNo='{}'", employeeId, printedName, empNo);
                return ResponseEntity.badRequest().build();
            }
            
            // Use same sanitization as PngGenerationService
            String sanitizedName = printedName.trim().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_");
            String sanitizedEmpNo = empNo.trim().replaceAll("[^a-zA-Z0-9]", "");
            String filename = String.format("%s_%s_signature.png", sanitizedName, sanitizedEmpNo);
            
            // Get output directory from service (you might want to make this configurable)
            String outputDirectory = System.getProperty("java.io.tmpdir") + "/signature-png";
            File file = new File(outputDirectory, filename);
            
            if (!file.exists()) {
                log.warn("PNG file not found: {}", file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + filename + "\"")
                .body(resource);
                
        } catch (Exception e) {
            log.error("Error downloading PNG for employee {}", employeeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/debug/{employeeId}")
    public ResponseEntity<Map<String, Object>> debugEmployeeData(@PathVariable Long employeeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            EmployeeDTO employee = employeeService.getEmployeeById(employeeId);
            if (employee == null) {
                response.put("found", false);
                response.put("message", "Employee not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("found", true);
            response.put("employeeId", employeeId);
            response.put("firstName", employee.getFirstName());
            response.put("lastName", employee.getLastName());
            response.put("fullName", employee.getFullName());
            response.put("printedName", employee.getPrintedName());
            response.put("empNo", employee.getEmpNo());
            response.put("title", employee.getTitle());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error debugging employee data for ID {}", employeeId, e);
            response.put("found", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/status/{employeeId}")
    public ResponseEntity<Map<String, Object>> checkPngStatus(@PathVariable Long employeeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            EmployeeDTO employee = employeeService.getEmployeeById(employeeId);
            if (employee == null) {
                response.put("exists", false);
                response.put("message", "Employee not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate employee data and construct filename
            String printedName = employee.getPrintedName();
            String empNo = employee.getEmpNo();
            
            if (printedName == null || printedName.trim().isEmpty() || empNo == null || empNo.trim().isEmpty()) {
                response.put("exists", false);
                response.put("message", "Employee data incomplete");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Use same sanitization as PngGenerationService
            String sanitizedName = printedName.trim().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_");
            String sanitizedEmpNo = empNo.trim().replaceAll("[^a-zA-Z0-9]", "");
            String filename = String.format("%s_%s_signature.png", sanitizedName, sanitizedEmpNo);
            
            String outputDirectory = System.getProperty("java.io.tmpdir") + "/signature-png";
            File file = new File(outputDirectory, filename);
            
            response.put("exists", file.exists());
            response.put("filename", filename);
            response.put("lastModified", file.exists() ? file.lastModified() : null);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking PNG status for employee {}", employeeId, e);
            response.put("exists", false);
            response.put("message", "Error checking status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
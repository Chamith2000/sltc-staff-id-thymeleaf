package com.oexil.staffid.controller;

import com.oexil.staffid.dto.staff.DepartmentDTO;
import com.oexil.staffid.dto.staff.EmployeeDTO;
import com.oexil.staffid.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping(value = "staff")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/employee-list")
    public String employeeHome(Model model, 
                              @RequestParam(defaultValue = "0") int page, 
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) Long staffTypeId,
                              @RequestParam(required = false) Long departmentId,
                              @RequestParam(required = false) Long facultyId,
                              @RequestParam(required = false) String designation) {
        employeeService.getAllEmployees(model, page, size, staffTypeId, departmentId, facultyId, designation);
        
        // Add filter options to model
        employeeService.getStaffTypes(model);
        employeeService.getAllDepartments(model);
        employeeService.getFaculties(model);
        
        // Add current filter values to model
        model.addAttribute("currentStaffTypeId", staffTypeId);
        model.addAttribute("currentDepartmentId", departmentId);
        model.addAttribute("currentFacultyId", facultyId);
        model.addAttribute("currentDesignation", designation);
        
        return "staff/employee/list";
    }

    @GetMapping("/employee-add")
    public String createEmployee(Model model) {
        model.addAttribute("staffDTO", new EmployeeDTO());
        employeeService.getAllDepartments(model);
        employeeService.getStaffTypes(model);
        employeeService.getFaculties(model);
        return "staff/employee/create";
    }

    @PostMapping("/employee-register")
    public String registerEmployee(@Valid @ModelAttribute("staffDTO") EmployeeDTO staffDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Validation errors occurred");
            employeeService.getAllDepartments(model);
            employeeService.getStaffTypes(model);
            employeeService.getFaculties(model);
            return "staff/employee/create";
        }

        String result = employeeService.registerStaff(staffDTO);

        if ("DUPLICATE".equals(result)) {
            model.addAttribute("error", "Staff with this phone number or nic or emp no already exists");
            employeeService.getAllDepartments(model);
            employeeService.getStaffTypes(model);
            employeeService.getFaculties(model);
            return "staff/employee/create";
        } else {
            redirectAttributes.addFlashAttribute("success", "Staff registered successfully");
            return "redirect:/staff/employee-list";
        }
    }

    // Edit Employee Page
    @GetMapping("/edit/{id}")
    public String editEmployee(@PathVariable Long id, Model model) {
        EmployeeDTO employeeDTO = employeeService.getEmployeeById(id);
        model.addAttribute("employeeDTO", employeeDTO);
        employeeService.getAllDepartments(model);
        employeeService.getStaffTypes(model);
        employeeService.getFaculties(model);
        return "staff/employee/edit";
    }

    @PostMapping("/update")
    public String updateEmployee(@ModelAttribute("employeeDTO") EmployeeDTO employeeDTO,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            model.addAttribute("error", "Error: Employee update failed with " + errors);
            employeeService.getAllDepartments(model);
            employeeService.getStaffTypes(model);
            employeeService.getFaculties(model);
            return "staff/employee/edit";
        }

        String result = employeeService.updateEmployee(employeeDTO);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Employee updated successfully");
        } else if ("DUPLICATE".equals(result)) {
            model.addAttribute("error", "Employee with this phone number or employee number already exists");
            employeeService.getAllDepartments(model);
            employeeService.getStaffTypes(model);
            employeeService.getFaculties(model);
            return "staff/employee/edit";
        } else if ("CONFIRMED".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Employee updated successfully");
            return "redirect:/confirmation/pending-list";
        } else {
            redirectAttributes.addFlashAttribute("error", "Error: Employee update failed with " + result);
        }
        return "redirect:/staff/employee-list";
    }

    @PostMapping("/upload-employee-excel")
    public String uploadEmployeeExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            // Validate file upload
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select an Excel file to upload.");
                return "redirect:/staff/employee-list";
            }

            // Validate file type
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
                redirectAttributes.addFlashAttribute("error", "Please upload a valid Excel file (.xlsx or .xls)");
                return "redirect:/staff/employee-list";
            }

            // Process the file
            String result = employeeService.registerEmployeeBatch(file);
            
            if (result.startsWith("SUCCESS")) {
                redirectAttributes.addFlashAttribute("success", result.replace("SUCCESS: ", ""));
            } else {
                redirectAttributes.addFlashAttribute("error", result.replace("ERROR: ", ""));
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }

        return "redirect:/staff/employee-list";
    }

    @PostMapping("/bulk-register")
    public String bulkRegisterEmployees(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            // Validate file upload
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select an Excel file to upload.");
                return "redirect:/staff/employee-add";
            }

            // Validate file type
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
                redirectAttributes.addFlashAttribute("error", "Please upload a valid Excel file (.xlsx or .xls)");
                return "redirect:/staff/employee-add";
            }

            // Process the file
            String result = employeeService.bulkRegisterEmployees(file);
            
            if (result.startsWith("SUCCESS")) {
                redirectAttributes.addFlashAttribute("success", result.replace("SUCCESS: ", ""));
            } else {
                redirectAttributes.addFlashAttribute("error", result.replace("ERROR: ", ""));
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }

        return "redirect:/staff/employee-add";
    }

    @GetMapping("/generate-id/{id}")
    public String generateIdCard(@PathVariable Long id, Model model) {
        Page<EmployeeDTO> studentDTOPage = new PageImpl<>(List.of(employeeService.getEmployeeById(id)), PageRequest.of(0, 1), 1);
        model.addAttribute("employees", studentDTOPage);

        return "staff/id/id-card";
    }

    @GetMapping("/generate-all-ids")
    public String generateAllIdCards(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<EmployeeDTO> pendingEmployees = employeeService.getPendingEmployees(page, size, null, null);
        model.addAttribute("employees", pendingEmployees);
        return "staff/id/id-card";
    }

    @GetMapping("/generate-eSignature/{id}")
    public String generateESignature(@PathVariable Long id, Model model) {
        Page<EmployeeDTO> employeeDTOPage = new PageImpl<>(List.of(employeeService.getEmployeeById(id)), PageRequest.of(0, 1), 1);
        model.addAttribute("employees", employeeDTOPage);
        return "staff/eSignature/e-signature";
    }

    @GetMapping("/signature-details/{id}")
    public String signatureDetails(@PathVariable Long id, Model model) {
        EmployeeDTO employeeDTO = employeeService.getEmployeeById(id);
        model.addAttribute("employee", employeeDTO);
        model.addAttribute("employeeId", id);
        return "staff/eSignature/signature-details";
    }

    // Delete Employee endpoint
    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            String result = employeeService.deleteEmployee(id);
            if ("SUCCESS".equals(result)) {
                redirectAttributes.addFlashAttribute("success", "Employee deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete employee: " + result);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting employee: " + e.getMessage());
        }
        return "redirect:/staff/employee-list";
    }

    // API endpoint for fetching departments by staff type
    @GetMapping("/api/departments")
    @ResponseBody
    public ResponseEntity<List<DepartmentDTO>> getDepartmentsByStaffType(@RequestParam(required = false) Long staffTypeId) {
        List<DepartmentDTO> departments = employeeService.getDepartmentsByStaffType(staffTypeId);
        return ResponseEntity.ok(departments);
    }
}
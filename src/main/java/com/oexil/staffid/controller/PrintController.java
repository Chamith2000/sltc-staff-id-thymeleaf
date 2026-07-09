package com.oexil.staffid.controller;

import com.oexil.staffid.dto.staff.EmployeeDTO;
import com.oexil.staffid.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/print")
@RequiredArgsConstructor
public class PrintController {

    private final EmployeeService employeeService;

    @GetMapping("/printed-list")
    public String printedList(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) {
        
        Page<EmployeeDTO> printedEmployees = employeeService.getPrintedEmployees(page, size, null, null);
        model.addAttribute("employees", printedEmployees);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        
        return "print/printed-list";
    }

    @GetMapping("/pending-list")
    public String pendingList(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) {
        
        Page<EmployeeDTO> pendingEmployees = employeeService.getPendingEmployees(page, size, null, null);
        model.addAttribute("employees", pendingEmployees);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        
        return "print/pending-list";
    }

    @PostMapping("/mark-as-printed")
    public String markAsPrinted(@RequestParam Long employeeId,
                               @RequestParam String printLabel,
                               @RequestParam String batchId,
                               RedirectAttributes redirectAttributes) {
        
        String result = employeeService.markEmployeeAsPrinted(employeeId, printLabel, batchId);
        
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Employee marked as printed successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to mark employee as printed: " + result);
        }
        
        return "redirect:/print/pending-list";
    }

    @PostMapping("/mark-batch-as-printed")
    public String markBatchAsPrinted(@RequestParam String batchId,
                                   @RequestParam String printLabel,
                                   RedirectAttributes redirectAttributes) {
        
        String result = employeeService.markBatchAsPrinted(batchId, printLabel);
        
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Batch marked as printed successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to mark batch as printed: " + result);
        }
        
        return "redirect:/print/pending-list";
    }
}
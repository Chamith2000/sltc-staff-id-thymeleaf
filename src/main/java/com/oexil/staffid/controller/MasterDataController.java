package com.oexil.staffid.controller;

import com.oexil.staffid.dto.staff.DepartmentDTO;
import com.oexil.staffid.dto.staff.FacultyDTO;
import com.oexil.staffid.dto.staff.StaffTypeDTO;
import com.oexil.staffid.service.MasterDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Controller
@RequestMapping("/admin/master-data")
@RequiredArgsConstructor
public class MasterDataController {

    private final MasterDataService masterDataService;

    @GetMapping
    public String masterDataHome(Model model) {
        masterDataService.getAllMasterData(model);
        return "admin/master-data/index";
    }

    // API endpoint for fetching departments by staff type
    @GetMapping("/departments")
    @ResponseBody
    public ResponseEntity<List<DepartmentDTO>> getDepartmentsByStaffType(@RequestParam(required = false) Long staffTypeId) {
        List<DepartmentDTO> departments = masterDataService.getDepartmentsByStaffType(staffTypeId);
        return ResponseEntity.ok(departments);
    }

    // Department Management
    @GetMapping("/departments/list")
    public String departmentList(Model model) {
        masterDataService.getAllDepartments(model);
        return "admin/master-data/departments/list";
    }

    @GetMapping("/departments/create")
    public String createDepartment(Model model) {
        model.addAttribute("departmentDTO", new DepartmentDTO());
        masterDataService.getAllStaffTypes(model);
        masterDataService.getAllFaculties(model);
        return "admin/master-data/departments/create";
    }

    @PostMapping("/departments/save")
    public String saveDepartment(@Valid @ModelAttribute("departmentDTO") DepartmentDTO departmentDTO, 
                                BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Validation errors occurred");
            masterDataService.getAllStaffTypes(model);
            masterDataService.getAllFaculties(model);
            return "admin/master-data/departments/create";
        }

        String result = masterDataService.saveDepartment(departmentDTO);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Department saved successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error saving department: " + result);
        }
        return "redirect:/admin/master-data/departments/list";
    }

    @GetMapping("/departments/edit/{id}")
    public String editDepartment(@PathVariable Long id, Model model) {
        DepartmentDTO departmentDTO = masterDataService.getDepartmentById(id);
        model.addAttribute("departmentDTO", departmentDTO);
        masterDataService.getAllStaffTypes(model);
        masterDataService.getAllFaculties(model);
        return "admin/master-data/departments/edit";
    }

    @PostMapping("/departments/update")
    public String updateDepartment(@Valid @ModelAttribute("departmentDTO") DepartmentDTO departmentDTO,
                                  BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Validation errors occurred");
            masterDataService.getAllStaffTypes(model);
            masterDataService.getAllFaculties(model);
            return "admin/master-data/departments/edit";
        }

        String result = masterDataService.updateDepartment(departmentDTO);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Department updated successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error updating department: " + result);
        }
        return "redirect:/admin/master-data/departments/list";
    }

    @PostMapping("/departments/delete/{id}")
    public String deleteDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String result = masterDataService.deleteDepartment(id);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Department deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error deleting department: " + result);
        }
        return "redirect:/admin/master-data/departments/list";
    }

    // Faculty Management
    @GetMapping("/faculties")
    public String facultyList(Model model) {
        masterDataService.getAllFaculties(model);
        return "admin/master-data/faculties/list";
    }

    @GetMapping("/faculties/create")
    public String createFaculty(Model model) {
        model.addAttribute("facultyDTO", new FacultyDTO());
        return "admin/master-data/faculties/create";
    }

    @PostMapping("/faculties/save")
    public String saveFaculty(@Valid @ModelAttribute("facultyDTO") FacultyDTO facultyDTO,
                             BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Validation errors occurred");
            return "admin/master-data/faculties/create";
        }

        String result = masterDataService.saveFaculty(facultyDTO);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Faculty saved successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error saving faculty: " + result);
        }
        return "redirect:/admin/master-data/faculties";
    }

    @GetMapping("/faculties/edit/{id}")
    public String editFaculty(@PathVariable Long id, Model model) {
        FacultyDTO facultyDTO = masterDataService.getFacultyById(id);
        model.addAttribute("facultyDTO", facultyDTO);
        return "admin/master-data/faculties/edit";
    }

    @PostMapping("/faculties/update")
    public String updateFaculty(@Valid @ModelAttribute("facultyDTO") FacultyDTO facultyDTO,
                               BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Validation errors occurred");
            return "admin/master-data/faculties/edit";
        }

        String result = masterDataService.updateFaculty(facultyDTO);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Faculty updated successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error updating faculty: " + result);
        }
        return "redirect:/admin/master-data/faculties";
    }

    @PostMapping("/faculties/delete/{id}")
    public String deleteFaculty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String result = masterDataService.deleteFaculty(id);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Faculty deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error deleting faculty: " + result);
        }
        return "redirect:/admin/master-data/faculties";
    }

    // Staff Type Management
    @GetMapping("/staff-types")
    public String staffTypeList(Model model) {
        masterDataService.getAllStaffTypes(model);
        return "admin/master-data/staff-types/list";
    }

    @GetMapping("/staff-types/create")
    public String createStaffType(Model model) {
        model.addAttribute("staffTypeDTO", new StaffTypeDTO());
        return "admin/master-data/staff-types/create";
    }

    @PostMapping("/staff-types/save")
    public String saveStaffType(@Valid @ModelAttribute("staffTypeDTO") StaffTypeDTO staffTypeDTO,
                               BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Validation errors occurred");
            return "admin/master-data/staff-types/create";
        }

        String result = masterDataService.saveStaffType(staffTypeDTO);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Staff Type saved successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error saving staff type: " + result);
        }
        return "redirect:/admin/master-data/staff-types";
    }

    @GetMapping("/staff-types/edit/{id}")
    public String editStaffType(@PathVariable Long id, Model model) {
        StaffTypeDTO staffTypeDTO = masterDataService.getStaffTypeById(id);
        model.addAttribute("staffTypeDTO", staffTypeDTO);
        return "admin/master-data/staff-types/edit";
    }

    @PostMapping("/staff-types/update")
    public String updateStaffType(@Valid @ModelAttribute("staffTypeDTO") StaffTypeDTO staffTypeDTO,
                                 BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Validation errors occurred");
            return "admin/master-data/staff-types/edit";
        }

        String result = masterDataService.updateStaffType(staffTypeDTO);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Staff Type updated successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error updating staff type: " + result);
        }
        return "redirect:/admin/master-data/staff-types";
    }

    @PostMapping("/staff-types/delete/{id}")
    public String deleteStaffType(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String result = masterDataService.deleteStaffType(id);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Staff Type deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error deleting staff type: " + result);
        }
        return "redirect:/admin/master-data/staff-types";
    }
}
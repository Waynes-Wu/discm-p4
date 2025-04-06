package p4.mainservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import p4.mainservice.dto.CourseDTO;
import p4.mainservice.service.CourseService;

@Controller
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    public String listCourses(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
        return "courses/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("course", new CourseDTO());
        return "courses/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createCourse(@ModelAttribute CourseDTO courseDTO, RedirectAttributes redirectAttributes) {
        CourseDTO created = courseService.createCourse(courseDTO);
        if (created != null) {
            redirectAttributes.addFlashAttribute("message", "Course created successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to create course!");
        }
        return "redirect:/courses";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        CourseDTO course = courseService.getCourseById(id);
        if (course != null) {
            model.addAttribute("course", course);
            return "courses/form";
        }
        return "redirect:/courses";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateCourse(@PathVariable Long id, @ModelAttribute CourseDTO courseDTO,
            RedirectAttributes redirectAttributes) {
        CourseDTO updated = courseService.updateCourse(id, courseDTO);
        if (updated != null) {
            redirectAttributes.addFlashAttribute("message", "Course updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to update course!");
        }
        return "redirect:/courses";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (courseService.deleteCourse(id)) {
            redirectAttributes.addFlashAttribute("message", "Course deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to delete course!");
        }

        return "redirect:/courses";
    }
}

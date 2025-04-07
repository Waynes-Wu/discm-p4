package p4.mainservice.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController implements ErrorController {

    @GetMapping("/")
    public String home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            model.addAttribute("username", auth.getName());
            // For now, just adding placeholder data
            model.addAttribute("enrolledCourses", 0);
            model.addAttribute("currentGpa", "0.00");
            model.addAttribute("unitsCompleted", 0);
            model.addAttribute("teachingCourses", 0);
            model.addAttribute("totalStudents", 0);
            model.addAttribute("pendingGrades", 0);
            model.addAttribute("totalFaculty", 0);
            model.addAttribute("activeCourses", 0);
            model.addAttribute("totalEnrollments", 0);
            return "dashboard";
        }
        // * i dont think this works | fix added in securityconfig
        return "redirect:/login";
    }

    @RequestMapping("/error")
    public String handleError() {
        return "error";
    }
}

package p4.mainservice.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
// import p4.mainservice.service.EnrollmentService;

@Controller
@RequestMapping("/enrollment")
public class EnrollmentController {
    
    @Value("${enrollment.service.url}")
    private String enrollmentServiceUrl;


    @GetMapping
    public String showEnrollForm(@RequestParam(value = "courseId", required = false) String courseId, Model model) {
        model.addAttribute("courseId", courseId != null ? courseId : "");
        return "enrollment/forms";
    }


    @PostMapping
    public String processEnrollment(
        @RequestParam("courseId") Long courseId,
        HttpServletRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
    
        String email = auth.getName();
        String jwt = null;
    
        // Extract JWT cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }
    
        if (jwt == null) {
            return "redirect:/login";
        }
    
        
        // TODO: make a request to enroll 
        try {
            String url = enrollmentServiceUrl + "/api/enrollments";
    
            // Request body as a map (JSON automatically)
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("courseId", courseId);
            requestBody.put("email", email);
    
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(jwt);
    
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
    
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
    
            if (response.getStatusCode().is2xxSuccessful()) {
                return "redirect:/enrollment/success";
            } else {
                System.out.println("Enrollment failed: " + response.getStatusCode());
                return "redirect:/enrollment?error=1";
            }
    
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/enrollment?error=1";
        }

    }
}

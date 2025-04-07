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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
// import p4.mainservice.service.EnrollmentService;

@Controller
@RequestMapping("/enrollment")
public class EnrollmentController {

    @Value("${enrollment.service.url}")
    private String enrollmentServiceUrl;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public EnrollmentController(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String showEnrollForm(@RequestParam(value = "courseCode", required = false) String courseCode, Model model) {
        model.addAttribute("courseCode", courseCode != null ? courseCode : "");
        return "enrollment/forms";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processEnrollment(
            @RequestParam("courseCode") String courseCode,
            HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

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
            return ResponseEntity.status(401).body(Map.of("error", "No JWT token found"));
        }

        try {
            // First, get user info from auth service
            String authUrl = authServiceUrl + "/api/auth/user";
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setBearerAuth(jwt);
            HttpEntity<?> authEntity = new HttpEntity<>(authHeaders);

            ResponseEntity<String> authResponse = restTemplate.exchange(
                    authUrl,
                    org.springframework.http.HttpMethod.GET,
                    authEntity,
                    String.class);

            if (!authResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid authentication"));
            }

            // Parse the auth response to get user ID
            JsonNode authJson = objectMapper.readTree(authResponse.getBody());
            if (!authJson.has("id")) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID not found in token"));
            }
            Long userId = authJson.get("id").asLong();

            // Now make the enrollment request
            String enrollUrl = enrollmentServiceUrl + "/api/enrollments";

            // Create an enrollment DTO structure
            Map<String, Object> enrollmentDTO = new HashMap<>();
            enrollmentDTO.put("userId", userId);
            enrollmentDTO.put("courseCode", courseCode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(jwt);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(enrollmentDTO, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(enrollUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(Map.of("message", "Enrollment successful"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Enrollment failed: " + response.getBody()));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }
}

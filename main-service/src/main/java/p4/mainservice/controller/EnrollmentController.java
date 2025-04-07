package p4.mainservice.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                System.out.println("DEBUG: No ID found in auth response: " + authResponse.getBody());
                return ResponseEntity.badRequest().body(Map.of("error", "User ID not found in token"));
            }
            Long userId = authJson.get("id").asLong();
            System.out.println("DEBUG: Got user ID from auth service: " + userId);

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
                JsonNode errorResponse = objectMapper.readTree(response.getBody());
                String errorMessage = errorResponse.path("message").asText("Enrollment failed");
                return ResponseEntity.badRequest().body(Map.of("error", errorMessage));
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("User is already enrolled in this course")) {
                return ResponseEntity.badRequest().body(Map.of("error", "You've already enrolled in " + courseCode));
            }
            if (e.getMessage() != null && e.getMessage().contains("Course does not exist")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Course does not exist"));
            }

            return ResponseEntity.internalServerError().body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/enrolled")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getEnrolledCourses(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(null);
        }

        String jwt = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt == null) {
            return ResponseEntity.status(401).body(null);
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
                return ResponseEntity.status(401).body(null);
            }

            // Parse the auth response to get user ID
            JsonNode authJson = objectMapper.readTree(authResponse.getBody());
            if (!authJson.has("id")) {
                System.out.println("DEBUG: No ID found in auth response: " + authResponse.getBody());
                return ResponseEntity.badRequest().body(null);
            }
            Long userId = authJson.get("id").asLong();
            System.out.println("DEBUG: Got user ID from auth service: " + userId);

            // Get enrollments from enrollment service
            String enrollUrl = enrollmentServiceUrl + "/api/enrollments/student/" + userId;
            System.out.println("DEBUG: Calling enrollment service URL: " + enrollUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwt);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    enrollUrl,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class);

            System.out.println("DEBUG: Enrollment service response status: " + response.getStatusCode());
            System.out.println("DEBUG: Enrollment service response body: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                JsonNode enrollments = responseJson.path("data");
                System.out.println("DEBUG: Parsed enrollments data: " + enrollments);

                List<Map<String, Object>> enrolledCourses = new ArrayList<>();

                if (enrollments.isArray()) {
                    System.out.println("DEBUG: Number of enrollments found: " + enrollments.size());
                    enrollments.forEach(enrollment -> {
                        Map<String, Object> courseInfo = new HashMap<>();
                        courseInfo.put("courseCode", enrollment.path("courseCode").asText());
                        courseInfo.put("createdAt", enrollment.path("createdAt").asText());
                        enrolledCourses.add(courseInfo);
                        System.out.println(
                                "DEBUG: Added enrollment for course: " + enrollment.path("courseCode").asText());
                    });
                } else {
                    System.out.println("DEBUG: Enrollments data is not an array");
                }

                return ResponseEntity.ok(enrolledCourses);
            }
            return ResponseEntity.status(response.getStatusCode()).body(null);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @DeleteMapping("/{courseCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unenroll(
            @PathVariable String courseCode,
            HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            Long userId = getUserIdFromToken(jwt);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid authentication"));
            }

            // Find the enrollment ID first
            String checkUrl = enrollmentServiceUrl + "/api/enrollments/check?userId=" + userId + "&courseCode="
                    + courseCode;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwt);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<String> checkResponse = restTemplate.exchange(
                    checkUrl,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class);

            if (!checkResponse.getStatusCode().is2xxSuccessful() ||
                    !objectMapper.readTree(checkResponse.getBody()).path("data").asBoolean()) {
                return ResponseEntity.badRequest().body(Map.of("error", "You are not enrolled in this course"));
            }

            // Get all enrollments to find the ID
            String enrollUrl = enrollmentServiceUrl + "/api/enrollments/student/" + userId;
            ResponseEntity<String> enrollmentsResponse = restTemplate.exchange(
                    enrollUrl,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class);

            if (enrollmentsResponse.getStatusCode().is2xxSuccessful()) {
                JsonNode enrollments = objectMapper.readTree(enrollmentsResponse.getBody()).path("data");
                Long enrollmentId = null;

                for (JsonNode enrollment : enrollments) {
                    if (courseCode.equals(enrollment.path("courseCode").asText())) {
                        enrollmentId = enrollment.path("id").asLong();
                        break;
                    }
                }

                if (enrollmentId != null) {
                    // Delete the enrollment
                    String deleteUrl = enrollmentServiceUrl + "/api/enrollments/" + enrollmentId;
                    restTemplate.exchange(
                            deleteUrl,
                            org.springframework.http.HttpMethod.DELETE,
                            entity,
                            String.class);
                    return ResponseEntity.ok(Map.of("message", "Successfully unenrolled from " + courseCode));
                }
            }

            return ResponseEntity.badRequest().body(Map.of("error", "Failed to unenroll from course"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "An error occurred while unenrolling"));
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private Long getUserIdFromToken(String jwt) throws Exception {
        String authUrl = authServiceUrl + "/api/auth/user";
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwt);
        HttpEntity<?> authEntity = new HttpEntity<>(authHeaders);

        ResponseEntity<String> authResponse = restTemplate.exchange(
                authUrl,
                org.springframework.http.HttpMethod.GET,
                authEntity,
                String.class);

        if (authResponse.getStatusCode().is2xxSuccessful()) {
            JsonNode authJson = objectMapper.readTree(authResponse.getBody());
            if (authJson.has("id")) {
                return authJson.get("id").asLong();
            }
        }
        return null;
    }
}

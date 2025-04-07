package p4.mainservice.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import p4.mainservice.dto.GradeForm;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

@Controller
public class ViewController implements ErrorController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${course.service.url}")
    private String courseServiceUrl;

    @Value("${grades.service.url}")
    private String gradesServiceUrl;

    @Value("${enrollment.service.url}")
    private String enrollmentServiceUrl;

    public ViewController(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

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
    public String dashboard(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            // Get JWT token from cookies
            String jwt = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("jwt".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }

            if (jwt != null) {
                try {
                    // Get user info from auth service
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(jwt);
                    HttpEntity<?> entity = new HttpEntity<>(headers);

                    ResponseEntity<String> authResponse = restTemplate.exchange(
                            authServiceUrl + "/api/auth/user",
                            org.springframework.http.HttpMethod.GET,
                            entity,
                            String.class);

                    if (authResponse.getStatusCode().is2xxSuccessful()) {
                        JsonNode userInfo = objectMapper.readTree(authResponse.getBody());

                        if (userInfo != null && userInfo.has("success") && userInfo.get("success").asBoolean()) {
                            JsonNode data = userInfo.get("data");
                            if (data != null) {
                                JsonNode user = data.get("user");
                                if (user != null) {
                                    String role = user.has("role") ? user.get("role").asText() : "";
                                    Long userId = user.has("id") ? user.get("id").asLong() : null;
                                    String username = user.has("username") ? user.get("username").asText() : "User";

                                    model.addAttribute("username", username);

                                    if (userId != null && !role.isEmpty()) {
                                        switch (role) {
                                            case "STUDENT":
                                                // Set default values
                                                model.addAttribute("enrolledCourses", 0);
                                                model.addAttribute("currentGpa", "0.00");

                                                try {
                                                    // Try to fetch enrolled courses count
                                                    ResponseEntity<String> enrollmentsResponse = restTemplate.exchange(
                                                            enrollmentServiceUrl + "/api/enrollments/student/" + userId,
                                                            org.springframework.http.HttpMethod.GET,
                                                            entity,
                                                            String.class);

                                                    if (enrollmentsResponse.getStatusCode().is2xxSuccessful()) {
                                                        JsonNode enrollmentsJson = objectMapper
                                                                .readTree(enrollmentsResponse.getBody());
                                                        if (enrollmentsJson.get("success").asBoolean()
                                                                && enrollmentsJson.has("data")) {
                                                            model.addAttribute("enrolledCourses",
                                                                    enrollmentsJson.get("data").size());
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println("Error fetching enrollments: " + e.getMessage());
                                                }

                                                try {
                                                    // Try to fetch GPA
                                                    ResponseEntity<String> gpaResponse = restTemplate.exchange(
                                                            gradesServiceUrl + "/api/grades/student/" + userId + "/gpa",
                                                            org.springframework.http.HttpMethod.GET,
                                                            entity,
                                                            String.class);

                                                    if (gpaResponse.getStatusCode().is2xxSuccessful()) {
                                                        JsonNode gpaJson = objectMapper.readTree(gpaResponse.getBody());
                                                        if (gpaJson.get("success").asBoolean() && gpaJson.has("data")) {
                                                            model.addAttribute("currentGpa",
                                                                    String.format("%.2f",
                                                                            gpaJson.get("data").asDouble()));
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println("Error fetching GPA: " + e.getMessage());
                                                }
                                                break;

                                            case "ADMIN":
                                                // Set default values
                                                model.addAttribute("totalStudents", 0);
                                                model.addAttribute("totalFaculty", 0);
                                                model.addAttribute("activeCourses", 0);
                                                model.addAttribute("totalEnrollments", 0);

                                                try {
                                                    // Try to fetch total students
                                                    ResponseEntity<String> studentsResponse = restTemplate.exchange(
                                                            authServiceUrl + "/api/auth/users/count/STUDENT",
                                                            org.springframework.http.HttpMethod.GET,
                                                            entity,
                                                            String.class);

                                                    if (studentsResponse.getStatusCode().is2xxSuccessful()) {
                                                        JsonNode countJson = objectMapper
                                                                .readTree(studentsResponse.getBody());
                                                        if (countJson.get("success").asBoolean()) {
                                                            model.addAttribute("totalStudents",
                                                                    countJson.get("data").asInt());
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    System.out
                                                            .println("Error fetching student count: " + e.getMessage());
                                                }

                                                try {
                                                    // Try to fetch total faculty
                                                    ResponseEntity<String> facultyResponse = restTemplate.exchange(
                                                            authServiceUrl + "/api/auth/users/count/FACULTY",
                                                            org.springframework.http.HttpMethod.GET,
                                                            entity,
                                                            String.class);

                                                    if (facultyResponse.getStatusCode().is2xxSuccessful()) {
                                                        JsonNode countJson = objectMapper
                                                                .readTree(facultyResponse.getBody());
                                                        if (countJson.get("success").asBoolean()) {
                                                            model.addAttribute("totalFaculty",
                                                                    countJson.get("data").asInt());
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    System.out
                                                            .println("Error fetching faculty count: " + e.getMessage());
                                                }

                                                try {
                                                    // Try to fetch active courses
                                                    ResponseEntity<String> coursesResponse = restTemplate.exchange(
                                                            courseServiceUrl + "/api/courses/count",
                                                            org.springframework.http.HttpMethod.GET,
                                                            entity,
                                                            String.class);

                                                    if (coursesResponse.getStatusCode().is2xxSuccessful()) {
                                                        JsonNode countJson = objectMapper
                                                                .readTree(coursesResponse.getBody());
                                                        if (countJson.get("success").asBoolean()) {
                                                            model.addAttribute("activeCourses",
                                                                    countJson.get("data").asInt());
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    System.out
                                                            .println("Error fetching course count: " + e.getMessage());
                                                }

                                                try {
                                                    // Try to fetch total enrollments
                                                    ResponseEntity<String> totalEnrollmentsResponse = restTemplate
                                                            .exchange(
                                                                    enrollmentServiceUrl + "/api/enrollments/count",
                                                                    org.springframework.http.HttpMethod.GET,
                                                                    entity,
                                                                    String.class);

                                                    if (totalEnrollmentsResponse.getStatusCode().is2xxSuccessful()) {
                                                        JsonNode countJson = objectMapper
                                                                .readTree(totalEnrollmentsResponse.getBody());
                                                        if (countJson.get("success").asBoolean()) {
                                                            model.addAttribute("totalEnrollments",
                                                                    countJson.get("data").asInt());
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    System.out
                                                            .println("Error fetching enrollment count: "
                                                                    + e.getMessage());
                                                }
                                                break;

                                            case "FACULTY":
                                                // Set default values
                                                model.addAttribute("teachingCourses", 0);
                                                model.addAttribute("totalStudents", 0);
                                                model.addAttribute("pendingGrades", 0);

                                                try {
                                                    // Try to fetch teaching courses count
                                                    ResponseEntity<String> teachingResponse = restTemplate.exchange(
                                                            courseServiceUrl + "/api/courses/instructor/" + userId
                                                                    + "/count",
                                                            org.springframework.http.HttpMethod.GET,
                                                            entity,
                                                            String.class);

                                                    if (teachingResponse.getStatusCode().is2xxSuccessful()) {
                                                        JsonNode countJson = objectMapper
                                                                .readTree(teachingResponse.getBody());
                                                        if (countJson.get("success").asBoolean()) {
                                                            model.addAttribute("teachingCourses",
                                                                    countJson.get("data").asInt());
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println(
                                                            "Error fetching teaching courses count: " + e.getMessage());
                                                }

                                                try {
                                                    // Try to fetch total students in their courses
                                                    ResponseEntity<String> courseStudentsResponse = restTemplate
                                                            .exchange(
                                                                    courseServiceUrl + "/api/courses/instructor/"
                                                                            + userId
                                                                            + "/students/count",
                                                                    org.springframework.http.HttpMethod.GET,
                                                                    entity,
                                                                    String.class);

                                                    if (courseStudentsResponse.getStatusCode().is2xxSuccessful()) {
                                                        JsonNode countJson = objectMapper
                                                                .readTree(courseStudentsResponse.getBody());
                                                        if (countJson.get("success").asBoolean()) {
                                                            model.addAttribute("totalStudents",
                                                                    countJson.get("data").asInt());
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    System.out
                                                            .println("Error fetching student count: " + e.getMessage());
                                                }

                                                try {
                                                    // Try to fetch pending grades count
                                                    ResponseEntity<String> pendingGradesResponse = restTemplate
                                                            .exchange(
                                                                    gradesServiceUrl + "/api/grades/instructor/"
                                                                            + userId
                                                                            + "/pending/count",
                                                                    org.springframework.http.HttpMethod.GET,
                                                                    entity,
                                                                    String.class);

                                                    if (pendingGradesResponse.getStatusCode().is2xxSuccessful()) {
                                                        JsonNode countJson = objectMapper
                                                                .readTree(pendingGradesResponse.getBody());
                                                        if (countJson.get("success").asBoolean()) {
                                                            model.addAttribute("pendingGrades",
                                                                    countJson.get("data").asInt());
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println(
                                                            "Error fetching pending grades count: " + e.getMessage());
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error fetching user info: " + e.getMessage());
                }
            }
            return "dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/grades")
    public String grades(Model model, HttpServletRequest request) {
        System.out.println("DEBUG: Entering /grades endpoint");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            System.out.println("DEBUG: User is authenticated: " + auth.getName());
            model.addAttribute("username", auth.getName());
            model.addAttribute("gradeForm", new GradeForm());

            // Get JWT token from cookies
            String jwt = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("jwt".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        System.out.println("DEBUG: Found JWT token in cookies");
                        break;
                    }
                }
            }

            if (jwt != null) {
                try {
                    // Get user info from auth service
                    String authUrl = authServiceUrl + "/api/auth/user";
                    System.out.println("DEBUG: Calling auth service at: " + authUrl);

                    HttpHeaders authHeaders = new HttpHeaders();
                    authHeaders.setBearerAuth(jwt);
                    HttpEntity<?> authEntity = new HttpEntity<>(authHeaders);

                    ResponseEntity<String> authResponse = restTemplate.exchange(
                            authUrl,
                            org.springframework.http.HttpMethod.GET,
                            authEntity,
                            String.class);

                    System.out.println("DEBUG: Auth service response status: " + authResponse.getStatusCode());
                    System.out.println("DEBUG: Auth service response body: " + authResponse.getBody());

                    if (authResponse.getStatusCode().is2xxSuccessful()) {
                        // Parse the auth response to get user ID
                        JsonNode authJson = objectMapper.readTree(authResponse.getBody());
                        System.out.println("DEBUG: Parsed auth response: " + authJson);

                        if (authJson.get("success").asBoolean() && authJson.has("id")) {
                            Long instructorId = authJson.get("id").asLong();
                            System.out.println("DEBUG: Got instructor ID: " + instructorId);

                            // Fetch courses for the instructor from grades service
                            String gradesUrl = gradesServiceUrl + "/api/grades/courses/instructor/" + instructorId;
                            System.out.println("DEBUG: Calling grades service at: " + gradesUrl);

                            // Get the response as a string first
                            ResponseEntity<String> gradesResponse = restTemplate.exchange(
                                    gradesUrl,
                                    org.springframework.http.HttpMethod.GET,
                                    null,
                                    String.class);

                            System.out.println("DEBUG: Grades service response: " + gradesResponse.getBody());

                            // Parse the response
                            JsonNode gradesJson = objectMapper.readTree(gradesResponse.getBody());
                            if (gradesJson.get("success").asBoolean() && gradesJson.has("data")) {
                                JsonNode coursesArray = gradesJson.get("data");
                                System.out.println("DEBUG: Got courses data: " + coursesArray);

                                // Convert JsonNode to List of Map
                                List<Object> courses = objectMapper.convertValue(coursesArray,
                                        objectMapper.getTypeFactory().constructCollectionType(List.class,
                                                Object.class));
                                model.addAttribute("courses", courses);
                            } else {
                                System.out.println("DEBUG: Grades response was not successful or missing data");
                                model.addAttribute("courses", List.of());
                            }
                        } else {
                            System.out.println("DEBUG: Auth response was not successful or missing ID");
                            model.addAttribute("courses", List.of());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG: Error occurred: " + e.getMessage());
                    e.printStackTrace();
                    // Log error and add empty list
                    model.addAttribute("courses", List.of());
                }
            } else {
                System.out.println("DEBUG: No JWT token found in cookies");
                model.addAttribute("courses", List.of());
            }

            return "grades/form";
        }
        System.out.println("DEBUG: User is not authenticated");
        return "redirect:/login";
    }

    @GetMapping("/grades/students/{courseCode}")
    public ResponseEntity<List<Object>> getStudentsForCourse(@PathVariable String courseCode,
            HttpServletRequest request) {
        System.out.println("DEBUG: Fetching students for course: " + courseCode);

        // Get JWT token from cookies
        String jwt = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt != null) {
            try {
                // Call enrollment service to get students for the course
                String enrollmentUrl = enrollmentServiceUrl + "/api/enrollments/course/" + courseCode;
                System.out.println("DEBUG: Calling enrollment service at: " + enrollmentUrl);

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(jwt);
                HttpEntity<?> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        enrollmentUrl,
                        org.springframework.http.HttpMethod.GET,
                        entity,
                        String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                    if (jsonResponse.get("success").asBoolean() && jsonResponse.has("data")) {
                        JsonNode studentsArray = jsonResponse.get("data");
                        List<Object> students = objectMapper.convertValue(studentsArray,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, Object.class));
                        return ResponseEntity.ok(students);
                    }
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Error fetching students: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/grades/check/{courseCode}/{studentId}")
    @ResponseBody
    public ResponseEntity<Boolean> checkGradeExists(@PathVariable String courseCode, @PathVariable Long studentId,
            HttpServletRequest request) {
        // Get JWT token from cookies
        String jwt = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt != null) {
            try {
                // Call grades service to check if grade exists
                String gradesUrl = gradesServiceUrl + "/api/grades/check/" + courseCode + "/" + studentId;
                System.out.println("DEBUG: Calling grades service at: " + gradesUrl);

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(jwt);
                HttpEntity<?> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        gradesUrl,
                        org.springframework.http.HttpMethod.GET,
                        entity,
                        String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                    return ResponseEntity.ok(jsonResponse.get("hasGrade").asBoolean());
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Error checking grade: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return ResponseEntity.ok(false);
    }

    @PostMapping(value = "/grades/submit", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitGrade(@Validated GradeForm gradeForm, BindingResult bindingResult,
            HttpServletRequest request) {
        System.out.println("DEBUG: Submitting grade for course: " + gradeForm.getCourseCode() +
                ", student: " + gradeForm.getStudentId() +
                ", grade: " + gradeForm.getGrade());

        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            System.out.println("DEBUG: Form validation errors: " + bindingResult.getAllErrors());
            response.put("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return ResponseEntity.badRequest().body(response);
        }

        // Get JWT token from cookies
        String jwt = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt != null) {
            try {
                // Call grades service to submit the grade
                String gradesUrl = gradesServiceUrl + "/api/grades";
                System.out.println("DEBUG: Calling grades service at: " + gradesUrl);

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(jwt);
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

                // First check if student already has a grade for this course
                try {
                    String checkUrl = gradesServiceUrl + "/api/grades/check/" + gradeForm.getCourseCode() + "/"
                            + gradeForm.getStudentId();
                    ResponseEntity<String> checkResponse = restTemplate.exchange(
                            checkUrl,
                            org.springframework.http.HttpMethod.GET,
                            new HttpEntity<>(headers),
                            String.class);

                    if (checkResponse.getStatusCode().is2xxSuccessful()) {
                        JsonNode checkResult = objectMapper.readTree(checkResponse.getBody());
                        if (checkResult.has("hasGrade") && checkResult.get("hasGrade").asBoolean()) {
                            response.put("error", "Student #" + gradeForm.getStudentId() +
                                    " already has a grade for " + gradeForm.getCourseCode());
                            return ResponseEntity.badRequest().body(response);
                        }
                    }
                } catch (Exception ex) {
                    // Log error but continue with submission attempt
                    System.out.println("DEBUG: Error checking existing grade: " + ex.getMessage());
                }

                // Create the request body
                String requestBody = String.format(
                        "{\"courseCode\":\"%s\",\"studentId\":%d,\"grade\":%f,\"comments\":\"%s\"}",
                        gradeForm.getCourseCode(),
                        gradeForm.getStudentId(),
                        gradeForm.getGrade(),
                        gradeForm.getComments() != null ? gradeForm.getComments() : "");

                HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> serviceResponse = restTemplate.exchange(
                        gradesUrl,
                        org.springframework.http.HttpMethod.POST,
                        entity,
                        String.class);

                if (serviceResponse.getStatusCode().is2xxSuccessful()) {
                    JsonNode jsonResponse = objectMapper.readTree(serviceResponse.getBody());
                    if (jsonResponse.get("success").asBoolean()) {
                        response.put("message", "Grade submitted successfully");
                        return ResponseEntity.ok(response);
                    } else {
                        response.put("error", jsonResponse.get("message").asText("Failed to submit grade"));
                        return ResponseEntity.badRequest().body(response);
                    }
                } else {
                    response.put("error", "Failed to submit grade");
                    return ResponseEntity.badRequest().body(response);
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Error submitting grade: " + e.getMessage());
                e.printStackTrace();

                // ASSUME THIS IS A DUPLICATE GRADE ERROR
                response.put("error", "Student #" + gradeForm.getStudentId() +
                        " already has a grade for " + gradeForm.getCourseCode());
                return ResponseEntity.internalServerError().body(response);
            }
        } else {
            response.put("error", "Not authenticated");
            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/grades/faculty-grades")
    @ResponseBody
    public ResponseEntity<List<Object>> getFacultyGrades(HttpServletRequest request) {
        // Get JWT token from cookies
        String jwt = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt != null) {
            try {
                // First get user info to get faculty ID
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(jwt);
                HttpEntity<?> entity = new HttpEntity<>(headers);

                ResponseEntity<String> authResponse = restTemplate.exchange(
                        authServiceUrl + "/api/auth/user",
                        org.springframework.http.HttpMethod.GET,
                        entity,
                        String.class);

                if (authResponse.getStatusCode().is2xxSuccessful()) {
                    JsonNode authJson = objectMapper.readTree(authResponse.getBody());

                    if (authJson.get("success").asBoolean() && authJson.has("id")) {
                        Long facultyId = authJson.get("id").asLong();

                        // Fetch courses for the instructor
                        String coursesUrl = gradesServiceUrl + "/api/grades/courses/instructor/" + facultyId;
                        ResponseEntity<String> coursesResponse = restTemplate.exchange(
                                coursesUrl,
                                org.springframework.http.HttpMethod.GET,
                                entity,
                                String.class);

                        Map<String, String> courseNameMap = new HashMap<>();
                        if (coursesResponse.getStatusCode().is2xxSuccessful()) {
                            JsonNode coursesJson = objectMapper.readTree(coursesResponse.getBody());
                            if (coursesJson.get("success").asBoolean() && coursesJson.has("data")) {
                                JsonNode courses = coursesJson.get("data");
                                for (JsonNode course : courses) {
                                    courseNameMap.put(
                                            course.get("courseCode").asText(),
                                            course.get("courseName").asText());
                                }
                            }
                        }

                        // For each course, fetch the grades
                        List<Object> allGrades = new ArrayList<>();
                        for (String courseCode : courseNameMap.keySet()) {
                            String gradesUrl = gradesServiceUrl + "/api/grades/course/" + courseCode;

                            ResponseEntity<String> gradesResponse = restTemplate.exchange(
                                    gradesUrl,
                                    org.springframework.http.HttpMethod.GET,
                                    entity,
                                    String.class);

                            if (gradesResponse.getStatusCode().is2xxSuccessful()) {
                                JsonNode gradesJson = objectMapper.readTree(gradesResponse.getBody());

                                if (gradesJson.get("success").asBoolean() && gradesJson.has("data")) {
                                    JsonNode grades = gradesJson.get("data");
                                    for (JsonNode grade : grades) {
                                        Map<String, Object> gradeWithCourseName = objectMapper.convertValue(grade,
                                                objectMapper.getTypeFactory().constructMapType(Map.class, String.class,
                                                        Object.class));
                                        gradeWithCourseName.put("courseName",
                                                courseNameMap.get(grade.get("courseCode").asText()));
                                        allGrades.add(gradeWithCourseName);
                                    }
                                }
                            }
                        }

                        return ResponseEntity.ok(allGrades);
                    }
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Error fetching faculty grades: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok(List.of());
    }

    @RequestMapping("/error")
    public String handleError() {
        return "error";
    }
}

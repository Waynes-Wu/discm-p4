package p4.mainservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import p4.mainservice.dto.ApiResponse;
import p4.mainservice.dto.CourseDTO;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class CourseService {

    private final WebClient courseServiceClient;
    private final ParameterizedTypeReference<ApiResponse<List<CourseDTO>>> courseListType = 
        new ParameterizedTypeReference<ApiResponse<List<CourseDTO>>>() {};
    private final ParameterizedTypeReference<ApiResponse<CourseDTO>> courseType = 
        new ParameterizedTypeReference<ApiResponse<CourseDTO>>() {};
    private final ParameterizedTypeReference<ApiResponse<Void>> voidType = 
        new ParameterizedTypeReference<ApiResponse<Void>>() {};

    @Autowired
    public CourseService(WebClient.Builder webClientBuilder, 
                        @Value("${course.service.url}") String courseServiceUrl) {
        this.courseServiceClient = webClientBuilder.baseUrl(courseServiceUrl).build();
    }

    public List<CourseDTO> getAllCourses() {
        ApiResponse<List<CourseDTO>> response = courseServiceClient.get()
                .uri("/api/courses")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(courseListType)
                .block();

        return response != null && response.isSuccess() ? response.getData() : Collections.emptyList();
    }

    public CourseDTO getCourseById(Long id) {
        ApiResponse<CourseDTO> response = courseServiceClient.get()
                .uri("/api/courses/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(courseType)
                .block();

        return response != null && response.isSuccess() ? response.getData() : null;
    }

    public CourseDTO createCourse(CourseDTO courseDTO) {
        ApiResponse<CourseDTO> response = courseServiceClient.post()
                .uri("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(courseDTO)
                .retrieve()
                .bodyToMono(courseType)
                .block();

        return response != null && response.isSuccess() ? response.getData() : null;
    }

    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        ApiResponse<CourseDTO> response = courseServiceClient.put()
                .uri("/api/courses/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(courseDTO)
                .retrieve()
                .bodyToMono(courseType)
                .block();

        return response != null && response.isSuccess() ? response.getData() : null;
    }

    public boolean deleteCourse(Long id) {
        ApiResponse<Void> response = courseServiceClient.delete()
                .uri("/api/courses/" + id)
                .retrieve()
                .bodyToMono(voidType)
                .block();

        return response != null && response.isSuccess();
    }
}

package p4.mainservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import p4.mainservice.dto.AuthResponse;
import p4.mainservice.dto.LoginRequest;
import p4.mainservice.dto.RegisterRequest;
import p4.mainservice.exception.AuthenticationException;
import p4.mainservice.exception.ServiceException;
import reactor.core.publisher.Mono;

@Service
public class AuthServiceClient {

    private final WebClient webClient;

    @Autowired
    public AuthServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${auth.service.url}") String authServiceUrl) {
        this.webClient = webClientBuilder
            .baseUrl(authServiceUrl)
            .build();
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        return webClient.post()
            .uri("/api/auth/login")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(AuthResponse.class)
            .onErrorMap(WebClientResponseException.class, ex -> {
                if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    return new AuthenticationException("Invalid email or password");
                }
                return new ServiceException("Auth service error: " + ex.getMessage(), 
                    HttpStatus.valueOf(ex.getStatusCode().value()));
            });
    }

    public Mono<AuthResponse> register(RegisterRequest request) {
        return webClient.post()
            .uri("/api/auth/register")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(AuthResponse.class)
            .onErrorMap(WebClientResponseException.class, ex -> {
                if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                    return new AuthenticationException("Email already exists");
                }
                return new ServiceException("Auth service error: " + ex.getMessage(),
                    HttpStatus.valueOf(ex.getStatusCode().value()));
            });
    }

    public Mono<Boolean> validateToken(String token) {
        return webClient.get()
            .uri("/api/auth/validate-token")
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(Boolean.class)
            .onErrorMap(WebClientResponseException.class, ex -> {
                if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    return new AuthenticationException("Invalid or expired token");
                }
                return new ServiceException("Auth service error: " + ex.getMessage(),
                    HttpStatus.valueOf(ex.getStatusCode().value()));
            });
    }

    public Mono<AuthResponse> getUserInfo(String email) {
        return webClient.get()
            .uri("/api/auth/user")
            .header("Authorization", "Bearer " + email)
            .retrieve()
            .bodyToMono(AuthResponse.class)
            .onErrorMap(WebClientResponseException.class, ex -> {
                if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return new AuthenticationException("User not found");
                }
                return new ServiceException("Auth service error: " + ex.getMessage(),
                    HttpStatus.valueOf(ex.getStatusCode().value()));
            });
    }
}

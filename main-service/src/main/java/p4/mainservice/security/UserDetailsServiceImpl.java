package p4.mainservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import p4.mainservice.dto.AuthResponse;
import p4.mainservice.exception.AuthenticationException;
import p4.mainservice.service.AuthServiceClient;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AuthServiceClient authServiceClient;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            AuthResponse response = authServiceClient.getUserInfo(email)
                .onErrorMap(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode().is4xxClientError()) {
                        return new UsernameNotFoundException("User not found or invalid credentials");
                    }
                    return new AuthenticationException("Error communicating with auth service: " + ex.getMessage());
                })
                .block();

            if (response == null || !response.isSuccess()) {
                throw new UsernameNotFoundException("Invalid user response from auth service");
            }

            return UserPrincipal.builder()
                .email(response.getEmail())
                .name(response.getName())
                .role(response.getRole())
                .build();
        } catch (Exception e) {
            if (e instanceof UsernameNotFoundException) {
                throw e;
            }
            throw new UsernameNotFoundException("Error loading user: " + e.getMessage());
        }
    }
}

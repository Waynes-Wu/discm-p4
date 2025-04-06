package p4.mainservice.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String email;
    private String name;
    private String role;
    private String message;
    private boolean success;
}

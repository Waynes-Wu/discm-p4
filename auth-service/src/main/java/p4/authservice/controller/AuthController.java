package p4.authservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import p4.authservice.dto.AuthResponse;
import p4.authservice.dto.LoginRequest;
import p4.authservice.dto.RegisterRequest;
import p4.authservice.model.User;
import p4.authservice.model.UserType;
import p4.authservice.repository.UserRepository;
import p4.authservice.service.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = userRepository.findByEmail(request.getEmail());
            String token = jwtService.generateToken(user);

            return ResponseEntity.ok(AuthResponse.builder()
                    .success(true)
                    .token(token)
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getType().toString())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(AuthResponse.builder()
                    .success(false)
                    .message("Invalid email or password")
                    .build());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.ok(AuthResponse.builder()
                    .success(false)
                    .message("Email already exists")
                    .build());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setType(UserType.valueOf(request.getRole().toUpperCase()));
        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getType().toString())
                .build());
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email);
            return ResponseEntity.ok(user != null && jwtService.isTokenValid(token, user));
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<AuthResponse> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email);

            if (user != null) {
                return ResponseEntity.ok(AuthResponse.builder()
                        .success(true)
                        .email(user.getEmail())
                        .name(user.getName())
                        .role(user.getType().toString())
                        .build());
            }
        } catch (Exception e) {
            // Token validation failed or user not found
        }

        return ResponseEntity.ok(AuthResponse.builder()
                .success(false)
                .message("User not found")
                .build());
    }
}

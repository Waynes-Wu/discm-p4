package p4.mainservice.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import p4.mainservice.dto.LoginRequest;
import p4.mainservice.dto.RegisterRequest;
import p4.mainservice.service.AuthServiceClient;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthServiceClient authServiceClient;

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletResponse response) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        try {
            var authResponse = authServiceClient.login(request).block();
            if (authResponse != null && authResponse.isSuccess()) {
                // Set JWT token in cookie
                Cookie cookie = new Cookie("jwt", authResponse.getToken());
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setMaxAge(86400); // 24 hours
                response.addCookie(cookie);

                return "redirect:/dashboard";
            }
            return "redirect:/login?error";
        } catch (Exception e) {
            return "redirect:/login?error";
        }
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String role,
            HttpServletResponse response) {
        
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword(password);
        request.setRole(role);

        try {
            var authResponse = authServiceClient.register(request).block();
            if (authResponse != null && authResponse.isSuccess()) {
                // Set JWT token in cookie
                Cookie cookie = new Cookie("jwt", authResponse.getToken());
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setMaxAge(86400); // 24 hours
                response.addCookie(cookie);

                return "redirect:/dashboard";
            }
            return "redirect:/register?error=" + (authResponse != null ? authResponse.getMessage() : "unknown");
        } catch (Exception e) {
            return "redirect:/register?error=unknown";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Clear JWT cookie
        Cookie cookie = new Cookie("jwt", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        SecurityContextHolder.clearContext();
        return "redirect:/login?logout";
    }
}

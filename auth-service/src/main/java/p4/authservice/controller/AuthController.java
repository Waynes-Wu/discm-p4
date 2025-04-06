package p4.authservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import p4.authservice.model.User;
import p4.authservice.repository.UserRepository;
import p4.authservice.service.JwtUtil;

public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // get user
    @GetMapping("/user")
    public User getUserByAuth(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(jwtToken);
        return userRepository.findByEmail(email);
    }

    // check if token is valid (probably backend communication)
    @GetMapping("/validate-token")
    public boolean isTokenValid(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            return jwtUtil.isTokenValid(jwtToken);
        } catch (Exception e) {
            return false;
        }
    }

    // check if credentials are correct
    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password) {
        User user = userRepository.findByEmail(email);

        if (user != null && user.getPassword().equals(password)) {
            return jwtUtil.generateToken(user.getId().toString());
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

}

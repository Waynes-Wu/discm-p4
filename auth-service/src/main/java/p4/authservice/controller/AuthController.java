package p4.authservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import p4.authservice.model.User;
import p4.authservice.repository.UserRepository;
import p4.authservice.service.JwtUtil;

@RestController
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // get user
    @GetMapping("/user")
    public Map<String, Object> getUserByAuth(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        String id = jwtUtil.extractUsername(jwtToken);

        User user = userRepository.findById(Long.parseLong(id));
        
        if (user == null) {
            System.out.println("User not found with ID: " + id);
            throw new RuntimeException("User not found");
        }
        
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("id", user.getId());
        userDetails.put("name", user.getName());
        userDetails.put("email", user.getEmail());
        userDetails.put("type", user.getType());
        
        System.out.println("User details: " + userDetails); 
        
        return userDetails;  
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

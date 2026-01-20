package com.example.taskandbookingmanagement.controller;

import com.example.taskandbookingmanagement.dto.UserResponse;
import com.example.taskandbookingmanagement.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Used for dropdown in "Create Task"
    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(u -> {
            UserResponse r = new UserResponse();
            r.setId(u.getId());
            r.setName(u.getName());
            r.setUsername(u.getUsername());
            r.setRole(u.getRole());
            return r;
        }).toList();
    }

    // Used to know "who am I" + role for UI authorization (show approve button if MANAGER)
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        String username = authentication.getName();

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found in DB: " + username));

        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setName(user.getName());
        r.setUsername(user.getUsername());
        r.setRole(user.getRole());
        return r;
    }
}

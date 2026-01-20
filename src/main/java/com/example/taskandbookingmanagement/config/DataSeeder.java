package com.example.taskandbookingmanagement.config;

import com.example.taskandbookingmanagement.model.Role;
import com.example.taskandbookingmanagement.model.User;
import com.example.taskandbookingmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return; // already seeded
            }

            userRepository.save(makeUser("Admin", "admin", "admin123", Role.ADMIN, encoder));
            userRepository.save(makeUser("Manager", "manager", "manager123", Role.MANAGER, encoder));
            userRepository.save(makeUser("User", "user", "user123", Role.USER, encoder));

            System.out.println("✅ Seeded default users: admin/manager/user");
        };
    }

    private User makeUser(String name, String username, String rawPassword, Role role, PasswordEncoder encoder) {
        User u = new User();
        u.setName(name);
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setRole(role);
        u.setEnabled(true);
        return u;
    }
}

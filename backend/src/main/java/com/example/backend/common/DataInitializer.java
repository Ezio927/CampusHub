package com.example.backend.common;

import com.example.backend.auth.security.PasswordHasher;
import com.example.backend.common.enums.UserGender;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            createUser("admin", "admin123", "ADMIN0001", "Admin", UserRole.ADMIN, UserStatus.ACTIVE);
        }
    }

    private void createUser(String username, String password, String studentId, String realName, UserRole role, UserStatus status) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setStudentId(studentId);
        user.setRealName(realName);
        user.setGender(status == UserStatus.ACTIVE ? UserGender.UNDISCLOSED : null);
        user.setCreditScore(100);
        user.setPublisherRating(0.0);
        user.setPublisherRatingCount(0);
        user.setAcceptorRating(0.0);
        user.setAcceptorRatingCount(0);
        user.setQuotaAvailable(status == UserStatus.ACTIVE && role == UserRole.STUDENT ? 20 : 0);
        user.setQuotaFrozen(0);
        user.setRole(role);
        user.setStatus(status);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(user.getCreatedAt());
        userRepository.save(user);
    }
}

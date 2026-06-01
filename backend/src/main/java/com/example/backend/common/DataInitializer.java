package com.example.backend.common;

import com.example.backend.auth.security.PasswordHasher;
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
            createUser("alice", "alice123", "2024001001", "Alice Student", UserRole.STUDENT, UserStatus.PENDING_CERT);
            createUser("admin", "admin123", "ADMIN0001", "Admin", UserRole.ADMIN, UserStatus.ACTIVE);
            createUser("1", "1", "TEST0001", "测试用户1", UserRole.STUDENT, UserStatus.ACTIVE);
            createUser("2", "2", "TEST0002", "测试用户2", UserRole.STUDENT, UserStatus.ACTIVE);
        }
    }

    private void createUser(String username, String password, String studentId, String realName, UserRole role, UserStatus status) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setStudentId(studentId);
        user.setRealName(realName);
        user.setCreditScore(100);
        user.setQuotaAvailable(status == UserStatus.ACTIVE && role == UserRole.STUDENT ? 20 : 0);
        user.setQuotaFrozen(0);
        user.setRole(role);
        user.setStatus(status);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(user.getCreatedAt());
        userRepository.save(user);
    }
}

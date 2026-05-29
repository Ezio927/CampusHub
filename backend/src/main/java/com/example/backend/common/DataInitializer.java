package com.example.backend.common;

import com.example.backend.auth.security.PasswordHasher;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.demand.entity.Tag;
import com.example.backend.demand.repository.TagRepository;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public DataInitializer(UserRepository userRepository, TagRepository tagRepository) {
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            createUser("alice", "alice123", "2024001001", "Alice Student", UserRole.STUDENT, UserStatus.PENDING_CERT);
            createUser("admin", "admin123", "ADMIN0001", "Admin", UserRole.ADMIN, UserStatus.ACTIVE);
            createUser("1", "1", "TEST0001", "测试用户1", UserRole.STUDENT, UserStatus.ACTIVE);
            createUser("2", "2", "TEST0002", "测试用户2", UserRole.STUDENT, UserStatus.ACTIVE);
        }

        if (tagRepository.count() == 0) {
            createTag("location");
            createTag("skill");
            createTag("study");
            createTag("secondhand");
        }
    }

    private void createUser(String username, String password, String studentId, String realName, UserRole role, UserStatus status) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setStudentId(studentId);
        user.setRealName(realName);
        user.setCreditScore(100);
        user.setRole(role);
        user.setStatus(status);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(user.getCreatedAt());
        userRepository.save(user);
    }

    private void createTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        tagRepository.save(tag);
    }
}

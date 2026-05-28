package com.example.backend.user.repository;

import com.example.backend.auth.security.PasswordHasher;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.user.entity.User;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {

    private final ConcurrentHashMap<Long, User> storage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> usernameIndex = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> studentIdIndex = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public UserRepository() {
        User student = new User();
        student.setId(idGenerator.getAndIncrement());
        student.setUsername("alice");
        student.setPasswordHash(PasswordHasher.hash("alice123"));
        student.setStudentId("2024001001");
        student.setRealName("Alice Student");
        student.setCreditScore(100);
        student.setRole(UserRole.STUDENT);
        student.setStatus(UserStatus.PENDING_CERT);
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(student.getCreatedAt());
        save(student);

        User admin = new User();
        admin.setId(idGenerator.getAndIncrement());
        admin.setUsername("admin");
        admin.setPasswordHash(PasswordHasher.hash("admin123"));
        admin.setStudentId("ADMIN0001");
        admin.setRealName("Admin");
        admin.setCreditScore(100);
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(admin.getCreatedAt());
        save(admin);

        // 测试用临时用户 1
        User u1 = new User();
        u1.setId(idGenerator.getAndIncrement());
        u1.setUsername("1");
        u1.setPasswordHash(PasswordHasher.hash("1"));
        u1.setStudentId("TEST0001");
        u1.setRealName("测试用户1");
        u1.setCreditScore(100);
        u1.setRole(UserRole.STUDENT);
        u1.setStatus(UserStatus.ACTIVE);
        u1.setCreatedAt(LocalDateTime.now());
        u1.setUpdatedAt(u1.getCreatedAt());
        save(u1);

        // 测试用临时用户 2
        User u2 = new User();
        u2.setId(idGenerator.getAndIncrement());
        u2.setUsername("2");
        u2.setPasswordHash(PasswordHasher.hash("2"));
        u2.setStudentId("TEST0002");
        u2.setRealName("测试用户2");
        u2.setCreditScore(100);
        u2.setRole(UserRole.STUDENT);
        u2.setStatus(UserStatus.ACTIVE);
        u2.setCreatedAt(LocalDateTime.now());
        u2.setUpdatedAt(u2.getCreatedAt());
        save(u2);
    }

    public synchronized User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        user.setUpdatedAt(LocalDateTime.now());
        storage.put(user.getId(), user);
        if (user.getUsername() != null) {
            usernameIndex.put(user.getUsername(), user.getId());
        }
        if (user.getStudentId() != null) {
            studentIdIndex.put(user.getStudentId(), user.getId());
        }
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<User> findByUsername(String username) {
        Long id = usernameIndex.get(username);
        return id == null ? Optional.empty() : findById(id);
    }

    public Optional<User> findByStudentId(String studentId) {
        Long id = studentIdIndex.get(studentId);
        return id == null ? Optional.empty() : findById(id);
    }

    public boolean existsByUsername(String username) {
        return usernameIndex.containsKey(username);
    }

    public boolean existsByStudentId(String studentId) {
        return studentIdIndex.containsKey(studentId);
    }

    public List<User> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(storage.values()));
    }
}

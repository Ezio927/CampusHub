package com.example.backend.user.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.user.entity.User;
import com.example.backend.user.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final UserMapper mapper;

    public UserRepository(UserMapper mapper) {
        this.mapper = mapper;
    }

    public User save(User user) {
        if (user.getCreatedAt() == null) user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        if (user.getId() == null) {
            mapper.insert(user);
        } else {
            mapper.updateById(user);
        }
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(mapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)));
    }

    public Optional<User> findByStudentId(String studentId) {
        return Optional.ofNullable(mapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getStudentId, studentId)));
    }

    public boolean existsByUsername(String username) {
        return mapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
    }

    public boolean existsByStudentId(String studentId) {
        return mapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getStudentId, studentId)) > 0;
    }

    public List<User> findAll() {
        return mapper.selectList(null);
    }

    public long count() {
        return mapper.selectCount(null);
    }
}

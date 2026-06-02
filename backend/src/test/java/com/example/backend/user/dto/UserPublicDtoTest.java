package com.example.backend.user.dto;

import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserGender;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.user.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserPublicDtoTest {

    @Test
    void forPublicApiShouldExposeOnlySafeFields() {
        User user = new User();
        user.setId(5L);
        user.setUsername("alice");
        user.setRealName("张三");
        user.setPhone("13800138000");
        user.setEmail("alice@school.edu");
        user.setAvatarUrl("/avatars/a.png");
        user.setGender(UserGender.FEMALE);
        user.setCreditScore(90);
        user.setQuotaAvailable(31);
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.ACTIVE);

        UserPublicDto dto = UserPublicDto.forPublicApi(user);

        Assertions.assertEquals(5L, dto.getId());
        Assertions.assertEquals("alice", dto.getUsername());
        Assertions.assertEquals("/avatars/a.png", dto.getAvatarUrl());
        Assertions.assertEquals(90, dto.getCreditScore());
        Assertions.assertNull(dto.getRealName());
        Assertions.assertNull(dto.getQuotaAvailable());
        Assertions.assertNull(dto.getRole());
        Assertions.assertNull(dto.getStatus());
        Assertions.assertNull(dto.getPhone());
        Assertions.assertNull(dto.getEmail());
        Assertions.assertNull(dto.getGender());
    }
}

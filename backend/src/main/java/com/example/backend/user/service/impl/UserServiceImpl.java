package com.example.backend.user.service.impl;

import com.example.backend.auth.security.PasswordHasher;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.UserGender;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.common.moderation.ContentModerationService;
import com.example.backend.common.validation.ImageDataUrlValidator;
import com.example.backend.user.dto.UserCreateDto;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.dto.UserPublicDto;
import com.example.backend.user.dto.UserUpdateDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.QuotaService;
import com.example.backend.user.service.UserService;
import com.example.backend.user.validation.UserProfileValidator;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ContentModerationService contentModerationService;
    private final QuotaService quotaService;

    public UserServiceImpl(UserRepository userRepository,
                           ContentModerationService contentModerationService,
                           QuotaService quotaService) {
        this.userRepository = userRepository;
        this.contentModerationService = contentModerationService;
        this.quotaService = quotaService;
    }

    @Override
    public UserDto createUser(UserCreateDto req) {
        if (req == null || isBlank(req.getPassword()) || isBlank(req.getStudentId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名/密码/学号/姓名不能为空");
        }
        String username = UserProfileValidator.requireValidUsername(req.getUsername());
        String realName = UserProfileValidator.requireValidRealName(req.getRealName());
        String phone = UserProfileValidator.normalizeOptionalPhone(req.getPhone());
        String email = UserProfileValidator.normalizeOptionalEmail(req.getEmail());
        String studentId = UserProfileValidator.requireValidStudentId(req.getStudentId());
        contentModerationService.assertUsernameAllowed(username);

        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (userRepository.existsByStudentId(studentId)) {
            throw new BusinessException(ErrorCode.STUDENT_ID_EXISTS);
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordHasher.hash(req.getPassword()));
        user.setStudentId(studentId);
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setGender(parseGender(req.getGender()));
        user.setAvatarUrl(null);
        user.setCreditScore(100);
        user.setQuotaAvailable(0);
        user.setQuotaFrozen(0);
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.PENDING_CERT);
        userRepository.save(user);
        grantProfileRewards(user.getId(), phone, email, false, false);
        return UserDto.from(findUser(user.getId()));
    }

    @Override
    public UserDto getProfile(Long userId) {
        return UserDto.from(findUser(userId));
    }

    @Override
    public UserPublicDto getPublicInfo(Long userId) {
        return UserPublicDto.forPublicApi(findUser(userId));
    }

    @Override
    public UserDto updateProfile(Long userId, UserUpdateDto req) {
        if (req == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请求体不能为空");
        }
        User user = findUser(userId);
        boolean hadPhone = !isBlank(user.getPhone());
        boolean hadEmail = !isBlank(user.getEmail());
        if (req.getUsername() != null) {
            String username = UserProfileValidator.requireValidUsername(req.getUsername());
            contentModerationService.assertUsernameAllowed(username);

            if (!username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
                throw new BusinessException(ErrorCode.USERNAME_EXISTS);
            }

            user.setUsername(username);
        }
        if (req.getPhone() != null) {
            user.setPhone(UserProfileValidator.normalizeOptionalPhone(req.getPhone()));
        }
        if (req.getEmail() != null) {
            user.setEmail(UserProfileValidator.normalizeOptionalEmail(req.getEmail()));
        }
        if (req.getAvatarUrl() != null) {
            String avatarUrl = req.getAvatarUrl().trim();
            user.setAvatarUrl(avatarUrl.isEmpty() ? null : ImageDataUrlValidator.requireValidUploadedImagePath(avatarUrl, "头像"));
        }
        if (req.getCampus() != null) {
            user.setCampus(req.getCampus().trim().isEmpty() ? null : req.getCampus().trim());
        }
        if (req.getGender() != null) {
            user.setGender(parseGender(req.getGender()));
        }
        userRepository.save(user);
        grantProfileRewards(userId, user.getPhone(), user.getEmail(), hadPhone, hadEmail);
        return UserDto.from(findUser(userId));
    }

    private void grantProfileRewards(Long userId, String phone, String email, boolean hadPhone, boolean hadEmail) {
        if (!hadPhone && !isBlank(phone)) {
            quotaService.grantPhoneReward(userId);
        }
        if (!hadEmail && !isBlank(email)) {
            quotaService.grantEmailReward(userId);
        }
    }

    private User findUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户ID不能为空");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private UserGender parseGender(String gender) {
        if (isBlank(gender)) return UserGender.UNDISCLOSED;
        try {
            return UserGender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UserGender.UNDISCLOSED;
        }
    }
}

/**
 * 用户服务接口
 *
 * 对外接口：
 *   UserDto getProfile(Long userId)                     — 获取用户完整信息
 *   UserDto updateProfile(Long userId, UserUpdateDto req) — 更新个人资料
 *   UserPublicDto getPublicInfo(Long userId)             — 获取他人公开信息（脱敏）
 *   UserDto createUser(UserCreateDto req)                — 内部注册用（AuthService 调用）
 */

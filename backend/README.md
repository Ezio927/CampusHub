# CampusHub 后端

Spring Boot 3 + Java 17 + Maven，模块化单体架构。

## 项目结构

```
src/main/java/com/example/backend/
├── BackendApplication.java              # Spring Boot 启动类
│
├── common/                              # 横切基础设施层 infra
│   ├── ApiResponse.java                 #   统一响应 {code, message, data}
│   ├── GlobalExceptionHandler.java      #   @ControllerAdvice 全局异常拦截
│   ├── BusinessException.java           #   业务异常（预期内错误）
│   ├── ErrorCode.java                   #   错误码常量（20000/40001/40100...）
│   └── enums/                           #   12 个全局枚举
│       ├── UserRole.java                #     STUDENT / ADMIN
│       ├── UserStatus.java              #     ACTIVE / BANNED / PENDING_CERT
│       ├── CertType.java                #     STUDENT_ID / STUDENT_CARD
│       ├── CertStatus.java              #     PENDING / APPROVED / REJECTED
│       ├── DemandCategory.java          #     ERRAND / SKILL / STUDY / SECONDHAND
│       ├── DemandStatus.java            #     OPEN / ACCEPTED / IN_PROGRESS / COMPLETED / CANCELLED / EXPIRED
│       ├── RewardType.java              #     MONEY / POINT
│       ├── OrderStatus.java             #     PENDING_ACCEPT / ACCEPTED / IN_PROGRESS / PENDING_CONFIRM / COMPLETED / CANCELLED
│       ├── ReviewerRole.java            #     PUBLISHER / ACCEPTOR
│       ├── ArbitrationStatus.java       #     PENDING / PROCESSING / RESOLVED / DISMISSED
│       ├── NotificationType.java        #     7 种通知类型
│       └── MessageType.java             #     TEXT / IMAGE / SYSTEM
│
├── config/                              # Spring 配置
│   ├── SecurityConfig.java              #   SecurityFilterChain + JWT 过滤器注册
│   ├── WebConfig.java                   #   CORS 跨域配置
│   └── JwtConfig.java                   #   JWT 密钥 + 过期时间
│
├── auth/                                # 认证鉴权模块
│   ├── controller/AuthController.java   #   /api/auth — register / login / logout
│   ├── service/
│   │   ├── AuthService.java             #     接口：注册、登录、登出、token 校验
│   │   └── impl/AuthServiceImpl.java    #     实现
│   ├── dto/
│   │   ├── LoginRequest.java            #     { username, password }
│   │   ├── RegisterRequest.java         #     { username, password, studentId, realName, phone?, email? }
│   │   └── TokenResponse.java           #     { token, expiresAt, user }
│   └── security/
│       ├── JwtTokenProvider.java         #     JWT 生成 / 解析 / 校验
│       └── JwtAuthFilter.java           #     OncePerRequestFilter 认证过滤器
│
├── user/                                # 用户中心模块
│   ├── controller/
│   │   ├── UserController.java           #   /api/users — 个人信息 CRUD + 信用记录
│   │   └── CertificationController.java  #   /api/certifications — 实名认证
│   ├── service/
│   │   ├── UserService.java              #     接口：资料查询/更新/创建
│   │   ├── CertificationService.java     #     接口：认证提交/查询/审核
│   │   ├── CreditService.java            #     接口：信用分查询/加减
│   │   └── impl/                         #     实现类 x3
│   ├── entity/
│   │   ├── User.java                     #     用户实体（users 表）
│   │   ├── Certification.java            #     实名认证实体
│   │   └── CreditRecord.java             #     信用分变动流水
│   ├── repository/                       #     JPA Repository x3
│   └── dto/                              #     7 个 DTO
│
├── demand/                              # 核心业务模块（需求 + 订单 + 评价 + 仲裁）
│   ├── controller/
│   │   ├── DemandController.java         #   /api/demands — 发布/浏览/详情/编辑/取消
│   │   ├── OrderController.java          #   /api/orders — 接单/列表/详情/送达/确认/取消
│   │   ├── ReviewController.java         #   /api/reviews — 提交评价/查看评价
│   │   └── ArbitrationController.java    #   /api/arbitrations — 创建/处理仲裁
│   ├── service/
│   │   ├── DemandService.java            #     接口
│   │   ├── OrderService.java             #     接口
│   │   ├── ReviewService.java            #     接口
│   │   ├── ArbitrationService.java       #     接口
│   │   └── impl/                         #     实现类 x4
│   ├── entity/
│   │   ├── Demand.java                   #     需求实体
│   │   ├── DemandImage.java              #     需求配图
│   │   ├── Tag.java                      #     标签字典
│   │   ├── Order.java                    #     订单实体
│   │   ├── OrderStatusLog.java           #     状态变更日志
│   │   ├── Review.java                   #     双向评价
│   │   └── ArbitrationTicket.java        #     仲裁工单
│   ├── repository/                       #     JPA Repository x7
│   ├── dto/                              #     11 个 DTO
│   └── pattern/                          #     设计模式实现
│       ├── strategy/                     #     策略模式：订单状态流转
│       │   ├── IOrderStateStrategy.java  #       状态策略接口
│       │   ├── OrderStateContext.java    #       策略上下文（Map 映射）
│       │   └── *Strategy.java            #       6 个状态策略实现类
│       ├── factory/                      #     工厂模式：需求创建校验
│       │   ├── DemandValidator.java      #       校验器接口
│       │   ├── DemandFactory.java        #       工厂类（Map 注册）
│       │   └── *Validator.java           #       4 个板块校验实现
│       ├── observer/                     #     观察者模式：事件解耦
│       │   ├── OrderEventPublisher.java  #       事件发布器
│       │   ├── OrderStatusChangedEvent.java #   状态变更事件
│       │   └── *Listener.java            #       3 个监听器
│       └── matcher/                      #     组合策略模式：智能推荐
│           ├── DemandMatcher.java        #       匹配器接口
│           ├── CompositeDemandMatcher.java #     加权组合器
│           └── *Matcher.java             #       3 个单维度匹配器
│
├── notification/                        # 站内通知模块
│   ├── controller/NotificationController.java  # /api/notifications
│   ├── service/                                # 接口 + 实现
│   ├── entity/Notification.java                # 通知实体
│   ├── repository/NotificationRepository.java  # 数据访问
│   └── dto/NotificationDto.java
│
├── chat/                                # 即时通讯模块（P2）
│   ├── controller/ConversationController.java  # /api/conversations
│   ├── service/                                # 接口 + 实现
│   ├── entity/Conversation.java + Message.java # 会话 + 消息实体
│   ├── repository/                             # 数据访问 x2
│   └── dto/                                    # 4 个 DTO
│
├── recommend/                           # 智能推荐模块（P2）
│   └── service/
│       ├── RecommendationService.java           # 接口
│       └── impl/RecommendationServiceImpl.java  # 实现（调用 CompositeDemandMatcher）
│
└── admin/                               # 后台管理模块（P2）
    ├── controller/AdminController.java          # /api/admin
    ├── service/                                  # 接口 + 实现
    ├── entity/AdminOperationLog.java             # 管理员操作日志
    ├── repository/                               # 数据访问
    └── dto/                                      # 3 个 DTO
```

## 分层架构

```
请求 → Controller  →  Service  →  Repository  →  DB
        │               │            │
    参数校验        业务逻辑       数据访问
    JWT 鉴权        跨模块编排      Entity 映射
    格式转换        事务管理
```

**跨模块调用铁律**：只能调对方模块的 Service 接口，**严禁直接注入其他模块的 Repository**。

例：`OrderService` 要查需求状态 → 调 `DemandService.getDetail(id)`，不能 `DemandRepository.findById(id)`。

## 设计模式总览

| 模式 | 位置 | 解决问题 |
|------|------|----------|
| 策略模式 | `demand/pattern/strategy/` | 6 种订单状态各有不同操作权限，避免 if-else 链 |
| 工厂模式 | `demand/pattern/factory/` | 4 种需求板块校验规则不同，新增板块不修改现有代码 |
| 观察者模式 | `demand/pattern/observer/` | 订单变更后通知/信用分/需求状态同步，事件解耦 |
| 组合策略 | `demand/pattern/matcher/` | 接单用户多维匹配（位置+技能+信用）加权排序 |

## 环境配置

| Profile | 数据库 | 用途 |
|---------|--------|------|
| `dev`（默认） | H2 内存库 | 本地开发，每次重启重建表，console 在 /h2-console |
| `docker` | MySQL 8.0 | 连接 docker-compose 中的 MySQL + Redis |

切换方式：`--spring.profiles.active=docker`

## 开发命令

```sh
# IDE 直接运行 BackendApplication.java（推荐开发方式）

# 或命令行
./mvnw spring-boot:run                          # 默认 dev
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=docker"

# 测试
./mvnw test

# 打包
./mvnw package -DskipTests
```

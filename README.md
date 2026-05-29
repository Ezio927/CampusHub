# CampusHub — 校园互助平台

面向大学生的校园任务互助系统，支持需求发布、接单、信用评价、认证、实时聊天等功能。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端框架 | Spring Boot 3 + MyBatis-Plus |
| 数据库 | MySQL 8.0（生产）/ H2（开发） |
| 缓存 | Redis 7 |
| 前端 | Vue 3 + Vite + Pinia + Vue Router |
| 容器化 | Docker Compose（MySQL + Redis） |
| CI/CD | GitLab CI |

## 功能模块

- **用户认证** — 注册/登录、JWT 鉴权、角色权限（学生/管理员）
- **实名认证** — 校园卡认证、学号绑定、管理员审核
- **需求广场** — 发布需求、搜索筛选、分类标签
- **订单系统** — 接单/送达/确认完成、状态机流转、超时自动确认
- **信用体系** — 信用分加减、扣分惩罚机制
- **评价系统** — 互评打分、图文评价、低分自动申诉
- **仲裁工单** — 争议申诉、管理员裁决
- **即时聊天** — 一对一私聊、未读计数
- **通知中心** — 系统通知、评价通知、认证通知
- **管理后台** — 用户封禁/解封、认证审核、仲裁处理、操作日志

## 快速开始

### 环境要求

- JDK 17+
- Docker Desktop
- Node.js 20+

### 1. 启动数据库

```bash
docker compose up -d
```

### 2. 启动后端

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

> 不想用 Docker 时，直接在 IDEA 启动后端即可（默认用 H2 内存库，无需任何数据库）。

## 项目结构

```
SECIITask/
├── backend/                     # Spring Boot 后端
│   └── src/main/java/com/example/backend/
│       ├── admin/               # 管理后台
│       ├── auth/                # 认证鉴权
│       ├── chat/                # 聊天模块
│       ├── common/              # 公共组件、枚举、异常
│       ├── config/              # Spring 配置
│       ├── demand/              # 需求/订单/评价/仲裁
│       ├── notification/        # 通知模块
│       └── user/                # 用户/认证/信用
├── frontend/                    # Vue 3 前端
├── docs/                        # 设计文档（P0-P3）
├── docker-compose.yml           # Docker 编排
├── .env.example                 # 环境变量模板
└── Docker使用说明.md             # Docker 详细指南
```

## 环境变量

复制 `.env.example` 为 `.env`，按需修改：

```env
DB_ROOT_PASSWORD=campushub123    # MySQL 密码
DB_NAME=campushub                # 数据库名
DB_PORT=3307                     # 宿主机端口（容器内 3306）
REDIS_PORT=6379
JWT_SECRET=your-secret-here
```

## 测试账号

启动后自动创建（H2 / MySQL 通用）：

| 用户名 | 密码 | 角色 |
|---|---|---|
| alice | alice123 | 学生 |
| admin | admin123 | 管理员 |
| 1 | 1 | 测试学生 |
| 2 | 2 | 测试学生 |

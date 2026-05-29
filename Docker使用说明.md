# Docker 使用说明

## 0. 为什么用 Docker

四个人的电脑上装的 MySQL 版本各不相同（5.7 / 8.0 / 甚至没装），Redis 在 Windows 上安装尤其折腾。Docker 让所有人在**完全一致的环境**里跑数据库，消除"我电脑上能跑"的问题。

**本项目的 Docker 只包了 MySQL 和 Redis**。后端和前端仍然在宿主机上原生运行（IDEA + 终端），方便打断点调试。

---

## 1. 安装 Docker

### Windows

1. 下载 [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/)
2. 安装时勾选 "Use WSL 2 instead of Hyper-V"（推荐）
3. 安装完成后重启电脑
4. 启动 Docker Desktop，等待右下角图标变绿
5. 打开终端验证：

```bash
docker --version
docker compose version
```

### macOS

```bash
brew install --cask docker
```

安装后启动 Docker Desktop 应用，同样用上述命令验证。

---

## 2. 项目文件说明

```
SECIITask/
├── docker-compose.yml    # 容器编排配置
├── .env                  # 环境变量（密码等，不提交 git）
├── .env.example          # 环境变量模板（提交 git 供队友参考）
├── backend/src/main/resources/
│   ├── application.yml           # 主配置（默认 dev 用 H2）
│   ├── application-dev.yml       # 开发环境（H2，免 Docker）
│   ├── application-docker.yml    # Docker 环境（连接 MySQL）
│   └── schema.sql                # MySQL 建表脚本（容器启动时自动执行）
```

`.env` 里可以改密码和端口，默认值：

```env
# --- MySQL（宿主机 3307 → 容器 3306）---
DB_ROOT_PASSWORD=campushub123
DB_NAME=campushub
DB_PORT=3307

# --- Redis ---
REDIS_PORT=6379
```

> **关于端口**：宿主机用 3307 是为了避免和本机已有的 MySQL 冲突，Docker 内部容器还是 3306。

---

## 3. 日常命令

以下命令全部在项目根目录执行。

### 启动

```bash
docker compose up -d
```

`-d` 表示后台运行。首次执行会拉取 MySQL 和 Redis 镜像，需要几分钟，之后秒启动。

### 查看运行状态

```bash
docker compose ps
```

正常输出类似：

```
NAME                STATUS                    PORTS
campushub-mysql     Up (healthy)   0.0.0.0:3307->3306/tcp
campushub-redis     Up (healthy)   0.0.0.0:6379->6379/tcp
```

`healthy` 说明数据库已经准备好接受连接了。

### 查看日志

```bash
docker compose logs -f mysql
docker compose logs -f redis
docker compose logs -f             # 两个一起看
```

### 停止

```bash
docker compose down
```

容器停止并删除，但**数据保留**（存在 Docker volume 里）。

### 停止并清空所有数据

```bash
docker compose down -v
```

`-v` 会把数据库数据也删掉（下次 `up` 时重新初始化空库）。调试期如果跑错了数据、想从头来过，就用这条。

### 重启某个容器

```bash
docker compose restart mysql
```

---

## 4. 启动后端（关键步骤）

**默认 profile 是 `dev`（H2 内存库），连 Docker 必须切换 profile：**

### IDEA 启动

在 Run Configuration 的 Environment 里加：

```
spring.profiles.active=docker
```

### 命令行启动

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

### 或者设环境变量

```bash
export SPRING_PROFILES_ACTIVE=docker
```

---

## 5. 连上数据库

### 用数据库工具连接（Navicat / DataGrip / DBeaver）

| 项 | 值 |
|---|---|
| Host | localhost |
| Port | 3307 |
| User | root |
| Password | campushub123 |
| Database | campushub |

### 进容器直接操作 MySQL

```bash
docker exec -it campushub-mysql mysql -uroot -pcampushub123 campushub
```

然后可以执行 SQL：

```sql
SHOW TABLES;
SELECT * FROM users;
```

`exit` 退出。

---

## 6. 初始化表结构

MySQL 容器首次启动时会自动执行 `backend/src/main/resources/schema.sql`（`docker-compose.yml` 里配了 volume 挂载到 `/docker-entrypoint-initdb.d/`）。往后如果改了 schema，需要重建：

```bash
docker compose down -v
docker compose up -d
```

---

## 7. 常见问题

### 端口冲突（3307 被占用）

改 `.env`：

```env
DB_PORT=3308
```

然后 `docker compose down && docker compose up -d`，同时改 `application-docker.yml` 里的 JDBC URL 端口。

### 容器启动失败

```bash
docker compose logs mysql
```

看最后几行报错。常见原因：
- **端口冲突** — 见上一条
- **Docker Desktop 没启动** — 右下角图标确认是绿色
- **磁盘空间不足** — 清理不用的镜像 `docker system prune`

### MySQL 一直是 starting（等很久）

首次启动 MySQL 需要初始化，大约 30 秒。如果超过一分钟还不 healthy：

```bash
docker compose logs mysql | tail -20
```

看到 `ready for connections` 就说明好了。

### 改密码后怎么生效

编辑 `.env` → `docker compose down -v && docker compose up -d`（必须 `-v`，因为密码已写入数据卷）

### 后端启动后连不上 MySQL

确认：
1. `docker compose ps` 显示 MySQL 是 `healthy`
2. 启动时指定了 `--spring.profiles.active=docker`（不是默认的 dev）
3. `.env` 里的 `DB_PORT` 和 `application-docker.yml` 里的端口一致

---

## 8. 完整开发环境启动顺序

```bash
# 1. 启动数据库
docker compose up -d

# 2. 等 MySQL healthy（docker compose ps 确认）

# 3. IDEA 里启动 BackendApplication（设 spring.profiles.active=docker）

# 4. 前端终端
cd frontend
npm run dev
```

关闭：

```bash
# IDEA 里停后端
# Ctrl+C 停前端
docker compose down
```

---

## 9. 快速参考卡片

| 操作 | 命令 |
|---|---|
| 启动 | `docker compose up -d` |
| 看状态 | `docker compose ps` |
| 看日志 | `docker compose logs -f` |
| 停止（保留数据） | `docker compose down` |
| 停止（清数据） | `docker compose down -v` |
| 进 MySQL | `docker exec -it campushub-mysql mysql -uroot -pcampushub123 campushub` |
| 进 Redis | `docker exec -it campushub-redis redis-cli` |

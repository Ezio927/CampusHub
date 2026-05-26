# Docker 使用说明

## 0. 为什么用 Docker

四个人的电脑上装的 MySQL 版本各不相同（5.7 / 8.0 / 甚至没装），Redis 在 Windows 上安装尤其折腾。Docker 让所有人在**完全一致的环境**里跑数据库，消除"我电脑上能跑，拉到你这就报错"的问题。

**本项目的 Docker 只包了 MySQL 和 Redis**。后端和前端仍然在宿主机上原生运行（IDEA + 终端），方便打断点调试。

---

## 1. 安装 Docker

### Windows

1. 下载 [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/)
2. 安装时勾选 "Use WSL 2 instead of Hyper-V"（推荐）
3. 安装完成后重启电脑
4. 启动 Docker Desktop，等待右下角图标变绿
5. 打开终端（PowerShell 或 Git Bash），验证：

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
```

`.env` 里可以改密码和端口，默认值：

```env
DB_ROOT_PASSWORD=campushub123
DB_NAME=campushub
DB_PORT=3306
REDIS_PORT=6379
```

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
campushub-mysql     Up (healthy)   0.0.0.0:3306->3306/tcp
campushub-redis     Up (healthy)   0.0.0.0:6379->6379/tcp
```

`healthy` 说明数据库已经准备好接受连接了。

### 查看日志

```bash
docker compose logs -f mysql       # MySQL 日志，-f 实时追踪
docker compose logs -f redis       # Redis 日志
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

## 4. 连上数据库

### 后端（Spring Boot）连接

`application.properties` 里的 datasource 配成：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/campushub?characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=campushub123
```

注意是 `localhost:3306`，因为后端跑在宿主机，MySQL 端口已经通过 Docker 映射出来了。

### 用数据库工具连接（Navicat / DataGrip / DBeaver）

| 项 | 值 |
|---|---|
| Host | localhost |
| Port | 3306 |
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

## 5. 初始化表结构

MySQL 容器首次启动时会自动执行 `backend/src/main/resources/schema.sql`（`docker-compose.yml` 里配了 volume 挂载）。往后如果改了 schema，需要重建：

```bash
docker compose down -v
docker compose up -d
```

---

## 6. 常见问题

### 端口冲突（3306 被占用）

如果本机已经装了 MySQL 占用了 3306，改 `.env`：

```env
DB_PORT=3307
```

然后 `docker compose down && docker compose up -d`，后端连接时用 `localhost:3307`。

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

编辑 `.env` → `docker compose down && docker compose up -d`

---

## 7. 完整开发环境启动顺序

```bash
# 1. 启动数据库
docker compose up -d

# 2. 等 MySQL healthy（可以用 docker compose ps 确认）

# 3. IDEA 里启动 BackendApplication

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

## 8. 快速参考卡片

| 操作 | 命令 |
|---|---|
| 启动 | `docker compose up -d` |
| 看状态 | `docker compose ps` |
| 看日志 | `docker compose logs -f` |
| 停止（保留数据） | `docker compose down` |
| 停止（清数据） | `docker compose down -v` |
| 进 MySQL | `docker exec -it campushub-mysql mysql -uroot -pcampushub123 campushub` |
| 进 Redis | `docker exec -it campushub-redis redis-cli` |

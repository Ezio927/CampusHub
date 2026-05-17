# 数据库设计

## 1. Chen 风格 ER 图

源码文件：`er-chen-complete.puml`

图中符号约定：

| 符号 | 含义 |
|---|---|
| 方形/矩形 | 实体 |
| 菱形 | 实体间关系 |
| 椭圆 | 属性 |
| `PK` | 主键 |
| `FK` | 外键 |
| 连线旁 `1`、`0..1`、`N` | 基数约束 |

## 2. 实体与主外键

| 实体 | 主键 | 关键外键/约束 | 说明 |
|---|---|---|---|
| `users` | `id` | `username`、`student_id` 唯一 | 用户账号、认证状态、信用分 |
| `certifications` | `id` | `user_id -> users.id`，且 `user_id` 唯一 | 一名用户至多一条当前实名认证记录 |
| `credit_records` | `id` | `user_id -> users.id`，`related_order_id -> orders.id` | 信用分增减流水 |
| `demands` | `id` | `publisher_id -> users.id` | 用户发布的互助需求 |
| `demand_images` | `id` | `demand_id -> demands.id` | 需求图片，按 `sort_order` 排序 |
| `tags` | `id` | `name` 唯一 | 位置、技能、学习等标签 |
| `demand_tags` | `(demand_id, tag_id)` | 分别引用 `demands`、`tags` | 需求与标签的多对多关系表 |
| `orders` | `id` | `demand_id -> demands.id` 且唯一，`publisher_id/acceptor_id -> users.id` | 一个需求最多对应一个订单 |
| `order_status_logs` | `id` | `order_id -> orders.id`，`operator_id -> users.id` | 订单状态流转日志 |
| `reviews` | `id` | `order_id -> orders.id`，`reviewer_id/reviewee_id -> users.id` | 一个订单双方各最多评价一次 |
| `arbitration_tickets` | `id` | `order_id -> orders.id`，`review_id -> reviews.id` | 低分或申诉产生的仲裁工单 |
| `notifications` | `id` | `user_id -> users.id` | 站内通知 |
| `conversations` | `id` | `user1_id/user2_id -> users.id`，`related_demand_id -> demands.id` | 双方围绕需求的会话 |
| `messages` | `id` | `conversation_id -> conversations.id`，`sender_id -> users.id` | 会话消息 |
| `admin_operation_logs` | `id` | `admin_id -> users.id` | 管理员审计日志 |

## 3. 关系与基数

| 关系 | 基数 | 说明 |
|---|---|---|
| 用户 - 发布 - 需求 | 1:N | 一个用户可发布多个需求，每个需求只有一个发布者 |
| 用户 - 拥有 - 实名认证 | 1:0..1 | 用户可未认证，也可有一条当前认证记录 |
| 用户 - 产生 - 信用记录 | 1:N | 信用分变化以流水保存 |
| 需求 - 包含 - 图片 | 1:N | 一个需求可有多张图片 |
| 需求 - 标记 - 标签 | N:M | 通过 `demand_tags` 拆成两个 1:N |
| 需求 - 生成 - 订单 | 1:0..1 | 一个需求最多被成功接单一次 |
| 用户 - 作为需求方 - 订单 | 1:N | 用户可作为发布方拥有多个订单 |
| 用户 - 作为服务方 - 订单 | 1:N | 用户可作为接单方拥有多个订单 |
| 订单 - 记录 - 状态日志 | 1:N | 每次状态变化写一条日志 |
| 订单 - 收到 - 评价 | 1:0..2 | 双方各最多评价一次 |
| 评价 - 触发 - 仲裁工单 | 1:0..1 | 低分评价可触发仲裁 |
| 用户 - 接收 - 通知 | 1:N | 每条通知属于一个接收人 |
| 用户 - 参与 - 会话 | 1:N | 会话由两个用户参与 |
| 会话 - 包含 - 消息 | 1:N | 一条会话包含多条消息 |
| 管理员 - 产生 - 操作日志 | 1:N | 管理后台操作用于审计 |


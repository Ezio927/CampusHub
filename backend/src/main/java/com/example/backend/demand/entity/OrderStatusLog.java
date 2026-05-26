/**
 * 订单状态变更日志 — order_status_logs 表
 *
 * 核心字段：id, orderId(FK), fromStatus, toStatus, operatorId(FK), remark, createdAt
 *
 * 每次订单状态变化都写入一条记录，用于全链路审计和纠纷追溯。
 */

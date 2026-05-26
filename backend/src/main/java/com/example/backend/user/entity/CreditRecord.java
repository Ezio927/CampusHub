/**
 * 信用分变动流水 — credit_records 表
 *
 * 核心字段：id, userId(FK), changeAmount, reason, relatedOrderId(FK), createdAt
 *
 * 纯数据记录，无实体方法。每次信用分增减都插入一条记录，不可删除。
 */

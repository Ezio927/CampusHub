<!--
  订单详情页 — 路由 /orders/:id

  职责：展示订单完整信息 + 当前可执行操作按钮

  展示内容：
    需求摘要、双方信息（接单后对方手机号可见）
    OrderStatusBadge 当前状态
    OrderStatusLog 时间线
    autoConfirmDeadline 倒计时

  根据角色 + 状态显示不同操作：
    服务方 + IN_PROGRESS   → "标记已送达"
    需求方 + PENDING_CONFIRM → "确认验收"
    任意方 + 符合条件       → "取消订单"（含原因填写）
    双方   + COMPLETED      → "去评价"

  待完成：
    - 24h 自动确认倒计时
    - 取消原因弹窗（区分 10 分钟内无责 / 超时扣分）
    - 确认验收后引导评价
-->

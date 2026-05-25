<!--
  订单卡片

  职责：在订单列表中渲染一条订单摘要

  Props:
    order: { id, demandId, demandTitle, publisher, acceptor, status,
             rewardAmount, acceptedAt, autoConfirmDeadline }

  Emits:
    click(orderId)  — 点击跳转订单详情

  展示内容：
    需求标题、对方用户信息、当前状态（OrderStatusBadge）
    报酬金额、接单时间、autoConfirmDeadline

  待完成：
    - 待处理状态高亮（PENDING_CONFIRM 倒计时提醒）
    - 区分"我发布的"和"我接单的"两种视角
-->

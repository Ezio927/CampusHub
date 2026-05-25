<!--
  订单状态徽章

  职责：根据 OrderStatus 渲染不同颜色和文字的标签

  Props:
    status: OrderStatus 枚举值

  颜色映射：
    PENDING_ACCEPT   → 蓝色 "待接单"
    ACCEPTED         → 青色 "已接单"
    IN_PROGRESS      → 橙色 "进行中"
    PENDING_CONFIRM  → 黄色 "待确认"
    COMPLETED        → 绿色 "已完成"
    CANCELLED        → 灰色 "已取消"

  无需 emit，纯展示组件。
-->

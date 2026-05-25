<!--
  消息气泡

  职责：渲染单条聊天消息

  Props:
    message: { id, senderId, content, messageType, createdAt }
    isMine: Boolean — 是否当前用户发送（控制左右对齐 + 颜色）

  类型渲染：
    TEXT   → 文字气泡
    IMAGE  → 图片气泡
    SYSTEM → 灰色系统提示条（如"订单已完成"）

  纯展示组件，无 emit。
-->

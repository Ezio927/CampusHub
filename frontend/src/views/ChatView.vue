<!--
  聊天页 — 路由 /chat/:conversationId

  职责：一对一即时通讯界面

  组装：
    ChatWindow   — 消息列表 + 输入框
    MessageBubble — 单条消息气泡（区分自己/对方、TEXT/IMAGE/SYSTEM）

  流程：
    onMounted → api/conversations.getMessages(id) 拉历史消息
    用户输入发送 → api/conversations.sendMessage(id, { content, messageType })
    新消息 → 轮询 或 WebSocket 更新

  待完成：
    - 图片消息的发送与展示
    - 滚动到底部 + 加载更多历史消息
    - 消息已读状态同步
-->

<!--
  聊天窗口

  职责：消息列表 + 底部输入框的完整聊天界面

  Props:
    conversationId: 会话 ID
    targetUser: { id, username, avatarUrl }  — 对方信息（用于顶栏）

  内部状态:
    messages[]    — 消息列表
    inputText     — 输入框内容

  组装：
    消息列表（v-for MessageBubble）
    底部输入框 + 发送按钮（支持文字 + 图片）

  流程：
    onMounted → api/conversations.getMessages(conversationId) 加载历史
    点发送 → api/conversations.sendMessage(id, { content, messageType })
    新消息到达 → push 到 messages 列表 + 滚动到底

  待完成：
    - 上拉加载更多历史消息
    - 新消息自动滚动到底部
    - 图片消息排版
-->

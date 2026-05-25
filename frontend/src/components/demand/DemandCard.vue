<!--
  需求卡片

  职责：在列表里渲染一条需求的摘要信息

  Props:
    demand: { id, title, description, category, rewardAmount, rewardType,
              status, isUrgent, urgentDeadline, locationTag, publisher, createdAt }

  Emits:
    click(demandId)  — 点击卡片跳详情

  展示内容：
    标题（加急订单显示 🔥+ 倒计时）、板块标签、报酬、位置
    发布者昵称 + 信用分、发布时间（format.js 相对时间）
    isUrgent 时整卡背景高亮

  待完成：
    - 加急倒计时（urgentDeadline - now）
    - 板块标签颜色区分
-->

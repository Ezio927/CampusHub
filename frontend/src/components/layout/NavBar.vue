<!--
  顶部导航栏

  职责：全局顶栏，显示 logo + 导航链接 + 通知角标 + 用户头像下拉

  Props: 无（直接从 stores/auth.js 读 currentUser）

  展示：
    左侧：CampusHub logo → 点击回 /
    中间：四大板块快捷入口（可选）
    右侧：
      通知铃铛 + unreadCount 角标 → 点击展开通知列表
      用户头像 + 用户名 → 下拉菜单（个人中心 / 我的订单 / 退出登录）

  待完成：
    - 通知下拉弹窗（调 stores/notifications.unreadCount）
    - 用户下拉菜单
    - 管理后台入口（role=ADMIN 时显示）
-->

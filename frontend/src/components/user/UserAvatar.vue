<!--
  用户头像组件

  职责：渲染用户头像图片或首字母占位符

  Props:
    username: String
    avatarUrl: String（可选）
    size: String   — "sm" | "md" | "lg"

  行为：
    avatarUrl 存在 → 显示图片
    avatarUrl 为空  → 显示 username 首字母圆形色块

  纯展示组件，无 emit。
-->

<!--
  用户管理页 — 路由 /admin/users

  职责：管理员查看用户列表 + 封禁/解封操作

  功能：
    搜索（用户名/学号模糊）
    状态筛选（ACTIVE / BANNED / PENDING_CERT）
    封禁按钮 → ConfirmModal → 填写封禁原因 → api/admin.banUser()
    解封按钮 → ConfirmModal → api/admin.unbanUser()

  待完成：
    - 分页表格
    - 封禁原因必填校验
    - 操作后刷新列表
-->

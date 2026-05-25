<!--
  注册页 — 路由 /register

  职责：新用户注册表单

  表单字段：
    username（必填,4-20位字母/数字/下划线）
    password（必填,6-32位）
    studentId（必填,唯一）
    realName（必填）
    phone（选填）
    email（选填）

  流程：
    提交 → stores/auth.register() → 成功跳 /login → 失败提示具体错误

  待完成：
    - 前端校验 + 后端 40904/40905 错误映射
    - 注册成功后 toast "注册成功，请登录"
-->

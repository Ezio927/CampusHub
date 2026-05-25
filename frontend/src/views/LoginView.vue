<!--
  登录页 — 路由 /login

  职责：用户名+密码登录表单

  表单字段：username（4-20位）, password（6-32位）

  流程：
    提交 → stores/auth.login() → 成功跳 / → 失败提示"用户名或密码错误"
    已有 token → 直接 redirect 到 /

  待完成：
    - 表单前端校验（空值、长度）
    - 40100 错误提示
    - 注册链接跳转 /register
-->

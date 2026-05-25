<!--
  实名认证审核页 — 路由 /admin/certifications

  职责：管理员审核学生实名认证申请

  功能：
    待审核列表（PENDING 状态认证）
    查看学生证照片 / 学号信息
    通过按钮 → api/certifications.review(id, { approve: true })
    驳回按钮 → 填写驳回原因 → api/certifications.review(id, { approve: false, reason })

  待完成：
    - 图片预览（学生证照片）
    - 驳回原因必填校验
-->

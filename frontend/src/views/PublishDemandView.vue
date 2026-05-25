<!--
  发布需求页 — 路由 /demands/publish

  职责：需求发布表单，板块切换时校验规则动态变化

  表单字段：
    title, description, category（下拉选四大板块）
    rewardAmount, rewardType（MONEY/POINT）
    isUrgent + urgentDeadline
    locationTag, tagIds, imageUrls

  板块校验（工厂模式前端对应）：
    ERRAND    → locationTag 必填
    SKILL     → tagIds 至少选 1 个
    STUDY     → 时间段必填
    SECONDHAND → imageUrls 至少 1 张

  流程：
    选择 category → 校验规则动态切换
    提交 → api/demands.publish() → 成功跳 /demands/{id}

  待完成：
    - 图片上传（前端压缩 → OSS → 拿 URL 放入 imageUrls）
    - 位置标签选择器（从 tags 表拉取）
    - 40302 未实名拦截
-->

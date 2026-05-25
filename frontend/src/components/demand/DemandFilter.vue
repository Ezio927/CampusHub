<!--
  需求筛选器

  职责：需求大厅顶部的筛选、排序控制栏

  Props:
    filters: 当前筛选条件对象
    category: 当前板块

  Emits:
    update:filters  — 筛选条件变化时 v-model 双向绑定

  筛选控件：
    排序下拉：按发布时间 / 报酬金额 / 加急截止时间
    状态筛选：只看 OPEN / 全部
    位置筛选：输入框或下拉（从 tags 表拉 location 标签）
    只看加急：switch 开关
    关键词搜索：输入框 + 搜索按钮

  待完成：
    - 搜索防抖 300ms
    - 位置标签下拉数据源（api 拉取或 store 缓存）
-->

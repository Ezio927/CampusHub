<!--
  需求大厅 — 路由 /

  职责：展示四大板块的需求列表，提供筛选、搜索、分页

  组装：
    CategoryTabs   — 四板块切换 (ERRAND / SKILL / STUDY / SECONDHAND)
    DemandFilter   — 排序、加急、位置筛选
    DemandCard[]   — 需求卡片列表
    Pagination     — 分页
    NavBar / TabBar

  数据流：
    onMounted → stores/demands.fetchDemands()
    用户操作筛选 → stores/demands.setFilter() → 自动重新请求

  待完成：
    - 下拉刷新（移动端）
    - 加急需求置顶 + 倒计时显示
    - 空状态 / 加载中 / 加载失败三种占位
-->

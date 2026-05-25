<!--
  分页器

  职责：通用分页组件

  Props:
    page: Number        — 当前页码（0-based）
    totalPages: Number  — 总页数
    totalElements: Number — 总条数（用于展示"共 X 条"）

  Emits:
    change(page) — 用户点击页码时触发

  展示：
    上一页 / 页码列表 / 下一页
    "共 X 条" 文字

  待完成：
    - 页码过多时省略号（如 1 ... 4 5 6 ... 20）
    - 移动端简化样式
-->

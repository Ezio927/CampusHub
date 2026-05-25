<!--
  星级评分组件

  职责：展示星级评分（只读 or 可交互打星）

  Props:
    rating: Number    — 当前评分（1-5）
    interactive: Boolean — 是否允许用户点击打分（默认 false 只读）
    size: String      — "sm" | "md" | "lg"

  Emits（仅 interactive=true 时）:
    update:rating — v-model 双向绑定，用户点击新星级时触发

  待完成：
    - 半星支持（如 4.5）——只读模式用 clip-path 裁切
    - 点击打星动画
-->

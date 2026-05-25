<!--
  确认弹窗

  职责：危险操作前的二次确认

  Props:
    visible: Boolean         — 是否显示
    title: String            — 弹窗标题
    message: String          — 提示文字
    showReasonInput: Boolean — 是否需要填写原因（取消订单 / 封禁用户时用）
    confirmText: String      — 确认按钮文字（默认"确认"）

  Emits:
    confirm(reason?) — 用户点确认，如果 showReasonInput=true 则带回 reason 字符串
    cancel           — 用户点取消或关弹窗

  待完成：
    - reason 输入框字数限制
    - 动画过渡（fade + scale）
-->

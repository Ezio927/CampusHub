/**
 * 格式化工具函数（纯函数，与 Vue 无关）
 *
 * 对外接口：
 *   formatRelativeTime(dateStr)  — ISO8601 → "3分钟前" / "2小时前" / "5月17日"
 *   formatMoney(amount)          — 5.00 → "¥5.00"
 *   maskPhone(phone)             — 13800138000 → "138****8000"
 *   maskEmail(email)             — alice@smail.nju.edu.cn → "a***e@smail.nju.edu.cn"
 *   formatDate(dateStr)          — ISO8601 → "2026-05-17 14:30"
 *
 * 待完成：
 *   - formatRelativeTime 处理边界：<1分钟、跨年显示年份
 *   - maskEmail 处理短用户名
 */

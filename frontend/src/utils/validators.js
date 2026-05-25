/**
 * 前端表单校验规则（纯函数，不依赖组件）
 *
 * 对外接口：
 *   validateUsername(v)  — 4-20位字母/数字/下划线，返回 { valid, message }
 *   validatePassword(v)  — 6-32位，返回 { valid, message }
 *   validateTitle(v)     — 2-50字，返回 { valid, message }
 *   validateDescription(v) — 10-500字，返回 { valid, message }
 *   validatePhone(v)     — 11位手机号，返回 { valid, message }
 *   validateStudentId(v) — 学号格式校验
 *   validateReviewContent(v, rating) — 当 rating≤3 时 content 必须 ≥15字
 *
 * 返回格式统一为 { valid: Boolean, message: String }
 *
 * 待完成：
 *   - 每项校验的具体正则/规则
 *   - 错误信息中文文案
 */

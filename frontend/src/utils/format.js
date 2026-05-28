export function formatRelativeTime(dateStr) {
  if (!dateStr) return ''
  const diff = Date.now() - new Date(dateStr).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return '刚刚'
  if (mins < 60) return `${mins}分钟前`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}小时前`
  return new Date(dateStr).toLocaleDateString()
}
export function formatMoney(amount) { return `¥${(amount || 0).toFixed(2)}` }
export function maskPhone(phone) { if (!phone || phone.length < 7) return phone; return phone.slice(0, 3) + '****' + phone.slice(-4) }
export function maskEmail(email) { if (!email) return ''; const at = email.indexOf('@'); if (at <= 1) return '***' + email.slice(at); return email[0] + '***' + email.slice(at - 1) }

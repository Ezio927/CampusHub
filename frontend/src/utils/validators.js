const usernamePattern = /^[\p{Script=Han}A-Za-z_-]{2,15}$/u
const realNamePattern = /^[\p{Script=Han}A-Za-z]{2,15}$/u
const phonePattern = /^(13\d|14[5-9]|15\d|16[5-7]|17[0-8]|18\d|19\d)\d{8}$/
const emailLocalPartPattern = /^[A-Za-z0-9._%+-]+$/
const allowedEmailDomains = new Set([
  'qq.com',
  '163.com',
  '126.com',
  'yeah.net',
  'gmail.com',
  'outlook.com',
  'hotmail.com',
  'live.com',
  'foxmail.com',
  'sina.com',
  'sina.cn',
  'sohu.com',
  '139.com',
  '189.cn',
  'wo.cn',
  'yahoo.com',
  'ymail.com',
  'icloud.com',
  'smail.nju.edu.cn',
  'nju.edu.cn',
])

export function normalizeUsername(value) {
  const normalized = requireText(value, '用户名不能为空')
  if (!usernamePattern.test(normalized)) {
    throw new Error('用户名仅支持中英文、下划线和连字符，长度为2-15个字符')
  }
  return normalized
}

export function normalizeRealName(value) {
  const normalized = requireText(value, '姓名不能为空')
  if (!realNamePattern.test(normalized)) {
    throw new Error('姓名仅支持汉字和英文，长度为2-15个字符')
  }
  return normalized
}

export function normalizePhone(value) {
  const normalized = optionalText(value)
  if (!normalized) return ''
  if (!phonePattern.test(normalized)) {
    throw new Error('手机号必须为中国大陆11位手机号，且号段合法')
  }
  return normalized
}

export function normalizeEmail(value) {
  const normalized = optionalText(value)?.toLowerCase()
  if (!normalized) return ''
  const at = normalized.lastIndexOf('@')
  const localPart = normalized.slice(0, at)
  const domain = normalized.slice(at + 1)
  if (at <= 0 || !emailLocalPartPattern.test(localPart) || !allowedEmailDomains.has(domain)) {
    throw new Error('邮箱仅支持常见邮箱或南京大学教育邮箱后缀')
  }
  return normalized
}

function requireText(value, message) {
  const normalized = optionalText(value)
  if (!normalized) throw new Error(message)
  return normalized
}

function optionalText(value) {
  return value == null ? '' : String(value).trim()
}

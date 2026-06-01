const usernamePattern = /^[\p{Script=Han}A-Za-z0-9_-]{2,15}$/u
const realNamePattern = /^[\p{Script=Han}A-Za-z]{2,15}$/u
const phonePattern = /^(13\d|14[5-9]|15\d|16[5-7]|17[0-8]|18\d|19\d)\d{8}$/
const undergradStudentIdPattern = /^\d{9}$/
const graduateStudentIdPattern = /^\d{12}$/
const legacyGraduateStudentIdPattern = /^[A-Z]{2}\d{8}$/
const graduateDegreeCodes = new Set(['50', '52', '60', '65', '66'])
const departmentCodes = new Set([
  '01',
  '02',
  '03',
  '04',
  '05',
  '06',
  '07',
  '08',
  '09',
  '10',
  '11',
  '12',
  '13',
  '14',
  '15',
  '16',
  '17',
  '18',
  '19',
  '20',
  '21',
  '22',
  '23',
  '24',
  '25',
  '27',
  '28',
  '29',
  '30',
  '32',
  '38',
  '43',
  '78',
])
const undergradMajorCategoryCodes = new Set([
  '098',
  '108',
  '180',
  '230',
  '232',
  '234',
  '240',
  '250',
  '810',
  '820',
  '830',
  '840',
  '850',
  '860',
  '870',
  '880',
  '900',
])
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
    throw new Error('用户名仅支持中英文、数字、下划线和连字符，长度为2-15个字符')
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

export function normalizeStudentId(value) {
  const normalized = requireText(value, '学号不能为空').toUpperCase()
  if (
    isValidUndergraduateStudentId(normalized) ||
    isValidGraduateStudentId(normalized) ||
    isValidLegacyGraduateStudentId(normalized)
  ) {
    return normalized
  }
  throw new Error('学号格式不符合南京大学规则')
}

function isValidUndergraduateStudentId(value) {
  if (!undergradStudentIdPattern.test(value)) return false
  const admissionType = value.slice(2, 3)
  const majorCode = value.slice(3, 6)
  const serial = value.slice(6)
  return ['1', '2', '5'].includes(admissionType) && isValidUndergradMajorCode(majorCode) && serial !== '000'
}

function isValidUndergradMajorCode(majorCode) {
  return (
    undergradMajorCategoryCodes.has(majorCode) ||
    /^50\d$/.test(majorCode) ||
    departmentCodes.has(majorCode.slice(0, 2))
  )
}

function isValidGraduateStudentId(value) {
  if (!graduateStudentIdPattern.test(value)) return false
  const degreeCode = value.slice(0, 2)
  const year = Number.parseInt(value.slice(2, 6), 10)
  const majorCode = value.slice(6, 9)
  const serial = value.slice(9)
  return graduateDegreeCodes.has(degreeCode) && year >= 2022 && majorCode !== '000' && serial !== '000'
}

function isValidLegacyGraduateStudentId(value) {
  if (!legacyGraduateStudentIdPattern.test(value)) return false
  const year = Number.parseInt(value.slice(2, 4), 10)
  const majorCode = value.slice(4, 7)
  const serial = value.slice(7)
  return year <= 21 && majorCode !== '000' && serial !== '000'
}

function requireText(value, message) {
  const normalized = optionalText(value)
  if (!normalized) throw new Error(message)
  return normalized
}

function optionalText(value) {
  return value == null ? '' : String(value).trim()
}

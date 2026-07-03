import { describe, expect, it } from 'vitest'

import {
  normalizeEmail,
  normalizePhone,
  normalizeRealName,
  normalizeStudentId,
  normalizeUsername,
} from '@/utils/validators'

describe('validators', () => {
  it('normalizes valid account fields', () => {
    expect(normalizeUsername(' alice_01 ')).toBe('alice_01')
    expect(normalizeRealName(' 张三 ')).toBe('张三')
    expect(normalizePhone('13800138000')).toBe('13800138000')
    expect(normalizeEmail(' Alice@NJU.edu.cn ')).toBe('alice@nju.edu.cn')
  })

  it('accepts supported student id formats', () => {
    expect(normalizeStudentId('221234001')).toBe('221234001')
    expect(normalizeStudentId('502024123001')).toBe('502024123001')
    expect(normalizeStudentId('MG21123001')).toBe('MG21123001')
  })

  it('rejects invalid user input with clear errors', () => {
    expect(() => normalizeUsername('a')).toThrow('用户名仅支持')
    expect(() => normalizeRealName('张3')).toThrow('姓名仅支持')
    expect(() => normalizePhone('12345')).toThrow('手机号必须')
    expect(() => normalizeEmail('alice@example.invalid')).toThrow('邮箱仅支持')
    expect(() => normalizeStudentId('000000000')).toThrow('学号格式')
  })
})

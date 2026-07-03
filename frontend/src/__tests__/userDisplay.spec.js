import { describe, expect, it } from 'vitest'

import { genderText, ratingText, userInitial } from '@/utils/userDisplay'

describe('user display helpers', () => {
  it('builds initials from username or real name', () => {
    expect(userInitial({ username: 'alice' })).toBe('A')
    expect(userInitial({ realName: '张三' })).toBe('张')
    expect(userInitial(null)).toBe('?')
  })

  it('formats gender based on account status', () => {
    expect(genderText('MALE', 'ACTIVE')).toBe('男')
    expect(genderText({ gender: 'FEMALE', status: 'ACTIVE' })).toBe('女')
    expect(genderText({ gender: 'MALE', status: 'PENDING_CERT' })).toBe('未实名')
    expect(genderText(undefined, 'ACTIVE')).toBe('不愿透露')
  })

  it('formats rating summaries', () => {
    expect(ratingText(4.86, 7)).toBe('4.9 / 5（7单）')
    expect(ratingText(0, 0)).toBe('暂无评分')
  })
})

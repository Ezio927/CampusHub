export function userInitial(user) {
  return (user?.username || user?.realName || '?').charAt(0).toUpperCase()
}

export function genderText(userOrGender, status) {
  const gender = typeof userOrGender === 'object' ? userOrGender?.gender : userOrGender
  const userStatus = typeof userOrGender === 'object' ? userOrGender?.status : status
  if (userStatus && userStatus !== 'ACTIVE') return '未实名'
  return {
    MALE: '男',
    FEMALE: '女',
    UNDISCLOSED: '不愿透露'
  }[gender] || (userStatus === 'ACTIVE' ? '不愿透露' : '未实名')
}

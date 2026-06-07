export function errorMessage(error, fallback = '操作失败') {
  return error?.message || error?.response?.data?.message || fallback
}

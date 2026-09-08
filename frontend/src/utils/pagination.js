export function getPagedRows(rows, page, pageSize) {
  const safeRows = Array.isArray(rows) ? rows : []
  const safePage = Number(page) > 0 ? Number(page) : 1
  const safeSize = Number(pageSize) > 0 ? Number(pageSize) : 10
  const start = (safePage - 1) * safeSize
  return safeRows.slice(start, start + safeSize)
}

export function resetPageBySize(state, sizeKey = 'pageSize', pageKey = 'page') {
  return (size) => {
    state[sizeKey] = size
    state[pageKey] = 1
  }
}


function buildQuery(params = {}) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.append(key, String(value))
    }
  })
  const str = query.toString()
  return str ? `?${str}` : ''
}

function handleUnauthorized() {
  try {
    localStorage.removeItem('role')
    localStorage.removeItem('user')
  } catch (_) {}
  if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}

async function request(url, options = {}) {
  const config = {
    credentials: 'include',
    ...options
  }

  const response = await fetch(url, config)
  let result = null
  try {
    result = await response.json()
  } catch (_) {
    const error = new Error('接口返回格式错误')
    error.response = response
    throw error
  }

  const failed = !response.ok || !result || result.code !== 0
  if (failed) {
    const message = result?.msg || result?.message || `请求失败(${response.status})`
    const error = new Error(message)
    error.response = response
    error.result = result
    if (response.status === 401 || result?.code === 401) {
      handleUnauthorized()
    }
    throw error
  }

  return result
}

export function get(url, params = {}, options = {}) {
  return request(`${url}${buildQuery(params)}`, {
    method: 'GET',
    ...options
  })
}

export function postForm(url, body = {}, options = {}) {
  return request(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      ...(options.headers || {})
    },
    body: new URLSearchParams(body),
    ...options
  })
}

export function postJson(url, body = {}, options = {}) {
  return request(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    },
    body: JSON.stringify(body),
    ...options
  })
}

export function pickData(result) {
  return result?.data1 ?? result?.data
}

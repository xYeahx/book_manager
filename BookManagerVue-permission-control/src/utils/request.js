import axios from 'axios'
import { Message } from 'element-ui'
import store from '@/store'
import { getToken, removeToken } from '@/utils/auth'

// create an axios instance
const service = axios.create({
  baseURL: 'http://localhost:8092/BookManager/', // url = base url + request url
  // withCredentials: true, // send cookies when cross-domain requests
  timeout: 5000 // request timeout
})

// request interceptor
service.interceptors.request.use(
  config => {
    if (store.getters.token) {
      config.headers['X-Token'] = getToken()
    }
    return config
  },
  error => {
    console.log(error)
    return Promise.reject(error)
  }
)

/**
 * 判断当前是否在登录/注册页面，避免重复拦截
 */
function isOnAuthPage() {
  return window.location.hash.indexOf('/login') !== -1
    || window.location.hash.indexOf('/register') !== -1
}

/**
 * response 拦截器
 *
 * 后端响应格式说明：
 *   - 成功: { status: 200, data: ... } 或 { code: 0, count: N, data: [...] }
 *   - 失败: { status: 420, message: "..." } 或 纯数字 (-1, -2 等)
 *   - 列表: 直接返回数组 [...]
 */
service.interceptors.response.use(
  response => {
    const res = response.data

    // ---- 1. 数组 / 数字 → 透传，让 view 层自行处理 ----
    if (Array.isArray(res)) {
      return res
    }
    if (typeof res === 'number') {
      return res
    }

    // ---- 2. 对象 → 检查业务状态码 ----
    if (typeof res === 'object' && res !== null) {
      // code=0 (分页列表) / status=200 (普通操作) 视为成功
      const code = res.code !== undefined ? res.code : res.status

      if (code === 0 || code === 200) {
        return res
      }

      const msg = res.message || '操作失败'

      // 420/401 → 未登录 / 会话过期（登录/注册页不弹重定向）
      if (code === 420 || code === 401) {
        if (!isOnAuthPage()) {
          Message.error('登录已过期，请重新登录')
          removeToken()
        }
        return Promise.reject(msg)
      }

      // 403/-3 → 权限不足
      if (code === 403 || code === -3) {
        Message.error('权限不足，无法执行此操作')
        return Promise.reject(msg)
      }

      // 其他错误码 → 统一弹后端返回的 message
      Message.error(msg)
      return Promise.reject(msg)
    }

    return res
  },
  error => {
    // 网络错误 / 超时
    if (error.code === 'ECONNABORTED') {
      Message.error('请求超时，请检查网络连接')
    } else if (error.response) {
      const status = error.response.status
      if (status === 404) {
        Message.error('请求的资源不存在 (404)')
      } else if (status >= 500) {
        Message.error('服务器内部错误 (500)')
      } else {
        Message.error('网络错误: ' + (error.message || '未知错误'))
      }
    } else {
      Message.error('无法连接到服务器，请检查服务是否启动')
    }
    // 网络错误 + 已登录 → 清理 token，让 permission.js 处理重定向
    if (!window.location.hash.includes('/login')) {
      if (store.getters.token) {
        removeToken()
      }
    }
    console.error('请求失败:', error)
    return Promise.reject(error)
  }
)

export default service

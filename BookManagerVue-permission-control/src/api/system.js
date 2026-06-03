import request from '@/utils/request'

export function queryLogsByPage(params) {
  return request({
    url: '/system/logs/queryByPage',
    method: 'get',
    params
  })
}

export function addLog(data) {
  return request({
    url: '/system/logs/add',
    method: 'post',
    data
  })
}

export function getAllConfig() {
  return request({
    url: '/system/config/getAll',
    method: 'get'
  })
}

export function updateConfig(data) {
  return request({
    url: '/system/config/update',
    method: 'post',
    data
  })
}

export function batchUpdateConfig(data) {
  return request({
    url: '/system/config/batchUpdate',
    method: 'post',
    data
  })
}

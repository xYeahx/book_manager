import request from '@/utils/request'

export function sendMessage(data) {
    return request({
        url: '/message/sendMessage',
        method: 'post',
        data
    })
}

export function queryMessagesByPage(params) {
    return request({
        url: '/message/queryMessagesByPage',
        method: 'get',
        params
    })
}

export function markAsRead(messageid) {
    return request({
        url: '/message/markAsRead',
        method: 'post',
        params: { messageid }
    })
}

export function getUnreadCount() {
    return request({
        url: '/message/getUnreadCount',
        method: 'get'
    })
}

export function sendReminder(userid, bookid, bookname) {
    return request({
        url: '/message/sendReminder',
        method: 'get',
        params: { userid, bookid, bookname }
    })
}

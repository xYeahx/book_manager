<template>
  <div class="dashboard-container">
    <div class="welcome-banner">
      <div class="banner-left">
        <h1 class="welcome-title">欢迎回来，{{ name || '用户' }}</h1>
        <p class="welcome-sub">{{ currentDate }} · {{ roleLabel }}</p>
      </div>
      <div class="banner-right">
        <div class="banner-stat" v-if="!isReader">
          <span class="banner-num">{{ overview.totalUsers }}</span>
          <span class="banner-label">注册用户</span>
        </div>
        <div class="banner-stat">
          <span class="banner-num">{{ overview.activeBorrows }}</span>
          <span class="banner-label">在借图书</span>
        </div>
        <div class="banner-stat highlight">
          <span class="banner-num">{{ overview.overdueCount }}</span>
          <span class="banner-label">逾期未还</span>
        </div>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card" v-for="(item, idx) in statCards" :key="idx" :style="{ '--accent': item.color }">
        <div class="stat-icon-wrap" :style="{ background: item.bg }">
          <i :class="item.icon"></i>
        </div>
        <div class="stat-body">
          <span class="stat-num">{{ item.value }}</span>
          <span class="stat-label">{{ item.label }}</span>
        </div>
        <div class="stat-trend" :class="item.trendClass">
          <i :class="item.trendIcon"></i>
        </div>
      </div>
    </div>

    <!-- 管理员/超级管理员：借阅趋势 + 图书分布 -->
    <el-row v-if="!isReader" :gutter="20" class="chart-row">
      <el-col :xs="24" :sm="24" :md="16">
        <el-card class="report-card" shadow="never">
          <div slot="header" class="card-header">
            <span><i class="el-icon-data-analysis"></i> 借阅趋势</span>
            <span class="header-sub">近7日借阅/归还统计</span>
          </div>
          <div class="bar-chart">
            <div class="chart-y-label">数量</div>
            <div class="chart-area">
              <div class="chart-bars">
                <div class="bar-group" v-for="(day, i) in weekData" :key="i">
                  <div class="bars-col">
                    <div class="bar bar-borrow" :style="{ height: getBarHeight(day.borrow, maxBarVal) + '%' }">
                      <span class="bar-tooltip" v-if="day.borrow > 0">借 {{ day.borrow }}</span>
                    </div>
                    <div class="bar bar-return" :style="{ height: getBarHeight(day.ret, maxBarVal) + '%' }">
                      <span class="bar-tooltip" v-if="day.ret > 0">还 {{ day.ret }}</span>
                    </div>
                  </div>
                  <span class="bar-day">{{ day.label }}</span>
                </div>
              </div>
              <div class="chart-grid-lines">
                <div class="grid-line" v-for="n in 5" :key="n"></div>
              </div>
            </div>
          </div>
          <div class="chart-legend">
            <span class="legend-item"><i class="legend-dot borrow-dot"></i>借出</span>
            <span class="legend-item"><i class="legend-dot return-dot"></i>归还</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="8">
        <el-card class="report-card" shadow="never">
          <div slot="header" class="card-header">
            <span><i class="el-icon-pie-chart"></i> 图书分布</span>
            <span class="header-sub">按类型统计</span>
          </div>
          <div class="type-list" v-if="typeStats.length > 0">
            <div class="type-item" v-for="(t, i) in typeStats" :key="i">
              <span class="type-name">{{ t.name }}</span>
              <div class="type-bar-track">
                <div class="type-bar-fill" :style="{ width: getTypePercent(t.count) + '%', background: typeColors[i % typeColors.length] }"></div>
              </div>
              <span class="type-count">{{ t.count }} 本</span>
            </div>
          </div>
          <div v-else class="empty-hint"><i class="el-icon-loading"></i> 加载中...</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 读者专属：我的借阅概览 + 消息提醒 -->
    <el-row v-else :gutter="20" class="chart-row">
      <el-col :xs="24" :sm="24" :md="14">
        <el-card class="report-card" shadow="never">
          <div slot="header" class="card-header">
            <span><i class="el-icon-reading"></i> 我的借阅概览</span>
            <router-link to="/bookmanage/borrow" class="view-all">查看全部 →</router-link>
          </div>
          <div class="borrow-overview" v-if="recentBorrows.length > 0">
            <div class="borrow-timeline">
              <div class="timeline-item" v-for="(item, i) in recentBorrows.slice(0, 5)" :key="i">
                <div class="tl-dot" :class="getTimelineClass(item)">
                  <i :class="item.returntimestr ? 'el-icon-check' : 'el-icon-reading'"></i>
                </div>
                <div class="tl-content">
                  <p class="tl-title">《{{ item.bookname }}》</p>
                  <p class="tl-meta">
                    <span v-if="!item.returntimestr" class="due-tag" :class="getDueTagClass(item)">{{ formatDueText(item) }}</span>
                    <span v-else class="returned-text">已归还 {{ formatReturnAgo(item.returntimestr) }}</span>
                  </p>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-hint"><i class="el-icon-collection"></i> 您还没有借阅记录，去浏览图书吧</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="10">
        <el-card class="report-card msg-card" shadow="never">
          <div slot="header" class="card-header">
            <span><i class="el-icon-bell"></i> 消息与提醒</span>
          </div>
          <div class="msg-list">
            <div class="msg-item unread" v-if="unreadMsgCount > 0" @click="$router.push('/message/reminder')">
              <div class="msg-icon-wrap warning"><i class="el-icon-bell"></i></div>
              <div class="msg-body">
                <p class="msg-title">您有 {{ unreadMsgCount }} 条未读催还通知</p>
                <p class="msg-desc">管理员发送的催还信息待查看</p>
              </div>
              <i class="msg-arrow el-icon-arrow-right"></i>
            </div>
            <div class="msg-item" v-else @click="$router.push('/message/reminder')">
              <div class="msg-icon-wrap ok"><i class="el-icon-check"></i></div>
              <div class="msg-body">
                <p class="msg-title">暂无新消息</p>
                <p class="msg-desc">所有催还通知已读</p>
              </div>
              <i class="msg-arrow el-icon-arrow-right"></i>
            </div>
            <div class="msg-item tip">
              <div class="msg-icon-wrap info"><i class="el-icon-info"></i></div>
              <div class="msg-body">
                <p class="msg-title">阅读小贴士</p>
                <p class="msg-desc">每本图书可续借一次，请在到期前操作</p>
              </div>
            </div>
            <div class="msg-item tip">
              <div class="msg-icon-wrap info"><i class="el-icon-time"></i></div>
              <div class="msg-body">
                <p class="msg-title">借阅期限</p>
                <p class="msg-desc">默认借期为 14 天，请按时归还</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="activity-row">
      <el-col :xs="24" :sm="24" :md="14">
        <el-card class="report-card activity-card" shadow="never">
          <div slot="header" class="card-header">
            <span><i class="el-icon-time"></i> {{ isReader ? '最近借阅动态' : '最近借阅动态' }}</span>
            <router-link :to="isReader ? '/bookmanage/borrow' : '/bookmanage/borrow'" class="view-all">{{ isReader ? '查看全部 →' : '管理借阅 →' }}</router-link>
          </div>
          <div class="activity-list" v-if="recentBorrows.length > 0">
            <div class="activity-item" v-for="(item, i) in recentBorrows" :key="i">
              <div class="act-icon" :class="item.returntimestr ? 'returned' : 'borrowed'">
                <i :class="item.returntimestr ? 'el-icon-check' : 'el-icon-reading'"></i>
              </div>
              <div class="act-content">
                <p class="act-title">《{{ item.bookname }}》</p>
                <p class="act-meta">
                  <span v-if="isReader && !item.returntimestr" class="due-tag" :class="getDueTagClass(item)">{{ formatDueText(item) }}</span>
                  <span v-else>{{ item.returntimestr ? '已归还于 ' + item.returntimestr : '借于 ' + item.borrowtimestr }}</span>
                  <span v-if="!isReader" class="act-user">{{ item.username }}</span>
                </p>
              </div>
            </div>
          </div>
          <div v-else class="empty-hint">暂无借阅记录</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="10">
        <el-card class="report-card quick-card" shadow="never">
          <div slot="header" class="card-header">
            <span><i class="el-icon-guide"></i> 常用功能</span>
          </div>
          <div class="quick-grid">
            <router-link v-for="(q, i) in quickLinks" :key="i" :to="q.path" class="quick-item" :style="{ '--qcolor': q.color }">
              <div class="quick-icon"><i :class="q.icon"></i></div>
              <span>{{ q.label }}</span>
            </router-link>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getCount as getUserCount } from '@/api/user'
import { getCount as getBookCount, getBookCountByType } from '@/api/bookinfo'
import { getCount as getTypeCount, queryBookTypes } from '@/api/booktype'
import { queryBorrowsByPage, getCount as getBorrowCount, getWeeklyStats } from '@/api/borrow'
import { getUnreadCount } from '@/api/message'

export default {
  name: 'Dashboard',
  data() {
    return {
      currentDate: '',
      overview: { totalUsers: '--', activeBorrows: '--', overdueCount: '--' },
      statCards: [],
      weekData: [],
      recentBorrows: [],
      typeStats: [],
      typeColors: ['#667eea', '#f5576c', '#43e97b', '#f093fb', '#4facfe', '#fa709a', '#fee140'],
      maxBarVal: 1,
      unreadMsgCount: 0
    }
  },
  computed: {
    ...mapGetters(['name', 'roles', 'id']),
    isAdmin() {
      return this.roles && (this.roles.includes('admin') || this.roles.includes('super_admin'))
    },
    isSuperAdmin() {
      return this.roles && this.roles.includes('super_admin')
    },
    isReader() {
      return this.roles && this.roles.includes('reader') && !this.isAdmin
    },
    roleLabel() {
      if (this.isSuperAdmin) return '超级管理员'
      if (this.isAdmin) return '管理员'
      return '读者'
    },
    quickLinks() {
      if (this.isReader) {
        return [
          { path: '/bookmanage/bookinfo', icon: 'el-icon-search', label: '浏览图书', color: '#667eea' },
          { path: '/bookmanage/borrow', icon: 'el-icon-document-checked', label: '借阅记录', color: '#f5576c' },
          { path: '/message/reminder', icon: 'el-icon-bell', label: '催还信息', color: '#E6A23C' },
          { path: '/account/password', icon: 'el-icon-lock', label: '修改密码', color: '#4facfe' },
          { path: '/account/profile', icon: 'el-icon-user', label: '账户信息', color: '#43e97b' }
        ]
      }
      return [
        { path: '/bookmanage/bookinfo', icon: 'el-icon-document-add', label: '图书管理', color: '#667eea' },
        { path: '/bookmanage/borrow', icon: 'el-icon-tickets', label: '借阅管理', color: '#f5576c' },
        { path: '/account/user', icon: 'el-icon-user-solid', label: '用户管理', color: '#4facfe' },
        { path: '/system/logs', icon: 'el-icon-document', label: '操作日志', color: '#43e97b' },
        { path: '/system/settings', icon: 'el-icon-setting', label: '系统设置', color: '#909399' }
      ]
    }
  },
  mounted() {
    this.setCurrentDate()
    this.loadAllData()
  },
  methods: {
    setCurrentDate() {
      const now = new Date()
      const w = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      this.currentDate = `${now.getFullYear()}年${m}月${d}日 ${w[now.getDay()]}`
    },
    async loadAllData() {
      try {
        const params = this.isReader ? { page: 1, limit: 5, userid: this.id } : { page: 1, limit: 5 }
        const [userRes, bookRes, typeRes, borrowRes, borrowListRes] = await Promise.all([
          getUserCount(),
          getBookCount(),
          getTypeCount(),
          getBorrowCount(),
          queryBorrowsByPage(params)
        ])

        const totalBooks = bookRes !== undefined && bookRes !== null ? bookRes : 0
        const totalTypes = typeRes !== undefined && typeRes !== null ? typeRes : 0
        const activeBorrows = borrowRes !== undefined && borrowRes !== null ? borrowRes : 0
        const totalUsers = userRes !== undefined && userRes !== null ? userRes : 0

        let myBorrowCount = 0
        let myOverdueCount = 0
        if (borrowListRes && borrowListRes.data) {
          this.recentBorrows = borrowListRes.data
          myBorrowCount = borrowListRes.count || 0
          borrowListRes.data.forEach(b => {
            if (!b.returntimestr) {
              const due = b.duetime ? new Date(b.duetime).getTime() : new Date(b.borrowtime).getTime() + 14 * 86400000
              if (Date.now() > due) myOverdueCount++
            }
          })
        }

        this.overview = {
          totalUsers,
          activeBorrows,
          overdueCount: this.isReader ? myOverdueCount : Math.min(myOverdueCount, activeBorrows)
        }

        if (this.isReader) {
          const activeBooks = this.recentBorrows.filter(b => !b.returntimestr).length
          const soonDue = this.recentBorrows.filter(b => {
            if (b.returntimestr) return false
            const due = b.duetime ? new Date(b.duetime).getTime() : new Date(b.borrowtime).getTime() + 14 * 86400000
            const diff = (due - Date.now()) / 86400000
            return diff > 0 && diff <= 3
          }).length
          this.statCards = [
            { icon: 'el-icon-tickets', label: '累计借阅', value: myBorrowCount, color: '#667eea', bg: 'linear-gradient(135deg,#667eea11,#764ba211)', trendIcon: 'el-icon-top-right', trendClass: 'up' },
            { icon: 'el-icon-reading', label: '在借图书', value: activeBooks, color: '#43e97b', bg: 'linear-gradient(135deg,#43e97b11,#38f9d711)', trendIcon: 'el-icon-top-right', trendClass: 'up' },
            { icon: 'el-icon-warning', label: '即将到期', value: soonDue, color: '#E6A23C', bg: 'linear-gradient(135deg,#E6A23C11,#F56C6C11)', trendIcon: 'el-icon-bottom-left', trendClass: 'down' },
            { icon: 'el-icon-success', label: '已归还', value: myBorrowCount - activeBooks, color: '#67C23A', bg: 'linear-gradient(135deg,#67C23A11,#85ce6111)', trendIcon: 'el-icon-circle-check', trendClass: 'up' }
          ]

          try {
            const msgRes = await getUnreadCount()
            if (msgRes && msgRes.status === 200 && msgRes.data !== undefined) {
              this.unreadMsgCount = msgRes.data
            }
          } catch (e) {}
        } else {
          this.statCards = [
            { icon: 'el-icon-reading', label: '馆藏图书', value: totalBooks, color: '#667eea', bg: 'linear-gradient(135deg,#667eea11,#764ba211)', trendIcon: 'el-icon-top-right', trendClass: 'up' },
            { icon: 'el-icon-notebook-2', label: '在借数量', value: activeBorrows, color: '#f5576c', bg: 'linear-gradient(135deg,#f5576c11,#f08a5d11)', trendIcon: 'el-icon-top-right', trendClass: 'up' },
            { icon: 'el-icon-user', label: '注册用户', value: totalUsers, color: '#4facfe', bg: 'linear-gradient(135deg,#4facfe11,#00f2fe11)', trendIcon: 'el-icon-top-right', trendClass: 'up' },
            { icon: 'el-icon-menu', label: '图书类型', value: totalTypes, color: '#43e97b', bg: 'linear-gradient(135deg,#43e97b11,#38f9d711)', trendIcon: 'el-icon-top-right', trendClass: 'up' }
          ]
          await this.loadWeekData()
          await this.loadTypeStats(totalBooks)
        }
      } catch (e) {
        console.error('加载数据失败', e)
      }
    },
    async loadWeekData() {
      try {
        const res = await getWeeklyStats()
        if (res && res.data) {
          const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
          const raw = res.data.map(item => ({
            dayOfWeek: new Date(item.statDate).getDay(),
            label: dayNames[new Date(item.statDate).getDay()],
            borrow: item.borrowCount || 0,
            ret: item.returnCount || 0
          }))
          raw.sort((a, b) => a.dayOfWeek - b.dayOfWeek)
          this.weekData = raw
          this.maxBarVal = Math.max(...this.weekData.map(d => Math.max(d.borrow, d.ret)), 1)
        } else {
          this.fallbackWeekData()
        }
      } catch (e) {
        console.log('加载周统计数据失败，使用默认数据')
        this.fallbackWeekData()
      }
    },
    fallbackWeekData() {
      const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      const today = new Date().getDay()
      const result = []
      for (let i = 6; i >= 0; i--) {
        const d = (today - i + 7) % 7
        result.push({ dayOfWeek: d, label: dayNames[d === 0 ? 6 : d - 1], borrow: 0, ret: 0 })
      }
      result.sort((a, b) => a.dayOfWeek - b.dayOfWeek)
      this.weekData = result
      this.maxBarVal = 1
    },
    async loadTypeStats(totalBooks) {
      try {
        const res = await getBookCountByType()
        if (res && res.data) {
          this.typeStats = res.data.map(t => ({
            name: t.typeName,
            count: t.bookCount || 0
          }))
        } else {
          this.fallbackTypeStats()
        }
      } catch (e) {
        this.fallbackTypeStats()
      }
    },
    fallbackTypeStats() {
      this.typeStats = []
    },
    getBarHeight(val, max) {
      if (!max) return 0
      return Math.round((val / max) * 85)
    },
    getTypePercent(count) {
      const total = this.typeStats.reduce((s, t) => s + t.count, 0)
      if (!total) return 0
      return Math.round((count / total) * 100)
    },
    getDueTagClass(item) {
      if (!item.duetime && !item.borrowtime) return ''
      const due = item.duetime ? new Date(item.duetime).getTime() : new Date(item.borrowtime).getTime() + 14 * 86400000
      const diff = (due - Date.now()) / 86400000
      if (diff < 0) return 'overdue'
      if (diff <= 3) return 'warning'
      return ''
    },
    formatDueText(item) {
      if (!item.duetime && !item.borrowtime) return ''
      const due = item.duetime ? new Date(item.duetime) : new Date(new Date(item.borrowtime).getTime() + 14 * 86400000)
      const m = String(due.getMonth() + 1).padStart(2, '0')
      const dd = String(due.getDate()).padStart(2, '0')
      const diff = Math.ceil((due.getTime() - Date.now()) / 86400000)
      if (diff < 0) return `已逾期 ${Math.abs(diff)} 天`
      if (diff <= 3) return `${m}/${dd} 到期（剩${diff}天）`
      return `${m}/${dd} 到期`
    },
    formatReturnAgo(dateStr) {
      if (!dateStr) return ''
      const diff = Date.now() - new Date(dateStr).getTime()
      const days = Math.floor(diff / 86400000)
      if (days === 0) return '今天'
      if (days === 1) return '昨天'
      if (days < 7) return `${days}天前`
      return dateStr.substring(5, 16)
    },
    getTimelineClass(item) {
      if (item.returntimestr) return 'done'
      const cls = this.getDueTagClass(item)
      if (cls === 'overdue') return 'danger'
      if (cls === 'warning') return 'warn'
      return 'active'
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #409EFF;
$bg: #f5f7fa;

.dashboard-container { padding: 18px; background: $bg; min-height: calc(100vh - 84px); }

.welcome-banner {
  display: flex; justify-content: space-between; align-items: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  border-radius: 14px; padding: 26px 32px; margin-bottom: 20px;
  color: #fff; position: relative; overflow: hidden;

  &::before { content: ''; position: absolute; top: -60px; right: -40px; width: 300px; height: 300px; background: rgba(255,255,255,.04); border-radius: 50%; }

  .welcome-title { font-size: 24px; font-weight: 700; margin: 0 0 6px; letter-spacing: .5px; }
  .welcome-sub { font-size: 13.5px; opacity: .75; margin: 0; }

  .banner-right { display: flex; gap: 28px; position: relative; z-index: 1; }
  .banner-stat {
    text-align: center; padding: 10px 18px;
    background: rgba(255,255,255,.07); border-radius: 10px; backdrop-filter: blur(6px);
    border: 1px solid rgba(255,255,255,.08);
    .banner-num { display: block; font-size: 26px; font-weight: 800; line-height: 1.2; }
    .banner-label { font-size: 12px; opacity: .7; margin-top: 2px; display: block; }
    &.highlight { background: rgba(245,108,108,.15); border-color: rgba(245,108,108,.25); .banner-num { color: #f56c6c; } }
  }
}

.stats-row {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px;
  @media (max-width: 1100px) { grid-template-columns: repeat(2, 1fr); }

  .stat-card {
    background: #fff; border-radius: 12px; padding: 20px 22px;
    display: flex; align-items: center; gap: 16px;
    box-shadow: 0 2px 12px rgba(0,0,0,.04);
    transition: transform .25s, box-shadow .25s; position: relative; overflow: hidden;
    &:hover { transform: translateY(-3px); box-shadow: 0 8px 26px rgba(0,0,0,.08); }

    .stat-icon-wrap { width: 52px; height: 52px; border-radius: 13px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; i { font-size: 24px; color: var(--accent); } }
    .stat-body { display: flex; flex-direction: column;
      .stat-num { font-size: 28px; font-weight: 800; color: #303133; line-height: 1.15; }
      .stat-label { font-size: 13px; color: #909399; margin-top: 3px; }
    }
    .stat-trend {
      position: absolute; right: 16px; top: 50%; transform: translateY(-50%);
      width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
      i { font-size: 14px; }
      &.up { background: #f0f9eb; color: #67C23A; }
      &.down { background: #fdf6ec; color: #E6A23C; }
    }
  }
}

.chart-row, .activity-row { margin-bottom: 20px; }

.report-card {
  border-radius: 12px; border: none; height: 100%;
  ::v-deep .el-card__header { padding: 16px 22px; border-bottom: 1px solid #f0f0f0; }
  ::v-deep .el-card__body { padding: 20px 22px; }

  .card-header {
    display: flex; align-items: center; justify-content: space-between;
    font-size: 15.5px; font-weight: 700; color: #303133;
    i { color: $primary; margin-right: 6px; font-size: 17px; }
    .header-sub { font-size: 12px; font-weight: 400; color: #909399; margin-left: 8px; }
    .view-all { font-size: 13px; color: $primary; font-weight: 500; text-decoration: none; &:hover { text-decoration: underline; } }
  }
}

.bar-chart {
  display: flex; padding-top: 10px;
  .chart-y-label { writing-mode: vertical-lr; font-size: 11px; color: #c0c4cc; display: flex; align-items: center; justify-content: center; padding: 0 6px; flex-shrink: 0; letter-spacing: 2px; }
  .chart-area {
    flex: 1; position: relative; height: 200px; padding: 0 10px 28px;
    .chart-grid-lines { position: absolute; left: 0; right: 0; top: 0; bottom: 28px; display: flex; flex-direction: column; justify-content: space-between; pointer-events: none;
      .grid-line { height: 1px; background: #f0f0f0; }
    }
    .chart-bars { display: flex; align-items: flex-end; justify-content: space-around; height: 172px; position: relative; z-index: 1;
      .bar-group { display: flex; flex-direction: column; align-items: center; flex: 1; }
      .bars-col { display: flex; align-items: flex-end; gap: 4px; height: 160px; }
      .bar { width: 22px; border-radius: 4px 4px 0 0; position: relative; transition: height .5s ease; min-height: 0; cursor: pointer;
        &.bar-borrow { background: linear-gradient(180deg, #667eea, #764ba2); }
        &.bar-return { background: linear-gradient(180deg, #43e97b, #38f9d7); }
        .bar-tooltip { position: absolute; top: -24px; left: 50%; transform: translateX(-50%); background: #303133; color: #fff; font-size: 11px; padding: 2px 7px; border-radius: 4px; white-space: nowrap; opacity: 0; pointer-events: none; transition: opacity .2s;
          &::after { content:''; position: absolute; bottom: -4px; left: 50%; transform: translateX(-50%); border: 4px solid transparent; border-top-color: #303133; }
        }
        &:hover .bar-tooltip { opacity: 1; }
      }
      .bar-day { font-size: 12px; color: #909399; margin-top: 8px; }
    }
  }
}
.chart-legend { display: flex; justify-content: center; gap: 24px; margin-top: 14px;
  .legend-item { display: flex; align-items: center; gap: 6px; font-size: 12.5px; color: #606266;
    .legend-dot { display: inline-block; width: 10px; height: 10px; border-radius: 3px; }
    .borrow-dot { background: linear-gradient(135deg, #667eea, #764ba2); }
    .return-dot { background: linear-gradient(135deg, #43e97b, #38f9d7); }
  }
}

.type-list { display: flex; flex-direction: column; gap: 16px; padding: 4px 0;
  .type-item { display: grid; grid-template-columns: 72px 1fr 48px; gap: 12px; align-items: center;

    .type-name {
      font-size: 13.5px; color: #303133; font-weight: 500;
      text-align: right; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
    }

    .type-bar-track {
      height: 18px; background: #f2f3f5; border-radius: 9px; overflow: hidden;
      position: relative; min-width: 0;

      .type-bar-fill {
        height: 100%; border-radius: 9px; transition: width .8s cubic-bezier(.22,1,.36,1);
        min-width: 0; position: relative;
        &::after {
          content: ''; position: absolute; top: 3px; right: 6px;
          width: 4px; height: 4px; background: rgba(255,255,255,.7); border-radius: 50%;
        }
      }
    }

    .type-count { font-size: 13px; color: #909399; font-weight: 700; }
  }
}

/* 读者专属：借阅时间线 */
.borrow-overview { padding: 4px 0; }
.borrow-timeline { position: relative; padding-left: 28px;
  &::before { content: ''; position: absolute; left: 10px; top: 0; bottom: 0; width: 2px; background: #ebeef5; }
  .timeline-item { position: relative; padding: 12px 0 18px;
    .tl-dot {
      position: absolute; left: -24px; top: 14px; width: 28px; height: 28px; border-radius: 50%;
      display: flex; align-items: center; justify-content: center; z-index: 1;
      i { font-size: 13px; color: #fff; }
      &.active { background: linear-gradient(135deg, #667eea, #764ba2); }
      &.warn { background: linear-gradient(135deg, #E6A23C, #F56C6C); }
      &.danger { background: linear-gradient(135deg, #F56C6C, #f78989); animation: pulse 2s infinite; }
      &.done { background: linear-gradient(135deg, #43e97b, #38f9d7); }
    }
    .tl-content {
      .tl-title { font-size: 14px; font-weight: 600; color: #303133; margin: 0 0 4px; }
      .tl-meta { font-size: 12.5px; color: #909399; margin: 0;
        .due-tag { padding: 1px 8px; border-radius: 10px; font-size: 11.5px; font-weight: 600;
          &.overdue { background: #fef0f0; color: #F56C6C; }
          &.warning { background: #fdf6ec; color: #E6A23C; }
        }
        .returned-text { color: #67C23A; font-weight: 500; }
      }
    }
  }
}

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }

/* 读者专属：消息卡片 */
.msg-card { ::v-deep .el-card__body { padding: 16px 20px; } }
.msg-list { display: flex; flex-direction: column; gap: 10px;
  .msg-item {
    display: flex; align-items: center; gap: 12px; padding: 12px 14px;
    border-radius: 10px; transition: all .2s; cursor: default; text-decoration: none; color: inherit;
    &:not(.tip):hover { background: #fafbfc; transform: translateX(4px); }
    &.unread { background: linear-gradient(135deg, #fffbf0, #fff); border: 1px solid #faecd8; cursor: pointer;
      .msg-arrow { color: #E6A23C; }
    }
    &.tip { background: #fafbfc; cursor: default; }
    .msg-icon-wrap {
      width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0;
      i { font-size: 16px; }
      &.warning { background: #fdf6ec; color: #E6A23C; }
      &.ok { background: #f0f9eb; color: #67C23A; }
      &.info { background: #ecf5ff; color: $primary; }
    }
    .msg-body { flex: 1; min-width: 0;
      .msg-title { font-size: 13.5px; font-weight: 600; color: #303133; margin: 0 0 2px; }
      .msg-desc { font-size: 12px; color: #909399; margin: 0; }
    }
    .msg-arrow { font-size: 14px; color: #c0c4cc; transition: color .2s; flex-shrink: 0; }
  }
}

.activity-list { display: flex; flex-direction: column; gap: 2px;
  .activity-item {
    display: flex; align-items: center; gap: 14px; padding: 12px 8px; border-radius: 8px; transition: background .2s;
    &:hover { background: #fafbfc; }
    .act-icon {
      width: 38px; height: 38px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; i { font-size: 17px; color: #fff; }
      &.borrowed { background: linear-gradient(135deg, #667eea, #764ba2); }
      &.returned { background: linear-gradient(135deg, #43e97b, #38f9d7); }
    }
    .act-content { flex: 1; min-width: 0;
      .act-title { font-size: 14px; font-weight: 600; color: #303133; margin: 0 0 3px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
      .act-meta { font-size: 12.5px; color: #909399; margin: 0; display: flex; align-items: center; gap: 10px; flex-wrap: wrap;
        .due-tag { padding: 1px 8px; border-radius: 10px; font-size: 11.5px; font-weight: 600;
          &.overdue { background: #fef0f0; color: #F56C6C; }
          &.warning { background: #fdf6ec; color: #E6A23C; }
        }
        .act-user { color: #606266; background: #f5f7fa; padding: 1px 8px; border-radius: 8px; }
      }
    }
  }
}

.quick-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;
  .quick-item {
    display: flex; align-items: center; gap: 10px; padding: 12px 14px; border-radius: 10px; background: #fafbfc; text-decoration: none; color: inherit;
    transition: all .25s; border: 1px solid transparent;
    &:hover { background: #fff; box-shadow: 0 4px 14px rgba(0,0,0,.06); border-color: var(--qcolor); transform: translateY(-1px); }
    .quick-icon { width: 36px; height: 36px; border-radius: 9px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; background: linear-gradient(135deg, var(--qcolor), var(--qcolor)); opacity: .12; i { font-size: 18px; color: var(--qcolor); } }
    span { font-size: 13.5px; font-weight: 600; color: #303133; }
  }
}

.empty-hint { text-align: center; color: #c0c4cc; padding: 30px 0; font-size: 13.5px; i { margin-right: 4px; } }

@media (max-width: 900px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .welcome-banner { flex-direction: column; text-align: center; padding: 22px 20px; gap: 16px; .banner-right { justify-content: center; } }
  .quick-grid { grid-template-columns: 1fr; }
}
</style>

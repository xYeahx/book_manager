<template>
  <div class="app-container">
    <div class="page-header">
      <i class="el-icon-bell header-icon"></i>
      <span class="header-title">催还信息</span>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="unread-badge">
        <span class="badge-text">未读消息</span>
      </el-badge>
    </div>

    <div v-if="tableData.length === 0 && !loading" class="empty-state">
      <i class="el-icon-message"></i>
      <p>暂无催还消息</p>
      <p class="empty-tip">管理员发送的催还通知将显示在这里</p>
    </div>

    <div v-else class="message-list">
      <div v-for="item in tableData" :key="item.messageid"
           class="message-card" :class="{ 'is-unread': item.isread === 0 }"
           @click="handleRead(item)">
        <div class="message-icon">
          <i :class="item.isread === 0 ? 'el-icon-bell' : 'el-icon-bell-outline'"
             :style="{ color: item.isread === 0 ? '#E6A23C' : '#C0C4CC' }"></i>
        </div>
        <div class="message-body">
          <div class="message-header">
            <span class="message-title">{{ item.title }}</span>
            <span class="message-time">{{ item.createtimestr }}</span>
          </div>
          <div class="message-content">{{ item.content }}</div>
          <div v-if="item.isread === 0" class="unread-dot"></div>
        </div>
      </div>
    </div>

    <div v-if="recordTotal > queryParam.limit" class="pagination-wrap">
      <el-pagination
        background
        :current-page.sync="queryParam.page"
        :page-sizes="[10, 20, 50]"
        :page-size="queryParam.limit"
        layout="total, sizes, prev, pager, next"
        :total="recordTotal"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { queryMessagesByPage, markAsRead, getUnreadCount } from '@/api/message'

export default {
  name: 'Reminder',
  data() {
    return {
      tableData: [],
      recordTotal: 0,
      unreadCount: 0,
      loading: false,
      queryParam: {
        page: 1,
        limit: 10
      }
    }
  },
  created() {
    this.loadData()
    this.loadUnreadCount()
  },
  methods: {
    loadData() {
      this.loading = true
      const params = { ...this.queryParam, userid: this.id }
      queryMessagesByPage(params).then(res => {
        this.tableData = res.data || []
        this.recordTotal = res.count || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    loadUnreadCount() {
      getUnreadCount().then(res => {
        if (res.status === 200) {
          this.unreadCount = res.data || 0
        }
      })
    },
    handleSizeChange(curSize) {
      this.queryParam.limit = curSize
      this.loadData()
    },
    handleCurrentChange(curPage) {
      this.queryParam.page = curPage
      this.loadData()
    },
    handleRead(item) {
      if (item.isread === 0) {
        markAsRead(item.messageid).then(() => {
          item.isread = 1
          if (this.unreadCount > 0) this.unreadCount--
          this.$set(item, 'isread', 1)
        })
      }
      this.$alert(item.content, item.title, {
        confirmButtonText: '我知道了',
        type: 'warning',
        customClass: 'reminder-dialog',
        dangerouslyUseHTMLString: false
      })
    }
  },
  computed: {
    ...mapGetters(['id', 'roles'])
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;

  .header-icon {
    font-size: 24px;
    color: #E6A23C;
  }
  .header-title {
    font-size: 20px;
    font-weight: 700;
    color: #303133;
  }

  .unread-badge {
    margin-left: auto;

    .badge-text {
      font-size: 13px;
      color: #909399;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
  color: #909399;

  i { font-size: 64px; margin-bottom: 16px; color: #dcdfe6; }
  p { margin: 4px 0; font-size: 15px; }
  .empty-tip { font-size: 13px; color: #c0c4cc; margin-top: 8px; }
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-card {
  display: flex;
  gap: 16px;
  padding: 18px 20px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;

  &:hover {
    box-shadow: 0 4px 16px rgba(230, 162, 60, 0.12);
    border-color: #E6A23C;
    transform: translateY(-1px);
  }

  &.is-unread {
    background: linear-gradient(135deg, #fffbf0 0%, #fff 100%);
    border-color: #faecd8;
    border-left: 4px solid #E6A23C;
  }

  .message-icon {
    flex-shrink: 0;
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fdf6ec;
    border-radius: 50%;

    i { font-size: 22px; }
  }

  .message-body {
    flex: 1;
    min-width: 0;
    position: relative;

    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .message-title {
        font-size: 15px;
        font-weight: 600;
        color: #303133;
      }
      .message-time {
        font-size: 12px;
        color: #c0c4cc;
        flex-shrink: 0;
        margin-left: 16px;
      }
    }

    .message-content {
      font-size: 14px;
      color: #606266;
      line-height: 1.6;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .unread-dot {
      position: absolute;
      top: 2px;
      right: 0;
      width: 8px;
      height: 8px;
      background: #E6A23C;
      border-radius: 50%;
      animation: blink 1.5s infinite;
    }
  }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px 0 4px;
}
</style>

<style>
.reminder-dialog {
  border-radius: 12px;
  width: 420px !important;

  .el-message-box__title {
    font-weight: 700;
    font-size: 17px;
  }
  .el-message-box__message {
    font-size: 14px;
    line-height: 1.7;
    color: #606266;
    white-space: pre-wrap;
  }
}
</style>

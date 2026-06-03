<template>
  <div class="app-container">
    <!-- 顶部功能 -->
    <div class="filter-container">
      <el-input v-model="queryParam.username" v-permission="['admin']" placeholder="用户名" style="width: 200px;" class="filter-item" clearable @keyup.enter.native="handleFilter" />
      <el-input v-model="queryParam.bookname" placeholder="图书名" style="width: 200px;" class="filter-item" clearable @keyup.enter.native="handleFilter" />
      <el-button v-waves class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">搜索</el-button>
      <el-button v-waves class="filter-item" type="default" @click="handleShowAll">显示全部</el-button>
      <el-button v-permission="['admin']" class="filter-item" type="danger" icon="el-icon-delete" @click="handleDeleteSome">批量删除</el-button>
    </div>

    <!--数据表格-->
    <el-table ref="multipleTable" :data="tableData" border stripe style="width: 100%" class="borrow-table">
      <el-table-column fixed type="selection" width="46" align="center" v-if="roleIsAdmin" />
      <el-table-column fixed prop="borrowid" label="序号" width="58" align="center" />
      <el-table-column
        prop="username"
        label="用户名"
        show-overflow-tooltip
        v-if="roleIsAdmin"
        width="95"
      />
      <el-table-column prop="bookname" label="图书名" show-overflow-tooltip min-width="150">
        <template slot-scope="scope">
          <span class="book-name">{{ scope.row.bookname }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="borrowtimestr" label="借书时间" width="168" />
      <el-table-column label="到期时间" width="128">
        <template slot-scope="scope">
          <span class="due-tag" :class="getDueClass(scope.row)">
            <i :class="getDueIcon(scope.row)"></i>
            {{ scope.row.duetime || formatDueDate(scope.row.borrowtime) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="还书时间" width="160">
        <template slot-scope="scope">
          <span v-if="!scope.row.returntimestr" class="status-pending">等待还书</span>
          <span v-else class="status-returned">{{ scope.row.returntimestr }}</span>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" :width="roleIsAdmin ? '160' : '180'" align="center">
        <template slot-scope="scope">
          <template v-if="roleIsAdmin">
            <div class="action-btns">
              <el-button v-if="!scope.row.returntimestr" type="warning" size="small" icon="el-icon-bell" plain round @click="handleRemind(scope.row)">催还</el-button>
              <el-button type="danger" size="small" icon="el-icon-delete" plain round @click="handleDelete(scope.row, scope.$index)">删除</el-button>
            </div>
          </template>
          <template v-else>
            <div class="action-btns">
              <el-button type="text" size="small" icon="el-icon-refresh-right" style="color: #E6A23C; margin-right: 8px;" :disabled="scope.row.returntimestr != null" @click="handleRenew(scope.row)">续借</el-button>
              <el-button v-if="!scope.row.returntimestr" type="success" size="small" icon="el-icon-refresh-right" @click="handleReturn(scope.row, scope.$index)">归还图书</el-button>
              <span v-else class="returned-badge">已归还</span>
            </div>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!--分页条-->
    <div class="pagination-wrap">
      <el-pagination background :current-page.sync="queryParam.page" :page-sizes="[5, 10, 20, 50]" :page-size="queryParam.limit" layout="total, sizes, prev, pager, next, jumper" :total="recordTotal" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <!-- 催还对话框 -->
    <el-dialog title="催还通知" :visible.sync="remindDialogVisible" width="460px" custom-class="remind-dialog">
      <div class="remind-content">
        <div class="remind-book-info">
          <i class="el-icon-reading"></i>
          <div class="info-text">
            <p><strong>图书：</strong>{{ remindBook.bookname }}</p>
            <p><strong>借阅人：</strong>{{ remindBook.username }}</p>
            <p><strong>最迟归还：</strong>{{ formatDueDate(remindBook.borrowtime) }}</p>
          </div>
        </div>
        <el-input type="textarea" v-model="remindMessage" :rows="3" placeholder="请输入催还备注信息（可选）" resize="none" maxlength="200" show-word-limit />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="remindDialogVisible = false">取 消</el-button>
        <el-button type="warning" icon="el-icon-message-solid" @click="submitRemind">发送催还</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import permission from '@/directive/permission/index.js'
import waves from '@/directive/waves'
import { queryBorrowsByPage, deleteBorrow, deleteBorrows, returnBook, renewBorrow } from '@/api/borrow'
import { sendReminder } from '@/api/message'

export default {
  name: 'Borrow',
  directives: { waves, permission },
  data() {
    return {
      tableData: [],
      recordTotal: 0,
      queryParam: {
        page: 1,
        limit: 10,
        userid: null,
        username: null,
        bookname: null
      },
      remindDialogVisible: false,
      remindBook: {},
      remindMessage: ''
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      queryBorrowsByPage(this.queryParam).then(res => {
        this.tableData = res.data
        this.recordTotal = res.count
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
    handleFilter() {
      this.queryParam.page = 1
      this.loadData()
    },
    handleShowAll() {
      this.queryParam.page = 1
      this.queryParam.username = null
      this.queryParam.bookname = null
      this.loadData()
    },
    handleDelete(row, index) {
      this.$confirm('确定要删除该条记录吗?', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(() => {
        deleteBorrow(row).then(res => {
          if (res === 1) {
            this.$message.success('删除记录成功')
            this.tableData.splice(index, 1)
            if (this.tableData.length === 0 && this.queryParam.page > 1) {
              this.queryParam.page--
              this.handleCurrentChange(this.queryParam.page)
            }
          } else {
            this.$message.error('删除记录失败')
          }
        })
      })
    },
    handleDeleteSome() {
      this.$confirm('确定要删除这些记录吗?', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(() => {
        const items = this.$refs.multipleTable.selection
        deleteBorrows(items).then(res => {
          if (res > 0) {
            this.$message.success('删除' + res + '条记录成功')
            if (this.tableData.length <= res && this.queryParam.page > 1) this.queryParam.page--
            this.handleCurrentChange(this.queryParam.page)
          } else {
            this.$message.error('删除记录失败')
          }
        })
      })
    },
    handleReturn(row, index) {
      this.$confirm('确定要归还《' + row.bookname + '》吗?', '确认还书', { confirmButtonText: '确定归还', cancelButtonText: '取消', type: 'success' }).then(() => {
        returnBook(row.borrowid, row.bookid).then(res => {
          if (res === 1) {
            this.$message.success('还书成功，感谢您的配合！')
            this.handleCurrentChange(this.queryParam.page)
          } else {
            this.$message.error('还书失败，请稍后重试')
          }
        })
      }).catch(() => {})
    },
    handleRenew(row) {
      if (row.returntime !== null && row.returntime !== '') {
        this.$message.warning('该书已归还，无法续借')
        return
      }
      const dueDate = row.duetime ? new Date(row.duetime).toLocaleDateString() : ''
      this.$confirm(`确定要续借《${row.bookname}》吗？`, '续借确认', {
        confirmButtonText: '确定续借',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        renewBorrow(row.borrowid, row.bookid).then(res => {
          if (res === 1) {
            this.$message.success('续借成功！借期已延长14天')
            this.loadData()
          } else if (res === -1) {
            this.$message.warning('该书已归还，无法续借')
          } else if (res === -2) {
            this.$message.error('借阅记录异常')
          } else if (res === -3) {
            this.$message.error('该图书已逾期，无法续借，请尽快归还')
          } else if (res === -4) {
            this.$message.warning('该图书已达到最大续借次数')
          } else {
            this.$message.error('续借失败')
          }
        })
      }).catch(() => {})
    },
    isOverdue(row) {
      return !row.returntimestr && row.borrowtime && new Date(row.borrowtime).getTime() + 14 * 86400000 < Date.now()
    },
    handleRemind(row) {
      this.remindBook = row
      const dueTimestamp = row.duetime ? new Date(row.duetime).getTime() : new Date(row.borrowtime).getTime() + 14 * 86400000
      if (Date.now() > dueTimestamp) {
        const overdueDays = Math.floor((Date.now() - dueTimestamp) / 86400000)
        this.remindMessage = `您好！您借阅的《${row.bookname}》已逾期${overdueDays}天，请尽快归还。`
      } else {
        const remainDays = Math.ceil((dueTimestamp - Date.now()) / 86400000)
        this.remindMessage = `您好！您借阅的《${row.bookname}》将于${remainDays}天后到期，请按时归还。`
      }
      this.remindDialogVisible = true
    },
    submitRemind() {
      sendReminder(this.remindBook.userid, this.remindBook.bookid, this.remindBook.bookname).then(res => {
        if (res.status === 200) {
          this.$notify({
            title: '催还成功',
            message: `已向 ${this.remindBook.username} 发送关于《${this.remindBook.bookname}》的催还通知`,
            type: 'warning',
            duration: 4000,
            position: 'top-right'
          })
        } else {
          this.$message.error(res.message || '催还信息发送失败')
        }
        this.remindDialogVisible = false
      }).catch(() => {
        this.$message.error('催还信息发送失败，请稍后重试')
        this.remindDialogVisible = false
      })
    },
    formatDueDate(borrowTime) {
      if (!borrowTime) return '--'
      const date = new Date(borrowTime)
      date.setDate(date.getDate() + 14)
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    },
    getDueClass(row) {
      if (row.returntimestr) return 'due-normal'
      const dueStr = row.duetime
      if (dueStr) {
        const due = new Date(dueStr)
        const now = new Date()
        const diffDays = Math.ceil((due - now) / (1000 * 60 * 60 * 24))
        if (diffDays < 0) return 'due-overdue'
        if (diffDays <= 3) return 'due-warning'
        return 'due-normal'
      }
      if (!row.borrowtime) return 'due-normal'
      const due = new Date(row.borrowtime)
      due.setDate(due.getDate() + 14)
      const now = new Date()
      const diffDays = Math.ceil((due - now) / (1000 * 60 * 60 * 24))
      if (diffDays < 0) return 'due-overdue'
      if (diffDays <= 3) return 'due-warning'
      return 'due-normal'
    },
    getDueIcon(row) {
      const cls = this.getDueClass(row)
      if (cls === 'due-overdue') return 'el-icon-warning'
      if (cls === 'due-warning') return 'el-icon-time'
      return 'el-icon-date'
    }
  },
  computed: {
    ...mapGetters(['id', 'roles']),
    roleIsAdmin() {
      return this.roles && (this.roles.includes('admin') || this.roles.includes('super_admin'))
    }
  },
  watch: {
    'queryParam.userid': {
      immediate: true,
      handler() {
        if (this.roleIsAdmin) {
          this.queryParam.userid = null
        } else {
          this.queryParam.userid = this.id
        }
      }
    }
  }
}
</script>

<style scoped>
.filter-container { margin-bottom: 15px; }

.borrow-table {
  ::v-deep .el-table__header th { background: #fafbfc; font-weight: 600; color: #303133; font-size: 13px; border-bottom: 2px solid #ebeef5; }
  ::v-deep .el-table__body tr:hover > td { background: #f5f7ff; }
  ::v-deep .el-table__body td { border-bottom: 1px solid #f0f2f7; padding: 10px 0; font-size: 13px; }

  .book-name { font-weight: 600; color: #303133; }

  .action-btns {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;

    .el-button {
      margin: 0 !important;
    }
  }
}

.status-pending { color: #F56C6C; font-weight: 500; }
.status-returned { color: #67C23A; font-weight: 500; }
.returned-badge { display: inline-block; padding: 2px 10px; border-radius: 10px; background: #f0f9eb; color: #67C23A; font-size: 12px; }

.due-tag {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 3px 10px; border-radius: 10px; font-size: 12px;
  i { font-size: 13px; }
  &.due-normal { color: #909399; background: #f5f7fa; }
  &.due-warning { color: #E6A23C; background: #fdf6ec; font-weight: 500; }
  &.due-overdue { color: #F56C6C; background: #fef0f0; font-weight: 600; animation: pulse 2s infinite; }
}
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }

.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; padding: 16px 0 4px; }

::v-deep .remind-dialog {
  border-radius: 12px;
  .el-dialog__header { padding: 16px 20px 12px; border-bottom: 1px solid #f0f0f0; }
  .el-dialog__title { font-weight: 700; font-size: 16px; color: #E6A23C; }
  .el-dialog__body { padding: 20px; }
  .el-dialog__footer { padding: 12px 20px 18px; border-top: 1px solid #f0f0f0; }
}

.remind-content {
  .remind-book-info {
    display: flex; gap: 14px; padding: 14px 18px;
    background: #fdf6ec; border-radius: 10px; border-left: 4px solid #E6A23C;
    margin-bottom: 16px;

    i { font-size: 36px; color: #E6A23C; flex-shrink: 0; line-height: 1; }
    .info-text p { margin: 4px 0; font-size: 13px; color: #606266; strong { color: #303133; } }
  }
}
</style>

<template>
  <div class="app-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">用户管理中心</h2>
        <p class="page-desc" v-if="isSuperAdmin">最高管理员 · 拥有全部用户管理权限</p>
        <p class="page-desc" v-else-if="isAdmin">管理员 · 仅可管理普通读者</p>
      </div>
      <div class="header-stats">
        <div class="stat-chip total">
          <i class="el-icon-user"></i>
          <span>用户总数：<strong>{{ recordTotal }}</strong></span>
        </div>
        <div class="stat-chip current-role">
          <i class="el-icon-s-custom"></i>
          <span>当前身份：<strong>{{ roleLabel }}</strong></span>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <el-input v-model="queryParam.username" placeholder="用户名搜索" prefix-icon="el-icon-search" clearable @keyup.enter.native="handleFilter" class="filter-input" />
      </div>
      <div class="filter-actions">
        <el-button type="primary" icon="el-icon-search" @click="handleFilter">搜索</el-button>
        <el-button type="default" @click="handleShowAll">显示全部</el-button>
        <el-button v-if="!isReader" type="success" icon="el-icon-plus" @click="handleCreate">添加用户</el-button>
        <el-button v-if="!isReader" type="danger" icon="el-icon-delete" :disabled="!selectedRows.length" @click="handleDeleteSome">批量删除</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table ref="multipleTable" :data="tableData" border stripe style="width: 100%" class="data-table" @selection-change="handleSelectionChange">
      <el-table-column fixed type="selection" width="46" align="center" />
      <el-table-column prop="userid" label="ID" width="65" align="center" />
      <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip>
        <template slot-scope="scope">
          <span class="username-text">{{ scope.row.username }}</span>
          <el-tag v-if="scope.row.userid === id" size="mini" type="info" effect="plain" style="margin-left:6px;">我</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="角色" width="120" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isadmin === 2" type="danger" effect="dark">超级管理员</el-tag>
          <el-tag v-else-if="scope.row.isadmin === 1" type="warning" effect="dark">管理员</el-tag>
          <el-tag v-else type="success" effect="dark">读者</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="180" align="center">
        <template slot-scope="scope">
          <template v-if="canManage(scope.row)">
            <el-button type="primary" size="small" icon="el-icon-edit" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" icon="el-icon-delete" @click="handleDelete(scope.row, scope.$index)">删除</el-button>
          </template>
          <el-tag v-else type="info" size="small" effect="plain">无权操作</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination background :current-page.sync="queryParam.page" :page-sizes="[5, 10, 20]" :page-size="queryParam.limit" layout="total, sizes, prev, pager, next, jumper" :total="recordTotal" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="formTitle" :visible.sync="dialogFormVisible" width="420px" custom-class="user-dialog">
      <el-form ref="ruleForm" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名称" prop="username"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="用户密码" prop="userpassword"><el-input v-model="form.userpassword" /></el-form-item>
        <el-form-item label="分配角色" prop="isadmin">
          <el-radio-group v-model="form.isadmin">
            <el-radio :label="0">读者</el-radio>
            <el-radio v-if="isSuperAdmin" :label="1">管理员</el-radio>
            <el-radio v-if="isSuperAdmin && form.userid !== id" :label="2">超级管理员</el-radio>
          </el-radio-group>
          <div v-if="!isSuperAdmin && formType === 0" class="role-tip">您只能创建读者账号</div>
          <div v-else-if="isSuperAdmin && form.userid === id && form.isadmin === 2" class="role-tip" style="color: #F56C6C;">⚠️ 您不能修改自己的角色</div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import waves from '@/directive/waves'
import { getCount, addUser, deleteUser, deleteUsers, updateUser, queryUsersByPageWithAuth } from '@/api/user'
import { getToken } from '@/utils/auth'

export default {
  name: 'UserManagement',
  directives: { waves },
  data() {
    return {
      tableData: [],
      recordTotal: 0,
      selectedRows: [],
      queryParam: { page: 1, limit: 10, username: null },
      dialogFormVisible: false,
      formType: 0,
      form: { userid: null, username: '', userpassword: '', isadmin: 0 },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        userpassword: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        isadmin: [{ required: true, message: '请选择角色', trigger: 'change' }]
      }
    }
  },
  created() {
    this.loadData()
  },
  computed: {
    ...mapGetters(['id', 'name', 'roles']),
    isAdmin() { return this.roles.includes('admin') || this.roles.includes('super_admin') },
    isSuperAdmin() { return this.roles.includes('super_admin') },
    isReader() { return this.roles.length === 1 && this.roles[0] === 'reader' },
    roleLabel() {
      if (this.isSuperAdmin) return '超级管理员'
      if (this.isAdmin) return '管理员'
      return '读者'
    },
    formTitle() { return this.formType === 0 ? '添加用户' : '编辑用户' }
  },
  methods: {
    loadData() {
      queryUsersByPageWithAuth(this.queryParam).then(res => {
        if (res.code === 403) {
          this.tableData = []
          this.recordTotal = 0
          this.$message.warning(res.message || '无权限访问')
          return
        }
        this.tableData = res.data || []
        this.recordTotal = res.count || 0
      })
    },

    canManage(row) {
      if (this.isReader) return false
      if (row.userid === this.id) return false
      if (this.isSuperAdmin) return true // 超管可以管理所有用户（包括其他超管）
      if (row.isadmin <= 0) return true // 管理员只能管理读者
      return false
    },

    handleSizeChange(curSize) { this.queryParam.limit = curSize; this.loadData() },
    handleCurrentChange(curPage) { this.queryParam.page = curPage; this.loadData() },
    handleFilter() { this.queryParam.page = 1; this.loadData() },
    handleShowAll() { this.queryParam.page = 1; this.queryParam.username = null; this.loadData() },
    handleSelectionChange(rows) { this.selectedRows = rows },

    handleCreate() {
      this.formType = 0
      this.form = { userid: null, username: '', userpassword: '', isadmin: 0 }
      this.dialogFormVisible = true
    },

    handleUpdate(row) {
      this.formType = 1
      this.form = { userid: row.userid, username: row.username, userpassword: row.userpassword, isadmin: row.isadmin }
      this.dialogFormVisible = true
    },

    submitForm() {
      this.$refs.ruleForm.validate(valid => {
        if (!valid) return

        if (!this.isSuperAdmin && this.form.isadmin > 0) {
          this.$message.warning('您没有权限分配此角色')
          return
        }

        // 编辑模式下，如果目标用户是超管且要降级，需要二次确认
        if (this.formType === 1) {
          const originalUser = this.tableData.find(u => u.userid === this.form.userid)
          if (originalUser && originalUser.isadmin === 2 && this.form.isadmin < 2) {
            this.$confirm(
              `⚠️ 您正在将超级管理员「${originalUser.username}」降级为${this.form.isadmin === 1 ? '管理员' : '读者'}！\n\n确定要继续吗？`,
              '危险操作确认',
              { confirmButtonText: '确认降级', cancelButtonText: '取消', type: 'warning' }
            ).then(() => {
              this.doUpdateUser()
            }).catch(() => {})
            return
          }
        }

        if (this.formType === 0) {
          addUser(this.form).then(res => {
            if (res === 1) {
              this.$message.success('用户添加成功')
              this.dialogFormVisible = false
              getCount().then(c => { this.recordTotal = c; this.queryParam.page = Math.ceil(c / this.queryParam.limit); this.handleCurrentChange(this.queryParam.page) })
            } else { this.$message.error('添加失败') }
          })
        } else {
          this.doUpdateUser()
        }
      })
    },

    doUpdateUser() {
      updateUser(this.form).then(res => {
        if (res === 1) { this.$message.success('用户信息更新成功'); this.dialogFormVisible = false; this.loadData() }
        else if (res === -5) { this.$message.error('不能将最后一个超级管理员降级，系统至少需要保留一个超管账号') }
        else if (res === -6) { this.$message.warning('不能修改自己的角色') }
        else { this.$message.error('更新失败：' + (res || '未知错误')) }
      })
    },

    handleDelete(row, index) {
      let warnMsg = `确定要删除用户「${row.username}」吗？`
      if (row.isadmin === 2) {
        warnMsg = `⚠️ 危险操作：确定要删除超级管理员「${row.username}」吗？\n\n此操作不可撤销！`
      } else if (row.isadmin === 1) {
        warnMsg += '\n⚠️ 该用户为管理员！'
      }

      this.$confirm(warnMsg, '确认删除', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: row.isadmin === 2 ? 'error' : 'warning' }).then(() => {
        deleteUser(row).then(res => {
          if (res === -1) { this.$message.error('未登录或会话已过期') }
          else if (res === -2) { this.$message.warning('不能删除自己的账号') }
          else if (res === -3) { this.$message.error('您没有权限删除此用户') }
          else if (res === -5) { this.$message.error('不能删除最后一个超级管理员，系统至少需要保留一个超管账号') }
          else if (res === -6) { this.$message.warning('不能修改自己的角色') }
          else if (res === 1) {
            this.$message.success('删除成功')
            this.tableData.splice(index, 1)
            if (this.tableData.length === 0 && this.queryParam.page > 1) { this.queryParam.page--; this.handleCurrentChange(this.queryParam.page) }
          } else { this.$message.error('删除失败') }
        })
      }).catch(() => {})
    },

    handleDeleteSome() {
      const hasSuperAdmin = this.selectedRows.some(r => r.isadmin === 2)
      let warnMsg = `确定要删除选中的 ${this.selectedRows.length} 个用户吗？`
      if (hasSuperAdmin) warnMsg += '\n\n⚠️ 选中包含超级管理员账号！'

      this.$confirm(warnMsg, '批量删除', { confirmButtonText: '确定', cancelButtonText: '取消', type: hasSuperAdmin ? 'error' : 'warning' }).then(() => {
        deleteUsers(this.selectedRows).then(res => {
          if (res === -2) { this.$message.warning('不能在批量操作中删除自己的账号') }
          else if (res === -3) { this.$message.error('您没有权限删除部分用户') }
          else if (res === -5) { this.$message.error('不能删除所有超级管理员，系统至少需要保留一个超管账号') }
          else if (res > 0) {
            this.$message.success(`成功删除 ${res} 条记录`)
            if (this.tableData.length <= res && this.queryParam.page > 1) this.queryParam.page--
            this.handleCurrentChange(this.queryParam.page)
          } else { this.$message.error('删除失败') }
        })
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
$primary-color: #409EFF;
$dark-bg: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);

.app-container { padding: 20px; background: #f5f7fa; min-height: calc(100vh - 84px); }

.page-header {
  display: flex; justify-content: space-between; align-items: center;
  background: $dark-bg; border-radius: 14px; padding: 22px 28px; margin-bottom: 18px; color: white;
  position: relative; overflow: hidden;
  &::before { content: ''; position: absolute; top: -30%; right: -6%; width: 260px; height: 260px; background: rgba(255,255,255,0.04); border-radius: 50%; }
  .header-left { position: relative; z-index: 1;
    .page-title { font-size: 22px; font-weight: 700; margin: 0 0 4px; letter-spacing: 1px; }
    .page-desc { font-size: 13px; opacity: 0.75; margin: 0; }
  }
  .header-stats { display: flex; gap: 14px; position: relative; z-index: 1;
    .stat-chip { padding: 7px 16px; border-radius: 20px; font-size: 13px; backdrop-filter: blur(4px); display: flex; align-items: center; gap: 6px;
      i { font-size: 15px; }
      strong { font-weight: 700; }
      &.total { background: rgba(64,158,255,0.18); }
      &.current-role { background: rgba(245,108,108,0.18); }
    }
  }
}

.filter-bar {
  background: white; border-radius: 12px; padding: 16px 20px; margin-bottom: 18px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  .filter-row { display: flex; gap: 12px; margin-bottom: 12px; }
  .filter-actions { display: flex; gap: 8px; padding-top: 12px; border-top: 1px solid #f0f0f0; flex-wrap: wrap; }
  .filter-input { width: 240px; }
}

.data-table {
  border-radius: 10px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  ::v-deep .el-table__header th { background: #fafbfc; font-weight: 600; font-size: 13px; border-bottom: 2px solid #ebeef5; }
  ::v-deep .el-table__body tr:hover > td { background: #f5f7ff; }
  ::v-deep .el-table__body td { border-bottom: 1px solid #f0f2f7; padding: 9px 0; font-size: 13px; }
  .username-text { font-weight: 600; color: #303133; }
}

.pagination-wrap { display: flex; justify-content: center; margin-top: 18px; padding: 14px 0 4px; }

::v-deep .user-dialog { border-radius: 12px;
  .el-dialog__header { padding: 16px 24px 12px; border-bottom: 1px solid #f0f0f0; }
  .el-dialog__title { font-weight: 700; font-size: 17px; }
  .el-dialog__body { padding: 24px; }
  .el-dialog__footer { padding: 12px 24px 18px; border-top: 1px solid #f0f0f0; }
  .role-tip { color: #E6A23C; font-size: 12px; margin-top: 4px; }
}
</style>

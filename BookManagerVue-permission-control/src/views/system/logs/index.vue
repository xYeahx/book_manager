<template>
  <div class="logs-page">
    <!-- 头部 -->
    <div class="page-header">
      <h2>📋 操作日志中心</h2>
      <p>记录所有管理员的系统操作</p>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <el-select v-model="queryParam.operationType" placeholder="操作类型" clearable size="medium">
          <el-option label="全部" value="" />
          <el-option label="入库" value="入库" />
          <el-option label="下架" value="下架" />
          <el-option label="上架" value="上架" />
          <el-option label="删除" value="删除" />
          <el-option label="编辑" value="编辑" />
          <el-option label="催还" value="催还" />
          <el-option label="用户管理" value="用户管理" />
        </el-select>
        <el-input v-model="queryParam.operatorName" placeholder="操作人" prefix-icon="el-icon-search" size="medium" clearable style="width: 180px;" />
      </div>
      <div class="filter-actions">
        <el-button type="primary" icon="el-icon-search" size="medium" @click="handleFilter">搜索</el-button>
        <el-button icon="el-icon-refresh" size="medium" @click="handleReset">显示全部</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" border stripe class="data-table">
      <el-table-column prop="logid" label="序号" width="70" align="center" />
      <el-table-column prop="operatorName" label="操作人" width="100" align="center" />
      <el-table-column label="角色" width="110" align="center">
        <template slot-scope="scope">
          <el-tag :type="getRoleTagType(scope.row.operatorRole)" size="small">
            {{ getRoleName(scope.row.operatorRole) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作类型" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="getTypeTagType(scope.row.operationType)" size="small">{{ scope.row.operationType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetName" label="操作对象" width="120" show-overflow-tooltip />
      <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作时间" width="170" align="center">
        <template slot-scope="scope">
          {{ scope.row.operateTime }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination background :current-page.sync="queryParam.page" :page-sizes="[10, 20, 50]" :page-size="queryParam.limit" layout="total, sizes, prev, pager, next, jumper" :total="recordTotal" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { queryLogsByPage } from '@/api/system'

export default {
  name: 'OperationLogs',
  data() {
    return {
      tableData: [],
      recordTotal: 0,
      queryParam: {
        page: 1,
        limit: 10,
        operationType: '',
        operatorName: ''
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      queryLogsByPage(this.queryParam).then(res => {
        this.tableData = res.data || []
        this.recordTotal = res.count || 0
      }).catch(() => {})
    },
    handleFilter() {
      this.queryParam.page = 1
      this.loadData()
    },
    handleReset() {
      this.queryParam = { page: 1, limit: 10, operationType: '', operatorName: '' }
      this.loadData()
    },
    handleSizeChange(size) {
      this.queryParam.limit = size
      this.loadData()
    },
    handleCurrentChange(page) {
      this.queryParam.page = page
      this.loadData()
    },
    getTypeTagType(type) {
      const map = { '入库': 'success', '下架': 'danger', '上架': 'primary', '删除': 'danger', '编辑': 'warning', '催还': 'info', '用户管理': '' }
      return map[type] || 'info'
    },
    getRoleName(role) {
      const map = { 0: '读者', 1: '管理员', 2: '超级管理员' }
      return map[role] || '未知'
    },
    getRoleTagType(role) {
      const map = { 0: '', 1: 'warning', 2: 'danger' }
      return map[role] || 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
$dark-bg: #1a1a2e;
$mid-bg: #16213e;
$light-bg: #0f3460;
$page-bg: #f5f7fa;

.logs-page {
  min-height: calc(100vh - 84px);
  padding: 20px;
  background: $page-bg;
}

.page-header {
  background: linear-gradient(135deg, $dark-bg 0%, $mid-bg 50%, $light-bg 100%);
  border-radius: 12px;
  padding: 24px 28px;
  margin-bottom: 20px;
  color: #fff;

  h2 { margin: 0 0 8px; font-size: 20px; font-weight: 700; }
  p { margin: 0; opacity: 0.75; font-size: 14px; }
}

.filter-bar {
  background: linear-gradient(135deg, $dark-bg 0%, $mid-bg 50%, $light-bg 100%);
  border-radius: 12px;
  padding: 18px 22px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);

  .filter-row { display: flex; gap: 14px; margin-bottom: 12px; flex-wrap: wrap; }

  .filter-actions { display: flex; gap: 10px; padding-top: 10px; border-top: 1px solid rgba(255,255,255,0.12); }

  ::v-deep .el-input__inner {
    height: 38px; line-height: 38px; border-radius: 8px; border: none;
    background: rgba(255,255,255,0.12); color: #fff; font-size: 13px;
    &::placeholder { color: rgba(255,255,255,0.5); }
  }
  ::v-deep .el-select .el-input__inner { cursor: pointer; }
}

.data-table {
  border-radius: 10px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04);

  ::v-deep .el-table__header th { background: #fafbfc !important; font-weight: 600; font-size: 13px; border-bottom: 2px solid #ebeef5; }
  ::v-deep .el-table__body tr:hover > td { background: #f5f7ff !important; }
}

.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; padding: 16px 0 4px; }
</style>

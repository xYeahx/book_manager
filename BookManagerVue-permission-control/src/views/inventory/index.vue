<template>
  <div class="inventory-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">库存管理中心</h2>
        <p class="page-desc">统一管理图书入库、编辑、下架与分类维护</p>
      </div>
      <div class="header-stats">
        <div class="stat-chip total">
          <i class="el-icon-reading"></i>
          <span>馆藏总量：<strong>{{ recordTotal }}</strong></span>
        </div>
        <div class="stat-chip borrowed">
          <i class="el-icon-notebook-2"></i>
          <span>在借：<strong>{{ borrowCount }}</strong></span>
        </div>
      </div>
    </div>

    <div class="filter-bar">
      <div class="filter-row">
        <el-input v-model="queryParam.bookname" placeholder="书名" prefix-icon="el-icon-search" clearable @keyup.enter.native="handleFilter" class="filter-input" />
        <el-input v-model="queryParam.bookauthor" placeholder="作者" prefix-icon="el-icon-user" clearable @keyup.enter.native="handleFilter" class="filter-input" />
        <el-select v-model="queryParam.booktypeid" filterable placeholder="分类筛选" clearable class="filter-select">
          <el-option v-for="item in typeData" :key="item.booktypeid" :label="item.booktypename" :value="item.booktypeid" />
        </el-select>
        <el-select v-model="queryParam.status" placeholder="状态筛选" clearable class="filter-select-short">
          <el-option label="全部" value="" />
          <el-option label="在架可借" value="0" />
          <el-option label="已借出" value="1" />
          <el-option label="已下架" value="2" />
        </el-select>
      </div>
      <div class="filter-actions">
        <el-button type="primary" icon="el-icon-search" @click="handleFilter">搜索</el-button>
        <el-button type="default" @click="handleShowAll">显示全部</el-button>
        <el-button type="success" icon="el-icon-plus" @click="handleCreate">新增入库</el-button>
        <el-button type="warning" icon="el-icon-upload2" @click="showImportDialog = true">批量录入</el-button>
        <el-button type="danger" icon="el-icon-delete" :disabled="!selectedRows.length" @click="handleBatchDelete">批量操作</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table ref="tableRef" :data="tableData" border stripe style="width: 100%" class="data-table" @selection-change="handleSelectionChange">
      <el-table-column fixed type="selection" width="46" align="center" />
      <el-table-column prop="bookid" label="ID" width="60" align="center" />
      <el-table-column prop="bookname" label="书名" min-width="150" show-overflow-tooltip>
        <template slot-scope="scope"><span class="book-name">{{ scope.row.bookname }}</span></template>
      </el-table-column>
      <el-table-column prop="bookauthor" label="作者" width="90" show-overflow-tooltip />
      <el-table-column prop="bookprice" label="价格" width="75" align="right">
        <template slot-scope="scope"><span class="price-text">¥{{ scope.row.bookprice }}</span></template>
      </el-table-column>
      <el-table-column prop="booktypename" label="分类" width="90" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isborrowed === 1" type="danger" size="mini" effect="dark">已借出</el-tag>
          <el-tag v-else-if="scope.row.isoffshelf === 1" type="info" size="mini" effect="dark">已下架</el-tag>
          <el-tag v-else type="success" size="mini" effect="dark">在架</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="220" align="center">
        <template slot-scope="scope">
          <el-button type="text" size="small" icon="el-icon-edit" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button v-if="scope.row.isoffshelf !== 1" type="text" size="small" icon="el-icon-bottom" style="color: #E6A23C;" @click="handleOffShelf(scope.row)">下架</el-button>
          <el-button v-else type="text" size="small" icon="el-icon-top" style="color: #67C23A;" @click="handleOnShelf(scope.row)">上架</el-button>
          <el-button type="text" size="small" icon="el-icon-delete" style="color: #F56C6C;" @click="handleDelete(scope.row, scope.$index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination background :current-page.sync="queryParam.page" :page-sizes="[5, 10, 20, 50]" :page-size="queryParam.limit" layout="total, sizes, prev, pager, next, jumper" :total="recordTotal" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogFormVisible" width="600px" custom-class="book-dialog">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-form ref="ruleForm" :model="form" :rules="rules" label-width="85px">
            <el-form-item label="图书名称" prop="bookname"><el-input v-model="form.bookname" /></el-form-item>
            <el-form-item label="作者" prop="bookauthor"><el-input v-model="form.bookauthor" /></el-form-item>
            <el-form-item label="价格" prop="bookprice"><el-input v-model="form.bookprice" /></el-form-item>
            <el-form-item label="图书类型" prop="booktypeid">
              <el-select v-model="form.booktypeid" filterable allow-create placeholder="选择或输入新类型" style="width: 100%">
                <el-option v-for="item in typeData" :key="item.booktypeid" :label="item.booktypename" :value="item.booktypeid" />
              </el-select>
            </el-form-item>
            <el-form-item label="书籍描述" prop="bookdesc"><el-input v-model="form.bookdesc" type="textarea" :rows="3" /></el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="form.isoffshelf">
                <el-radio :label="0">在架（可借）</el-radio>
                <el-radio :label="1">下架（不可借）</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="8">
          <div class="upload-area">
            <p class="upload-tip">上传封面图片</p>
            <el-upload action="http://localhost:8092/BookManager/update/updateImg" :show-file-list="false" :on-success="handleAvatarSuccess" :before-upload="beforeAvatarUpload">
              <img v-if="form.bookimg" :src="form.bookimg" class="avatar" alt="封面">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
          </div>
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 批量录入对话框 -->
    <el-dialog title="批量录入馆藏" :visible.sync="showImportDialog" width="520px" custom-class="import-dialog">
      <div class="import-content">
        <el-alert title="请按以下格式填写每行一条图书记录，字段间用逗号分隔" type="info" :closable="false" show-icon style="margin-bottom: 16px;" />
        <div class="import-example">
          <code>书名,作者,价格,类型名,描述</code><br/>
          <code class="example-row">西游记,吴承恩,45.0,文学,四大名著之一</code><br/>
          <code class="example-row">红楼梦,曹雪芹,36.0,文学,古典小说巅峰</code>
        </div>
        <el-input type="textarea" v-model="importText" :rows="10" placeholder="在此粘贴或输入图书数据..." resize="none" />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showImportDialog = false">取 消</el-button>
        <el-button type="success" icon="el-icon-upload2" @click="handleBatchImport" :loading="importLoading">{{ importLoading ? '导入中...' : '开始导入' }}</el-button>
      </div>
    </el-dialog>

    <!-- 分类管理抽屉 -->
    <el-drawer title="分类管理" :visible.sync="showTypeDrawer" size="400px" direction="rtl" custom-class="type-drawer">
      <div class="type-drawer-body">
        <div class="drawer-header-action">
          <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAddType">新增分类</el-button>
        </div>
        <el-table :data="typeData" border size="small" class="type-table">
          <el-table-column prop="booktypeid" label="ID" width="55" align="center" />
          <el-table-column prop="booktypename" label="分类名称" />
          <el-table-column label="操作" width="140" align="center">
            <template slot-scope="scope">
              <el-button type="text" size="small" icon="el-icon-edit" @click="handleEditType(scope.row)"></el-button>
              <el-button type="text" size="small" icon="el-icon-delete" style="color: #F56C6C;" @click="handleDeleteType(scope.row)"></el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>

    <!-- 分类编辑对话框 -->
    <el-dialog :title="typeDialogTitle" :visible.sync="typeDialogVisible" width="420px" append-to-body>
      <el-form :model="typeForm" :rules="typeRules" ref="typeRuleForm" label-width="80px">
        <el-form-item label="分类名称" prop="booktypename">
          <el-input v-model="typeForm.booktypename" placeholder="请输入分类名称" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="typeDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitTypeForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 浮动操作按钮 -->
    <div class="float-btn" @click="showTypeDrawer = true">
      <i class="el-icon-menu"></i>
      <span>分类管理</span>
    </div>
  </div>
</template>

<script>
import permission from '@/directive/permission/index.js'
import waves from '@/directive/waves'
import { queryBookInfosByPage, addBookInfo, deleteBookInfo, deleteBookInfos, updateBookInfo } from '@/api/bookinfo'
import { queryBookTypes, addBookType, deleteBookType, updateBookType } from '@/api/booktype'
import { getCount as getBorrowCount } from '@/api/borrow'

export default {
  name: 'Inventory',
  directives: { waves, permission },
  data() {
    const validatePrice = (rule, value, callback) => {
      if (!value || !String(value).trim()) { callback(new Error('请输入价格')) }
      else if (isNaN(value)) { callback(new Error('价格必须为数字')) }
      else { callback() }
    }
    return {
      tableData: [],
      typeData: [],
      recordTotal: 0,
      borrowCount: '--',
      selectedRows: [],
      queryParam: { page: 1, limit: 10, bookname: null, bookauthor: null, booktypeid: null, status: null },
      dialogFormVisible: false,
      dialogType: 0,
      dialogTitle: '',
      form: { bookid: null, bookname: '', bookauthor: '', bookprice: '', booktypeid: null, bookdesc: '', isborrowed: 0, isoffshelf: 0, bookimg: '' },
      rules: {
        bookname: [{ required: true, message: '请输入图书名称', trigger: 'blur' }],
        bookauthor: [{ required: true, message: '请输入作者', trigger: 'blur' }],
        bookprice: [{ required: true, validator: validatePrice, trigger: 'blur' }],
        booktypeid: [{ required: true, message: '请选择类型', trigger: 'change' }],
        bookdesc: [{ required: true, message: '请输入描述', trigger: 'blur' }]
      },
      showImportDialog: false,
      importText: '',
      importLoading: false,
      showTypeDrawer: false,
      typeDialogVisible: false,
      typeDialogTitle: '',
      typeDialogType: 0,
      typeForm: { booktypeid: null, booktypename: '' },
      typeRules: { booktypename: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }
    }
  },
  created() {
    this.loadData()
    this.loadTypes()
    this.loadBorrowCount()
  },
  methods: {
    loadData() {
      queryBookInfosByPage(this.queryParam).then(res => {
        this.tableData = res.data
        this.recordTotal = res.count
      })
    },
    loadTypes() {
      queryBookTypes().then(res => { this.typeData = res })
    },
    async loadBorrowCount() {
      try { this.borrowCount = await getBorrowCount() } catch (e) {}
    },
    handleSizeChange(curSize) { this.queryParam.limit = curSize; this.loadData() },
    handleCurrentChange(curPage) { this.queryParam.page = curPage; this.loadData() },
    handleFilter() { this.queryParam.page = 1; this.loadData() },
    handleShowAll() {
      this.queryParam.page = 1; this.queryParam.bookname = null; this.queryParam.bookauthor = null
      this.queryParam.booktypeid = null; this.queryParam.status = null; this.loadData()
    },
    handleSelectionChange(rows) { this.selectedRows = rows },

    handleCreate() {
      this.dialogType = 0; this.dialogTitle = '新增入库'; this.form = { bookid: null, bookname: '', bookauthor: '', bookprice: '', booktypeid: null, bookdesc: '', isborrowed: 0, isoffshelf: 0, bookimg: '' }
      this.loadTypes(); this.dialogFormVisible = true
    },
    handleUpdate(row) {
      this.dialogType = 1; this.dialogTitle = '编辑图书信息'; this.form = { ...row }; this.loadTypes(); this.dialogFormVisible = true
    },
    submitForm() {
      this.$refs.ruleForm.validate(valid => {
        if (!valid) return
        const action = this.dialogType === 0 ? addBookInfo(this.form) : updateBookInfo(this.form)
        action.then(res => {
          if (res === 1) {
            this.$message.success(this.dialogType === 0 ? '入库成功！' : '更新成功！')
            this.dialogFormVisible = false; this.loadData()
          } else { this.$message.error('操作失败') }
        }).catch(() => {})
      })
    },

    handleOffShelf(row) {
      this.$confirm(`确定要将《${row.bookname}》下架吗？下架后读者将无法借阅。`, '确认下架', { confirmButtonText: '确定下架', cancelButtonText: '取消', type: 'warning' }).then(() => {
        updateBookInfo({ ...row, isoffshelf: 1 }).then(res => {
          if (res === 1) { this.$message.success('已下架'); this.loadData() } else { this.$message.error('下架失败') }
        })
      }).catch(() => {})
    },
    handleOnShelf(row) {
      updateBookInfo({ ...row, isoffshelf: 0 }).then(res => {
        if (res === 1) { this.$message.success('已重新上架'); this.loadData() } else { this.$message.error('上架失败') }
      })
    },
    handleDelete(row, index) {
      this.$confirm(`确定要删除《${row.bookname}》吗？此操作不可恢复。`, '警告', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error' }).then(() => {
        deleteBookInfo(row).then(res => {
          if (res === 1) { this.$message.success('删除成功'); this.tableData.splice(index, 1); if (this.tableData.length === 0 && this.queryParam.page > 1) { this.queryParam.page--; this.handleCurrentChange(this.queryParam.page) } } else { this.$message.error('删除失败') }
        })
      }).catch(() => {})
    },
    handleBatchDelete() {
      this.$confirm(`确定要对选中的 ${this.selectedRows.length} 条记录进行批量删除吗？`, '批量操作', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(() => {
        deleteBookInfos(this.selectedRows).then(res => {
          if (res > 0) { this.$message.success(`成功删除 ${res} 条`); if (this.tableData.length <= res && this.queryParam.page > 1) this.queryParam.page--; this.handleCurrentChange(this.queryParam.page) } else { this.$message.error('删除失败') }
        })
      }).catch(() => {})
    },

    handleBatchImport() {
      if (!this.importText.trim()) { this.$message.warning('请输入导入数据'); return }
      const lines = this.importText.trim().split('\n').filter(l => l.trim())
      if (!lines.length) { this.$message.warning('没有有效数据'); return }

      this.importLoading = true
      let success = 0, fail = 0

      const doImport = async () => {
        for (const line of lines) {
          const parts = line.split(',').map(s => s.trim())
          if (parts.length < 4) { fail++; continue }
          try {
            const res = await addBookInfo({ bookname: parts[0], bookauthor: parts[1], bookprice: parts[2] || '0', bookdesc: parts[4] || '', isoffshelf: 0, isborrowed: 0, bookimg: '' })
            if (res === 1) success++; else fail++
          } catch (e) { fail++ }
        }
      }

      doImport().then(() => {
        this.importLoading = false; this.showImportDialog = false; this.importText = ''
        this.$notify({ title: '导入完成', message: `成功 ${success} 条，失败 ${fail} 条`, type: success > 0 ? 'success' : 'warning', duration: 5000 })
        this.loadData()
      }).catch(() => { this.importLoading = false })
    },

    handleAddType() { this.typeDialogType = 0; this.typeDialogTitle = '新增图书类型'; this.typeForm = { booktypeid: null, booktypename: '' }; this.typeDialogVisible = true },
    handleEditType(row) { this.typeDialogType = 1; this.typeDialogTitle = '编辑图书类型'; this.typeForm = { ...row }; this.typeDialogVisible = true },
    submitTypeForm() {
      this.$refs.typeRuleForm.validate(valid => {
        if (!valid) return
        const action = this.typeDialogType === 0 ? addBookType(this.typeForm) : updateBookType(this.typeForm)
        action.then(res => {
          if (res === 1) { this.$message.success(this.typeDialogType === 0 ? '添加成功' : '修改成功'); this.typeDialogVisible = false; this.loadTypes() } else { this.$message.error('操作失败') }
        })
      })
    },
    handleDeleteType(row) {
      this.$confirm(`确定要删除分类「${row.booktypename}」吗？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(() => {
        deleteBookType(row).then(res => { if (res === 1) { this.$message.success('删除成功'); this.loadTypes() } else { this.$message.error('删除失败，该分类可能已被使用') } })
      }).catch(() => {})
    },

    handleAvatarSuccess(res) { if (res.code === 0) { this.form.bookimg = res.data; this.$message.success('封面上传成功') } else { this.$message.error('上传失败') } },
    beforeAvatarUpload(file) { const isImage = file.type.startsWith('image/'), isLt2M = file.size / 1024 / 1024 < 2; if (!isImage) this.$message.error('只能上传图片!'); if (!isLt2M) this.$message.error('大小不能超过 2MB!'); return isImage && isLt2M }
  }
}
</script>

<style lang="scss" scoped>
$primary-color: #409EFF;
$dark-bg: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);

.inventory-page { padding: 20px; background: #f5f7fa; min-height: calc(100vh - 84px); position: relative; }

.page-header {
  display: flex; justify-content: space-between; align-items: center;
  background: $dark-bg; border-radius: 14px; padding: 22px 28px; margin-bottom: 18px; color: white;
  position: relative; overflow: hidden;

  &::before { content: ''; position: absolute; top: -40%; right: -8%; width: 300px; height: 300px; background: rgba(255,255,255,0.04); border-radius: 50%; }

  .header-left { position: relative; z-index: 1;
    .page-title { font-size: 22px; font-weight: 700; margin: 0 0 4px; letter-spacing: 1px; }
    .page-desc { font-size: 13px; opacity: 0.75; margin: 0; }
  }

  .header-stats { display: flex; gap: 14px; position: relative; z-index: 1;
    .stat-chip { padding: 7px 16px; border-radius: 20px; font-size: 13px; backdrop-filter: blur(4px); display: flex; align-items: center; gap: 6px;
      i { font-size: 15px; }
      strong { font-weight: 700; }
      &.total { background: rgba(64, 158, 255, 0.18); }
      &.borrowed { background: rgba(245, 108, 108, 0.18); }
    }
  }
}

.filter-bar {
  background: white; border-radius: 12px; padding: 16px 20px; margin-bottom: 18px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);

  .filter-row { display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 12px; }
  .filter-actions { display: flex; gap: 8px; padding-top: 12px; border-top: 1px solid #f0f0f0; flex-wrap: wrap;

    ::v-deep .el-button--default { border-color: #dcdfe6; &:hover { color: $primary-color; border-color: $primary-color; } }
  }

  ::v-deep .el-input__inner { height: 38px; border-radius: 8px; }
  .filter-input { width: 180px; }
  .filter-select { width: 160px; }
  .filter-select-short { width: 130px; }
}

.data-table {
  border-radius: 10px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  ::v-deep .el-table__header th { background: #fafbfc; font-weight: 600; font-size: 13px; border-bottom: 2px solid #ebeef5; }
  ::v-deep .el-table__body tr:hover > td { background: #f5f7ff; }
  ::v-deep .el-table__body td { border-bottom: 1px solid #f0f2f7; padding: 9px 0; font-size: 13px; }
  .book-name { font-weight: 600; color: #303133; }
  .price-text { color: #E6A23C; font-weight: 500; }
}
.pagination-wrap { display: flex; justify-content: center; margin-top: 18px; padding: 14px 0 4px; }

::v-deep .book-dialog { border-radius: 12px;
  .el-dialog__header { padding: 16px 24px 12px; border-bottom: 1px solid #f0f0f0; }
  .el-dialog__title { font-weight: 700; font-size: 17px; color: #303133; }
  .el-dialog__body { padding: 24px; }
  .el-dialog__footer { padding: 12px 24px 18px; border-top: 1px solid #f0f0f0; }
}
.upload-area { text-align: center; padding: 8px;
  .upload-tip { color: #909399; font-size: 13px; margin-bottom: 10px; }
  .avatar-uploader-icon { font-size: 28px; color: #c0c4cc; width: 130px; height: 170px; line-height: 170px; text-align: center; }
  .avatar { width: 130px; height: 170px; object-fit: cover; border-radius: 6px; }
  ::v-deep .el-upload { border: 2px dashed #dcdfe6; border-radius: 10px; cursor: pointer; &:hover { border-color: $primary-color; } }
}

::v-deep .import-dialog { border-radius: 12px;
  .import-content { .import-example { background: #f5f7fa; border-radius: 8px; padding: 12px 16px; margin-bottom: 12px; font-size: 13px;
    code { color: #606266; line-height: 1.8; }
    .example-row { color: #909399; font-size: 12px; }
  }}
}
::v-deep .type-drawer { .el-drawer__header { margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid #f0f0f0; span { font-weight: 700; font-size: 16px; } }
  .el-drawer__body { overflow-y: auto; }
  .type-drawer-body { padding: 16px 20px;
    .drawer-header-action { margin-bottom: 14px; }
    .type-table { ::v-deep th { background: #fafbfc; } }
  }
}

.float-btn {
  position: fixed; right: 30px; bottom: 100px; z-index: 999;
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  width: 54px; height: 54px; border-radius: 14px;
  background: linear-gradient(135deg, #667eea, #764ba2); color: white;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.35); cursor: pointer;
  transition: all 0.3s ease; user-select: none;

  i { font-size: 22px; }
  span { font-size: 11px; writing-mode: vertical-lr; letter-spacing: 2px; font-weight: 600; }

  &:hover { transform: translateY(-3px) scale(1.05); box-shadow: 0 8px 25px rgba(102, 126, 234, 0.45); }
}

@media screen and (max-width: 768px) {
  .inventory-page { padding: 12px; }
  .page-header { flex-direction: column; text-align: center; gap: 12px; padding: 18px 20px; }
  .header-stats { justify-content: center; }
  .filter-bar .filter-row { flex-direction: column; }
  .filter-input, .filter-select, .filter-select-short { width: 100% !important; }
  .float-btn { right: 16px; bottom: 70px; }
}
</style>

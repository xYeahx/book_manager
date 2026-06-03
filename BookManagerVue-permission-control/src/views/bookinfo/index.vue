<template>
  <div class="bookinfo-page">
    <div class="filter-bar">
      <div class="filter-bar-inner">
        <div class="filter-row">
          <el-input v-model="queryParam.bookname" placeholder="请输入书名" class="filter-input" prefix-icon="el-icon-search" clearable @keyup.enter.native="handleFilter" />
          <el-input v-model="queryParam.bookauthor" placeholder="请输入作者" class="filter-input" prefix-icon="el-icon-user" clearable @keyup.enter.native="handleFilter" />
          <el-select v-model="queryParam.booktypeid" filterable placeholder="选择类型" clearable class="filter-select">
            <el-option v-for="item in typeData" :key="item.booktypeid" :label="item.booktypename" :value="item.booktypeid" />
          </el-select>
        </div>
        <div class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="handleFilter">搜索</el-button>
          <el-button type="default" @click="handleShowAll">显示全部</el-button>
        </div>
      </div>
    </div>

    <!--弹出框-->
    <el-dialog :title="formTitle" :visible.sync="dialogFormVisible" width="50%" custom-class="book-dialog">
      <el-row :gutter="24">
        <el-col :span="16">
          <el-form ref="ruleForm" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="图书名称" prop="bookname">
              <el-input v-model="form.bookname" />
            </el-form-item>
            <el-form-item label="作者" prop="bookauthor">
              <el-input v-model="form.bookauthor" />
            </el-form-item>
            <el-form-item label="价格" prop="bookprice">
              <el-input v-model="form.bookprice" />
            </el-form-item>
            <el-form-item label="图书类型" prop="booktypeid">
              <el-select v-model="form.booktypeid" placeholder="请选择类型" style="width: 100%">
                <el-option v-for="item in typeData" :key="item.booktypeid" :label="item.booktypename" :value="item.booktypeid" />
              </el-select>
            </el-form-item>
            <el-form-item label="书籍描述" prop="bookdesc">
              <el-input v-model="form.bookdesc" type="textarea" :rows="3" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="8">
          <div class="upload-area">
            <p class="upload-tip">点击上传封面图片</p>
            <el-upload
              class="avatar-uploader"
              action="http://localhost:8092/BookManager/update/updateImg"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
            >
              <img v-if="form.bookimg" :src="form.bookimg" class="avatar" alt="封面无法显示">
              <i v-else class="el-icon-plus avatar-uploader-icon" />
            </el-upload>
          </div>
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

    <!--弹出框2-->
    <el-dialog title="选择需要借阅此书的用户" :visible.sync="dialogFormVisible2" width="420px">
      <el-form :model="form2">
        <el-form-item label="用户名" label-width="80px">
          <el-select v-model="form2.userid" placeholder="请选择用户" style="width: 100%">
            <el-option v-for="item in userData" :key="item.userid" :label="item.username" :value="item.userid" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible2 = false">取 消</el-button>
        <el-button type="primary" @click="submitForm2">确 定</el-button>
      </div>
    </el-dialog>

    <!--数据表格-->
    <el-table ref="multipleTable" :data="tableData" border stripe style="width: 100%" class="data-table">
      <el-table-column fixed type="selection" width="48" align="center" />
      <el-table-column fixed prop="bookid" label="序号" width="70" align="center" />
      <el-table-column prop="bookname" label="图书名称" min-width="140" show-overflow-tooltip>
        <template slot-scope="scope">
          <span class="book-name">{{ scope.row.bookname }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="bookauthor" label="作者" width="95" show-overflow-tooltip />
      <el-table-column prop="bookprice" label="价格" width="80" align="right">
        <template slot-scope="scope">
          <span class="price-text">¥{{ scope.row.bookprice }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="booktypename" label="类型" width="90" align="center" />
      <el-table-column prop="bookdesc" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="90" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isborrowed === 1" type="danger" size="small" effect="dark">已借出</el-tag>
          <el-tag v-else type="success" size="small" effect="dark">可借阅</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" :width="roleIsAdmin ? 150 : 140" align="center">
        <template slot-scope="scope">
          <el-button v-permission="['admin', 'super_admin']" type="text" size="small" icon="el-icon-edit" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button type="text" size="small" icon="el-icon-eye" style="color: #409EFF; margin-right: 8px;" @click="handleView(scope.row)">查看</el-button>
          <el-button v-if="!roleIsAdmin" type="text" size="small" icon="el-icon-reading" style="color: #67C23A;" @click="handleBorrow(scope.row)">借阅</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 图书详情对话框 -->
    <el-dialog title="图书详情" :visible.sync="dialogFormVisible3" width="420px" append-to-body>
      <div class="book-detail">
        <!-- 封面展示区 -->
        <div class="cover-section">
          <div class="cover-label">封面</div>
          <div class="cover-container">
            <img v-if="form.bookimg" :src="form.bookimg" alt="封面" class="cover-image" />
            <div v-else class="cover-placeholder">
              <i class="el-icon-picture-outline"></i>
              <span>暂无封面</span>
            </div>
          </div>
        </div>
        <div class="detail-item">
          <span class="detail-label">图书名称</span>
          <span class="detail-value">{{ form.bookname || '--' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">作者</span>
          <span class="detail-value">{{ form.bookauthor || '--' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">价格</span>
          <span class="detail-value">¥{{ form.bookprice || '--' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">类型</span>
          <span class="detail-value">{{ form.booktypename || '--' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">状态</span>
          <span class="detail-value">
            <el-tag :type="form.isborrowed === 1 ? 'danger' : 'success'" size="small">
              {{ form.isborrowed === 1 ? '已借出' : '可借阅' }}
            </el-tag>
          </span>
        </div>
        <div class="detail-item desc">
          <span class="detail-label">描述</span>
          <span class="detail-value">{{ form.bookdesc || '--' }}</span>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible3 = false">关闭</el-button>
      </div>
    </el-dialog>

    <!--分页条-->
    <div class="pagination-wrap">
      <el-pagination background :current-page.sync="queryParam.page" :page-sizes="[5, 10, 20, 50]" :page-size="queryParam.limit" layout="total, sizes, prev, pager, next, jumper" :total="recordTotal" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import permission from '@/directive/permission/index.js'
import waves from '@/directive/waves'
import { getCount, queryBookInfosByPage, addBookInfo, deleteBookInfo, deleteBookInfos, updateBookInfo, queryBooksForReader } from '@/api/bookinfo'
import { queryBookTypes } from '@/api/booktype'
import { borrowBook } from '@/api/borrow'
import { queryUsers } from '@/api/user'

export default {
  name: 'Bookinfo',
  directives: { waves, permission },
  data() {
    return {
      tableData: [],
      recordTotal: 0,
      typeData: [],
      userData: [],
      queryParam: {
        page: 1,
        limit: 10,
        bookname: null,
        bookauthor: null,
        booktypeid: null
      },
      dialogFormVisible: false,
      dialogFormVisible2: false,
      dialogFormVisible3: false,
      formType: 0,
      form: {
        bookid: null,
        bookname: '',
        bookauthor: '',
        bookprice: 0,
        booktypeid: 1,
        bookdesc: '',
        isborrowed: 0,
        bookimg: ''
      },
      form2: {
        userid: null,
        bookid: null
      },
      rules: {
        bookname: [{ required: true, message: '请输入图书名称', trigger: 'blur' }],
        bookauthor: [{ required: true, message: '请输入作者', trigger: 'blur' }],
        bookprice: [{ required: true, message: '请输入价格', trigger: 'blur' }],
        booktypeid: [{ required: true, message: '请选择类型', trigger: 'change' }],
        bookdesc: [{ required: true, message: '请输入描述', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadData()
    queryBookTypes().then(res => {
      this.typeData = res
    })
  },
  mounted() {
    if (!this.roleIsAdmin) {
      this.queryParam.limit = 5
      this.handleSizeChange(this.queryParam.limit)
    }
  },
  methods: {
    loadData() {
      const api = this.roleIsAdmin ? queryBookInfosByPage : queryBooksForReader
      api(this.queryParam).then(res => {
        this.tableData = res.data
        this.recordTotal = res.count
      })
    },
    handleSizeChange(curSize) {
      const params = this.queryParam
      params.limit = curSize
      const api = this.roleIsAdmin ? queryBookInfosByPage : queryBooksForReader
      api(params).then(res => {
        this.tableData = res.data
        this.recordTotal = res.count
      })
    },
    handleCurrentChange(curPage) {
      const params = this.queryParam
      params.page = curPage
      const api = this.roleIsAdmin ? queryBookInfosByPage : queryBooksForReader
      api(params).then(res => {
        this.tableData = res.data
        this.recordTotal = res.count
      })
    },
    handleFilter() {
      this.queryParam.page = 1
      const api = this.roleIsAdmin ? queryBookInfosByPage : queryBooksForReader
      api(this.queryParam).then(res => {
        if (res.code === 0) {
          this.tableData = res.data
          this.recordTotal = res.count
        }
      })
    },
    handleShowAll() {
      this.queryParam.page = 1
      this.queryParam.bookname = null
      this.queryParam.bookauthor = null
      this.queryParam.booktypeid = null
      const api = this.roleIsAdmin ? queryBookInfosByPage : queryBooksForReader
      api(this.queryParam).then(res => {
        if (res.code === 0) {
          this.tableData = res.data
          this.recordTotal = res.count
        }
      })
    },
    handleAvatarSuccess(res) {
      if (res.code === 0) {
        this.$message.success('上传成功')
        this.form.bookimg = res.data
      } else {
        this.$message.error('上传失败，请联系管理员')
      }
    },
    beforeAvatarUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isImage) this.$message.error('只能上传图片文件!')
      if (!isLt2M) this.$message.error('图片大小不能超过 2MB!')
      return isImage && isLt2M
    },
    handleCreate() {
      queryBookTypes().then(res => { this.typeData = res })
      this.formType = 0
      this.form = { bookid: null, bookname: '', bookauthor: '', bookprice: '', booktypeid: null, bookdesc: '', isborrowed: 0, bookimg: '' }
      this.dialogFormVisible = true
    },
    handleView(row) {
      this.form = {
        bookid: row.bookid,
        bookname: row.bookname,
        bookauthor: row.bookauthor,
        bookprice: row.bookprice,
        booktypeid: row.booktypeid,
        booktypename: row.booktypename,
        bookdesc: row.bookdesc,
        isborrowed: row.isborrowed,
        bookimg: row.bookimg
      }
      this.dialogFormVisible3 = true
    },
    handleUpdate(row) {
      queryBookTypes().then(res => { this.typeData = res })
      this.formType = 1
      this.form = { bookid: row.bookid, bookname: row.bookname, bookauthor: row.bookauthor, bookprice: row.bookprice, booktypeid: row.booktypeid, bookdesc: row.bookdesc, isborrowed: row.isborrowed, bookimg: row.bookimg }
      this.dialogFormVisible = true
    },
    handleBorrow(row) {
      if (row.isborrowed === 1) {
        this.$message.warning('该图书已被借出，无法再次借阅')
        return
      }
      if (this.roleIsAdmin) {
        this.form2.bookid = row.bookid
        this.form2.userid = null
        queryUsers().then(res => {
          this.userData = res
          this.dialogFormVisible2 = true
        })
      } else {
        this.$confirm('您确定要借阅《' + row.bookname + '》吗？', '确认借阅', { confirmButtonText: '确定借阅', cancelButtonText: '取消', type: 'info' }).then(() => {
          borrowBook(this.id, row.bookid).then(res => {
            if (res === 1) {
              this.$message.success('借书成功')
              this.handleCurrentChange(this.queryParam.page)
            } else {
              this.$message.error('借书失败，请稍后重试')
            }
          })
        }).catch(() => {})
      }
    },
    submitForm() {
      if (this.formType === 0) {
        addBookInfo(this.form).then(res => {
          if (res === 1) {
            this.$message.success('添加记录成功')
            getCount().then(res => {
              this.recordTotal = res
              this.queryParam.page = Math.ceil(this.recordTotal / this.queryParam.limit)
              this.handleCurrentChange(this.queryParam.page)
            })
          } else {
            this.$message.error('添加记录失败')
          }
          this.dialogFormVisible = false
        })
      } else {
        updateBookInfo(this.form).then(res => {
          if (res === 1) {
            this.$message.success('更新记录成功')
            this.handleCurrentChange(this.queryParam.page)
          } else {
            this.$message.error('更新记录失败')
          }
          this.dialogFormVisible = false
        })
      }
    },
    submitForm2() {
      borrowBook(this.form2.userid, this.form2.bookid).then(res => {
        if (res === 1) {
          this.$message.success('借书成功')
          this.handleCurrentChange(this.queryParam.page)
        } else {
          this.$message.error('借书失败')
        }
        this.dialogFormVisible2 = false
      })
    },
    handleDelete(row, index) {
      this.$confirm('确定要删除该条记录吗?', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(() => {
        deleteBookInfo(row).then(res => {
          if (res === 1) {
            this.$message.success('删除记录成功')
            this.tableData.splice(index, 1)
            if (this.tableData.length === 0 && this.queryParam.page > 1) {
              this.queryParam.page -= 1
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
        deleteBookInfos(items).then(res => {
          if (res > 0) {
            this.$message.success('删除' + res + '条记录成功')
            if (this.tableData.length <= res && this.queryParam.page > 1) this.queryParam.page--
            this.handleCurrentChange(this.queryParam.page)
          } else {
            this.$message.error('删除记录失败')
          }
        })
      })
    }
  },
  computed: {
    ...mapGetters(['id', 'name', 'roles']),
    formTitle() {
      return this.formType === 0 ? '添加图书' : '编辑图书'
    },
    roleIsAdmin() {
      return this.roles && (this.roles.includes('admin') || this.roles.includes('super_admin'))
    }
  }
}
</script>

<style lang="scss" scoped>
$dark-bg: #1a1a2e;
$mid-bg: #16213e;
$light-bg: #0f3460;
$page-bg: #f5f7fa;

.bookinfo-page {
  min-height: calc(100vh - 84px);
  padding: 20px;
  background: $page-bg;
}

.filter-bar {
  background: linear-gradient(135deg, $dark-bg 0%, $mid-bg 50%, $light-bg 100%);
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);

  .filter-bar-inner {
    .filter-row {
      display: flex;
      gap: 16px;
      margin-bottom: 14px;
      flex-wrap: wrap;
    }

    .filter-actions {
      display: flex;
      gap: 10px;
      padding-top: 12px;
      border-top: 1px solid rgba(255, 255, 255, 0.12);
    }

    ::v-deep .el-input__inner {
      height: 40px;
      line-height: 40px;
      border-radius: 8px;
      border: none;
      background: rgba(255, 255, 255, 0.12);
      color: #fff;
      font-size: 13px;

      &::placeholder { color: rgba(255, 255, 255, 0.5); }

      &:focus {
        background: rgba(255, 255, 255, 0.18);
        outline: none;
      }

      &:hover:not(:focus) {
        background: rgba(255, 255, 255, 0.15);
      }
    }

    ::v-deep .el-input__prefix {
      left: 10px;
      color: rgba(255, 255, 255, 0.55);
    }

    ::v-deep .el-input__inner {
      padding-left: 34px;
    }

    .filter-input {
      width: 200px;
    }

    .filter-select {
      width: 180px;

      ::v-deep .el-input__inner {
        cursor: pointer;
      }
    }

    ::v-deep .el-button--default {
      background: rgba(255, 255, 255, 0.15);
      border-color: transparent;
      color: #fff;

      &:hover {
        background: rgba(255, 255, 255, 0.25);
        color: #fff;
      }
    }

    ::v-deep .el-button--success {
      border-radius: 8px;
    }

    ::v-deep .el-button--danger {
      border-radius: 8px;
    }
  }
}

.data-table {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

  ::v-deep .el-table__header th {
    background: #fafbfc !important;
    font-weight: 600;
    color: #303133;
    font-size: 13px;
    border-bottom: 2px solid #ebeef5;
  }

  ::v-deep .el-table__body tr {
    transition: background 0.2s;

    &:hover > td {
      background: #f5f7ff !important;
    }
  }

  ::v-deep .el-table__body td {
    border-bottom: 1px solid #f0f2f7;
    padding: 10px 0;
    font-size: 13px;
    color: #606266;
  }

  .book-name {
    font-weight: 600;
    color: #303133;
  }

  .price-text {
    color: #E6A23C;
    font-weight: 500;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 16px 0 4px;
}

.upload-area {
  text-align: center;
  padding: 10px;

  .upload-tip {
    color: #909399;
    font-size: 13px;
    margin-bottom: 12px;
  }
}

.avatar-uploader .el-upload {
  border: 2px dashed #dcdfe6;
  border-radius: 10px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;

  &:hover {
    border-color: #409EFF;
  }
}

.avatar-uploader-icon {
  font-size: 32px;
  color: #c0c4cc;
  width: 150px;
  height: 200px;
  line-height: 200px;
  text-align: center;
}

.avatar {
  width: 150px;
  height: 200px;
  display: block;
  object-fit: cover;
  border-radius: 6px;
}

::v-deep .book-dialog {
  border-radius: 12px;

  .el-dialog__header {
    padding: 18px 24px 12px;
    border-bottom: 1px solid #f0f0f0;
  }

  .el-dialog__title {
    font-weight: 700;
    font-size: 17px;
    color: #303133;
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    padding: 12px 24px 20px;
    border-top: 1px solid #f0f0f0;
  }
}

.book-detail {
  .cover-section {
    text-align: center;
    padding: 16px 0;
    border-bottom: 1px solid #f5f5f5;
    margin-bottom: 8px;

    .cover-label {
      font-size: 13px;
      color: #909399;
      margin-bottom: 10px;
      display: block;
    }

    .cover-container {
      width: 150px;
      height: 200px;
      margin: 0 auto;
      border-radius: 8px;
      overflow: hidden;
      border: 1px solid #ebeef5;
    }

    .cover-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .cover-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: #fafafa;
      color: #c0c4cc;

      i {
        font-size: 32px;
        margin-bottom: 8px;
      }

      span {
        font-size: 13px;
      }
    }
  }

  .detail-item {
    display: flex;
    padding: 12px 0;
    border-bottom: 1px solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    &.desc {
      flex-direction: column;
      gap: 8px;

      .detail-value {
        line-height: 1.6;
        color: #606266;
      }
    }
  }

  .detail-label {
    width: 80px;
    font-size: 13px;
    color: #909399;
    flex-shrink: 0;
  }

  .detail-value {
    flex: 1;
    font-size: 14px;
    color: #303133;
    font-weight: 500;
  }
}
</style>

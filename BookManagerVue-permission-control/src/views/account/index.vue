<template>
  <div class="account-page">
    <div class="profile-header" :class="isSuperAdmin ? 'super-admin-bg' : (isAdmin ? 'admin-bg' : 'reader-bg')">
      <div class="profile-avatar-wrap" @click="triggerUpload">
        <div v-if="!avatarUrl" class="avatar-circle avatar-default">
          <i :class="isSuperAdmin ? 'el-icon-star-on' : (isAdmin ? 'el-icon-s-custom' : 'el-icon-user-solid')"></i>
          <span class="upload-hint">点击上传</span>
        </div>
        <img v-else :src="avatarUrl" class="avatar-img" alt="头像" />
        <div class="online-dot"></div>
        <div class="avatar-overlay">
          <i class="el-icon-camera"></i>
        </div>
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleFileChange"
        />
      </div>
      <div class="profile-info">
        <h2 class="user-name">{{ name || '--' }}</h2>
        <p class="user-role-tag" :class="isSuperAdmin ? 'tag-super-admin' : (isAdmin ? 'tag-admin' : 'tag-reader')">
          <i :class="isSuperAdmin ? 'el-icon-star-on' : (isAdmin ? 'el-icon-s-custom' : 'el-icon-user-solid')"></i>
          {{ isSuperAdmin ? '超级管理员' : (isAdmin ? '管理员' : '读者') }}
        </p>
      </div>
    </div>

    <div class="info-cards">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="info-card" shadow="hover">
            <div slot="header" class="card-title"><i class="el-icon-user"></i><span>基本信息</span></div>
            <ul class="info-list">
              <li><span class="info-label">用户ID</span><span class="info-value">{{ id }}</span></li>
              <li><span class="info-label">用户名</span><span class="info-value name-value">{{ name }}</span></li>
              <li><span class="info-label">角色</span><span class="info-value"><el-tag :type="isSuperAdmin ? 'danger' : (isAdmin ? 'warning' : 'success')" size="small">{{ isSuperAdmin ? '超级管理员' : (isAdmin ? '管理员' : '读者') }}</el-tag></span></li>
              <li><span class="info-label">登录状态</span><span class="info-value"><el-tag type="success" size="small">在线</el-tag></span></li>
              <li><span class="info-label">头像状态</span><span class="info-value"><el-tag :type="avatarUrl ? 'success' : 'info'" size="small">{{ avatarUrl ? '已设置' : '未设置' }}</el-tag></span></li>
            </ul>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="info-card" shadow="hover">
            <div slot="header" class="card-title"><i class="el-icon-setting"></i><span>快捷操作</span></div>
            <div class="action-grid">
              <router-link to="/account/password" class="quick-action">
                <div class="action-icon-wrap lock"><i class="el-icon-lock"></i></div>
                <span>修改密码</span>
              </router-link>
              <router-link v-if="isAdmin" to="/account/user" class="quick-action">
                <div class="action-icon-wrap users"><i class="el-icon-user-solid"></i></div>
                <span>用户管理</span>
              </router-link>
              <router-link to="/bookmanage/bookinfo" class="quick-action">
                <div class="action-icon-wrap books"><i class="el-icon-reading"></i></div>
                <span>浏览图书</span>
              </router-link>
              <router-link to="/bookmanage/borrow" class="quick-action">
                <div class="action-icon-wrap borrows"><i class="el-icon-notebook-2"></i></div>
                <span>借阅记录</span>
              </router-link>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="tips-section">
      <el-alert
        :title="isSuperAdmin ? '最高管理员，拥有全部权限' : (isAdmin ? '您拥有系统管理权限，请谨慎操作' : '您可以浏览和借阅图书，请在14天内归还')"
        :type="isSuperAdmin ? 'danger' : (isAdmin ? 'warning' : 'info')"
        :closable="false"
        show-icon
      />
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Profile',
  data() {
    return {}
  },
  computed: {
    ...mapGetters(['id', 'name', 'roles', 'avatar']),
    isAdmin() {
      return this.roles && this.roles.includes('admin')
    },
    isSuperAdmin() {
      return this.roles && this.roles.includes('super_admin')
    },
    avatarUrl: {
      get() { return this.avatar },
      set(val) { this.$store.dispatch('user/setAvatar', val) }
    }
  },
  created() {
    const saved = localStorage.getItem('user_avatar_' + (this.id || 'default'))
    if (saved) this.avatarUrl = saved
  },
  methods: {
    triggerUpload() {
      this.$refs.fileInput.click()
    },
    handleFileChange(e) {
      const file = e.target.files[0]
      if (!file) return

      if (!file.type.startsWith('image/')) {
        this.$message.error('只能上传图片文件!')
        e.target.value = ''
        return
      }
      if (file.size / 1024 / 1024 > 2) {
        this.$message.error('图片大小不能超过 2MB!')
        e.target.value = ''
        return
      }

      const formData = new FormData()
      formData.append('file', file)

      fetch('http://localhost:8092/BookManager/update/updateImg', {
        method: 'POST',
        body: formData
      })
        .then(res => res.json())
        .then(res => {
          if (res.code === 0 && res.data) {
            this.avatarUrl = res.data
            localStorage.setItem('user_avatar_' + (this.id || 'default'), res.data)
            this.$message.success('头像更新成功')
          } else {
            this.$message.error('头像上传失败')
          }
        })
        .catch(() => { this.$message.error('上传请求失败') })

      e.target.value = ''
    }
  }
}
</script>

<style lang="scss" scoped>
.account-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.profile-header {
  display: flex; align-items: center; gap: 24px;
  border-radius: 16px; padding: 32px 36px;
  margin-bottom: 20px; color: white;
  position: relative; overflow: hidden;

  &.admin-bg, &.reader-bg, &.super-admin-bg { background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%); }

  &.super-admin-bg {
    background: linear-gradient(135deg, #2d1b3d 0%, #1a1a2e 50%, #0f3460 100%);
  }

  &::before {
    content: ''; position: absolute; top: -40%; right: -10%;
    width: 350px; height: 350px; background: rgba(255, 255, 255, 0.05); border-radius: 50%;
  }

  .profile-avatar-wrap {
    position: relative; z-index: 1; flex-shrink: 0; cursor: pointer;

    .avatar-circle {
      width: 90px; height: 90px; border-radius: 24px;
      background: rgba(255, 255, 255, 0.15);
      display: flex; align-items: center; justify-content: center;
      backdrop-filter: blur(4px);
      border: 2px dashed rgba(255, 255, 255, 0.35);
      transition: all 0.3s ease;

      &:hover {
        border-color: rgba(255, 255, 255, 0.6);
        background: rgba(255, 255, 255, 0.22);
        .upload-hint { opacity: 1; transform: translateY(0); }
      }

      i { font-size: 36px; }

      .upload-hint {
        position: absolute; bottom: -18px; font-size: 10px;
        opacity: 0; transition: all 0.25s ease; transform: translateY(-4px);
        white-space: nowrap; letter-spacing: 1px;
      }
    }

    .avatar-img {
      width: 90px; height: 90px; border-radius: 24px;
      object-fit: cover; border: 3px solid rgba(255, 255, 255, 0.35);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
      transition: all 0.3s ease;

      &:hover { border-color: rgba(255, 255, 255, 0.65); }
    }

    .online-dot {
      position: absolute; bottom: 2px; right: 2px;
      width: 14px; height: 14px; border-radius: 50%;
      background: #67C23A; border: 3px solid rgba(26, 26, 46, 0.8);
      z-index: 2;
    }

    .avatar-overlay {
      position: absolute; inset: 0; border-radius: 24px;
      background: rgba(0, 0, 0, 0.45);
      display: flex; align-items: center; justify-content: center;
      opacity: 0; transition: opacity 0.3s ease; z-index: 1;

      i { font-size: 24px; color: white; }
    }

    &:hover .avatar-overlay { opacity: 1; }
  }

  .profile-info {
    position: relative; z-index: 1;

    .user-name { font-size: 26px; font-weight: 700; margin: 0 0 6px; letter-spacing: 1px; }

    .user-role-tag {
      display: inline-flex; align-items: center; gap: 5px;
      padding: 4px 14px; border-radius: 12px; font-size: 13px; font-weight: 600;

      &.tag-admin { background: rgba(64, 158, 255, 0.25); }
      &.tag-super-admin { background: rgba(245, 108, 108, 0.3); }
      &.tag-reader { background: rgba(103, 194, 58, 0.2); }
      i { font-size: 13px; }
    }
  }
}

.info-cards {
  .info-card {
    border-radius: 14px; border: none;

    ::v-deep .el-card__header { padding: 15px 20px; border-bottom: 1px solid #f0f0f0; }
    ::v-deep .el-card__body { padding: 20px; }

    .card-title {
      display: flex; align-items: center; gap: 7px;
      font-size: 15px; font-weight: 600; color: #303133;
      i { font-size: 17px; color: #409EFF; }
    }

    .info-list {
      list-style: none; padding: 0; margin: 0;

      li {
        display: flex; justify-content: space-between; align-items: center;
        padding: 11px 0; border-bottom: 1px dashed #ebeef5;
        &:last-child { border-bottom: none; }
        .info-label { color: #909399; font-size: 13px; }
        .info-value { color: #303133; font-size: 14px; font-weight: 500; }
        .name-value { font-size: 17px; font-weight: 600; }
      }
    }

    .action-grid {
      display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px;

      .quick-action {
        display: flex; flex-direction: column; align-items: center; gap: 10px;
        padding: 20px 10px; border-radius: 12px;
        text-decoration: none; color: inherit;
        transition: all 0.3s ease; border: 1px solid #f0f0f0;

        &:hover { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08); border-color: transparent; }

        .action-icon-wrap {
          width: 48px; height: 48px; border-radius: 12px;
          display: flex; align-items: center; justify-content: center;
          font-size: 22px; color: white;

          &.lock { background: linear-gradient(135deg, #667eea, #764ba2); }
          &.users { background: linear-gradient(135deg, #f093fb, #f5576c); }
          &.books { background: linear-gradient(135deg, #4facfe, #00f2fe); }
          &.borrows { background: linear-gradient(135deg, #43e97b, #38f9d7); }
        }
        span { font-size: 13px; font-weight: 500; color: #606266; }
      }
    }
  }
}
.tips-section { margin-top: 16px; }
</style>

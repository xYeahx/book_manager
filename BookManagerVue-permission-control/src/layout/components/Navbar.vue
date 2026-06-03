<template>
  <div class="navbar">
    <hamburger :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />

    <breadcrumb class="breadcrumb-container" />

    <div class="right-menu">
      <el-dropdown class="avatar-container" trigger="click">
        <div class="avatar-wrapper">
          <img v-if="avatar" :src="avatar" class="user-avatar" alt="头像">
          <div v-else class="user-avatar default-avatar">
            <i class="el-icon-user-solid"></i>
          </div>
          <span class="user-name">{{ name }}</span>
          <i class="el-icon-caret-bottom" />
        </div>
        <el-dropdown-menu slot="dropdown" class="user-dropdown">
          <router-link to="/">
            <el-dropdown-item>首页</el-dropdown-item>
          </router-link>
          <router-link to="/account/profile">
            <el-dropdown-item>我的账号</el-dropdown-item>
          </router-link>
          <el-dropdown-item divided @click.native="logout">
            <span style="display:block;">退出登录</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'

export default {
  components: { Breadcrumb, Hamburger },
  computed: {
    ...mapGetters(['sidebar', 'avatar', 'name'])
  },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    async logout() {
      await this.$store.dispatch('user/logout')
      this.$router.push(`/login`)
    }
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);

  .hamburger-container {
    line-height: 46px; height: 100%; float: left; cursor: pointer;
    transition: background .3s;
    -webkit-tap-highlight-color:transparent;
    &:hover { background: rgba(0, 0, 0, .025) }
  }

  .breadcrumb-container { float: left; }

  .right-menu {
    float: right; height: 100%; line-height: 50px;

    &:focus { outline: none; }

    .avatar-container {
      margin-right: 24px;

      .avatar-wrapper {
        display: flex; align-items: center; gap: 8px;
        padding: 4px 12px 4px 6px;
        border-radius: 20px;
        cursor: pointer;
        transition: all 0.3s ease;
        position: relative;

        &:hover {
          background: #f5f7fa;
        }

        .user-avatar {
          width: 32px; height: 32px; border-radius: 50%;
          object-fit: cover; border: 2px solid #e4e7ed;
          flex-shrink: 0;

          &.default-avatar {
            background: linear-gradient(135deg, #667eea, #764ba2);
            display: flex; align-items: center; justify-content: center;
            border: none;

            i { font-size: 16px; color: white; }
          }
        }

        .user-name {
          font-size: 14px; font-weight: 500; color: #303133;
          white-space: nowrap; max-width: 120px;
          overflow: hidden; text-overflow: ellipsis;
        }

        .el-icon-caret-bottom {
          font-size: 11px; color: #909399; transition: transform 0.3s;
        }
      }
    }
  }
}
</style>

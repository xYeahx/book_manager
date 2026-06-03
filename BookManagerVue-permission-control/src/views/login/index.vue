<template>
  <div class="login-container">
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="login-card">
      <div class="login-left">
        <div class="brand-section">
          <div class="logo-icon">
            <svg-icon icon-class="book" />
          </div>
          <h1 class="brand-title">图书管理系统</h1>
          <p class="brand-subtitle">Library Management System</p>
          <div class="feature-list">
            <div class="feature-item">
              <i class="el-icon-reading"></i>
              <span>海量图书资源</span>
            </div>
            <div class="feature-item">
              <i class="el-icon-star-on"></i>
              <span>便捷借阅体验</span>
            </div>
            <div class="feature-item">
              <i class="el-icon-s-check"></i>
              <span>安全可靠服务</span>
            </div>
          </div>
        </div>
      </div>

      <div class="login-right">
        <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form" auto-complete="on">
          <div class="form-header">
            <h2 class="form-title">欢迎回来</h2>
            <p class="form-subtitle">请输入您的账号信息登录系统</p>
          </div>

          <el-form-item prop="username">
            <el-input
              ref="username"
              v-model="loginForm.username"
              placeholder="请输入用户名"
              name="username"
              type="text"
              tabindex="1"
              auto-complete="on"
              prefix-icon="el-icon-user"
              @blur="handleUsernameBlur"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              :key="passwordType"
              ref="password"
              v-model="loginForm.password"
              :type="passwordType"
              placeholder="请输入密码"
              name="password"
              tabindex="2"
              auto-complete="on"
              prefix-icon="el-icon-lock"
              @keyup.enter.native="handleLogin"
            >
              <i slot="suffix" :class="'el-icon-' + (passwordType === 'password' ? 'view' : 'hide')" class="show-pwd" @click="showPwd"></i>
            </el-input>
          </el-form-item>

          <transition name="role-fade">
            <div v-if="roleDetected" class="identity-display">
              <i :class="detectedRole === 2 ? 'el-icon-star-on' : (detectedRole === 1 ? 'el-icon-s-custom' : 'el-icon-user-solid')" :style="{ color: detectedRole === 2 ? '#F56C6C' : (detectedRole === 1 ? '#409EFF' : '#67C23A') }"></i>
              <span class="identity-label">身份：</span>
              <span class="identity-value" :class="detectedRole === 2 ? 'super-admin-tag' : (detectedRole === 1 ? 'admin-tag' : 'reader-tag')">
                {{ detectedRole === 2 ? '超级管理员' : (detectedRole === 1 ? '管理员' : '读者') }}
              </span>
            </div>
          </transition>

          <el-button :loading="loading" type="primary" class="login-btn" @click.native.prevent="handleLogin">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>

          <div class="form-footer">
            <span class="footer-text">还没有账号？</span>
            <el-link type="primary" :underline="false" @click.native.prevent="handleRegister">立即注册</el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { checkRole } from '@/api/user'

export default {
  name: 'Login',
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!value || value.trim() === '') {
        callback(new Error('请输入用户名'))
      } else {
        callback()
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (!value || value.trim() === '') {
        callback(new Error('请输入密码'))
      } else if (value.length < 3) {
        callback(new Error('密码长度不能小于3位'))
      } else {
        callback()
      }
    }
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }]
      },
      loading: false,
      passwordType: 'password',
      redirect: undefined,
      roleDetected: false,
      detectedRole: null,
      checkingRole: false
    }
  },
  methods: {
    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = ''
      } else {
        this.passwordType = 'password'
      }
      this.$nextTick(() => {
        this.$refs.password.focus()
      })
    },
    handleUsernameBlur() {
      const username = this.loginForm.username.trim()
      if (!username) {
        this.roleDetected = false
        this.detectedRole = null
        return
      }
      this.checkingRole = true
      checkRole(username).then(res => {
        if (res.status === 200 && res.data) {
          this.detectedRole = res.data.isadmin
          this.roleDetected = true
        } else {
          this.roleDetected = false
          this.detectedRole = null
        }
      }).catch(() => {
        this.roleDetected = false
        this.detectedRole = null
      }).finally(() => {
        this.checkingRole = false
      })
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          const { username, password } = this.loginForm
          this.$store.dispatch('user/login', {
            username: username.trim(),
            password,
            isadmin: this.detectedRole
          }).then(() => {
            this.$router.push({ path: '/' })
            this.loading = false
          }).catch((message) => {
            this.$message.error(message)
            this.loading = false
          })
        }
      })
    },
    handleRegister() {
      this.$router.push({ path: '/register' })
    }
  }
}
</script>

<style lang="scss" scoped>
$primary-color: #409EFF;
$primary-gradient: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
$card-bg: rgba(255, 255, 255, 0.97);

.login-container {
  min-height: 100vh;
  width: 100%;
  background: $primary-gradient;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  .bg-shapes {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    pointer-events: none;

    .shape {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.08);
    }

    .shape-1 {
      width: 400px;
      height: 400px;
      top: -100px;
      right: -100px;
      animation: float 8s ease-in-out infinite;
    }

    .shape-2 {
      width: 300px;
      height: 300px;
      bottom: -50px;
      left: -80px;
      animation: float 10s ease-in-out infinite reverse;
    }

    .shape-3 {
      width: 200px;
      height: 200px;
      top: 50%;
      left: 10%;
      animation: float 6s ease-in-out infinite;
    }

    @keyframes float {
      0%, 100% { transform: translateY(0) rotate(0deg); }
      50% { transform: translateY(-30px) rotate(5deg); }
    }
  }

  .login-card {
    display: flex;
    width: 900px;
    max-width: 95vw;
    min-height: 560px;
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 25px 60px rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(10px);
    z-index: 1;
    animation: slideUp 0.6s ease-out;

    @keyframes slideUp {
      from { opacity: 0; transform: translateY(30px); }
      to { opacity: 1; transform: translateY(0); }
    }
  }

  .login-left {
    width: 380px;
    background: linear-gradient(135deg, rgba(20, 20, 30, 0.95) 0%, rgba(40, 40, 55, 0.95) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;
    color: white;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: -50%;
      right: -50%;
      width: 200%;
      height: 200%;
      background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    }

    .brand-section {
      text-align: center;
      z-index: 1;

      .logo-icon {
        width: 80px;
        height: 80px;
        background: rgba(255, 255, 255, 0.15);
        border-radius: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 24px;
        font-size: 40px;
        color: white;
        backdrop-filter: blur(4px);
        border: 1px solid rgba(255, 255, 255, 0.2);
      }

      .brand-title {
        font-size: 28px;
        font-weight: 700;
        margin: 0 0 8px;
        letter-spacing: 2px;
      }

      .brand-subtitle {
        font-size: 14px;
        opacity: 0.85;
        margin: 0 0 36px;
        letter-spacing: 1px;
      }

      .feature-list {
        text-align: left;

        .feature-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px 16px;
          margin-bottom: 8px;
          background: rgba(255, 255, 255, 0.1);
          border-radius: 10px;
          transition: all 0.3s ease;

          &:hover {
            background: rgba(255, 255, 255, 0.18);
            transform: translateX(6px);
          }

          i {
            font-size: 20px;
            width: 28px;
            text-align: center;
          }

          span {
            font-size: 14px;
            font-weight: 500;
          }
        }
      }
    }
  }

  .login-right {
    flex: 1;
    background: $card-bg;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 48px 56px;

    .login-form {
      width: 100%;

      .form-header {
        margin-bottom: 36px;

        .form-title {
          font-size: 26px;
          font-weight: 700;
          color: #303133;
          margin: 0 0 8px;
        }

        .form-subtitle {
          font-size: 14px;
          color: #909399;
          margin: 0;
        }
      }

      ::v-deep .el-input__inner {
        height: 46px;
        line-height: 46px;
        border-radius: 10px;
        border: 2px solid #E4E7ED;
        padding-left: 38px;
        font-size: 14px;
        transition: all 0.3s ease;

        &:focus {
          border-color: $primary-color;
          box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
        }

        &:hover:not(:focus) {
          border-color: #C0C4CC;
        }
      }

      ::v-deep .el-input__prefix {
        left: 12px;
        font-size: 17px;
        color: #909399;
      }

      ::v-deep .el-input__suffix {
        right: 12px;
      }

      ::v-deep .el-form-item {
        margin-bottom: 22px;
      }

      .show-pwd {
        cursor: pointer;
        font-size: 16px;
        color: #909399;
        transition: color 0.3s;

        &:hover {
          color: $primary-color;
        }
      }

      .identity-display {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 10px 16px;
        margin-bottom: 22px;
        background: #f5f7fa;
        border-radius: 10px;
        border: 1px solid #e4e7ed;
        font-size: 14px;

        i {
          font-size: 18px;
        }

        .identity-label {
          color: #606266;
        }

        .identity-value {
          font-weight: 600;
          padding: 2px 12px;
          border-radius: 12px;
          font-size: 13px;

          &.admin-tag {
            background: rgba(64, 158, 255, 0.1);
            color: #409EFF;
          }

          &.super-admin-tag {
            background: rgba(245, 108, 108, 0.1);
            color: #F56C6C;
          }

          &.reader-tag {
            background: rgba(103, 194, 58, 0.1);
            color: #67C23A;
          }
        }
      }

      .role-fade-enter-active,
      .role-fade-leave-active {
        transition: all 0.3s ease;
      }

      .role-fade-enter,
      .role-fade-leave-to {
        opacity: 0;
        transform: translateY(-8px);
      }

      .login-btn {
        width: 100%;
        height: 48px;
        border-radius: 10px;
        font-size: 16px;
        font-weight: 600;
        letter-spacing: 4px;
        background: $primary-gradient;
        border: none;
        margin-top: 8px;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 25px rgba(102, 126, 234, 0.45);
        }

        &:active {
          transform: translateY(0);
        }
      }

      .form-footer {
        text-align: center;
        margin-top: 20px;

        .footer-text {
          font-size: 13px;
          color: #909399;
        }

        .el-link {
          font-weight: 600;
          font-size: 13px;
        }
      }
    }
  }
}

@media screen and (max-width: 768px) {
  .login-container .login-card {
    flex-direction: column;
    min-height: auto;

    .login-left {
      display: none;
    }

    .login-right {
      padding: 32px 24px;
    }
  }
}
</style>

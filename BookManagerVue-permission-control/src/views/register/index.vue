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
              <span>安全可靠管理</span>
            </div>
          </div>
        </div>
      </div>

      <div class="login-right">
        <el-form
          ref="loginForm"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          auto-complete="on"
        >
          <div class="form-header">
            <h2 class="form-title">创建新账号</h2>
            <p class="form-subtitle">填写以下信息完成注册</p>
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
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              ref="password"
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              name="password"
              tabindex="2"
              auto-complete="on"
              prefix-icon="el-icon-lock"
            />
          </el-form-item>

          <el-form-item prop="repeat">
            <el-input
              ref="repeat"
              v-model="loginForm.repeat"
              type="password"
              placeholder="请确认密码"
              name="repeat"
              tabindex="3"
              auto-complete="on"
              prefix-icon="el-icon-lock"
              @keyup.enter.native="handleRight"
            />
          </el-form-item>

          <el-form-item prop="isadmin">
            <div class="role-selector">
              <div class="role-option reader" :class="{ active: loginForm.isadmin === 0 }" @click="selectRole(0)">
                <i class="el-icon-user-solid"></i>
                <span>读者</span>
              </div>
              <div class="role-option admin" :class="{ active: loginForm.isadmin === 1 }" @click="selectRole(1)">
                <i class="el-icon-s-custom"></i>
                <span>管理员</span>
              </div>
              <div class="role-option super-admin" :class="{ active: loginForm.isadmin === 2 }" @click="selectRole(2)">
                <i class="el-icon-star-on"></i>
                <span>超管</span>
              </div>
            </div>
          </el-form-item>

          <!-- 邀请码输入框（仅选择管理员/超管时显示） -->
          <transition name="fade-slide">
            <el-form-item v-if="loginForm.isadmin > 0" prop="inviteCode" key="inviteCode">
              <el-input
                v-model="loginForm.inviteCode"
                placeholder="请输入管理员邀请码"
                prefix-icon="el-icon-key"
                type="password"
                show-password
              />
              <div class="invite-tip">注册管理/超管账号需要有效邀请码</div>
            </el-form-item>
          </transition>

          <el-button :loading="loading" type="primary" class="login-btn" @click.native.prevent="handleRight">
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>

          <div class="form-footer">
            <span class="footer-text">已有账号？</span>
            <el-link type="primary" :underline="false" @click.native.prevent="handleBack">返回登录</el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { register } from '@/api/user'

export default {
  name: 'Register',
  data() {
    const validateRepeat = (rule, value, callback) => {
      if (value !== this.loginForm.password) {
        callback(new Error('两次输入的密码不一致!'))
      } else {
        callback()
      }
    }
    return {
      loginForm: {
        username: '',
        password: '',
        repeat: '',
        isadmin: 0,
        inviteCode: ''
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 3, message: '密码长度不能小于3位', trigger: 'blur' }
        ],
        repeat: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { trigger: 'blur', validator: validateRepeat }
        ],
        isadmin: [
          { required: true, message: '请选择身份', trigger: 'change' }
        ],
        inviteCode: [
          { required: false, message: '请输入邀请码', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    selectRole(role) {
      this.loginForm.isadmin = role
      if (role === 0) this.loginForm.inviteCode = ''
    },
    handleRight() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          // 选择高级角色但未填邀请码
          if (this.loginForm.isadmin > 0 && !this.loginForm.inviteCode.trim()) {
            this.$message.warning('请输入管理员邀请码')
            return
          }
          this.loading = true
          register({
            username: this.loginForm.username,
            password: this.loginForm.password,
            isadmin: this.loginForm.isadmin,
            inviteCode: this.loginForm.inviteCode
          }).then((res) => {
            this.loading = false
            if (res === -1) {
              this.$message.error('邀请码无效，无法注册该角色')
            } else if (res === 0) {
              this.$message.error('注册失败，可能账号重复了')
            } else {
              this.$message.success('注册成功')
              setTimeout(() => { this.$router.push('/login') }, 1000)
            }
          }).catch(() => { this.loading = false })
        }
      })
    },
    handleBack() {
      this.$router.push('/login')
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

      ::v-deep .el-form-item {
        margin-bottom: 22px;
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

      .role-selector {
        display: flex;
        gap: 10px;

        .role-option {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          gap: 6px;
          padding: 12px 8px;
          border: 2px solid #E4E7ED;
          border-radius: 10px;
          cursor: pointer;
          transition: all 0.3s ease;
          color: #909399;
          white-space: nowrap;

          i { font-size: 20px; margin-bottom: 2px; }
          span { font-size: 13px; font-weight: 500; }

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.08);
          }

          &.reader:hover, &.reader.active {
            border-color: $primary-color;
            color: $primary-color;
          }
          &.reader.active {
            background: rgba(64, 158, 255, 0.08);
            box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
          }

          &.admin:hover, &.admin.active {
            border-color: #E6A23C;
            color: #E6A23C;
          }
          &.admin.active {
            background: rgba(230, 162, 60, 0.08);
            box-shadow: 0 2px 8px rgba(230, 162, 60, 0.15);
          }

          &.super-admin:hover, &.super-admin.active {
            border-color: #F56C6C;
            color: #F56C6C;
          }
          &.super-admin.active {
            background: rgba(245, 108, 108, 0.08);
            box-shadow: 0 2px 8px rgba(245, 108, 108, 0.15);
          }
        }
      }

      .invite-tip {
        font-size: 12px;
        color: #E6A23C;
        margin-top: 4px;
        line-height: 1.4;
      }

      /* 过渡动画 */
      .fade-slide-enter-active,
      .fade-slide-leave-active {
        transition: all 0.3s ease;
        overflow: hidden;
      }
      .fade-slide-enter,
      .fade-slide-leave-to {
        opacity: 0;
        max-height: 0;
        margin-bottom: 0;
        padding-top: 0;
        padding-bottom: 0;
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

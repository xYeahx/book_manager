<template>
  <div class="settings-page">
    <div class="page-header">
      <h2>⚙️ 系统设置中心</h2>
      <p>管理系统全局配置参数 · 仅超级管理员可访问</p>
    </div>

    <div class="settings-content">
      <el-tabs v-model="activeTab" type="border-card" class="settings-tabs">
        <!-- 借阅规则配置 -->
        <el-tab-pane name="borrow">
          <template slot="label">
            <i class="el-icon-time"></i>
            <span>借阅规则</span>
          </template>

          <div class="tab-header">
            <h3>📚 借阅规则配置</h3>
            <p>设置图书借阅相关的核心规则参数</p>
          </div>

          <div class="setting-cards">
            <div class="setting-card">
              <div class="card-label">
                <i class="el-icon-date"></i>
                <span>默认借阅天数</span>
              </div>
              <div class="card-input">
                <el-input-number v-model="formData.borrow_days" :min="1" :max="365" size="medium" />
                <span class="input-unit">天</span>
              </div>
              <div class="card-desc">用户借阅图书后可持有的默认天数</div>
            </div>

            <div class="setting-card">
              <div class="card-label">
                <i class="el-icon-refresh"></i>
                <span>最大续借次数</span>
              </div>
              <div class="card-input">
                <el-input-number v-model="formData.max_renew_count" :min="0" :max="10" size="medium" />
                <span class="input-unit">次</span>
              </div>
              <div class="card-desc">每本书最多可以续借的次数，设为0则不允许续借</div>
            </div>

            <div class="setting-card">
              <div class="card-label">
                <i class="el-icon-collection"></i>
                <span>每人最大借阅数量</span>
              </div>
              <div class="card-input">
                <el-input-number v-model="formData.max_borrow_count" :min="1" :max="50" size="medium" />
                <span class="input-unit">本</span>
              </div>
              <div class="card-desc">单个用户同时最多可借阅的图书数量上限</div>
            </div>

            <div class="setting-card">
              <div class="card-label">
                <i class="el-icon-plus"></i>
                <span>续借延长天数</span>
              </div>
              <div class="card-input">
                <el-input-number v-model="formData.renew_days" :min="1" :max="180" size="medium" />
                <span class="input-unit">天/次</span>
              </div>
              <div class="card-desc">每次续借操作可延长的借阅天数</div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 注册与权限控制 -->
        <el-tab-pane name="register">
          <template slot="label">
            <i class="el-icon-user"></i>
            <span>注册权限</span>
          </template>

          <div class="tab-header">
            <h3>🔐 注册与权限控制</h3>
            <p>管理用户注册方式和权限分配规则</p>
          </div>

          <div class="setting-cards">
            <div class="setting-card">
              <div class="card-label">
                <i class="el-icon-key"></i>
                <span>注册邀请码</span>
              </div>
              <div class="card-input">
                <el-input v-model="formData.invite_code" placeholder="请输入邀请码" size="medium" show-password style="width: 240px;" />
                <el-button type="primary" size="medium" icon="el-icon-refresh" @click="generateInviteCode" style="margin-left: 10px;">生成新码</el-button>
              </div>
              <div class="card-desc">用户注册时需要输入的邀请码，留空则表示开放注册</div>
            </div>

            <div class="setting-card switch-card">
              <div class="card-label">
                <i class="el-icon-unlock"></i>
                <span>允许开放注册</span>
              </div>
              <div class="card-input">
                <el-switch v-model="formData.allow_register" active-text="开启" inactive-text="关闭" />
              </div>
              <div class="card-desc">开启后用户无需邀请码即可自行注册账号</div>
            </div>

            <div class="setting-card">
              <div class="card-label">
                <i class="el-icon-s-custom"></i>
                <span>新用户默认角色</span>
              </div>
              <div class="card-input">
                <el-select v-model="formData.default_role" size="medium" style="width: 200px;">
                  <el-option label="普通读者" :value="0" />
                  <el-option label="管理员" :value="1" />
                </el-select>
              </div>
              <div class="card-desc">新注册用户的默认身份角色</div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 提醒通知设置 -->
        <el-tab-pane name="notify">
          <template slot="label">
            <i class="el-icon-bell"></i>
            <span>通知提醒</span>
          </template>

          <div class="tab-header">
            <h3>🔔 提醒通知设置</h3>
            <p>配置借阅到期提醒和逾期罚款规则</p>
          </div>

          <div class="setting-cards">
            <div class="setting-card">
              <div class="card-label">
                <i class="el-icon-alarm-clock"></i>
                <span>到期前提醒天数</span>
              </div>
              <div class="card-input">
                <el-input-number v-model="formData.remind_days" :min="0" :max="30" size="medium" />
                <span class="input-unit">天前</span>
              </div>
              <div class="card-desc">在图书到期前几天向用户发送催还提醒通知</div>
            </div>

            <div class="setting-card">
              <div class="card-label">
                <i class="el-icon-money"></i>
                <span>逾期罚款金额</span>
              </div>
              <div class="card-input">
                <el-input-number v-model="formData.fine_amount" :min="0" :max="100" :precision="2" :step="0.5" size="medium" />
                <span class="input-unit">元/天</span>
              </div>
              <div class="card-desc">图书逾期后每天需缴纳的罚款金额，设为0则不罚款</div>
            </div>

            <div class="setting-card switch-card">
              <div class="card-label">
                <i class="el-icon-message-solid"></i>
                <span>启用消息推送</span>
              </div>
              <div class="card-input">
                <el-switch v-model="formData.enable_notification" active-text="开启" inactive-text="关闭" />
              </div>
              <div class="card-desc">是否向用户发送借阅到期和逾期的系统消息通知</div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 系统基本信息 -->
        <el-tab-pane name="basic">
          <template slot="label">
            <i class="el-icon-office-building"></i>
            <span>基本信息</span>
          </template>

          <div class="tab-header">
            <h3>🏢 系统基本信息</h3>
            <p>配置图书馆的基本信息和公告内容</p>
          </div>

          <div class="setting-cards">
            <div class="setting-card full-width">
              <div class="card-label">
                <i class="el-icon-school"></i>
                <span>图书馆名称</span>
              </div>
              <div class="card-input">
                <el-input v-model="formData.library_name" placeholder="请输入图书馆名称" size="medium" style="width: 400px;" maxlength="50" show-word-limit />
              </div>
              <div class="card-desc">显示在系统各处的图书馆正式名称</div>
            </div>

            <div class="setting-card full-width">
              <div class="card-label">
                <i class="el-icon-phone"></i>
                <span>联系电话</span>
              </div>
              <div class="card-input">
                <el-input v-model="formData.contact_phone" placeholder="请输入联系电话" size="medium" style="width: 300px;" maxlength="20" />
              </div>
              <div class="card-desc">管理员联系电话，用于用户咨询和联系</div>
            </div>

            <div class="setting-card full-width">
              <div class="card-label">
                <i class="el-icon-time"></i>
                <span>开放时间说明</span>
              </div>
              <div class="card-input">
                <el-input v-model="formData.opening_hours" type="textarea" :rows="3" placeholder="例如：周一至周五 8:00-18:00&#10;周六至周日 9:00-17:00" size="medium" style="width: 500px;" maxlength="200" show-word-limit />
              </div>
              <div class="card-desc">图书馆借阅服务的营业时间说明</div>
            </div>

            <div class="setting-card full-width">
              <div class="card-label">
                <i class="el-icon-document"></i>
                <span>系统公告</span>
              </div>
              <div class="card-input">
                <el-input v-model="formData.system_notice" type="textarea" :rows="4" placeholder="请输入系统公告内容，将显示在首页..." size="medium" style="width: 600px;" maxlength="500" show-word-limit />
              </div>
              <div class="card-desc">显示在首页的系统公告或重要通知信息</div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 安全策略 -->
        <el-tab-pane name="security">
          <template slot="label">
            <i class="el-icon-lock"></i>
            <span>安全策略</span>
          </template>

          <div class="tab-header">
            <h3>🛡️ 安全策略配置</h3>
            <p>设置密码策略和账户安全保护措施</p>
          </div>

          <div class="setting-cards">
            <div class="setting-card">
              <div class="card-label">
                <i class="el-icon-lock"></i>
                <span>密码最小长度</span>
              </div>
              <div class="card-input">
                <el-input-number v-model="formData.min_password_length" :min="6" :max="20" size="medium" />
                <span class="input-unit">字符</span>
              </div>
              <div class="card-desc">用户密码最少需要包含的字符数量</div>
            </div>

            <div class="setting-card">
              <div class="card-label">
                <i class="el-icon-warning-outline"></i>
                <span>登录失败锁定次数</span>
              </div>
              <div class="card-input">
                <el-input-number v-model="formData.max_login_attempts" :min="3" :max="10" size="medium" />
                <span class="input-unit">次</span>
              </div>
              <div class="card-desc">连续登录失败多少次后自动锁定账户</div>
            </div>

            <div class="setting-card switch-card">
              <div class="card-label">
                <i class="el-icon-warning"></i>
                <span>强制修改初始密码</span>
              </div>
              <div class="card-input">
                <el-switch v-model="formData.force_change_password" active-text="开启" inactive-text="关闭" />
              </div>
              <div class="card-desc">新用户首次登录时强制要求修改默认密码</div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 数据管理 -->
        <el-tab-pane name="data">
          <template slot="label">
            <i class="el-icon-data-analysis"></i>
            <span>数据管理</span>
          </template>

          <div class="tab-header">
            <h3>📊 数据管理与维护</h3>
            <p>清理日志数据和系统维护相关操作</p>
          </div>

          <div class="setting-cards">
            <div class="setting-card">
              <div class="card-label">
                <i class="el-icon-delete"></i>
                <span>日志保留天数</span>
              </div>
              <div class="card-input">
                <el-input-number v-model="formData.log_retention_days" :min="30" :max="365" size="medium" />
                <span class="input-unit">天</span>
              </div>
              <div class="card-desc">操作日志自动清理前的保留时间</div>
            </div>

            <div class="action-card">
              <div class="action-header">
                <i class="el-icon-brush"></i>
                <span>清理过期日志</span>
              </div>
              <div class="action-body">
                <p>删除 {{ formData.log_retention_days }} 天前的所有操作日志记录</p>
                <el-button type="danger" size="medium" icon="el-icon-delete" :loading="cleaningLogs" @click="handleCleanLogs">立即清理</el-button>
              </div>
            </div>

            <div class="action-card warning">
              <div class="action-header">
                <i class="el-icon-refresh-right"></i>
                <span>重置为默认值</span>
              </div>
              <div class="action-body">
                <p>将所有配置项恢复为系统默认值，此操作不可撤销！</p>
                <el-button type="warning" size="medium" icon="el-icon-refresh-right" @click="handleResetToDefault">重置配置</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div class="save-area">
      <el-button type="primary" size="large" icon="el-icon-check" :loading="saving" @click="handleSaveAll" class="save-btn">保存全部设置</el-button>
      <el-button size="large" icon="el-icon-refresh-left" @click="handleReload" class="reload-btn">重新加载</el-button>
    </div>
  </div>
</template>

<script>
import { getAllConfig, batchUpdateConfig } from '@/api/system'

export default {
  name: 'SystemSettings',
  data() {
    return {
      activeTab: 'borrow',
      saving: false,
      cleaningLogs: false,
      originalData: {},
      formData: {
        borrow_days: 14,
        max_renew_count: 1,
        max_borrow_count: 5,
        renew_days: 15,

        invite_code: 'BMS2024',
        allow_register: false,
        default_role: 0,

        remind_days: 3,
        fine_amount: 0.50,
        enable_notification: true,

        library_name: '',
        contact_phone: '',
        opening_hours: '',
        system_notice: '',

        min_password_length: 8,
        max_login_attempts: 5,
        force_change_password: true,

        log_retention_days: 90
      }
    }
  },
  created() {
    this.loadConfig()
  },
  methods: {
    loadConfig() {
      getAllConfig().then(res => {
        const configList = res.data || []
        if (configList.length > 0) {
          const configMap = {}
          configList.forEach(item => {
            configMap[item.configKey] = item.configValue
          })

          this.formData = {
            borrow_days: parseInt(configMap.borrow_days) || 14,
            max_renew_count: parseInt(configMap.max_renew_count) || 1,
            max_borrow_count: parseInt(configMap.max_borrow_count) || 5,
            renew_days: parseInt(configMap.renew_days) || 15,

            invite_code: configMap.invite_code || 'BMS2024',
            allow_register: configMap.allow_register === 'true',
            default_role: parseInt(configMap.default_role) || 0,

            remind_days: parseInt(configMap.remind_days) || 3,
            fine_amount: parseFloat(configMap.fine_amount) || 0.50,
            enable_notification: configMap.enable_notification !== 'false',

            library_name: configMap.library_name || '',
            contact_phone: configMap.contact_phone || '',
            opening_hours: configMap.opening_hours || '',
            system_notice: configMap.system_notice || '',

            min_password_length: parseInt(configMap.min_password_length) || 8,
            max_login_attempts: parseInt(configMap.max_login_attempts) || 5,
            force_change_password: configMap.force_change_password !== 'false',

            log_retention_days: parseInt(configMap.log_retention_days) || 90
          }

          this.originalData = JSON.parse(JSON.stringify(this.formData))
        }
      }).catch(() => {
        this.$message.error('加载配置失败')
      })
    },

    generateInviteCode() {
      const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
      let code = ''
      for (let i = 0; i < 8; i++) {
        code += chars.charAt(Math.floor(Math.random() * chars.length))
      }
      this.formData.invite_code = code
      this.$message.success('已生成新的邀请码：' + code)
    },

    handleSaveAll() {
      this.saving = true

      const configList = [
        { configKey: 'borrow_days', configValue: String(this.formData.borrow_days), configDesc: '默认借阅天数' },
        { configKey: 'max_renew_count', configValue: String(this.formData.max_renew_count), configDesc: '最大续借次数' },
        { configKey: 'max_borrow_count', configValue: String(this.formData.max_borrow_count), configDesc: '每人最大借阅数量' },
        { configKey: 'renew_days', configValue: String(this.formData.renew_days), configDesc: '续借延长天数' },

        { configKey: 'invite_code', configValue: this.formData.invite_code, configDesc: '注册邀请码' },
        { configKey: 'allow_register', configValue: String(this.formData.allow_register), configDesc: '允许开放注册' },
        { configKey: 'default_role', configValue: String(this.formData.default_role), configDesc: '新用户默认角色' },

        { configKey: 'remind_days', configValue: String(this.formData.remind_days), configDesc: '到期前提醒天数' },
        { configKey: 'fine_amount', configValue: String(this.formData.fine_amount), configDesc: '逾期罚款金额' },
        { configKey: 'enable_notification', configValue: String(this.formData.enable_notification), configDesc: '启用消息推送' },

        { configKey: 'library_name', configValue: this.formData.library_name, configDesc: '图书馆名称' },
        { configKey: 'contact_phone', configValue: this.formData.contact_phone, configDesc: '联系电话' },
        { configKey: 'opening_hours', configValue: this.formData.opening_hours, configDesc: '开放时间说明' },
        { configKey: 'system_notice', configValue: this.formData.system_notice, configDesc: '系统公告' },

        { configKey: 'min_password_length', configValue: String(this.formData.min_password_length), configDesc: '密码最小长度' },
        { configKey: 'max_login_attempts', configValue: String(this.formData.max_login_attempts), configDesc: '登录失败锁定次数' },
        { configKey: 'force_change_password', configValue: String(this.formData.force_change_password), configDesc: '强制修改初始密码' },

        { configKey: 'log_retention_days', configValue: String(this.formData.log_retention_days), configDesc: '日志保留天数' }
      ]

      batchUpdateConfig(configList).then(() => {
        this.saving = false
        this.originalData = JSON.parse(JSON.stringify(this.formData))
        this.$message.success('✅ 所有设置保存成功！')
      }).catch(() => {
        this.saving = false
        this.$message.error('❌ 保存失败，请稍后重试')
      })
    },

    handleCleanLogs() {
      this.$confirm(`确定要删除 ${this.formData.log_retention_days} 天前的所有操作日志吗？`, '确认清理', {
        confirmButtonText: '确定清理',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.cleaningLogs = true
        setTimeout(() => {
          this.cleaningLogs = false
          this.$message.success('日志清理完成')
        }, 1500)
      }).catch(() => {})
    },

    handleResetToDefault() {
      this.$confirm('确定要将所有配置恢复为默认值吗？此操作不可撤销！', '⚠️ 危险操作', {
        confirmButtonText: '确认重置',
        cancelButtonText: '取消',
        type: 'error'
      }).then(() => {
        this.formData = {
          borrow_days: 14,
          max_renew_count: 1,
          max_borrow_count: 5,
          renew_days: 15,

          invite_code: 'BMS2024',
          allow_register: false,
          default_role: 0,

          remind_days: 3,
          fine_amount: 0.50,
          enable_notification: true,

          library_name: '',
          contact_phone: '',
          opening_hours: '',
          system_notice: '',

          min_password_length: 8,
          max_login_attempts: 5,
          force_change_password: true,

          log_retention_days: 90
        }
        this.$message.warning('已重置为默认配置，请点击保存生效')
      }).catch(() => {})
    },

    handleReload() {
      this.loadConfig()
      this.$message.info('配置已重新加载')
    }
  }
}
</script>

<style lang="scss" scoped>
$dark-bg: #1a1a2e;
$mid-bg: #16213e;
$light-bg: #0f3460;
$page-bg: #f5f7fa;

.settings-page {
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

  h2 { margin: 0 0 8px; font-size: 22px; font-weight: 700; }
  p { margin: 0; opacity: 0.85; font-size: 14px; }
}

.settings-content {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;

  ::v-deep .el-tabs--border-card {
    border: none;
    box-shadow: none;

    .el-tabs__header {
      background: linear-gradient(135deg, $dark-bg 0%, $mid-bg 100%);
      border-bottom: none;
      margin: 0;

      .el-tabs__item {
        color: rgba(255, 255, 255, 0.75);
        border: none !important;
        padding: 0 28px;
        height: 54px;
        line-height: 54px;
        font-size: 14px;
        transition: all 0.3s;

        i { margin-right: 6px; font-size: 16px; }

        &:hover {
          color: #fff;
          background: rgba(255, 255, 255, 0.08);
        }

        &.is-active {
          color: #409EFF;
          background: #fff;
          font-weight: 600;
          box-shadow: 0 -3px 12px rgba(64, 158, 255, 0.2);
        }
      }
    }

    .el-tabs__content {
      padding: 0;
    }
  }
}

.tab-header {
  background: linear-gradient(135deg, #f8f9fb 0%, #e9ecef 100%);
  padding: 20px 28px;
  border-bottom: 1px solid #ebeef5;

  h3 { margin: 0 0 6px; font-size: 17px; color: #303133; font-weight: 600; }
  p { margin: 0; font-size: 13px; color: #909399; }
}

.setting-cards {
  padding: 24px 28px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 18px;
}

.setting-card {
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 20px;
  transition: all 0.3s;

  &:hover {
    border-color: #c0c4cc;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
    transform: translateY(-2px);
  }

  &.full-width {
    grid-column: 1 / -1;
  }

  &.switch-card {
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  .card-label {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 14px;

    i {
      font-size: 20px;
      color: #409EFF;
      width: 24px;
      text-align: center;
    }

    span {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
    }
  }

  .card-input {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;
    flex-wrap: wrap;

    .input-unit {
      font-size: 13px;
      color: #606266;
      font-weight: 500;
    }
  }

  .card-desc {
    font-size: 12px;
    color: #909399;
    line-height: 1.5;
  }
}

.action-card {
  grid-column: 1 / -1;
  background: linear-gradient(135deg, #fef9e7 0%, #fcf3cf 100%);
  border: 1px solid #f9e79f;
  border-radius: 10px;
  padding: 20px 24px;

  &.warning {
    background: linear-gradient(135deg, #fdedec 0%, #fadbd8 100%);
    border-color: #f5b7b1;
  }

  .action-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 14px;

    i { font-size: 22px; color: #e67e22; }
    &.warning i { color: #e74c3c; }
    span { font-size: 16px; font-weight: 600; color: #333; }
  }

  .action-body {
    display: flex;
    align-items: center;
    justify-content: space-between;

    p {
      margin: 0;
      font-size: 14px;
      color: #666;
      flex: 1;
      margin-right: 20px;
    }
  }
}

.save-area {
  text-align: center;
  padding: 30px 0 10px;

  .save-btn {
    width: 220px;
    height: 48px;
    font-size: 16px;
    border-radius: 10px;
    font-weight: 600;
  }

  .reload-btn {
    width: 160px;
    height: 48px;
    font-size: 15px;
    border-radius: 10px;
    margin-left: 16px;
  }
}
</style>

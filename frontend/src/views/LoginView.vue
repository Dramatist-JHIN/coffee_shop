<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <div class="login-icon">☕</div>
        <h1>像素咖啡店</h1>
        <p>8-BIT COFFEE SHOP</p>
      </div>

      <div class="pixel-card login-card">
        <div class="pixel-tabs">
          <div
            class="pixel-tab"
            :class="{ active: activeTab === 'login' }"
            @click="activeTab = 'login'"
          >
            登录
          </div>
          <div
            class="pixel-tab"
            :class="{ active: activeTab === 'register' }"
            @click="activeTab = 'register'"
          >
            注册
          </div>
        </div>

        <div class="pixel-tab-content">
          <!-- 登录表单 -->
          <form v-if="activeTab === 'login'" @submit.prevent="handleLogin">
            <div class="pixel-form-group">
              <label>用户名</label>
              <input
                type="text"
                class="pixel-input"
                v-model="loginForm.username"
                placeholder="请输入用户名"
              />
            </div>
            <div class="pixel-form-group">
              <label>密码</label>
              <input
                type="password"
                class="pixel-input"
                v-model="loginForm.password"
                placeholder="请输入密码"
              />
            </div>
            <div v-if="error" class="pixel-alert error">{{ error }}</div>
            <button type="submit" class="pixel-btn primary" style="width: 100%">
              进入咖啡店
            </button>
          </form>

          <!-- 注册表单 -->
          <form v-else @submit.prevent="handleRegister">
            <div class="pixel-form-group">
              <label>用户名</label>
              <input
                type="text"
                class="pixel-input"
                v-model="registerForm.username"
                placeholder="请输入用户名"
              />
            </div>
            <div class="pixel-form-group">
              <label>密码</label>
              <input
                type="password"
                class="pixel-input"
                v-model="registerForm.password"
                placeholder="请输入密码"
              />
            </div>
            <div class="pixel-form-group">
              <label>昵称</label>
              <input
                type="text"
                class="pixel-input"
                v-model="registerForm.nickname"
                placeholder="请输入昵称"
              />
            </div>
            <div v-if="error" class="pixel-alert error">{{ error }}</div>
            <div v-if="success" class="pixel-alert success">{{ success }}</div>
            <button type="submit" class="pixel-btn primary" style="width: 100%">
              注册账号
            </button>
          </form>
        </div>
      </div>

      <div class="demo-accounts">
        <p>演示账号：</p>
        <div class="demo-list">
          <span class="demo-item" @click="fillDemo('admin', '123456')">管理员: admin / 123456</span>
          <span class="demo-item" @click="fillDemo('barista', '123456')">咖啡师: barista / 123456</span>
          <span class="demo-item" @click="fillDemo('customer', '123456')">顾客: customer / 123456</span>
        </div>
      </div>

      <div class="game-intro">
        <h3>🎮 角色说明</h3>
        <ul>
          <li>👨‍💼 <b>管理员</b> - 管理咖啡菜单、查看订单统计</li>
          <li>👨‍🍳 <b>咖啡师</b> - 接单、制作咖啡</li>
          <li>🧑 <b>顾客</b> - 浏览菜单、下单购买</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const error = ref('')
const success = ref('')

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  nickname: ''
})

const handleLogin = async () => {
  error.value = ''
  try {
    const user = await userStore.login(loginForm.username, loginForm.password)
    // 根据角色跳转到不同页面
    if (user.role === 'MANAGER') {
      router.push('/manager')
    } else if (user.role === 'BARISTA') {
      router.push('/barista')
    } else {
      router.push('/customer')
    }
  } catch (e) {
    error.value = e.message || '登录失败'
  }
}

const handleRegister = async () => {
  error.value = ''
  success.value = ''
  try {
    await userStore.register({
      ...registerForm,
      role: 'CUSTOMER'
    })
    success.value = '注册成功！请登录'
    activeTab.value = 'login'
    loginForm.username = registerForm.username
    loginForm.password = registerForm.password
  } catch (e) {
    error.value = e.message || '注册失败'
  }
}

const fillDemo = (username, password) => {
  loginForm.username = username
  loginForm.password = password
  activeTab.value = 'login'
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--pixel-cream) 0%, var(--pixel-light-cream) 100%);
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-icon {
  font-size: 64px;
  animation: bounce 1s infinite;
}

.login-header h1 {
  font-family: var(--font-pixel);
  font-size: 16px;
  color: var(--pixel-brown);
  text-shadow: 2px 2px 0 var(--pixel-shadow);
  margin: 16px 0 8px;
}

.login-header p {
  font-family: var(--font-pixel);
  font-size: 10px;
  color: var(--pixel-gray);
}

.login-card {
  padding: 0;
}

.login-card .pixel-tabs {
  margin-bottom: 0;
}

.login-card .pixel-tab-content {
  border-top: none;
}

.demo-accounts {
  margin-top: 24px;
  text-align: center;
  color: var(--pixel-gray);
  font-size: 12px;
}

.demo-accounts p {
  margin-bottom: 8px;
}

.demo-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.demo-item {
  padding: 4px 8px;
  background: var(--pixel-white);
  border: 2px solid var(--pixel-brown);
  cursor: pointer;
  transition: all 0.1s;
}

.demo-item:hover {
  background: var(--pixel-brown);
  color: var(--pixel-white);
}

.game-intro {
  margin-top: 24px;
  background: var(--pixel-white);
  border: 4px solid var(--pixel-black);
  box-shadow: 4px 4px 0 var(--pixel-shadow);
  padding: 16px;
  font-size: 13px;
}

.game-intro h3 {
  margin-bottom: 12px;
  color: var(--pixel-brown);
}

.game-intro ul {
  margin: 8px 0;
  padding-left: 20px;
}

.game-intro li {
  margin: 8px 0;
}
</style>

<template>
  <div class="app pixel-container">
    <header class="pixel-header" v-if="userStore.isLoggedIn">
      <div class="logo" @click="goHome">
        <span class="coffee-icon">☕</span>
        <h1>像素咖啡店</h1>
      </div>
      <nav class="pixel-nav">
        <span class="username">{{ userStore.user?.nickname }}</span>
        <button class="pixel-btn small" @click="logout">退出</button>
      </nav>
    </header>
    <main class="pixel-main" :class="{ 'no-header': !userStore.isLoggedIn }">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from './stores/user'

const router = useRouter()
const userStore = useUserStore()

const goHome = () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  router.push('/game')
}

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.pixel-main.no-header {
  padding: 0;
}
</style>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="container topbar-inner">
        <RouterLink class="brand" to="/">优选商城</RouterLink>
        <nav class="nav-links">
          <RouterLink to="/">首页</RouterLink>
          <RouterLink to="/search">搜索</RouterLink>
          <RouterLink to="/cart">购物车({{ cartCount }})</RouterLink>
          <RouterLink to="/checkout">下单</RouterLink>
          <RouterLink v-if="auth.isLoggedIn" to="/profile">个人中心</RouterLink>
          <RouterLink v-if="auth.isAdmin" to="/admin">管理员</RouterLink>
        </nav>
        <div class="auth-zone">
          <template v-if="auth.isLoggedIn">
            <span class="muted">{{ auth.user?.username }} ({{ auth.user?.role === 'admin' ? '管理员' : '买家' }})</span>
            <RouterLink class="btn ghost" to="/login">切换账号</RouterLink>
            <button class="btn ghost" @click="logout">退出</button>
          </template>
          <template v-else>
            <RouterLink class="btn ghost" to="/login">登录</RouterLink>
            <RouterLink class="btn primary" to="/register">注册</RouterLink>
          </template>
        </div>
      </div>
    </header>

    <main class="container page-wrap">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useCartStore } from './stores/cart'
import { apiGetCarts, apiGetMe } from './api/modules'

const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const cartCount = computed(() => cart.totalCount)

async function syncCartCount() {
  if (!auth.isLoggedIn) {
    cart.clearAll()
    return
  }
  try {
    const carts = await apiGetCarts()
    cart.setItems(carts)
  } catch (_) {
    // Keep UI stable if cart API is temporarily unavailable.
  }
}

async function syncUser() {
  if (!auth.isLoggedIn) return
  try {
    const me = await apiGetMe()
    auth.setUserProfile(me)
  } catch (_) {
    // Keep UI stable when profile endpoint is temporarily unavailable.
  }
}

function logout() {
  auth.logout()
  cart.clearAll()
  router.push('/login')
}

onMounted(() => {
  syncCartCount()
  syncUser()
})

watch(
  () => auth.token,
  () => {
    syncCartCount()
    syncUser()
  }
)
</script>

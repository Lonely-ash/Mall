<template>
  <section class="card" style="max-width:420px;margin:30px auto;">
    <h2>登录</h2>
    <p class="muted">使用后端 `/users/login` 接口</p>
    <div class="form-grid">
      <input class="input" v-model.trim="form.username" placeholder="用户名" />
      <input class="input" v-model="form.password" type="password" placeholder="密码" />
      <label class="flex muted"><input type="checkbox" v-model="form.rememberMe" /> 记住我</label>
      <button class="btn primary" :disabled="loading" @click="submit">{{ loading ? '登录中...' : '登录' }}</button>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiLogin } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const error = ref('')
const form = reactive({ username: '', password: '', rememberMe: false })

async function submit() {
  error.value = ''
  if (!form.username || !form.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  try {
    const data = await apiLogin(form)
    auth.setLogin(data)
    router.push(route.query.redirect || '/')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

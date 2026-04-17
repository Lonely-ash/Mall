<template>
  <section class="card" style="max-width:460px;margin:30px auto;">
    <h2>注册</h2>
    <p class="muted">注册成功后请返回登录页进行登录。</p>
    <div class="form-grid">
      <input class="input" v-model.trim="form.username" placeholder="用户名" />
      <input class="input" v-model="form.password" type="password" placeholder="密码" />
      <input class="input" v-model="form.confirmPassword" type="password" placeholder="确认密码" />
      <button class="btn primary" :disabled="loading" @click="submit">{{ loading ? '注册中...' : '注册' }}</button>
      <p v-if="msg" :class="ok ? 'muted' : 'error'">{{ msg }}</p>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { apiRegister } from '../api/modules'

const loading = ref(false)
const msg = ref('')
const ok = ref(false)
const form = reactive({ username: '', password: '', confirmPassword: '' })

async function submit() {
  msg.value = ''
  ok.value = false
  if (!form.username || !form.password) {
    msg.value = '请填写用户名和密码'
    return
  }
  if (form.password !== form.confirmPassword) {
    msg.value = '两次密码不一致'
    return
  }
  loading.value = true
  try {
    await apiRegister({ username: form.username, password: form.password })
    ok.value = true
    msg.value = '注册成功，请返回登录'
  } catch (e) {
    msg.value = `注册失败：${e.message}`
  } finally {
    loading.value = false
  }
}
</script>

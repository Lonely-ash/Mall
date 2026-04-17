<template>
  <section class="card detail-shell">
    <div class="detail-top">
      <button class="btn ghost" @click="back">返回</button>
      <span class="muted">商品详情</span>
    </div>

    <p v-if="error" class="error">{{ error }}</p>

    <div v-if="loading" class="muted">加载中...</div>
    <div v-else-if="item" class="detail-grid">
      <div class="detail-image-wrap">
        <img class="detail-image" :src="item.image || placeholder" :alt="item.name" />
      </div>
      <div class="detail-content">
        <h1 class="detail-title">{{ item.name }}</h1>
        <div class="detail-price">{{ fenToYuan(item.price || 0) }}</div>
        <div class="detail-meta">
          <span>品牌：{{ item.brand || '-' }}</span>
          <span>分类：{{ item.category || '-' }}</span>
          <span>规格：{{ item.spec || '-' }}</span>
          <span>库存：{{ item.stock ?? '-' }}</span>
        </div>

        <div class="detail-actions">
          <button class="btn ghost" @click="back">返回列表</button>
          <button class="btn primary" @click="addToCart(item)">加入购物车</button>
          <button class="btn accent" @click="buyNow(item)">立即购买</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiAddCart, apiGetCarts, apiGetItemById } from '../api/modules'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import { fenToYuan } from '../utils/format'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cartStore = useCartStore()
const loading = ref(false)
const error = ref('')
const item = ref(null)
const placeholder = 'https://dummyimage.com/900x900/e5e7eb/6b7280&text=No+Image'

async function load() {
  loading.value = true
  error.value = ''
  try {
    item.value = await apiGetItemById(route.params.id)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function back() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/')
}

async function addToCart(data) {
  if (!auth.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    await apiAddCart({
      itemId: data.id,
      name: data.name,
      spec: data.spec,
      price: data.price,
      image: data.image
    })
    const carts = await apiGetCarts()
    cartStore.setItems(carts)
    alert('已加入购物车')
  } catch (e) {
    alert(`加入失败：${e.message}`)
  }
}

async function buyNow(data) {
  await addToCart(data)
  if (auth.isLoggedIn) {
    router.push('/checkout')
  }
}

onMounted(load)
</script>

<style scoped>
.detail-shell {
  background: linear-gradient(180deg, #ffffff, #f8fbff);
}

.detail-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(280px, 420px) 1fr;
  gap: 22px;
  align-items: start;
}

.detail-image-wrap {
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #d7e2f0;
  background: #fff;
  box-shadow: 0 10px 26px rgba(15, 50, 95, 0.12);
}

.detail-image {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
}

.detail-title {
  margin: 0 0 10px;
  font-size: 32px;
  line-height: 1.25;
}

.detail-price {
  font-size: 38px;
  font-weight: 800;
  color: #0a6fb3;
  margin-bottom: 14px;
}

.detail-meta {
  display: grid;
  gap: 8px;
  color: #334155;
  margin-bottom: 20px;
}

.detail-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
  .detail-title {
    font-size: 26px;
  }
  .detail-price {
    font-size: 30px;
  }
}
</style>

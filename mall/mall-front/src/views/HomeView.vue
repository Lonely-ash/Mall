<template>
  <div>
    <section class="card" style="margin-bottom: 16px;">
      <div class="space-between" style="align-items: flex-start;">
        <div>
          <h2 style="margin:0 0 8px;">首页入口</h2>
        </div>
        <div class="flex" style="flex-wrap: wrap; justify-content: flex-end;">
          <RouterLink v-if="auth.isLoggedIn" class="btn ghost" to="/profile">个人中心</RouterLink>
        </div>
      </div>
      <div class="flex" style="margin-top: 12px; flex-wrap: wrap;">
      </div>
    </section>

    <section class="card">
      <div class="space-between" style="margin-bottom:14px;">
        <h2 style="margin:0;">首页商品推荐</h2>
        <div class="flex">
          <button class="btn ghost" @click="load">刷新</button>
        </div>
      </div>

      <p v-if="error" class="error">{{ error }}</p>
      <p class="muted">共 {{ total }} 条，当前第 {{ pageNo }} / {{ pages || 1 }} 页</p>
      <div v-if="loading" class="muted">加载中...</div>

      <div class="row" v-else>
        <article class="product" v-for="item in items" :key="item.id">
          <img :src="item.image || placeholder" :alt="item.name" class="product-clickable" @click="goDetail(item.id)" />
          <div class="product-content">
            <div class="product-title product-clickable" @click="goDetail(item.id)">{{ item.name }}</div>
            <div class="product-meta">{{ item.brand }} / {{ item.category }}</div>
            <div class="price">{{ fenToYuan(item.price) }}</div>
            <div class="product-actions">
              <button class="btn ghost" @click="goDetail(item.id)">查看详情</button>
              <button class="btn primary btn-cart" @click="addToCart(item)">加入购物车</button>
              <button class="btn accent btn-buy" @click="buyNow(item)">立即下单</button>
            </div>
          </div>
        </article>
      </div>
    </section>

    <div class="pagination-wrap" v-if="pages > 0">
      <div class="pagination">
        <button class="page-btn" :disabled="pageNo <= 1" @click="goPage(1)">首页</button>
        <button class="page-btn" :disabled="pageNo <= 1" @click="goPage(pageNo - 1)">上一页</button>

        <button
          v-for="p in visiblePages"
          :key="p"
          class="page-btn"
          :class="{ active: p === pageNo }"
          @click="goPage(p)"
        >
          {{ p }}
        </button>

        <button class="page-btn" :disabled="pageNo >= pages" @click="goPage(pageNo + 1)">下一页</button>
        <button class="page-btn" :disabled="pageNo >= pages" @click="goPage(pages)">末页</button>
        <span class="muted">跳转到</span>
        <input class="page-input" type="number" min="1" :max="pages" v-model.number="jumpPage" />
        <button class="page-btn go" @click="goJumpPage">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { apiAddCart, apiGetItemsByPage, apiGetCarts } from '../api/modules'
import { fenToYuan } from '../utils/format'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'

const auth = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()
const loading = ref(false)
const error = ref('')
const items = ref([])
const total = ref(0)
const pages = ref(0)
const pageNo = ref(1)
const pageSize = ref(16)
const jumpPage = ref(1)
const placeholder = 'https://dummyimage.com/600x600/e5e7eb/6b7280&text=No+Image'

const visiblePages = computed(() => {
  const totalPages = pages.value || 1
  const cur = pageNo.value
  const start = Math.max(1, cur - 2)
  const end = Math.min(totalPages, start + 4)
  const fixedStart = Math.max(1, end - 4)
  return Array.from({ length: end - fixedStart + 1 }, (_, i) => fixedStart + i)
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await apiGetItemsByPage({ pageNo: pageNo.value, pageSize: pageSize.value })
    items.value = res.list || []
    total.value = res.total || 0
    pages.value = res.pages || 0
    if (pageNo.value > (pages.value || 1)) {
      pageNo.value = pages.value || 1
    }
    jumpPage.value = pageNo.value
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function addToCart(item) {
  if (!auth.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    await apiAddCart({
      itemId: item.id,
      name: item.name,
      spec: item.spec,
      price: item.price,
      image: item.image
    })
    const carts = await apiGetCarts()
    cartStore.setItems(carts)
    alert('已加入购物车')
  } catch (e) {
    alert(`加入失败：${e.message}`)
  }
}

async function buyNow(item) {
  await addToCart(item)
  if (auth.isLoggedIn) {
    router.push('/checkout')
  }
}

function goDetail(id) {
  router.push(`/items/${id}`)
}

function goPage(target) {
  const max = pages.value || 1
  if (target < 1 || target > max || target === pageNo.value) return
  pageNo.value = target
  load()
}

function goJumpPage() {
  const max = pages.value || 1
  const target = Number(jumpPage.value)
  if (!target || target < 1 || target > max) return
  goPage(target)
}

onMounted(load)
</script>

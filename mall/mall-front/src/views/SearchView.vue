<template>
  <div>
    <section class="card">
      <h2>搜索商品</h2>
      <div class="form-grid" style="grid-template-columns:2fr 1fr 1fr auto;align-items:end;">
        <input class="input" v-model.trim="query.key" placeholder="关键词（商品名称）" />
        <input class="input" v-model.trim="query.brand" placeholder="品牌" />
        <input class="input" v-model.trim="query.category" placeholder="分类" />
        <button class="btn primary btn-search" @click="search(true)">搜索</button>
      </div>

      <p v-if="error" class="error">{{ error }}</p>
      <p class="muted">共 {{ total }} 条，当前第 {{ query.pageNo }} / {{ pages || 1 }} 页</p>

      <div class="row">
        <article class="product" v-for="item in list" :key="item.id">
          <img :src="item.image || placeholder" :alt="item.name" class="product-clickable" @click="goDetail(item.id)" />
          <div class="product-content">
            <div class="product-title product-clickable" @click="goDetail(item.id)">{{ item.name }}</div>
            <div class="product-meta">{{ item.brand }} / {{ item.category }}</div>
            <div class="price">{{ fenToYuan(item.price) }}</div>
            <div class="product-actions">
              <button class="btn ghost" @click="goDetail(item.id)">查看详情</button>
              <button class="btn primary btn-cart" @click="addToCart(item)">加入购物车</button>
              <button class="btn accent btn-buy" @click="buyNow(item)">立即购买</button>
            </div>
          </div>
        </article>
      </div>
    </section>

    <div class="pagination-wrap" v-if="pages > 0">
      <div class="pagination">
        <button class="page-btn" :disabled="query.pageNo <= 1" @click="goPage(1)">首页</button>
        <button class="page-btn" :disabled="query.pageNo <= 1" @click="goPage(query.pageNo - 1)">上一页</button>

        <button
          v-for="p in visiblePages"
          :key="p"
          class="page-btn"
          :class="{ active: p === query.pageNo }"
          @click="goPage(p)"
        >
          {{ p }}
        </button>

        <button class="page-btn" :disabled="query.pageNo >= pages" @click="goPage(query.pageNo + 1)">下一页</button>
        <button class="page-btn" :disabled="query.pageNo >= pages" @click="goPage(pages)">末页</button>
        <span class="muted">跳转到</span>
        <input class="page-input" type="number" min="1" :max="pages" v-model.number="jumpPage" />
        <button class="page-btn go" @click="goJumpPage">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiAddCart, apiGetCarts, apiSearchItems } from '../api/modules'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import { fenToYuan } from '../utils/format'

const router = useRouter()
const auth = useAuthStore()
const cartStore = useCartStore()
const placeholder = 'https://dummyimage.com/600x600/e5e7eb/6b7280&text=No+Image'
const query = reactive({ key: '', brand: '', category: '', pageNo: 1, pageSize: 20 })
const list = ref([])
const total = ref(0)
const pages = ref(0)
const jumpPage = ref(1)
const error = ref('')

const visiblePages = computed(() => {
  const totalPages = pages.value || 1
  const cur = query.pageNo
  const start = Math.max(1, cur - 2)
  const end = Math.min(totalPages, start + 4)
  const fixedStart = Math.max(1, end - 4)
  return Array.from({ length: end - fixedStart + 1 }, (_, i) => fixedStart + i)
})

async function search(resetPage = false) {
  if (resetPage) {
    query.pageNo = 1
  }
  error.value = ''
  try {
    const data = await apiSearchItems(query)
    list.value = data.list || []
    total.value = data.total || 0
    pages.value = data.pages || 0
    if (query.pageNo > (pages.value || 1)) {
      query.pageNo = pages.value || 1
    }
    jumpPage.value = query.pageNo
  } catch (e) {
    error.value = e.message
  }
}

async function addToCart(item) {
  if (!auth.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    await apiAddCart({ itemId: item.id, name: item.name, spec: item.spec, price: item.price, image: item.image })
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
  if (target < 1 || target > max || target === query.pageNo) return
  query.pageNo = target
  search(false)
}

function goJumpPage() {
  const max = pages.value || 1
  const target = Number(jumpPage.value)
  if (!target || target < 1 || target > max) return
  goPage(target)
}

search()
</script>

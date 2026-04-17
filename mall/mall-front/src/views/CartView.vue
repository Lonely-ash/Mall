<template>
  <section class="card">
    <div class="space-between">
      <h2 style="margin:0;">购物车</h2>
      <button class="btn ghost" @click="load">刷新</button>
    </div>

    <p v-if="error" class="error">{{ error }}</p>

    <table class="table" v-if="items.length">
      <thead>
        <tr>
          <th>商品</th>
          <th>单价</th>
          <th>数量</th>
          <th>小计</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in items" :key="item.id">
          <td>
            <div class="cart-item-info">
              <img class="cart-item-image" :src="item.image || placeholder" :alt="item.name" />
              <div>
                <div class="cart-item-title">{{ item.name }}</div>
                <div class="muted" v-if="item.spec">规格：{{ item.spec }}</div>
              </div>
            </div>
          </td>
          <td>{{ fenToYuan(item.newPrice || item.price) }}</td>
          <td>
            <input class="input" type="number" min="1" v-model.number="item.num" @change="updateNum(item)" style="max-width:84px;" />
          </td>
          <td>{{ fenToYuan((item.newPrice || item.price) * item.num) }}</td>
          <td><button class="btn danger" @click="remove(item.id)">删除</button></td>
        </tr>
      </tbody>
    </table>

    <div v-else class="muted" style="padding:20px 0;">购物车为空</div>

    <div class="space-between" style="margin-top:14px;">
      <strong>合计：{{ fenToYuan(totalPrice) }}</strong>
      <RouterLink class="btn primary" to="/checkout">去购买</RouterLink>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { apiDeleteCart, apiGetCarts, apiUpdateCart } from '../api/modules'
import { useCartStore } from '../stores/cart'
import { fenToYuan } from '../utils/format'

const cartStore = useCartStore()
const items = ref([])
const error = ref('')
const placeholder = 'https://dummyimage.com/300x300/e5e7eb/6b7280&text=No+Image'

const totalPrice = computed(() => {
  return items.value.reduce((sum, item) => sum + (item.newPrice || item.price) * item.num, 0)
})

async function load() {
  error.value = ''
  try {
    const carts = await apiGetCarts()
    items.value = carts || []
    cartStore.setItems(items.value)
  } catch (e) {
    error.value = e.message
  }
}

async function updateNum(item) {
  if (!item.num || item.num < 1) item.num = 1
  try {
    await apiUpdateCart({ id: item.id, num: item.num })
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function remove(id) {
  try {
    await apiDeleteCart(id)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(load)
</script>

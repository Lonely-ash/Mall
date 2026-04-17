<template>
  <section class="card">
    <h2>订单结算</h2>
    <p class="muted">流程：选择地址 -> 创建订单 -> 余额支付。</p>

    <p v-if="error" class="error">{{ error }}</p>

    <div class="space-between" style="margin:10px 0;">
      <div>
        <strong>购物车商品数：{{ items.length }}</strong>
      </div>
      <button class="btn ghost" @click="load">刷新数据</button>
    </div>

    <div class="form-grid" style="grid-template-columns: 1fr 260px;">
      <div>
        <label class="muted">收货地址</label>
        <select v-model="addressId">
          <option disabled value="">请选择地址</option>
          <option v-for="addr in addresses" :key="addr.id" :value="addr.id">
            {{ addr.contact }} {{ addr.mobile }} {{ addr.province }}{{ addr.city }}{{ addr.town }}{{ addr.street }}
          </option>
        </select>
      </div>
      <div>
        <label class="muted">支付密码</label>
        <input class="input" v-model="payPassword" type="password" placeholder="请输入支付密码" />
      </div>
    </div>

    <table class="table" style="margin-top:14px;" v-if="items.length">
      <thead>
        <tr>
          <th>商品</th>
          <th>数量</th>
          <th>单价</th>
          <th>小计</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in items" :key="item.id">
          <td>{{ item.name }}</td>
          <td>{{ item.num }}</td>
          <td>{{ fenToYuan(item.newPrice || item.price) }}</td>
          <td>{{ fenToYuan((item.newPrice || item.price) * item.num) }}</td>
        </tr>
      </tbody>
    </table>

    <div class="space-between" style="margin-top:14px;">
      <strong>需支付：{{ fenToYuan(totalPrice) }}</strong>
      <button class="btn primary" :disabled="loading || !items.length" @click="submitOrder">
        {{ loading ? '处理中...' : '创建并支付订单' }}
      </button>
    </div>

    <p v-if="success" class="muted" style="margin-top:12px;">{{ success }}</p>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  apiApplyPayOrder,
  apiCreateOrder,
  apiGetAddresses,
  apiGetCarts,
  apiGetOrder,
  apiGetPayOrderByBizOrderNo,
  apiPayByBalance
} from '../api/modules'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import { fenToYuan } from '../utils/format'

const cartStore = useCartStore()
const auth = useAuthStore()
const router = useRouter()

const loading = ref(false)
const error = ref('')
const success = ref('')

const items = ref([])
const addresses = ref([])
const addressId = ref('')
const payPassword = ref('')

const totalPrice = computed(() => items.value.reduce((sum, i) => sum + (i.newPrice || i.price) * i.num, 0))

async function load() {
  error.value = ''
  try {
    const [carts, addrs] = await Promise.all([apiGetCarts(), apiGetAddresses()])
    items.value = carts || []
    addresses.value = addrs || []
    const defaultAddr = addresses.value.find((a) => a.isDefault === 1) || addresses.value[0]
    addressId.value = defaultAddr ? defaultAddr.id : ''
    cartStore.setItems(items.value)
  } catch (e) {
    error.value = e.message
  }
}

async function submitOrder() {
  success.value = ''
  error.value = ''
  if (!addressId.value) {
    error.value = '请先选择收货地址'
    return
  }
  if (!payPassword.value) {
    error.value = '请输入支付密码'
    return
  }

  loading.value = true
  try {
    const details = items.value.map((i) => ({ itemId: i.itemId || i.id, num: i.num }))
    const orderId = await apiCreateOrder({
      addressId: Number(addressId.value),
      paymentType: 3,
      details
    })

    const order = await apiGetOrder(orderId)
    await apiApplyPayOrder({
      bizOrderNo: orderId,
      amount: order.totalFee,
      payChannelCode: 'balance',
      payType: 5,
      orderInfo: `order-${orderId}`
    })
    const payOrder = await apiGetPayOrderByBizOrderNo(orderId)
    const payOrderId = payOrder?.id
    if (!payOrderId) {
      throw new Error('支付单不存在')
    }
    await apiPayByBalance(payOrderId, { pw: payPassword.value })

    auth.updateBalance((auth.user?.balance || 0) - (order.totalFee || 0))
    success.value = `支付成功，订单号：${orderId}`
    alert(`支付成功，订单号：${orderId}`)
    payPassword.value = ''
    await load()
    router.push('/profile')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="form-grid" style="gap: 16px;">
    <section class="card">
      <div class="space-between">
        <h2 style="margin:0;">管理员总览</h2>
        <button class="btn ghost" @click="loadAll">刷新</button>
      </div>
      <div class="row" style="margin-top: 12px;">
        <article class="card" style="padding: 14px;">
          <div class="muted">用户数</div>
          <div style="font-size: 26px; font-weight: 700;">{{ stats.userTotal }}</div>
        </article>
        <article class="card" style="padding: 14px;">
          <div class="muted">商品数</div>
          <div style="font-size: 26px; font-weight: 700;">{{ stats.itemTotal }}</div>
        </article>
        <article class="card" style="padding: 14px;">
          <div class="muted">订单数</div>
          <div style="font-size: 26px; font-weight: 700;">{{ stats.orderTotal }}</div>
        </article>
        <article class="card" style="padding: 14px;">
          <div class="muted">待处理订单</div>
          <div style="font-size: 26px; font-weight: 700;">{{ stats.pendingOrders }}</div>
        </article>
      </div>
    </section>

    <section class="card">
      <div class="space-between">
        <h2 style="margin:0;">用户权限管理</h2>
      </div>
      <p v-if="userErr" class="error">{{ userErr }}</p>
      <table class="table" style="margin-top: 10px;" v-if="users.length">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>手机号</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.phone || '-' }}</td>
            <td>{{ u.status === 1 ? '正常' : '冻结' }}</td>
            <td>
              <button class="btn ghost" :disabled="u.username === 'admin'" @click="toggleUserStatus(u)">
                {{ u.status === 1 ? '冻结' : '解冻' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="card">
      <div class="space-between">
        <h2 style="margin:0;">商品库维护</h2>
      </div>
      <p v-if="itemErr" class="error">{{ itemErr }}</p>
      <div class="form-grid" style="grid-template-columns: 2fr 1fr 1fr auto; margin-top: 10px;">
        <input class="input" v-model.trim="itemQuery.key" placeholder="按名称搜索" />
        <select v-model="itemQuery.status">
          <option :value="''">全部状态</option>
          <option :value="1">上架</option>
          <option :value="2">下架</option>
          <option :value="3">删除</option>
        </select>
        <select v-model="itemForm.category">
          <option value="">分类(可选)</option>
          <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
        </select>
        <button class="btn ghost" @click="loadItems">查询</button>
      </div>

      <table class="table" style="margin-top: 10px;" v-if="items.length">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>分类</th>
            <th>价格</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="i in items" :key="i.id">
            <td>{{ i.id }}</td>
            <td>{{ i.name }}</td>
            <td>{{ i.category }}</td>
            <td>{{ fenToYuan(i.price || 0) }}</td>
            <td>{{ itemStatusText(i.status) }}</td>
            <td class="flex" style="flex-wrap: wrap;">
              <button class="btn ghost" @click="editItem(i)">编辑</button>
              <button class="btn ghost" @click="switchItemStatus(i, i.status === 1 ? 2 : 1)">{{ i.status === 1 ? '下架' : '上架' }}</button>
              <button class="btn danger" @click="removeItem(i.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="form-grid" style="grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); margin-top: 12px;">
        <input class="input" v-model.trim="itemForm.name" placeholder="商品名称" />
        <input class="input" v-model.trim="itemForm.brand" placeholder="品牌" />
        <input class="input" v-model.trim="itemForm.category" placeholder="分类" />
        <input class="input" v-model.number="itemForm.price" type="number" min="1" placeholder="价格(分)" />
        <input class="input" v-model.number="itemForm.stock" type="number" min="0" placeholder="库存" />
        <input class="input" v-model.trim="itemForm.image" placeholder="图片URL" />
      </div>
      <div class="flex" style="margin-top: 10px;">
        <button class="btn primary" @click="saveItem">{{ itemForm.id ? '更新商品' : '新增商品' }}</button>
        <button class="btn ghost" v-if="itemForm.id" @click="resetItemForm">取消编辑</button>
      </div>
    </section>

    <section class="card">
      <h2 style="margin:0;">订单监控</h2>
      <p v-if="orderErr" class="error">{{ orderErr }}</p>
      <table class="table" style="margin-top: 10px;" v-if="orders.length">
        <thead>
          <tr>
            <th>订单号</th>
            <th>用户ID</th>
            <th>金额</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in orders" :key="o.id">
            <td>{{ o.id }}</td>
            <td>{{ o.userId }}</td>
            <td>{{ fenToYuan(o.totalFee || 0) }}</td>
            <td>{{ orderStatusText(o.status) }}</td>
            <td class="flex" style="flex-wrap: wrap;">
              <button class="btn ghost" @click="setOrderStatus(o.id, 3)">标记发货</button>
              <button class="btn ghost" @click="setOrderStatus(o.id, 4)">标记完成</button>
              <button class="btn danger" @click="setOrderStatus(o.id, 5)">取消订单</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  apiCreateItem,
  apiDeleteItem,
  apiGetCategories,
  apiGetItemsByAdminPage,
  apiGetOrdersByPage,
  apiGetUsersByPage,
  apiUpdateItem,
  apiUpdateItemStatus,
  apiUpdateOrderStatus,
  apiUpdateUserStatus
} from '../api/modules'
import { fenToYuan } from '../utils/format'

const stats = reactive({
  userTotal: 0,
  itemTotal: 0,
  orderTotal: 0,
  pendingOrders: 0
})

const users = ref([])
const userErr = ref('')

const categories = ref([])
const items = ref([])
const itemErr = ref('')
const itemQuery = reactive({ pageNo: 1, pageSize: 30, key: '', status: '' })
const itemForm = reactive({
  id: null,
  name: '',
  brand: '',
  category: '',
  price: 100,
  stock: 0,
  image: '',
  spec: ''
})

const orders = ref([])
const orderErr = ref('')

function itemStatusText(status) {
  return ({ 1: '上架', 2: '下架', 3: '删除' })[status] || `状态${status}`
}

function orderStatusText(status) {
  return ({ 1: '待处理', 2: '已支付', 3: '已发货', 4: '已完成', 5: '已取消', 6: '已评价' })[status] || `状态${status}`
}

async function loadUsers() {
  userErr.value = ''
  try {
    const res = await apiGetUsersByPage({ pageNo: 1, pageSize: 30 })
    users.value = res.list || []
    stats.userTotal = res.total || 0
  } catch (e) {
    userErr.value = e.message
  }
}

async function toggleUserStatus(user) {
  userErr.value = ''
  try {
    await apiUpdateUserStatus(user.id, user.status === 1 ? 0 : 1)
    await loadUsers()
  } catch (e) {
    userErr.value = e.message
  }
}

async function loadItems() {
  itemErr.value = ''
  try {
    const res = await apiGetItemsByAdminPage({
      pageNo: itemQuery.pageNo,
      pageSize: itemQuery.pageSize,
      key: itemQuery.key || undefined,
      status: itemQuery.status === '' ? undefined : Number(itemQuery.status)
    })
    items.value = res.list || []
    stats.itemTotal = res.total || 0
  } catch (e) {
    itemErr.value = e.message
  }
}

function editItem(item) {
  itemForm.id = item.id
  itemForm.name = item.name || ''
  itemForm.brand = item.brand || ''
  itemForm.category = item.category || ''
  itemForm.price = item.price || 0
  itemForm.stock = item.stock || 0
  itemForm.image = item.image || ''
  itemForm.spec = item.spec || ''
}

function resetItemForm() {
  itemForm.id = null
  itemForm.name = ''
  itemForm.brand = ''
  itemForm.category = ''
  itemForm.price = 100
  itemForm.stock = 0
  itemForm.image = ''
  itemForm.spec = ''
}

async function saveItem() {
  itemErr.value = ''
  if (!itemForm.name || !itemForm.category || !itemForm.price) {
    itemErr.value = '请至少填写商品名称、分类和价格'
    return
  }
  const payload = {
    ...itemForm,
    isAD: false
  }
  try {
    if (itemForm.id) {
      await apiUpdateItem(payload)
    } else {
      await apiCreateItem(payload)
    }
    resetItemForm()
    await loadItems()
  } catch (e) {
    itemErr.value = e.message
  }
}

async function switchItemStatus(item, status) {
  itemErr.value = ''
  try {
    await apiUpdateItemStatus(item.id, status)
    await loadItems()
  } catch (e) {
    itemErr.value = e.message
  }
}

async function removeItem(id) {
  itemErr.value = ''
  try {
    await apiDeleteItem(id)
    await loadItems()
  } catch (e) {
    itemErr.value = e.message
  }
}

async function loadOrders() {
  orderErr.value = ''
  try {
    const res = await apiGetOrdersByPage({ pageNo: 1, pageSize: 30 })
    orders.value = res.list || []
    stats.orderTotal = res.total || 0
    stats.pendingOrders = (res.list || []).filter((o) => o.status === 1).length
  } catch (e) {
    orderErr.value = e.message
  }
}

async function setOrderStatus(id, status) {
  orderErr.value = ''
  try {
    await apiUpdateOrderStatus(id, status)
    await loadOrders()
  } catch (e) {
    orderErr.value = e.message
  }
}

async function loadAll() {
  try {
    categories.value = await apiGetCategories()
  } catch (_) {
    categories.value = []
  }
  await Promise.all([loadUsers(), loadItems(), loadOrders()])
}

onMounted(loadAll)
</script>

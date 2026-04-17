<template>
  <div class="form-grid" style="gap: 16px;">
    <section class="card">
      <div class="space-between">
        <h2 style="margin:0;">个人资料</h2>
        <button class="btn ghost" @click="loadProfile">刷新</button>
      </div>
      <p v-if="profileErr" class="error">{{ profileErr }}</p>
      <div class="form-grid" style="grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); margin-top: 12px;">
        <div>
          <label class="muted">用户名</label>
          <input class="input" :value="profile.username" disabled />
        </div>
        <div>
          <label class="muted">角色</label>
          <input class="input" :value="profile.role === 'admin' ? '管理员' : '买家'" disabled />
        </div>
        <div>
          <label class="muted">手机号</label>
          <input class="input" v-model.trim="profileForm.phone" placeholder="手机号" />
        </div>
        <div>
          <label class="muted">旧密码（可选）</label>
          <input class="input" type="password" v-model="profileForm.oldPassword" />
        </div>
        <div>
          <label class="muted">新密码（可选）</label>
          <input class="input" type="password" v-model="profileForm.newPassword" />
        </div>
      </div>
      <div style="margin-top: 10px;">
        <button class="btn primary" :disabled="profileLoading" @click="saveProfile">{{ profileLoading ? '保存中...' : '保存资料' }}</button>
      </div>
    </section>

    <section class="card">
      <div class="space-between">
        <h2 style="margin:0;">收货地址</h2>
        <button class="btn ghost" @click="loadAddresses">刷新</button>
      </div>
      <p v-if="addressErr" class="error">{{ addressErr }}</p>

      <table class="table" style="margin-top: 10px;" v-if="addresses.length">
        <thead>
          <tr>
            <th>联系人</th>
            <th>手机</th>
            <th>地址</th>
            <th>默认</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="addr in addresses" :key="addr.id">
            <td>{{ addr.contact }}</td>
            <td>{{ addr.mobile }}</td>
            <td>{{ addr.province }}{{ addr.city }}{{ addr.town }} {{ addr.street }}</td>
            <td>{{ addr.isDefault === 1 ? '是' : '否' }}</td>
            <td class="flex" style="flex-wrap: wrap;">
              <button class="btn ghost" @click="editAddress(addr)">编辑</button>
              <button class="btn ghost" v-if="addr.isDefault !== 1" @click="setDefault(addr.id)">设默认</button>
              <button class="btn danger" @click="removeAddress(addr.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="muted" style="margin-top:10px;">暂无地址</div>

      <div class="form-grid" style="grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); margin-top: 12px;">
        <input class="input" v-model.trim="addressForm.contact" placeholder="联系人" />
        <input class="input" v-model.trim="addressForm.mobile" placeholder="手机号" />
        <input class="input" v-model.trim="addressForm.province" placeholder="省" />
        <input class="input" v-model.trim="addressForm.city" placeholder="市" />
        <input class="input" v-model.trim="addressForm.town" placeholder="区/县" />
        <input class="input" v-model.trim="addressForm.street" placeholder="详细地址" />
      </div>
      <div class="flex" style="margin-top: 10px;">
        <label class="muted"><input type="checkbox" v-model="addressDefaultChecked" /> 设为默认地址</label>
        <button class="btn primary" @click="saveAddress">{{ addressForm.id ? '更新地址' : '新增地址' }}</button>
        <button class="btn ghost" v-if="addressForm.id" @click="resetAddressForm">取消编辑</button>
      </div>
    </section>

    <section class="card">
      <div class="space-between">
        <h2 style="margin:0;">我的订单</h2>
        <button class="btn ghost" @click="loadOrders">刷新</button>
      </div>
      <p v-if="orderErr" class="error">{{ orderErr }}</p>
      <table class="table" style="margin-top: 10px;" v-if="orders.length">
        <thead>
          <tr>
            <th>订单号</th>
            <th>金额</th>
            <th>状态</th>
            <th>创建时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in orders" :key="order.id">
            <td>{{ order.id }}</td>
            <td>{{ fenToYuan(order.totalFee || 0) }}</td>
            <td>{{ statusText(order.status) }}</td>
            <td>{{ order.createTime || '-' }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="muted" style="margin-top:10px;">暂无订单</div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  apiCreateAddress,
  apiDeleteAddress,
  apiGetAddresses,
  apiGetMe,
  apiGetMyOrders,
  apiSetDefaultAddress,
  apiUpdateAddress,
  apiUpdateMe
} from '../api/modules'
import { useAuthStore } from '../stores/auth'
import { fenToYuan } from '../utils/format'

const auth = useAuthStore()

const profile = ref({ username: '', role: 'buyer' })
const profileForm = reactive({ phone: '', oldPassword: '', newPassword: '' })
const profileErr = ref('')
const profileLoading = ref(false)

const addresses = ref([])
const addressErr = ref('')
const addressForm = reactive({
  id: null,
  contact: '',
  mobile: '',
  province: '',
  city: '',
  town: '',
  street: ''
})
const addressDefaultChecked = ref(false)

const orders = ref([])
const orderErr = ref('')

function statusText(status) {
  return ({
    1: '待支付/待处理',
    2: '已支付',
    3: '已发货',
    4: '已完成',
    5: '已取消',
    6: '已评价'
  })[status] || `状态${status}`
}

async function loadProfile() {
  profileErr.value = ''
  try {
    const me = await apiGetMe()
    profile.value = me || {}
    profileForm.phone = me?.phone || ''
    auth.setUserProfile(me)
  } catch (e) {
    profileErr.value = e.message
  }
}

async function saveProfile() {
  profileLoading.value = true
  profileErr.value = ''
  try {
    await apiUpdateMe({
      phone: profileForm.phone || null,
      oldPassword: profileForm.oldPassword || null,
      newPassword: profileForm.newPassword || null
    })
    profileForm.oldPassword = ''
    profileForm.newPassword = ''
    await loadProfile()
  } catch (e) {
    profileErr.value = e.message
  } finally {
    profileLoading.value = false
  }
}

async function loadAddresses() {
  addressErr.value = ''
  try {
    addresses.value = await apiGetAddresses()
  } catch (e) {
    addressErr.value = e.message
  }
}

function editAddress(addr) {
  addressForm.id = addr.id
  addressForm.contact = addr.contact || ''
  addressForm.mobile = addr.mobile || ''
  addressForm.province = addr.province || ''
  addressForm.city = addr.city || ''
  addressForm.town = addr.town || ''
  addressForm.street = addr.street || ''
  addressDefaultChecked.value = addr.isDefault === 1
}

function resetAddressForm() {
  addressForm.id = null
  addressForm.contact = ''
  addressForm.mobile = ''
  addressForm.province = ''
  addressForm.city = ''
  addressForm.town = ''
  addressForm.street = ''
  addressDefaultChecked.value = false
}

async function saveAddress() {
  addressErr.value = ''
  const payload = {
    contact: addressForm.contact,
    mobile: addressForm.mobile,
    province: addressForm.province,
    city: addressForm.city,
    town: addressForm.town,
    street: addressForm.street,
    isDefault: addressDefaultChecked.value ? 1 : 0
  }
  if (!payload.contact || !payload.mobile || !payload.street) {
    addressErr.value = '请补全联系人、手机号和详细地址'
    return
  }
  try {
    if (addressForm.id) {
      await apiUpdateAddress(addressForm.id, payload)
    } else {
      await apiCreateAddress(payload)
    }
    resetAddressForm()
    await loadAddresses()
  } catch (e) {
    addressErr.value = e.message
  }
}

async function removeAddress(id) {
  addressErr.value = ''
  try {
    await apiDeleteAddress(id)
    await loadAddresses()
  } catch (e) {
    addressErr.value = e.message
  }
}

async function setDefault(id) {
  addressErr.value = ''
  try {
    await apiSetDefaultAddress(id)
    await loadAddresses()
  } catch (e) {
    addressErr.value = e.message
  }
}

async function loadOrders() {
  orderErr.value = ''
  try {
    const res = await apiGetMyOrders({ pageNo: 1, pageSize: 30 })
    orders.value = res.list || []
  } catch (e) {
    orderErr.value = e.message
  }
}

onMounted(async () => {
  await Promise.all([loadProfile(), loadAddresses(), loadOrders()])
})
</script>

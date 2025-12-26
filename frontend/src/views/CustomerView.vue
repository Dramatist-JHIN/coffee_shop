<template>
  <div class="customer-view">
    <div class="pixel-card">
      <div class="card-header">
        <h2>咖啡菜单</h2>
        <span class="cart-badge" v-if="cart.length">
          购物车: {{ cartCount }} 杯
        </span>
      </div>

      <div class="menu-grid">
        <div
          v-for="coffee in coffees"
          :key="coffee.id"
          class="menu-item pixel-card"
          :class="{ 'out-of-stock': coffee.stock < 1 }"
          @click="addToCart(coffee)"
        >
          <div class="item-icon">☕</div>
          <div class="item-info">
            <div class="item-name">{{ coffee.name }}</div>
            <div class="item-desc">{{ coffee.description }}</div>
            <div class="item-meta">
              <span class="item-price">¥{{ coffee.price }}</span>
              <span class="item-stock" :class="{ low: coffee.stock < 10 }">
                库存: {{ coffee.stock }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 购物车 -->
    <div class="cart-panel pixel-card" v-if="cart.length">
      <h3>购物车</h3>
      <div class="cart-items">
        <div v-for="item in cart" :key="item.coffeeId" class="cart-item">
          <span class="cart-item-name">{{ item.name }}</span>
          <div class="cart-item-qty">
            <button @click="decreaseQty(item)">-</button>
            <span>{{ item.quantity }}</span>
            <button @click="increaseQty(item)">+</button>
          </div>
          <span class="cart-item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
        </div>
      </div>
      <div class="cart-total">
        总计: ¥{{ cartTotal.toFixed(2) }}
      </div>
      <div class="pixel-form-group">
        <input
          v-model="remark"
          class="pixel-input"
          placeholder="备注（可选）"
        />
      </div>
      <div class="cart-actions">
        <button class="pixel-btn" @click="clearCart">清空</button>
        <button class="pixel-btn primary" @click="submitOrder">下单</button>
      </div>
    </div>

    <!-- 我的订单 -->
    <div class="pixel-card orders-panel">
      <h3>我的订单</h3>
      <div v-if="orders.length === 0" class="empty-hint">暂无订单</div>
      <div v-else class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-item pixel-card">
          <div class="order-header">
            <span class="order-no">#{{ order.orderNo.slice(-6) }}</span>
            <span class="order-status" :class="order.status.toLowerCase()">
              {{ statusMap[order.status] }}
            </span>
          </div>
          <div class="order-items">
            <span v-for="item in order.items" :key="item.id">
              {{ item.coffeeName }} x{{ item.quantity }}
            </span>
          </div>
          <div class="order-footer">
            <span class="order-time">{{ formatTime(order.createdAt) }}</span>
            <span class="order-amount">¥{{ order.totalAmount }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { coffeeApi, orderApi } from '../api'

const coffees = ref([])
const cart = ref([])
const orders = ref([])
const remark = ref('')

const statusMap = {
  PENDING: '待处理',
  MAKING: '制作中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

const cartCount = computed(() => {
  return cart.value.reduce((sum, item) => sum + item.quantity, 0)
})

const cartTotal = computed(() => {
  return cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

onMounted(async () => {
  await loadCoffees()
  await loadOrders()
})

const loadCoffees = async () => {
  try {
    const res = await coffeeApi.list()
    if (res.code === 200) {
      coffees.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const loadOrders = async () => {
  try {
    const res = await orderApi.myOrders()
    if (res.code === 200) {
      orders.value = res.data.records || []
    }
  } catch (e) {
    console.error(e)
  }
}

const addToCart = (coffee) => {
  if (coffee.stock < 1) {
    alert('该咖啡已售罄')
    return
  }

  const existing = cart.value.find(item => item.coffeeId === coffee.id)
  if (existing) {
    if (existing.quantity >= coffee.stock) {
      alert('库存不足')
      return
    }
    existing.quantity++
  } else {
    cart.value.push({
      coffeeId: coffee.id,
      name: coffee.name,
      price: coffee.price,
      quantity: 1,
      maxStock: coffee.stock
    })
  }
}

const increaseQty = (item) => {
  if (item.quantity < item.maxStock) {
    item.quantity++
  }
}

const decreaseQty = (item) => {
  if (item.quantity > 1) {
    item.quantity--
  } else {
    cart.value = cart.value.filter(i => i.coffeeId !== item.coffeeId)
  }
}

const clearCart = () => {
  cart.value = []
  remark.value = ''
}

const submitOrder = async () => {
  if (cart.value.length === 0) return

  try {
    const res = await orderApi.create({
      items: cart.value.map(item => ({
        coffeeId: item.coffeeId,
        quantity: item.quantity
      })),
      remark: remark.value
    })

    if (res.code === 200) {
      alert('下单成功!')
      clearCart()
      await loadCoffees()
      await loadOrders()
    } else {
      alert(res.message || '下单失败')
    }
  } catch (e) {
    alert(e.response?.data?.message || '下单失败')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.customer-view {
  padding: 16px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.cart-badge {
  background: var(--pixel-green);
  color: white;
  padding: 4px 12px;
  font-size: 12px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.menu-item {
  padding: 12px;
  cursor: pointer;
  transition: transform 0.1s;
  display: flex;
  gap: 12px;
}

.menu-item:hover {
  transform: translateY(-2px);
}

.menu-item.out-of-stock {
  opacity: 0.5;
  cursor: not-allowed;
}

.item-icon {
  font-size: 32px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-weight: bold;
  margin-bottom: 4px;
}

.item-desc {
  font-size: 12px;
  color: var(--pixel-gray);
  margin-bottom: 8px;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.item-price {
  color: var(--pixel-brown);
  font-weight: bold;
}

.item-stock {
  color: var(--pixel-green);
}

.item-stock.low {
  color: var(--pixel-red);
}

/* 购物车 */
.cart-panel {
  margin-top: 16px;
  padding: 16px;
}

.cart-items {
  margin: 12px 0;
}

.cart-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 2px dashed var(--pixel-cream);
}

.cart-item-qty {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cart-item-qty button {
  width: 24px;
  height: 24px;
  border: 2px solid var(--pixel-black);
  background: var(--pixel-white);
  cursor: pointer;
}

.cart-total {
  font-size: 18px;
  font-weight: bold;
  text-align: right;
  margin: 12px 0;
  color: var(--pixel-brown);
}

.cart-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* 订单列表 */
.orders-panel {
  margin-top: 16px;
}

.empty-hint {
  color: var(--pixel-gray);
  text-align: center;
  padding: 24px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  padding: 12px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.order-no {
  font-family: var(--font-pixel);
  font-size: 12px;
}

.order-status {
  font-size: 12px;
  padding: 2px 8px;
}

.order-status.pending {
  background: var(--pixel-yellow);
}

.order-status.making {
  background: var(--pixel-blue);
  color: white;
}

.order-status.completed {
  background: var(--pixel-green);
  color: white;
}

.order-status.cancelled {
  background: var(--pixel-gray);
  color: white;
}

.order-items {
  font-size: 14px;
  color: var(--pixel-gray);
  margin-bottom: 8px;
}

.order-items span {
  margin-right: 12px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.order-time {
  color: var(--pixel-gray);
}

.order-amount {
  font-weight: bold;
  color: var(--pixel-brown);
}
</style>

<template>
  <div class="barista-view">
    <div class="pixel-card">
      <h2>待处理订单</h2>
      <div v-if="pendingOrders.length === 0" class="empty-hint">
        暂无待处理订单
      </div>
      <div v-else class="order-queue">
        <div
          v-for="order in pendingOrders"
          :key="order.id"
          class="order-card pixel-card"
        >
          <div class="order-header">
            <span class="order-no">#{{ order.orderNo.slice(-6) }}</span>
            <span class="order-customer">{{ order.customerName || '顾客' }}</span>
          </div>
          <div class="order-items">
            <div v-for="item in order.items" :key="item.id" class="order-item-row">
              <span>☕ {{ item.coffeeName }}</span>
              <span>x{{ item.quantity }}</span>
            </div>
          </div>
          <div v-if="order.remark" class="order-remark">
            备注: {{ order.remark }}
          </div>
          <div class="order-footer">
            <span class="order-time">{{ formatTime(order.createdAt) }}</span>
            <span class="order-amount">¥{{ order.totalAmount }}</span>
          </div>
          <button class="pixel-btn primary" @click="acceptOrder(order.id)">
            接单
          </button>
        </div>
      </div>
    </div>

    <div class="pixel-card">
      <h2>制作中</h2>
      <div v-if="makingOrders.length === 0" class="empty-hint">
        暂无制作中订单
      </div>
      <div v-else class="order-queue">
        <div
          v-for="order in makingOrders"
          :key="order.id"
          class="order-card pixel-card making"
        >
          <div class="order-header">
            <span class="order-no">#{{ order.orderNo.slice(-6) }}</span>
            <span class="order-status">制作中...</span>
          </div>
          <div class="order-items">
            <div v-for="item in order.items" :key="item.id" class="order-item-row">
              <span>☕ {{ item.coffeeName }}</span>
              <span>x{{ item.quantity }}</span>
            </div>
          </div>
          <button class="pixel-btn success" @click="completeOrder(order.id)">
            完成制作
          </button>
        </div>
      </div>
    </div>

    <div class="pixel-card">
      <h2>今日已完成</h2>
      <div v-if="completedOrders.length === 0" class="empty-hint">
        暂无已完成订单
      </div>
      <div v-else class="completed-list">
        <div
          v-for="order in completedOrders"
          :key="order.id"
          class="completed-item"
        >
          <span>#{{ order.orderNo.slice(-6) }}</span>
          <span>{{ order.items?.map(i => i.coffeeName).join(', ') }}</span>
          <span>¥{{ order.totalAmount }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { orderApi } from '../api'

const pendingOrders = ref([])
const makingOrders = ref([])
const completedOrders = ref([])

let refreshInterval = null

onMounted(async () => {
  await loadOrders()
  refreshInterval = setInterval(loadOrders, 5000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})

const loadOrders = async () => {
  try {
    // 加载待处理订单
    const pendingRes = await orderApi.pending()
    if (pendingRes.code === 200) {
      pendingOrders.value = pendingRes.data || []
    }

    // 加载制作中订单
    const makingRes = await orderApi.page({ status: 'MAKING' })
    if (makingRes.code === 200) {
      makingOrders.value = makingRes.data.records || []
    }

    // 加载已完成订单
    const completedRes = await orderApi.page({ status: 'COMPLETED' })
    if (completedRes.code === 200) {
      completedOrders.value = (completedRes.data.records || []).slice(0, 10)
    }
  } catch (e) {
    console.error(e)
  }
}

const acceptOrder = async (orderId) => {
  try {
    const res = await orderApi.accept(orderId)
    if (res.code === 200) {
      await loadOrders()
    } else {
      alert(res.message || '接单失败')
    }
  } catch (e) {
    alert('接单失败')
  }
}

const completeOrder = async (orderId) => {
  try {
    const res = await orderApi.complete(orderId)
    if (res.code === 200) {
      await loadOrders()
    } else {
      alert(res.message || '完成失败')
    }
  } catch (e) {
    alert('完成失败')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.barista-view {
  padding: 16px;
  max-width: 1000px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.empty-hint {
  color: var(--pixel-gray);
  text-align: center;
  padding: 24px;
}

.order-queue {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card {
  padding: 16px;
}

.order-card.making {
  border-color: var(--pixel-blue);
  background: rgba(0, 122, 204, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.order-no {
  font-family: var(--font-pixel);
  font-weight: bold;
}

.order-customer {
  color: var(--pixel-gray);
}

.order-status {
  color: var(--pixel-blue);
  font-weight: bold;
}

.order-items {
  margin-bottom: 12px;
}

.order-item-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  border-bottom: 1px dashed var(--pixel-cream);
}

.order-remark {
  font-size: 12px;
  color: var(--pixel-gray);
  background: var(--pixel-cream);
  padding: 8px;
  margin-bottom: 12px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 12px;
}

.order-time {
  color: var(--pixel-gray);
}

.order-amount {
  font-weight: bold;
  color: var(--pixel-brown);
}

.pixel-btn {
  width: 100%;
}

.pixel-btn.success {
  background: var(--pixel-green);
  color: white;
}

.completed-list {
  font-size: 12px;
}

.completed-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed var(--pixel-cream);
}
</style>

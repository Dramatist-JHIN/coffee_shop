<template>
  <div class="manager-page">
    <!-- 角色标识 -->
    <div class="character-display">
      <div class="character">
        <div class="character-badge">管理员</div>
        <div class="character-avatar">👨‍💼</div>
      </div>
    </div>

    <!-- 库存预警 -->
    <div v-if="lowStockCoffees.length > 0" class="pixel-alert warning">
      <span>⚠️</span>
      <span>库存预警：{{ lowStockCoffees.map(c => c.name).join('、') }} 库存不足！</span>
    </div>

    <!-- 标签页 -->
    <div class="pixel-tabs">
      <div
        class="pixel-tab"
        :class="{ active: activeTab === 'coffee' }"
        @click="activeTab = 'coffee'"
      >
        咖啡管理
      </div>
      <div
        class="pixel-tab"
        :class="{ active: activeTab === 'orders' }"
        @click="activeTab = 'orders'; loadOrders()"
      >
        订单列表
      </div>
      <div
        class="pixel-tab"
        :class="{ active: activeTab === 'stats' }"
        @click="activeTab = 'stats'; loadStats()"
      >
        统计数据
      </div>
    </div>

    <div class="pixel-tab-content">
      <!-- 咖啡管理 -->
      <div v-if="activeTab === 'coffee'">
        <div class="toolbar">
          <div class="search-box">
            <input
              type="text"
              class="pixel-input"
              v-model="searchKeyword"
              placeholder="搜索咖啡..."
              @input="handleSearch"
            />
          </div>
          <button class="pixel-btn primary" @click="openAddModal">
            + 添加咖啡
          </button>
        </div>

        <table class="pixel-table">
          <thead>
            <tr>
              <th>图片</th>
              <th>名称</th>
              <th>分类</th>
              <th>价格</th>
              <th>库存</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="coffee in coffees" :key="coffee.id">
              <td>
                <div class="coffee-thumb">☕</div>
              </td>
              <td>
                <strong>{{ coffee.name }}</strong>
                <div class="desc">{{ coffee.description }}</div>
              </td>
              <td>{{ coffee.category }}</td>
              <td class="price">¥{{ coffee.price }}</td>
              <td>
                <span :class="{ 'low-stock': coffee.stock < 10 }">
                  {{ coffee.stock }}
                </span>
              </td>
              <td>
                <span class="status-tag" :class="coffee.status === 1 ? 'active' : 'inactive'">
                  {{ coffee.status === 1 ? '上架' : '下架' }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="pixel-btn small" @click="openEditModal(coffee)">编辑</button>
                  <button class="pixel-btn small warning" @click="openStockModal(coffee)">补货</button>
                  <button class="pixel-btn small danger" @click="handleDelete(coffee)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="coffees.length === 0" class="pixel-empty">
          <div class="pixel-empty-icon">☕</div>
          <p>暂无咖啡数据</p>
        </div>
      </div>

      <!-- 订单列表 -->
      <div v-if="activeTab === 'orders'">
        <div class="toolbar">
          <select class="pixel-select" v-model="orderStatusFilter" @change="loadOrders" style="width: 120px">
            <option value="">全部状态</option>
            <option value="PENDING">待处理</option>
            <option value="MAKING">制作中</option>
            <option value="COMPLETED">已完成</option>
            <option value="CANCELLED">已取消</option>
          </select>
          <input
            type="text"
            class="pixel-input"
            v-model="customerNameFilter"
            placeholder="顾客名称"
            @input="debouncedLoadOrders"
            style="width: 120px"
          />
          <input
            type="text"
            class="pixel-input"
            v-model="coffeeNameFilter"
            placeholder="咖啡名称"
            @input="debouncedLoadOrders"
            style="width: 120px"
          />
          <input
            type="date"
            class="pixel-input"
            v-model="startDateFilter"
            @change="loadOrders"
            style="width: 140px"
          />
          <span style="line-height: 36px">至</span>
          <input
            type="date"
            class="pixel-input"
            v-model="endDateFilter"
            @change="loadOrders"
            style="width: 140px"
          />
          <button class="pixel-btn" @click="clearOrderFilters">清空筛选</button>
        </div>

        <table class="pixel-table">
          <thead>
            <tr>
              <th>订单号</th>
              <th>顾客</th>
              <th>咖啡师</th>
              <th>商品</th>
              <th>金额</th>
              <th>状态</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.id">
              <td>{{ order.orderNo?.slice(-8) }}</td>
              <td>{{ order.customerName || '-' }}</td>
              <td>{{ order.baristaName || '-' }}</td>
              <td>
                <div v-for="item in order.items" :key="item.id" class="order-item-line">
                  {{ item.coffeeName }} × {{ item.quantity }}
                </div>
              </td>
              <td class="price">¥{{ order.totalAmount }}</td>
              <td>
                <span class="status-tag" :class="order.status?.toLowerCase()">
                  {{ getStatusName(order.status) }}
                </span>
              </td>
              <td>{{ formatTime(order.createdAt) }}</td>
            </tr>
          </tbody>
        </table>

        <div v-if="orders.length === 0" class="pixel-empty">
          <div class="pixel-empty-icon">📋</div>
          <p>暂无订单</p>
        </div>
      </div>

      <!-- 统计数据 -->
      <div v-if="activeTab === 'stats'">
        <div class="pixel-grid cols-3">
          <div class="stat-card">
            <div class="stat-card-value">¥{{ stats.totalRevenue || 0 }}</div>
            <div class="stat-card-label">总销售额</div>
          </div>
          <div class="stat-card">
            <div class="stat-card-value">{{ stats.completedOrders || 0 }}</div>
            <div class="stat-card-label">已完成订单</div>
          </div>
          <div class="stat-card">
            <div class="stat-card-value">{{ stats.pendingOrders || 0 }}</div>
            <div class="stat-card-label">待处理订单</div>
          </div>
        </div>

        <div class="pixel-grid cols-2" style="margin-top: 24px">
          <div class="pixel-card">
            <div class="pixel-card-header">咖啡品种总数</div>
            <div class="stat-big">{{ stats.totalCoffees || 0 }} 种</div>
          </div>
          <div class="pixel-card">
            <div class="pixel-card-header">库存预警</div>
            <div v-for="coffee in lowStockCoffees" :key="coffee.id" class="low-stock-item">
              <span class="name">{{ coffee.name }}</span>
              <span class="stock">剩余 {{ coffee.stock }} 份</span>
              <button class="pixel-btn small warning" @click="openStockModal(coffee)">补货</button>
            </div>
            <div v-if="lowStockCoffees.length === 0" class="pixel-empty">
              <p>库存充足</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑咖啡模态框 -->
    <div v-if="showCoffeeModal" class="pixel-modal-overlay" @click.self="closeCoffeeModal">
      <div class="pixel-modal">
        <div class="pixel-modal-header">
          <span>{{ editingCoffee ? '编辑咖啡' : '添加咖啡' }}</span>
          <button class="pixel-modal-close" @click="closeCoffeeModal">×</button>
        </div>
        <form @submit.prevent="handleSaveCoffee">
          <div class="pixel-form-group">
            <label>咖啡名称 *</label>
            <input type="text" class="pixel-input" v-model="coffeeForm.name" required />
          </div>
          <div class="pixel-form-group">
            <label>描述</label>
            <input type="text" class="pixel-input" v-model="coffeeForm.description" />
          </div>
          <div class="pixel-grid cols-2">
            <div class="pixel-form-group">
              <label>价格 *</label>
              <input type="number" step="0.01" class="pixel-input" v-model="coffeeForm.price" required />
            </div>
            <div class="pixel-form-group">
              <label>库存 *</label>
              <input type="number" class="pixel-input" v-model="coffeeForm.stock" required />
            </div>
          </div>
          <div class="pixel-form-group">
            <label>分类</label>
            <input type="text" class="pixel-input" v-model="coffeeForm.category" placeholder="如：经典、特调" />
          </div>
          <div class="pixel-form-group">
            <label>状态</label>
            <select class="pixel-select" v-model="coffeeForm.status">
              <option :value="1">上架</option>
              <option :value="0">下架</option>
            </select>
          </div>
          <div class="pixel-modal-footer">
            <button type="button" class="pixel-btn" @click="closeCoffeeModal">取消</button>
            <button type="submit" class="pixel-btn primary">保存</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 补货模态框 -->
    <div v-if="showStockModal" class="pixel-modal-overlay" @click.self="closeStockModal">
      <div class="pixel-modal">
        <div class="pixel-modal-header">
          <span>补货 - {{ stockCoffee?.name }}</span>
          <button class="pixel-modal-close" @click="closeStockModal">×</button>
        </div>
        <div class="pixel-form-group">
          <label>当前库存：{{ stockCoffee?.stock }}</label>
        </div>
        <div class="pixel-form-group">
          <label>新库存数量</label>
          <input type="number" class="pixel-input" v-model="newStock" min="0" />
        </div>
        <div class="pixel-modal-footer">
          <button class="pixel-btn" @click="closeStockModal">取消</button>
          <button class="pixel-btn primary" @click="handleUpdateStock">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { coffeeApi, orderApi, statsApi } from '../api'

const activeTab = ref('coffee')
const searchKeyword = ref('')
const orderStatusFilter = ref('')
const customerNameFilter = ref('')
const coffeeNameFilter = ref('')
const startDateFilter = ref('')
const endDateFilter = ref('')

// 防抖定时器
let orderSearchTimer = null
const debouncedLoadOrders = () => {
  if (orderSearchTimer) clearTimeout(orderSearchTimer)
  orderSearchTimer = setTimeout(loadOrders, 300)
}

// 清空订单筛选
const clearOrderFilters = () => {
  orderStatusFilter.value = ''
  customerNameFilter.value = ''
  coffeeNameFilter.value = ''
  startDateFilter.value = ''
  endDateFilter.value = ''
  loadOrders()
}

const coffees = ref([])
const lowStockCoffees = ref([])
const orders = ref([])
const stats = ref({})

// 咖啡模态框
const showCoffeeModal = ref(false)
const editingCoffee = ref(null)
const coffeeForm = reactive({
  name: '',
  description: '',
  price: '',
  stock: '',
  category: '',
  status: 1
})

// 补货模态框
const showStockModal = ref(false)
const stockCoffee = ref(null)
const newStock = ref(0)

onMounted(async () => {
  await loadCoffees()
})

const loadCoffees = async () => {
  try {
    const res = await coffeeApi.page({ keyword: searchKeyword.value })
    if (res.code === 200) {
      coffees.value = res.data.records || []
      lowStockCoffees.value = coffees.value.filter(c => c.stock < 10)
    }
  } catch (e) {
    console.error(e)
  }
}

const handleSearch = () => {
  loadCoffees()
}

const loadOrders = async () => {
  try {
    const params = {}
    if (orderStatusFilter.value) {
      params.status = orderStatusFilter.value
    }
    if (customerNameFilter.value) {
      params.customerName = customerNameFilter.value
    }
    if (coffeeNameFilter.value) {
      params.coffeeName = coffeeNameFilter.value
    }
    if (startDateFilter.value) {
      params.startDate = startDateFilter.value
    }
    if (endDateFilter.value) {
      params.endDate = endDateFilter.value
    }
    const res = await orderApi.page(params)
    if (res.code === 200) {
      orders.value = res.data.records || []
    }
  } catch (e) {
    console.error(e)
  }
}

const loadStats = async () => {
  try {
    const res = await statsApi.dashboard()
    if (res.code === 200) {
      stats.value = res.data
      lowStockCoffees.value = res.data.lowStockCoffees || []
    }
  } catch (e) {
    console.error(e)
  }
}

const openAddModal = () => {
  editingCoffee.value = null
  Object.assign(coffeeForm, {
    name: '',
    description: '',
    price: '',
    stock: '',
    category: '',
    status: 1
  })
  showCoffeeModal.value = true
}

const openEditModal = (coffee) => {
  editingCoffee.value = coffee
  Object.assign(coffeeForm, {
    name: coffee.name,
    description: coffee.description,
    price: coffee.price,
    stock: coffee.stock,
    category: coffee.category,
    status: coffee.status
  })
  showCoffeeModal.value = true
}

const closeCoffeeModal = () => {
  showCoffeeModal.value = false
  editingCoffee.value = null
}

const handleSaveCoffee = async () => {
  try {
    if (editingCoffee.value) {
      await coffeeApi.update(editingCoffee.value.id, coffeeForm)
    } else {
      await coffeeApi.create(coffeeForm)
    }
    closeCoffeeModal()
    await loadCoffees()
  } catch (e) {
    alert(e.message || '保存失败')
  }
}

const handleDelete = async (coffee) => {
  if (confirm(`确定要删除 ${coffee.name} 吗？`)) {
    try {
      await coffeeApi.delete(coffee.id)
      await loadCoffees()
    } catch (e) {
      alert(e.message || '删除失败')
    }
  }
}

const openStockModal = (coffee) => {
  stockCoffee.value = coffee
  newStock.value = coffee.stock
  showStockModal.value = true
}

const closeStockModal = () => {
  showStockModal.value = false
  stockCoffee.value = null
}

const handleUpdateStock = async () => {
  try {
    await coffeeApi.updateStock(stockCoffee.value.id, newStock.value)
    closeStockModal()
    await loadCoffees()
  } catch (e) {
    alert(e.message || '更新失败')
  }
}

const getStatusName = (status) => {
  const names = {
    PENDING: '待处理',
    MAKING: '制作中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return names[status] || status
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.manager-page {
  position: relative;
  padding: 16px;
}

.character-display {
  position: absolute;
  top: -60px;
  right: 20px;
}

.character-avatar {
  font-size: 48px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.search-box {
  display: flex;
  gap: 12px;
}

.search-box .pixel-input {
  width: 200px;
}

.coffee-thumb {
  width: 40px;
  height: 40px;
  background: var(--pixel-cream);
  border: 2px solid var(--pixel-brown);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.desc {
  font-size: 12px;
  color: var(--pixel-gray);
  margin-top: 4px;
}

.price {
  font-weight: bold;
  color: var(--pixel-brown);
}

.low-stock {
  color: var(--pixel-red);
  font-weight: bold;
}

.action-btns {
  display: flex;
  gap: 8px;
}

.order-item-line {
  font-size: 12px;
  padding: 2px 0;
}

.low-stock-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-bottom: 1px dashed var(--pixel-brown);
  gap: 12px;
}

.low-stock-item .name {
  flex: 1;
}

.low-stock-item .stock {
  color: var(--pixel-red);
  font-weight: bold;
}

.stat-big {
  font-size: 36px;
  font-weight: bold;
  text-align: center;
  padding: 24px;
  color: var(--pixel-brown);
}
</style>

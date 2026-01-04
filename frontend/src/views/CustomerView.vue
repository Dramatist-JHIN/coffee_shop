<template>
  <div class="customer-view">
    <!-- 新增游戏容器界面（按q键显示） -->
    <div class="game-container" v-if="currentView === 'game'">
      <!-- 游戏容器内容，可添加图片或游戏元素 -->
      <!-- <h2>游戏界面</h2>-->
      <img src="@/assets/images/game_bg.jpg" alt="游戏背景" class="game-bg">
      <!-- 顾客角色：绑定坐标变量 -->
      <div
          class="customer-role"
          :style="{ left: customerX + 'px', top: customerY + 'px' }"
      ></div>
      <!-- 两个交互点可视化（对应橙色圆圈，放在游戏容器内） -->
      <div
          v-for="(point, index) in interactionPoints"
          :key="index"
          class="interaction-point"
          :style="{ left: point.x + 'px', top: point.y + 'px' }"
          :class="{ active: isNearAnyInteractionPoint }"
      ></div>
      <!-- 缩小后的禁入区（仅覆盖柜台/桌子，下方留通道） -->
      <!-- 红色区域对应的禁入区 -->
      <div class="forbidden-area" style="left: 0; top: 0; width: 1200px; height: 200px;"></div>
      <div class="forbidden-area" style="left: 0; top: 0; width: 430px; height: 400px;"></div>
      <!-- 交互点标记（用于判断是否在附近） -->
      <!--<div class="interaction-point" :class="{ active: startInteractionCheck }"></div>-->
    </div>
    <div
        class="pixel-card"
        v-else-if="currentView === 'menu'"
        tabindex="0"
        ref="menuContainer"
    >
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
              <span class="item-stock" :class="stockClass(coffee.stock)">
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
    <!-- 动态咖啡杯 -->
    <!-- 功能：固定在右下角的可点击咖啡杯，点击后弹出对话框 -->
    <!-- 位置：fixed定位，右下角20px -->
    <!-- 交互：点击触发toggleCoffeeDialog函数 -->
    <div class="coffee-cup" @click="toggleCoffeeDialog">
      ☕
    </div>

    <!-- 咖啡杯对话框 -->
    <!-- 功能：点击咖啡杯后显示的AI对话界面 -->
    <!-- 显示条件：showCoffeeDialog为true时显示 -->
    <!-- 交互：点击遮罩或关闭按钮可关闭对话框 -->
    <div v-if="showCoffeeDialog" class="coffee-dialog-overlay" @click="toggleCoffeeDialog">
      <!-- 对话框内容，使用@click.stop防止事件冒泡 -->
      <div class="coffee-dialog pixel-card" @click.stop>
        <!-- 对话框头部：标题和关闭按钮 -->
        <div class="dialog-header">
          <h3>☕ 咖啡AI助手</h3>
          <button class="close-btn" @click="toggleCoffeeDialog">×</button>
        </div>
        <!-- 对话框内容区：AI对话 -->
        <div class="dialog-content chat-content">
          <!-- 对话消息列表 -->
          <div v-if="chatMessages.length === 0" class="empty-chat">
            <p>你好！有什么关于咖啡的问题可以问我~</p>
          </div>
          <div v-for="(message, index) in chatMessages" :key="index"
               :class="['chat-message', message.role]">
            <div class="message-content">
              {{ message.content }}
            </div>
          </div>
          <!-- 加载状态 -->
          <div v-if="isTyping" class="chat-message assistant typing">
            <div class="message-content">
              <span class="typing-dots">正在输入...</span>
            </div>
          </div>
        </div>
        <!-- 对话框底部：消息输入框 -->
        <div class="dialog-footer chat-input-area">
          <input
              v-model="userMessage"
              class="chat-input"
              placeholder="输入你的问题..."
              @keyup.enter="sendMessage"
          />
          <button class="pixel-btn" @click="sendMessage" :disabled="!userMessage.trim()">
            发送
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { coffeeApi, orderApi } from '../api'
// 引入提示框工具函数（如果需要在游戏界面中使用提示）
//import { showAlert } from '../js/api' // 假设路径正确，根据实际项目调整

const coffees = ref([])
const cart = ref([])
const orders = ref([])
const remark = ref('')

// 新增：控制当前显示的界面（菜单/游戏）
const currentView = ref('game') // 'menu' 表示菜单界面，'game' 表示游戏容器界面
// 新增菜单容器的ref
const menuContainer = ref(null);
// 咖啡杯对话框显示状态
const showCoffeeDialog = ref(false)

// 交互点配置：新增两个橙色圆圈对应的交互点（核心修改）
const interactionPoints = ref([
  { x: 250, y: 550, range: 60 }, // 第一个橙色圆圈位置（可根据实际背景图微调）
  { x: 408, y: 285, range: 60 }  // 第二个橙色圆圈位置（可根据实际背景图微调）
])
const isNearAnyInteractionPoint = ref(false) // 判定是否在任意一个交互点附近
// 新增↓ 顾客角色初始坐标（可自定义初始位置）
const customerX = ref(620) // 初始X轴位置
const customerY = ref(680) // 初始Y轴位置
const customerSize = { width: 32, height: 57 } // 顾客尺寸

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

// AI对话相关状态
const chatMessages = ref([]) // 对话消息列表
const userMessage = ref('') // 用户输入消息
const isTyping = ref(false) // AI正在输入状态
const sessionId = ref('coffee-ai-' + Date.now()) // 会话ID，确保唯一性

// 判断库存状态的辅助方法  好像没用
const stockClass = (stock) => {
  return { low: stock < 10 }
}

// 切换咖啡杯对话框显示/隐藏
const toggleCoffeeDialog = () => {
  showCoffeeDialog.value = !showCoffeeDialog.value
  // 如果是打开对话框，初始化会话
  if (showCoffeeDialog.value) {
    // 可以在这里添加欢迎消息
    if (chatMessages.value.length === 0) {
      chatMessages.value.push({
        role: 'assistant',
        content: '你好！我是咖啡AI助手，有什么关于咖啡的问题可以问我~'
      })
    }
  }
}

// 发送消息给AI
const sendMessage = async () => {
  if (!userMessage.value || isTyping.value) return;

  isTyping.value = true;
  const message = userMessage.value;
  userMessage.value = '';

  // 添加用户消息到对话列表
  chatMessages.value.push({
    role: 'user',
    content: message
  });

  try {
    const response = await aiApi.chat({
      message,
      sessionId: sessionId.value
    });

    // 添加AI回复到对话列表
    chatMessages.value.push({
      role: 'assistant',
      content: response.data?.reply || '抱歉，未收到AI回复'
    });
  } catch (error) {
    console.error('AI对话错误:', error);
    // 添加错误提示消息
    chatMessages.value.push({
      role: 'assistant',
      content: error.response?.data?.reply || '抱歉，AI服务暂时不可用，请稍后再试。'
    });
  } finally {
    isTyping.value = false;
    // 滚动到底部
    nextTick(() => {
      const chatContent = document.querySelector('.chat-content');
      if (chatContent) {
        chatContent.scrollTop = chatContent.scrollHeight;
      }
    });
  }
}

onMounted(async () => {
  await loadCoffees()
  await loadOrders()
  console.log('组件挂载，绑定键盘事件'); // 新增
  // 新增：监听键盘事件
  document.body.addEventListener('keydown', handleKeyPress, true);
  console.log('键盘事件已绑定到window捕获阶段');
  setTimeout(() => {
    document.addEventListener('keydown', handleKeyPress);
    console.log('window键盘事件绑定成功');
  }, 0);
  // 新增↓ 监听点击事件，移动顾客
  document.addEventListener('click', handleClickMove)
  // 新增：启动交互点检测（模拟逻辑，实际需替换）
  startInteractionCheck()
})

// 新增：组件卸载时移除事件监听，避免内存泄漏
onUnmounted(() => {
  console.log('组件卸载，移除键盘事件'); // 新增
  document.body.removeEventListener('keydown', handleKeyPress, true);
  // 新增↓ 移除点击事件监听，避免内存泄漏
  document.removeEventListener('click', handleClickMove)
  // 清除交互点检测的定时器
  if (document.interactionTimer) {
    clearInterval(document.interactionTimer)
  }
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

// 新增：键盘事件处理函数
const handleKeyPress = (e) => {
  console.log('按下的键：', e.key, '当前视图：', currentView.value);

  // 按 Q 键切换到游戏界面（忽略大小写）
  if (e.key.toLowerCase() === 'q') {
    currentView.value = 'game';
    console.log('Q键触发，切回游戏界面');
    //showAlert('已进入游戏界面', 'info') // 可选：添加提示
    //alert('已进入游戏界面') // 临时用原生 alert 替代，避免报错
  }
  // 按 E 键且在交互点附近时，从游戏界面切回菜单界面
  if (e.key.toLowerCase() === 'e' && currentView.value === 'game' && isNearAnyInteractionPoint.value) {
    currentView.value = 'menu';
    //showAlert('已返回菜单界面', 'info') // 可选：添加提示
    //alert('已返回菜单界面')
  }

}

// 新增：检测目标位置是否在禁入区内
const isInForbiddenArea = (targetX, targetY) => {
  // 获取所有禁入区元素
  const forbiddenAreas = document.querySelectorAll('.forbidden-area');
  // 遍历所有禁入区，判断顾客是否与禁入区重叠
  for (let area of forbiddenAreas) {
    const areaRect = area.getBoundingClientRect();
    const gameContainer = document.querySelector('.game-container');
    const containerRect = gameContainer.getBoundingClientRect();

    // 转换目标坐标为相对于游戏容器的禁入区坐标
    const areaX = areaRect.left - containerRect.left;
    const areaY = areaRect.top - containerRect.top;
    const areaWidth = areaRect.width;
    const areaHeight = areaRect.height;

    // 顾客的碰撞盒（以顾客左上角为基准，匹配尺寸）
    const customerRect = {
      x: targetX,
      y: targetY,
      width: 32, // 顾客宽度
      height: 57 // 顾客高度
    };

    // 矩形碰撞检测：判断顾客是否与禁入区重叠
    const isOverlap = !(
        customerRect.x + customerRect.width < areaX ||
        customerRect.x > areaX + areaWidth ||
        customerRect.y + customerRect.height < areaY ||
        customerRect.y > areaY + areaHeight
    );

    if (isOverlap) {
      return true; // 命中禁入区，禁止移动
    }
  }
  return false; // 未命中禁入区，允许移动
};

// 1. 新增：点击移动顾客逻辑
// 修复后的点击移动逻辑（添加禁入区检测）
const handleClickMove = (e) => {
  if (currentView.value !== 'game') return;
  const gameContainer = document.querySelector('.game-container');
  if (!gameContainer) return;

  // 计算目标坐标（顾客居中）
  const rect = gameContainer.getBoundingClientRect();
  const targetX = e.clientX - rect.left - 16; // 32/2=16
  const targetY = e.clientY - rect.top - 28.5; // 57/2=28.5

  // 第一步：检测目标位置是否在禁入区内
  if (isInForbiddenArea(targetX, targetY)) {
    console.log('禁止移动：目标位置在禁入区内');
    return; // 禁止移动
  }

  // 第二步：限制顾客在容器内（原有逻辑）
  if (targetX >= 0 && targetX <= rect.width - 32) {
    customerX.value = targetX;
  }
  if (targetY >= 0 && targetY <= rect.height - 57) {
    customerY.value = targetY;
  }
};

// 2. 重构：交互点附近判定（基于顾客与交互点的距离）
const startInteractionCheck = () => {
  window.interactionTimer = setInterval(() => {
    const customerCenterX = customerX.value + 16; // 顾客宽度32/2
    const customerCenterY = customerY.value + 28.5; // 顾客高度57/2
    //console.log('顾客中心坐标：', customerCenterX, customerCenterY);
    //console.log('交互点坐标：', interactionPoints.value);

    const isNear = interactionPoints.value.some(point => {
      const distance = Math.sqrt(
          Math.pow(customerCenterX - point.x, 2) +
          Math.pow(customerCenterY - point.y, 2)
      );
      //console.log('到交互点距离：', distance, '阈值：', point.range);
      return distance <= point.range;
    });
    isNearAnyInteractionPoint.value = isNear;
    console.log('是否在交互点附近：', isNearAnyInteractionPoint.value);
  }, 300);
};
// 原有购物车相关方法保持不变
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
.forbidden-area {
  position: absolute;
  background: transparent;/*rgba(255, 0, 0, 0.2); 红色半透明，调试用 */
  z-index: 8; /* 低于顾客，高于背景 */
}

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

/* 游戏容器样式 */
.game-container {
  width: 100%;
  height: 500px; /* 根据需求调整 */
  position: relative;
  border: 2px solid #333;
  background-color: #f0f0f0;
  overflow: hidden;
}

/* 游戏背景图样式 */
.game-bg {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: relative; /* 让z-index生效 */
  z-index: 1; /* 背景图层级最低 */
}

/* 顾客角色：绑定Customer.png，配置尺寸+平滑移动 */
.customer-role {
  position: absolute; /* 基于游戏容器定位 */
  width: 97px;   /* 按Customer.png实际尺寸调整（示例：32px宽） */
  height: 97px;  /* 按Customer.png实际尺寸调整（示例：48px高） */
  background-image: url('@/assets/images/Customer.gif'); /* 顾客图片路径（必须和文件一致） */
  background-size: contain; /* 让图片适配元素尺寸，无拉伸 */
  /* background-repeat: no-repeat;  防止图片重复填充 */
  background-color: transparent; /* 强制背景透明（关键） */
  border: none; /* 无边框 */
  outline: none; /* 无外框 */
  opacity: 1; /* 确保元素不透明 */
  transition: left 0.3s ease, top 0.3s ease; /* 关键：点击移动时平滑过渡，不卡顿 */
  z-index: 10; /* 确保顾客在背景图上层显示 */
}

/* 交互点样式 */
.interaction-point {
  width: 50px;
  height: 50px;
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(255, 255, 0, 0.5);
  border-radius: 50%;
  display: none; /* 默认隐藏 */
}

/* 交互点样式（对应橙色圆圈） */
.interaction-point {
  position: absolute;
  width: 20px;
  height: 20px;
  background: rgba(255, 165, 0, 0.5); /* 橙色半透明，匹配设计图 */
  border-radius: 50%;
  z-index: 5;
}
.interaction-point.active {
  background: rgba(255, 165, 0, 1); /* 靠近时变为不透明橙色 */
}
/* 当在交互点附近时显示 */
.interaction-point.active {
  display: block;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { transform: translateX(-50%) scale(1); }
  50% { transform: translateX(-50%) scale(1.2); }
  100% { transform: translateX(-50%) scale(1); }
}
</style>

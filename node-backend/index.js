import express from "express";
import axios from "axios";
import cors from "cors";
import dotenv from "dotenv";
//明确：为什么给了限定提示词，就能够约束ai的身份，也就是回答
//在大型语言模型（如文心一言）的对话机制中，系统提示词(system prompt)具有最高优先级，被设计为指导模型行为的核心指令。代码中第58行的注释也明确指出："在千帆v2/文心模型里，system role的权重是最高的"。

// ### 2. 工作原理
// - 初始化模型行为 ：系统提示词在对话开始时设置，相当于给模型设定了一个基础角色和行为准则
// - 上下文理解框架 ：提示词为模型提供了理解用户问题的框架，决定了模型应该如何解读用户输入
// - 输出格式指导 ：明确告诉模型应该以什么格式返回信息（如代码中的JSON格式订单）
// ### 3. 技术实现
// 在node-backend/index.js中，通过 generateSystemPrompt() 函数动态构建系统提示，包含：

// - 角色设定："专业的咖啡店智能客服"
// - 行为规则：只回答本店咖啡产品相关问题，不提及不存在的产品
// - 输出指令：点单时返回特定JSON格式
// - 上下文信息：动态注入当前咖啡菜单
// 系统提示词约束AI行为的底层原理涉及大型语言模型的工作机制，我从几个层面来详细解释：

// ### 1. 模型训练与理解机制
// - **预训练基础**：AI模型在训练过程中学习了人类语言的语义、语法和上下文理解能力，包括指令的重要性
// - **条件概率预测**：模型本质上是一个基于上下文预测下一个词的系统，系统提示词作为初始上下文，显著影响后续预测
// - **指令跟随能力**：现代大型语言模型专门在训练过程中增强了对指令的理解和执行能力，这是通过大规模指令数据微调实现的

// ### 2. 系统提示词的特殊处理
// - **优先级机制**：许多模型架构（如文心一言）专门设计了系统提示词的高优先级处理机制，使其权重高于普通用户输入
// - **上下文窗口影响**：系统提示词位于对话历史的最前端，对整个上下文的理解框架影响最大
// - **角色锚定效应**：明确的角色定义会在模型生成回复时形成一个稳定的行为模式

// ### 3. 技术实现层面
// - **注意力机制**：在Transformer架构中，注意力机制会对系统提示词给予更多关注，特别是与角色和行为规则相关的部分
// - **格式引导编码**：具体的输出格式要求（如JSON结构）会被模型编码为一种生成约束
// - **约束网络形成**：多条规则共同形成一个约束网络，使模型生成时必须满足多个条件

// ### 4. 类比理解
// 我们可以把系统提示词理解为：
// - **程序的初始化参数**：设定模型的初始状态和行为参数
// - **游戏规则说明**：告诉模型在这个对话游戏中应该遵循什么规则
// - **角色剧本**：为AI提供一个角色设定，包括身份、知识范围和行为准则

// ### 5. 实际效果验证
// 在node-backend/index.js中，系统提示词通过generateSystemPrompt()函数动态构建，包含了精确的角色定义、行为规则和输出格式要求，实际运行时，AI模型会严格按照这些要求生成回复，例如只推荐菜单上的咖啡产品，并在用户点单时返回JSON格式的订单信息。

// 这种机制的有效性基于语言模型对人类指令的强大理解能力，通过精确的系统提示词设计，我们可以有效地引导AI在特定场景下的行为表现。

// 配置axios超时时间
axios.defaults.timeout = 60000;

// 配置环境变量
dotenv.config();

// 创建Express应用
const app = express();
// 启用CORS中间件
app.use(cors());
// 解析JSON请求体
app.use(express.json());

// ⚡ 配置 API_KEY 和 APPID
const API_KEY = process.env.BAIDU_API_KEY || "bce-v3/ALTAK-8BTj0w9Z1YiVUsrW7wsy1/fdab3e3fc726ffab0117acb6fa4fee5ff1decb50";
const APP_ID = process.env.BAIDU_APPID || "app-uMhFYP0m";

// 保存用户上下文（简单内存方案）
// key = sessionId, value = [{role, content}, ...]
const sessions = {};

// 保存用户会话状态
const userStates = {};

// 保存咖啡菜单缓存--规定上下文，只限于本店
let coffeeMenuCache = null;
let lastMenuUpdate = 0;
const MENU_CACHE_DURATION = 5 * 60 * 1000; // 5分钟缓存

// 获取咖啡菜单
async function getCoffeeMenu() {
  const now = Date.now();
  
  // 如果缓存存在且未过期，直接返回
  if (coffeeMenuCache && (now - lastMenuUpdate) < MENU_CACHE_DURATION) {
    return coffeeMenuCache;
  }
  
  try {
    // 从Java后端获取咖啡菜单
    const response = await axios.get('http://localhost:8080/api/coffee');
    
    if (response.data && response.data.code === 200 && response.data.data) {
      coffeeMenuCache = response.data.data;
      lastMenuUpdate = now;
      return coffeeMenuCache;
    }
    
    // 如果获取失败，返回空数组
    return [];
  } catch (error) {
    console.error('❌ 获取咖啡菜单失败:', error.message);
    return [];
  }
}

// 生成系统提示
async function generateSystemPrompt() {
  // 获取咖啡菜单
  const coffeeList = await getCoffeeMenu();
  
  // 构建咖啡菜单文本
  let coffeeMenuText = "";
  if (coffeeList.length > 0) {
    coffeeMenuText = "\n\n当前咖啡店提供的咖啡产品如下：\n";
    coffeeList.forEach(coffee => {
      coffeeMenuText += `- ${coffee.name}：${coffee.price}元${coffee.description ? "，" + coffee.description : ""}\n`;
    });
  }
  
  // 系统提示模板
  //在 千帆 v2 / 文心模型里，system role 的权重是最高的
  const systemPrompt = `你是一个专业的咖啡店智能客服。请严格按照以下规则回答顾客问题：
1. 只回答关于本店实际提供的咖啡产品、价格、营业时间等相关问题
2. 绝对不要提及或推荐本店不存在的咖啡种类或产品
3. 如果顾客询问的咖啡产品不在菜单中，请明确告知并推荐菜单中的产品
4. 请用简洁友好的语言回答，回复时使用中文，语气亲切热情
5. 当不确定答案时，请诚实告知，不要猜测或编造信息${coffeeMenuText}
6. 如果用户提到菜单之外的产品，必须明确说明"本店未提供该产品"

当顾客想要点单时，请以JSON格式返回订单信息，格式为：
{
  "orderType": "order",
  "items": [
    {"coffeeName": "咖啡名称", "quantity": 数量}
  ],
  "remark": "备注信息"
}

注意事项：
- 请确保咖啡名称必须与菜单完全一致
- 请忽略菜单中不存在的咖啡种类
- 如果用户没有明确指定数量，默认为1
- 如果用户没有提供备注，remark字段为空字符串
- 只有当用户明确表示要点单或确认订单时，才返回JSON格式
- 如果用户只是询问或聊天，请正常回复，不要返回JSON格式`;

  return systemPrompt;
}

// 解析AI返回的订单信息
function parseOrderInfo(reply) {
  try {
    // 尝试从回复中提取JSON格式的订单信息
    const jsonStart = reply.indexOf('{');
    const jsonEnd = reply.lastIndexOf('}') + 1;
    
    if (jsonStart !== -1 && jsonEnd > jsonStart) {
      const jsonStr = reply.substring(jsonStart, jsonEnd);
      const orderData = JSON.parse(jsonStr);
      
      // 验证是否是有效的订单格式
      if (orderData.orderType === 'order' && Array.isArray(orderData.items)) {
        return orderData;
      }
    }
    return null;
  } catch (error) {
    console.error('❌ 解析订单信息失败:', error.message);
    return null;
  }
}

// 验证订单信息并转换为后端需要的格式
async function validateAndTransformOrder(orderData) {
  try {
    const coffeeList = await getCoffeeMenu();
    const validItems = [];
    
    for (const item of orderData.items) {
      // 查找对应的咖啡
      const coffee = coffeeList.find(c => c.name === item.coffeeName);
      if (coffee && coffee.status === 1) { // 确保咖啡存在且未下架
        validItems.push({
          coffeeId: coffee.id,
          quantity: item.quantity || 1
        });
      }
    }
    
    if (validItems.length === 0) {
      throw new Error('订单中没有有效的咖啡产品');
    }
    
    return {
      items: validItems,
      remark: orderData.remark || ''
    };
  } catch (error) {
    throw error;
  }
}

// 调用后端创建订单
async function createOrderBackend(userId, orderData) {
  try {
    const response = await axios.post(
      'http://localhost:8080/api/order',
      orderData,
      {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${userId}` // 简化处理，实际应使用有效的token
        }
      }
    );
    
    return response.data;
  } catch (error) {
    console.error('❌ 创建订单失败:', error.response?.data || error.message);
    throw error;
  }
}

// 健康检查接口
app.get("/ping", (req, res) => res.send("pong"));

// 聊天接口
app.post("/api/chat", async (req, res) => {
  try {
    const { message, sessionId, userId = '1' } = req.body; // 默认用户ID为1，实际应从前端获取
    
    // 验证参数
    if (!message) return res.status(400).json({ error: "message 不能为空" });

    // 初始化会话上下文（首次创建会话时添加系统提示）
    if (!sessions[sessionId]) {
      // 生成包含实际咖啡菜单的系统提示
      const systemPrompt = await generateSystemPrompt();
      sessions[sessionId] = [{ 
        role: "system", 
        content: systemPrompt
      }];
      // 初始化用户状态
      userStates[sessionId] = {
        status: 'normal', // normal, waiting_for_confirm, order_created
        pendingOrder: null
      };
    }

    // 获取当前用户状态
    const userState = userStates[sessionId];
    
    // 处理订单确认
    if (userState.status === 'waiting_for_confirm' && 
        (message.includes('确认') || message.includes('是') || message.includes('对') || message.includes('好') || message.includes('确定'))) {
      // 用户确认订单，调用后端API创建订单
      if (userState.pendingOrder) {
        try {
          const orderResult = await createOrderBackend(userId, userState.pendingOrder);
          
          // 更新用户状态
          userState.status = 'order_created';
          
          // 返回订单创建成功消息
          const successReply = `订单已创建成功！\n订单号：${orderResult.data.orderNo.slice(-6)}\n预计等待时间约为10分钟。`;
          res.json({ reply: successReply, orderCreated: true, orderInfo: orderResult.data });
          return;
        } catch (error) {
          // 重置状态
          userState.status = 'normal';
          userState.pendingOrder = null;
          res.json({ reply: `创建订单失败：${error.message || '未知错误'}` });
          return;
        }
      }
    } else if (userState.status === 'waiting_for_confirm') {
      // 用户取消订单
      userState.status = 'normal';
      userState.pendingOrder = null;
      res.json({ reply: '已取消订单，您可以重新下单。' });
      return;
    }

    // 当前消息加入上下文
    sessions[sessionId].push({ role: "user", content: message });

    // 构建请求体
    const payload = {
      model: "ernie-4.5-turbo-128k",
      messages: sessions[sessionId]
    };

    // 调用文心一言API (正确格式)
    const apiUrl = "https://qianfan.baidubce.com/v2/chat/completions";
    
    console.log('📤 发送请求到文心一言API:', {
      url: apiUrl,
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${API_KEY}`,
        "appid": APP_ID
      },
      payload: payload
    });

    const response = await axios.post(
      apiUrl,
      payload,
      {
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${API_KEY}`,
          "appid": APP_ID
        },
        timeout: 60000
      }
    );

    // 记录API响应
    console.log('📥 收到文心一言API响应:', {
      status: response.status,
      data: response.data
    });

    // 提取AI回复
    const reply = response.data?.choices?.[0]?.message?.content || "模型未返回内容";

    // 将模型回复加入上下文
    sessions[sessionId].push({ role: "assistant", content: reply });
    
    // 尝试解析是否包含订单信息
    const orderInfo = parseOrderInfo(reply);
    if (orderInfo) {
      try {
        // 验证订单并转换格式
        const transformedOrder = await validateAndTransformOrder(orderInfo);
        
        // 生成订单摘要
        const coffeeList = await getCoffeeMenu();
        let orderSummary = '您的订单详情：\n';
        let totalPrice = 0;
        
        transformedOrder.items.forEach(item => {
          const coffee = coffeeList.find(c => c.id === item.coffeeId);
          if (coffee) {
            const itemPrice = coffee.price * item.quantity;
            totalPrice += itemPrice;
            orderSummary += `- ${coffee.name} × ${item.quantity}  ${itemPrice}元\n`;
          }
        });
        
        orderSummary += `总计：${totalPrice}元\n`;
        if (transformedOrder.remark) {
          orderSummary += `备注：${transformedOrder.remark}\n`;
        }
        orderSummary += '\n请问是否确认订单？';
        
        // 更新用户状态
        userState.status = 'waiting_for_confirm';
        userState.pendingOrder = transformedOrder;
        
        // 返回订单摘要和确认提示
        res.json({ reply: orderSummary });
      } catch (error) {
        res.json({ reply: `订单验证失败：${error.message}` });
      }
    } else {
      // 不是订单，直接返回AI回复
      res.json({ reply });
    }
  } catch (e) {
    // 错误处理
    console.error("❌ 调用模型失败:", e.response?.data || e.message);
    // 返回用户友好的错误信息
    if (e.code === 'ECONNABORTED' || e.message?.includes('timeout')) {
      res.status(500).json({ reply: "抱歉，当前AI服务响应较慢，请稍后再试。" });
    } else {
      res.status(500).json({ reply: "抱歉，AI服务暂时不可用，请稍后再试。" });
    }
  }
});

// 监听端口 - 使用3003端口避免冲突
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`✅ Node.js后端服务已启动，监听端口：http://localhost:${PORT}`));
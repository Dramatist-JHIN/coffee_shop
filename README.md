# ☕ 像素咖啡店

一个像素风格的咖啡店管理系统，支持多角色操作。

## 项目简介

这是一个简单的咖啡店模拟系统，实现了基本的点单、制作、管理功能。

### 角色说明

| 角色 | 功能 |
|------|------|
| 👨‍💼 管理员 | 管理咖啡菜单、查看订单、数据统计 |
| 👨‍🍳 咖啡师 | 接单、制作咖啡 |
| 🧑 顾客 | 浏览菜单、下单购买 |

## 技术栈

### 后端
- Java 17
- Spring Boot 3
- MyBatis-Plus
- MySQL 8

### 前端（两个版本）
- **Vue版** - Vue 3 + Vite
- **简易版** - HTML + CSS + JavaScript

## 项目结构

```
coffee/
├── backend/              # 后端代码
│   └── src/main/
│       ├── java/com/coffee/
│       │   ├── controller/   # 接口层
│       │   ├── service/      # 业务层
│       │   ├── mapper/       # 数据层
│       │   ├── entity/       # 实体类
│       │   └── config/       # 配置类
│       └── resources/
│           ├── application.yml
│           └── schema.sql
├── frontend/             # Vue前端
└── frontend-simple/      # 简易HTML前端
```

## 数据库设计

```
用户表 (user)
├── id, username, password
├── nickname, role, avatar
└── created_at, updated_at

咖啡表 (coffee)
├── id, name, description
├── price, category, stock
└── status, created_at

订单表 (order)
├── id, order_no
├── customer_id, barista_id
├── total_amount, status
└── remark, created_at

订单项表 (order_item)
├── id, order_id
├── coffee_id, coffee_name
└── price, quantity
```

## 运行步骤

### 1. 初始化数据库

```sql
-- 执行数据库脚本
source backend/src/main/resources/schema.sql
```

### 2. 修改配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    password: 你的MySQL密码
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

### 4. 启动前端

**简易版（直接打开）：**
```
用浏览器打开 frontend-simple/index.html
```

**Vue版：**
```bash
cd frontend
npm install
npm run dev
```

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 咖啡师 | barista | 123456 |
| 顾客 | customer | 123456 |

## 主要功能

### 顾客端
- 查看咖啡菜单
- 添加购物车
- 提交订单
- 查看订单状态

### 咖啡师端
- 查看待处理订单
- 接单
- 完成制作

### 管理员端
- 咖啡增删改查
- 库存管理
- 订单查看
- 销售统计

## 接口列表

```
认证接口
POST /api/auth/login      登录
POST /api/auth/register   注册

咖啡接口
GET  /api/coffee          获取列表
GET  /api/coffee/page     分页查询
POST /api/coffee          添加
PUT  /api/coffee/{id}     修改
DELETE /api/coffee/{id}   删除

订单接口
GET  /api/order/page      分页查询
GET  /api/order/pending   待处理订单
POST /api/order           创建订单
POST /api/order/{id}/accept   接单
POST /api/order/{id}/complete 完成

统计接口
GET  /api/stats/dashboard 统计数据
```

## 团队分工

- 成员A - 后端开发
- 成员B - 前端开发
- 成员C - 数据库设计、测试

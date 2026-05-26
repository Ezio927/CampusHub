# CampusHub 前端

Vue 3 + Vite + Pinia + Vue Router，纯 JavaScript（无 TypeScript）。

## 项目结构

```
src/
├── main.js                         # 应用入口，挂载 Vue/Pinia/Router
├── App.vue                         # 根组件
│
├── router/index.js                 # 路由表，URL → 页面组件映射，含路由守卫
│
├── api/                            # 后端接口调用，每个文件对应一个后端 Controller
│   ├── client.js                   #   Axios 实例：baseURL、JWT 拦截、统一错误处理
│   ├── auth.js                     #   /api/auth — 注册、登录、登出
│   ├── users.js                    #   /api/users — 个人信息、信用记录
│   ├── demands.js                  #   /api/demands — 需求发布、浏览、筛选
│   ├── orders.js                   #   /api/orders — 接单、确认、取消
│   ├── reviews.js                  #   /api/reviews — 双向评价
│   ├── notifications.js            #   /api/notifications — 站内通知
│   ├── conversations.js            #   /api/conversations — 即时通讯
│   ├── certifications.js           #   /api/certifications — 实名认证
│   └── admin.js                    #   /api/admin — 后台管理
│
├── stores/                         # Pinia 全局状态，跨页面共享数据
│   ├── auth.js                     #   登录态、JWT、当前用户
│   ├── demands.js                  #   需求列表缓存 + 筛选条件
│   └── notifications.js            #   未读通知计数
│
├── views/                          # 页面组件，一个文件 = 一个路由页面
│   ├── HomeView.vue                #   需求大厅 /（四大板块 + 筛选 + 分页）
│   ├── LoginView.vue               #   登录页 /login
│   ├── RegisterView.vue            #   注册页 /register
│   ├── PublishDemandView.vue       #   发布需求 /demands/publish
│   ├── DemandDetailView.vue        #   需求详情 /demands/:id
│   ├── MyOrdersView.vue            #   我的订单 /orders
│   ├── OrderDetailView.vue         #   订单详情 /orders/:id
│   ├── ChatView.vue                #   聊天页 /chat/:conversationId
│   ├── ProfileView.vue             #   个人中心 /profile
│   ├── ReviewsView.vue             #   评价列表 /reviews/user/:userId
│   └── admin/                      #   管理后台（仅 ADMIN 角色可访问）
│       ├── DashboardView.vue       #     数据看板 /admin
│       ├── UserManageView.vue      #     用户管理 /admin/users
│       ├── CertReviewView.vue      #     认证审核 /admin/certifications
│       └── ArbitrationView.vue     #     仲裁处理 /admin/arbitrations
│
├── components/                     # 可复用 UI 组件（通过 props/emits 与父组件通信）
│   ├── layout/                     #   布局
│   │   ├── AppLayout.vue           #     全局外壳（NavBar + 主体 + TabBar）
│   │   ├── NavBar.vue              #     顶部导航（logo、通知角标、用户菜单）
│   │   └── TabBar.vue              #     底部 tab（移动端）
│   ├── demand/                     #   需求相关
│   │   ├── DemandCard.vue          #     需求卡片（列表项）
│   │   ├── DemandFilter.vue        #     筛选栏（排序、搜索、加急开关）
│   │   └── CategoryTabs.vue        #     四大板块切换标签
│   ├── order/                      #   订单相关
│   │   ├── OrderCard.vue           #     订单卡片
│   │   └── OrderStatusBadge.vue    #     订单状态徽章（颜色+文字）
│   ├── user/                       #   用户相关
│   │   ├── CreditScore.vue         #     信用分展示（颜色分级）
│   │   └── UserAvatar.vue          #     头像（图片/首字母占位）
│   ├── chat/                       #   聊天
│   │   ├── ChatWindow.vue          #     聊天窗口（消息列表 + 输入框）
│   │   └── MessageBubble.vue       #     消息气泡（TEXT/IMAGE/SYSTEM）
│   └── common/                     #   通用
│       ├── StarRating.vue          #     星级评分（只读/可交互）
│       ├── Pagination.vue          #     分页器
│       └── ConfirmModal.vue        #     确认弹窗（危险操作二次确认）
│
├── utils/                          # 纯函数工具，不依赖 Vue 响应式
│   ├── format.js                   #   时间格式化、金额、手机号脱敏
│   └── validators.js               #   表单校验规则（用户名、密码、评价等）
│
└── assets/styles/main.css          # 全局样式 & CSS 变量
```

## 数据流

```
用户操作 → views 组件 → api/ 发请求 → client.js 拦截器自动塞 JWT
                                            │
                                            ▼ HTTP
                                    后端 8080 返回 JSON
                                            │
                                            ▼
                              client.js 拦截器检查 code
                              → code≠20000 弹错误提示
                              → code=20000 返回 data
                                            │
                                            ▼
                              views 拿到数据 → 更新 store / 渲染组件
```

## 开发命令

```sh
npm install          # 安装依赖
npm run dev          # 启动开发服务器（默认 localhost:5173）
npm run build        # 生产构建
npm run test:unit    # 运行单元测试
npm run lint         # ESLint + Oxlint 检查
npm run format       # Prettier 格式化
```

## 与后端通信

开发时 Vite 配了代理，`/api` 开头的请求自动转发到 `localhost:8080`。前端代码里写相对路径即可：

```js
// api/demands.js
client.get('/api/demands', { params })   // → 自动代理到 localhost:8080/api/demands
```

## 接口约定

- 所有响应统一格式：`{ code, message, data }`
- `20000` 成功，`40000+` 客户端错误，`50000+` 服务端错误
- 需鉴权的接口在 Header 携带 `Authorization: Bearer <JWT>`
- 分页参数：`{ page, size }`，响应含 `{ content, totalElements, totalPages }`

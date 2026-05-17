# CampusHub API 规范文档

> 版本：v1.0 | 最后更新：2026-05-17

---

## 一、通用约定

### 1.1 基础信息

| 项目 | 说明 |
|------|------|
| 协议 | HTTPS（生产）/ HTTP（本地开发） |
| 域名 | 生产：`https://campushub.example.com`；本地：`http://localhost:8080` |
| 内容类型 | `application/json; charset=utf-8` |
| 字符编码 | UTF-8 |

### 1.2 统一响应格式

所有接口返回以下 JSON 结构：

```json
{
  "code": 20000,
  "message": "success",
  "data": { }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 业务状态码（见错误码表） |
| message | String | 提示信息 |
| data | Object / null | 实际返回数据，错误时为 null |

### 1.3 认证方式

需鉴权的接口在 HTTP Header 中携带：

```
Authorization: Bearer <JWT_TOKEN>
```

Token 通过登录接口获取，过期后需重新登录。

### 1.4 分页格式

请求参数：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | Integer | 0 | 页码（从 0 开始） |
| size | Integer | 20 | 每页条数 |

响应格式：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

### 1.5 时间格式

所有时间字段使用 ISO 8601 格式：`2026-05-17T14:30:00Z`

### 1.6 命名规范

- URL 路径：小写、复数名词、短横线分隔（`/api/demands`）
- 请求体字段：camelCase（`publisherId`, `createdAt`）
- 响应体字段：camelCase

---

## 二、错误码定义

### 2.1 错误码分段规则

| 范围 | 类别 | 说明 |
|------|------|------|
| 20000 | 成功 | 操作成功 |
| 40000-40099 | 参数错误 | 请求参数缺失、格式不合法 |
| 40100-40199 | 认证错误 | 未登录、Token 过期/无效 |
| 40300-40399 | 授权错误 | 权限不足、信用分不足 |
| 40400-40499 | 资源不存在 | 用户/需求/订单等不存在 |
| 40900-40999 | 业务冲突 | 状态不允许操作、重复操作 |
| 50000-50099 | 服务端错误 | 服务器内部异常 |

### 2.2 具体错误码

| 错误码 | 说明 |
|--------|------|
| 20000 | 操作成功 |
| 40001 | 参数缺失或格式错误 |
| 40002 | 参数值不在允许范围内 |
| 40100 | 未提供认证 Token |
| 40101 | Token 已过期 |
| 40102 | Token 无效 |
| 40300 | 权限不足 |
| 40301 | 信用分不足（< 60），无法接单 |
| 40302 | 未完成实名认证 |
| 40303 | 账号已被封禁 |
| 40400 | 资源不存在 |
| 40401 | 用户不存在 |
| 40402 | 需求不存在 |
| 40403 | 订单不存在 |
| 40900 | 业务状态不允许此操作 |
| 40901 | 需求已被他人接单 |
| 40902 | 订单已取消，不可操作 |
| 40903 | 已提交过评价，不可重复评价 |
| 40904 | 用户名已存在 |
| 40905 | 学号已被注册 |
| 50000 | 服务器内部错误 |

---

## 三、认证模块 — `/api/auth`

### 3.1 用户注册

**POST** `/api/auth/register`

> 无需认证

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 登录名，4-20 位字母/数字/下划线 |
| password | String | 是 | 密码，6-32 位 |
| studentId | String | 是 | 学号，唯一 |
| realName | String | 是 | 真实姓名 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |

请求示例：

```json
{
  "username": "alice",
  "password": "Abc12345",
  "studentId": "2024001001",
  "realName": "张三",
  "phone": "13800138000",
  "email": "alice@smail.nju.edu.cn"
}
```

成功响应：

```json
{
  "code": 20000,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "alice",
    "studentId": "2024001001",
    "realName": "张三",
    "role": "STUDENT",
    "status": "PENDING_CERT",
    "creditScore": 100,
    "createdAt": "2026-05-17T10:00:00Z"
  }
}
```

失败响应：

```json
{
  "code": 40904,
  "message": "用户名已存在",
  "data": null
}
```

---

### 3.2 用户登录

**POST** `/api/auth/login`

> 无需认证

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 登录名 |
| password | String | 是 | 密码 |

请求示例：

```json
{
  "username": "alice",
  "password": "Abc12345"
}
```

成功响应：

```json
{
  "code": 20000,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "expiresAt": "2026-05-18T10:00:00Z",
    "user": {
      "id": 1,
      "username": "alice",
      "role": "STUDENT",
      "status": "ACTIVE",
      "creditScore": 100
    }
  }
}
```

失败响应：

```json
{
  "code": 40100,
  "message": "用户名或密码错误",
  "data": null
}
```

---

### 3.3 用户登出

**POST** `/api/auth/logout`

> 需认证

无请求体。

成功响应：

```json
{
  "code": 20000,
  "message": "已登出",
  "data": null
}
```

---

## 四、实名认证模块 — `/api/certifications`

### 4.1 提交认证申请

**POST** `/api/certifications`

> 需认证

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| certType | String | 是 | 认证类型：`STUDENT_ID` / `STUDENT_CARD` |
| studentId | String | 是 | 学号 |
| cardImageUrl | String | 否 | 学生证照片 URL（certType=STUDENT_CARD 时必填） |

请求示例：

```json
{
  "certType": "STUDENT_ID",
  "studentId": "2024001001"
}
```

成功响应：

```json
{
  "code": 20000,
  "message": "认证申请已提交",
  "data": {
    "id": 1,
    "certType": "STUDENT_ID",
    "status": "PENDING",
    "createdAt": "2026-05-17T10:30:00Z"
  }
}
```

---

### 4.2 查看我的认证状态

**GET** `/api/certifications/me`

> 需认证

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "id": 1,
    "certType": "STUDENT_ID",
    "studentId": "2024001001",
    "status": "APPROVED",
    "reviewedAt": "2026-05-17T14:00:00Z",
    "createdAt": "2026-05-17T10:30:00Z"
  }
}
```

失败响应（未提交认证时 data 为 null）：

```json
{
  "code": 40400,
  "message": "未提交认证申请",
  "data": null
}
```

---

### 4.3 管理员审核认证

**PUT** `/api/certifications/{id}/review`

> 需管理员认证

路径参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 认证申请 ID |

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| approve | Boolean | 是 | 是否通过 |
| reason | String | 否 | 驳回时必填驳回原因 |

请求示例：

```json
{
  "approve": true
}
```

```json
{
  "approve": false,
  "reason": "学生证照片不清晰，请重新上传"
}
```

---

## 五、用户模块 — `/api/users`

### 5.1 获取当前用户信息

**GET** `/api/users/me`

> 需认证

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "id": 1,
    "username": "alice",
    "realName": "张三",
    "studentId": "2024001001",
    "phone": "138****8000",
    "email": "a***e@smail.nju.edu.cn",
    "avatarUrl": "https://example.com/avatars/1.jpg",
    "creditScore": 100,
    "role": "STUDENT",
    "status": "ACTIVE",
    "createdAt": "2026-05-17T10:00:00Z"
  }
}
```

> 注：`phone` 和 `email` 仅在查看自己信息时返回完整值；查看他人时脱敏展示。

---

### 5.2 更新个人资料

**PUT** `/api/users/me`

> 需认证

请求体（所有字段可选，仅更新传入的字段）：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| realName | String | 否 | 真实姓名 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| avatarUrl | String | 否 | 头像地址 |

请求示例：

```json
{
  "phone": "13900139000",
  "email": "newemail@smail.nju.edu.cn"
}
```

成功响应：

```json
{
  "code": 20000,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "alice",
    "realName": "张三",
    "phone": "139****9000",
    "email": "n***l@smail.nju.edu.cn",
    "avatarUrl": "https://example.com/avatars/1.jpg",
    "creditScore": 100
  }
}
```

---

### 5.3 获取用户公开信息

**GET** `/api/users/{id}`

> 需认证

路径参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户 ID |

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "id": 1,
    "username": "alice",
    "avatarUrl": "https://example.com/avatars/1.jpg",
    "creditScore": 100,
    "role": "STUDENT",
    "status": "ACTIVE",
    "completedOrders": 15,
    "averageRating": 4.8
  }
}
```

---

### 5.4 信用变动记录

**GET** `/api/users/me/credit-records`

> 需认证

查询参数：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | Integer | 0 | 页码 |
| size | Integer | 20 | 每页条数 |

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 10,
        "changeAmount": 5,
        "reason": "完成订单加分",
        "relatedOrderId": 42,
        "createdAt": "2026-05-17T16:00:00Z"
      },
      {
        "id": 9,
        "changeAmount": -10,
        "reason": "违约取消扣分",
        "relatedOrderId": 40,
        "createdAt": "2026-05-16T12:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 25,
    "totalPages": 2
  }
}
```

---

## 六、需求模块 — `/api/demands`

### 6.1 发布需求

**POST** `/api/demands`

> 需认证，需完成实名认证

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 需求标题，2-50 字 |
| description | String | 是 | 详细描述，10-500 字 |
| category | String | 是 | `ERRAND` / `SKILL` / `STUDY` / `SECONDHAND` |
| rewardAmount | BigDecimal | 是 | 报酬金额，≥ 0 |
| rewardType | String | 是 | `MONEY` / `POINT` |
| isUrgent | Boolean | 否 | 是否加急，默认 false |
| urgentDeadline | String | 否 | 加急截止时间（isUrgent=true 时必填） |
| locationTag | String | 是 | 位置标签（如"南区快递点""一教""图书馆"） |
| tagIds | Long[] | 否 | 关联标签 ID 列表 |
| imageUrls | String[] | 否 | 需求图片 URL 列表 |

> 按板块的额外校验（由 DemandFactory + Validator 处理）：
> - **ERRAND**：必须填写 locationTag
> - **SKILL**：必须关联技能标签（tagIds 至少 1 个）
> - **STUDY**：必须填写时间段相关信息
> - **SECONDHAND**：必须上传至少 1 张实物图片（imageUrls 非空）

请求示例：

```json
{
  "title": "南区快递点代取快递",
  "description": "今天下午有空帮忙取一下中通快递，送到 5 栋楼下",
  "category": "ERRAND",
  "rewardAmount": 5.00,
  "rewardType": "MONEY",
  "isUrgent": false,
  "locationTag": "南区快递点",
  "tagIds": [1, 3]
}
```

成功响应：

```json
{
  "code": 20000,
  "message": "发布成功",
  "data": {
    "id": 100,
    "title": "南区快递点代取快递",
    "description": "今天下午有空帮忙取一下中通快递，送到 5 栋楼下",
    "category": "ERRAND",
    "rewardAmount": 5.00,
    "rewardType": "MONEY",
    "status": "OPEN",
    "isUrgent": false,
    "urgentDeadline": null,
    "locationTag": "南区快递点",
    "publisher": {
      "id": 1,
      "username": "alice",
      "creditScore": 100
    },
    "tags": [
      { "id": 1, "name": "南区快递点" },
      { "id": 3, "name": "代取快递" }
    ],
    "images": [],
    "createdAt": "2026-05-17T11:00:00Z"
  }
}
```

失败响应：

```json
{
  "code": 40302,
  "message": "请先完成实名认证后再发布需求",
  "data": null
}
```

---

### 6.2 浏览需求列表（含筛选）

**GET** `/api/demands`

> 需认证

查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| category | String | 否 | — | 板块筛选：`ERRAND` / `SKILL` / `STUDY` / `SECONDHAND` |
| status | String | 否 | — | 状态筛选：`OPEN` / `ACCEPTED` / `IN_PROGRESS` |
| keyword | String | 否 | — | 标题/描述模糊搜索 |
| locationTag | String | 否 | — | 位置筛选 |
| isUrgent | Boolean | 否 | — | 只看加急需求 |
| sortBy | String | 否 | `createdAt` | 排序字段：`createdAt` / `rewardAmount` / `urgentDeadline` |
| sortDir | String | 否 | `desc` | 排序方向：`asc` / `desc` |
| page | Integer | 否 | 0 | 页码 |
| size | Integer | 否 | 20 | 每页条数 |

请求示例：

```
GET /api/demands?category=ERRAND&status=OPEN&locationTag=南区快递点&sortBy=createdAt&sortDir=desc&page=0&size=20
```

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 100,
        "title": "南区快递点代取快递",
        "category": "ERRAND",
        "rewardAmount": 5.00,
        "rewardType": "MONEY",
        "status": "OPEN",
        "isUrgent": false,
        "locationTag": "南区快递点",
        "publisher": {
          "id": 1,
          "username": "alice",
          "creditScore": 100
        },
        "createdAt": "2026-05-17T11:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 42,
    "totalPages": 3
  }
}
```

---

### 6.3 获取需求详情

**GET** `/api/demands/{id}`

> 需认证

路径参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 需求 ID |

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "id": 100,
    "title": "南区快递点代取快递",
    "description": "今天下午有空帮忙取一下中通快递，送到 5 栋楼下",
    "category": "ERRAND",
    "rewardAmount": 5.00,
    "rewardType": "MONEY",
    "status": "OPEN",
    "isUrgent": false,
    "urgentDeadline": null,
    "locationTag": "南区快递点",
    "publisher": {
      "id": 1,
      "username": "alice",
      "creditScore": 100,
      "completedOrders": 15,
      "averageRating": 4.8
    },
    "tags": [
      { "id": 1, "name": "南区快递点" },
      { "id": 3, "name": "代取快递" }
    ],
    "images": [],
    "createdAt": "2026-05-17T11:00:00Z",
    "updatedAt": "2026-05-17T11:00:00Z"
  }
}
```

失败响应：

```json
{
  "code": 40402,
  "message": "需求不存在",
  "data": null
}
```

---

### 6.4 编辑需求

**PUT** `/api/demands/{id}`

> 需认证，仅需求发布者可编辑，仅 OPEN 状态可编辑

路径参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 需求 ID |

请求体（仅传入需修改的字段）：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 否 | 需求标题 |
| description | String | 否 | 详细描述 |
| rewardAmount | BigDecimal | 否 | 报酬金额 |
| locationTag | String | 否 | 位置标签 |
| isUrgent | Boolean | 否 | 是否加急 |
| urgentDeadline | String | 否 | 加急截止时间 |

成功响应：同 6.3 需求详情（含更新后数据）

---

### 6.5 取消需求

**DELETE** `/api/demands/{id}`

> 需认证，仅需求发布者可取消，仅 OPEN 状态可取消

路径参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 需求 ID |

成功响应：

```json
{
  "code": 20000,
  "message": "需求已取消",
  "data": null
}
```

失败响应：

```json
{
  "code": 40900,
  "message": "当前状态不允许取消",
  "data": null
}
```

---

## 七、订单模块 — `/api/orders`

### 7.1 接单

**POST** `/api/orders`

> 需认证，需完成实名认证，信用分 ≥ 60

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| demandId | Long | 是 | 要接的需求 ID |

请求示例：

```json
{
  "demandId": 100
}
```

成功响应：

```json
{
  "code": 20000,
  "message": "接单成功",
  "data": {
    "id": 42,
    "demandId": 100,
    "demandTitle": "南区快递点代取快递",
    "publisher": {
      "id": 1,
      "username": "alice"
    },
    "acceptor": {
      "id": 2,
      "username": "bob"
    },
    "status": "ACCEPTED",
    "acceptedAt": "2026-05-17T11:30:00Z",
    "autoConfirmDeadline": "2026-05-18T11:30:00Z"
  }
}
```

失败响应：

```json
{
  "code": 40901,
  "message": "该需求已被他人接单",
  "data": null
}
```

```json
{
  "code": 40301,
  "message": "信用分不足，无法接单",
  "data": null
}
```

---

### 7.2 我的订单列表

**GET** `/api/orders`

> 需认证

查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| role | String | 否 | — | 角色筛选：`PUBLISHER` / `ACCEPTOR` |
| status | String | 否 | — | 状态筛选：`PENDING_ACCEPT` / `ACCEPTED` / `IN_PROGRESS` / `PENDING_CONFIRM` / `COMPLETED` / `CANCELLED` |
| page | Integer | 否 | 0 | 页码 |
| size | Integer | 否 | 20 | 每页条数 |

请求示例：

```
GET /api/orders?role=ACCEPTOR&status=IN_PROGRESS&page=0&size=20
```

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 42,
        "demandId": 100,
        "demandTitle": "南区快递点代取快递",
        "publisher": { "id": 1, "username": "alice" },
        "acceptor": { "id": 2, "username": "bob" },
        "status": "IN_PROGRESS",
        "acceptedAt": "2026-05-17T11:30:00Z",
        "autoConfirmDeadline": "2026-05-18T11:30:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

---

### 7.3 订单详情

**GET** `/api/orders/{id}`

> 需认证，仅订单参与方（需求方/服务方）可查看

路径参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单 ID |

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "id": 42,
    "demandId": 100,
    "demandTitle": "南区快递点代取快递",
    "demandDescription": "今天下午有空帮忙取一下中通快递，送到 5 栋楼下",
    "category": "ERRAND",
    "rewardAmount": 5.00,
    "rewardType": "MONEY",
    "publisher": {
      "id": 1,
      "username": "alice",
      "phone": "138****8000",
      "creditScore": 100
    },
    "acceptor": {
      "id": 2,
      "username": "bob",
      "phone": "139****9000",
      "creditScore": 98
    },
    "status": "IN_PROGRESS",
    "acceptedAt": "2026-05-17T11:30:00Z",
    "autoConfirmDeadline": "2026-05-18T11:30:00Z",
    "statusLogs": [
      {
        "fromStatus": "PENDING_ACCEPT",
        "toStatus": "ACCEPTED",
        "operatorId": 2,
        "remark": null,
        "createdAt": "2026-05-17T11:30:00Z"
      }
    ]
  }
}
```

> 注：`phone` 字段仅在接单后对对方可见，订单完成后自动恢复隐藏。

---

### 7.4 服务方标记已送达

**PUT** `/api/orders/{id}/deliver`

> 需认证，仅服务方可操作，仅 IN_PROGRESS 状态可操作

路径参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单 ID |

无请求体。

成功响应：

```json
{
  "code": 20000,
  "message": "已标记完成，等待需求方确认",
  "data": {
    "id": 42,
    "status": "PENDING_CONFIRM",
    "updatedAt": "2026-05-17T12:00:00Z"
  }
}
```

---

### 7.5 需求方确认验收

**PUT** `/api/orders/{id}/confirm`

> 需认证，仅需求方可操作，仅 PENDING_CONFIRM 状态可操作

路径参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单 ID |

无请求体。

成功响应：

```json
{
  "code": 20000,
  "message": "已确认完成，请对服务方进行评价",
  "data": {
    "id": 42,
    "status": "COMPLETED",
    "completedAt": "2026-05-17T12:10:00Z"
  }
}
```

---

### 7.6 取消订单

**PUT** `/api/orders/{id}/cancel`

> 需认证，需为订单参与方

路径参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单 ID |

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | String | 是 | 取消原因 |

请求示例：

```json
{
  "reason": "临时有事无法完成"
}
```

成功响应（10 分钟内无责取消）：

```json
{
  "code": 20000,
  "message": "已取消（无责取消，信用分不受影响）",
  "data": {
    "id": 42,
    "status": "CANCELLED",
    "cancelledAt": "2026-05-17T11:35:00Z"
  }
}
```

成功响应（超时取消，扣信用分）：

```json
{
  "code": 20000,
  "message": "已取消（超时取消，扣除信用分 10 分）",
  "data": {
    "id": 42,
    "status": "CANCELLED",
    "cancelledAt": "2026-05-17T13:00:00Z",
    "creditPenalty": -10
  }
}
```

---

## 八、评价模块 — `/api/reviews`

### 8.1 提交评价

**POST** `/api/reviews`

> 需认证，仅订单参与方可评价，每个订单每人限评 1 次

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | Long | 是 | 订单 ID |
| rating | Integer | 是 | 评分 1-5 |
| content | String | 条件必填 | 评价内容，≤3 星时必填 ≥ 15 字 |
| imageUrls | String[] | 条件必填 | 截图 URL，≤3 星时至少 1 张 |

请求示例（5 星好评）：

```json
{
  "orderId": 42,
  "rating": 5,
  "content": "接单很快，态度也很好！"
}
```

请求示例（2 星差评，触发低分举证）：

```json
{
  "orderId": 42,
  "rating": 2,
  "content": "接单后迟迟不处理，快递等了三个小时才拿到，中间也联系不上人",
  "imageUrls": ["https://example.com/evidence/chat1.png", "https://example.com/evidence/chat2.png"]
}
```

成功响应：

```json
{
  "code": 20000,
  "message": "评价已提交",
  "data": {
    "id": 10,
    "orderId": 42,
    "reviewerId": 1,
    "revieweeId": 2,
    "reviewerRole": "PUBLISHER",
    "rating": 5,
    "content": "接单很快，态度也很好！",
    "imageUrls": [],
    "createdAt": "2026-05-17T12:15:00Z"
  }
}
```

失败响应（重复评价）：

```json
{
  "code": 40903,
  "message": "您已对此订单提交过评价",
  "data": null
}
```

---

### 8.2 查看用户收到的评价

**GET** `/api/reviews/user/{userId}`

> 需认证

路径参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户 ID |

查询参数：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | Integer | 0 | 页码 |
| size | Integer | 20 | 每页条数 |

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 10,
        "orderId": 42,
        "reviewer": { "id": 1, "username": "alice" },
        "reviewerRole": "PUBLISHER",
        "rating": 5,
        "content": "接单很快，态度也很好！",
        "createdAt": "2026-05-17T12:15:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  }
}
```

---

### 8.3 查看某订单的评价

**GET** `/api/reviews/order/{orderId}`

> 需认证，仅订单参与方可查看

路径参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| orderId | Long | 订单 ID |

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": [
    {
      "id": 10,
      "orderId": 42,
      "reviewer": { "id": 1, "username": "alice" },
      "reviewee": { "id": 2, "username": "bob" },
      "reviewerRole": "PUBLISHER",
      "rating": 5,
      "content": "接单很快，态度也很好！",
      "createdAt": "2026-05-17T12:15:00Z"
    }
  ]
}
```

---

## 九、仲裁模块 — `/api/arbitrations`

### 9.1 创建仲裁工单

**POST** `/api/arbitrations`

> 需认证。低分评价（≤3 星）提交后系统自动调用；用户也可手动发起申诉。

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reviewId | Long | 否 | 关联评价 ID（低分评价时传入） |
| orderId | Long | 是 | 关联订单 ID |
| reason | String | 是 | 申诉原因 |
| evidenceUrls | String[] | 否 | 证据截图 URL |

请求示例：

```json
{
  "orderId": 42,
  "reason": "服务方未按约定完成，且沟通中态度恶劣",
  "evidenceUrls": ["https://example.com/evidence/1.png"]
}
```

成功响应：

```json
{
  "code": 20000,
  "message": "仲裁工单已创建",
  "data": {
    "id": 5,
    "reviewId": null,
    "orderId": 42,
    "reporterId": 1,
    "reason": "服务方未按约定完成，且沟通中态度恶劣",
    "status": "PENDING",
    "createdAt": "2026-05-17T13:00:00Z"
  }
}
```

---

### 9.2 待处理工单列表

**GET** `/api/arbitrations`

> 需管理员认证

查询参数：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| status | String | 否 | 状态筛选：`PENDING` / `PROCESSING` |
| page | Integer | 0 | 页码 |
| size | Integer | 20 | 每页条数 |

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 5,
        "orderId": 42,
        "reporter": { "id": 1, "username": "alice" },
        "reason": "服务方未按约定完成",
        "status": "PENDING",
        "createdAt": "2026-05-17T13:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 3,
    "totalPages": 1
  }
}
```

---

### 9.3 处理工单

**PUT** `/api/arbitrations/{id}/resolve`

> 需管理员认证

路径参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 工单 ID |

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| resolution | String | 是 | 处理结果说明 |
| action | String | 是 | 处理措施：`DISMISS`（驳回）/ `PENALIZE_PUBLISHER` / `PENALIZE_ACCEPTOR` |
| creditPenalty | Integer | 否 | 信用扣分（action 为罚则时必填） |
| banDays | Integer | 否 | 限制接单天数（action 为罚则时可选） |

请求示例：

```json
{
  "resolution": "经核实，服务方确实未按约定时间完成，扣除信用分 10 分",
  "action": "PENALIZE_ACCEPTOR",
  "creditPenalty": 10
}
```

---

## 十、通知模块 — `/api/notifications`

### 10.1 通知列表

**GET** `/api/notifications`

> 需认证

查询参数：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | Integer | 0 | 页码 |
| size | Integer | 20 | 每页条数 |

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 50,
        "type": "ORDER_ACCEPTED",
        "title": "您的需求已被接单",
        "content": "用户 bob 已接下您的需求「南区快递点代取快递」",
        "relatedId": 42,
        "isRead": false,
        "createdAt": "2026-05-17T11:30:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 12,
    "totalPages": 1
  }
}
```

---

### 10.2 未读数量

**GET** `/api/notifications/unread-count`

> 需认证

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "unreadCount": 3
  }
}
```

---

### 10.3 标记已读

**PUT** `/api/notifications/{id}/read`

> 需认证，仅通知接收者可操作

---

### 10.4 全部标记已读

**PUT** `/api/notifications/read-all`

> 需认证

成功响应：

```json
{
  "code": 20000,
  "message": "已全部标记为已读",
  "data": null
}
```

---

## 十一、即时通讯模块 — `/api/conversations`（P2）

### 11.1 获取或创建会话

**POST** `/api/conversations`

> 需认证

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetUserId | Long | 是 | 对方用户 ID |
| demandId | Long | 否 | 关联需求 ID |

请求示例：

```json
{
  "targetUserId": 2,
  "demandId": 100
}
```

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "id": 10,
    "user1": { "id": 1, "username": "alice" },
    "user2": { "id": 2, "username": "bob" },
    "relatedDemandId": 100,
    "lastMessageContent": null,
    "lastMessageAt": null,
    "createdAt": "2026-05-17T11:30:00Z"
  }
}
```

---

### 11.2 会话列表

**GET** `/api/conversations`

> 需认证

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": [
    {
      "id": 10,
      "targetUser": { "id": 2, "username": "bob", "avatarUrl": "..." },
      "relatedDemandId": 100,
      "lastMessageContent": "好的，我马上到",
      "lastMessageAt": "2026-05-17T11:35:00Z",
      "unreadCount": 1,
      "createdAt": "2026-05-17T11:30:00Z"
    }
  ]
}
```

---

### 11.3 发送消息

**POST** `/api/conversations/{id}/messages`

> 需认证

路径参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 会话 ID |

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | String | 是 | 消息内容 |
| messageType | String | 否 | `TEXT`（默认）/ `IMAGE` |

请求示例：

```json
{
  "content": "我到了，你在哪？",
  "messageType": "TEXT"
}
```

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "id": 200,
    "conversationId": 10,
    "senderId": 1,
    "content": "我到了，你在哪？",
    "messageType": "TEXT",
    "isRead": false,
    "createdAt": "2026-05-17T11:40:00Z"
  }
}
```

---

### 11.4 获取历史消息

**GET** `/api/conversations/{id}/messages`

> 需认证

路径参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 会话 ID |

查询参数：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | Integer | 0 | 页码 |
| size | Integer | 50 | 每页条数 |

---

## 十二、管理后台模块 — `/api/admin`（P2）

### 12.1 数据看板

**GET** `/api/admin/dashboard`

> 需管理员认证

成功响应：

```json
{
  "code": 20000,
  "message": "success",
  "data": {
    "totalUsers": 1200,
    "activeDemands": 85,
    "completedOrders": 3420,
    "averageRating": 4.6,
    "pendingArbitrations": 5
  }
}
```

---

### 12.2 用户管理列表

**GET** `/api/admin/users`

> 需管理员认证

查询参数：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| keyword | String | 否 | 用户名/学号模糊搜索 |
| status | String | 否 | 状态筛选：`ACTIVE` / `BANNED` / `PENDING_CERT` |
| page | Integer | 0 | 页码 |
| size | Integer | 20 | 每页条数 |

---

### 12.3 封禁用户

**PUT** `/api/admin/users/{id}/ban`

> 需管理员认证

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | String | 是 | 封禁原因 |

---

### 12.4 解封用户

**PUT** `/api/admin/users/{id}/unban`

> 需管理员认证

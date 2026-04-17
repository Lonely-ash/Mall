# mall-front

Vue 3 + Vite 商城前端，已对接后端接口：

- 登录：`POST /users/login`
- 注册：`POST /users/register`（后端若未实现会报错提示）
- 首页商品：`GET /items/page`
- 搜索：`GET /search/list`
- 购物车：`GET/POST/PUT/DELETE /carts`
- 地址：`GET /addresses`
- 下单：`POST /orders`
- 支付：`POST /pay-orders`、`POST /pay-orders/{id}`

## 运行

```bash
npm install
npm run dev
```

默认前端端口 `5173`，并通过 Vite 代理到网关 `http://localhost:8083`。

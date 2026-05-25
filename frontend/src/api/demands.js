/**
 * 需求接口 — 对应后端 DemandController (/api/demands)
 *
 * 对外接口：
 *   publish(data)         → POST /api/demands
 *   search(params)        → GET  /api/demands?category=&status=&keyword=&page=&size=
 *   getDetail(id)         → GET  /api/demands/{id}
 *   update(id, data)      → PUT  /api/demands/{id}
 *   cancel(id)            → PUT  /api/demands/{id}/cancel
 *
 * 待完成：
 *   - search 用于 HomeView 需求大厅列表 + 筛选 + 分页
 *   - publish 板块校验：ERRAND→必填 locationTag, SECONDHAND→必填 imageUrls
 *   - 处理 40302（未实名认证不可发布）
 */

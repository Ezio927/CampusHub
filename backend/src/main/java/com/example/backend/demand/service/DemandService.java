/**
 * 需求服务接口
 *
 * 对外接口：
 *   DemandDto createDemand(Long userId, DemandCreateDto req)
 *                        — 发布需求（经 DemandFactory 校验）
 *   Page<DemandDto> search(DemandSearchCriteria criteria, Pageable pageable)
 *                        — 多条件筛选 + 分页 + 排序
 *   DemandDto getDetail(Long demandId)
 *                        — 需求详情（含发布者信息 + 标签 + 图片）
 *   DemandDto update(Long demandId, Long userId, DemandUpdateDto req)
 *                        — 编辑（仅发布者 + OPEN 状态）
 *   void cancel(Long demandId, Long userId)
 *                        — 取消（仅发布者 + OPEN 状态）
 *
 * 约束：创建需求时通过 DemandFactory 匹配校验规则，不直接在 Service 内 switch
 */

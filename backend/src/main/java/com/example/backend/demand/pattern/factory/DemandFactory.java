/**
 * 需求工厂（工厂模式）
 *
 * 职责：维护 DemandCategory → DemandValidator 的 Map 映射，
 *       接收 DemandCreateDto，根据 category 选择合适的 Validator 校验，
 *       校验通过后构建 Demand 实体返回
 *
 * 对外接口：
 *   Demand createDemand(DemandCreateDto param)
 *
 * 扩展性：新增板块只需新增一个 Validator 实现类并注册到 Map，工厂和 Service 代码不动
 */

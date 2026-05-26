/**
 * 需求校验器接口（工厂模式）
 *
 * 不同板块的需求有不同的校验规则，各自实现本接口
 *
 * 对外接口：
 *   void validate(DemandCreateDto param) — 校验不通过抛 BusinessException
 */

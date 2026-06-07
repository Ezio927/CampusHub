package com.example.backend.demand.pattern.factory;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.DemandCategory;
import com.example.backend.demand.dto.DemandCreateDto;
import com.example.backend.demand.entity.Demand;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class DemandFactory {

    private final Map<DemandCategory, DemandValidator> validatorMap = new EnumMap<DemandCategory, DemandValidator>(DemandCategory.class);

    public DemandFactory(ErrandValidator errandValidator,
                         SkillValidator skillValidator,
                         StudyValidator studyValidator,
                         SecondhandValidator secondhandValidator,
                         OtherValidator otherValidator) {
        validatorMap.put(DemandCategory.ERRAND, errandValidator);
        validatorMap.put(DemandCategory.SKILL, skillValidator);
        validatorMap.put(DemandCategory.STUDY, studyValidator);
        validatorMap.put(DemandCategory.SECONDHAND, secondhandValidator);
        validatorMap.put(DemandCategory.OTHER, otherValidator);
    }

    public Demand createDemand(DemandCreateDto param) {
        if (param == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请求不能为空");
        }
        validateCommonFields(param);
        if (param.getCategory() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "需求分类不能为空");
        }
        DemandValidator validator = validatorMap.get(param.getCategory());
        if (validator == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的需求分类");
        }
        validator.validate(param);

        Demand demand = new Demand();
        demand.setTitle(param.getTitle());
        demand.setDescription(param.getDescription());
        demand.setCategory(param.getCategory());
        demand.setRewardAmount(param.getRewardAmount());
        demand.setRewardType(param.getRewardType());
        demand.setUrgent(Boolean.TRUE.equals(param.getUrgent()));
        demand.setUrgentDeadline(param.getUrgentDeadline());
        demand.setLocationTag(param.getLocationTag());
        demand.setCampus(isBlank(param.getCampus()) ? null : param.getCampus().trim());
        return demand;
    }

    private void validateCommonFields(DemandCreateDto param) {
        if (isBlank(param.getTitle())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "标题不能为空");
        }
        if (isBlank(param.getDescription())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "详细描述不能为空");
        }
        if (param.getRewardAmount() == null || param.getRewardAmount() < 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "报酬金额不能为负数");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

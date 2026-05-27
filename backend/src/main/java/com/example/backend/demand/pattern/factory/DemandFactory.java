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
                         SecondhandValidator secondhandValidator) {
        validatorMap.put(DemandCategory.ERRAND, errandValidator);
        validatorMap.put(DemandCategory.SKILL, skillValidator);
        validatorMap.put(DemandCategory.STUDY, studyValidator);
        validatorMap.put(DemandCategory.SECONDHAND, secondhandValidator);
    }

    public Demand createDemand(DemandCreateDto param) {
        if (param == null || param.getCategory() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "category is required");
        }
        DemandValidator validator = validatorMap.get(param.getCategory());
        if (validator == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "unsupported category");
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
        return demand;
    }
}

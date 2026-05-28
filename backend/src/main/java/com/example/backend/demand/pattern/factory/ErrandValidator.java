package com.example.backend.demand.pattern.factory;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.demand.dto.DemandCreateDto;
import org.springframework.stereotype.Component;

@Component
public class ErrandValidator implements DemandValidator {

    @Override
    public void validate(DemandCreateDto param) {
        if (param == null || isBlank(param.getLocationTag())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "跑腿需求必须填写位置");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

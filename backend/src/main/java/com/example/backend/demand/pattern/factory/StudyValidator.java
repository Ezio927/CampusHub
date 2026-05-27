package com.example.backend.demand.pattern.factory;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.demand.dto.DemandCreateDto;
import org.springframework.stereotype.Component;

@Component
public class StudyValidator implements DemandValidator {

    @Override
    public void validate(DemandCreateDto param) {
        if (param == null || param.getUrgentDeadline() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "study demand requires urgentDeadline");
        }
    }
}

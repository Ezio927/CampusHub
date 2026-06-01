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
            throw new BusinessException(ErrorCode.BAD_REQUEST, "学习需求必须设置截止时间");
        }
        if (param.getUrgentDeadline().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "学习需求的截止时间必须在未来");
        }
    }
}

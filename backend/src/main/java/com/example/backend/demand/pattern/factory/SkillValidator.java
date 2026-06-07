package com.example.backend.demand.pattern.factory;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.demand.dto.DemandCreateDto;
import org.springframework.stereotype.Component;

@Component
public class SkillValidator implements DemandValidator {

    @Override
    public void validate(DemandCreateDto param) {
    }
}

package com.example.backend.demand.pattern.factory;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.demand.dto.DemandCreateDto;
import org.springframework.stereotype.Component;

@Component
public class SecondhandValidator implements DemandValidator {

    @Override
    public void validate(DemandCreateDto param) {
        if (param == null || param.getImageUrls() == null || param.getImageUrls().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "二手需求必须上传至少一张图片");
        }
    }
}

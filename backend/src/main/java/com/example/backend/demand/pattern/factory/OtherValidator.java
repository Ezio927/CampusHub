package com.example.backend.demand.pattern.factory;

import com.example.backend.demand.dto.DemandCreateDto;
import org.springframework.stereotype.Component;

@Component
public class OtherValidator implements DemandValidator {

    @Override
    public void validate(DemandCreateDto param) {
    }
}

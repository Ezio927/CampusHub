package com.example.backend.demand.pattern.factory;

import com.example.backend.demand.dto.DemandCreateDto;

public interface DemandValidator {

    void validate(DemandCreateDto param);
}

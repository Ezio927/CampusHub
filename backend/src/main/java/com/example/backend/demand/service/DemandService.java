package com.example.backend.demand.service;

import com.example.backend.demand.dto.DemandCreateDto;
import com.example.backend.demand.dto.DemandDto;
import com.example.backend.demand.dto.DemandSearchCriteria;
import com.example.backend.demand.dto.DemandUpdateDto;
import com.example.backend.common.enums.DemandStatus;

import java.util.List;

public interface DemandService {

    DemandDto createDemand(Long userId, DemandCreateDto req);

    List<DemandDto> search(DemandSearchCriteria criteria);

    DemandDto getDetail(Long demandId);

    default void updateStatus(Long demandId, DemandStatus status) {
        throw new UnsupportedOperationException("updateStatus is not implemented");
    }

    DemandDto update(Long demandId, Long userId, DemandUpdateDto req);

    void cancel(Long demandId, Long userId);
}

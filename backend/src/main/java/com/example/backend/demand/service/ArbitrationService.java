package com.example.backend.demand.service;

import com.example.backend.demand.dto.ArbitrationCreateDto;
import com.example.backend.demand.dto.ArbitrationDto;
import com.example.backend.demand.dto.ArbitrationResolveDto;

import java.util.List;

public interface ArbitrationService {

    ArbitrationDto createTicket(Long reporterId, Long orderId, Long reviewId, String reason, List<String> evidenceUrls);

    ArbitrationDto createTicket(Long reporterId, ArbitrationCreateDto req);

    List<ArbitrationDto> listMine(Long reporterId);

    List<ArbitrationDto> listPending();

    ArbitrationDto resolve(Long ticketId, Long adminId, ArbitrationResolveDto req);
}

package com.example.backend.user.service;

import com.example.backend.user.dto.CertificationDto;
import com.example.backend.user.dto.CertificationSubmitDto;

import java.util.List;

public interface CertificationService {

    CertificationDto submit(Long userId, CertificationSubmitDto req);

    CertificationDto getMyCertification(Long userId);

    CertificationDto review(Long id, boolean approve, String reason, Long reviewerId);

    List<CertificationDto> listPending();
}

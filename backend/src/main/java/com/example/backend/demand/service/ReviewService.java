package com.example.backend.demand.service;

import com.example.backend.demand.dto.ReviewDto;
import com.example.backend.demand.dto.ReviewSubmitDto;

import java.util.List;

public interface ReviewService {

    ReviewDto submitReview(Long userId, ReviewSubmitDto req);

    List<ReviewDto> listByUser(Long userId);

    List<ReviewDto> listByOrder(Long orderId, Long userId);
}

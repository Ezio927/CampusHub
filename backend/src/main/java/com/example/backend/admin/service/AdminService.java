package com.example.backend.admin.service;

import com.example.backend.admin.dto.AdminUserDto;
import com.example.backend.admin.dto.AdminOrderManageDto;
import com.example.backend.admin.dto.BanUserDto;
import com.example.backend.admin.dto.DashboardDto;
import com.example.backend.admin.dto.SendNotificationRequest;
import com.example.backend.admin.dto.RatingUpdateDto;
import com.example.backend.demand.dto.ArbitrationDto;
import com.example.backend.demand.dto.ArbitrationResolveDto;
import com.example.backend.user.dto.CertificationDto;
import com.example.backend.user.dto.CertificationReviewDto;

import java.util.List;

public interface AdminService {

    DashboardDto getDashboard();

    List<AdminUserDto> listUsers(String keyword, String status);

    List<AdminOrderManageDto> listOrders(String status);

    void banUser(Long userId, Long adminId, BanUserDto req);

    void unbanUser(Long userId, Long adminId);

    AdminUserDto updateUserRating(Long userId, Long adminId, RatingUpdateDto req);

    CertificationDto reviewCertification(Long certificationId, Long adminId, CertificationReviewDto req);

    ArbitrationDto resolveArbitration(Long ticketId, Long adminId, ArbitrationResolveDto req);

    void deleteOrder(Long orderId, Long adminId);

    void deleteDemand(Long demandId, Long adminId);

    void sendNotification(SendNotificationRequest request, Long adminId);
}

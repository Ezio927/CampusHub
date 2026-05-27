package com.example.backend.admin.service;

import com.example.backend.admin.dto.AdminUserDto;
import com.example.backend.admin.dto.BanUserDto;
import com.example.backend.admin.dto.DashboardDto;

import java.util.List;

public interface AdminService {

    DashboardDto getDashboard();

    List<AdminUserDto> listUsers(String keyword, String status);

    void banUser(Long userId, Long adminId, BanUserDto req);

    void unbanUser(Long userId, Long adminId);
}

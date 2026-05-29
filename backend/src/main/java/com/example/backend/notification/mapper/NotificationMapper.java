package com.example.backend.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.notification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}

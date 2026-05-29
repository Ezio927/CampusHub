package com.example.backend.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.admin.entity.AdminOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLog> {
}

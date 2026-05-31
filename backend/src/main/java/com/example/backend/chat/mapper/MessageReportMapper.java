package com.example.backend.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.chat.entity.MessageReport;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageReportMapper extends BaseMapper<MessageReport> {
}

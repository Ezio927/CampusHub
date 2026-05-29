package com.example.backend.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.chat.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}

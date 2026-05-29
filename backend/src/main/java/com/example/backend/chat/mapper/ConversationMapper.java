package com.example.backend.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.chat.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}

package com.example.backend.demand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.demand.entity.OrderStatusLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderStatusLogMapper extends BaseMapper<OrderStatusLog> {
}

package com.example.backend.demand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.demand.entity.Demand;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemandMapper extends BaseMapper<Demand> {
}

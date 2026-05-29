package com.example.backend.demand.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.common.enums.ArbitrationStatus;
import com.example.backend.demand.entity.ArbitrationTicket;
import com.example.backend.demand.mapper.ArbitrationTicketMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ArbitrationTicketRepository {

    private final ArbitrationTicketMapper mapper;

    public ArbitrationTicketRepository(ArbitrationTicketMapper mapper) {
        this.mapper = mapper;
    }

    public ArbitrationTicket save(ArbitrationTicket t) {
        if (t.getCreatedAt() == null) t.setCreatedAt(LocalDateTime.now());
        if (t.getId() == null) {
            mapper.insert(t);
        } else {
            mapper.updateById(t);
        }
        return t;
    }

    public Optional<ArbitrationTicket> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public List<ArbitrationTicket> findByStatusOrderByCreatedAtAsc(ArbitrationStatus status) {
        return mapper.selectList(
                new LambdaQueryWrapper<ArbitrationTicket>()
                        .eq(ArbitrationTicket::getStatus, status)
                        .orderByAsc(ArbitrationTicket::getCreatedAt));
    }

    public List<ArbitrationTicket> findAll() {
        return mapper.selectList(null);
    }
}

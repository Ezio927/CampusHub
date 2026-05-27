package com.example.backend.demand.repository;

import com.example.backend.common.enums.ArbitrationStatus;
import com.example.backend.demand.entity.ArbitrationTicket;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ArbitrationTicketRepository {

    private final ConcurrentHashMap<Long, ArbitrationTicket> storage = new ConcurrentHashMap<Long, ArbitrationTicket>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public synchronized ArbitrationTicket save(ArbitrationTicket ticket) {
        if (ticket.getId() == null) {
            ticket.setId(idGenerator.getAndIncrement());
        }
        if (ticket.getCreatedAt() == null) {
            ticket.setCreatedAt(LocalDateTime.now());
        }
        storage.put(ticket.getId(), ticket);
        return ticket;
    }

    public Optional<ArbitrationTicket> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<ArbitrationTicket> findByStatusOrderByCreatedAtAsc(ArbitrationStatus status) {
        return storage.values().stream()
                .filter(ticket -> ticket.getStatus() == status)
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<ArbitrationTicket> findAll() {
        return Collections.unmodifiableList(new ArrayList<ArbitrationTicket>(storage.values()));
    }
}

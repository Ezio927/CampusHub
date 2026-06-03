package com.example.backend.demand.service.impl;

import com.example.backend.common.PageResult;
import com.example.backend.common.enums.DemandCategory;
import com.example.backend.demand.dto.DemandDto;
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.entity.Order;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.demand.repository.OrderRepository;
import com.example.backend.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
class RecommendationService {

    private final DemandRepository demandRepository;
    private final OrderRepository orderRepository;

    RecommendationService(DemandRepository demandRepository, OrderRepository orderRepository) {
        this.demandRepository = demandRepository;
        this.orderRepository = orderRepository;
    }

    PageResult<DemandDto> recommend(User user, int page, int size) {
        Map<DemandCategory, Double> categoryPref = buildCategoryPreference(user);
        DemandScorer scorer = new DemandScorer(categoryPref, user);

        List<Demand> openDemands = demandRepository.findAllOpen();

        Map<Long, Double> scores = new HashMap<>();
        for (Demand d : openDemands) {
            scores.put(d.getId(), scorer.score(d));
        }

        List<Demand> sorted = openDemands.stream()
                .sorted(Comparator.comparingDouble((Demand d) -> scores.getOrDefault(d.getId(), 0.0)).reversed())
                .collect(Collectors.toList());

        sorted = applyDiversity(sorted);

        List<DemandDto> dtos = sorted.stream()
                .map(DemandDto::from)
                .collect(Collectors.toList());

        int total = dtos.size();
        int from = page * size;
        int to = Math.min(from + size, total);
        List<DemandDto> pageContent = from < total ? dtos.subList(from, to) : new ArrayList<>();
        return PageResult.of(pageContent, page, size, total);
    }

    private Map<DemandCategory, Double> buildCategoryPreference(User user) {
        List<Order> orders = orderRepository.findByAcceptorId(user.getId());
        Map<DemandCategory, Integer> counts = new HashMap<>();
        for (Order order : orders) {
            Long demandId = order.getDemandId();
            if (demandId == null) continue;
            demandRepository.findById(demandId).ifPresent(d -> {
                if (d.getCategory() != null) {
                    counts.merge(d.getCategory(), 1, Integer::sum);
                }
            });
        }
        if (counts.isEmpty()) return Map.of();
        Map<DemandCategory, Double> pref = new HashMap<>();
        double maxLog = 0;
        for (int count : counts.values()) {
            double v = Math.log(count + 1) / Math.log(2);
            if (v > maxLog) maxLog = v;
        }
        for (Map.Entry<DemandCategory, Integer> e : counts.entrySet()) {
            double v = Math.log(e.getValue() + 1) / Math.log(2);
            pref.put(e.getKey(), maxLog > 0 ? v / maxLog : 0);
        }
        return pref;
    }

    private List<Demand> applyDiversity(List<Demand> ranked) {
        if (ranked.size() <= 3) return ranked;
        List<Demand> result = new ArrayList<>();
        Map<DemandCategory, Integer> consecutive = new HashMap<>();
        for (int i = 0; i < ranked.size(); i++) {
            Demand d = ranked.get(i);
            DemandCategory cat = d.getCategory();
            int count = consecutive.getOrDefault(cat, 0);
            if (count >= 3) {
                Demand moved = removeFirstOfOtherCategory(ranked, i, cat);
                if (moved != null) {
                    result.add(moved);
                    consecutive.clear();
                    consecutive.merge(moved.getCategory(), 1, Integer::sum);
                    continue;
                }
            }
            result.add(d);
            consecutive.merge(cat, 1, Integer::sum);
            for (Map.Entry<DemandCategory, Integer> e : new HashMap<>(consecutive).entrySet()) {
                if (!e.getKey().equals(cat)) consecutive.put(e.getKey(), 0);
            }
        }
        return result;
    }

    private Demand removeFirstOfOtherCategory(List<Demand> list, int from, DemandCategory exclude) {
        for (int i = from + 1; i < list.size(); i++) {
            if (list.get(i).getCategory() != exclude) {
                return list.remove(i);
            }
        }
        return null;
    }
}

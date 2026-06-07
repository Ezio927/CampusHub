package com.example.backend.demand.pattern.matcher;

import com.example.backend.demand.entity.Demand;
import com.example.backend.user.entity.User;

import java.util.Collections;
import java.util.List;

public interface DemandMatcher {

    default List<User> match(Demand demand) {
        return Collections.emptyList();
    }

    double score(User user, Demand demand);
}

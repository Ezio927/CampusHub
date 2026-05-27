package com.example.backend.demand.pattern.matcher;

import com.example.backend.demand.entity.Demand;
import com.example.backend.user.entity.User;

import java.util.List;

public interface DemandMatcher {

    List<User> match(Demand demand);

    double score(User user, Demand demand);
}

package com.example.backend.demand.pattern.matcher;

import com.example.backend.demand.entity.Demand;
import com.example.backend.user.entity.User;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SkillMatcher implements DemandMatcher {

    @Override
    public double score(User user, Demand demand) {
        if (user == null || demand == null) {
            return 0;
        }
        Set<String> demandTokens = demandTokens(demand);
        if (demandTokens.isEmpty()) {
            return 50;
        }
        String userText = normalize(join(
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                invokeString(user, "getBio"),
                invokeString(user, "getSkillTags"),
                invokeString(user, "getSkills"),
                invokeString(user, "getTags")
        ));
        int matched = 0;
        for (String token : demandTokens) {
            if (userText.contains(token)) {
                matched++;
            }
        }
        if (matched == 0) {
            return demand.getCategory() == com.example.backend.common.enums.DemandCategory.SKILL ? 35 : 50;
        }
        return Math.min(100, 60 + matched * 20);
    }

    private Set<String> demandTokens(Demand demand) {
        Set<String> tokens = new HashSet<String>();
        String text = normalize(join(demand.getTitle(), demand.getDescription(),
                demand.getCategory() == null ? null : demand.getCategory().name()));
        for (String token : text.split(" ")) {
            if (!isBlank(token) && token.length() >= 2) {
                tokens.add(token);
            }
        }
        for (String keyword : knownSkillKeywords()) {
            if (text.contains(keyword)) {
                tokens.add(keyword);
            }
        }
        return tokens;
    }

    private List<String> knownSkillKeywords() {
        List<String> keywords = new ArrayList<String>();
        Collections.addAll(keywords, "java", "python", "c++", "vue", "spring", "math", "english",
                "ps", "excel", "ppt", "programming", "coding", "skill", "study");
        return keywords;
    }

    private String invokeString(User user, String methodName) {
        try {
            Method method = user.getClass().getMethod(methodName);
            Object value = method.invoke(user);
            return value == null ? null : String.valueOf(value);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private String join(String... values) {
        StringBuilder builder = new StringBuilder();
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (!isBlank(value)) {
                builder.append(value).append(' ');
            }
        }
        return builder.toString();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replace('-', ' ')
                .replace('_', ' ')
                .replace(',', ' ')
                .replace(';', ' ')
                .trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

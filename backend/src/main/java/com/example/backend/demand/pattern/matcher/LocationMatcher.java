package com.example.backend.demand.pattern.matcher;

import com.example.backend.demand.entity.Demand;
import com.example.backend.user.entity.User;

import java.lang.reflect.Method;
import java.util.Locale;

public class LocationMatcher implements DemandMatcher {

    @Override
    public double score(User user, Demand demand) {
        if (user == null || demand == null || isBlank(demand.getLocationTag())) {
            return 0;
        }
        String location = normalize(demand.getLocationTag());
        String userText = normalize(join(
                invokeString(user, "getCampus"),
                invokeString(user, "getCampusName"),
                invokeString(user, "getLocationTag"),
                invokeString(user, "getAddress"),
                user.getUsername(),
                user.getRealName(),
                user.getEmail()
        ));
        if (userText.contains(location)) {
            return 100;
        }
        for (String token : location.split(" ")) {
            if (!isBlank(token) && token.length() >= 2 && userText.contains(token)) {
                return 80;
            }
        }
        return 50;
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
                .trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

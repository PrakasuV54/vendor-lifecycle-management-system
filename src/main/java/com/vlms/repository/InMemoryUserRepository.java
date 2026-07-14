package com.vlms.repository;

import com.vlms.enums.UserRole;
import com.vlms.model.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository implementation for User entities.
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> userStore = new HashMap<>();

    @Override
    public void save(User user) {
        userStore.put(user.getUserId(), user);
    }

    @Override
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(userStore.get(userId));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userStore.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(userStore.values());
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return userStore.values().stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toList());
    }
}

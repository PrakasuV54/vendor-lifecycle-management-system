package com.vlms.repository;

import com.vlms.enums.UserRole;
import com.vlms.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Interface definition for User persistence.
 * Follows DIP: service layer depends on this interface.
 */
public interface UserRepository {
    void save(User user);
    Optional<User> findById(String userId);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    List<User> findByRole(UserRole role);
}

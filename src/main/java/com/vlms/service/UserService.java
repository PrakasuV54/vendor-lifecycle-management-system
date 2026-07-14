package com.vlms.service;

import com.vlms.enums.UserRole;
import com.vlms.model.Admin;
import com.vlms.model.FinanceManager;
import com.vlms.model.ProcurementManager;
import com.vlms.model.User;
import com.vlms.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * UserService: manages creation and retrieval of system users.
 * SRP: solely responsible for user management.
 * DIP: depends on UserRepository abstraction, not a concrete data source.
 */
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Admin createAdmin(String name, String email) {
        Admin admin = new Admin(name, email);
        userRepository.save(admin);
        System.out.println("  [UserService] Admin created: " + admin);
        return admin;
    }

    public ProcurementManager createProcurementManager(String name, String email, String department) {
        ProcurementManager pm = new ProcurementManager(name, email, department);
        userRepository.save(pm);
        System.out.println("  [UserService] ProcurementManager created: " + pm);
        return pm;
    }

    public FinanceManager createFinanceManager(String name, String email, String costCenter) {
        FinanceManager fm = new FinanceManager(name, email, costCenter);
        userRepository.save(fm);
        System.out.println("  [UserService] FinanceManager created: " + fm);
        return fm;
    }

    public Optional<User> findUserById(String userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
}

package com.vlms.model;

import com.vlms.enums.UserRole;

/**
 * Admin user with full system access.
 * Extends User (Inheritance). Demonstrates Liskov Substitution: usable wherever User is expected.
 */
public class Admin extends User {

    public Admin(String name, String email) {
        super(name, email, UserRole.ADMIN);
    }

    @Override
    public String getRoleDescription() {
        return "System Administrator - Full access to all modules including user management, vendor lifecycle, and system configuration.";
    }

    @Override
    public String toString() {
        return "Admin[" + getUserId() + " | " + getName() + " | " + getEmail() + "]";
    }
}

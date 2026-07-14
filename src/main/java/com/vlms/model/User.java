package com.vlms.model;

import com.vlms.enums.UserRole;
import com.vlms.interfaces.Notifiable;
import com.vlms.util.IdGenerator;
import com.vlms.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for all system users.
 * Demonstrates Abstraction: common fields/behaviour are centralised here.
 * Demonstrates Encapsulation: fields are private with controlled access.
 */
public abstract class User implements Notifiable {

    private final String userId;
    private final String name;
    private final String email;
    private final UserRole role;
    private final LocalDateTime createdAt;
    private final List<Notification> receivedNotifications;

    protected User(String name, String email, UserRole role) {
        ValidationUtil.requireNonBlank(name, "User name");
        ValidationUtil.requireValidEmail(email);

        this.userId = IdGenerator.generateUserId();
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = LocalDateTime.now();
        this.receivedNotifications = new ArrayList<>();
    }

    // --- Notifiable implementation ---

    @Override
    public void receiveNotification(Notification notification) {
        receivedNotifications.add(notification);
    }

    @Override
    public String getNotificationTarget() {
        return email;
    }

    // --- Abstract methods subclasses must provide ---

    /**
     * Returns a description of this user's responsibilities.
     */
    public abstract String getRoleDescription();

    // --- Getters ---

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<Notification> getReceivedNotifications() {
        return Collections.unmodifiableList(receivedNotifications);
    }

    @Override
    public String toString() {
        return String.format("User[%s | %s | %s | %s]", userId, name, email, role);
    }
}

package com.vlms.interfaces;

import com.vlms.model.Notification;

/**
 * Interface for entities that can receive and display notifications.
 * Demonstrates Interface Segregation: decoupled from other entity concerns.
 */
public interface Notifiable {

    /**
     * Delivers a notification to this entity.
     */
    void receiveNotification(Notification notification);

    /**
     * Returns the primary contact identifier (email/ID) for notification routing.
     */
    String getNotificationTarget();
}

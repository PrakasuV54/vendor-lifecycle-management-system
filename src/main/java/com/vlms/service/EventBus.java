package com.vlms.service;

import com.vlms.event.VendorEvent;
import com.vlms.interfaces.EventListener;
import com.vlms.util.ConsoleLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory Event Bus implementing the Observer Pattern.
 * Decouples notifications, task creation, and audit logging from core services.
 */
public class EventBus {

    private final List<EventListener> listeners = new ArrayList<>();

    public synchronized void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    public synchronized void publish(VendorEvent event) {
        ConsoleLogger.info("  [EventBus] Publishing event: " + event.getEventType());
        for (EventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                ConsoleLogger.error("Error dispatching event to listener " + listener.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}

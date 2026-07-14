package com.vlms.event;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Event model carried across the system via the Event Bus.
 * Used for loose coupling and pub/sub notifications.
 */
public class VendorEvent {

    private final String eventType; // E.g., "WORKFLOW_STARTED", "TASK_ASSIGNED", etc.
    private final LocalDateTime timestamp;
    private final Map<String, Object> parameters;

    public VendorEvent(String eventType) {
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.parameters = new HashMap<>();
    }

    public VendorEvent withParam(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }

    public String getEventType() { return eventType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Object getParam(String key) { return parameters.get(key); }
    public Map<String, Object> getParameters() { return parameters; }

    @Override
    public String toString() {
        return "VendorEvent{" + "type='" + eventType + '\'' + ", params=" + parameters + '}';
    }
}

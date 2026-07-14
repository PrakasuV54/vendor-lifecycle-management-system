package com.vlms.service;

import com.vlms.enums.NotificationType;
import com.vlms.event.VendorEvent;
import com.vlms.interfaces.EventListener;
import com.vlms.interfaces.Notifiable;
import com.vlms.model.Notification;
import com.vlms.util.ConsoleLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NotificationService: routes and logs all notifications.
 * Refactored to remove the Singleton pattern in favor of constructor injection and event subscription (Observer).
 */
public class NotificationService implements EventListener {

    private final List<Notification> allNotifications = new ArrayList<>();

    public NotificationService() {
        // Injectable constructor
    }

    /**
     * Creates and dispatches a notification to the target entity.
     * The notification is also persisted in the global notification log.
     */
    public Notification sendNotification(NotificationType type, String message,
                                          String targetEntityId, String targetEntityType,
                                          Notifiable recipient) {
        Notification notification = new Notification(type, message, targetEntityId, targetEntityType);
        allNotifications.add(notification);

        if (recipient != null) {
            recipient.receiveNotification(notification);
        }

        ConsoleLogger.info("  [Notification] " + type + " → " + targetEntityType + " [" + targetEntityId + "]: " + message);
        return notification;
    }

    /**
     * Broadcasts a notification to multiple recipients.
     */
    public Notification broadcastNotification(NotificationType type, String message,
                                                String targetEntityId, String targetEntityType,
                                                List<? extends Notifiable> recipients) {
        Notification notification = new Notification(type, message, targetEntityId, targetEntityType);
        allNotifications.add(notification);

        for (Notifiable recipient : recipients) {
            recipient.receiveNotification(notification);
        }

        ConsoleLogger.info("  [Notification-Broadcast] " + type + " → " + recipients.size()
                + " recipients | " + message);
        return notification;
    }

    @Override
    public void onEvent(VendorEvent event) {
        String type = event.getEventType();
        String entityId = (String) event.getParam("entityId");
        String entityType = (String) event.getParam("entityType");
        String remarks = (String) event.getParam("remarks");
        String title = (String) event.getParam("title");

        String msg = "";
        NotificationType nType = NotificationType.VENDOR_APPROVAL; // fallback

        switch (type) {
            case "WORKFLOW_STARTED":
                msg = "Workflow started for entity " + entityId;
                break;
            case "TASK_ASSIGNED":
                msg = "New Human Task assigned: \"" + title + "\" to " + event.getParam("assignedGroup");
                break;
            case "TASK_COMPLETED":
                msg = "Human Task completed: \"" + title + "\" by user " + event.getParam("userId") + ". Remarks: " + remarks;
                break;
            case "WORKFLOW_COMPLETED":
                msg = "Workflow successfully completed for entity " + entityId;
                break;
            case "WORKFLOW_FAILED":
                msg = "Workflow FAILED for entity " + entityId + ". Reason: " + remarks;
                break;
            case "CONTRACT_EXPIRING":
                nType = NotificationType.CONTRACT_EXPIRY;
                msg = "Contract " + entityId + " is expiring soon!";
                break;
            case "INVOICE_APPROVED":
                msg = "Invoice " + entityId + " has been approved for payment.";
                break;
            case "PAYMENT_COMPLETED":
                nType = NotificationType.PAYMENT_COMPLETION;
                msg = "Payment for invoice " + entityId + " has been successfully processed.";
                break;
            case "VENDOR_SUSPENDED":
                msg = "Vendor " + entityId + " has been suspended. Reason: " + remarks;
                break;
            case "VENDOR_ACTIVATED":
                msg = "Vendor " + entityId + " has been successfully activated.";
                break;
        }

        if (!msg.isEmpty()) {
            sendNotification(nType, msg, entityId, entityType != null ? entityType : "Event", null);
        }
    }

    public List<Notification> getAllNotifications() {
        return Collections.unmodifiableList(allNotifications);
    }

    public List<Notification> getNotificationsByType(NotificationType type) {
        return allNotifications.stream()
                .filter(n -> n.getType() == type)
                .toList();
    }

    public int getTotalNotificationCount() {
        return allNotifications.size();
    }
}

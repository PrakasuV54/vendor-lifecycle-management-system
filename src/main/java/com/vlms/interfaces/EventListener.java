package com.vlms.interfaces;

import com.vlms.event.VendorEvent;

/**
 * Interface definition for Observer Pattern event listeners.
 */
public interface EventListener {
    void onEvent(VendorEvent event);
}

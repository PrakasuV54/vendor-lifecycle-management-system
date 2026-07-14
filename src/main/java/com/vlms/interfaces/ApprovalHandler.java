package com.vlms.interfaces;

import com.vlms.model.ApprovalRequest;

/**
 * Contract for Chain of Responsibility approval handlers.
 */
public interface ApprovalHandler {
    void setNext(ApprovalHandler next);
    void handle(ApprovalRequest request);
}

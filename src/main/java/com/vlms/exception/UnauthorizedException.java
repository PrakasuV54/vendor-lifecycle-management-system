package com.vlms.exception;

import com.vlms.enums.UserRole;

/**
 * Thrown when an RBAC authorization check fails in a service or workflow.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String userId, UserRole role, String action) {
        super(String.format("User [%s] with role [%s] is unauthorized to perform action: %s", userId, role, action));
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}

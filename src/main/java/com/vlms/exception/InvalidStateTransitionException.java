package com.vlms.exception;

import com.vlms.enums.VendorStatus;

/**
 * Thrown when an invalid lifecycle state transition is attempted.
 */
public class InvalidStateTransitionException extends RuntimeException {

    private final VendorStatus currentState;
    private final VendorStatus attemptedState;

    public InvalidStateTransitionException(VendorStatus currentState, VendorStatus attemptedState) {
        super("Cannot transition vendor from [" + currentState + "] to [" + attemptedState + "]");
        this.currentState = currentState;
        this.attemptedState = attemptedState;
    }

    public VendorStatus getCurrentState() {
        return currentState;
    }

    public VendorStatus getAttemptedState() {
        return attemptedState;
    }
}

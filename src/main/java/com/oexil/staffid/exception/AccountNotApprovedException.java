package com.oexil.staffid.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountNotApprovedException extends AuthenticationException {
    
    public AccountNotApprovedException(String message) {
        super(message);
    }
    
    public AccountNotApprovedException(String message, Throwable cause) {
        super(message, cause);
    }
}
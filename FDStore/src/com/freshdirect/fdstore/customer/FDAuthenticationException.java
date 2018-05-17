/*
 * $Workfile$
 *
 * $Date$
 * 
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer;

import com.freshdirect.fdstore.FDException;

/**
 * Exception for authentication failure (eg. bad username/password).
 *
 * @version $Revision$
 * @author $Author$
 */ 
public class FDAuthenticationException extends FDException {
    
    /**
     * Default constructor.
     */    
    public FDAuthenticationException() {
        super();
    }
    
    /** 
     * Creates an exception with a custom message.
     *
     * @param message a custom message
     */    
    public FDAuthenticationException(String message) {
        super(message);
    }
    
    /**
     * Creates an exception that wraps another exception.
     *
     * @param ex the wrapped exception
     */    
    public FDAuthenticationException(Exception ex) {
        super(ex);
    }
    
    /**
     * Creates an exception with a custom message and a wrapped exception.
     *
     * @param ex the wrapped exception
     * @param message a custom message
     */    
    public FDAuthenticationException(Exception ex, String message) {
        super(ex, message);
    }

}

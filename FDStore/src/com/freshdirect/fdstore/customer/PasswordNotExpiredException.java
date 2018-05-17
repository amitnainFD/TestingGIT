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
 * Exception for password request
 *
 * @version $Revision$
 * @author $Author$
 */
public class PasswordNotExpiredException extends FDException {

    /**
     * Default constructor.
     */
    public PasswordNotExpiredException() {
        super();
    }

    /**
     * Creates an exception with a custom message.
     *
     * @param message a custom message
     */
    public PasswordNotExpiredException(String message) {
        super(message);
    }

    /**
     * Creates an exception that wraps another exception.
     *
     * @param ex the wrapped exception
     */
    public PasswordNotExpiredException(Exception ex) {
        super(ex);
    }

    /**
     * Creates an exception with a custom message and a wrapped exception.
     *
     * @param ex the wrapped exception
     * @param message a custom message
     */
    public PasswordNotExpiredException(Exception ex, String message) {
        super(ex, message);
    }

}

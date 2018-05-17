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
 * Exception for locator failure (eg. bad first name/last name/email address).
 * This will be of use primarily for the CallCenter application.
 *
 * @version $Revision$
 * @author $Author$
 */
public class FDLocatorException extends FDException {

    /**
     * Default constructor.
     */
    public FDLocatorException() {
        super();
    }

    /**
     * Creates an exception with a custom message.
     *
     * @param message a custom message
     */
    public FDLocatorException(String message) {
        super(message);
    }

    /**
     * Creates an exception that wraps another exception.
     *
     * @param ex the wrapped exception
     */
    public FDLocatorException(Exception ex) {
        super(ex);
    }

    /**
     * Creates an exception with a custom message and a wrapped exception.
     *
     * @param ex the wrapped exception
     * @param message a custom message
     */
    public FDLocatorException(Exception ex, String message) {
        super(ex, message);
    }

} // class FDLocatorException

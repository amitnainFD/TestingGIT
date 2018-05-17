package com.freshdirect.fdstore.lists;

import com.freshdirect.fdstore.FDException;

public class FDCustomerListExistsException extends FDException {
    /**
     * Default constructor.
     */    
    public FDCustomerListExistsException() {
        super();
    }
    
    /** 
     * Creates an exception with a custom message.
     *
     * @param message a custom message
     */    
    public FDCustomerListExistsException(String message) {
        super(message);
    }
    
    /**
     * Creates an exception that wraps another exception.
     *
     * @param ex the wrapped exception
     */    
    public FDCustomerListExistsException(Exception ex) {
        super(ex);
    }
    
    /**
     * Creates an exception with a custom message and a wrapped exception.
     *
     * @param ex the wrapped exception
     * @param message a custom message
     */    
    public FDCustomerListExistsException(Exception ex, String message) {
        super(ex, message);
    }

}

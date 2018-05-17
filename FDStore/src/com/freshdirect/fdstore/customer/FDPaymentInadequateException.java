/*
 * ErpDuplicateUserIdException.java
 *
 * Created on January 5, 2002, 5:33 PM
 */

package com.freshdirect.fdstore.customer;

/**
 *
 * @author  skrishnasamy
 * @version 
 */
import com.freshdirect.framework.core.ExceptionSupport;

public class FDPaymentInadequateException extends ExceptionSupport {

	/** Creates new ErpDuplicateUserIdException */
    public FDPaymentInadequateException() {
		super();
    }
	
	/** 
     * Creates an exception with a custom message.
     *
     * @param message a custom message
     */    
    public FDPaymentInadequateException(String message) {
        super(message);
    }
    
    /**
     * Creates an exception that wraps another exception.
     *
     * @param ex the wrapped exception
     */    
    public FDPaymentInadequateException(Exception ex) {
        super(ex);
    }
    
    /**
     * Creates an exception with a custom message and a wrapped exception.
     *
     * @param ex the wrapped exception
     * @param message a custom message
     */    
    public FDPaymentInadequateException(Exception ex, String message) {
        super(ex, message);
    }

}

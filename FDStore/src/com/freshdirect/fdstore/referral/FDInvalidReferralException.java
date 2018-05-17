/*
 * Created on Jun 15, 2005
 *
 */
package com.freshdirect.fdstore.referral;

import com.freshdirect.fdstore.FDException;

/**
 * @author jng
 *
 */
public class FDInvalidReferralException extends FDException {

	/*
	    * Default constructor.
	    */    
	   public FDInvalidReferralException() {
	       super();
	   }
	   
	   /** 
	    * Creates an exception with a custom message.
	    *
	    * @param message a custom message
	    */    
	   public FDInvalidReferralException(String message) {
	       super(message);
	   }
	   
	   /**
	    * Creates an exception that wraps another exception.
	    *
	    * @param ex the wrapped exception
	    */    
	   public FDInvalidReferralException(Exception ex) {
	       super(ex);
	   }
	   
	   /**
	    * Creates an exception with a custom message and a wrapped exception.
	    *
	    * @param ex the wrapped exception
	    * @param message a custom message
	    */    
	   public FDInvalidReferralException(Exception ex, String message) {
	       super(ex, message);
	   }
	
}

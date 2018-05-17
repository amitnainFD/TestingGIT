package com.freshdirect.fdstore.promotion.management;

import com.freshdirect.fdstore.FDException;

public class FDDuplicatePromoFieldException  extends FDException {
	/*
    * Default constructor.
    */    
   public FDDuplicatePromoFieldException() {
       super();
   }
   
   /** 
    * Creates an exception with a custom message.
    *
    * @param message a custom message
    */    
   public FDDuplicatePromoFieldException(String message) {
       super(message);
   }
   
   /**
    * Creates an exception that wraps another exception.
    *
    * @param ex the wrapped exception
    */    
   public FDDuplicatePromoFieldException(Exception ex) {
       super(ex);
   }
   
   /**
    * Creates an exception with a custom message and a wrapped exception.
    *
    * @param ex the wrapped exception
    * @param message a custom message
    */    
   public FDDuplicatePromoFieldException(Exception ex, String message) {
       super(ex, message);
   }

}

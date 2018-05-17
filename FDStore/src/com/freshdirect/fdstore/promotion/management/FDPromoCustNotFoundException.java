package com.freshdirect.fdstore.promotion.management;

import com.freshdirect.fdstore.FDException;

public class FDPromoCustNotFoundException  extends FDException {
	/*
    * Default constructor.
    */    
   public FDPromoCustNotFoundException() {
       super();
   }
   
   /** 
    * Creates an exception with a custom message.
    *
    * @param message a custom message
    */    
   public FDPromoCustNotFoundException(String message) {
       super(message);
   }
   
   /**
    * Creates an exception that wraps another exception.
    *
    * @param ex the wrapped exception
    */    
   public FDPromoCustNotFoundException(Exception ex) {
       super(ex);
   }
   
   /**
    * Creates an exception with a custom message and a wrapped exception.
    *
    * @param ex the wrapped exception
    * @param message a custom message
    */    
   public FDPromoCustNotFoundException(Exception ex, String message) {
       super(ex, message);
   }

}

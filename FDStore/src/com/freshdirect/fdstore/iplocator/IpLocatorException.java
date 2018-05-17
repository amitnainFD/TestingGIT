package com.freshdirect.fdstore.iplocator;


public class IpLocatorException extends Exception{
	private static final long serialVersionUID = -1623078382710221204L;

	public IpLocatorException() {
	}
	
	public IpLocatorException(Throwable cause) {
		super(cause);
	}
	
    public IpLocatorException(String message) {
    	super(message);
    }
    
    public IpLocatorException(String message, Throwable cause) {
        super(message, cause);
    }
}


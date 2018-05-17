package com.freshdirect.fdstore.coremetrics.builder;

public class SkipTagException extends Exception {
	private static final long serialVersionUID = -5489981923972733050L;
	
	public SkipTagException(String message) {
		super(message);
	}
	
	public SkipTagException(String message, Throwable cause) {
        super(message, cause);
    }
}

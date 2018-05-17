package com.freshdirect.fdstore.customer;

public class OrderLineLimitExceededException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 824007919916721823L;
	
	
	public OrderLineLimitExceededException(String s) {
		super(s);
	}
	
	public OrderLineLimitExceededException() {
	}

}

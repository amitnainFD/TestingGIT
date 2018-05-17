package com.freshdirect.fdstore.content.customerrating;

import java.io.Serializable;

@Deprecated
public class BazaarvoiceFeedProcessResult implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5315673239463792603L;
	private boolean success;
	private String error;
	
	public BazaarvoiceFeedProcessResult(boolean success, String error) {
		super();
		this.success = success;
		this.error = error;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getError() {
		return error;
	}

}

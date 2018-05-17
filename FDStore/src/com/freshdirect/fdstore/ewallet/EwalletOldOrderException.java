package com.freshdirect.fdstore.ewallet;

import com.freshdirect.framework.core.ExceptionSupport;

public class EwalletOldOrderException extends ExceptionSupport {
	public EwalletOldOrderException() {
		super();
	}
	
	public EwalletOldOrderException(String msg) {
		super(msg);
	}
	
	public EwalletOldOrderException(Exception e) {
		super(e);
	}
	
	public EwalletOldOrderException(Exception e, String msg) {
		super(e, msg);
	}
}

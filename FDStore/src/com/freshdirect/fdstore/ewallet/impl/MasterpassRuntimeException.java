package com.freshdirect.fdstore.ewallet.impl;

import com.freshdirect.framework.core.RuntimeExceptionSupport;

public class MasterpassRuntimeException extends RuntimeExceptionSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2292281336190939663L;

	public MasterpassRuntimeException() {
		super();
	}

	public MasterpassRuntimeException(String message) {
		super(message);
	}

	public MasterpassRuntimeException(Exception ex) {
		super(ex);
	}

	public MasterpassRuntimeException(Exception ex, String message) {
		super(ex, message);
	}
}

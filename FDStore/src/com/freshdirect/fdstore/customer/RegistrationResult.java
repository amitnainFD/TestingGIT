/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer;


/**
 *
 *
 * @version $Revision$
 * @author $Author$
 */
public class RegistrationResult implements java.io.Serializable {

	private final FDIdentity identity;
	private final boolean foundFraud;

	public RegistrationResult(FDIdentity id) {
		this(id, false);
	}

	public RegistrationResult(FDIdentity id, boolean b) {
		super();
		this.identity = id;
		this.foundFraud = b;
	}

	public FDIdentity getIdentity() {
		return this.identity;
	}

	public boolean hasPossibleFraud() {
		return this.foundFraud;
	}

}
package com.freshdirect.fdstore.customer;

public class FDTransientCartModel extends FDCartModel {

	private static final long	serialVersionUID	= -2108759470092594823L;

	public FDTransientCartModel() {
		super();
	}

	public FDTransientCartModel( FDCartModel cart ) {
		super( cart );
	}

	@Override
	public boolean isPersistent() {
		return false;
	}
}

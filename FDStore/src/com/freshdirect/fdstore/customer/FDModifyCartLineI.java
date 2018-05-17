package com.freshdirect.fdstore.customer;

/**
 * Interface for modify cartline models to implement
 */
public interface FDModifyCartLineI extends FDCartLineI {

	public FDCartLineI getOriginalOrderLine();

}

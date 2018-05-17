package com.freshdirect.fdstore.services.tax;

import com.freshdirect.fdstore.FDException;
import com.freshdirect.fdstore.customer.FDCartI;

public interface TaxStrategy {
	String getType();
	AvalaraContext getTaxResponse(AvalaraContext avalaraContext) throws FDException;
	boolean cancelTax(AvalaraContext avalaraContext);
}
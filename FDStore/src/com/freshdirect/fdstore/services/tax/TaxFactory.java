package com.freshdirect.fdstore.services.tax;

import com.freshdirect.fdstore.customer.FDCartI;

public interface TaxFactory{
	AvalaraContext getTax(AvalaraContext avalaraContext);
	boolean cancelTax(AvalaraContext avalaraContext);
}
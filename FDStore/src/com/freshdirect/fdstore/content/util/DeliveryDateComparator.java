package com.freshdirect.fdstore.content.util;

import java.util.Comparator;

import com.freshdirect.fdstore.customer.FDOrderInfoI;

public class DeliveryDateComparator implements Comparator<FDOrderInfoI>{

	@Override
	public int compare(FDOrderInfoI arg0, FDOrderInfoI arg1) {
		return arg0.getDeliveryStartTime().compareTo(arg1.getDeliveryStartTime());
	}

}

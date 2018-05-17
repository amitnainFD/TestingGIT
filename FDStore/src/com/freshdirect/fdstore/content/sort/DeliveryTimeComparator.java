package com.freshdirect.fdstore.content.sort;

import java.util.Comparator;

import com.freshdirect.fdstore.customer.FDOrderInfoI;

public class DeliveryTimeComparator implements Comparator<FDOrderInfoI> {

    public int compare(FDOrderInfoI order1, FDOrderInfoI order2) {

		if ( order1 == null && order2 == null ) 
			return 0;
		else if ( order1 == null )
			return 1;
		else if ( order2 == null )
			return -1;

		if (order1.getDeliveryStartTime().before(order2.getDeliveryStartTime())) {
			return -1;
		} else if (order1.getDeliveryStartTime().after(order2.getDeliveryStartTime())) {
			return 1;
		} else {
			return 0;
		}
    }
}

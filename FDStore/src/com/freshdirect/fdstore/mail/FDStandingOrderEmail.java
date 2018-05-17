package com.freshdirect.fdstore.mail;

import java.util.List;
import java.util.Map;

import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;

public class FDStandingOrderEmail extends FDTransactionalEmail {
	private static final long serialVersionUID = 8314062573191547683L;

	FDStandingOrder standingOrder;

	List<FDCartLineI> unavCartItems;

	public FDStandingOrderEmail(FDCustomerInfo customer, FDOrderI order, FDStandingOrder standingOrder, List<FDCartLineI> unavCartItems) {
		super(customer, order);
		this.standingOrder = standingOrder;
		this.unavCartItems = unavCartItems;
	}

	public FDStandingOrder getStandingOrder() {
		return standingOrder;
	}
	
	public void setStandingOrder(FDStandingOrder standingOrder) {
		this.standingOrder = standingOrder;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void decorateMap(Map map) {
		super.decorateMap(map);
		map.put("standingOrder", standingOrder);
		// map.put("result", result);
		map.put("unavailableItems", unavCartItems);
		map.put("hasUnavailableItems", unavCartItems == null ? false : unavCartItems.size() > 0);
	}
}

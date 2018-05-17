package com.freshdirect.fdstore.mail;

import java.util.Map;

import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;

public class FDStandingOrderErrorEmail extends FDInfoEmail {
	private static final long serialVersionUID = 6561045913857658499L;

	FDStandingOrder standingOrder;
	
	public FDStandingOrderErrorEmail(FDCustomerInfo customer, FDStandingOrder standingOrder) {
		super(customer);
		this.standingOrder = standingOrder;
	}
	
	public FDStandingOrder getStandingOrder() {
		return standingOrder;
	}
	
	public void setStandingOrder(FDStandingOrder standingOrder) {
		this.standingOrder = standingOrder;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void decorateMap(Map map) {
		super.decorateMap(map);
		map.put("standingOrder", standingOrder);
	}
}

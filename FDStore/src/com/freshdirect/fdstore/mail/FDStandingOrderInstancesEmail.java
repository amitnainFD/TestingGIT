package com.freshdirect.fdstore.mail;

import java.util.List;
import java.util.Map;

import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.fdstore.customer.FDOrderInfoI;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;

public class FDStandingOrderInstancesEmail extends FDInfoEmail{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2252614128675230466L;
	
	FDStandingOrder standingOrder;
	List<FDOrderInfoI> orders;
	
	public FDStandingOrderInstancesEmail(FDCustomerInfo customer, FDStandingOrder sOrder, List<FDOrderInfoI> orders) {
		super(customer);
		this.standingOrder=sOrder;
		this.orders=orders;
	}

	public FDStandingOrder getStandingOrder() {
		return standingOrder;
	}

	public void setStandingOrder(FDStandingOrder standingOrder) {
		this.standingOrder = standingOrder;
	}
	
	public int getOrderCount(){
		return orders.size();
	}
		
	public List<FDOrderInfoI> getOrders() {
		return orders;
	}

	public void setOrders(List<FDOrderInfoI> orders) {
		this.orders = orders;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void decorateMap(Map map) {
		super.decorateMap(map);
		map.put("standingOrder", standingOrder);
		map.put("orders", orders);
		map.put("orderCount", getOrderCount());
	}
	
}

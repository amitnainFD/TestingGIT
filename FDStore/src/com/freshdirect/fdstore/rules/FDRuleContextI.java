package com.freshdirect.fdstore.rules;

import java.util.List;

import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.fdlogistics.model.EnumDeliveryFeeTier;
import com.freshdirect.fdlogistics.model.FDTimeslot;
import com.freshdirect.fdstore.customer.FDUserI;

public interface FDRuleContextI {
	
	public String getCounty();
	public EnumServiceType getServiceType();
	public boolean isChefsTable();
	public boolean isVip();
	public double getOrderTotal();
	public EnumDeliveryFeeTier getDeliverFeeTier();
	public boolean hasProfileAttribute(String attributeName, String attributeValue);
	public String getDepotCode ();
	
	public FDUserI getUser();
	
	public FDTimeslot getTimeslot();

	public Double getSubTotal();
	
}

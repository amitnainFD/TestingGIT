package com.freshdirect.fdstore.standingorders;

import java.io.Serializable;

public class UnAvailabilityDetails implements Serializable {
		
	double unavailQty;
	UnavailabilityReason reason;
	String altSkucode;
	
	public UnAvailabilityDetails(double unavailQty, UnavailabilityReason reason,String altSkucode) {
		super();
		this.unavailQty = unavailQty;
		this.reason = reason;
		this.altSkucode = altSkucode;
	}
	
	public double getUnavailQty() {
		return unavailQty;
	}

	public UnavailabilityReason getReason() {
		return reason;
	}
	
	public String getAltSkucode() {
		return altSkucode;
	}
}

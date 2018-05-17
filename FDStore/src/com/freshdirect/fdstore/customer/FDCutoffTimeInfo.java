package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.Date;

import com.freshdirect.customer.EnumSaleStatus;

public class FDCutoffTimeInfo implements Serializable {
	
	private final EnumSaleStatus status;
	private final Date cutoffTime;
	private final int orderCount;
	
	public FDCutoffTimeInfo(EnumSaleStatus status, Date cutoffTime, int orderCount){
		this.status = status;
		this.cutoffTime = cutoffTime;
		this.orderCount = orderCount;
	}
	
	public EnumSaleStatus getStatus(){
		return this.status;
	}
	
	public Date getCutoffTime () {
		return this.cutoffTime;
	}
	
	public int getOrderCount() {
		return this.orderCount;
	}
}

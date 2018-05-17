package com.freshdirect.fdstore.promotion.management;

import java.util.Date;

import com.freshdirect.fdstore.promotion.EnumPromotionStatus;
import com.freshdirect.framework.core.ModelSupport;

public class WSAdminInfo extends ModelSupport {
	private int day; 
	private double amountSpent;
		
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public void setAmountSpent(double amountSpent) {
		this.amountSpent = amountSpent;
	}
	public double getAmountSpent() {
		return amountSpent;
	}
	
}

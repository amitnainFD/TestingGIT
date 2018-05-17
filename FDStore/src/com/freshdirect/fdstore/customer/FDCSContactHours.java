package com.freshdirect.fdstore.customer;

import java.io.Serializable;

public class FDCSContactHours implements Serializable {	
	private String daysDisplay;
	private String hoursDisplay;
	
	public FDCSContactHours(String daysDisplay, String hoursDisplay) {
		super();
		this.daysDisplay = daysDisplay;
		this.hoursDisplay = hoursDisplay;
	}
	
	public String getDaysDisplay() {
		return daysDisplay;
	}
	public void setDaysDisplay(String daysDisplay) {
		this.daysDisplay = daysDisplay;
	}
	public String getHoursDisplay() {
		return hoursDisplay;
	}
	public void setHoursDisplay(String hoursDisplay) {
		this.hoursDisplay = hoursDisplay;
	}
	
}

package com.freshdirect.fdstore.rules;

import java.io.Serializable;

public class DlvPremium implements Serializable {
	
	private double value;
	
	public DlvPremium() {
		
	}
	
	public DlvPremium(double value) {
		this();
		this.value = value;
	}
	
	public double getValue() {
		return this.value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public String toString() {
		return  "DlvPremium[" + value + "]";
	}
}

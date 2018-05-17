package com.freshdirect.fdstore.rules;

import java.io.Serializable;

public class Adjustment implements Serializable {
	
	private double value;
	
	public Adjustment() {
		
	}
	
	public Adjustment(double value) {
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
		return  "Adjustment[" + value + "]";
	}
}

package com.freshdirect.fdstore.rules;

import java.io.Serializable;

public class BasePrice implements Serializable {
	
	private double price;
	
	public BasePrice(){
		
	}
	
	public BasePrice(double price) {
		this();
		this.price = price;
	}
	
	public double getPrice(){
		return this.price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public String toString() {
		return  "BasePrice[" + price + "]";
	}
}

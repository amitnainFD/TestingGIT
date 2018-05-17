package com.freshdirect.fdstore.rules;

import java.io.Serializable;

public class TieredPrice implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5803780958885112910L;

	public TieredPrice(double basePrice, double promoPrice) {
		super();
		this.basePrice = basePrice;
		this.promoPrice = promoPrice;
	}

	private double basePrice;
	private double promoPrice;
	
	public TieredPrice(){
		
	}

	public double getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(double basePrice) {
		this.basePrice = basePrice;
	}

	public double getPromoPrice() {
		return promoPrice;
	}

	public void setPromoPrice(double promoPrice) {
		this.promoPrice = promoPrice;
	}

	@Override
	public String toString() {
		return "TieredPrice [basePrice=" + basePrice + ", promoPrice="
				+ promoPrice + "]";
	}
	
	

}

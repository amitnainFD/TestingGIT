package com.freshdirect.fdstore.promotion;

import java.io.Serializable;

public class SteeringDiscount implements Serializable {
	
	private boolean isFreeDelivery = false;
	private double discount;
	/**
	 * @return the isFreeDelivery
	 */
	public boolean isFreeDelivery() {
		return isFreeDelivery;
	}
	/**
	 * @param isFreeDelivery the isFreeDelivery to set
	 */
	public void setFreeDelivery(boolean isFreeDelivery) {
		this.isFreeDelivery = isFreeDelivery;
	}
	/**
	 * @return the discount
	 */
	public double getDiscount() {
		return discount;
	}
	/**
	 * @param discount the discount to set
	 */
	public void setDiscount(double discount) {
		this.discount = discount;
	}
	public SteeringDiscount(boolean isFreeDelivery, double discount) {
		super();
		this.isFreeDelivery = isFreeDelivery;
		this.discount = discount;
	}
	
}

package com.freshdirect.fdstore.promotion;

public class DCPDiscountRule extends HeaderDiscountRule {
	private final double percentOff;

	public DCPDiscountRule(double minSubtotal, double percentOff) {
		super(minSubtotal, 0.0);
		this.percentOff = percentOff;
	}

	public double getPercentOff() {
		return percentOff;
	}

	public String toString() {
		return "SignupDiscountRule[minSubtotal="
			+ this.getMinSubtotal()
			+ " maxAmount="
			+ this.getMaxAmount()
			+ " percentOff="
			+ this.percentOff			
			+ "]";
	}

}
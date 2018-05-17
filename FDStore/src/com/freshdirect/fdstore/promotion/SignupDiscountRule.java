package com.freshdirect.fdstore.promotion;

public class SignupDiscountRule extends HeaderDiscountRule {
	private final double maxAmountPerSku;

	public SignupDiscountRule(double minSubtotal, double maxAmount, double maxAmountPerSku) {
		super(minSubtotal, maxAmount);
		this.maxAmountPerSku = maxAmountPerSku;
	}

	public double getMaxAmountPerSku() {
		return maxAmountPerSku;
	}

	public String toString() {
		return "SignupDiscountRule[minSubtotal="
			+ this.getMinSubtotal()
			+ " maxAmount="
			+ this.getMaxAmount()
			+ " maxAmountPerSku="
			+ this.maxAmountPerSku
			+ "]";
	}

}
/*
 * $Workfile$
 *
 * $Date$
 * 
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer;

/**
 * Class representing a discount.
 *
 * @version $Revision$
 * @author $Author$
 */
public class ExtendDPDiscountModel implements java.io.Serializable {

	private final String promotionCode;
	private final int extendDays;

	public ExtendDPDiscountModel(String promotionCode, int extendDays) {
		this.promotionCode = promotionCode;
		this.extendDays = extendDays;
	}

	public String getPromotionCode() {
		return this.promotionCode;
	}

	public int getExtendedDays() {
		return this.extendDays;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("ExtendDPDiscountModel[");
		buf.append(this.promotionCode).append(' ');
		buf.append(this.extendDays).append(' ');
		buf.append(']');
		return buf.toString();
	}

}
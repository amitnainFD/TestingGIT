package com.freshdirect.fdstore.promotion;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;



public  class PromotionErrorType extends Enum{
	
	public static final PromotionErrorType ERROR_GENERIC = new PromotionErrorType("100", 100, "Generic Error");
	public static final PromotionErrorType ERROR_REDEMPTION_EXCEEDED = new PromotionErrorType("101", 101, "Redemption Exceeded");
	public static final PromotionErrorType NO_ELIGIBLE_CART_LINES = new PromotionErrorType("102", 102, "No Eligible Cart Lines");
	public static final PromotionErrorType NO_DELIVERY_ADDRESS_SELECTED = new PromotionErrorType("103", 103, "No Delivery Address Selected");
	public static final PromotionErrorType NO_PAYMENT_METHOD_SELECTED = new PromotionErrorType("104", 104, "No Payment Method Selected");
	public static final PromotionErrorType NO_ELIGIBLE_ADDRESS_SELECTED = new PromotionErrorType("105", 105, "No Eligible Address Selected");
	public static final PromotionErrorType NO_ELIGIBLE_PAYMENT_SELECTED = new PromotionErrorType("106", 106, "No Eligible Payment Selected");
	public static final PromotionErrorType NO_ELIGIBLE_TIMESLOT_SELECTED = new PromotionErrorType("107", 107, "No Eligible Timeslot Selected");
	public static final PromotionErrorType ERROR_USAGE_LIMIT_ONE_EXCEEDED = new PromotionErrorType("108", 108, "Usage Limit 1 Exceeded");
	public static final PromotionErrorType ERROR_USAGE_LIMIT_MORE_EXCEEDED = new PromotionErrorType("109", 109, "Usage Limit More Exceeded");
	public static final PromotionErrorType ERROR_DUPE_FN_LN_ZIP = new PromotionErrorType("110", 109, "Address not eligible for Referral promotion");
	
	private int code; 	
	private String description;
	
	public PromotionErrorType(String name, int code, String description) {
		super(name);
		this.code = code;
		this.description = description;
	}

	public static PromotionErrorType getEnum(String name) {
		return (PromotionErrorType) getEnum(PromotionErrorType.class, name);
	}

	public static Map getEnumMap() {
		return getEnumMap(PromotionErrorType.class);
	}

	public static List getEnumList() {
		return getEnumList(PromotionErrorType.class);
	}

	public static Iterator iterator() {
		return iterator(PromotionErrorType.class);
	}

	  	
	public String getDescription() {
		return this.description;
	}
	
	public int getErrorCode() {
		return this.code;
	}
	
	public String toString() {
		return this.getName();
	}
	
	public String getInfo() {
		return this.getName()+" - "+this.getDescription();
	}

}

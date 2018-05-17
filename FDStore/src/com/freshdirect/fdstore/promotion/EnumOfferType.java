package com.freshdirect.fdstore.promotion;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

public class EnumOfferType extends Enum {

	public static final EnumOfferType SAMPLE = new EnumOfferType("SAMPLE", "Sample Item Promotions");
	public static final EnumOfferType LINE_ITEM = new EnumOfferType("LINE_ITEM", "Line Item Promotions");
	public static final EnumOfferType GIFT_CARD= new EnumOfferType("GIFT_CARD", "Gift Certificate");
	public static final EnumOfferType WINDOW_STEERING = new EnumOfferType("WINDOW_STEERING", "Window Steering");
	public static final EnumOfferType WAIVE_DLV_CHARGE = new EnumOfferType("WAIVECHARGE", "Waive Delivery Charge");
	public static final EnumOfferType DP_EXTN = new EnumOfferType("DPETXN", "Delivery Pass Extension");
	public static final EnumOfferType GENERIC = new EnumOfferType("GENERIC", "All other header promotions");
	public static final EnumOfferType DOLLAR_OPTIONS = new EnumOfferType("DOLLAR_OPTIONS", "Dollar Off Options");	
	public static final EnumOfferType PRODUCT_SAMPLE = new EnumOfferType("PRODUCT_SAMPLE", "free product samples");

	private final String description;

	public EnumOfferType(String name, String description) {
		super(name);
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}
	
	public static EnumOfferType getEnum(String name) {
		return (EnumOfferType) getEnum(EnumOfferType.class, name);
	}

	public static Map getEnumMap() {
		return getEnumMap(EnumOfferType.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumOfferType.class);
	}

	public static Iterator iterator() {
		return iterator(EnumOfferType.class);
	}

}

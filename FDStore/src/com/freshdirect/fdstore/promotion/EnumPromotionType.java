package com.freshdirect.fdstore.promotion;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

public class EnumPromotionType extends Enum {

	public static final EnumPromotionType SAMPLE = new EnumPromotionType("SAMPLE", "Sample Item Promotions", 0, false);
	public static final EnumPromotionType SIGNUP = new EnumPromotionType("SIGNUP", "Signup Promotions", 10, true);
	public static final EnumPromotionType REFERRAL= new EnumPromotionType("REFERRAL", "Referral Promotion", 20, true);	
	public static final EnumPromotionType REFERRER= new EnumPromotionType("REFERRER", "Referrer Promotion", 30, true);
	public static final EnumPromotionType GIFT_CARD= new EnumPromotionType("GIFT_CARD", "Gift Certificate", 40, true);
	public static final EnumPromotionType DCP_DISCOUNT = new EnumPromotionType("DCPD", "Dept/Category Promotions", 50, true);
	public static final EnumPromotionType REDEMPTION = new EnumPromotionType("REDEMPTION", "Redemption Code Promotions", 60, true);
	public static final EnumPromotionType LINE_ITEM = new EnumPromotionType("LINE_ITEM", "Line Item Promotions", 70, false);
	public static final EnumPromotionType WINDOW_STEERING = new EnumPromotionType("WINDOW_STEERING", "Window Steering", 80, true);
	public static final EnumPromotionType HEADER = new EnumPromotionType("HEADER", "Header Promotions", 90, false);
	public static final EnumPromotionType GENERIC = new EnumPromotionType("GENERIC", "Generic", 90, false);
	public static final EnumPromotionType WAIVE_CHARGE = new EnumPromotionType("WAIVE_CHARGE", "Waive Delivery Charge", 110, false);
	public static final EnumPromotionType DP_EXTENSION = new EnumPromotionType("DP_EXTENSION", "Delivery Pass Extension", 120, false);
	public static final EnumPromotionType PRODUCT_SAMPLE = new EnumPromotionType("PRODUCT_SAMPLE", "Sample Products", 120, false);

	private final String description;
	private final int priority;
	private final boolean obsolete;

	public EnumPromotionType(String name, String description, int priority, boolean obsolete) {
		super(name);
		this.description = description;
		this.priority = priority;
		this.obsolete = obsolete;
	}

	public String getDescription() {
		return this.description;
	}
	
	public int getPriority() {
		return priority;
	}

	public static EnumPromotionType getEnum(String name) {
		return (EnumPromotionType) getEnum(EnumPromotionType.class, name);
	}

	public static Map getEnumMap() {
		return getEnumMap(EnumPromotionType.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumPromotionType.class);
	}

	public static Iterator iterator() {
		return iterator(EnumPromotionType.class);
	}

}

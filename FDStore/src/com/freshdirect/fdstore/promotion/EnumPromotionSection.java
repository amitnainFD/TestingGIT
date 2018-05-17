package com.freshdirect.fdstore.promotion;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

public class EnumPromotionSection extends Enum {

	public static final EnumPromotionSection BASIC_INFO = new EnumPromotionSection("BASIC_INFO", "Edit basic information");
	public static final EnumPromotionSection OFFER_INFO = new EnumPromotionSection("OFFER_INFO", "Edit offer");
	public static final EnumPromotionSection CUST_REQ_INFO = new EnumPromotionSection("CUST_REQ_INFO", "Edit customer requirement");
	public static final EnumPromotionSection CART_REQ_INFO = new EnumPromotionSection("CART_REQ_INFO", "Edit cart requirement");
	public static final EnumPromotionSection DELIVERY_REQ_INFO = new EnumPromotionSection("DELIVERY_REQ_INFO", "Edit delivery requirement");
	public static final EnumPromotionSection PAYMENT_INFO = new EnumPromotionSection("PAYMENT_INFO", "Edit payment requirement");
	
	private final String description;

	public EnumPromotionSection(String name,String description) {
		super(name);
		this.description = description;
	}
	
	public static EnumPromotionSection getEnum(String name) {
		return (EnumPromotionSection) getEnum(EnumPromotionSection.class, name);
	}

	public static Map getEnumMap() {
		return getEnumMap(EnumPromotionSection.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumPromotionSection.class);
	}

	public static Iterator iterator() {
		return iterator(EnumPromotionSection.class);
	}

	public String getDescription() {
		return description;
	}
}

package com.freshdirect.fdstore.promotion;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

public class EnumPromoChangeType extends Enum {
	private static final long serialVersionUID = 5462364594504096445L;

	public static final EnumPromoChangeType CREATE = new EnumPromoChangeType("CREATE", "Create Promotion");
	public static final EnumPromoChangeType MODIFY = new EnumPromoChangeType("MODIFY", "Modify Promotion");
	public static final EnumPromoChangeType MODIFY_WS = new EnumPromoChangeType("MODIFY_WINDOWS_STEERING", "Modify Promotion from Windows Steering UI");
	public static final EnumPromoChangeType APPROVE = new EnumPromoChangeType("APPROVE", "Change Status - Approved");
	public static final EnumPromoChangeType CANCEL = new EnumPromoChangeType("CANCEL", "Change Status - Cancelling");
	public static final EnumPromoChangeType STATUS_PROGRESS = new EnumPromoChangeType("STATUS_PROGRESS", "Change Status - In Progress");
	public static final EnumPromoChangeType STATUS_TEST = new EnumPromoChangeType("STATUS_TEST", "Change Status - Test");
	public static final EnumPromoChangeType PUBLISH = new EnumPromoChangeType("PUBLISH", "Publish Promotion");
	public static final EnumPromoChangeType HOLD = new EnumPromoChangeType("HOLD", "Change Status - Hold");
	public static final EnumPromoChangeType UNHOLD = new EnumPromoChangeType("UNHOLD", "Change Status - Release Hold");
	public static final EnumPromoChangeType CANCELLED = new EnumPromoChangeType("CANCELLED", "Change Status - Cancelled");

	public static final EnumPromoChangeType CLONE = new EnumPromoChangeType("CLONE", "Clone Promotion");
	public static final EnumPromoChangeType IMPORT = new EnumPromoChangeType("IMPORT", "Import Promotion");
	
	public static final EnumPromoChangeType BASIC_INFO = new EnumPromoChangeType("BASIC_INFO", "Edit basic information");
	public static final EnumPromoChangeType OFFER_INFO = new EnumPromoChangeType("OFFER_INFO", "Edit offer");
	public static final EnumPromoChangeType CUST_REQ_INFO = new EnumPromoChangeType("CUST_REQ_INFO", "Edit customer requirement");
	public static final EnumPromoChangeType CART_REQ_INFO = new EnumPromoChangeType("CART_REQ_INFO", "Edit cart requirement");
	public static final EnumPromoChangeType DELIVERY_REQ_INFO = new EnumPromoChangeType("DELIVERY_REQ_INFO", "Edit delivery requirement");
	public static final EnumPromoChangeType PAYMENT_INFO = new EnumPromoChangeType("PAYMENT_INFO", "Edit payment requirement");
	
	private final String description;
	
	public EnumPromoChangeType(String name, String description) {
		super(name);
		this.description = description;
	}
	
	public static EnumPromoChangeType getEnum(String name) {
		return (EnumPromoChangeType) getEnum(EnumPromoChangeType.class, name);
	}

	@SuppressWarnings("unchecked")
	public static Map getEnumMap() {
		return getEnumMap(EnumPromoChangeType.class);
	}

	@SuppressWarnings("unchecked")
	public static List getEnumList() {
		return getEnumList(EnumPromoChangeType.class);
	}

	@SuppressWarnings("unchecked")
	public static Iterator iterator() {
		return iterator(EnumPromoChangeType.class);
	}

	public String getDescription() {
		return description;
	}
}

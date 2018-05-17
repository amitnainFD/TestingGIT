package com.freshdirect.fdstore.ecoupon;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

public class EnumCouponStatus extends Enum implements Serializable {
	
	public final static EnumCouponStatus					COUPON_ACTIVE							= new EnumCouponStatus( "COUPON_ACTIVE", "coupon active", false);
	public final static EnumCouponStatus					COUPON_CLIPPED_ACTIVE					= new EnumCouponStatus( "COUPON_CLIPPED_ACTIVE", "coupon clipped", false);
	public final static EnumCouponStatus					COUPON_CLIPPED_PENDING					= new EnumCouponStatus( "COUPON_CLIPPED_PENDING", "redeem Pending", false);
	public final static EnumCouponStatus					COUPON_CLIPPED_EXPIRED					= new EnumCouponStatus( "COUPON_CLIPPED_EXPIRED", "coupon expired", true);
	public final static EnumCouponStatus					COUPON_CLIPPED_REDEEMED					= new EnumCouponStatus( "COUPON_CLIPPED_REDEEMED", "redeemed", false);
	
	public final static EnumCouponStatus					COUPON_CLIPPED_FILTERED					= new EnumCouponStatus( "COUPON_CLIPPED_FILTERED", "Not valid on chosen delivery date", false);
	
	public final static EnumCouponStatus					COUPON_APPLIED						    = new EnumCouponStatus( "COUPON_APPLIED", "coupon applied", true);//Runtime status
	public final static EnumCouponStatus					COUPON_MIN_QTY_NOT_MET					= new EnumCouponStatus( "COUPON_MIN_QTY_NOT_MET", "min qty not met", true);//Runtime status
	
	private final String description;
	private final boolean displayMessage;
	
	public EnumCouponStatus(String name, String description, boolean displayMessage) {
		super(name);
		this.description = description;
		this.displayMessage = displayMessage;
	}
	
	public static EnumCouponStatus getEnum(String name) {
		return (EnumCouponStatus) getEnum(EnumCouponStatus.class, name);
	}

	public static Map getEnumMap() {
		return getEnumMap(EnumCouponStatus.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumCouponStatus.class);
	}

	public static Iterator iterator() {
		return iterator(EnumCouponStatus.class);
	}

	public String getDescription() {
		return description;
	}
	
	public boolean isDisplayMessage() {
		return displayMessage;
	}
	
}

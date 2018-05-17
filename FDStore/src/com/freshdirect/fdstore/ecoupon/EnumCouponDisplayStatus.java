package com.freshdirect.fdstore.ecoupon;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

public class EnumCouponDisplayStatus extends Enum implements Serializable {
	
	public final static EnumCouponDisplayStatus					COUPON_CLIPPABLE							= new EnumCouponDisplayStatus( "COUPON_CLIPPABLE", "Coupon Clippable" );
	public final static EnumCouponDisplayStatus					COUPON_CLIPPED_DISABLED						= new EnumCouponDisplayStatus( "COUPON_CLIPPED_DISABLED", "Coupon clipped disabled" );
	public final static EnumCouponDisplayStatus					COUPON_USED_DONOTDISPLAY					= new EnumCouponDisplayStatus( "COUPON_USED_DONOTDISPLAY", "Coupon used donot display" );
	
	private final String description;
	public EnumCouponDisplayStatus(String name, String description) {
		super(name);
		this.description = description;
	}
	
	public static EnumCouponDisplayStatus getEnum(String name) {
		return (EnumCouponDisplayStatus) getEnum(EnumCouponDisplayStatus.class, name);
	}

	public static Map getEnumMap() {
		return getEnumMap(EnumCouponDisplayStatus.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumCouponDisplayStatus.class);
	}

	public static Iterator iterator() {
		return iterator(EnumCouponDisplayStatus.class);
	}

	public String getDescription() {
		return description;
	}	

}

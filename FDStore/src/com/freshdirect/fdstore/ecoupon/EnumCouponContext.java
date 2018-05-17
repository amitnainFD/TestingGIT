package com.freshdirect.fdstore.ecoupon;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

public class EnumCouponContext extends Enum implements Serializable {
	
	public final static EnumCouponContext					PRODUCT							= new EnumCouponContext( "PRODUCT", "Coupon in Product Display" );
	public final static EnumCouponContext					VIEWCART					= new EnumCouponContext( "VIEWCART", "Coupon Regular Cart Line Item" );
	public final static EnumCouponContext					CHECKOUT					= new EnumCouponContext( "CHECKOUT", "Coupon Checkout Cart Line Item" );
	public final static EnumCouponContext					VIEWORDER					= new EnumCouponContext( "VIEWORDER", "Coupon View Order Line Item" );
	
	private final String description;
	public EnumCouponContext(String name, String description) {
		super(name);
		this.description = description;
	}
	
	public static EnumCouponContext getEnum(String name) {
		return (EnumCouponContext) getEnum(EnumCouponContext.class, name);
	}

	public static Map getEnumMap() {
		return getEnumMap(EnumCouponContext.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumCouponContext.class);
	}

	public static Iterator iterator() {
		return iterator(EnumCouponContext.class);
	}

	public String getDescription() {
		return description;
	}	

}

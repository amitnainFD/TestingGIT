package com.freshdirect.fdstore.promotion;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

public class EnumOrderType extends Enum {

	public final static EnumOrderType HOME = new EnumOrderType("HOME");
	public final static EnumOrderType DEPOT = new EnumOrderType("DEPOT");
	public final static EnumOrderType PICKUP = new EnumOrderType("PICKUP");
	public final static EnumOrderType CORPORATE = new EnumOrderType("CORPORATE");
	public final static EnumOrderType FDX = new EnumOrderType("FDX");

	public EnumOrderType(String name) {
		super(name);
	}

	public static EnumOrderType getEnum(String name) {
		return (EnumOrderType) getEnum(EnumOrderType.class, name);
	}

	public static Map getEnumMap() {
		return getEnumMap(EnumOrderType.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumOrderType.class);
	}

	public static Iterator iterator() {
		return iterator(EnumOrderType.class);
	}

}

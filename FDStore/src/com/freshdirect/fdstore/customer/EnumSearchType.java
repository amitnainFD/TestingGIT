package com.freshdirect.fdstore.customer;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang.enums.Enum;

public class EnumSearchType extends Enum{
	
	public static EnumSearchType COMPANY_SEARCH = new EnumSearchType("COMPANY_SEARCH");
	public static EnumSearchType RESERVATION_SEARCH = new EnumSearchType("RESERVATION_SEARCH");
	public static EnumSearchType EXEC_SUMMARY_SEARCH = new EnumSearchType("EXEC_SUMMARY_SEARCH");
	public static EnumSearchType GIFTCARD_SEARCH = new EnumSearchType("GIFTCARD_SEARCH");

	protected EnumSearchType(String name) {
		super(name);
	}
	
	public static EnumSearchType getEnum(String name) {
		return (EnumSearchType) getEnum(EnumSearchType.class, name);
	}

	public static Map getEnumMap() {
		return getEnumMap(EnumSearchType.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumSearchType.class);
	}
}

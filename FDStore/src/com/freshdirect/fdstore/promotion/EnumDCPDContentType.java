package com.freshdirect.fdstore.promotion;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

public class EnumDCPDContentType extends Enum {

	public static final EnumDCPDContentType DEPARTMENT = new EnumDCPDContentType("Department", "Deparments");
	public static final EnumDCPDContentType CATEGORY = new EnumDCPDContentType("Category", "Categories");
	public static final EnumDCPDContentType RECIPE= new EnumDCPDContentType("Recipe", "Recipes");
	public static final EnumDCPDContentType SKU = new EnumDCPDContentType("Sku","Skus");
	public static final EnumDCPDContentType BRAND= new EnumDCPDContentType("Brand", "Brands");
		    
	private final String description;

	public EnumDCPDContentType(String name, String description) {
		super(name);
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public static EnumDCPDContentType getEnum(String name) {
		return (EnumDCPDContentType) getEnum(EnumDCPDContentType.class, name);
	}

	public static Map getEnumMap() {
		return getEnumMap(EnumDCPDContentType.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumDCPDContentType.class);
	}

	public static Iterator iterator() {
		return iterator(EnumDCPDContentType.class);
	}

}

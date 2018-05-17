/*
 * $Workfile$
 *
 * $Date$
 * 
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.content.util;; 
 
/**
 * Type-safe enumeration for alcoholic content.
 *
 * @version $Revision$
 * @author $Author$
 */
public class EnumWineSortType implements java.io.Serializable {
	public final static EnumWineSortType NONE   = new EnumWineSortType("none",  "none", false);
	public final static EnumWineSortType ABC   = new EnumWineSortType("sort_abc",  "ABC", false);
	public final static EnumWineSortType PRICE   = new EnumWineSortType("sort_price", "Price", false);
	public final static EnumWineSortType RATING   = new EnumWineSortType("sort_rating", "Rating", true);
	public final static EnumWineSortType REGION   = new EnumWineSortType("sort_region", "Region", false);
	public final static EnumWineSortType VARIETY   = new EnumWineSortType("sort_variety", "Variety", false);
	public final static EnumWineSortType VINTAGE   = new EnumWineSortType("sort_vintage", "Vintage", false);


	private final String code;
	private final String name;
    private final boolean sortDesc;
    
    public static EnumWineSortType getWineSortType(String code) {
        if (ABC.getCode().equalsIgnoreCase(code))
            return ABC;
        else if (PRICE.getCode().equalsIgnoreCase(code))
            return PRICE;
        else if (RATING.getCode().equalsIgnoreCase(code))
            return RATING;
        else if (REGION.getCode().equalsIgnoreCase(code))
            return REGION;
        else if (VARIETY.getCode().equalsIgnoreCase(code))
            return VARIETY;
        else if (VINTAGE.getCode().equalsIgnoreCase(code))
            return VINTAGE;
        else
            return NONE;
    }

	private EnumWineSortType(String code, String name, boolean sortDesc) {
		this.code = code;
		this.name = name;
		this.sortDesc = sortDesc;
	}

	public String getCode() {
		return this.code;
	}
    
    public String getName() {
        return this.name;
    }
	
    public boolean getSortDesc() {
        return this.sortDesc;
    }
	public String toString() {
		return this.name;
	}

	public boolean equals(Object o) {
		if (o instanceof EnumWineSortType) {
			return this.code.equals(((EnumWineSortType)o).code);
		}
		return false;
	}

}
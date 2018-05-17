package com.freshdirect.fdstore.content.util;

/**@author ekracoff*/
public class SortStrategyElement {
		
	public final static int PRODUCTS_BY_PRIORITY = 0;
	public final static int PRODUCTS_BY_PRICE = 1;
	public final static int PRODUCTS_BY_NAME = 2;
	public final static int PRODUCTS_BY_KOSHER = 3;
	public final static int PRODUCTS_BY_NUTRITION = 4;
	//public final static int PRODUCTS_BY_ATTRIBUTE = 5;
	public final static int PRODUCTS_BY_WINE_ATTRIBUTE = 6;
	public final static int PRODUCTS_BY_RATING = 7;
	public final static int PRODUCTS_BY_DOMAIN_RATING = 8;
	public final static int PRODUCTS_BY_WINE_COUNTRY = 9;
	public final static int PRODUCTS_BY_SEAFOOD_SUSTAINABILITY = 10;

	public final static int PRODUCTS_BY_POPULARITY = 11;
	public final static int PRODUCTS_BY_SALE = 12;	
	
	public final static int GROUP_BY_CATEGORY_NAME = 200;
	public final static int GROUP_BY_CATEGORY_PRIORITY = 201;
	public final static int GROUP_BY_AVAILABILITY = 202;
	
	public final static int NO_SORT = 99999;
	
	public final static String SORTNAME_GLANCE = "glance";
	public final static String SORTNAME_NAV    = "nav";
	public final static String SORTNAME_FULL   = "full";
	
	private final int sortType;
	private final boolean sortDescending;
	private final String secondaryAttrib;
	private final String multiAttribName;
	
	public SortStrategyElement(int sortType){
		this(sortType, null, null, false);
	}
	
	public SortStrategyElement(int sortType, boolean sortDescending){
		this(sortType, null, null, sortDescending);
	}
	
	public SortStrategyElement(int sortType, String secondaryAttrib, boolean sortDescending){
		this(sortType, secondaryAttrib, null, sortDescending);
	}
	
	public SortStrategyElement(int sortType, String secondaryAttrib, String multiAttribName, boolean sortDescending){
		this.sortType = sortType;
		this.secondaryAttrib = secondaryAttrib;
		this.sortDescending = sortDescending;
		this.multiAttribName = multiAttribName;
	}

    public SortStrategyElement(int sortType, boolean sortDescending, String multiAttribName){
        this.sortType = sortType;
        this.sortDescending = sortDescending;
        this.secondaryAttrib = null;
        this.multiAttribName = multiAttribName;
    }
	
	public int getSortType(){
		return sortType;
	}
	
	public boolean sortDescending() {
		return sortDescending;
	}

	public String getSecondayAttrib(){
		return secondaryAttrib;
	}
	
	public String getMultiAttribName(){
		return multiAttribName;
	}
	@Override
    public String toString() {
        return "SortStrategy[type:" + sortType + (multiAttribName != null ? ",attr=" + multiAttribName : "")
                + (secondaryAttrib != null ? ",secondary=" + secondaryAttrib : "") + ",descending=" + sortDescending + "]";
    }

}

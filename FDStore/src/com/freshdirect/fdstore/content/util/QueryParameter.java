package com.freshdirect.fdstore.content.util;

import java.io.Serializable;

public class QueryParameter implements Serializable {
	private static final long serialVersionUID = -1993032840954192897L;

	public static final String PRODUCT_ID = "productId";
	public static final String CAT_ID = "catId";
	public static final String DEPT_ID = "deptId";

	public static final String TRK = "trk";
	public static final String TRKD = "trkd";

	public static final String WINE_FILTER = "wineFilter";
	public static final String WINE_FILTER_CLICKED = "wineFilterClicked";
	public static final String WINE_SORT_BY = "wineSortBy";
	public static final String WINE_VIEW = "wineView";
	public static final String WINE_PAGE_SIZE = "winePageSize";
	public static final String WINE_PAGE_NO = "winePageNo";
	public static final String WINE_CLEAR_FILTER_CLICKED = "wineClearFilterClicked";
	
	public static final String GENERIC_FILTER = "genericFilter";

	private final String name;
	private final String value;

	public QueryParameter(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryParameter other = (QueryParameter) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
}

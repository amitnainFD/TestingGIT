package com.freshdirect.fdstore.util;

import com.freshdirect.fdstore.content.SearchSortType;

public interface ProductPagerNavigator {
	public String getSearchTerm();

	public String getUpc();

	public boolean isEmptySearch();
	
	public int getPageSize();

	public int getPageOffset();

	public int getPageNumber();

	public String getCategory();

	public String getDepartment();

	public String getBrand();

	public String getRecipeFilter();

	public boolean isProductsFiltered();

	public boolean isRecipesFiltered();

	public boolean isSortOrderingAscending();
	
	public SearchSortType getSortBy();

	public boolean isFromDym();
	
	public boolean isRefined();
}

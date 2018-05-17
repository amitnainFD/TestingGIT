/**
 * 
 */
package com.freshdirect.fdstore.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.freshdirect.fdstore.content.SearchSortType;

/**
 * @author skrishnasamy
 *
 */
public class NewProductsNavigator extends AbstractNavigator {

	/**
	 * @param request
	 */
	public NewProductsNavigator(HttpServletRequest request) {
		super(request);
	}

	/**
	 * @param searchAction
	 * @param terms
	 * @param view
	 * @param dept
	 * @param cat
	 * @param brand
	 * @param rcp
	 * @param psize
	 * @param page
	 * @param sort
	 * @param ascend
	 */
	public NewProductsNavigator(String searchAction, int view,
			String dept, String cat, String brand, String rcp, int psize,
			int page, SearchSortType sort, boolean ascend, boolean refined) {
		super(searchAction, view, dept, cat, brand, rcp, psize, page, sort, ascend, refined);
	}

	/* (non-Javadoc)
	 * @see com.freshdirect.fdstore.util.AbstractNavigator#clone()
	 */
	@Override
	public Object clone() {
		return new NewProductsNavigator(searchAction, view, deptFilter, categoryFilter, brandFilter, recipeFilter, pageSize, pageNumber, sortBy, isOrderAscending, refined);
	}

	/* (non-Javadoc)
	 * @see com.freshdirect.fdstore.util.AbstractNavigator#dup()
	 */
	@Override
	public AbstractNavigator dup() {
		return new NewProductsNavigator(searchAction, view, deptFilter, categoryFilter, brandFilter, recipeFilter, pageSize, pageNumber, sortBy, isOrderAscending, refined);
	}

	/* (non-Javadoc)
	 * @see com.freshdirect.fdstore.util.AbstractNavigator#getSortBar()
	 */
	@Override
	public SortDisplay[] getSortBar() {
		SortDisplay[] sortDisplayBar = new SortDisplay[3];
		sortDisplayBar[0] = new SortDisplay(SearchSortType.BY_RECENCY, isDefaultSort(), isSortOrderingAscending(), "Recency", "Recently Added", "Least Recent");
		sortDisplayBar[1] = new SortDisplay(SearchSortType.BY_NAME, isSortByName(), isSortOrderingAscending(), "Name", "Name (A-Z)", "Name (Z-A)");
		sortDisplayBar[2] = new SortDisplay(SearchSortType.BY_PRICE, isSortByPrice(), isSortOrderingAscending(), "Price", "Price (low)", "Price (high)");
		return sortDisplayBar;
	}

	/* (non-Javadoc)
	 * @see com.freshdirect.fdstore.util.AbstractNavigator#init(java.util.Map)
	 */
	@Override
	protected void init(Map<String, String> params) {
		super.init(params);
	}

	public SearchSortType getDefaultSortType(){
		return SearchSortType.BY_RECENCY;
	}
	
	public String getDefaultViewName() {
		return getViewName(VIEW_GRID);
	}
	
}

package com.freshdirect.fdstore.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.content.SearchSortType;

/**
 * Navigator component for search page.
 * 
 * @author segabor
 * 
 */
public class SearchNavigator implements ProductPagerNavigator {
	public static final Logger LOGGER = Logger.getLogger(SearchNavigator.class);

	public static final String RECIPES_DEPT = "rec";

	private String searchTerm;
	private String upc;

	public static final int VIEW_LIST = 0;
	public static final int VIEW_GRID = 1;
	public static final int VIEW_TEXT = 2;
	public static final int VIEW_DEFAULT = VIEW_GRID;

	public static SearchNavigator mock(String searchTerm, String upc, int view, String departmentId,
			String categoryId, String brandId, String recipeClassification, int pageSize, int page, SearchSortType sort,
			boolean ascend, boolean fromDym, boolean refined) {
		if (pageSize < 0)
			switch (view) {
				case VIEW_LIST:
					pageSize = 30;
					break;
				case VIEW_GRID:
					pageSize = 40;
					break;
				default:
					pageSize = 0;
			}		
		return new SearchNavigator(searchTerm, upc, view, departmentId, categoryId, brandId, recipeClassification,
				pageSize, page, sort, ascend, fromDym, refined);
	}
	
	int view = VIEW_DEFAULT;

	String deptFilter; // Note: Department OR Category filter. They cannot exist at once
	String categoryFilter;
	String recipeFilter;

	String brandFilter;
	boolean fromDym;
	boolean refined;

	int pageSize = 0; // 0 means show all in one page
	int pageNumber = 0;

	SearchSortType sortBy;
	boolean isOrderAscending;

	public class SortDisplay {
		public final SearchSortType sortType; // == SORT_BY_<sort type>
		public final boolean isSelected;
		public final boolean ascending; // Display ascending
		public final String text; // display name

		public SortDisplay(SearchSortType sortType, boolean isSelected, boolean ascending, String text, String ascText,
				String descText) {
			this.sortType = sortType;
			this.isSelected = isSelected;
			this.ascending = ascending;
			this.text = isSelected ? (ascending ? ascText : descText) : text;
		}
	}

	public SearchNavigator(ServletRequest servletRequest) {
		Map<String, String> p = new HashMap<String, String>(servletRequest.getParameterMap().size());
		for (Enumeration<?> it = servletRequest.getParameterNames(); it.hasMoreElements();) {
			String key = (String) it.nextElement();
			String value = servletRequest.getParameterValues(key)[0];
			p.put(key, value);
		}

		init(p);
	}

	/**
	 * Clone support
	 */
	protected SearchNavigator(String terms, String upc, int view, String dept, String cat, String brand,
			String rcp, int psize, int page, SearchSortType sort, boolean ascend, boolean fromDym, boolean refined) {
		this.searchTerm = terms;
		this.upc = upc;
		this.view = view;
		this.deptFilter = dept;
		this.categoryFilter = cat;
		this.brandFilter = brand;
		this.recipeFilter = rcp;
		this.pageSize = psize;
		this.pageNumber = page;
		this.sortBy = sort;
		this.isOrderAscending = ascend;
		this.fromDym = fromDym;
		this.refined = refined;
	}

	public Object clone() {
		return new SearchNavigator(searchTerm, upc, view, deptFilter, categoryFilter, brandFilter, recipeFilter,
				pageSize, pageNumber, sortBy, isOrderAscending, fromDym, refined);
	}

	/**
	 * Convenience method of clone()
	 */
	public SearchNavigator dup() {
		return (SearchNavigator) new SearchNavigator(searchTerm, upc, view, deptFilter, categoryFilter, brandFilter,
				recipeFilter, pageSize, pageNumber, sortBy, isOrderAscending, fromDym, refined);
	}

	/**
	 * Build navigator from request parameters
	 * 
	 * @param params
	 */
	private void init(Map<String, String> params) {
		String val;

		/* search terms */
		val = (String) params.get("searchParams");
		if (val != null && val.trim().length() > 0) {
			searchTerm = val.trim();
		}

		val = (String) params.get("upc");
		if (val != null && val.trim().length() > 0) {
			upc = val.trim();
		}

		/* view */
		val = (String) params.get("view");
		if (val != null && val.length() > 0) {
			if ("list".equalsIgnoreCase(val)) {
				view = VIEW_LIST;
			} else if ("grid".equalsIgnoreCase(val)) {
				view = VIEW_GRID;
			} else if ("text".equalsIgnoreCase(val)) {
				view = VIEW_TEXT;
			}
		} else {
			// default view
			view = VIEW_DEFAULT;
		}

		/* paging parameters */

		val = (String) params.get("pageSize");
		if (val != null && val.length() > 0) {
			pageSize = Integer.parseInt(val);
		} else {
			if (view == VIEW_LIST) {
				pageSize = 30;
			} else if (view == VIEW_GRID) {
				pageSize = 40;
			} else {
				pageSize = 0;
			}
		}

		// start = offset*page size
		val = (String) params.get("start");
		if (val != null && val.length() > 0) {
			int offset = Integer.parseInt(val);
			pageNumber = offset / pageSize;
		}

		// offset = page number
		val = (String) params.get("offset");
		if (val != null && val.length() > 0) {
			pageNumber = Integer.parseInt(val);
		}

		/* filters */

		val = (String) params.get("catId");
		if (val != null && val.length() > 0) {
			categoryFilter = val;
			deptFilter = null;
			refined = true;
		}

		val = (String) params.get("deptId");
		if (val != null && val.length() > 0) {
			categoryFilter = null;
			deptFilter = val;
			refined = true;
		}

		if (RECIPES_DEPT.equalsIgnoreCase(deptFilter)) {
			val = (String) params.get("classification");
			if (val != null && val.length() > 0) {
				recipeFilter = val;
				refined = true;
			}
		}

		val = (String) params.get("brandValue");
		if (val != null && val.length() > 0) {
			brandFilter = val;
			refined = true;
		}

		/* sort */

		val = (String) params.get("sort");
		if (val != null && val.length() > 0) {
			sortBy = SearchSortType.findByLabel(val) /* SearchNavigator.convertToSort(val) */;
			if (sortBy == null)
				sortBy = SearchSortType.DEFAULT;

			System.err.println("DEBUG: sort = " + sortBy.getLabel());

			/*
			 * if (sortBy < 0 || sortBy > SORT_BY_SALE) { sortBy = SORT_DEFAULT; }
			 */
		} else {
			if (RECIPES_DEPT.equalsIgnoreCase(deptFilter)) {
				sortBy = SearchSortType.DEF4RECIPES /* SORT_DEFAULT_RECIPE */;
			} else {
				// sortBy = (view == VIEW_TEXT ? SORT_DEFAULT_TEXT : SORT_DEFAULT_NOT_TEXT);
				sortBy = (view == VIEW_TEXT ? SearchSortType.DEF4TEXT : SearchSortType.DEF4NOTTEXT);
			}
		}

		val = (String) params.get("order");
		if (val != null && val.length() > 0) {
			isOrderAscending = !("desc".equalsIgnoreCase(val));
		} else {
			isOrderAscending = true;
		}
		
		val = (String) params.get("fromDym");
		if (val != null && val.length() > 0)
			fromDym = true;

		val = (String) params.get("refinement");
		if (val != null && val.length() > 0)
			refined = true;
	}

	public SortDisplay[] getSortBar() {
		SortDisplay[] sortDisplayBar;

		if (this.isTextView()) {
			sortDisplayBar = new SortDisplay[6];

			sortDisplayBar[0] = new SortDisplay(SearchSortType.DEFAULT, isDefaultSort(), isSortOrderingAscending(), "Default",
					"Default", "Default");
			sortDisplayBar[1] = new SortDisplay(SearchSortType.BY_RELEVANCY, isSortByRelevancy(), isSortOrderingAscending(),
					"Relevance", "Most Relevant", "Least Relevant");
			sortDisplayBar[2] = new SortDisplay(SearchSortType.BY_NAME, isSortByName(), isSortOrderingAscending(), "Name",
					"Name (A-Z)", "Name (Z-A)");
			sortDisplayBar[3] = new SortDisplay(SearchSortType.BY_PRICE, isSortByPrice(), isSortOrderingAscending(), "Price",
					"Price (low)", "Price (high)");
			sortDisplayBar[4] = new SortDisplay(SearchSortType.BY_POPULARITY, isSortByPopularity(), isSortOrderingAscending(),
					"Popularity", "Most Popular", "Least Popular");
			sortDisplayBar[5] = new SortDisplay(SearchSortType.BY_SALE, isSortBySale(), isSortOrderingAscending(), "Sale",
					"Sale (yes)", "Sale (no)");
		} else {
			sortDisplayBar = new SortDisplay[5];

			sortDisplayBar[0] = new SortDisplay(SearchSortType.BY_RELEVANCY, isDefaultSort(), isSortOrderingAscending(),
					"Relevance", "Most Relevant", "Least Relevant");
			sortDisplayBar[1] = new SortDisplay(SearchSortType.BY_NAME, isSortByName(), isSortOrderingAscending(), "Name",
					"Name (A-Z)", "Name (Z-A)");
			sortDisplayBar[2] = new SortDisplay(SearchSortType.BY_PRICE, isSortByPrice(), isSortOrderingAscending(), "Price",
					"Price (low)", "Price (high)");
			sortDisplayBar[3] = new SortDisplay(SearchSortType.BY_POPULARITY, isSortByPopularity(), isSortOrderingAscending(),
					"Popularity", "Most Popular", "Least Popular");
			sortDisplayBar[4] = new SortDisplay(SearchSortType.BY_SALE, isSortBySale(), isSortOrderingAscending(), "Sale",
					"Sale (yes)", "Sale (no)");
		}
		return sortDisplayBar;
	}

	public String getUpc() {
		return upc;
	}
	
	public String getSearchTerm() {
		return searchTerm;
	}

	public boolean isFullPageMode() {
		return pageSize == 0;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int size) {
		if (pageSize == size)
			return;

		// preserve offset
		int offset = pageSize * pageNumber;

		if (size > 0) {
			pageSize = size;
			pageNumber = offset / size; // recalculate page number
		} else {
			pageSize = 0;
			pageNumber = 0;
		}
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int ix) {
		pageNumber = ix;
	}

	public int getPageOffset() {
		return pageSize * pageNumber;
	}

	public void setPageOffset(int offset) {
		if (pageSize > 0) {
			pageNumber = offset / pageSize;
		} else {
			pageNumber = 0;
		}
	}

	public SearchNavigator incPage() {
		pageNumber++;
		return this;
	}

	public SearchNavigator decPage() {
		if (pageNumber > 0)
			--pageNumber;
		return this;
	}

	public String getBrand() {
		return brandFilter;
	}

	public void setBrand(String brandName) {
		brandFilter = brandName;
	}

	public String getCategory() {
		return categoryFilter;
	}

	public void setCategory(String catId) {
		categoryFilter = catId;
		deptFilter = null;
		recipeFilter = null;
	}

	public String getDepartment() {
		return deptFilter;
	}

	public void setDepartment(String deptId) {
		deptFilter = deptId;
		categoryFilter = null;
		recipeFilter = null;
	}

	public String getRecipeFilter() {
		return this.recipeFilter;
	}

	public void setRecipeFilter(String rcp) {
		this.recipeFilter = rcp;
	}

	public boolean isRecipesFiltered() {
		return RECIPES_DEPT.equalsIgnoreCase(deptFilter);
	}

	public void switchView(String viewName) {
		if (viewName != null && viewName.length() > 0) {
			if ("list".equalsIgnoreCase(viewName)) {
				view = VIEW_LIST;
			} else if ("grid".equalsIgnoreCase(viewName)) {
				view = VIEW_GRID;
			} else if ("text".equalsIgnoreCase(viewName)) {
				view = VIEW_TEXT;
			}
		} else {
			// default view
			view = VIEW_DEFAULT;
		}
	}

	public int getView() {
		return view;
	}

	protected String getViewName() {
		switch (view) {
			case VIEW_LIST:
				return "list";
			case VIEW_GRID:
			default: // VIEW_DEFAULT
				return "grid";
			case VIEW_TEXT:
				return "text";
		}
	}

	public static String getViewName(int viewType) {
		switch (viewType) {
			case VIEW_LIST:
				return "list";
			case VIEW_GRID:
			default: // VIEW_DEFAULT
				return "grid";
			case VIEW_TEXT:
				return "text";
		}
	}

	public static String getDefaultViewName() {
		return getViewName(VIEW_DEFAULT);
	}

	public void setView(int viewType) {
		if (viewType == view)
			return;

		int oldView = view;

		if (viewType >= VIEW_LIST && viewType <= VIEW_TEXT) {
			view = viewType;
		} else {
			view = VIEW_DEFAULT;
		}

		// adjust sort
		if (oldView == VIEW_TEXT && (view == VIEW_GRID || view == VIEW_LIST) && sortBy == SearchSortType.DEFAULT) {
			sortBy = SearchSortType.BY_RELEVANCY /* SORT_DEFAULT_NOT_TEXT */;
			isOrderAscending = true;
		} else if ((oldView == VIEW_GRID || oldView == VIEW_LIST) && view == VIEW_TEXT && sortBy == SearchSortType.DEFAULT /* SORT_DEFAULT_NOT_TEXT */) {
			sortBy = SearchSortType.BY_RELEVANCY;
			isOrderAscending = true;
		}

		// reset page number and size to default
		pageNumber = 0;
		switch (view) {
			case VIEW_LIST:
				pageSize = 30;
				break;
			case VIEW_GRID:
				pageSize = 40;
				break;
			default:
				pageSize = 0;
		}
	}

	public boolean isListView() {
		return view == VIEW_LIST;
	}

	public boolean isGridView() {
		return view == VIEW_GRID;
	}

	public boolean isTextView() {
		return view == VIEW_TEXT;
	}

	public boolean isRecipesView() {
		// show recipes if recipes department is selected or all products are shown (no filters given)
		return RECIPES_DEPT.equalsIgnoreCase(deptFilter) || (categoryFilter == null && deptFilter == null);
	}
	
	public boolean isProductsFiltered() {
		return categoryFilter != null || (deptFilter != null && !RECIPES_DEPT.equalsIgnoreCase(deptFilter));
	}

	public SearchSortType getSortBy() {
		return sortBy;
	}

	// convenience method
	public String getSortByName() {
		return sortBy.getLabel();
	}

	public void setSortBy(SearchSortType sortType) {
		if (sortBy == sortType)
			return;

		if (view == VIEW_TEXT) {
			sortBy = (sortType == null ? SearchSortType.DEF4TEXT : sortType);
		} else if (view == VIEW_GRID || view == VIEW_LIST) {
			sortBy = (sortType == null ? SearchSortType.DEF4NOTTEXT : sortType);
		} else if (RECIPES_DEPT.equalsIgnoreCase(recipeFilter)) {
			sortBy = SearchSortType.DEF4RECIPES;
		} else {
			sortBy = SearchSortType.DEF4NOTTEXT;
		}

		isOrderAscending = true; // reset order direction
	}

	public boolean isSortByRelevancy() {
		return sortBy == SearchSortType.BY_RELEVANCY;
	}

	public boolean isSortByName() {
		return sortBy == SearchSortType.BY_NAME;
	}

	public boolean isSortByPrice() {
		return sortBy == SearchSortType.BY_PRICE;
	}

	public boolean isSortByPopularity() {
		return sortBy == SearchSortType.BY_POPULARITY;
	}

	public boolean isSortBySale() {
		return sortBy == SearchSortType.BY_SALE;
	}

	public boolean isDefaultSort() {
		return (view == VIEW_TEXT && sortBy == SearchSortType.DEF4TEXT)
				|| ((view == VIEW_GRID || view == VIEW_LIST) && sortBy == SearchSortType.DEF4NOTTEXT)
				|| (RECIPES_DEPT.equalsIgnoreCase(deptFilter) && sortBy == SearchSortType.DEF4RECIPES);
	}

	public boolean isTextViewDefault() {
		return view == VIEW_TEXT && sortBy == SearchSortType.DEF4TEXT;
	}

	public boolean isSortOrderingAscending() {
		return isOrderAscending;
	}

	public SearchNavigator revertSortOrdering() {
		isOrderAscending = !isOrderAscending;
		return this;
	}

	// convenience method
	private String safeURLEncode(String str) {
		try {
			return URLEncoder.encode(str, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// NOTE: this should never happen!
			return "";
		}
	}

	/**
	 * Serialize navigator state into action link
	 */
	public String getLink() {
		StringBuffer buf = new StringBuffer();

		// URL prefix - the search page
		// buf.append( (this.searchAction != null ? this.searchAction : "/search.jsp") + "?");
		buf.append("?"); // generate relative url

		if (upc != null) {
			buf.append("upc=");
			buf.append(safeURLEncode(upc));
		} else {
			// search terms
			buf.append("searchParams=");
			// buf.append(safeURLEncode(searchTerms, "ISO-8859-1"));
			// buf.append(StringUtil.escapeUri(searchTerms));
			buf.append(safeURLEncode(searchTerm));
		}

		if (view != VIEW_DEFAULT) {
			buf.append("&amp;view=" + getViewName());
		}

		if (!(view == VIEW_TEXT || (view == VIEW_LIST && pageSize == 30) || (view == VIEW_GRID && pageSize == 40))) {
			buf.append("&amp;pageSize=");
			buf.append(pageSize);
		}
		if (pageSize > 0 && pageNumber > 0) {
			buf.append("&amp;start=");
			buf.append(pageSize * pageNumber);
		}

		if (deptFilter != null) {
			buf.append("&amp;deptId=");
			buf.append(deptFilter);
		} else if (categoryFilter != null) {
			buf.append("&amp;catId=");
			buf.append(categoryFilter);
		}
		if (brandFilter != null) {
			buf.append("&amp;brandValue=");
			buf.append(brandFilter);
		}

		if (RECIPES_DEPT.equalsIgnoreCase(deptFilter) && recipeFilter != null) {
			buf.append("&amp;classification=");
			buf.append(recipeFilter);
		}

		// no sort options in recipes view, ignore them
		if (!isDefaultSort()) {
			buf.append("&amp;sort=" + sortBy.getLabel());
		}

		if (!isOrderAscending) {
			buf.append("&amp;order=desc");
		}

		if (fromDym)
			buf.append("&amp;fromDym=1");

		buf.append("&amp;refinement=1");

		return buf.toString();
	}

	public void appendToBrandForm(JspWriter out) throws IOException {
		if (view != VIEW_DEFAULT) {
			append(out, "view", getViewName());
		}

		if (deptFilter != null) {
			append(out, "deptId", deptFilter);
		} else if (categoryFilter != null) {
			append(out, "catId", categoryFilter);
		}

		append(out, "refinement", "1");
	}

	private void append(JspWriter out, String name, String value) throws IOException {
		if (out != null && name != null && value != null) {
			out.write("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\">\n");
		}
	}

	//
	// ACTIONS (LINKS)
	//

	public String getNextPageAction() {
		return dup().incPage().getLink();
	}

	public String getPrevPageAction() {
		return dup().decPage().getLink();
	}

	public String getPageSizeAction(int size) {
		SearchNavigator n = dup();
		n.setPageSize(size);
		return n.getLink();
	}

	public String getSwitchViewAction(int viewType) {
		SearchNavigator n = dup();
		n.setView(viewType);
		return n.getLink();
	}

	public String getRevertedOrderingAction() {
		return dup().revertSortOrdering().getLink();
	}

	public String getChangeSortAction(SearchSortType sortType) {
		SearchNavigator n = dup();
		if (n.getSortBy() != sortType) {
			n.setSortBy(sortType);
		} else {
			// 'Default' sort has no reverse ordering
			if (SearchSortType.DEFAULT != n.getSortBy())
				n.revertSortOrdering();
		}
		n.pageNumber = 0;
		return n.getLink();
	}

	public String getDepartmentAction(String deptId) {
		SearchNavigator n = dup();

		n.setDepartment(deptId);

		// reset params
		n.setBrand(null);
		n.setPageNumber(0);

		return n.getLink();
	}

	public String getCategoryAction(String catId) {
		SearchNavigator n = dup();

		n.setCategory(catId);

		// reset params
		n.setBrand(null);
		n.setPageNumber(0);

		return n.getLink();
	}

	public String getRecipesAction() {
		SearchNavigator n = dup();

		n.setDepartment(RECIPES_DEPT);

		// reset params
		n.setBrand(null);
		n.setPageNumber(0);

		// reset sort
		n.setSortBy(SearchSortType.DEF4RECIPES);

		return n.getLink();
	}

	public String getUnfilteredPageAction() {
		// delete department and category filters
		return getDepartmentAction(null);
	}

	public String getJumpToPageAction(int offset) {
		SearchNavigator n = dup();

		n.setPageOffset(offset);

		return n.getLink();
	}

	public String getJumpToFilteredRecipesAction(String filter) {
		SearchNavigator n = dup();

		n.setDepartment(RECIPES_DEPT);
		n.setRecipeFilter(filter);

		// reset sort
		n.setSortBy(SearchSortType.DEF4RECIPES);

		return n.getLink();
	}

	public static int convertToView(String viewName) {
		int view = VIEW_DEFAULT;

		if ("list".equalsIgnoreCase(viewName)) {
			view = VIEW_LIST;
		} else if ("grid".equalsIgnoreCase(viewName)) {
			view = VIEW_GRID;
		} else if ("text".equalsIgnoreCase(viewName)) {
			view = VIEW_TEXT;
		}

		return view;
	}

	public static String convertToViewName(int viewType) {
		switch (viewType) {
			case VIEW_LIST:
				return "list";
			case VIEW_GRID: // == VIEW_DEFAULT
			default: // VIEW_GRID == VIEW_DEFAULT
				return "grid";
			case VIEW_TEXT:
				return "text";
		}
	}

	public static SearchSortType convertToSort(String sortName) {
		SearchSortType s = SearchSortType.findByLabel(sortName);
		if (s == null)
			s = SearchSortType.DEF4NOTTEXT;

		return s;
	}

	public static String convertToSortName(int sortType) {
		SearchSortType s = SearchSortType.findByType(sortType);
		if (s == null)
			s = SearchSortType.DEF4NOTTEXT;

		return s.getLabel();
	}

	public String toString() {
		return getLink();
	}

	public static class SearchDefaults {
		public final int smallPageSize;
		public final int normalPageSize;
		public final int view;
		public final boolean isDefaultView;

		public SearchDefaults(int view, int pageSize1, int pageSize2, boolean isDefault) {
			this.smallPageSize = pageSize1; // smaller size
			this.normalPageSize = pageSize2; // bigger size
			this.view = view; // view type - see VIEW_ prefixed constans
			this.isDefaultView = isDefault;
		}
	}

	// set defaults:
	// LIST: 15, 30
	// GRID: 20, 40
	// @type Map<String,SearchDefaults>
	public static Map<String, SearchDefaults> DEFAULTS = new HashMap<String, SearchDefaults>();

	static {
		DEFAULTS.put("list", new SearchDefaults(VIEW_LIST, 15, 30, VIEW_DEFAULT == VIEW_LIST));
		DEFAULTS.put("grid", new SearchDefaults(VIEW_GRID, 20, 40, VIEW_DEFAULT == VIEW_GRID));
	}

	public SearchDefaults getDefaults() {
		return (SearchDefaults) SearchNavigator.DEFAULTS.get(getViewName());
	}

	public static SearchDefaults getDefaultForView(int viewType) {
		switch (viewType) {
			case VIEW_LIST:
				return (SearchDefaults) SearchNavigator.DEFAULTS.get("list");
			case VIEW_GRID:
				return (SearchDefaults) SearchNavigator.DEFAULTS.get("grid");
			default:
				return null;
		}
	}
	
	public boolean isEmptySearch() {
		return searchTerm == null && upc == null;
	}

	@Override
	public boolean isFromDym() {
		return fromDym;
	}

	@Override
	public boolean isRefined() {
		return refined;
	}
}

package com.freshdirect.fdstore.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.content.SearchSortType;

/**
 * Navigator component for new products page.
 * 
 * @author skrishnasamy
 * 
 */
public abstract class AbstractNavigator implements ProductPagerNavigator {
	public static final Logger LOGGER = Logger.getLogger(AbstractNavigator.class);

	public static final String RECIPES_DEPT = "rec";

	// String searchTerms;

	public static final int VIEW_LIST = 0;
	public static final int VIEW_GRID = 1;
	public static final int VIEW_TEXT = 2;
	public static final int VIEW_DEFAULT = VIEW_GRID;

	int view = VIEW_DEFAULT;

	String deptFilter; // Note: Department OR Category filter. They cannot exist at once
	String categoryFilter;
	String brandFilter;
	String recipeFilter;
	boolean refined;

	int pageSize = 0; // 0 means show all in one page
	int pageNumber = 0;

	SearchSortType sortBy;
	boolean isOrderAscending;

	String searchAction = "/search.jsp";

	public static class SortDisplay {
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

	@SuppressWarnings("unchecked")
	public AbstractNavigator(HttpServletRequest request) {
		Map<String, String> p = new HashMap<String, String>(request.getParameterMap().size());
		for (Enumeration<String> it = request.getParameterNames(); it.hasMoreElements();) {
			String key = it.nextElement();
			String value = request.getParameterValues(key)[0];
			p.put(key, value);
		}

		searchAction = request.getRequestURI();

		init(p);
	}

	/**
	 * Clone support
	 */
	protected AbstractNavigator(String searchAction, int view, String dept, String cat, String brand, String rcp, int psize,
			int page, SearchSortType sort, boolean ascend, boolean refined) {
		if (searchAction != null)
			this.searchAction = searchAction;

		// this.searchTerms = terms;
		this.view = view;
		this.deptFilter = dept;
		this.categoryFilter = cat;
		this.brandFilter = brand;
		this.recipeFilter = rcp;
		this.pageSize = psize;
		this.pageNumber = page;
		this.sortBy = sort;
		this.isOrderAscending = ascend;
		this.refined = true;
		if (deptFilter != null || categoryFilter != null || brandFilter != null || recipeFilter != null)
			refined = true;
	}

	public abstract Object clone();

	/**
	 * Convenience method of clone()
	 */
	public abstract AbstractNavigator dup();

	/**
	 * Build navigator from request parameters
	 * 
	 * @param params
	 */
	protected void init(Map<String, String> params) {
		String val;

		/* search terms */
		/*
		 * val = (String)params.get("searchParams"); if (val != null && val.length() > 0) { searchTerms = val; }
		 */

		/* view */
		val = params.get("view");
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
			view = convertToView(getDefaultViewName());
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

		val = (String) params.get("brandValue");
		if (val != null && val.length() > 0) {
			brandFilter = val;
			refined = true;
		}

		if (RECIPES_DEPT.equalsIgnoreCase(deptFilter)) {
			val = (String) params.get("classification");
			if (val != null && val.length() > 0) {
				recipeFilter = val;
				refined = true;
			}
		}

		/* sort */
		initSort(params);

		val = (String) params.get("order");
		if (val != null && val.length() > 0) {
			isOrderAscending = !("desc".equalsIgnoreCase(val));
		} else {
			isOrderAscending = true;
		}

		val = (String) params.get("refinement");
		if (val != null && val.length() > 0)
			refined = true;
	}

	protected void initSort(Map<String, String> params) {
		String val = params.get("sort");
		if (val != null && val.length() > 0) {
			sortBy = SearchSortType.findByLabel(val) /* SearchNavigator.convertToSort(val) */;
			if (sortBy == null)
				sortBy = getDefaultSortType();

			System.err.println("DEBUG: sort = " + sortBy.getLabel());

			/*
			 * if (sortBy < 0 || sortBy > SORT_BY_SALE) { sortBy = SORT_DEFAULT; }
			 */
		} else {
			sortBy = getDefaultSortType();
		}
	}

	public abstract SortDisplay[] getSortBar();

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

	public AbstractNavigator incPage() {
		pageNumber++;
		return this;
	}

	public AbstractNavigator decPage() {
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

	public boolean isRecipesDeptSelected() {
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

	public String getDefaultViewName() {
		return getViewName(VIEW_DEFAULT);
	}

	public SearchSortType getDefaultSortType() {
		return SearchSortType.DEFAULT;
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
		sortBy = (sortType == null ? getDefaultSortType() : sortType);
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

	public boolean isSortByRecency() {
		return sortBy == SearchSortType.BY_RECENCY;
	}

	public boolean isDefaultSort() {
		return sortBy == getDefaultSortType();
	}

	public boolean isSortOrderingAscending() {
		return isOrderAscending;
	}

	public AbstractNavigator revertSortOrdering() {
		isOrderAscending = !isOrderAscending;
		return this;
	}

	/**
	 * Serialize navigator state into action link
	 */
	public String getLink() {
		StringBuffer buf = new StringBuffer();
		String amp = "";

		// URL prefix - the search page
		// buf.append( (this.searchAction != null ? this.searchAction : "/search.jsp") + "?");
		buf.append("?"); // generate relative url

		// search terms
		// buf.append("searchParams=");
		// buf.append(safeURLEncode(searchTerms, "ISO-8859-1"));
		// buf.append(StringUtil.escapeUri(searchTerms));
		// buf.append(safeURLEncode(searchTerms));

		if (view != VIEW_DEFAULT) {
			buf.append("view=" + getViewName());
			amp = "&amp;";
		}

		if (!(view == VIEW_TEXT || (view == VIEW_LIST && pageSize == 30) || (view == VIEW_GRID && pageSize == 40))) {
			buf.append(amp);
			buf.append("pageSize=");
			buf.append(pageSize);
			amp = "&amp;";
		}
		if (pageSize > 0 && pageNumber > 0) {
			buf.append(amp);
			buf.append("start=");
			buf.append(pageSize * pageNumber);
			amp = "&amp;";
		}

		if (deptFilter != null) {
			buf.append(amp);
			buf.append("deptId=");
			buf.append(deptFilter);
			amp = "&amp;";
		} else if (categoryFilter != null) {
			buf.append(amp);
			buf.append("catId=");
			buf.append(categoryFilter);
			amp = "&amp;";
		}
		if (brandFilter != null) {
			buf.append(amp);
			buf.append("brandValue=");
			buf.append(brandFilter);
			amp = "&amp;";
		}

		if (RECIPES_DEPT.equalsIgnoreCase(deptFilter) && recipeFilter != null) {
			buf.append(amp);
			buf.append("classification=");
			buf.append(recipeFilter);
			amp = "&amp;";
		}

		// no sort options in recipes view, ignore them
		if (!isDefaultSort()) {
			buf.append(amp);
			buf.append("sort=" + sortBy.getLabel());
			amp = "&amp;";
		}

		if (!isOrderAscending) {
			buf.append(amp);
			buf.append("order=desc");
			amp = "&amp;";
		}

		buf.append(amp);
		buf.append("refinement=1");
		amp = "&amp;";

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

	protected void append(JspWriter out, String name, String value) throws IOException {
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
		AbstractNavigator n = dup();
		n.setPageSize(size);
		return n.getLink();
	}

	public String getSwitchViewAction(int viewType) {
		AbstractNavigator n = dup();
		n.setView(viewType);
		return n.getLink();
	}

	public String getRevertedOrderingAction() {
		return dup().revertSortOrdering().getLink();
	}

	public String getChangeSortAction(SearchSortType sortType) {
		AbstractNavigator n = dup();
		if (n.getSortBy() != sortType) {
			n.setSortBy(sortType);
		} else {
			// 'Default' sort has no reverse ordering
			if (SearchSortType.DEFAULT != n.getSortBy())
				n.revertSortOrdering();
			// also reset paging
		}
		n.pageNumber = 0;
		return n.getLink();
	}

	public String getDepartmentAction(String deptId) {
		AbstractNavigator n = dup();

		n.setDepartment(deptId);

		// reset params
		n.setBrand(null);
		n.setPageNumber(0);

		return n.getLink();
	}

	public String getCategoryAction(String catId) {
		AbstractNavigator n = dup();

		n.setCategory(catId);

		// reset params
		n.setBrand(null);
		n.setPageNumber(0);

		return n.getLink();
	}

	public String getRecipesAction() {
		AbstractNavigator n = dup();

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
		AbstractNavigator n = dup();

		n.setPageOffset(offset);

		return n.getLink();
	}

	public String getJumpToFilteredRecipesAction(String filter) {
		AbstractNavigator n = dup();

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

	public SearchSortType convertToSort(String sortName) {
		SearchSortType s = SearchSortType.findByLabel(sortName);
		if (s == null)
			s = getDefaultSortType();

		return s;
	}

	public String convertToSortName(int sortType) {
		SearchSortType s = SearchSortType.findByType(sortType);
		if (s == null)
			s = getDefaultSortType();

		return s.getLabel();
	}

	public String toString() {
		return getLink();
	}

	public static class PageView {
		public final int smallPageSize;
		public final int normalPageSize;
		public final int view;
		public final boolean isDefaultView;

		public PageView(int view, int pageSize1, int pageSize2, boolean isDefault) {
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
	public static Map<String, PageView> DEFAULTS = new HashMap<String, PageView>();

	static {
		DEFAULTS.put("list", new PageView(VIEW_LIST, 15, 30, VIEW_DEFAULT == VIEW_LIST));
		DEFAULTS.put("grid", new PageView(VIEW_GRID, 20, 40, VIEW_DEFAULT == VIEW_GRID));
	}

	public PageView getDefaults() {
		return (PageView) AbstractNavigator.DEFAULTS.get(getViewName());
	}

	public static PageView getDefaultForView(int viewType) {
		switch (viewType) {
			case VIEW_LIST:
				return (PageView) AbstractNavigator.DEFAULTS.get("list");
			case VIEW_GRID:
				return (PageView) AbstractNavigator.DEFAULTS.get("grid");
			default:
				return null;
		}
	}

	@Override
	public String getSearchTerm() {
		return null;
	}

	@Override
	public String getUpc() {
		return null;
	}

	@Override
	public boolean isEmptySearch() {
		return false;
	}

	@Override
	public boolean isProductsFiltered() {
		return categoryFilter != null || deptFilter != null;
	}

	@Override
	public boolean isRecipesFiltered() {
		return false;
	}

	@Override
	public boolean isFromDym() {
		return false;
	}

	@Override
	public boolean isRefined() {
		return refined;
	}
}
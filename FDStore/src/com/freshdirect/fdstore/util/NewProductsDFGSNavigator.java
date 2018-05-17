/**
 * 
 */
package com.freshdirect.fdstore.util;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import com.freshdirect.fdstore.content.SearchSortType;

/**
 * @author skrishnasamy
 * This Navigator is written specific to New Products DFGS page. This class
 * overrides init() and getLink() methods.
 */
public class NewProductsDFGSNavigator extends NewProductsNavigator {

	/**
	 * @param request
	 */
	public NewProductsDFGSNavigator(HttpServletRequest request) {
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
	public NewProductsDFGSNavigator(String searchAction, int view,
			String dept, String cat, String brand, String rcp, int psize,
			int page, SearchSortType sort, boolean ascend, boolean refined) {
		super(searchAction, view, dept, cat, brand, rcp, psize, page, sort, ascend, refined);
	}

	/* (non-Javadoc)
	 * @see com.freshdirect.fdstore.util.AbstractNavigator#clone()
	 */
	@Override
	public Object clone() {
		return new NewProductsDFGSNavigator(searchAction, view, deptFilter, categoryFilter, brandFilter, recipeFilter, pageSize, pageNumber, sortBy, isOrderAscending, refined);
	}

	/* (non-Javadoc)
	 * @see com.freshdirect.fdstore.util.AbstractNavigator#dup()
	 */
	@Override
	public AbstractNavigator dup() {
		return new NewProductsDFGSNavigator(searchAction, view, deptFilter, categoryFilter, brandFilter, recipeFilter, pageSize, pageNumber, sortBy, isOrderAscending, refined);
	}

	/* (non-Javadoc)
	 * @see com.freshdirect.fdstore.util.AbstractNavigator#init(java.util.Map)
	 */
	@Override
	protected void init(Map<String, String> params) {
		String val;
		
		/* view */
		val = (String)params.get("view");
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
			pageNumber = offset/pageSize;
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
		}
		
		val = (String) params.get("deptId");
		if (val != null && val.length() > 0) {
			deptFilter = val;
		}

		val = (String) params.get("brandValue");
		if (val != null && val.length() > 0) {
			brandFilter = val;
		}

		if (RECIPES_DEPT.equalsIgnoreCase(deptFilter)) {
			val = (String) params.get("classification");
			if (val != null && val.length() > 0) {
				recipeFilter = val;
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
	}


	
	/**
	 * Serialize navigator state into action link
	 */
	public String getLink() {
		StringBuffer buf = new StringBuffer();
		String amp = "";
		
		// URL prefix - the search page
		buf.append("?"); // generate relative url
		
		if (view != VIEW_DEFAULT) {
			buf.append("view=" + getViewName());
			amp = "&amp;";
		}
		
		
		if ( !(view == VIEW_TEXT || (view == VIEW_LIST && pageSize == 30) || (view == VIEW_GRID && pageSize == 40)) ) {
			buf.append(amp);
			buf.append("pageSize=");
			buf.append(pageSize);
			amp = "&amp;";
		}
		if ( pageSize > 0 && pageNumber > 0) {
			buf.append(amp);
			buf.append("start=");
			buf.append(pageSize*pageNumber);
			amp = "&amp;";
		}


		if (deptFilter != null) {
			buf.append(amp);
			buf.append("deptId=");
			buf.append(deptFilter);
			amp = "&amp;";
		} 
		
		if (categoryFilter != null) {
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
		if ( !isDefaultSort() ) {
			buf.append(amp);
			buf.append("sort=" + sortBy.getLabel());
			amp = "&amp;";
		}

		if ( !isOrderAscending ) {
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
			super.append(out, "view", getViewName());
		}
		if (deptFilter != null) {
			append(out, "deptId", deptFilter);
		} 
		if (categoryFilter != null) {
			append(out, "catId", categoryFilter);
		}
		append(out, "refinement", "1");
	}

}

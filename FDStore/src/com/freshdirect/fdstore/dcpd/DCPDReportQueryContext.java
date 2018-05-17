package com.freshdirect.fdstore.dcpd;

import java.util.Collections;
import java.util.List;


/**
 * 
 * @author segabor
 *
 */
public class DCPDReportQueryContext {
	DCPDReportQuery query;
	
	boolean skipUnavailableItems = false; // don't display unavailable SKUs
	boolean renderCSV = false; // render CSV output instead of HTML
	boolean productsOnlyView = false; // hide left-side node tree view
	String delimiter = ",";		// CSV delimiter
	
	int prodsCnt = 0; // products counter



	public DCPDReportQueryContext(DCPDReportQuery query) {
		this.query = query;
	}

	public DCPDReportQuery getQuery() {
		return query;
	}


	public boolean isSkipUnavailableItems() {
		return skipUnavailableItems;
	}
	public void setSkipUnavailableItems(boolean skipUnavailableItems) {
		this.skipUnavailableItems = skipUnavailableItems;
	}

	public boolean isRenderCSV() {
		return renderCSV;
	}
	public void setRenderCSV(boolean renderCSV) {
		this.renderCSV = renderCSV;
	}

	public boolean isProductsOnlyView() {
		return productsOnlyView;
	}

	public void setProductsOnlyView(boolean productsOnlyView) {
		this.productsOnlyView = productsOnlyView;
	}
	
	
	public List getGoodKeys() {
		if (query != null)
			return query.getContentKeys();
		return Collections.EMPTY_LIST;
	}


	
	public int getProductsCount() {
		return this.prodsCnt;
	}
	
	public void resetProductsCounter() {
		this.prodsCnt = 0;
	}
	
	public void increaseProductsCounter() {
		this.prodsCnt++;
	}

	public void addToProductsCounter(int amount) {
		this.prodsCnt += amount;
	}


	
	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
}

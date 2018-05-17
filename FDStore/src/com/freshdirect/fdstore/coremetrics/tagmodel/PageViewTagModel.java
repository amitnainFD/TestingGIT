package com.freshdirect.fdstore.coremetrics.tagmodel;

import java.util.ArrayList;
import java.util.List;


public class PageViewTagModel extends AbstractTagModel  {
	
	private static final long	serialVersionUID	= 6256293255081926548L;

	private static final String functionName = "cmCreatePageviewTag";

	private String pageId; 
	private String categoryId;
	private String searchTerm; 
	private String searchResults;
	
	public String getPageId() {
		return pageId;
	}
	public void setPageId(String pageId) {
		this.pageId = pageId;
	}
	public String getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	public String getSearchTerm() {
		return searchTerm;
	}
	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}
	public String getSearchResults() {
		return searchResults;
	}
	public void setSearchResults(String searchResults) {
		this.searchResults = searchResults;
	}
	
	@Override
	public String getFunctionName() {
		return functionName;
	}
	
	@Override
	public List<String> toStringList() {
		List<String> pageViewData = new ArrayList<String>();
		pageViewData.add( getFunctionName() );
		pageViewData.add( getPageId() );
		pageViewData.add( getCategoryId() ); 
		pageViewData.add( getSearchTerm() );
		pageViewData.add( getSearchResults() ); 
		pageViewData.add( mapToAttrString( getAttributesMaps() ) );
		return pageViewData;
	} 
}
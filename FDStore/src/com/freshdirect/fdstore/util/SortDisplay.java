package com.freshdirect.fdstore.util;

import com.freshdirect.fdstore.content.SearchSortType;

public class SortDisplay {
	private SearchSortType sortType;
	private boolean isSelected;
	private boolean ascending;
	private String text;

	public SortDisplay(SearchSortType sortType, boolean isSelected, boolean ascending, String text, String ascText,
			String descText) {
		this.setSortType(sortType);
		this.setSelected(isSelected);
		this.setAscending(ascending);
		this.setText(isSelected ? (ascending ? ascText : descText) : text);
	}
	
	public SortDisplay(SearchSortType sortType, boolean isSelected, boolean ascending) {
		this.setSortType(sortType);
		this.setSelected(isSelected);
		this.setAscending(ascending);
		this.setText(isSelected ? (ascending ? sortType.getTextAsc() : sortType.getTextDesc()) : sortType.getText());
	}

	public SearchSortType getSortType() {
		return sortType;
	}

	public void setSortType(SearchSortType sortType) {
		this.sortType = sortType;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public boolean isAscending() {
		return ascending;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
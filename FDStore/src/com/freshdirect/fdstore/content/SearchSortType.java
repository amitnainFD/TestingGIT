package com.freshdirect.fdstore.content;

public enum SearchSortType implements SortTypeI {
    NATURAL_SORT(-1, "natr", "", "", ""),	// don't
    // sort
    BY_NAME(0, "name", "Name", "Name (A-Z)", "Name (Z-A)"),
	BY_PRICE(1, "prce", "Price", "Price (low)", "Price (high)"),
	BY_RELEVANCY(2, "relv", "Relevance", "Most Relevant", "Least Relevant"),
	BY_POPULARITY(3, "pplr", "Popularity", "Most Popular", "Least Popular"),
	DEFAULT(4, "tdef", "Default", "Default", "Default"),			// 'default' sort on text view
	BY_SALE(5, "sale", "Sale", "Sale (yes)", "Sale (no)"),
	BY_RECENCY(6, "recency", "Recency", "Most recent", "Least recent"),
	BY_OURFAVES(6, "ourFaves", "Our Favorites", "Our Favorites", "Our Favorites"),
	BY_DEPARTMENT(7, "dept", "Department", "Department", "Department"),
	BY_FREQUENCY(8, "freq", "Frequency","Frequency","Frequency" ),
	BY_EXPERT_RATING(9, "expr", "Expert Rating", "Expert Rating", "Expert Rating"),
    BY_START_DATE(10, "strd", "Date Added", "Date Added (new)", "Date Added (old)"),
	BY_EXPIRATION_DATE(11, "expr", "Expiring", "Expiring (soon)", "Expiring (later)"),
	BY_PERC_DISCOUNT(12, "poff", "% of Discount", "% of Discount (high)", "% of Discount (low)"),
	BY_DOLLAR_DISCOUNT(13, "doff", "Dollar Discount", "Dollar Discount (high)", "Dollar Discount (low)"),
	BY_PRIORITY(14, "prio", "Popularity", "Most Popular", "Least Popular");
    
    private int type;
    private String label;
    private String text;
    private String textAsc;
    private String textDesc;

    SearchSortType(int t, String label, String text, String textAsc, String textDesc) {
    	this.type = t;
    	this.label = label;
    	this.text = text;
    	this.textAsc = textAsc;
    	this.textDesc = textDesc;
    }

    public int getType() {
    	return this.type;
    }
    
    public String getLabel() {
    	return this.label;
    }
    
    public String getText() {
		return text;
	}
    
    public String getTextAsc() {
		return textAsc;
	}
    
    public String getTextDesc() {
		return textDesc;
	}
    
    public static SearchSortType findByLabel(String label) {
    	for (SearchSortType e : SearchSortType.values()) {
    		if (e.getLabel().equalsIgnoreCase(label)) {
    			return e;
    		}
    	}
    	return null;
    }
    
    public static SearchSortType findByType(int type) {
    	for (SearchSortType e : SearchSortType.values()) {
    		if (e.getType() == type) {
    			return e;
    		}
    	}
    	return null;
    }

    
    /**
     * Various defaults per view specific to search page.
     */
    public static final SearchSortType DEF4TEXT = SearchSortType.DEFAULT;
    public static final SearchSortType DEF4NOTTEXT = SearchSortType.BY_RELEVANCY;
    public static final SearchSortType DEF4RECIPES = SearchSortType.BY_NAME;
}

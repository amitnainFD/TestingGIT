package com.freshdirect.fdstore.content;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.collections.set.ListOrderedSet;

import com.freshdirect.WineUtil;
import com.freshdirect.cms.fdstore.FDContentTypes;

public enum EnumWineFilterDomain {
	TYPE("usq_type"), COUNTRY("usq_region"), PRICE, RATING, MORE("usq_more");

	private String categoryId;

	private EnumWineFilterDomain() {
		categoryId = null;
	}

	private EnumWineFilterDomain(String categoryId) {
		this.categoryId = categoryId;
	}

	public Collection<WineFilterValue> getFilterValues() {
		if (this == PRICE)
			return Arrays.asList((WineFilterValue[]) EnumWinePrice.values());
		else if (this == RATING) {			
			/**
			 * Remove NOT_RATED
			 */
			return Arrays.asList((WineFilterValue[]) EnumWineRating.values()).subList(1, EnumWineRating.values().length);
		}
		else if (categoryId != null) {
			CategoryModel category = getCategory();
			if (category != null) {
				String categoryId = category.toString();
				String wineAssId = WineUtil.getWineAssociateId().toLowerCase();

				if ("fdw".equalsIgnoreCase(wineAssId)) {
					wineAssId = "vin"; //make it match CMS setup
				}
				
				if (categoryId.startsWith("usq", 0) && !"usq".equalsIgnoreCase(wineAssId) ) {
					//flip to vin
					categoryId = categoryId.replace("usq", wineAssId);
					category = (CategoryModel) ContentFactory.getInstance().getContentNode(FDContentTypes.CATEGORY, categoryId);
					if (category == null) {
						return Collections.emptyList();
					}
				}
				Collection<WineFilterValue> values = ContentFactory.getInstance().getDomainValuesForWineDomainCategory(category);
				return values != null ? values : Collections.<WineFilterValue> emptyList();
			} else
				return Collections.emptyList();
		} else
			throw new IllegalStateException("");
	}

	public String getDomainEncoded() {
		Collection<WineFilterValue> values = getFilterValues();
		if (!values.isEmpty())
			return values.iterator().next().getDomainEncoded();

		return null;
	}

	public CategoryModel getCategory() {
		if (categoryId == null)
			return null;
		return (CategoryModel) ContentFactory.getInstance().getContentNode(FDContentTypes.CATEGORY, categoryId);
	}

	public static EnumWineFilterDomain decode(String encoded) {
		if (encoded == null)
			return null;

		for (EnumWineFilterDomain value : values())
			if (encoded.equals(value.getDomainEncoded()))
				return value;

		return null;
	}
	
	public boolean isSingleSelect() {
		if (this == TYPE || this == MORE) {
			return true;
		}
		return false;
	}
	
	public String getSubcategoryLabel() {
		if (this == TYPE) return "Varietal";
		if (this == COUNTRY) return "Region";
		return null;
	}
	
	public boolean isSubcategoryGrouped() {
		if (this == COUNTRY) return true;
		return false;
	}
	
	public Comparator<WineFilterValue> getComparator() {				
		
		switch (this) {
			case PRICE  : return new Comparator<WineFilterValue>() {
							public int compare(WineFilterValue o1, WineFilterValue o2) {
								return o1.getFilterRepresentation().length() - o2.getFilterRepresentation().length();
							}
						};
			case RATING : return new Comparator<WineFilterValue>() {
							public int compare(WineFilterValue o1, WineFilterValue o2) {
								return o2.getFilterRepresentation().length() - o1.getFilterRepresentation().length();
							}
						};
			case TYPE	:
			case MORE	: {
							Collection<WineFilterValue> values = ContentFactory.getInstance().getDomainValuesForWineDomainCategory(getCategory());
							if (values != null && values instanceof ListOrderedSet) {
								final ListOrderedSet indexes = (ListOrderedSet) values;
								return new Comparator<WineFilterValue>() {
									@Override
									public int compare(WineFilterValue o1, WineFilterValue o2) {
										int i1 = indexes.indexOf(o1);
										int i2 = indexes.indexOf(o2);
										if (i1 < 0 || i2 < 0)
											return o1.getFilterRepresentation().compareTo(o2.getFilterRepresentation());
										return i1 - i2;
									}
								};
							}
						  }
			default     : return new Comparator<WineFilterValue>() {
							public int compare(WineFilterValue o1, WineFilterValue o2) {				
								return o1.getFilterRepresentation().compareTo(o2.getFilterRepresentation());
							}
						};
		}
	}
}

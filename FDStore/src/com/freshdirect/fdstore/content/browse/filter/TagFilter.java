package com.freshdirect.fdstore.content.browse.filter;

import java.util.ArrayList;
import java.util.List;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.AbstractProductItemFilter;
import com.freshdirect.fdstore.content.FilterCacheStrategy;
import com.freshdirect.fdstore.content.FilteringProductItem;
import com.freshdirect.fdstore.content.ProductFilterModel;
import com.freshdirect.fdstore.content.TagModel;

public class TagFilter extends AbstractProductItemFilter {
	
	private List<TagModel> includeValues;
	
	public TagFilter(ProductFilterModel filterModel, String parentId) {
		super(filterModel, parentId);
		
		includeValues = new ArrayList<TagModel>();
		includeValues.add(filterModel.getTag());
	}

	public TagFilter(TagModel tagModel, String parentId) {
		super(tagModel.getContentName(), parentId, tagModel.getName());

		includeValues = new ArrayList<TagModel>();
		includeValues.add(tagModel);
	}

	@Override
	public boolean apply(FilteringProductItem prod) throws FDResourceException {
		
		for (TagModel includeValue : includeValues){
			if (prod.getProductModel().getAllTags().contains(includeValue)){
				return invertChecker(true);
			}
		}
		
		return invertChecker(false);
	}

	@Override
	public FilterCacheStrategy getCacheStrategy() {
		return FilterCacheStrategy.CMS_ONLY;
	}

}

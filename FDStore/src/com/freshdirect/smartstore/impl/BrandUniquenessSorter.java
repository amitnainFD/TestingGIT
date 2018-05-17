package com.freshdirect.smartstore.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.content.BrandModel;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.RecommendationService;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.WrapperRecommendationService;

public class BrandUniquenessSorter extends WrapperRecommendationService {
	private static final Logger LOGGER = LoggerFactory.getInstance(BrandUniquenessSorter.class);

	public BrandUniquenessSorter(RecommendationService internal) {
		super(internal);
	}

	public List<ContentNodeModel> recommendNodes(SessionInput input) {
		List<ContentNodeModel> nodes = internal.recommendNodes(input);
		//LOGGER.debug("Items before brand uniqueness sorting: " + nodes);
		List<ContentNodeModel> newNodes = new ArrayList<ContentNodeModel>(nodes.size());
		Set<BrandModel> brands = new HashSet<BrandModel>();
		
		int prioritizedCount = input.getPrioritizedCount();
		while ( nodes.size() > 0 ) {
			ListIterator<ContentNodeModel> it = nodes.listIterator();
			int i = 0;
			while ( it.hasNext() ) {
				ProductModel p = (ProductModel)it.next();
				if ( i++ < prioritizedCount || !containsAny( brands, p.getBrands() ) ) {
					newNodes.add( p );
					it.remove();
					brands.addAll( p.getBrands() );
				}
			}
			brands.clear();
		}

		//LOGGER.debug("Items after brand uniqueness sorting: " + newNodes);
		return newNodes;
	}

	private boolean containsAny(Set<? extends Object> set, List<? extends Object> elements) {
		for (int i = 0; i < elements.size(); i++)
			if (set.contains(elements.get(i)))
				return true;

		return false;
	}
}

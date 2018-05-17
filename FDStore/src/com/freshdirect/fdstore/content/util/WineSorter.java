package com.freshdirect.fdstore.content.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.content.ComparatorChain;
import com.freshdirect.fdstore.content.EnumWineRating;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.ProductRatingGroup;
import com.freshdirect.fdstore.content.WineFilter;
import com.freshdirect.smartstore.sorting.ScriptedContentNodeComparator;

public class WineSorter implements Serializable {
	private static final long serialVersionUID = 2075923425552479422L;

	private static enum Criterium {
		BY_RATING, ALPHABETICAL, BY_POPULARITY, BY_PRICE;
	}

	public static enum Type {
		EXPERT_RATING(Criterium.BY_RATING, false),
		ABC(Criterium.ALPHABETICAL, false),
		POPULARITY(Criterium.BY_POPULARITY, false),
		PRICE(Criterium.BY_PRICE, false),
		PRICE_REVERSE(Criterium.BY_PRICE, true);

		private boolean reverse;
		private Criterium criterium;

		Type(Criterium criterium, boolean reverse) {
			this.criterium = criterium;
			this.reverse = reverse;
		}

		public boolean isReverse() {
			return reverse;
		}
		
		protected Criterium getCriteria() {
			return criterium;
		}
	}
	
	private PricingContext pricingContext;
	private Type type;
	private boolean forceNoGrouping;
	private List<ProductRatingGroup> results;
	WineFilter filter;
	Collection<ProductModel> products;
	
	public WineSorter(PricingContext pricingContext, Type type, EnumWineViewType view, WineFilter filter) {
		super();
		this.pricingContext = pricingContext;
		this.type = type;
		this.forceNoGrouping = view == EnumWineViewType.DETAILS;
		this.filter = filter;
		this.products = null;
	}

	public WineSorter(PricingContext pricingContext, Type type, EnumWineViewType view, Collection<ProductModel> products) {
		super();
		this.pricingContext = pricingContext;
		this.type = type;
		this.forceNoGrouping = view == EnumWineViewType.DETAILS;
		this.filter = null;
		this.products = products;
	}
	
	/**
	 * Special case for experts top rated feed
	 * @param pricingContext
	 * @param filter
	 */
	public WineSorter(PricingContext pricingContext, WineFilter filter) {
		super();
		this.pricingContext = pricingContext;
		this.type = null;
		this.forceNoGrouping = true;
		this.filter = filter;
		this.products = null;
	}
	
	public Type getType() {
		return type;
	}
	
	public boolean isForceNoGrouping() {
		return forceNoGrouping;
	}

	public List<ProductRatingGroup> getResults() {
		if (results != null)
			return results;

		Collection<ProductModel> products = filter != null ? filter.getProducts() : this.products;
		List<ProductRatingGroup> groups = new ArrayList<ProductRatingGroup>(6);
		boolean reverse = false;
		boolean grouping;
		boolean forceNoGrouping = products.size() <= 6 ? true : this.forceNoGrouping;
		ComparatorChain<ProductModel> comparator;
		if (type != null) {
			switch (type) {
				case PRICE_REVERSE:
					reverse = true;
					break;
				default:
					reverse = false;
			}
			switch (type.criterium) {
				case BY_RATING:
					grouping = !forceNoGrouping;
					comparator = ComparatorChain.create(ProductModel.GENERIC_RATING_COMPARATOR);
					comparator.chain(ProductModel.GENERIC_PRICE_COMPARATOR);
					comparator.chain(ProductModel.FULL_NAME_PRODUCT_COMPARATOR);
					break;
				case ALPHABETICAL:
					comparator = ComparatorChain.create(ProductModel.FULL_NAME_PRODUCT_COMPARATOR);
					grouping = false;
					break;
				case BY_PRICE:
					comparator = ComparatorChain.create(ProductModel.GENERIC_PRICE_COMPARATOR);
					comparator.chain(ProductModel.FULL_NAME_PRODUCT_COMPARATOR);
					grouping = false;
					break;
				case BY_POPULARITY:
					comparator = ComparatorChain.create(ScriptedContentNodeComparator.createGlobalComparator(null, pricingContext));
					comparator.chain(ProductModel.FULL_NAME_PRODUCT_COMPARATOR);
					grouping = false;
					break;
				default:
					throw new IllegalStateException("Unknown sorting criterium");
			}
		} else {
			comparator = ComparatorChain.create(ProductModel.GENERIC_RATING_COMPARATOR);
			comparator = comparator.chain(ScriptedContentNodeComparator.createGlobalComparator(null, pricingContext));
			comparator.chain(ProductModel.FULL_NAME_PRODUCT_COMPARATOR);
			grouping = false;
		}
		if (grouping) {
			Map<EnumWineRating,ProductRatingGroup> map = new HashMap<EnumWineRating, ProductRatingGroup>(6);
			for (ProductModel p : products) {
				try {
					EnumWineRating rating = EnumWineRating.getEnumByRating(p.getProductRatingEnum());
					if (!map.containsKey(rating))
						map.put(rating, new ProductRatingGroup(rating, products.size()));
					
					map.get(rating).getProducts().add(p);
				} catch (FDResourceException e) {
					throw new FDRuntimeException(e);
				}
			}
			
			
			groups.addAll(map.values());
			Collections.sort(groups);
			Collections.reverse(groups);
		} else {
			ProductRatingGroup group = new ProductRatingGroup(EnumWineRating.NOT_RATED, products.size());
			group.getProducts().addAll(products);
			groups.add(group);
		}
		
		for (ProductRatingGroup group : groups) {
			if (reverse)
				comparator = ComparatorChain.reverseOrder(comparator);
			Collections.sort(group.getProducts(), comparator);
		}
		
		return results = groups;
	}
}

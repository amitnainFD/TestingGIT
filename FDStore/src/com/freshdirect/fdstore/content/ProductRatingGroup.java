package com.freshdirect.fdstore.content;

import java.util.ArrayList;
import java.util.List;

import com.freshdirect.fdstore.content.util.EnumWinePageSize;

public class ProductRatingGroup implements Comparable<ProductRatingGroup> {
	private EnumWineRating rating;

	private final List<ProductModel> products;

	public ProductRatingGroup(EnumWineRating rating, int capacity) {
		super();
		this.rating = rating;
		this.products = new ArrayList<ProductModel>(capacity);
	}
	
	@Override
	public int compareTo(ProductRatingGroup o) {
		return rating.compareTo(o.rating);
	}

	public EnumWineRating getRating() {
		return rating;
	}

	public List<ProductModel> getProducts() {
		return products;
	}
	
	public List<PriceCalculator> getPriceCalculators() {
	    List<PriceCalculator> calcs = new ArrayList<PriceCalculator>(products.size());
	    for (ProductModel p : products) {
	        calcs.add(p.getPriceCalculator());
	    }
	    return calcs;
	}
	
	public static int productCount(List<ProductRatingGroup> groups) {
		int i = 0;
		for (ProductRatingGroup group : groups)
			i += group.products.size();
		
		return i;
	}
	
	public static int pageCount(List<ProductRatingGroup> groups, EnumWinePageSize size) {
		int productCount = productCount(groups);
		int i = productCount / size.getSize();
		if (productCount % size.getSize() > 0)
			i++;
		return i;
	}
}

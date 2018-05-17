package com.freshdirect.fdstore.content.sort;

import java.util.Comparator;

import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.SkuModel;
import com.freshdirect.smartstore.sorting.ScriptedContentNodeComparator;

/**
 * Compares content nodes by their short-term-popularity value.
 * 
 * Sorts by most popular first.
 *
 * It is actually only a wrapper around the smartstore popularity comparator,
 * providing a simpler interface without the bizarre parameters like pricingcontext, etc..
 * 
 * Ensures that only ProductModels are passed, as there are no scores like popularity for anything else.
 * 
 * @author treer
 *
 */
public class PopularityComparator implements Comparator<ContentNodeModel> {

	// Standard Popularity factor
	//	private final com.freshdirect.smartstore.sorting.PopularityComparator comp = 
	//			new com.freshdirect.smartstore.sorting.PopularityComparator( false, false, null, null );

	// Short-term Popularity factor	
	Comparator<ProductModel> comp = ScriptedContentNodeComparator.createShortTermPopularityComparator(null, null);

	@Override
	public int compare( ContentNodeModel o1, ContentNodeModel o2 ) {
		ProductModel prod1 = o1 instanceof ProductModel ? (ProductModel)o1 : (o1 instanceof SkuModel ? ((SkuModel)o1).getProductModel() : null); 
		ProductModel prod2 = o2 instanceof ProductModel ? (ProductModel)o2 : (o2 instanceof SkuModel ? ((SkuModel)o2).getProductModel() : null);
		
		if ( prod1 == null && prod2 == null ) 
			return 0;
		else if ( prod1 == null )
			return 1;
		else if ( prod2 == null )
			return -1;
		
		return comp.compare( prod1, prod2 );
	}

	@Override
	public String toString() {
		return "Popularity";
	}
}

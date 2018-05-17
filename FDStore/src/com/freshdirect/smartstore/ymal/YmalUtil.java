/**
 * 
 */
package com.freshdirect.smartstore.ymal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletRequest;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.Recipe;
import com.freshdirect.fdstore.content.YmalSource;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartLineModel;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.framework.util.log.LoggerFactory;


/**
 * Utility to pick the best fitting product from different types of sources (order lines, product list).
 * The selected product will be the trigger for YMAL offering.
 * 
 * @author csongor
 *
 */
public class YmalUtil {
	private static Category LOGGER = LoggerFactory.getInstance(YmalUtil.class);

	protected static class PriceComparator implements Comparator<FDCartLineI> {
		public int compare(FDCartLineI c1, FDCartLineI c2) {
			return ProductModel.PRODUCT_MODEL_PRICE_COMPARATOR_INVERSE
					.compare(c1.lookupProduct(), c2.lookupProduct());
		}
	}




	/**
	 * Selects the 'best' fitting product from customer's recent order lines.
	 * This is currently the most expensive.
	 * 
	 * @param user Customer
	 * @return The most expensive product as YmalSource
	 */
	public static YmalSource resolveYmalSource(FDUserI user, ServletRequest request) {
		YmalSource source = null;
		if (user != null && user.getShoppingCart() != null
				&& user.getShoppingCart().getRecentOrderLines() != null) {
			
			// select the appropriate item
			FDCartLineModel selected = getSelectedCartLine(user);
			
			if (selected != null) {
				
				// item came from recipe
				if (selected.getRecipeSourceId() != null) {
					Recipe recipe = (Recipe) ContentFactory.getInstance()
							.getContentNode(selected.getRecipeSourceId());
					if (recipe != null) {
				        if (isYmalSourceEmpty(recipe)) {
							LOGGER.info("recipYmalSet newSete (" + recipe.getContentKey().getId()
									+ ") not eligible, fallback to product");
							source = selected.lookupProduct();
						} else {
							source = recipe;
						}
					} else {
						source = selected.lookupProduct();
					}
				} else {
					source = selected.lookupProduct();
				}
			}
		}

		resetActiveYmalSetSession(source, request);
		
		return source;
	}

	/**
	 * !!! HACK !!!
	 * 
	 * This is a very nasty hack to keep tracking if the active ymal set has
	 * been reset during the current request
	 * 
	 * @see YmalSource#resetActiveYmalSetSession()
	 * 
	 * @param source
	 * @param request
	 */
	public static void resetActiveYmalSetSession(YmalSource source, ServletRequest request) {
		if (request.getAttribute("freshdirect.ymalSource.reset") == null) {
			if (source != null)
				source.resetActiveYmalSetSession();
			request.setAttribute("freshdirect.ymalSource.reset", Boolean.TRUE);
		}
	}

	/**
	 * Helper method to pick product from recently added order lines
	 *   that will be the originating product for YMAL offerings.
	 *   
	 * Currently it selects the most expensive product.
	 * 
	 * @param user Customer
	 * 
	 * @return Selected cart line item
	 */
	public static FDCartLineModel getSelectedCartLine(FDUserI user) {
		List<FDCartLineI> orderLines = new ArrayList<FDCartLineI>(user.getShoppingCart().getRecentOrderLines());

		FDCartLineModel selected = null;
		if (orderLines.size() == 1)
			selected = (FDCartLineModel) orderLines.get(0);
		else if (orderLines.size() > 1) {
			Collections.sort(orderLines, new PriceComparator());
			selected = (FDCartLineModel) orderLines.get(0);
		}
		return selected;
	}
	

	public static boolean isYmalSourceEmpty(YmalSource source) {
		return source.getActiveYmalSet() == null &&
			source.getYmalProducts().isEmpty() &&
			source.getYmalRecipes().isEmpty() &&
			source.getYmalCategories().isEmpty();
	}
}

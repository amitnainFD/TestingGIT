package com.freshdirect.smartstore.fdstore;

import java.util.List;

/**
 * Provide the entire score range for all available products.
 * 
 * Available means, available in context (see the method docs).
 * 
 * This class is used as an interface (and cache) for database
 * source factors.
 * 
 * @author istvan
 *
 */
public interface ScoreRangeProvider {

	/**
	 * Get the values for the factor.
	 * 
	 * @param userId customerId, maybe null for non-personalized factors
	 * @param factor name of factor
	 * @return values of factor for each product in the order of {@link #products(String) products(factor)}
	 */
	public double [] getRange(String userId, String factor);
	
	/**
	 * Get all products for which this scores is available.
	 *
	 * It is guaranteed that {@link #getRange(String, String) getRange(userId,factor)}
	 * returns the scores in this exact order. 
	 * 
	 * @param userId customer id, may be null for non-personalized factors
	 * @return
	 */
	public List<String> products(String userId);
	
	/**
	 * If there is some caching involved, purge that cache.
	 */
	public void purge();
	
	/**
	 * Synchronously cache values now.
	 * @param userId customerId, maybe null for non-personalized factors
	 */
	public void cache(String userId);
	
}

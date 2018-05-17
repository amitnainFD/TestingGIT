package com.freshdirect.fdstore.content;

import java.util.Map;
import java.util.TreeMap;

/**
 * Recommended alternative with parameters.
 * 
 * The parameters include all what is needed to render the product, including sku code, category, configuration etc.
 *
 */
public class RecommendedAlternative {
	private ProductModel alternative;
	private Map queryParameters = new TreeMap();
	
	/**
	 * Constructor.
	 * @param alternative
	 */
	public RecommendedAlternative(ProductModel alternative) {
		this.alternative = alternative;
	}
	
	/**
	 * Get the actual product instance.
	 * Note that <tt>ConfiguredProductGroup</tt>s are unrolled.
	 * @return alternative
	 */
	public ProductModel getProduct() { return alternative; }
	
	/** 
	 * Query parameters as String - Object pairs.
	 * @return query params
	 */
	public Map getQueryParameters() { return queryParameters; }
	
	/**
	 * Is alternative unavailable.
	 * @return whether alternative is unavailable
	 */
	public boolean isUnavailable() { return alternative.isUnavailable(); }

	/**
	 * Add a query paramter
	 * @param param string key`
	 * @param value param value
	 * @return this
	 */
	public RecommendedAlternative addQueryParameter(String param, Object value) {
		if (value != null) queryParameters.put(param, value);
		return this;
	}
}

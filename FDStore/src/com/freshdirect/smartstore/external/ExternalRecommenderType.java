package com.freshdirect.smartstore.external;

/**
 * The type of the external recommender.
 * 
 * @author csongor
 */
public enum ExternalRecommenderType {
	/**
	 * For those which can be aligned with <b>PersonalizedItems_<i>Xyz</i></b>
	 * generator functions and <b>Personalized_<i>Xyz</i></b> factors.
	 */
	PERSONALIZED,
	/**
	 * For those which can be aligned with <b>RelatedItems_<i>Xyz</i></b>
	 * generator functions.
	 */
	RELATED
}

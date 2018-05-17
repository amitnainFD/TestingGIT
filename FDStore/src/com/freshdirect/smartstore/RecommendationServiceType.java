package com.freshdirect.smartstore;

import java.util.List;

import org.apache.commons.lang.enums.Enum;

/**
 * Type of recommendation service.
 * @author istvan
 *
 */
public class RecommendationServiceType extends Enum {

	// generated id.
	private static final long serialVersionUID = -14506744566071264L;

	/** DYF: frequently bought. */
	@Deprecated
	public final static RecommendationServiceType FREQUENTLY_BOUGHT_DYF = 
			new RecommendationServiceType("freqbought_dyf");
	
	/** DYF: random selection. */	
	@Deprecated
	public final static RecommendationServiceType RANDOM_DYF =
			new RecommendationServiceType("random_dyf");
	
	/** _ANY_: no recommendation. */
	public final static RecommendationServiceType NIL =
			new RecommendationServiceType("nil");
	
	public final static RecommendationServiceType FEATURED_ITEMS = 
			new RecommendationServiceType("featured_items");

    public final static RecommendationServiceType ALL_PRODUCT_IN_CATEGORY = 
        	new RecommendationServiceType("all_prod_in_cat");

    public final static RecommendationServiceType CANDIDATE_LIST = 
        	new RecommendationServiceType("candidate_list");
    
    public final static RecommendationServiceType MANUAL_OVERRIDE = 
        	new RecommendationServiceType("manual_override");
    
    public final static RecommendationServiceType YOUR_FAVORITES_IN_FEATURED_ITEMS = 
    		new RecommendationServiceType("your_fav_in_fi");
        
    public final static RecommendationServiceType FAVORITES = 
        new RecommendationServiceType("favorites");

    public final static RecommendationServiceType SCRIPTED = 
            new RecommendationServiceType("scripted");
    
    public final static RecommendationServiceType CLASSIC_YMAL =
    		new RecommendationServiceType("classicYMAL");

    public final static RecommendationServiceType SMART_YMAL =
    		new RecommendationServiceType("smartYMAL");
    
    public final static RecommendationServiceType YMAL_YF =
    		new RecommendationServiceType("ymal-yf");
    
    public final static RecommendationServiceType TAB_STRATEGY =
		new RecommendationServiceType("tab-strategy");

	/**
	 * Constructor.
	 * Not public, since it is a constant.
	 * @param name
	 */
	protected RecommendationServiceType(String name) {
		super(name);
	}
	
	/**
	 * Helper to make enum by name.
	 * @param name
	 * @return existing enum or null
	 */
	public static RecommendationServiceType getEnum(String name) {
		return (RecommendationServiceType)getEnum(RecommendationServiceType.class, name);
	}
	
	/**
	 * Helper to make a list of enums.
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	public static List<RecommendationServiceType> all() {
	    return getEnumList(RecommendationServiceType.class);
	}

	
}

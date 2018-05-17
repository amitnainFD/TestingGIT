package com.freshdirect.smartstore;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.freshdirect.fdstore.content.EnumBurstType;
import com.freshdirect.fdstore.util.EnumSiteFeature;

/**
 * SmartStore algorithm variant.
 * 
 * An instance represents a configured algorithm. 
 * 
 * @author istvan
 *
 */
public class Variant implements Comparable<Variant>, Serializable {
	private static final long serialVersionUID = -1534257161948385156L;

	public static final String NIL_ID = "nilv";
	
	// site feature
	private EnumSiteFeature siteFeature;
	
	// service type
	private RecommendationServiceConfig serviceConfig;
	
	// id of variant
	private String id;
	
	private SortedMap<Integer, SortedMap<Integer, CartTabStrategyPriority>> tabStrategyPriorities;
	
	private RecommendationService recommender;
	
	private Set<EnumBurstType> hideBursts;

	/*
	 * APPDEV-2320 Determines the look of Cart'N'Tabs variant
	 */
	private boolean defaultTabLook = true;

	/**
	 * Get identifier.
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get site feature.
	 * @return site feature
	 */
	public EnumSiteFeature getSiteFeature() {
		return this.siteFeature;
	}
	
	/**
	 * Get service type.
	 * @return service type
	 */
	public RecommendationServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	/**
	 * Constructor.
	 * @param id
	 * @param siteFeature {@link EnumSiteFeature}
	 */
	public Variant(String id, EnumSiteFeature siteFeature,
			RecommendationServiceConfig serviceConfig) {
		this.id = id;
		this.siteFeature = siteFeature;
		this.serviceConfig = serviceConfig;
		this.tabStrategyPriorities = new TreeMap<Integer, SortedMap<Integer, CartTabStrategyPriority>>();
	}
	
	public Variant(String id, EnumSiteFeature siteFeature,
			RecommendationServiceConfig serviceConfig, SortedMap<Integer, SortedMap<Integer, CartTabStrategyPriority>> tabStrategyPriorities) {
		this.id = id;
		this.siteFeature = siteFeature;
		this.serviceConfig = serviceConfig;
		this.tabStrategyPriorities = tabStrategyPriorities;
	}

	/**
	 * Compare method.
	 * @param other variant
	 * @return result of comparison on id
	 */
	public int compareTo(Variant other) { 
		return getId().compareTo(other.getId());
	}

	/**
	 * Hash code.
	 * @return getId().hashCode()
	 */
	public int hashCode() {
		return getId().hashCode();
	}
	
	/**
	 * Equality.
	 * @param o object
	 * @return whether the parameter is a {@link Variant} and has the same {@link #getId() id}
	 */
	public boolean equals(Object o) {
		if (!(o instanceof Variant)) 
			return false;
		
		return ((Variant)o).getId().equals(getId());
	}

	
    public String toString() {
        return "Variant(" + id + ',' + siteFeature + ',' + serviceConfig + ',' + 
            (serviceConfig != null && serviceConfig.getType()!=null ? serviceConfig.getType().getName() : "null") + ')';
    }
	
    public SortedMap<Integer, SortedMap<Integer, CartTabStrategyPriority>> getTabStrategyPriorities() {
		return tabStrategyPriorities;
	}

    /**
     * Smart Savings feature is now deprecated
     * Forced to return false value
     * @return
     */
    @Deprecated
    public boolean isSmartSavings() {
		return false;
	}
    
    
    public boolean isDefaultTabLook() {
		return defaultTabLook;
	}
    

    /**
     * APPDEV-2320 Sets default look of cart'n'tabs.
     * True value means classic (or default) tabbed look, false means flattened look
     * 
     * @param defaultTabLook
     */
    public void setDefaultTabLook(boolean defaultTabLook) {
		this.defaultTabLook = defaultTabLook;
	}


    /**
     * Deprecated function. No longer has effect until removal
     * @param isSmartSavings
     */
    @Deprecated
    public void setSmartSavings(boolean isSmartSavings) {
	}

	public void setRecommender(RecommendationService recommender) {
		this.recommender = recommender;
	}

	public RecommendationService getRecommender() {
		return recommender;
	}

	public Set<EnumBurstType> getHideBursts() {
		return hideBursts;
	}

	/**
	 * Specify bursts to hide on front-end
	 * @param hideBursts
	 */
	public void setHideBursts(Set<EnumBurstType> hideBursts) {
		this.hideBursts = hideBursts;
	}
}

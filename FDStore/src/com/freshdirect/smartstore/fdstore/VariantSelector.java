package com.freshdirect.smartstore.fdstore;

import java.util.HashMap;
import java.util.Map;

import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.smartstore.RecommendationService;
import com.freshdirect.smartstore.Variant;

/**
 * Variant selection based on cohort name or user id.
 * 
 * Variants (aka particular {@link RecommendationService recommendation services}) are assigned based on
 * user ids (cohorts). The two public method is {@link #select(String)}, or the {@link #getService(String)}
 * 
 * 
 * For each site feature these cohorts can be assigned differently to the n variants.
 * This class first identifies which cohort the user belongs to and then returns the corresponding
 * variant.
 * 
 * 
 * @author istvan
 * @author zsombor
 */
public class VariantSelector {
    private Map<String, Variant> services = new HashMap<String, Variant>();
    private boolean needUpdate;

    private EnumSiteFeature siteFeature;

    public VariantSelector(EnumSiteFeature siteFeature) {
		super();
		if (siteFeature == null)
			throw new IllegalArgumentException("site feature must not be null");
		needUpdate = false;
		this.siteFeature = siteFeature;
	}
    
    /**
	 * helper method to mark the selector to reload
	 * 
	 * @param needUpdate
	 */
    protected void setNeedUpdate(boolean needUpdate) {
		this.needUpdate = needUpdate;
	}
    
    protected boolean isNeedUpdate() {
		return needUpdate;
	}

	/**
	 * !!! This method should become protected !!!
	 * 
	 * @param cohortName
	 * @param variant
	 */
	public void addCohort(String cohortName, Variant variant) {
        this.services.put(cohortName, variant);
    }
    
    public Variant getVariant(String cohortName) {
        return services.get(cohortName);
    }
	
    public Variant select(FDUserI user) {
    	return select(user, false);
    }

    public Variant select(FDUserI user, boolean ignoreOverriddenVariants) {
    	Variant variant;
    	
		if (!ignoreOverriddenVariants) {
			variant = selectOverridden(user);
			if (variant != null)
				return variant;
		}
		
        String cohortName = CohortSelector.getInstance().getCohortName(user.getPrimaryKey());
        return services.get(cohortName);
    }

	public Variant selectOverridden(FDUserI user) {
		return OverriddenVariantsHelper.getOverriddenVariant(user, siteFeature);
	}
}

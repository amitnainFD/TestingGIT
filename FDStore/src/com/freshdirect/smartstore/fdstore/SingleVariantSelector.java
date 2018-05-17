package com.freshdirect.smartstore.fdstore;

import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.smartstore.Variant;

/**
 * This VariantSelector returns the default variant for all users.
 * 
 * @author zsombor
 *
 */
public class SingleVariantSelector extends VariantSelector {

    Variant variant;
    
    public SingleVariantSelector(Variant variant) {
    	super(variant.getSiteFeature());
        this.variant = variant;
    }

    protected void init() {

    }

    @Override
    public Variant select(FDUserI user) {
        return variant;
    }
    
    @Override
    public Variant select(FDUserI user, boolean ignoreOverriddenVariants) {
        return variant;
    }
    
    @Override
    public Variant selectOverridden(FDUserI user) {
    	return null;
    }

    @Override
    public Variant getVariant(String cohortName) {
        return variant;
    }
}

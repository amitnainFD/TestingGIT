package com.freshdirect.smartstore.fdstore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.service.VariantRegistry;

/**
 * Factory for variant selectors.
 * 
 * The factory maps site features to {@link VariantSelector variant selectors}
 * and implements the singleton pattern.
 * 
 * @author istvan
 * 
 */
public class VariantSelectorFactory {
	
	private final static Logger LOGGER = LoggerFactory.getInstance(VariantSelectorFactory.class);

    private static Map<EnumSiteFeature, VariantSelector> selectors = new HashMap<EnumSiteFeature, VariantSelector>();

    /**
	 * Get the appropriate variant selector.
	 * 
	 * If the site feature has not yet been exercised, its parameters will be
	 * loaded and the instance created.
	 * 
	 * @param siteFeature
	 * @return variant selector corresponding to site feature.
	 */
    public synchronized static VariantSelector getSelector(final EnumSiteFeature siteFeature) {

        VariantSelector selector = selectors.get(siteFeature);
        if (selector == null || selector.isNeedUpdate()) {


        	if (EnumSiteFeature.NIL.equals(siteFeature)) {
        		VariantSelector sel = selectors.get(EnumSiteFeature.NIL);
        		if (sel == null) {
        			final Variant v = VariantRegistry.getInstance().getService( Variant.NIL_ID );
        			sel = new SingleVariantSelector( v );
        			selectors.put(EnumSiteFeature.NIL, sel);
        		}
        		return sel;
        	}


            try {
				VariantSelector tmpSelector = new VariantSelector(siteFeature);
				Map<String, Variant> id2vrnt = VariantRegistry.getInstance().getServices(siteFeature);
				Set<Variant> variants = new HashSet<Variant>();

				// (cohort id -> variant id)
				Map<String, String> assignment =
						VariantSelection.getInstance().getVariantMap(siteFeature);
				for (Iterator<String> it = assignment.keySet().iterator(); it.hasNext();) {
				    String cohortId = it.next();
				    Variant variant = id2vrnt.get(assignment.get(cohortId));
					tmpSelector.addCohort(cohortId, variant);
					variants.add(variant);
				}

				selectors.put(siteFeature, tmpSelector);
				LOGGER.info("loaded selector for " + siteFeature.getName()
						+ " with " + variants.size() + " variant for " + assignment.size() + " cohorts");
				
				return tmpSelector;
				
			} catch (RuntimeException e) {
				String siteFeatureName = siteFeature !=null ? siteFeature.getName() : null;
				if (selector == null) {
					throw new FDRuntimeException(e,	"failed to initialize variant mappings for " + siteFeatureName);
				}
				LOGGER.warn("failed to reload variant mappings for " + siteFeatureName + ", returning previous mapping");
			}
        }
		return selector;
    }
    
    /**
	 * !!! ONLY FOR TESTING PURPOSES - DO NOT CALL DIRECTLY !!!
	 * 
	 * @param siteFeature
	 * @param selector
	 */
    public synchronized static void setVariantSelector(
    		final EnumSiteFeature siteFeature, VariantSelector selector) {
        selectors.put(siteFeature, selector);
    }
    
    public synchronized static void refresh() {
    	for (Entry<EnumSiteFeature, VariantSelector> entry : selectors.entrySet()) {
    		entry.getValue().setNeedUpdate(true);
    		LOGGER.info("variant mappings selector for " + entry.getKey().getName() + " is marked for reload");
    	}
    }
}

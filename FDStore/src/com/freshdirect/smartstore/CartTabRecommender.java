package com.freshdirect.smartstore;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.fdstore.FDStoreRecommender;
import com.freshdirect.smartstore.fdstore.Recommendations;
import com.freshdirect.smartstore.fdstore.VariantSelectorFactory;

public class CartTabRecommender {

    private static Logger LOGGER = LoggerFactory.getInstance(CartTabRecommender.class);

    /**
     * 
     * @param user
     * @param input
     * @param maxRecommendations
     * @return up to 3 variant used to produce cart tab recommendations
     */
    public static TabRecommendation recommendTabs(FDUserI user, SessionInput input) {
        List<Variant> recs = new ArrayList<Variant>();
        Variant tabVariant = null;
        boolean smartSavingsFound = false;
        if (user != null) {
            tabVariant = VariantSelectorFactory.getSelector(EnumSiteFeature.CART_N_TABS).select(user);
            if (tabVariant != null) {
                SortedMap<Integer, SortedMap<Integer, CartTabStrategyPriority>> prios = tabVariant.getTabStrategyPriorities();

                  for (Integer p1 : prios.keySet()) { 
                    SortedMap<Integer,CartTabStrategyPriority> map = prios.get(p1);

                    for (Integer p2 : map.keySet()) { 
                        CartTabStrategyPriority strat = map.get(p2);

                        if (strat == null) {
                            continue;
                        }

                        try {
                        	EnumSiteFeature sf = EnumSiteFeature.getEnum(strat.getSiteFeatureId());
                        	if(sf.isSmartSavings() && smartSavingsFound) {
                        	    continue;
                        	}
                            Recommendations rec = FDStoreRecommender.getInstance().getRecommendations(sf, user, input);
                            if (rec.getProducts().size() > 0) {
                                recs.add(rec.getVariant());
                                if(sf.isSmartSavings()) {
                                    smartSavingsFound = true;
                                }
                                break;
                            }
                        } catch (FDResourceException e) {

                        }

                    }

                    if (recs.size() >= 3) {
                        break;
                    }
                }
            } else {
                LOGGER.warn("failed to retrieve cart recommenders, returning no tabs");
            }
        }

        return new TabRecommendation(tabVariant, recs);
    }
}

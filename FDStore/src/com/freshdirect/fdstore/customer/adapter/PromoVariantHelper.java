package com.freshdirect.fdstore.customer.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDPromotionEligibility;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.promotion.PromoVariantCache;
import com.freshdirect.fdstore.promotion.PromoVariantModel;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.smartstore.CartTabRecommender;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.TabRecommendation;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.fdstore.FDStoreRecommender;
import com.freshdirect.smartstore.fdstore.Recommendations;
import com.freshdirect.smartstore.fdstore.VariantSelection;
import com.freshdirect.smartstore.fdstore.VariantSelectorFactory;

public class PromoVariantHelper {

	public static Map getPromoVariantMap(FDUserI user, FDPromotionEligibility eligibilities){
		Set recommendedPromos = eligibilities.getRecommendedPromos();
		eligibilities.setEligiblity(recommendedPromos, false);
        VariantSelection helper = VariantSelection.getInstance();
        List ssFeatures = EnumSiteFeature.getSmartSavingsFeatureList();
        Map promoVariantMap = new ConcurrentHashMap();
        
        for (Iterator it = ssFeatures.iterator(); it.hasNext();) {
            // fetch variant assignment (cohort -> variant map)
            EnumSiteFeature siteFeature = (EnumSiteFeature) it.next();

            final Variant variant = VariantSelectorFactory.getSelector(siteFeature).select(user);
            String variantId = variant != null ? variant.getId() : null;

            if (variantId != null) {
                List promoVariants = PromoVariantCache.getInstance().getAllPromoVariants(variantId);
                if (promoVariants != null) {
                    for (Iterator iter = promoVariants.iterator(); iter.hasNext();) {
                        PromoVariantModel promoVariant = (PromoVariantModel) iter.next();
                        String promoCode = promoVariant.getAssignedPromotion().getPromotionCode();
                        if (recommendedPromos != null && recommendedPromos.contains(promoCode)) {
                            promoVariantMap.put(variantId, promoVariant);
                            eligibilities.setEligibility(promoCode, true);
                            break;
                        }
                    }
                }
            }
        }
        return promoVariantMap;
	}
	
	public static void updateSavingsVariant(FDUserI user, Map savingsLookupTable){
		Map pvMap = user.getPromoVariantMap();
		if(pvMap == null || pvMap.size() == 0) {
			user.setSavingsVariantId("");
			return ;
		}
		String savVariant = null;
		Set keys = pvMap.keySet();
		FDStoreRecommender recommender = FDStoreRecommender.getInstance();
		SessionInput input = new SessionInput(user);
		input.setCheckForEnoughSavingsMode(true);
		
		List availablePromoVariants = new ArrayList();
		int recSize = 0;
		for(Iterator it = keys.iterator(); it.hasNext();){
			String variantId = (String) it.next();
			PromoVariantModel pv = (PromoVariantModel)pvMap.get(variantId);
			if(savingsLookupTable != null && savingsLookupTable.containsKey(variantId)) {
				recSize = ((Integer)savingsLookupTable.get(variantId)).intValue();
			}else {
				try {

					Recommendations rec = recommender.getRecommendations(pv.getSiteFeature(), user, input);
					recSize = rec.getProducts().size();
					savingsLookupTable.put(variantId, new Integer(recSize));
				}catch (FDResourceException e) {
					//Do nothing.
                }
			}
			  if (recSize > 0) {
				  //no recommendations found for this smart saving feature. remove it from the promo variant map.
				  availablePromoVariants.add(pv);
			  }
		}
		input.setCheckForEnoughSavingsMode(false);
		if(availablePromoVariants.size() == 0) {
			//no savings variant available to show.
			user.setSavingsVariantId("");
			return;
		}
		if(availablePromoVariants.size() > 1)
			Collections.sort(availablePromoVariants, PromoVariantCache.FEATURE_PRIORITY_COMPARATOR);
		PromoVariantModel selected = (PromoVariantModel) availablePromoVariants.get(0);
		savVariant = selected.getVariantId();
		user.setSavingsVariantId(savVariant);
	}
	
	public static void updateSavingsVariantFound(FDUserI user,  int maxRecommendations, ServletRequest request) {
		SessionInput input = new SessionInput(user);
		TabRecommendation tabs = null;
		FDStoreRecommender.initYmalSource(input, user, request);
		input.setCurrentNode( input.getYmalSource() );
		input.setMaxRecommendations(maxRecommendations);
		input.setPreviousRecommendations((Map<String, List<ContentKey>>)
				((HttpServletRequest) request).getSession().getAttribute("fd.ss.prevRec"));
		tabs = CartTabRecommender.recommendTabs(user, input);	
		if(tabs == null || tabs.size() == 0) 
		    user.setSavingsVariantFound(false);
		for (int i = 0; i < tabs.size(); i++) {
			String variantId = tabs.get(i).getId();
			if(variantId.equals(user.getSavingsVariantId())){
				user.setSavingsVariantFound(true);
				return;
			}
		}
		user.setSavingsVariantFound(false);
	}
}

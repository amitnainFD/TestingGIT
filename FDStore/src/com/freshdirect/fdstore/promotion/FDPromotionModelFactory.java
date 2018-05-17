package com.freshdirect.fdstore.promotion;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.promotion.management.FDPromotionManager;
import com.freshdirect.fdstore.promotion.management.FDPromotionModel;
import com.freshdirect.framework.util.ExpiringReference;

@Deprecated
public class FDPromotionModelFactory {

	private final static FDPromotionModelFactory INSTANCE = new FDPromotionModelFactory();
	
	private ExpiringReference promotions = new ExpiringReference(10 * 60 * 1000) {

		protected Object load() {
			try {
				List promoList = FDPromotionManager.getPromotions();
				Map promos = new HashMap(promoList.size());
				for (Iterator i = promoList.iterator(); i.hasNext();) {
					FDPromotionModel promo = (FDPromotionModel) i.next();
					promos.put(promo.getPromotionCode(), promo);
				}
				return promos;
			} catch (FDResourceException ex) {
				throw new FDRuntimeException(ex);
			}
		}
		
	};

	private FDPromotionModelFactory() {
	}

	public static FDPromotionModelFactory getInstance() {
		return INSTANCE;
	}

	private Map getPromotionMap() {
		return (Map) this.promotions.get();
	}

	public Collection getPromotions() {
		return this.getPromotionMap().values();
	}

	public Collection getPromotionCodes() {
		return this.getPromotionMap().keySet();
	}
	
	public FDPromotionModel getPromotion(String promotionCode) {
		return (FDPromotionModel) this.getPromotionMap().get(promotionCode);
	}

	public Set searchByString(String search) {
		Set s = new HashSet();
		if(search == null)
			return s;
		
		search = search.trim().toUpperCase();
		
		for (Iterator i = this.getPromotions().iterator(); i.hasNext();) {
			FDPromotionModel promo = (FDPromotionModel) i.next();
			String desc = promo.getDescription();
			if(desc != null) {
				desc = desc.toUpperCase();
			} else {
				desc = "";
			}
			
			String name = promo.getName();
			if(name != null) {
				name = name.toUpperCase();
			} else {
				name = "";
			}

			String code = promo.getPromotionCode();
			if(code != null) {
				code = code.toUpperCase();
			} else {
				code = "";
			}
			
			String redempCode = promo.getRedemptionCode();;
			if(redempCode != null) {
				redempCode = redempCode.toUpperCase();
			} else {
				redempCode = "";
			}

			if( (desc.indexOf(search) >= 0) ||
				(code.indexOf(search) >= 0) ||
				(redempCode.indexOf(search) >= 0) ||
				(name.indexOf(search) >= 0)) {
				s.add(promo);
			}
		}
		return s;
	}
	
	public void forceRefresh() {
		promotions.forceRefresh();
	}
	
}

package com.freshdirect.fdstore.promotion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.promotion.management.FDPromotionNewManager;
import com.freshdirect.fdstore.promotion.management.FDPromotionNewModel;
import com.freshdirect.framework.util.ExpiringReference;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDPromotionNewModelFactory {

	private final static Object lock = new Object();
	
	private static FDPromotionNewModelFactory INSTANCE = null;
	
	private static final Category LOGGER = LoggerFactory.getInstance(FDPromotionNewModelFactory.class);
	private Date maxLastModified;
	
	private Map<String, FDPromotionNewModel> fdPromotionModelMap = new LinkedHashMap<String, FDPromotionNewModel>();

	/*
	 * Map of cached promotions where keys are promo codes
	 */
	private ExpiringReference<List<FDPromotionNewModel>> promotions = new ExpiringReference<List<FDPromotionNewModel>>(10 * 60 * 1000) {

		@Override
		protected List<FDPromotionNewModel> load() {
			try {
				LOGGER.info("REFRESHING FDPROMOTION MAP FOR ANY NEW PROMOTIONS FROM LAST MODIFIED TIME "+maxLastModified);
				List<FDPromotionNewModel> promoList = new ArrayList<FDPromotionNewModel>();
				if(null !=maxLastModified){
					promoList = FDPromotionNewManager.getModifiedOnlyPromotions(maxLastModified);
				}else{
					loadAllPromotions();
				}
//				List<FDPromotionNewModel> promoList = FDPromotionNewManager.getModifiedOnlyPromotions(maxLastModified);
				LOGGER.info("REFRESHED FDPROMOTION MAP FOR ANY NEW PROMOTIONS. FOUND "+promoList.size());
				return promoList;
			} catch (FDResourceException ex) {
				throw new FDRuntimeException(ex);
			}
		}
		
	};

	private FDPromotionNewModelFactory() {
		loadAllPromotions();
	}

	public static FDPromotionNewModelFactory getInstance() {
		synchronized(lock) {
			if (INSTANCE == null) {
				INSTANCE = new FDPromotionNewModelFactory();
			}
		}
		return INSTANCE;
	}
	
	private void loadAllPromotions(){
		try {
			List<FDPromotionNewModel> promoList = FDPromotionNewManager.getPromotions();
			for ( FDPromotionNewModel promo : promoList ) {
				Date promoModifyDate = promo.getModifiedDate();
				if(promoModifyDate == null) {
					//ignore as Promotion setup is incorrect
					continue;
				}
				Date now = new Date();
				if(this.maxLastModified == null || (this.maxLastModified.before(promoModifyDate) && !promoModifyDate.after(now))){
					this.maxLastModified = new Date(promoModifyDate.getTime());
				}
				this.fdPromotionModelMap.put(promo.getPromotionCode(), promo);
			}
		} catch (FDResourceException ex) {
			LOGGER.error("Failed to load promotions", ex);
		}
	}

	protected synchronized Map<String, FDPromotionNewModel> getPromotionMap() {
		List<FDPromotionNewModel> promoList = this.promotions.get();
		if(promoList.size() > 0){
			for ( FDPromotionNewModel promo : promoList ) {
				Date promoModifyDate = promo.getModifiedDate();
				Date now = new Date();
				if(this.maxLastModified == null  || (this.maxLastModified.before(promoModifyDate) && !promoModifyDate.after(now))){
					this.maxLastModified = new Date(promoModifyDate.getTime());
				}
				String promoId = promo.getPromotionCode();
				//If the modified promo is a redemption ignore it.
				this.fdPromotionModelMap.put(promoId, promo);	
			}
			promoList.clear();
		}
		return this.fdPromotionModelMap;
	}
	/*
	private Map<String,FDPromotionNewModel> getPromotionMap() {
		return this.promotions.get();
	}
	*/
	public Collection<FDPromotionNewModel> getPromotions() {
		return this.getPromotionMap().values();
	}

	public Collection<String> getPromotionCodes() {
		return this.getPromotionMap().keySet();
	}
	
	public FDPromotionNewModel getPromotion(String promotionCode) {
		FDPromotionNewModel fp = (FDPromotionNewModel) this.getPromotionMap().get(promotionCode);
		//System.out.println("----------------"+fp.isFuelSurchargeIncluded());
		return (FDPromotionNewModel) this.getPromotionMap().get(promotionCode);
	}

	/*public Set searchByString(String search) {
		Set s = new HashSet();
		if(search == null)
			return s;
		
		search = search.trim().toUpperCase();
		
		for (Iterator i = this.getPromotions().iterator(); i.hasNext();) {
			FDPromotionNewModel promo = (FDPromotionNewModel) i.next();
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
	}*/
	
	public void forceRefresh() {
		promotions.forceRefresh();
	}
	
	public List<String> getPromotionsCreatedUsers(){
		Collection<FDPromotionNewModel> promotions = getPromotions();
		List<String> users = new ArrayList<String>();
		for (Object object : promotions) {
			FDPromotionNewModel model = (FDPromotionNewModel)object;
			if(null !=model.getCreatedBy() && !users.contains(model.getCreatedBy())){
				users.add(model.getCreatedBy());
			}
			
		}
		Collections.sort(users, new StringComparator());
		return users;
	}
	
	public List<String> getPromotionsModifiedUsers(){
		Collection<FDPromotionNewModel> promotions = getPromotions();
		List<String> users = new ArrayList<String>();
		for (Object object : promotions) {
			FDPromotionNewModel model = (FDPromotionNewModel)object;
			if(null !=model.getModifiedBy() && !users.contains(model.getModifiedBy())){
				users.add(model.getModifiedBy());
			}
			
		}
		Collections.sort(users,new StringComparator());
		return users;
	}


	private class StringComparator implements Comparator<String> {
		@Override
		public int compare(String str1, String str2) {
			return str1.toLowerCase().compareTo(str2.toLowerCase());
		}
	}
}

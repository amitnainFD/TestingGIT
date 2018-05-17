package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.freshdirect.fdstore.content.ProductModel;

public class DCPDPromoProductCache implements Serializable{
	/*
	 * This Map contains the list of product keys that are already
	 * evaluated for a specific DCPD promo.
	 * product key -> DCPDPromoInfoMap
	 */
	private Map dcpdProductMap = new HashMap();
	
    public void retainAll(Set productIds){
    	if(this.dcpdProductMap.isEmpty()){
    		return;
    	}
    	Set eligibleProdKeys = this.dcpdProductMap.keySet();
    	//Retain only the product keys thar are in the cart.
    	eligibleProdKeys.retainAll(productIds);
    }
    
	public void setPromoProductInfo(String productId, String promoId, boolean eligible){
		DCPDPromoInfoMap infoMap = (DCPDPromoInfoMap)this.dcpdProductMap.get(productId);
														
		if(infoMap == null){
			infoMap = new DCPDPromoInfoMap();
		}
		infoMap.setEligibility(promoId, eligible);
		this.dcpdProductMap.put(productId, infoMap);
	}
	
	public boolean isEligible(String productId, String promoId) {
		DCPDPromoInfoMap infoMap = (DCPDPromoInfoMap)this.dcpdProductMap.get(productId);
		if(infoMap != null){
			return infoMap.isEligibleFor(promoId);
		}
		return false;
	}
	
	public boolean isEvaluated(String productId, String promoId) {
		DCPDPromoInfoMap infoMap = (DCPDPromoInfoMap)this.dcpdProductMap.get(productId);
		if(infoMap != null){
			return infoMap.isEvaluated(promoId);
		}
		return false;
	}
	
	/* TODO - Reset the DCPD eligiblity map to re-calculate the promo if in case promotion was modified.*/
	public void clear(){
		if(this.dcpdProductMap != null){
			this.dcpdProductMap.clear();
		}
	}
	
	protected class DCPDPromoInfoMap  {
		
		Map<String, Boolean> info = new HashMap<String, Boolean>();
		
		public DCPDPromoInfoMap(){
		}
		
		public void setEligibility(String promoId, boolean eligible){
			info.put(promoId, Boolean.valueOf(eligible));
		}
		
		public boolean isEligibleFor(String promoId){
			Boolean value = info.get(promoId);
			return (value != null ? value.booleanValue() : false); 
		}
		
		public boolean isEvaluated(String promoId){
			Boolean value = info.get(promoId);
			return (value != null ? true : false); 
		}
	}
}

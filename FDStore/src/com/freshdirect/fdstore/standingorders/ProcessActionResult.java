package com.freshdirect.fdstore.standingorders;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;



import com.freshdirect.fdstore.customer.FDCartLineI;


public class ProcessActionResult implements Serializable {
	
	private static final long serialVersionUID = -8086313206892349807L;
	
	private boolean generalIssue = false;
	private Map<FDCartLineI, UnAvailabilityDetails> unavItemsMap = new HashMap<FDCartLineI, UnAvailabilityDetails>();
	private Map<FDCartLineI, String> messages = new HashMap<FDCartLineI, String>();
	

	public void addUnavailableItem(FDCartLineI item, UnavailabilityReason reason, String message, double unavailQty,String altSkuCode) {
		unavItemsMap.put(item, new UnAvailabilityDetails(unavailQty, reason,altSkuCode));
		if (message != null) {
			messages.put(item, message);
		}
	}
	
	public Set<FDCartLineI> getUnavailableItems() {
		return unavItemsMap.keySet();
	}
	
	public UnavailabilityReason getReason(FDCartLineI item) {
		UnAvailabilityDetails unAvailDtl = unavItemsMap.get(item);
		return unAvailDtl != null ? unAvailDtl.getReason() : null;
	}
	
	public String getMessage(FDCartLineI item) {
		return messages.get(item);
	}

	
	public Map<FDCartLineI, UnAvailabilityDetails> getUnavItemsMap() {
		return unavItemsMap;
	}

	/**
	 * Returns true if an issue raised during item list process without
	 * knowing what was broken
	 *  
	 * @return
	 */
	public boolean isGeneralIssue() {
		return generalIssue;
	}
	
	public void markGeneralIssue() {
		this.generalIssue = true;
	}

	public boolean isFail() {
		return generalIssue || unavItemsMap.size() > 0;
	}

	@Override
	public String toString() {
		return "Bad items: " + unavItemsMap.size() + " -> " + isFail();
	}
}

package com.freshdirect.fdstore.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.freshdirect.fdlogistics.model.FDDeliveryTimeslots;
import com.freshdirect.fdstore.customer.FDDeliveryTimeslotModel;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.logistics.delivery.model.DlvZoneModel;

public class DlvTimeslotStats {
	
	private List<String> messages = new ArrayList<String>();
	private List<String> comments = new ArrayList<String>();

	private boolean hasCapacity = true;
	private boolean isKosherSlotAvailable = false;
	private boolean ctActive = false;
	private double maxDiscount = 0.0;
	private int ctSlots = 0;
	private int alcoholSlots = 0;
	private int ecoFriendlySlots = 0;
	private int neighbourhoodSlots = 0;
	private double soldOut = 0.0;
	private double totalSlots = 0.0;
	private Date sameDayCutoff;
	private String sameDayCutoffUTC;
	private Map<String, DlvZoneModel> zonesMap = new HashMap<String, DlvZoneModel>();
	private boolean isAlcoholDelivery = false;
	private int earlyAmSlots=0;
	
	
	public boolean isAlcoholDelivery() {
		return isAlcoholDelivery;
	}
	
	public void setAlcoholDelivery(boolean isAlcoholDelivery) {
		this.isAlcoholDelivery = isAlcoholDelivery;
	}
	
	public List<String> getMessages() {
		return messages;
	}
	
	public List<String> getComments() {
		return comments;
	}
	
	public Map<String, DlvZoneModel> getZonesMap() {
		return zonesMap;
	}
	
	public void setZonesMap(Map<String, DlvZoneModel> zonesMap) {
		this.zonesMap = zonesMap;
	}

	public void setCtActive(boolean ctActive) {
		this.ctActive = ctActive;
	}
	
	public void incrementAlcoholSlots() {
		alcoholSlots++;
	}
	
	public void setMaximumDiscount(double discount) {
		if (discount > maxDiscount)
			maxDiscount = discount;
	}
	
	public void updateKosherSlotAvailable(boolean flag) {
		isKosherSlotAvailable = isKosherSlotAvailable || flag;
	}
	
	public void incrementEarlyAMSlots(){
		earlyAmSlots++;
	}
	
	
	/**
	 * Apply stat results to delivery model
	 * 
	 * @param deliveryModel
	 */
	public void apply(FDDeliveryTimeslotModel deliveryModel) {
		deliveryModel.setAlcoholDelivery(isAlcoholDelivery);
		// deliveryModel.setTimeslotList(timeslotList);
		deliveryModel.setZones(zonesMap);
		deliveryModel.setZoneCtActive(ctActive);
		deliveryModel.setHasCapacity(hasCapacity);
		deliveryModel.setKosherSlotAvailable(isKosherSlotAvailable);
		deliveryModel.setGeoRestrictionmessages(messages);
		deliveryModel.setComments(comments);
		deliveryModel.setMaxDiscount(maxDiscount);
		deliveryModel.setAlcoholRestrictedCount(alcoholSlots);
		deliveryModel.setEcoFriendlyCount(ecoFriendlySlots);
		deliveryModel.setNeighbourhoodCount(neighbourhoodSlots);
		deliveryModel.setPercSlotsSold(totalSlots > 0 ? Math
				.round((soldOut / totalSlots) * 100) : 0.0);
		deliveryModel.setEarlyAMCount(earlyAmSlots);
	}

	/**
	 * Update chefs table stats in user
	 * @param user
	 */
	public void apply(FDUserI user) {
		if (user != null) {
			user.setTotalCTSlots(ctSlots);
			user.setPercSlotsSold(totalSlots > 0 ? Math
					.round((soldOut / totalSlots) * 100) : 0.0);
		}
	}

	public Date getSameDayCutoff() {
		return sameDayCutoff;
	}

	public void setSameDayCutoff(Date sameDayCutoff) {
		this.sameDayCutoff = sameDayCutoff;
	}

	public String getSameDayCutoffUTC() {
		return sameDayCutoffUTC;
	}

	public void setSameDayCutoffUTC(String sameDayCutoffUTC) {
		this.sameDayCutoffUTC = sameDayCutoffUTC;
	}

	public void apply(FDDeliveryTimeslots t) {
		if(t.getZones()!=null && !t.getZones().isEmpty()){
			this.getZonesMap().putAll(t.getZones());
		}
		if(t.getGeoRestrictionmessages()!=null && !t.getGeoRestrictionmessages().isEmpty()){
			this.getMessages().addAll(t.getGeoRestrictionmessages());
		}
		if(t.getComments()!=null && !t.getComments().isEmpty())
			this.getComments().addAll(t.getComments());
		
		this.setSameDayCutoff(t.getSameDayCutoff());
		this.setCtActive(this.isCtActive() || t.isCtActive());
		this.setCtSlots(this.getCtSlots() + t.getCtSlots());
		this.setEcoFriendlySlots(this.getEcoFriendlySlots() + t.getEcoFriendlySlots());
		this.setNeighbourhoodSlots(this.getNeighbourhoodSlots() + t.getNeighbourhoodSlots());
		this.setTotalSlots(this.getTotalSlots() + t.getTotalSlots());
		this.setHasCapacity(this.hasCapacity || t.isHasCapacity());
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	
	public void setComments(List<String> comments) {
		this.comments = comments;
	}
	
	public int getCtSlots() {
		return ctSlots;
	}

	public void setCtSlots(int ctSlots) {
		this.ctSlots = ctSlots;
	}

	public int getEcoFriendlySlots() {
		return ecoFriendlySlots;
	}

	public void setEcoFriendlySlots(int ecoFriendlySlots) {
		this.ecoFriendlySlots = ecoFriendlySlots;
	}

	public int getNeighbourhoodSlots() {
		return neighbourhoodSlots;
	}

	public void setNeighbourhoodSlots(int neighbourhoodSlots) {
		this.neighbourhoodSlots = neighbourhoodSlots;
	}

	public double getTotalSlots() {
		return totalSlots;
	}

	public void setTotalSlots(double totalSlots) {
		this.totalSlots = totalSlots;
	}

	public boolean isHasCapacity() {
		return hasCapacity;
	}

	public void setHasCapacity(boolean hasCapacity) {
		this.hasCapacity = hasCapacity;
	}

	public boolean isCtActive() {
		return ctActive;
	}

}

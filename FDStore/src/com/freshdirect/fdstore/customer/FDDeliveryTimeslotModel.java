package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.freshdirect.delivery.restriction.RestrictionI;
import com.freshdirect.fdlogistics.model.FDDeliveryTimeslots;
import com.freshdirect.fdlogistics.model.FDReservation;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;
import com.freshdirect.fdstore.util.FDTimeslotUtil;
import com.freshdirect.logistics.delivery.model.DlvZoneModel;

public class FDDeliveryTimeslotModel implements Serializable{
	
	private static final long	serialVersionUID	= 4180048313457481300L;
	

	private List<FDTimeslotUtil> timeslotList;
	private Map<String, DlvZoneModel> zones;	
	



	private List<String> messages;
		
	public List<String> getMessages() {
		return messages;
	}
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	private FDCartModel shoppingCart;
	
	boolean zoneCtActive = false;	
	private List<String> geoRestrictionmessages;
	
	public List<String> getGeoRestrictionmessages() {
		return geoRestrictionmessages;
	}
	public void setGeoRestrictionmessages(List<String> geoRestrictionmessages) {
		this.geoRestrictionmessages = geoRestrictionmessages;
	}
	private List<String> comments;
	
	public List<String> getComments() {
		return comments;
	}
	public void setComments(List<String> comments) {
		this.comments = comments;
	}
	private boolean hasPreReserved;
	private String preReserveSlotId;
	private String timeSlotId;
	private FDReservation rsv;
	private boolean hasCapacity;
	
	boolean thxgivingRestriction = false;
	boolean easterRestriction = false;
	boolean easterMealRestriction = false; 
	boolean valentineRestriction = false;
	boolean kosherRestriction = false;
	boolean alcoholRestriction = false;
    boolean thxgiving_meal_Restriction=false;
    boolean isKosherSlotAvailable;
    
    private String zoneId;
    private double zonePromoAmount;
    private FDStandingOrder currentStandingOrder;
	private List<RestrictionI> holidayRestrictions;
	private List<RestrictionI> specialItemDlvRestrctions;
    private double maxDiscount;
    private int ecoFriendlyCount;
    private int alcoholRestrictedCount;
    private boolean isAlcoholDelivery;
    private boolean minOrderReqd;
    private int earlyAMCount;
    
    
	public int getEarlyAMCount() {
		return earlyAMCount;
	}
	public void setEarlyAMCount(int earlyAMCount) {
		this.earlyAMCount = earlyAMCount;
	}
	public boolean isAlcoholDelivery() {
		return isAlcoholDelivery;
	}
	public void setAlcoholDelivery(boolean isAlcoholDelivery) {
		this.isAlcoholDelivery = isAlcoholDelivery;
	}
	private int neighbourhoodCount;
    private double percSlotsSold;


	private boolean showPremiumSlots;
	private String sameDay;
	Date sameDayCutoff;
	String sameDayCutoffUTC;
    
    public double getPercSlotsSold() {
		return percSlotsSold;
	}
	public void setPercSlotsSold(double percSlotsSold) {
		this.percSlotsSold = percSlotsSold;
	}
	public int getNeighbourhoodCount() {
		return neighbourhoodCount;
	}
	public void setNeighbourhoodCount(int neighbourhoodCount) {
		this.neighbourhoodCount = neighbourhoodCount;
	}

	public boolean isKosherSlotAvailable() {
		return isKosherSlotAvailable;
	}
	public void setKosherSlotAvailable(boolean isKosherSlotAvailable) {
		this.isKosherSlotAvailable = isKosherSlotAvailable;
	}
	public boolean hasCapacity() {
		return hasCapacity;
	}
	public void setHasCapacity(boolean hasCapacity) {
		this.hasCapacity = hasCapacity;
	}
    public int getEcoFriendlyCount() {
		return ecoFriendlyCount;
	}
	public void setEcoFriendlyCount(int ecoFriendlyCount) {
		this.ecoFriendlyCount = ecoFriendlyCount;
	}
	public int getAlcoholRestrictedCount() {
		return alcoholRestrictedCount;
	}
	public void setAlcoholRestrictedCount(int alcoholRestrictedCount) {
		this.alcoholRestrictedCount = alcoholRestrictedCount;
	}	

    public double getMaxDiscount() {
		return maxDiscount;
	}
	public void setMaxDiscount(double maxDiscount) {
		this.maxDiscount = maxDiscount;
	}
	public List<RestrictionI> getSpecialItemDlvRestrctions() {
		return specialItemDlvRestrctions;
	}
	public void setSpecialItemDlvRestrctions(
			List<RestrictionI> specialItemDlvRestrctions) {
		this.specialItemDlvRestrctions = specialItemDlvRestrctions;
	}
	public List<RestrictionI> getHolidayRestrictions() {
		return holidayRestrictions;
	}
	public void setHolidayRestrictions(List<RestrictionI> holidayRestrictions) {
		this.holidayRestrictions = holidayRestrictions;
	}
	public FDStandingOrder getCurrentStandingOrder() {
		return currentStandingOrder;
	}
	public void setCurrentStandingOrder(FDStandingOrder currentStandingOrder) {
		this.currentStandingOrder = currentStandingOrder;
	}
	public List<FDTimeslotUtil> getTimeslotList() {
		return timeslotList;
	}
	public void setTimeslotList(List<FDTimeslotUtil> timeslotList) {
		this.timeslotList = timeslotList;
	}
	public Map<String, DlvZoneModel> getZones() {
		return zones;
	}
	public void setZones(Map<String, DlvZoneModel> zones) {
		this.zones = zones;
	}
	public boolean isZoneCtActive() {
		return zoneCtActive;
	}
	public void setZoneCtActive(boolean zoneCtActive) {
		this.zoneCtActive = zoneCtActive;
	}	
	public double getZonePromoAmount() {
		return zonePromoAmount;
	}
	public void setZonePromoAmount(double zonePromoAmount) {
		this.zonePromoAmount = zonePromoAmount;
	}
	public String getZoneId() {
		return zoneId;
	}
	public void setZoneId(String zoneId) {
		this.zoneId = zoneId;
	}
	public FDReservation getRsv() {
		return rsv;
	}
	public void setRsv(FDReservation rsv) {
		this.rsv = rsv;
	}
	public FDCartModel getShoppingCart() {
		return shoppingCart;
	}
	public void setShoppingCart(FDCartModel shoppingCart) {
		this.shoppingCart = shoppingCart;
	}
	public boolean isPreReserved() {
		return hasPreReserved;
	}
	public void setPreReserved(boolean hasPreReserved) {
		this.hasPreReserved = hasPreReserved;
	}
	public String getPreReserveSlotId() {
		return preReserveSlotId;
	}
	public void setPreReserveSlotId(String preReserveSlotId) {
		this.preReserveSlotId = preReserveSlotId;
	}
	public String getTimeSlotId() {
		return timeSlotId;
	}
	public void setTimeSlotId(String timeSlotId) {
		this.timeSlotId = timeSlotId;
	}
	
	public boolean isThxgivingRestriction() {
		return thxgivingRestriction;
	}
	public void setThxgivingRestriction(boolean thxgivingRestriction) {
		this.thxgivingRestriction = thxgivingRestriction;
	}
	public boolean isEasterRestriction() {
		return easterRestriction;
	}
	public void setEasterRestriction(boolean easterRestriction) {
		this.easterRestriction = easterRestriction;
	}
	public boolean isEasterMealRestriction() {
		return easterMealRestriction;
	}
	public void setEasterMealRestriction(boolean easterMealRestriction) {
		this.easterMealRestriction = easterMealRestriction;
	}
	public boolean isValentineRestriction() {
		return valentineRestriction;
	}
	public void setValentineRestriction(boolean valentineRestriction) {
		this.valentineRestriction = valentineRestriction;
	}
	public boolean isKosherRestriction() {
		return kosherRestriction;
	}
	public void setKosherRestriction(boolean kosherRestriction) {
		this.kosherRestriction = kosherRestriction;
	}
	public boolean isThxgiving_meal_Restriction() {
		return thxgiving_meal_Restriction;
	}
	public void setThxgiving_meal_Restriction(boolean thxgiving_meal_Restriction) {
		this.thxgiving_meal_Restriction = thxgiving_meal_Restriction;
	}
	
	public boolean isAlcoholRestriction() {
		return alcoholRestriction;
	}
	public void setAlcoholRestriction(boolean alcoholRestriction) {
		this.alcoholRestriction = alcoholRestriction;
	}
	public void setShowPremiumSlots(boolean showPremiumSlots) {
		this.showPremiumSlots = showPremiumSlots;
	}
	public boolean isShowPremiumSlots() {
		return this.showPremiumSlots;
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
	public boolean isMinOrderReqd() {
		return minOrderReqd;
	}
	public void setMinOrderReqd(boolean minOrderReqd) {
		this.minOrderReqd = minOrderReqd;
	}
	
	public void apply(FDDeliveryTimeslots t) {
		if(StringUtils.isNotBlank(t.getPreselectedTimeslotId())){
			this.setPreReserveSlotId(t.getPreselectedTimeslotId());
		}
	}
}
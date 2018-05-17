package com.freshdirect.fdstore.promotion.management;

import java.util.ArrayList;
import java.util.List;

import com.freshdirect.framework.core.ModelSupport;

public class FDPromoDlvZoneStrategyModel extends ModelSupport {
	
	private String promotionId;
	private String dlvDays;
	private String[] dlvZones;
	private List<FDPromoDlvTimeSlotModel> dlvTimeSlots;
	private List<FDPromoDlvDayModel> dlvDayRedemtions;
	
	public String getPromotionId() {
		return promotionId;
	}
	public void setPromotionId(String promotionId) {
		this.promotionId = promotionId;
	}
	public String getDlvDays() {
		return dlvDays;
	}
	public void setDlvDays(String dlvDays) {
		this.dlvDays = dlvDays;
	}
	public String[] getDlvZones() {
		return dlvZones;
	}
	public void setDlvZones(String[] dlvZones) {
		this.dlvZones = dlvZones;
	}
	public List<FDPromoDlvTimeSlotModel> getDlvTimeSlots() {
		return dlvTimeSlots;
	}
	public void setDlvTimeSlots(List<FDPromoDlvTimeSlotModel> dlvTimeSlots) {
		this.dlvTimeSlots = dlvTimeSlots;
	}
	public List<FDPromoDlvDayModel> getDlvDayRedemtions() {
		return dlvDayRedemtions;
	}
	public void setDlvDayRedemtions(List<FDPromoDlvDayModel> dlvDayRedemtions) {
		this.dlvDayRedemtions = dlvDayRedemtions;
	}
	public List getDays(){
		List daysList = new ArrayList();
		if(null !=getDlvDays()){
			try {
				Integer daysInt = Integer.parseInt(getDlvDays());
				while(daysInt>0){
					int rem = daysInt%10;
					daysInt = daysInt/10;
					daysList.add(rem);
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return daysList;
	}
	
	

}

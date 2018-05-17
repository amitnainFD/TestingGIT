package com.freshdirect.fdstore.promotion.management;

import com.freshdirect.framework.core.ModelSupport;

public class FDPromoDlvDayModel extends ModelSupport {

	private String promoDlvZoneId;
	private Integer dayId;
	private Integer redeemCount;

	public String getPromoDlvZoneId() {
		return promoDlvZoneId;
	}

	public void setPromoDlvZoneId(String promoDlvZoneId) {
		this.promoDlvZoneId = promoDlvZoneId;
	}

	public Integer getDayId() {
		return dayId;
	}

	public void setDayId(Integer dayId) {
		this.dayId = dayId;
	}

	public Integer getRedeemCount() {
		return redeemCount;
	}

	public void setRedeemCount(Integer redeemCount) {
		this.redeemCount = redeemCount;
	}

	@Override
	public String toString() {
		if(null != dayId && null != redeemCount ){
			return "Day Id:"+ dayId + " End Time: "+redeemCount;
		}
		return super.toString();
		
	}
}

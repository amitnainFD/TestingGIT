package com.freshdirect.fdstore.promotion;

import java.util.Date;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

public class PromotionDlvDay extends ModelSupport {

	private Integer dayId;
	private Integer redeemCnt;
	
	public PromotionDlvDay(Integer dayId, Integer redeemCnt) {
		super();
		this.dayId = dayId;
		this.redeemCnt = redeemCnt;
	}
	
	public Integer getDayId() {
		return dayId;
	}
	public void setDayId(Integer dayId) {
		this.dayId = dayId;
	}

	public Integer getRedeemCnt() {
		return redeemCnt;
	}

	public void setRedeemCnt(Integer redeemCnt) {
		this.redeemCnt = redeemCnt;
	}
	
}

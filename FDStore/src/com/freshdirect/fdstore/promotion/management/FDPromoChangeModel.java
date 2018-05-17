package com.freshdirect.fdstore.promotion.management;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.freshdirect.fdstore.promotion.EnumPromoChangeType;
import com.freshdirect.framework.core.ModelSupport;

public class FDPromoChangeModel extends ModelSupport {

	private String promotionId;
	private String userId;
	private Date actionDate;
	private EnumPromoChangeType actionType;
	private List<FDPromoChangeDetailModel> changeDetails;
	
	public FDPromoChangeModel() {
		super();
	}
	
	public FDPromoChangeModel(String promotionId, String userId,
			Date actionDate, EnumPromoChangeType actionType) {
		super();
		this.promotionId = promotionId;
		this.userId = userId;
		this.actionDate = actionDate;
		this.actionType = actionType;
	}
	public String getPromotionId() {
		return promotionId;
	}
	public void setPromotionId(String promotionId) {
		this.promotionId = promotionId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Date getActionDate() {
		return actionDate;
	}
	public void setActionDate(Date actionDate) {
		this.actionDate = actionDate;
	}
	public EnumPromoChangeType getActionType() {
		return actionType;
	}
	public void setActionType(EnumPromoChangeType actionType) {
		this.actionType = actionType;
	}
	public List<FDPromoChangeDetailModel> getChangeDetails() {
		return changeDetails;
	}
	public void setChangeDetails(List<FDPromoChangeDetailModel> changeDetails) {
		this.changeDetails = changeDetails;
	}

	public void addChangeDetail(FDPromoChangeDetailModel changeDetail) {
		if (this.changeDetails == null)
			this.changeDetails = new ArrayList<FDPromoChangeDetailModel>();
		
		this.changeDetails.add(changeDetail);
	}
}

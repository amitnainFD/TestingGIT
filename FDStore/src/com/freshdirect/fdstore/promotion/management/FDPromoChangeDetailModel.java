package com.freshdirect.fdstore.promotion.management;

import com.freshdirect.fdstore.promotion.EnumPromotionSection;
import com.freshdirect.framework.core.ModelSupport;

public class FDPromoChangeDetailModel extends ModelSupport{

	private String promoChangeId;
	private EnumPromotionSection changeSectionId;
	private String changeFieldName;
	private String changeFieldOldValue;
	
	public FDPromoChangeDetailModel() {
		super();
	}
	
	public FDPromoChangeDetailModel(String promoChangeId,
			EnumPromotionSection changeSectionId, String changeFieldName,
			String changeFieldOldValue, String changeFieldNewValue) {
		super();
		this.promoChangeId = promoChangeId;
		this.changeSectionId = changeSectionId;
		this.changeFieldName = changeFieldName;
		this.changeFieldOldValue = changeFieldOldValue;
		this.changeFieldNewValue = changeFieldNewValue;
	}
	public String getPromoChangeId() {
		return promoChangeId;
	}
	public void setPromoChangeId(String promoChangeId) {
		this.promoChangeId = promoChangeId;
	}
	public EnumPromotionSection getChangeSectionId() {
		return changeSectionId;
	}
	public void setChangeSectionId(EnumPromotionSection changeSectionId) {
		this.changeSectionId = changeSectionId;
	}
	public String getChangeFieldName() {
		return changeFieldName;
	}
	public void setChangeFieldName(String changeFieldName) {
		this.changeFieldName = changeFieldName;
	}
	public String getChangeFieldOldValue() {
		return changeFieldOldValue;
	}
	public void setChangeFieldOldValue(String changeFieldOldValue) {
		this.changeFieldOldValue = changeFieldOldValue;
	}
	public String getChangeFieldNewValue() {
		return changeFieldNewValue;
	}
	public void setChangeFieldNewValue(String changeFieldNewValue) {
		this.changeFieldNewValue = changeFieldNewValue;
	}
	private String changeFieldNewValue;
}

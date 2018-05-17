package com.freshdirect.fdstore.promotion.management;

import com.freshdirect.fdstore.promotion.EnumDCPDContentType;
import com.freshdirect.framework.core.ModelSupport;

public class FDPromoContentModel extends ModelSupport {
	
	private String promotionId;
	private EnumDCPDContentType contentType;
	private String contentId;
	private boolean excluded;
	private boolean loopThru;
	private boolean recCategory = false;
	private Integer content_set_num;
	
	public String getPromotionId() {
		return promotionId;
	}
	public void setPromotionId(String promotionId) {
		this.promotionId = promotionId;
	}
	public EnumDCPDContentType getContentType() {
		return contentType;
	}
	public void setContentType(EnumDCPDContentType contentType) {
		this.contentType = contentType;
	}
	public String getContentId() {
		return contentId;
	}
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
	public boolean isExcluded() {
		return excluded;
	}
	public void setExcluded(boolean excluded) {
		this.excluded = excluded;
	}
	public boolean isLoopEnabled() {
		return loopThru;
	}
	public void setLoopEnabled(boolean loopThru) {
		this.loopThru = loopThru;
	}
	public boolean isRecCategory() {
		return recCategory;
	}
	public void setRecCategory(boolean recCategory) {
		this.recCategory = recCategory;
	}
	public Integer getContent_set_num() {
		return content_set_num;
	}
	public void setContent_set_num(Integer content_set_num) {
		this.content_set_num = content_set_num;
	}
	
}

package com.freshdirect.fdstore.promotion.management;

import java.util.Date;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.util.DateUtil;

public class FDPromoDlvDateModel extends ModelSupport{

	private String promoId;
	private Date dlvDateStart;
	private Date dlvDateEnd;
	
	public String getPromoId() {
		return promoId;
	}
	public void setPromoId(String promoId) {
		this.promoId = promoId;
	}
	public Date getDlvDateStart() {
		return dlvDateStart;
	}
	public void setDlvDateStart(Date dlvDateStart) {
		this.dlvDateStart = dlvDateStart;
	}
	public Date getDlvDateEnd() {
		return dlvDateEnd;
	}
	public void setDlvDateEnd(Date dlvDateEnd) {
		this.dlvDateEnd = dlvDateEnd;
	}
	@Override
	public String toString() {
		
		if(null != dlvDateStart && null != dlvDateEnd){
			return "Start Date:"+DateUtil.formatDate(dlvDateStart)+" End Date:"+DateUtil.formatDate(dlvDateStart);
		}
		return super.toString();
	}
	
	
}

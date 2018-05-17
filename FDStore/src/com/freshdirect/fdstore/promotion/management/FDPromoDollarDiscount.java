package com.freshdirect.fdstore.promotion.management;

import java.util.Date;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.util.DateUtil;

public class FDPromoDollarDiscount extends ModelSupport{

	private String promoId;
	private String pddId;
	private Double dollarOff;
	private Double OrderSubtotal;
	
	public String getPromoId() {
		return promoId;
	}
	public void setPromoId(String promoId) {
		this.promoId = promoId;
	}
	public String getPddId() {
		return pddId;
	}
	public void setPddId(String pddId) {
		this.pddId = pddId;
	}
	public Double getDollarOff() {
		return dollarOff;
	}
	public void setDollarOff(Double dollarOff) {
		this.dollarOff = dollarOff;
	}
	public Double getOrderSubtotal() {
		return OrderSubtotal;
	}
	public void setOrderSubtotal(Double orderSubtotal) {
		OrderSubtotal = orderSubtotal;
	}
	
	@Override
	public String toString() {
		return "FDPromoDollarDiscount [OrderSubtotal=" + OrderSubtotal
				+ ", dollarOff=" + dollarOff + ", pddId=" + pddId
				+ ", promoId=" + promoId + "]";
	}	
	
}

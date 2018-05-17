package com.freshdirect.fdstore.standingorders;

import java.io.Serializable;
import java.util.List;

public class FDStandingOrderInfoList implements Serializable {	
	
	private final List<FDStandingOrderInfo> standingOrdersInfo;
	
	public FDStandingOrderInfoList(List<FDStandingOrderInfo> standingOrdersInfo) {		
		this.standingOrdersInfo = standingOrdersInfo;
	}
	public List<FDStandingOrderInfo> getStandingOrdersInfo() {
		return standingOrdersInfo;
	}

}

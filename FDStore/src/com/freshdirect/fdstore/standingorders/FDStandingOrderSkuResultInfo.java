package com.freshdirect.fdstore.standingorders;

import java.io.Serializable;
import java.util.List;

public class FDStandingOrderSkuResultInfo implements Serializable {
	
	String errorMessage;
	List<FDStandingOrderProductSku>  productSkuList;
	
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public List<FDStandingOrderProductSku> getProductSkuList() {
		return productSkuList;
	}
	public void setProductSkuList(List<FDStandingOrderProductSku> productSkuList) {
		this.productSkuList = productSkuList;
	}

}

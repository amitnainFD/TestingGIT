package com.freshdirect.fdstore.customer;

import com.freshdirect.common.context.UserContext;
import com.freshdirect.customer.ErpDeliveryPlantInfoModel;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;

public class FDUserUtil {
	
	public static boolean isAlcoholRestricted(String zipCode) throws FDResourceException {
		String county = FDDeliveryManager.getInstance().lookupCountyByZip(zipCode);
		 String state = FDDeliveryManager.getInstance().lookupStateByZip(zipCode);
		return  FDDeliveryManager.getInstance().checkForAlcoholDelivery(state, county, zipCode);
	}
	
	public static ErpDeliveryPlantInfoModel getDeliveryPlantInfo(FDUserI user) {
		
		UserContext ctx=user.getUserContext();
		ErpDeliveryPlantInfoModel delPlantInfo=getDeliveryPlantInfo(ctx);
		return delPlantInfo;
	}
	
	public static ErpDeliveryPlantInfoModel getDeliveryPlantInfo(UserContext userContext) {
		ErpDeliveryPlantInfoModel delPlantInfo=new ErpDeliveryPlantInfoModel();
		delPlantInfo.setPlantId(userContext.getFulfillmentContext().getPlantId());
		delPlantInfo.setSalesOrg(userContext.getPricingContext().getZoneInfo().getSalesOrg());
		delPlantInfo.setDistChannel(userContext.getPricingContext().getZoneInfo().getDistributionChanel());
		return delPlantInfo;
	}

	public static ErpDeliveryPlantInfoModel getDefaultDeliveryPlantInfo() {
		ErpDeliveryPlantInfoModel delPlantInfo=new ErpDeliveryPlantInfoModel();
		delPlantInfo.setPlantId("1000");
		delPlantInfo.setSalesOrg("0001");
		delPlantInfo.setDistChannel("01");
		delPlantInfo.setDivision("01");
		return delPlantInfo;
	}
}

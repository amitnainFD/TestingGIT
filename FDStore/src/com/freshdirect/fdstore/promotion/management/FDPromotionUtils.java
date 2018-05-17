package com.freshdirect.fdstore.promotion.management;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.promotion.pxp.PromoPublisher;

public class FDPromotionUtils {
	public static String generateCloneCode(String promotionCode) throws FDResourceException {
		if (!FDPromotionNewManager.lookupPromotion(promotionCode))
			return promotionCode;

		for (int i = 0; i < Math.pow(36, 15); i++) {
			String postFix = Integer.toString(i, 36);
			String candidate = promotionCode.substring(0, Math.min(promotionCode.length(), 16 - postFix.length() - 1));
			candidate += "_" + postFix;
			if (!FDPromotionNewManager.lookupPromotion(candidate))
				return candidate;
		}
		throw new FDResourceException("cannot generate clone code -- left out possibilities");
	}
	
	public static List<WSAdminInfo> getWSAdminInfo(){
		PromoPublisher publisherAgent = new PromoPublisher();
		List<WSAdminInfo> wsadmininfo = (List<WSAdminInfo>)publisherAgent.getWSAdminInfo();// calling ?action=getWSPromosForAutoCancel
		if(wsadmininfo == null || wsadmininfo.size() == 0){
			return Collections.emptyList();
		} 
		return wsadmininfo;

	}
	
	public static double getAmountSpent(int day, List<WSAdminInfo> adminInfos){
		for(Iterator<WSAdminInfo> it = adminInfos.iterator(); it.hasNext();) {
			WSAdminInfo adminInfo = it.next();
			if(adminInfo.getDay() == day)
				return adminInfo.getAmountSpent();
		}
		return 0.0;
	}
}

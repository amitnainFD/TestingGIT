package com.freshdirect.fdstore.lists;

import java.util.List;

import com.freshdirect.fdstore.customer.FDUserI;

public class CclUtils {
	
	public static final String CC_LIST_ID = "ccListId";
	public static final String STARTER_LIST_ID = "starterListId";
    
	public static boolean isCCLInExperienced(FDUserI user, List<FDCustomerListInfo> lists) {
			
		if (user.getLevel() == FDUserI.GUEST) {
			return true;
		}
		
		if (lists == null) {
			return false;
		}
		
		if (lists.size() > 1) {
			return false;
		}
		FDCustomerList l = lists.get(0);
		if (!l.getCreateDate().equals(l.getModificationDate())) {
			return false;
		}
		return true;
	}

	/**
	 * @param user
	 * @return
	 */
	@Deprecated
	public static boolean isCCLEnabled(FDUserI user) {
		return true;
	}
}

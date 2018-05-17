/*
 * Created on Apr 2, 2003
 *
 */
package com.freshdirect.fdstore.customer;

/**
 * @author knadeem
 */
import java.util.Iterator;
import java.util.List;

import com.freshdirect.customer.EnumComplaintStatus;
import com.freshdirect.customer.ErpComplaintModel;
import com.freshdirect.framework.util.MathUtil;

public class FDComplaintUtil {

	public static boolean isComplaintReversable(ErpComplaintModel complaint, List customerCredits){
		if(!EnumComplaintStatus.APPROVED.equals(complaint.getStatus())){
			return false;
		}
		int complaintMethod = complaint.getType().getValue();
		if(ErpComplaintModel.CASH_BACK == complaintMethod || ErpComplaintModel.MIXED == complaintMethod){
			return false;
		}
		boolean reversable = true;
		for(Iterator i = customerCredits.iterator(); i.hasNext(); ){
			FDCustomerCreditModel customerCredit = (FDCustomerCreditModel)i.next();
			if(MathUtil.roundDecimal(customerCredit.getRemainingAmount()) != MathUtil.roundDecimal(customerCredit.getOriginalAmount())){
				reversable = false;
				break;
			}
		}
		return reversable;
	}

}

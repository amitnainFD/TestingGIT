package com.freshdirect.fdstore.standingorders.validation;

import java.util.Collection;

import com.freshdirect.customer.ErpPaymentMethodI;
import com.freshdirect.customer.ErpPaymentMethodModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;
import com.freshdirect.framework.webapp.ActionError;
import com.freshdirect.framework.webapp.ActionResult;

public class PaymentMethodValidator {
	
	public static ErpPaymentMethodI validatePaymentMethod(FDStandingOrder stadingOrder, FDUserI user, ActionResult result) throws FDResourceException {
		
		String paymentId = stadingOrder.getPaymentMethodId();

		if (paymentId == null || paymentId.length() == 0) {
			result.addError(true, Validations.PAYMENT_METHOD, "payment method not specified");
			return null;
		}

		Collection<ErpPaymentMethodI> paymentMethods = FDCustomerManager.getPaymentMethods(user.getIdentity());
		ErpPaymentMethodI paymentMethod = null;

		for ( ErpPaymentMethodI item : paymentMethods ) {
			if ( item.getPK().getId().equals(paymentId) ) {
				paymentMethod = item;
				break;
			}			
		}

		if (paymentMethod == null) {
			result.addError(new ActionError(Validations.PAYMENT_METHOD, "payment method does not exist"));
		}

		return paymentMethod;
	}

	public static ErpPaymentMethodI checkPaymentMethod(FDStandingOrder stadingOrder, ErpPaymentMethodI paymentMethod, FDUserI user,	ActionResult result) {
		// TODO continue
		return paymentMethod;
	}
}

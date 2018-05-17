/*
 * FDCustomerCreditUtil.java
 * 
 * Created May 08, 2002, 11:52 AM
 */

package com.freshdirect.fdstore.customer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Category;

import com.freshdirect.customer.EnumPaymentType;
import com.freshdirect.customer.ErpAppliedCreditModel;
import com.freshdirect.customer.ErpCreditModel;
import com.freshdirect.customer.ErpCustomerCreditModel;
import com.freshdirect.customer.ErpCustomerModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.adapter.FDOrderAdapter;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.util.DateComparator;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * 
 * @author knadeem
 */
public class FDCustomerCreditUtil {

	private static final Category LOGGER = LoggerFactory.getInstance(FDCustomerCreditUtil.class);

	public static void applyCustomerCredit(FDCartModel cart, FDIdentity identity) throws FDResourceException {

		if (cart.getPaymentMethod() != null && EnumPaymentType.MAKE_GOOD.equals(cart.getPaymentMethod().getPaymentType())) {
			// don't apply credits on make-good orders
			cart.setCustomerCredits(new ArrayList());
			return;
		}

		ErpCustomerModel erpCustomer = FDCustomerFactory.getErpCustomer(identity.getErpCustomerPK());
		List customerCredits = new ArrayList(erpCustomer.getCustomerCredits());

		LOGGER.debug("Customer Credit size: " + customerCredits.size());

		Collections.sort(customerCredits, new CustomerCreditComparator(DateComparator.PRECISION_DAY));
					
		double orderSubTotal = cart.getPreDeductionTotal();
						
		orderSubTotal -= cart.getTotalDiscountValue();

		List appliedCredits = new ArrayList();

		if (cart instanceof FDModifyCartModel) {

			FDModifyCartModel modifyCart = (FDModifyCartModel) cart;
			FDOrderAdapter order = modifyCart.getOriginalOrder();

			for (Iterator i = order.getAppliedCredits().iterator(); i.hasNext();) {
				ErpAppliedCreditModel originalCredit = (ErpAppliedCreditModel) i.next();

				double appliedAmount = Math.min(originalCredit.getAmount(), orderSubTotal);

				if (appliedAmount >= 0.01) {
					appliedCredits
						.add(createAppliedCredit(originalCredit.getCustomerCreditPk(), originalCredit, appliedAmount));
					orderSubTotal -= appliedAmount;
				}

				if (orderSubTotal <= 0) {
					break;
				}

			}
		}

		for (Iterator i = customerCredits.iterator(); i.hasNext() && orderSubTotal > 0;) {
			ErpCustomerCreditModel customerCredit = (ErpCustomerCreditModel) i.next();

			double appliedAmount = Math.min(customerCredit.getRemainingAmount(), orderSubTotal);

			if (appliedAmount >= 0.01) {
				appliedCredits.add(createAppliedCredit(customerCredit.getPK(), customerCredit, appliedAmount));
				orderSubTotal -= appliedAmount;
			}

			if (orderSubTotal <= 0) {
				break;
			}

		}

		LOGGER.debug("Applied Credit size: " + appliedCredits.size());
		cart.setCustomerCredits(appliedCredits);
	}

	private static ErpAppliedCreditModel createAppliedCredit(
		PrimaryKey customerCreditPk,
		ErpCreditModel originalCredit,
		double amount) {
		
		ErpAppliedCreditModel appliedCredit = new ErpAppliedCreditModel();
		appliedCredit.setAmount(amount);
		appliedCredit.setAffiliate(originalCredit.getAffiliate());
		appliedCredit.setDepartment(originalCredit.getDepartment());
		appliedCredit.setCustomerCreditPk(customerCreditPk);
		return appliedCredit;
	}

	private static class CustomerCreditComparator extends DateComparator {

		public CustomerCreditComparator(int precision) {
			super(precision);
		}

		public int compare(Object o1, Object o2) {
			ErpCustomerCreditModel c1 = (ErpCustomerCreditModel) o1;
			ErpCustomerCreditModel c2 = (ErpCustomerCreditModel) o2;
			return super.compare(c1.getCreateDate(), c2.getCreateDate());
		}

	}

}
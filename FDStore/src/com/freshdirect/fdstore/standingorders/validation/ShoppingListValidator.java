package com.freshdirect.fdstore.standingorders.validation;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.fdlogistics.model.FDDeliveryAddressVerificationResponse;
import com.freshdirect.fdlogistics.model.FDInvalidAddressException;
import com.freshdirect.fdstore.FDCachedFactory;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.ZonePriceListing;
import com.freshdirect.fdstore.content.PriceCalculator;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.FDCartLineModel;
import com.freshdirect.fdstore.customer.FDInvalidConfigurationException;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.customer.ejb.FDCustomerManagerSessionBean;
import com.freshdirect.fdstore.lists.FDCustomerCreatedList;
import com.freshdirect.fdstore.lists.FDCustomerListItem;
import com.freshdirect.fdstore.lists.FDCustomerProductListLineItem;
import com.freshdirect.fdstore.lists.FDListManager;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.framework.webapp.ActionResult;

public class ShoppingListValidator {
	
	protected final static NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US);

	private final static Logger LOGGER = LoggerFactory.getInstance(ShoppingListValidator.class);

	public static void validateShoppingList(FDStandingOrder standingOrder, FDUserI user, ErpAddressModel deliveryAddress,
			ActionResult result) throws FDResourceException {
		
		String customerListPK = standingOrder.getCustomerListId();

		if (customerListPK == null || customerListPK.length() == 0) {
			result.addError(true, Validations.SHOPPING_LIST, "shopping list not specified");
			return;
		}

		FDCustomerCreatedList ccl = null;
		try {
			ccl = FDListManager.getCustomerCreatedList(user.getIdentity(), customerListPK);
		} catch (NullPointerException e) {
			result.addError(true, Validations.SHOPPING_LIST, "no such shopping list");
		}
		if (ccl == null) {
			result.addError(true, Validations.SHOPPING_LIST, "no such shopping list");
			return;
		}

		double minOrderAmount = getMinimumOrderAmount(deliveryAddress);

		double subTotal = 0.0;
		boolean alcoholChecked = false;
		
		Iterator<FDCustomerListItem> it = ccl.getLineItems().iterator();

		while ( it.hasNext() ) {
			
			FDCustomerListItem listItem = it.next();
			if ( !( listItem instanceof FDCustomerProductListLineItem ) ) {
				// remove if not appropriate type
				it.remove();
				continue;
			}
			
			FDCustomerProductListLineItem item = (FDCustomerProductListLineItem)listItem;
			try {
				final ProductModel product = item.getProduct();

				if (!product.isFullyAvailable()) {
					result.addWarning(true, Validations.SHOPPING_LIST_ITEM, "product " + product.getFullName()
							+ " is unavailable -- will be removed from the order");
					it.remove();
					continue;
				}

				PriceCalculator calculator = product.getPriceCalculator();
				if (calculator.getHighestDealPercentage() > 0) {
					result.addWarning(true, Validations.SHOPPING_LIST_ITEM, "product " + product.getFullName()
							+ " has discounts which may make difficult to fulfill minimum order amount requirement");
				}

				FDProductInfo prodInfo = FDCachedFactory.getProductInfo(item.getSkuCode());

				FDCartLineModel cartLine = new FDCartLineModel(prodInfo, product, item.getConfiguration(), null, user.getUserContext());
				try {
					cartLine.refreshConfiguration();

					subTotal += cartLine.getPrice();

					if (cartLine.isAlcohol() && !alcoholChecked) {
						if (!FDDeliveryManager.getInstance().checkForAlcoholDelivery(deliveryAddress)) {
							result.addError(true, Validations.SHOPPING_LIST, "no alcohol delivery for this address");
							return;
						}

						if (!standingOrder.isAlcoholAgreement()) {
							result.addError(true, Validations.SHOPPING_LIST,
									"age has not been verified yet but shopping list contains alcohol product");
							return;
						}

						alcoholChecked = true;
					}
				} catch (FDInvalidConfigurationException e) {
					result.addWarning(true, Validations.SHOPPING_LIST_ITEM, "product " + product.getFullName()
							+ " has options not supported anymore -- will be removed from the order");
					it.remove();
					continue;
				}
			} catch (FDSkuNotFoundException e) {
				result.addWarning(true, Validations.SHOPPING_LIST_ITEM, "product not found with SKU " + item.getSkuCode()
						+ " -- will not be included in the order");
				it.remove();
				continue;
			}
		}

		if (subTotal < minOrderAmount) {
			result.addError(true, Validations.SHOPPING_LIST, "estimated total of this order is under the FreshDirect "
					+ CURRENCY_FORMATTER.format(minOrderAmount) + " minimum order requirement");
			return;
		}
	}

	protected static double getMinimumOrderAmount(ErpAddressModel deliveryAddress) throws FDResourceException {
		try {
			FDDeliveryAddressVerificationResponse davResponse = FDDeliveryManager.getInstance().scrubAddress(deliveryAddress, true);
			AddressModel address = davResponse.getAddress();
			String county = FDDeliveryManager.getInstance().getCounty(address);
			if ("SUFFOLK".equalsIgnoreCase(county)) {
				return 100.0;
			}

		} catch (FDInvalidAddressException e) {
			//throw new FDResourceException(e);
			LOGGER.info("ignore invalid address exception");
		}
		return EnumServiceType.CORPORATE.equals(deliveryAddress.getServiceType()) ? FDUserI.MIN_CORP_ORDER_AMOUNT : FDUserI.MINIMUM_ORDER_AMOUNT;
	}
}

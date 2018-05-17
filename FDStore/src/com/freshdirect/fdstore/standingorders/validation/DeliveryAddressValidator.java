package com.freshdirect.fdstore.standingorders.validation;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.freshdirect.common.address.AddressInfo;
import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.fdlogistics.model.EnumRestrictedAddressReason;
import com.freshdirect.fdlogistics.model.FDDeliveryAddressVerificationResponse;
import com.freshdirect.fdlogistics.model.FDDeliveryZoneInfo;
import com.freshdirect.fdlogistics.model.FDInvalidAddressException;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;
import com.freshdirect.framework.webapp.ActionResult;
import com.freshdirect.logistics.delivery.model.EnumAddressVerificationResult;
import com.freshdirect.logistics.delivery.model.EnumZipCheckResponses;

public class DeliveryAddressValidator {
	
	public static ErpAddressModel validateAddress(FDStandingOrder standingOrder, FDUserI user, ActionResult result) throws FDResourceException {
		String addressPK = standingOrder.getAddressId();

		if (addressPK == null || addressPK.length() == 0) {
			result.addError(true, Validations.DELIVERY_ADDRESS, "address not specified");
			return null;
		}

		ErpAddressModel shippingAddress = FDCustomerManager.getAddress(user.getIdentity(), addressPK);

		if (shippingAddress == null) {
			result.addError(true, Validations.DELIVERY_ADDRESS, "address does not exist");
			return null;
		}

		// scrubbing
		FDDeliveryAddressVerificationResponse davResponse;
		try {
			davResponse = FDDeliveryManager.getInstance().scrubAddress(shippingAddress, true);
		
		if (!EnumAddressVerificationResult.ADDRESS_OK.equals(davResponse.getVerifyResult())) {
			result.addError(EnumAddressVerificationResult.NOT_VERIFIED.equals(davResponse.getVerifyResult()),
					Validations.DELIVERY_ADDRESS, "service unavailable at this address");

			result.addError(EnumAddressVerificationResult.ADDRESS_BAD.equals(davResponse.getVerifyResult()),
					Validations.DELIVERY_ADDRESS, "unrecognized address");

			result.addError(EnumAddressVerificationResult.STREET_WRONG.equals(davResponse.getVerifyResult()),
					Validations.DELIVERY_ADDRESS, "unknown street");

			result.addError(EnumAddressVerificationResult.BUILDING_WRONG.equals(davResponse.getVerifyResult()),
					Validations.DELIVERY_ADDRESS, "unknown building number");

			String apartment = davResponse.getAddress().getApartment();
			result.addError(EnumAddressVerificationResult.APT_WRONG.equals(davResponse.getVerifyResult()), Validations.DELIVERY_ADDRESS,
					((apartment == null) || (apartment.length() < 1)) ? "missing apartment number" : "unknown apartment number");

			result.addError(EnumAddressVerificationResult.ADDRESS_NOT_UNIQUE.equals(davResponse.getVerifyResult()),
					Validations.DELIVERY_ADDRESS, "multiple geographic locations match with this address");

			result.addError(result.isSuccess(), Validations.DELIVERY_ADDRESS, "address is invalid from unknown reason");
			return null;
		}
		
		AddressModel address = davResponse.getAddress();

		// for Hamptons customer altContactNumber is mandatory
		if ("SUFFOLK".equals(FDDeliveryManager.getInstance().getCounty(address)) && shippingAddress.getAltContactPhone() == null) {
			result.addError(true, Validations.DELIVERY_ADDRESS, "alternative contact phone number is required");
			return null;
		}

		// restriction checking
		EnumRestrictedAddressReason reason = FDDeliveryManager.getInstance().checkAddressForRestrictions(address);
		if (!EnumRestrictedAddressReason.NONE.equals(reason)) {
			result.addError(true, Validations.DELIVERY_ADDRESS, "address is restricted (" + reason.getDescription().toLowerCase() + ")");
			return null;
		}
		
		String geocodeResult = davResponse.getGeocodeResult();

			if ("GEOCODE_OK".equalsIgnoreCase(geocodeResult)) {
			//	address = geocodeResponse.getAddress();
			} else {
				result.addError(true, Validations.DELIVERY_ADDRESS, "failed to find geographic location for this address");
				return null;
			}
		

		Calendar date = new GregorianCalendar();
		date.add(Calendar.DATE, 7);

		FDDeliveryZoneInfo dlvResponse;
		dlvResponse = FDDeliveryManager.getInstance().getZoneInfo(address, date.getTime(), user.getHistoricOrderSize(),  user.getRegionSvcType(address.getId()));
			result.addError((!EnumZipCheckResponses.DELIVER.equals(dlvResponse.getResponse())), Validations.DELIVERY_ADDRESS,
					"service unavailable in this zone");
			if (result.isFailure())
				return null;

			AddressInfo info = address.getAddressInfo();
			if (info == null) {
				info = new AddressInfo();
			}
			info.setZoneId(dlvResponse.getZoneId());
			info.setZoneCode(dlvResponse.getZoneCode());
			address.setAddressInfo(info);

			shippingAddress.setAddressInfo(address.getAddressInfo());

			checkForSoEligibility(shippingAddress, result);
			if (result.isSuccess())
				return shippingAddress;
			else
				return null;
		}catch (FDInvalidAddressException e) {
			result.addError(true, Validations.DELIVERY_ADDRESS, "invalid address detected");
			return null;
		}
	}

	private static void checkForSoEligibility(ErpAddressModel address, ActionResult result) {
		result.addError(!EnumServiceType.CORPORATE.equals(address.getServiceType()), Validations.DELIVERY_ADDRESS,
				"Standing Orders service is not available for this address");
	}
}

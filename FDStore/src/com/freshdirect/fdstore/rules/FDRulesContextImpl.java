package com.freshdirect.fdstore.rules;

import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpDepotAddressModel;
import com.freshdirect.fdlogistics.model.EnumDeliveryFeeTier;
import com.freshdirect.fdlogistics.model.FDDeliveryDepotModel;
import com.freshdirect.fdlogistics.model.FDTimeslot;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.customer.ProfileModel;
import com.freshdirect.logistics.controller.data.PickupData;
import com.freshdirect.logistics.delivery.model.Pickup;
import com.freshdirect.rules.RulesRuntimeException;

/**
 * @author knadeem Date Apr 12, 2005
 */

public class FDRulesContextImpl implements FDRuleContextI {
	
	private final FDUserI user;
	private final FDTimeslot timeslot;
	private Double subTotal;
	private EnumDeliveryFeeTier deliveryFeeTier;

	public FDRulesContextImpl(FDUserI user) {
		this.user = user;
		this.timeslot = null;
	}

	public FDRulesContextImpl(FDUserI user, FDTimeslot timeslot) {
		this.user = user;
		this.timeslot = timeslot;
		this.subTotal = getOrderTotal();
	}
	public FDRulesContextImpl(FDUserI user, FDTimeslot timeslot, Double subTotal) {
		this.user = user;
		this.timeslot = timeslot;
		this.subTotal = subTotal;
	}
	public FDRulesContextImpl(FDUserI user, EnumDeliveryFeeTier tier) {
		this.user = user;
		this.timeslot = null;
		this.deliveryFeeTier = tier;
	}

	public String getCounty() {
		try {
			ErpAddressModel address = user.getShoppingCart().getDeliveryAddress();
			return FDDeliveryManager.getInstance().getCounty(address);
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
	}

	public EnumServiceType getServiceType() {
		return user.getSelectedServiceType();
	}

	public boolean isChefsTable() {
		try {
			return this.user.isChefsTable();
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
	}

	public boolean isVip() {
		return this.hasProfileAttribute("VIPCustomer", "true");
	}

	public double getOrderTotal() {
		return this.user.getShoppingCart().getSubTotal();
	}

	public boolean hasProfileAttribute(String attributeName, String attributeValue) {
		try {
			if (user.getIdentity() == null)
				return false;
			ProfileModel pm = user.getFDCustomer().getProfile();
			if (pm == null)
				return false;

			String attribValue = pm.getAttribute(attributeName);
			if (attributeValue == null)
				return attribValue != null;
			return (attributeValue.equalsIgnoreCase(attribValue));
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
	}
	
	public String getDepotCode () {
		AddressModel address = this.user.getShoppingCart().getDeliveryAddress();
		if(address == null || !(address instanceof ErpDepotAddressModel)){
			return "";
		}
		ErpDepotAddressModel location = (ErpDepotAddressModel)address;
		try {
			FDDeliveryDepotModel depot = FDDeliveryManager.getInstance().getDepotByLocationId(location.getLocationId());
			return depot.getDepotCode();
		} catch (FDResourceException e) {
			throw new RulesRuntimeException("Cannot find depot for location: "+location.getLocationId());
		}
	}

	public FDUserI getUser() {
		return this.user;
	}

	public FDTimeslot getTimeslot() {
		return timeslot;
	}
	public Double getSubTotal() {
		return subTotal;
	}

	@Override
	public EnumDeliveryFeeTier getDeliverFeeTier() {
		return deliveryFeeTier;
	}
}

package com.freshdirect.fdstore.customer;

import java.util.HashMap;
import java.util.Map;

import com.freshdirect.framework.core.ModelSupport;

public class ProfileModel extends ModelSupport {

	private final Map<String,String> attributes = new HashMap<String,String>();

	public String getAttribute(String key) {
		return (String) attributes.get(key);
	}

	public void setAttribute(String key, String value) {
		this.attributes.put(key, value);
	}

	public Map<String,String> getAttributes() {
		return this.attributes;
	}

	public void setAttributes(Map<String,String> attributes) {
		this.attributes.putAll(attributes);
	}

	public void removeAttribute(String key) {
		this.attributes.remove(key);
	}
	
	/** helper methods **/
	public boolean isVIPCustomer() {
		return "true".equalsIgnoreCase(getAttribute("VIPCustomer"));
	}

	public boolean isChefsTable() {
		return "1".equalsIgnoreCase(getAttribute("ChefsTable"));
	}
	public boolean isOASTest() {
		return "true".equalsIgnoreCase(getAttribute("OAS_TEST"));
	}

	public String getCustomerMetalType() {
		String metalValue = getAttribute("MetalCategory");
		return (metalValue == null || metalValue.length() != 1 || "0123456".indexOf(metalValue) == -1) ? "0" : metalValue;
	}
	
	public String getChefsTableInduction() {
		String inductionValue = getAttribute("ChefsTableInduction");
		return (inductionValue == null || inductionValue.length() < 1) ? "0" : inductionValue;
	}
	
	public String getWinback() {
		String winbackValue = getAttribute("Winback");
		return (winbackValue == null || winbackValue.length() < 1) ? "0" : winbackValue;
	}
	
	public String getMarketingPromo() {
		String marketingPromoValue = getAttribute("MarketingPromo");
		return (marketingPromoValue == null || marketingPromoValue.length() < 1) ? "0" : marketingPromoValue;
	}

	public boolean isCOSPilot() {
		return "true".equalsIgnoreCase(getAttribute("COSPilot"));
	}

	public String getQuickShopLevel() {
		return getAttribute("QuickShopLevel");
	}

	public String getEmergencyFlag() {
		return getAttribute("EmergencyFlag");
	}

	public String getProfileGroup() {
		return getAttribute("ProfileGroup");
	}

	public boolean isPhonePrivate() {
		return "true".equalsIgnoreCase(getAttribute("PhonePrivate"));
	}
	
	public boolean isOnFDAccount(){
		return ("true".equalsIgnoreCase(getAttribute("onFDAccount")));
	}
	
	public String getEcpPromo(){
		return getAttribute("EcpPromo");
	}
	
	public String getDeliveryPass() {
		return getAttribute("DeliveryPass");
	}
	public String getHouseholdType() {
		String householdType = getAttribute("HouseholdType");
		return (householdType == null || householdType.length() != 1 || "0123456".indexOf(householdType) == -1) ? "unknown" : householdType;
	}
	
	public boolean allowApplyGC() {
		return null == (getAttribute("allow_apply_gc")) || "true".equalsIgnoreCase(getAttribute("allow_apply_gc"));
	}

	public boolean isVHInDelivery() {
		return "1".equalsIgnoreCase(getAttribute("VoucherHolderInDeliveryZone"));
	}

	public boolean isVHOutOfDelivery() {
		return "1".equalsIgnoreCase(getAttribute("VoucherHolderOutOfDeliveryZone"));
	}
	
}

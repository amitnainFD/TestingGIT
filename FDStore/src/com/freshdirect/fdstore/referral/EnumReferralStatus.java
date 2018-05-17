/*
 * Created on Jun 2, 2005
 *
 */
package com.freshdirect.fdstore.referral;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

/**
 * @author jng
 */
public class EnumReferralStatus extends Enum {

	public static final EnumReferralStatus REFERRED = new EnumReferralStatus("REF", "Referred");
	public static final EnumReferralStatus RESTRICTED = new EnumReferralStatus("REST", "Customer is restricted from referrals");
	public static final EnumReferralStatus MAXED_REFERRALS = new EnumReferralStatus("MAX_REF", "Customer reached maximum referrals to send");
	public static final EnumReferralStatus REFERRAL_ALREADY_CUST = new EnumReferralStatus("FDCUST", "Referral already a customer");
	public static final EnumReferralStatus INELIGIBLE = new EnumReferralStatus("INEL", "Customer is ineligible for referrals");
	public static final EnumReferralStatus DELETED = new EnumReferralStatus("DEL", "Referral deleted");
	public static final EnumReferralStatus SIGNUP = new EnumReferralStatus("SIGNUP", "Referral Signed up");
	public static final EnumReferralStatus REFERRER_PROMOTED = new EnumReferralStatus("REFERRER_PROMOTED", "Referrer Promoted");

    private String description;

    protected EnumReferralStatus(String name, String description) {
		super(name);
	    this.description = description;
	}
	
	public static EnumReferralStatus getEnum(String type) {
		return (EnumReferralStatus) getEnum(EnumReferralStatus.class, type);
	}
		
	public static Map getEnumMap() {
		return getEnumMap(EnumReferralStatus.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumReferralStatus.class);
	}

	public static Iterator iterator() {
		return iterator(EnumReferralStatus.class);
	}

	public String getDescription(){
		return this.description;
	}

	public String toString() {
		return this.description;		
	}

}

/*
 * Created on Jun 2, 2005
 */
package com.freshdirect.fdstore.referral;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

/**
 * @author jng
 *
 */
public class EnumReferralActionType extends Enum {
	public static final EnumReferralActionType SENT = new EnumReferralActionType("SNT", "Email Sent");
	public static final EnumReferralActionType ACCEPT = new EnumReferralActionType("ACT", "Referral Accepted");
	public static final EnumReferralActionType SIGNUP = new EnumReferralActionType("SUP", "Referral Signed up");
	public static final EnumReferralActionType NONE = new EnumReferralActionType("NONE", "No Action Taken");

    private String description;

    protected EnumReferralActionType(String name, String description) {
		super(name);
	    this.description = description;
	}
	
	public static EnumReferralActionType getEnum(String type) {
		return (EnumReferralActionType) getEnum(EnumReferralActionType.class, type);
	}
		
	public static Map getEnumMap() {
		return getEnumMap(EnumReferralActionType.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumReferralActionType.class);
	}

	public static Iterator iterator() {
		return iterator(EnumReferralActionType.class);
	}

	public String getDescription(){
		return this.description;
	}

	public String toString() {
		return this.description;		
	}	
}

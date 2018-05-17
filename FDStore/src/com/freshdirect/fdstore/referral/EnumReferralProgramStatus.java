/*
 * Created on Jun 2, 2005
 *
 */
package com.freshdirect.fdstore.referral;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;


public class EnumReferralProgramStatus extends Enum {
	public static final EnumReferralProgramStatus NEW = new EnumReferralProgramStatus("NEW", "New");
	public static final EnumReferralProgramStatus DELETED = new EnumReferralProgramStatus("DEL", "Deleted");
	public static final EnumReferralProgramStatus ACTIVE = new EnumReferralProgramStatus("ACT", "Active");
	public static final EnumReferralProgramStatus EXPIRED = new EnumReferralProgramStatus("EXP", "Expired");

    private String description;

    protected EnumReferralProgramStatus(String name, String description) {
		super(name);
	    this.description = description;
	}
	
	public static EnumReferralProgramStatus getEnum(String type) {
		return (EnumReferralProgramStatus) getEnum(EnumReferralProgramStatus.class, type);
	}
		
	public static Map getEnumMap() {
		return getEnumMap(EnumReferralProgramStatus.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumReferralProgramStatus.class);
	}

	public static Iterator iterator() {
		return iterator(EnumReferralProgramStatus.class);
	}

	public String getDescription(){
		return this.description;
	}

	public String toString() {
		return this.description;		
	}
}

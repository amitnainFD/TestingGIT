package com.freshdirect.fdstore.customer;

import org.apache.commons.lang.enums.Enum;

public class EnumIPhoneCaptureType extends Enum {
	
	public static final EnumIPhoneCaptureType UNREGISTERED = new EnumIPhoneCaptureType("UNREGISTERED");
	public static final EnumIPhoneCaptureType EXISTING = new EnumIPhoneCaptureType("EXISTING");
	public static final EnumIPhoneCaptureType INVALID_EMAIL = new EnumIPhoneCaptureType("INVALID_EMAIL");

	protected EnumIPhoneCaptureType(String name) {
		super(name);
	}
	
	public static EnumIPhoneCaptureType getEnum(String type) {
		return (EnumIPhoneCaptureType) getEnum(EnumIPhoneCaptureType.class, type);
	}

}

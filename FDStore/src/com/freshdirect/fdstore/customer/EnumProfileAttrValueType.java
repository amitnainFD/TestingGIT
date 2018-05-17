/*
 * Created on Jun 28, 2005
 *
 */
package com.freshdirect.fdstore.customer;

import org.apache.commons.lang.enums.Enum;

/**
 * @author jng
 *
 */
public class EnumProfileAttrValueType extends Enum {

	public static final EnumProfileAttrValueType TEXT = new EnumProfileAttrValueType("TEXT");
	public static final EnumProfileAttrValueType SET = new EnumProfileAttrValueType("SET");
	public static final EnumProfileAttrValueType TRUE_FALSE = new EnumProfileAttrValueType("TRUE_FALSE");
	public static final EnumProfileAttrValueType ON_OFF = new EnumProfileAttrValueType("ON_OFF");
	public static final EnumProfileAttrValueType YES_NO = new EnumProfileAttrValueType("YES_NO");

	protected EnumProfileAttrValueType(String name) {
		super(name);
	}
	
	public static EnumProfileAttrValueType getEnum(String type) {
		return (EnumProfileAttrValueType) getEnum(EnumProfileAttrValueType.class, type);
	}
}

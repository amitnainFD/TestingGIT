package com.freshdirect.fdstore.survey;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;



public class EnumFormDisplayType  extends Enum {
	
	public static final EnumFormDisplayType SINGLE_ANS_PER_ROW = new EnumFormDisplayType("1", "Single Answer per row");
	public static final EnumFormDisplayType TWO_ANS_PER_ROW = new EnumFormDisplayType("2", "Two Answers per row");
	public static final EnumFormDisplayType GROUPED_RADIO_BUTTON = new EnumFormDisplayType("3", "Grouped Radio Buttons");
	public static final EnumFormDisplayType GROUPED_MULTI_SELECTION = new EnumFormDisplayType("4", "Grouped Multi selections");
	public static final EnumFormDisplayType DISPLAY_PULLDOWN_GROUP = new EnumFormDisplayType("5", "Pulldown with grouped display");
	public static final EnumFormDisplayType IMAGE_DISPLAY = new EnumFormDisplayType("6", "Display 6 images per row");
	public static final EnumFormDisplayType MULTIPLE_PULLDOWN_GROUP = new EnumFormDisplayType("7", "Multiple Pulldown with grouped display");
	
	private final String displayName;
	private EnumFormDisplayType(String name, String displayName) {
		super(name);
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static EnumFormDisplayType getEnum(String name) {
		return name==null||"".equals(name)?EnumFormDisplayType.SINGLE_ANS_PER_ROW:(EnumFormDisplayType) getEnum(EnumFormDisplayType.class, name);
	}

	public static Map getEnumMap() {
		return getEnumMap(EnumFormDisplayType.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumFormDisplayType.class);
	}

	public static Iterator iterator() {
		return iterator(EnumFormDisplayType.class);
	}
}


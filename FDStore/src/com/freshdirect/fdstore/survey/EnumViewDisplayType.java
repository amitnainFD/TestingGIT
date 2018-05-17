package com.freshdirect.fdstore.survey;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

public class EnumViewDisplayType extends Enum {
	
	public static final EnumViewDisplayType SINGLE_ANS_PER_ROW = new EnumViewDisplayType("1", "Single Answer per row");
	public static final EnumViewDisplayType NUMBERED_LIST = new EnumViewDisplayType("2", "Numbered List");
	public static final EnumViewDisplayType COMMA_SEPARATED = new EnumViewDisplayType("3", "Comma separated");
	public static final EnumViewDisplayType GROUPED_COMMA_SEPARATED = new EnumViewDisplayType("4", "Grouped Comma Separated");
	public static final EnumViewDisplayType GROUPED_LIST = new EnumViewDisplayType("5", "Grouped List");
	public static final EnumViewDisplayType IMAGE_DISPLAY = new EnumViewDisplayType("6", "Display 6 images per row");
	public static final EnumViewDisplayType MULTIPLE_PULLDOWN_GROUP = new EnumViewDisplayType("7", "Multiple Pulldown with grouped display");
	
	private final String displayName;
	private EnumViewDisplayType(String name, String displayName) {
		super(name);
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static EnumViewDisplayType getEnum(String name) {
		return name==null||"".equals(name)?EnumViewDisplayType.SINGLE_ANS_PER_ROW: (EnumViewDisplayType) getEnum(EnumViewDisplayType.class, name);
	}

	public static Map getEnumMap() {
		return getEnumMap(EnumViewDisplayType.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumViewDisplayType.class);
	}

	public static Iterator iterator() {
		return iterator(EnumViewDisplayType.class);
	}
}
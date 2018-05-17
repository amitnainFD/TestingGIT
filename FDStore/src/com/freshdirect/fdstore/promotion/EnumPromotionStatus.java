package com.freshdirect.fdstore.promotion;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

public class EnumPromotionStatus extends Enum{
	private static final long serialVersionUID = 3857209226074690728L;

	public static final EnumPromotionStatus DRAFT = new EnumPromotionStatus("DRAFT", "Draft"); // client-side
	public static final EnumPromotionStatus APPROVE = new EnumPromotionStatus("APPROVED", "Approved"); // client-side
	public static final EnumPromotionStatus PROGRESS = new EnumPromotionStatus("PROGRESS", "In Progress");
	public static final EnumPromotionStatus TEST = new EnumPromotionStatus("TEST", "Test");  // client-side
	public static final EnumPromotionStatus PUBLISHED = new EnumPromotionStatus("PUBLISHED", "Published"); // client-side
	public static final EnumPromotionStatus EXPIRED = new EnumPromotionStatus("EXPIRED", "Expired"); // client-side
	public static final EnumPromotionStatus CANCELLING = new EnumPromotionStatus("CANCELLING", "Cancelling"); // client-side

	public static final EnumPromotionStatus LIVE = new EnumPromotionStatus("LIVE", "Live"); // server-side
	public static final EnumPromotionStatus CANCELLED = new EnumPromotionStatus("CANCELLED", "Cancelled"); // server AND client-side

	
	private final String description;
	public EnumPromotionStatus(String name, String description) {
		super(name);
		this.description = description;
	}
	
	public static EnumPromotionStatus getEnum(String name) {
		return (EnumPromotionStatus) getEnum(EnumPromotionStatus.class, name);
	}

	public static Map getEnumMap() {
		return getEnumMap(EnumPromotionStatus.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumPromotionStatus.class);
	}

	public static Iterator iterator() {
		return iterator(EnumPromotionStatus.class);
	}

	public String getDescription() {
		return description;
	}

}

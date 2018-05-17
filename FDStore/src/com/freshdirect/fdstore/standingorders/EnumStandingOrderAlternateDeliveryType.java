package com.freshdirect.fdstore.standingorders;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

public class EnumStandingOrderAlternateDeliveryType extends Enum implements Serializable{
		
	private static final long serialVersionUID = -2259486152763482895L;
	
	public final static EnumStandingOrderAlternateDeliveryType	SKIP_DELIVERY	= new EnumStandingOrderAlternateDeliveryType( "SKIP_DELIVERY", "Skip the standing order next delivery" );
	public final static EnumStandingOrderAlternateDeliveryType	ALTERNATE_DELIVERY	= new EnumStandingOrderAlternateDeliveryType( "ALTERNATE_DELIVERY", "Change the standing order delivery" );
	
	private final String description;
	public EnumStandingOrderAlternateDeliveryType(String name, String description) {
		super(name);
		this.description = description;
	}
	
	public static EnumStandingOrderAlternateDeliveryType getEnum(String name) {
		return (EnumStandingOrderAlternateDeliveryType) getEnum(EnumStandingOrderAlternateDeliveryType.class, name);
	}

	public static Map getEnumMap() {
		return getEnumMap(EnumStandingOrderAlternateDeliveryType.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumStandingOrderAlternateDeliveryType.class);
	}

	public static Iterator iterator() {
		return iterator(EnumStandingOrderAlternateDeliveryType.class);
	}

	public String getDescription() {
		return description;
	}	

}

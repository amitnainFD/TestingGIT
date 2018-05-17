package com.freshdirect.fdstore.customer.ejb;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;

import com.freshdirect.framework.util.log.LoggerFactory;

public class EnumMobilePreferenceType implements java.io.Serializable {
	
	private static final long	serialVersionUID	= -2281974517045649443L;

	private final static Category LOGGER = LoggerFactory.getInstance(EnumMobilePreferenceType.class);
	
	private final static Map<String,EnumMobilePreferenceType> MOB_PREF_MAP = new HashMap<String,EnumMobilePreferenceType>();
	
	private static int idCounter = 0;

	public final static EnumMobilePreferenceType	SAW_MOBILE_PREF	 = new EnumMobilePreferenceType( "Saw the Mobile Pref Window", "1" );
	public final static EnumMobilePreferenceType	UPDATED_FROM_RECEIPT_PAGE = new EnumMobilePreferenceType("Updated From Receipt page", "2" );
	
	
	private EnumMobilePreferenceType(String code, String name) {
        this.id = idCounter++;
        this.code = code;
        this.name = name;
        
        if ( code == null || code.length() == 0 ) {
        	LOGGER.warn( "Activity code cannot be null or empty. This will cause sql errors later." );
        	return;
        }
        if ( code.length() > 16 ) {
        	LOGGER.warn( "Activity code ["+code+"] cannot be longer than 16. This will cause sql errors later." );
        	return;        	
        }
        
        MOB_PREF_MAP.put(code, this);
    }

    public String getCode() {
		return this.code;
	}

    public String getName() {
        return this.name;
    }

	public static EnumMobilePreferenceType getActivityType(String code) {
		EnumMobilePreferenceType activityType = MOB_PREF_MAP.get(code);
		return activityType;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof EnumMobilePreferenceType) {
			return this.id == ((EnumMobilePreferenceType)o).id;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return getCode() + " / " + getName();
	}

    private int id;
    private String code;
    private String name;

}

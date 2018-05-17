package com.freshdirect.fdstore.iplocator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Category;

import com.freshdirect.common.address.AddressModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDUser;
import com.freshdirect.framework.util.NVL;
import com.freshdirect.framework.util.log.LoggerFactory;

public class IpLocatorUtil {

	private static final Category LOGGER = LoggerFactory.getInstance(IpLocatorUtil.class);
	
	private static final Map<String, String> STATE_TO_CODE_MAP = new ConcurrentHashMap<String, String>(); 
	
	static {
		STATE_TO_CODE_MAP.put("ALABAMA", "AL");
		STATE_TO_CODE_MAP.put("ALASKA", "AK");
		STATE_TO_CODE_MAP.put("ARIZONA", "AZ");
		STATE_TO_CODE_MAP.put("ARKANSAS", "AR");
		STATE_TO_CODE_MAP.put("CALIFORNIA", "CA");
		STATE_TO_CODE_MAP.put("COLORADO", "CO");
		STATE_TO_CODE_MAP.put("CONNECTICUT", "CT");
		STATE_TO_CODE_MAP.put("DELAWARE", "DE");
		STATE_TO_CODE_MAP.put("FLORIDA", "FL");
		STATE_TO_CODE_MAP.put("GEORGIA", "GA");
		STATE_TO_CODE_MAP.put("HAWAII", "HI");
		STATE_TO_CODE_MAP.put("IDAHO", "ID");
		STATE_TO_CODE_MAP.put("ILLINOIS", "IL");
		STATE_TO_CODE_MAP.put("INDIANA", "IN");
		STATE_TO_CODE_MAP.put("IOWA", "IA");
		STATE_TO_CODE_MAP.put("KANSAS", "KS");
		STATE_TO_CODE_MAP.put("KENTUCKY", "KY");
		STATE_TO_CODE_MAP.put("LOUISIANA", "LA");
		STATE_TO_CODE_MAP.put("MAINE", "ME");
		STATE_TO_CODE_MAP.put("MARYLAND", "MD");
		STATE_TO_CODE_MAP.put("MASSACHUSETTS", "MA");
		STATE_TO_CODE_MAP.put("MICHIGAN", "MI");
		STATE_TO_CODE_MAP.put("MINNESOTA", "MN");
		STATE_TO_CODE_MAP.put("MISSISSIPPI", "MS");
		STATE_TO_CODE_MAP.put("MISSOURI", "MO");
		STATE_TO_CODE_MAP.put("MONTANA", "MT");
		STATE_TO_CODE_MAP.put("NEBRASKA", "NE");
		STATE_TO_CODE_MAP.put("NEVADA", "NV");
		STATE_TO_CODE_MAP.put("NEW HAMPSHIRE", "NH");
		STATE_TO_CODE_MAP.put("NEW JERSEY", "NJ");
		STATE_TO_CODE_MAP.put("NEW MEXICO", "NM");
		STATE_TO_CODE_MAP.put("NEW YORK", "NY");
		STATE_TO_CODE_MAP.put("NORTH CAROLINA", "NC");
		STATE_TO_CODE_MAP.put("NORTH DAKOTA", "ND");
		STATE_TO_CODE_MAP.put("OHIO", "OH");
		STATE_TO_CODE_MAP.put("OKLAHOMA", "OK");
		STATE_TO_CODE_MAP.put("OREGON", "OR");
		STATE_TO_CODE_MAP.put("PENNSYLVANIA", "PA");
		STATE_TO_CODE_MAP.put("RHODE ISLAND", "RI");
		STATE_TO_CODE_MAP.put("SOUTH CAROLINA", "SC");
		STATE_TO_CODE_MAP.put("SOUTH DAKOTA", "SD");
		STATE_TO_CODE_MAP.put("TENNESSEE", "TN");
		STATE_TO_CODE_MAP.put("TEXAS", "TX");
		STATE_TO_CODE_MAP.put("UTAH", "UT");
		STATE_TO_CODE_MAP.put("VERMONT", "VT");
		STATE_TO_CODE_MAP.put("VIRGINIA", "VA");
		STATE_TO_CODE_MAP.put("WASHINGTON", "WA");
		STATE_TO_CODE_MAP.put("WEST VIRGINIA", "WV");
		STATE_TO_CODE_MAP.put("WISCONSIN", "WI");
		STATE_TO_CODE_MAP.put("WYOMING", "WY");
		STATE_TO_CODE_MAP.put("AMERICAN SAMOA", "AS");
		STATE_TO_CODE_MAP.put("DISTRICT OF COLUMBIA", "DC");
		STATE_TO_CODE_MAP.put("FEDERATED STATES OF MICRONESIA", "FM");
		STATE_TO_CODE_MAP.put("GUAM", "GU");
		STATE_TO_CODE_MAP.put("MARSHALL ISLANDS", "MH");
		STATE_TO_CODE_MAP.put("NORTHERN MARIANA ISLANDS", "MP");
		STATE_TO_CODE_MAP.put("PALAU", "PW");
		STATE_TO_CODE_MAP.put("PUERTO RICO", "PR");
		STATE_TO_CODE_MAP.put("VIRGIN ISLANDS", "VI");
	}
	
	public static String getStateCode(String state){
		return NVL.apply(STATE_TO_CODE_MAP.get(state.toUpperCase()), state); 
	}
	
	public static boolean validate(IpLocatorData ipLocatorData) {

		String zipCode = ipLocatorData.getZipCode();

		if (zipCode==null || "".equals(zipCode.trim())){
			LOGGER.error("Zip is empty");
			return false;
		}

		String countryCode = ipLocatorData.getCountryCode();
		if (!countryCode.equalsIgnoreCase("US")){
			String errorMsg = "Country code invalid: " + countryCode;
			LOGGER.error(errorMsg);
			return false;
		}
		
		return true;
	}

	public static void appendMissingFieldsToUserAddress(IpLocatorData ipLocatorData, FDUser user) throws FDResourceException {
		AddressModel address = user.getAddress();
		if (address!=null && (
				NVL.apply(address.getCity(), "").length()==0 || 
				NVL.apply(address.getState(), "").length()==0)){
			
			address.setState(getStateCode(ipLocatorData.getRegion()));
			address.setCity(ipLocatorData.getCity());
			
			FDCustomerManager.storeUser(user);
			LOGGER.error("Appended city and state fields to address: " + address);
		}
	}
	
}

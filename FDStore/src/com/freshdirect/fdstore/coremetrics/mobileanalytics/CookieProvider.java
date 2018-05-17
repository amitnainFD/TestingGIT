package com.freshdirect.fdstore.coremetrics.mobileanalytics;

public class CookieProvider {

	public static String generateUniqueCJUID(String fdUserId) {

		StringBuffer cjuid = new StringBuffer();
		
		cjuid.append("1");

		for (int i = fdUserId.length() + 1; i < 23; i++) {
			cjuid.append("0");
		}
		
		cjuid.append(fdUserId);
		
		return cjuid.toString();
	}

	public static String generateUniqueCJSID() {

		StringBuffer cjsid = new StringBuffer();
		
		int hashCodePart = new Object().hashCode();
		int randomPart = new Double(Math.random()  * 100000).intValue() * 100;
		
		int sumPart = hashCodePart + randomPart;
		
		cjsid.append(Integer.toString(sumPart));
		
		for (int i = cjsid.length(); i < 10; i++) {
			cjsid.append(new Double(Math.random()  * 10).intValue());
		}
		
		return cjsid.toString();
	}
	
}

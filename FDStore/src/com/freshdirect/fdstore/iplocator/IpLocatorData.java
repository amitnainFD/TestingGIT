package com.freshdirect.fdstore.iplocator;

public class IpLocatorData {
	private String zipCode;
	private String countryCode;
	private String region;
	private String city;
	
	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	
	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String toString(){
		return String.format("zipCode: %s, countryCode: %s, region: %s, city: %s", zipCode, countryCode, region, city);
	}
}

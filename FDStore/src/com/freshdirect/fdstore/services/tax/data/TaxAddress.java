package com.freshdirect.fdstore.services.tax.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaxAddress // Nested in GetTaxResult object
{
	@JsonProperty("Address")
	public String address;

	@JsonProperty("AddressCode")
	public String addressCode;
	
	@JsonProperty("City")
	public String city;
	
	@JsonProperty("Region")
	public String region;
	
	@JsonProperty("Country")
	public String country;
	
	@JsonProperty("PostalCode")
	public String postalCode;
	
	@JsonProperty("Latitude")
	public String latitude;
	
	@JsonProperty("Longitude")
	public String longitude;
	
	@JsonProperty("TaxRegionId")
	public String taxRegionId;
	
	@JsonProperty("JurisCode")
	public String jurisCode;

	public String getAddressCode() {
		return addressCode;
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}

	public String getRegion() {
		return region;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getCountry() {
		return country;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public String getTaxRegionId() {
		return taxRegionId;
	}

	public String getJurisCode() {
		return jurisCode;
	}

	public void setAddressCode(String addressCode) {
		this.addressCode = addressCode;
	}

	public void setLine1(String address) {
		this.address = address;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public void setTaxRegionId(String taxRegionId) {
		this.taxRegionId = taxRegionId;
	}

	public void setJurisCode(String jurisCode) {
		this.jurisCode = jurisCode;
	}
}
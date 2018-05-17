package com.freshdirect.fdstore.services.tax.data;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Address {
	
	private static final Logger LOGGER = Logger.getLogger(Address.class);
	
	public enum AddressType
	{
		F,//Firm or company address
        G,//General Delivery address
        H,//High-rise or business complex
        P,//PO box address
        R,//Rural route address
        S;//Street or residential address
	}
    //Address can be determined for tax calculation by Line1, City, Region, PostalCode, Country OR Latitude/Longitude OR TaxRegionId
    @JsonProperty("AddressCode")
	private String addressCode; //Input for GetTax only, not by address validation
    @JsonProperty("Line1")
    private String line1;
    @JsonProperty("Line2")
    private String line2;
    @JsonProperty("Line3")
    private String line3;
    @JsonProperty("City")
    private String city;
    @JsonProperty("Region")
    private String region;
    @JsonProperty("PostalCode")
    private String postalCode;
    @JsonProperty("Country")
    private String country;
    @JsonProperty("County")
    private String county; //Output for ValidateAddress only
    @JsonProperty("FipsCode")
    private String fipsCode; //Output for ValidateAddress only
    @JsonProperty("CarrierRoute")
    private String carrierRoute; //Output for ValidateAddress only
    @JsonProperty("PostNet")
    private String postNet;//Output for ValidateAddress only
    @JsonProperty("AddressType")
    private AddressType addressType; //Output for ValidateAddress only
    @JsonProperty("Latitude")
    private BigDecimal latitude; //Input for GetTax only
    @JsonProperty("Longitude")
    private BigDecimal longitude; //Input for GetTax only
    @JsonProperty("TaxRegionId")
    private String taxRegionId; //Input for GetTax only
	
    public String toQuery(){ //Formats the address input information as a query string for address validation.
    	String query = "";
    	String[][] addressinfo = {{"Line1","Line2","Line3","City","Region","PostalCode","Country"},{line1, line2, line3, city, region, postalCode, country}};
    	for (int i=0; i<7; i++)
    	{
	        if(addressinfo[1][i] != null &&!addressinfo[1][i].isEmpty()){
	        	try {
					query = ampQuery(query) + addressinfo[0][i]+"="+ URLEncoder.encode(addressinfo[1][i], "UTF-8");
				} catch (UnsupportedEncodingException e) {
					LOGGER.error(e);
				}
	        }
    	}
    	return query;
    }
    
    public String toAddrString(){
    	return line1+ " "+ city + ", "+ region+" "+postalCode;
    }
    private String ampQuery(String sub)
    {
    	if(!sub.endsWith("&") && sub.length()>0)
    	{
    		return sub+"&";
    	}
    	else
    	{
    		return sub;
    	}
    	
    }
    
    public String getAddressCode() {
		return addressCode;
	}

	public void setAddressCode(String addressCode) {
		this.addressCode = addressCode;
	}

	public String getLine1() {
		return line1;
	}

	public void setLine1(String line1) {
		this.line1 = line1;
	}

	public String getLine2() {
		return line2;
	}

	public void setLine2(String line2) {
		this.line2 = line2;
	}

	public String getLine3() {
		return line3;
	}

	public void setLine3(String line3) {
		this.line3 = line3;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getFipsCode() {
		return fipsCode;
	}

	public void setFipsCode(String fipsCode) {
		this.fipsCode = fipsCode;
	}

	public String getCarrierRoute() {
		return carrierRoute;
	}

	public void setCarrierRoute(String carrierRoute) {
		this.carrierRoute = carrierRoute;
	}

	public String getPostNet() {
		return postNet;
	}

	public void setPostNet(String postNet) {
		this.postNet = postNet;
	}

	public AddressType getAddressType() {
		return addressType;
	}

	public void setAddressType(AddressType addressType) {
		this.addressType = addressType;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public String getTaxRegionId() {
		return taxRegionId;
	}

	public void setTaxRegionId(String taxRegionId) {
		this.taxRegionId = taxRegionId;
	}
}
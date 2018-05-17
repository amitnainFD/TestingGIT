package com.freshdirect.fdstore.services.tax.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaxDetail // Nested in GetTaxResult object
{
	@JsonProperty("Rate")
	public double rate;

	@JsonProperty("Tax")
	public double tax;

	@JsonProperty("Taxable")
	public double taxable;

	@JsonProperty("Country")
	public String country;

	@JsonProperty("Region")
	public String region;

	@JsonProperty("JurisType")
	public String jurisType;

	@JsonProperty("JurisName")
	public String jurisName;

	@JsonProperty("JurisCode")
	public String jurisCode;

	@JsonProperty("TaxName")
	public String taxName;

	public double getRate() {
		return rate;
	}

	public double getTax() {
		return tax;
	}

	public double getTaxable() {
		return taxable;
	}

	public String getCountry() {
		return country;
	}

	public String getRegion() {
		return region;
	}

	public String getJurisType() {
		return jurisType;
	}

	public String getJurisName() {
		return jurisName;
	}

	public String getJurisCode() {
		return jurisCode;
	}

	public String getTaxName() {
		return taxName;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public void setTax(double tax) {
		this.tax = tax;
	}

	public void setTaxable(double taxable) {
		this.taxable = taxable;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public void setJurisType(String jurisType) {
		this.jurisType = jurisType;
	}

	public void setJurisName(String jurisName) {
		this.jurisName = jurisName;
	}

	public void setJurisCode(String jurisCode) {
		this.jurisCode = jurisCode;
	}

	public void setTaxName(String taxName) {
		this.taxName = taxName;
	}
}
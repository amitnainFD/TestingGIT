package com.freshdirect.fdstore.services.tax.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaxLine // Nested in GetTaxResult object
{
	@JsonProperty("LineNo")
	public String lineNo;

	@JsonProperty("TaxCode")
	public String taxCode;

	@JsonProperty("Taxability")
	public Boolean taxability;

	@JsonProperty("Taxable")
	public double taxable;

	@JsonProperty("Rate")
	public double rate;

	@JsonProperty("Tax")
	public double tax;

	@JsonProperty("Discount")
	public double discount;

	@JsonProperty("TaxCalculated")
	public double taxCalculated;

	@JsonProperty("Exemption")
	public double exemption;

	@JsonProperty("TaxDetails")
	public TaxDetail[] taxDetails;

	@JsonProperty("BoundaryLevel")
	public String boundaryLevel;

	public String getLineNo() {
		return lineNo;
	}

	public String getTaxCode() {
		return taxCode;
	}

	public Boolean getTaxability() {
		return taxability;
	}

	public double getTaxable() {
		return taxable;
	}

	public double getRate() {
		return rate;
	}

	public double getTax() {
		return tax;
	}

	public double getDiscount() {
		return discount;
	}

	public double getTaxCalculated() {
		return taxCalculated;
	}

	public double getExemption() {
		return exemption;
	}

	public TaxDetail[] getTaxDetails() {
		return taxDetails;
	}

	public String getBoundaryLevel() {
		return boundaryLevel;
	}

	public void setLineNo(String lineNo) {
		this.lineNo = lineNo;
	}

	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}

	public void setTaxability(Boolean taxability) {
		this.taxability = taxability;
	}

	public void setTaxable(double taxable) {
		this.taxable = taxable;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public void setTax(double tax) {
		this.tax = tax;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public void setTaxCalculated(double taxCalculated) {
		this.taxCalculated = taxCalculated;
	}

	public void setExemption(double exemption) {
		this.exemption = exemption;
	}

	public void setTaxDetails(TaxDetail[] taxDetails) {
		this.taxDetails = taxDetails;
	}

	public void setBoundaryLevel(String boundaryLevel) {
		this.boundaryLevel = boundaryLevel;
	}
}
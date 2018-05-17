package com.freshdirect.fdstore.services.tax.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaxOverrideDef
{
	@JsonProperty("TaxOverrideType")
	public String taxOverrideType; //Limited permitted values: TaxAmount, Exemption, TaxDate
	@JsonProperty("Reason")
	public String reason;
	@JsonProperty("TaxAmount")
	public String taxAmount; //If included, must be valid decimal
	@JsonProperty("TaxDate")
	public String taxDate; //If included, must be valid date 
	
	public String getTaxOverrideType() {
		return taxOverrideType;
	}
	
	public void setTaxOverrideType(String taxOverrideType) {
		this.taxOverrideType = taxOverrideType;
	}
	
	public String getReason() {
		return reason;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public String getTaxAmount() {
		return taxAmount;
	}
	
	public void setTaxAmount(String taxAmount) {
		this.taxAmount = taxAmount;
	}
	
	public String getTaxDate() {
		return taxDate;
	}
	
	public void setTaxDate(String taxDate) {
		this.taxDate = taxDate;
	}
}
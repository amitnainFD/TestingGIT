package com.freshdirect.fdstore.services.tax.data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetTaxResult {
    
	@JsonProperty("DocCode")
	public String docCode;
    
	@JsonProperty("DocDate")
    public Date docDate;
    
	@JsonProperty("Timestamp")
    public Date timestamp;
    
	@JsonProperty("TotalAmount")
    public double totalAmount;
    
	@JsonProperty("TotalDiscount")
    public double totalDiscount;
    
	@JsonProperty("TotalExemption")
    public double totalExemption;
    
	@JsonProperty("TotalTaxable")
    public double totalTaxable;
    
	@JsonProperty("TotalTax")
    public double totalTax;
    
	@JsonProperty("TotalTaxCalculated")
    public double totalTaxCalculated;
    
	@JsonProperty("TaxDate")
    public Date taxDate;
    
	@JsonProperty("TaxSummary")
    public TaxDetail[] taxSummary;
    
	@JsonProperty("TaxLines")
    public TaxLine[] taxLines;
    
	@JsonProperty("TaxDetails")
    public TaxDetail[] taxDetails;
    
	@JsonProperty("TaxAddresses")
    public TaxAddress[] taxAddresses;
    
	@JsonProperty("ResultCode")
    public CommonResponse.SeverityLevel resultCode;
    
	@JsonProperty("Messages")
    public CommonResponse.Message[] messages;
    
    public String getDocCode(){return docCode;}
    public Date getDocDate(){return docDate;}
    public Date getTimestamp(){return timestamp;}
    public double getTotalAmount(){return totalAmount;}
    public double getTotalDiscount(){return totalDiscount;}
    public double getTotalExemption(){return totalExemption;}
    public double getTotalTaxable(){return totalTaxable;}
    public double getTotalTax(){return totalTax;}
    public double getTotalTaxCalculated(){return totalTaxCalculated;}
    public Date getTaxDate(){return taxDate;}
    public TaxLine[] getTaxLines(){return taxLines;}
    public TaxDetail[] getTaxSummary(){return taxSummary;}
    public TaxDetail[] getTaxDetails(){return taxDetails;}
    public TaxAddress[] getTaxAddresses(){return taxAddresses;}
	public CommonResponse.SeverityLevel getResultCode(){return resultCode;}
	public CommonResponse.Message[] getMessages(){return messages;}
    
    public void setDocCode(String docCode){this.docCode = docCode;}
    public void setDocDate(Date docDate){this.docDate = docDate;}
    public void setTimestamp(Date timestamp){this.timestamp = timestamp;}
    public void setTotalAmount(double totalAmount){this.totalAmount = totalAmount;}
    public void setTotalDiscount(double totalDiscount){this.totalDiscount = totalDiscount;}
    public void setTotalExemption(double totalExemption){this.totalExemption = totalExemption;}
    public void setTotalTaxable(double totalTaxable){this.totalTaxable = totalTaxable;}
    public void setTotalTax(double totalTax){this.totalTax = totalTax;}
    public void setTotalTaxCalculated(double totalTaxCalculated){this.totalTaxCalculated = totalTaxCalculated;}
    public void setTaxDate(Date taxDate){this.taxDate = taxDate;}
    public void setTaxLines(TaxLine[] taxLines){this.taxLines = taxLines;}
    public void setTaxSummary(TaxDetail[] taxSummary){this.taxSummary = taxSummary;}
    public void setTaxDetails(TaxDetail[] taxDetails){this.taxDetails = taxDetails;}
    public void setTaxAddresses(TaxAddress[] taxAddresses){this.taxAddresses = taxAddresses;}
	public void setResultCode(CommonResponse.SeverityLevel resultCode){this.resultCode = resultCode;}
	public void setMessages(CommonResponse.Message[] messages){this.messages = messages;}
}
package com.freshdirect.fdstore.services.tax.data;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Line{
    
	@JsonProperty(required=true,value="LineNo")
	private String lineNo; //Required
	@JsonProperty(required=true,value="DestinationCode")
    private String destinationCode; //Required
	@JsonProperty(required=true,value="OriginCode")
    private String originCode; //Required
	@JsonProperty(required=true,value="ItemCode")
    private String itemCode; //Required
	@JsonProperty(required=true,value="Qty")
    private BigDecimal qty; //Required
	@JsonProperty(required=true,value="Amount")
    private BigDecimal amount; //Required
	@JsonProperty(required=false,value="TaxCode")
    private String taxCode; //Best practice
	@JsonProperty(required=false,value="CustomerUsageType")
	private String customerUsageType;
	@JsonProperty(required=false,value="Description")
    private String description; //Best Practice
	@JsonProperty(required=false,value="Discounted")
	private Boolean discounted;
	@JsonProperty(required=false,value="TaxIncluded")
    private Boolean taxIncluded;
	@JsonProperty(required=false,value="Ref1")
    private String ref1;
	@JsonProperty(required=false,value="Ref2")
    private String ref2;
    
    
    public String getLineNo() {
		return lineNo;
	}
	public void setLineNo(String lineNo) {
		this.lineNo = lineNo;
	}
	public String getDestinationCode() {
		return destinationCode;
	}
	public void setDestinationCode(String destinationCode) {
		this.destinationCode = destinationCode;
	}
	public String getOriginCode() {
		return originCode;
	}
	public void setOriginCode(String originCode) {
		this.originCode = originCode;
	}
	public String getItemCode() {
		return itemCode;
	}
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	public BigDecimal getQty() {
		return qty;
	}
	public void setQty(BigDecimal qty) {
		this.qty = qty;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getTaxCode() {
		return taxCode;
	}
	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}
	public String getCustomerUsageType() {
		return customerUsageType;
	}
	public void setCustomerUsageType(String customerUsageType) {
		this.customerUsageType = customerUsageType;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Boolean getDiscounted() {
		return discounted;
	}
	public void setDiscounted(Boolean discounted) {
		this.discounted = discounted;
	}
	public Boolean getTaxIncluded() {
		return taxIncluded;
	}
	public void setTaxIncluded(Boolean taxIncluded) {
		this.taxIncluded = taxIncluded;
	}
	public String getRef1() {
		return ref1;
	}
	public void setRef1(String ref1) {
		this.ref1 = ref1;
	}
	public String getRef2() {
		return ref2;
	}
	public void setRef2(String ref2) {
		this.ref2 = ref2;
	}
}
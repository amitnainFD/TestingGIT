package com.freshdirect.fdstore.services.tax.data;


import java.math.BigDecimal;
import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.freshdirect.fdstore.services.tax.data.DetailLevel;
import com.freshdirect.fdstore.services.tax.data.DocType;


public class GetTaxRequest {
	//Required for tax calculation
	@JsonProperty("DocDate")
	private Date docDate; //Must be valid YYYY-MM-DD format
	@JsonProperty("CustomerCode")
	private String customerCode;
	@JsonProperty("Addresses")
	private Address[] addresses;
	@JsonProperty("Lines")
	private Line[] lines;
    
	//Best Practice for tax calculation
    @JsonProperty("DocCode")
    private String docCode;
    @JsonProperty("DocType")
    private DocType docType;
    @JsonProperty("CompanyCode")
    private String companyCode;
    @JsonProperty("Commit")
    private Boolean commit;
    @JsonProperty("DetailLevel")
    private DetailLevel detailLevel;
    @JsonProperty("Client")
    private String client;
    //Use where appropriate to the situation
    @JsonProperty("CustomerUsageType")
    private String customerUsageType;
    @JsonProperty("ExemptionNo")
    private String exemptionNo;
    @JsonProperty("Discount")
    private BigDecimal discount;
    @JsonProperty("TaxOverride")
    private TaxOverrideDef taxOverride;
    @JsonProperty("BusinessIdentificationNo")
    private String businessIdentificationNo;
    
    //Optional
    @JsonProperty("PurchaseOrderNo")
    private String purchaseOrderNo;
    @JsonProperty("PaymentDate")
    private String paymentDate;
    @JsonProperty("ReferenceCode")
    private String referenceCode;								
    @JsonProperty("PosLaneCode")
    private String posLaneCode;
    @JsonProperty("CurrencyCode")
    private String currencyCode;								
 
    public enum SystemCustomerUsageType
    { 
        L,//"Other",
        A,//"Federal government",
        B,//"State government",
        C,//"Tribe / Status Indian / Indian Band",
        D,//"Foreign diplomat",
        E,//"Charitable or benevolent organization",
        F,//"Religious or educational organization",
        G,//"Resale",
        H,//"Commercial agricultural production",
        I,// "Industrial production / manufacturer",
        J,// "Direct pay permit",
        K,// "Direct Mail",
        N,// "Local Government",
        P,// "Commercial Aquaculture",
        Q,// "Commercial Fishery",
        R// "Non-resident"
    }

	public Date getDocDate() {
		return docDate;
	}

	public void setDocDate(Date docDate) {
		this.docDate = docDate;
	}

	public Address[] getAddresses() {
		return addresses;
	}

	public void setAddresses(Address[] addresses) {
		this.addresses = addresses;
	}

	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}
	
	public Line[] getLines() {
		return lines;
	}

	public void setLines(Line[] lines) {
		this.lines = lines;
	}

	public String getDocCode() {
		return docCode;
	}

	public void setDocCode(String docCode) {
		this.docCode = docCode;
	}

	public DocType getDocType() {
		return docType;
	}

	public void setDocType(DocType docType) {
		this.docType = docType;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public Boolean getCommit() {
		return commit;
	}

	public void setCommit(Boolean commit) {
		this.commit = commit;
	}

	public DetailLevel getDetailLevel() {
		return detailLevel;
	}

	public void setDetailLevel(DetailLevel detailLevel) {
		this.detailLevel = detailLevel;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getCustomerUsageType() {
		return customerUsageType;
	}

	public void setCustomerUsageType(String customerUsageType) {
		this.customerUsageType = customerUsageType;
	}

	public String getExemptionNo() {
		return exemptionNo;
	}

	public void setExemptionNo(String exemptionNo) {
		this.exemptionNo = exemptionNo;
	}

	public BigDecimal getDiscount() {
		return discount;
	}

	public void setDiscount(BigDecimal discount) {
		this.discount = discount;
	}

	public TaxOverrideDef getTaxOverride() {
		return taxOverride;
	}

	public void setTaxOverride(TaxOverrideDef taxOverride) {
		this.taxOverride = taxOverride;
	}

	public String getBusinessIdentificationNo() {
		return businessIdentificationNo;
	}

	public void setBusinessIdentificationNo(String businessIdentificationNo) {
		this.businessIdentificationNo = businessIdentificationNo;
	}

	public String getPurchaseOrderNo() {
		return purchaseOrderNo;
	}

	public void setPurchaseOrderNo(String purchaseOrderNo) {
		this.purchaseOrderNo = purchaseOrderNo;
	}

	public String getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
	}

	public String getReferenceCode() {
		return referenceCode;
	}

	public void setReferenceCode(String referenceCode) {
		this.referenceCode = referenceCode;
	}

	public String getPosLaneCode() {
		return posLaneCode;
	}

	public void setPosLaneCode(String posLaneCode) {
		this.posLaneCode = posLaneCode;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
}
package com.freshdirect.fdstore.services.tax.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.freshdirect.fdstore.services.tax.data.DocType;



public class CancelTaxRequest {
	public enum CancelCode { Unspecified, PostFailed, DocDeleted, DocVoided, AdjustmentCancelled; }

	@JsonProperty("CancelCode")
	private CancelCode CancelCode;
	//The document needs to be uniquely identified by DocCode/DocType/CompanyCode 
	
	@JsonProperty("DocType")
	private DocType docType; //Note that the only *meaningful* values for this property here are SalesInvoice, ReturnInvoice, PurchaseInvoice.
	
	@JsonProperty("CompanyCode")
	private String companyCode;
	
	@JsonProperty("DocCode")
	private String docCode;
	
	public CancelCode getCancelCode() {
		return CancelCode;
	}
	public void setCancelCode(CancelCode cancelCode) {
		CancelCode = cancelCode;
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
	public String getDocCode() {
		return docCode;
	}
	public void setDocCode(String docCode) {
		this.docCode = docCode;
	}
}
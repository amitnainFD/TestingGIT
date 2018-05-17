package com.freshdirect.fdstore.services.tax.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.freshdirect.fdstore.services.tax.data.CommonResponse.Message;
import com.freshdirect.fdstore.services.tax.data.CommonResponse.SeverityLevel;

public class CancelTaxResponse {
	
	@JsonProperty("CancelTaxResult")
	private CancelTaxResult cancelTaxResult;
	
	@JsonProperty("ResultCode")
	private SeverityLevel resultCode;
	
	@JsonProperty("Messages")
	private Message[] messages;
	
	public CancelTaxResult getCancelTaxResult() {
		return cancelTaxResult;
	}
	public void setCancelTaxResult(CancelTaxResult cancelTaxResult) {
		this.cancelTaxResult = cancelTaxResult;
	}
	public SeverityLevel getResultCode() {
		return resultCode;
	}
	public void setResultCode(SeverityLevel resultCode) {
		this.resultCode = resultCode;
	}
	public Message[] getMessages() {
		return messages;
	}
	public void setMessages(Message[] messages) {
		this.messages = messages;
	}
}
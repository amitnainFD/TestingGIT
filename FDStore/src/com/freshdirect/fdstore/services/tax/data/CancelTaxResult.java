package com.freshdirect.fdstore.services.tax.data;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.freshdirect.fdstore.services.tax.data.CommonResponse.Message;
import com.freshdirect.fdstore.services.tax.data.CommonResponse.SeverityLevel;


public class CancelTaxResult
{
	@JsonProperty("ResultCode")
	public SeverityLevel resultCode;
	
	@JsonProperty("TransactionId")
	public String transactionId;
	
	@JsonProperty("DocId")
	public String docId;
	
	@JsonProperty("Messages")
	public Message[] messages;
	
	public SeverityLevel getResultCode(){return resultCode;}
	public String getTransactionId(){return transactionId;}
	public String getDocId(){return docId;}
	public Message[] getMessages(){return messages;}
	
	public void setResultCode(SeverityLevel resultCode){this.resultCode = resultCode;}
	public void setTransactionId(String transactionId){this.transactionId = transactionId;}
	public void setDocId(String docId){this.docId = docId;}
	public void setMessages(Message[] messages){this.messages = messages;}
	
}

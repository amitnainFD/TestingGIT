package com.freshdirect.fdstore.temails;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.mail.EnumEmailType;
import com.freshdirect.mail.EnumTEmailProviderType;
import com.freshdirect.mail.EnumTranEmailType;

public class TEmailTemplateInfo extends ModelSupport{

	private EnumTEmailProviderType provider;
	private String templateId;
	private EnumTranEmailType transactionType;
	private EnumEmailType emailType;
	private String description;
	private boolean isActive;
    private String fromAddress;
    private String subject;
	private String targetProgId;
	private boolean productionReady;	
	
	public boolean isProductionReady() {
		return productionReady;
	}


	public void setProductionReady(boolean productionReady) {
		this.productionReady = productionReady;
	}
    
	public TEmailTemplateInfo(){}

	public String getTargetProgId() {
		return targetProgId;
	}


	public void setTargetProgId(String targetProgId) {
		this.targetProgId = targetProgId;
	}
	
	public EnumTEmailProviderType getProvider() {
		return provider;
	}

	public void setProvider(EnumTEmailProviderType provider) {
		this.provider = provider;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public EnumTranEmailType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(EnumTranEmailType transactionType) {
		this.transactionType = transactionType;
	}

	

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public EnumEmailType getEmailType() {
		return emailType;
	}

	public void setEmailType(EnumEmailType emailType) {
		this.emailType = emailType;
	}


	@Override
	public String toString() {
		return "TEmailTemplateInfo [description=" + description
				+ ", emailType=" + emailType + ", fromAddress=" + fromAddress
				+ ", isActive=" + isActive + ", productionReady="
				+ productionReady + ", provider=" + provider + ", subject="
				+ subject + ", targetProgId=" + targetProgId + ", templateId="
				+ templateId + ", transactionType=" + transactionType + "]";
	}
	
	
	
}

package com.freshdirect.fdstore.temails;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.framework.mail.EmailAddress;
import com.freshdirect.framework.mail.EmailSupport;
import com.freshdirect.framework.mail.TEmailI;
import com.freshdirect.mail.EnumEmailType;
import com.freshdirect.mail.EnumTEmailProviderType;
import com.freshdirect.mail.EnumTEmailStatus;
import com.freshdirect.mail.EnumTranEmailType;

public class TransEmailInfoModel extends EmailSupport implements TEmailI {

	private String customerId;
	private String orderId;
	private String emailContent;
	private EnumTEmailStatus emailStatus;
	private EnumTranEmailType emailTransactionType;
	private String templateId;
	private String id; 
	private EnumTEmailProviderType provider;
	private EnumEmailType emailType;
	private Date croModDate;
	private String targetProgId;
	private boolean productionReady;	
	private String oasQueryString;
	
	public void setOasQueryString(String val) {
		this.oasQueryString = val;
	}
	
	public String getOasQueryString() {
		return oasQueryString;
	}
	
	public boolean isProductionReady() {
		return productionReady;
	}


	public void setProductionReady(boolean productionReady) {
		this.productionReady = productionReady;
	}
	
	
	public String getTargetProgId() {
		return targetProgId;
	}


	public void setTargetProgId(String targetProgId) {
		this.targetProgId = targetProgId;
	}


	public TransEmailInfoModel(String customerId,String orderId,String emailContent,String templateId,EnumTEmailStatus status,EnumTranEmailType type,EnumEmailType emailType) {
		super();
		this.customerId = customerId;
		this.orderId=orderId;
		this.emailContent=emailContent;
		this.templateId=templateId;
		this.emailStatus=status;
		this.emailTransactionType=type;
		this.emailType=emailType;
	}
	
	
	public TransEmailInfoModel(){
		this.emailStatus=EnumTEmailStatus.NEW;
	}
	
	
	public TransEmailInfoModel(TEmailTemplateInfo info, Map input,String emailContent){
		FDCustomerInfo custInfo=(FDCustomerInfo)input.get(TEmailConstants.CUSTOMER_ID_INP_KEY);;
		if(custInfo!=null)   this.orderId=custInfo.getLastOrderId();
		FDOrderI order=(FDOrderI)input.get(TEmailConstants.ORDER_INP_KEY);
		if(order!=null) this.customerId=order.getCustomerId();
		this.templateId=info.getTemplateId();
		this.emailTransactionType=info.getTransactionType();
		this.emailContent=emailContent;
		this.emailType=info.getEmailType();
		this.provider=info.getProvider();
		super.setFromAddress(new EmailAddress(TEmailConstants.GENERAL_LABEL,info.getFromAddress()));
		super.setRecipient(custInfo.getEmailAddress());		
		List list=(List)input.get(TEmailConstants.BCC_INP_KEY);
		if(list!=null) super.setBCCList(list);
		
		List ccList=(List)input.get(TEmailConstants.CC_INP_KEY);
		if(ccList!=null) super.setCCList(ccList);

		String subject=(String)input.get(TEmailConstants.SUBJECT_KEY);
		if(subject!=null) super.setSubject(subject);
		else super.setSubject(info.getSubject());
		this.emailStatus=EnumTEmailStatus.NEW;
		
	}
	
	
	
	@Override
	public String getCustomerId() {
		// TODO Auto-generated method stub
		return this.customerId;
	}

	@Override
	public String getEmailContent() {
		// TODO Auto-generated method stub
		return this.emailContent;
	}

	

	@Override
	public String getEmailStatus() {
		// TODO Auto-generated method stub
		return this.emailStatus.getName();
	}

	@Override
	public String getEmailTransactionType() {
		// TODO Auto-generated method stub
		return this.emailTransactionType.getName();
	}

	@Override
	public String getOrderId() {
		// TODO Auto-generated method stub
		return this.orderId;
	}

	@Override
	public String getTemplateId() {
		// TODO Auto-generated method stub
		return this.templateId;
	}

	public String getId() {
		return id;
	}
	
	
	public String getCCListInStr(){
		if(super.getCCList()==null) return null;
		StringBuffer ccList = new StringBuffer(128);
		Iterator it = super.getCCList().iterator();
		if (it.hasNext())
			ccList.append(it.next());
		while (it.hasNext()) {
			ccList.append(',');
			ccList.append(it.next());
		}
		return ccList.toString();
	}
	
	
	public String getBCCListInStr(){
		if(super.getBCCList()==null) return null;
		StringBuffer ccList = new StringBuffer(128);
		Iterator it = super.getBCCList().iterator();
		if (it.hasNext())
			ccList.append(it.next());
		while (it.hasNext()) {
			ccList.append(',');
			ccList.append(it.next());
		}
		return ccList.toString();		
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProvider() {
		return provider.getName();
	}

	public void setProvider(EnumTEmailProviderType provider) {
		this.provider = provider;
	}

	@Override
	public String getEmailType() {
		// TODO Auto-generated method stub
		return emailType.getName();
	}


	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}


	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}


	public void setEmailContent(String emailContent) {
		this.emailContent = emailContent;
	}


	public void setEmailStatus(EnumTEmailStatus emailStatus) {
		this.emailStatus = emailStatus;
	}


	public void setEmailTransactionType(EnumTranEmailType emailTransactionType) {
		this.emailTransactionType = emailTransactionType;
	}


	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}


	public void setEmailType(EnumEmailType emailType) {
		this.emailType = emailType;
	}
	
	public void setCCListInStr(String contents){
		if(contents==null  || contents.trim().length()==0) return;
		List ccList=new ArrayList();
		StringTokenizer tokens=new StringTokenizer(contents,",");
		while(tokens.hasMoreElements()){
			String line=tokens.nextToken();
			ccList.add(line);
		}
		if(ccList.size()>0) super.setCCList(ccList);
		
	}
	
	public void setBCCListInStr(String contents){
		if(contents==null  || contents.trim().length()==0) return;
		List ccList=new ArrayList();
		StringTokenizer tokens=new StringTokenizer(contents,",");
		while(tokens.hasMoreElements()){
			String line=tokens.nextToken();
			ccList.add(line);
		}
		if(ccList.size()>0) super.setBCCList(ccList);
		
	}


	public Date getCroModDate() {
		return croModDate;
	}


	public void setCroModDate(Date croModDate) {
		this.croModDate = croModDate;
	}


	@Override
	public String toString() {
		return "TransEmailInfoModel [croModDate=" + croModDate
				+ ", customerId=" + customerId + ", emailContent="
				+ emailContent + ", emailStatus=" + emailStatus
				+ ", emailTransactionType=" + emailTransactionType
				+ ", emailType=" + emailType + ", id=" + id + ", orderId="
				+ orderId + ", productionReady=" + productionReady
				+ ", provider=" + provider + ", targetProgId=" + targetProgId
				+ ", templateId=" + templateId + "]";
	}
	

}

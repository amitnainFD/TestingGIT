package com.freshdirect.fdstore.temails;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.freshdirect.customer.ErpComplaintModel;
import com.freshdirect.fdstore.content.Recipe;
import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.mail.TellAFriend;
import com.freshdirect.giftcard.ErpGCDlvInformationHolder;
import com.freshdirect.mail.GiftCardOrderInfo;

public interface TEmailContextI extends Serializable {

	public GiftCardOrderInfo getGcOrderInfo(); 
	
	public String templateId();
	
	public String getProvider();
		
	
	public FDOrderI getOrder();
	
	public FDCustomerInfo getCustomer(); 
	
	public String encryptMailContent(String content);
	
	public String decryptMailContent(String encyContent);
	
	
	public String getOrderNumber();
	
	public Date getDeliveryStartTime();
	
	public Date getDeliveryEndTime();
		
	public ErpComplaintModel getComplaint();
	
	public String getPasswdLink();
	
	public Date getPasswdExpDateTime();

	public Date getDeliveryCutoffTime();

	public boolean isDeliveryPass();

	public TEmailRecipe getRecipe();

	public boolean isPreview();
	
	public TellAFriend getTellAFriend();
	
	public boolean isBulkGiftCardOrder();
		
	public String getOrderType(); 
	
	public ErpGCDlvInformationHolder getGcDeliveryInfo();

	public String getRecipentName();
	
	public RHOrderInfo getRhOrderInfo();

	public List getSmartEmailInfoList();	
	
}

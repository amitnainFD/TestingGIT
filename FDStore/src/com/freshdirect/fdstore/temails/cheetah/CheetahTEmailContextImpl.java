package com.freshdirect.fdstore.temails.cheetah;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.freshdirect.customer.EnumSaleType;
import com.freshdirect.customer.ErpComplaintModel;
import com.freshdirect.fdstore.content.Recipe;
import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.mail.TellAFriend;
import com.freshdirect.fdstore.mail.TellAFriendRecipe;
import com.freshdirect.fdstore.temails.RHOrderInfo;
import com.freshdirect.fdstore.temails.TEmailContextI;
import com.freshdirect.fdstore.temails.TEmailRecipe;
import com.freshdirect.giftcard.ErpGCDlvInformationHolder;
import com.freshdirect.giftcard.ErpGiftCardUtil;
import com.freshdirect.mail.GiftCardOrderInfo;

public class CheetahTEmailContextImpl implements TEmailContextI {

	public static final String PROVIDER_NAME="CHEETAH";
	
	
	private String templateId; 
	private FDCustomerInfo customer;
	private FDOrderI order;
	private String orderNumber;
	private Date deliveryStartTime;
	private Date deliveryEndTime;
	private ErpComplaintModel complaint;
	private String passwdLink;
	private Date passwdExpDateTime;
	private Date deliveryCutoffTime;
	private boolean isDeliveryPass;
	private TEmailRecipe  recipe;
	private TellAFriend tellAFriend;
	private boolean isPreview;
	private boolean isBulkGiftCardOrder;
	private EnumSaleType orderType;
	private ErpGCDlvInformationHolder gcDeliveryInfo;
	private String recipentName;
	private RHOrderInfo rhOrderInfo;
	private GiftCardOrderInfo gcOrderInfo;
	private List smartEmailInfoList;
	
	public List getSmartEmailInfoList() {
		return smartEmailInfoList;
	}


	public void setSmartEmailInfoList(List getSmartEmailInfoList) {
		this.smartEmailInfoList = getSmartEmailInfoList;
	}


	
	
	public GiftCardOrderInfo getGcOrderInfo() {
		return gcOrderInfo;
	}


	public void setGcOrderInfo(GiftCardOrderInfo gcOrderInfo) {
		this.gcOrderInfo = gcOrderInfo;
	}


	public RHOrderInfo getRhOrderInfo() {
		return rhOrderInfo;
	}


	public void setRhOrderInfo(RHOrderInfo rhOrderInfo) {
		this.rhOrderInfo = rhOrderInfo;
	}
	
	
	public String getRecipentName() {
		return recipentName;
	}


	public void setRecipentName(String recipentName) {
		this.recipentName = recipentName;
	}


	public ErpGCDlvInformationHolder getGcDeliveryInfo() {
		return gcDeliveryInfo;
	}


	public void setGcDeliveryInfo(ErpGCDlvInformationHolder gcDeliveryInfo) {
		this.gcDeliveryInfo = gcDeliveryInfo;
	}


	public String getOrderType() {
		if(orderType==null) return EnumSaleType.REGULAR.getName();
		return orderType.getName();
	}


	public void setOrderType(EnumSaleType orderType) {
		this.orderType = orderType;
	}


	public boolean isBulkGiftCardOrder() {
		return isBulkGiftCardOrder;
	}


	public void setBulkGiftCardOrder(boolean isBulkGiftCardOrder) {
		this.isBulkGiftCardOrder = isBulkGiftCardOrder;
	}


	
	
	public TellAFriend getTellAFriend() {
		return tellAFriend;
	}


	public void setTellAFriend(TellAFriend tellAFriend) {
		this.tellAFriend = tellAFriend;
	}



	
	public boolean isPreview() {
		return isPreview;
	}


	public void setPreview(boolean isPreview) {
		this.isPreview = isPreview;
	}


	public CheetahTEmailContextImpl(){
		
	}
	
	
	public String getProvider() {
		// TODO Auto-generated method stub
		return PROVIDER_NAME;
	}

	

	@Override
	public FDCustomerInfo getCustomer() {
		// TODO Auto-generated method stub
		return this.customer;
	}

	@Override
	public FDOrderI getOrder() {
		// TODO Auto-generated method stub
		return this.order;
	}

	@Override
	public String templateId() {
		// TODO Auto-generated method stub
		return this.templateId;
	}


	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}


	public void setCustomer(FDCustomerInfo customer) {
		this.customer = customer;
	}


	public void setOrder(FDOrderI order) {
		this.order = order;
	}


	@Override
	public String decryptMailContent(String encyContent) {
		// TODO Auto-generated method stub
		return ErpGiftCardUtil.decryptGivexNum(encyContent);
	}


	
	public String encryptMailContent(String content) {
		// TODO Auto-generated method stub
		return ErpGiftCardUtil.encryptGivexNum(content);
	}

	private static final String TIME_FORMAT="hh:mm aaa";
	
	public String getTime(Date date) {
		// TODO Auto-generated method stub
		DateFormat format=new SimpleDateFormat(TIME_FORMAT);
		String text=format.format(date);
		return text;
	}
	
	private static final String DATE_FORMAT="EEEEEEE, MMM d";
	public String getDate(Date date) {
		// TODO Auto-generated method stub
		DateFormat format=new SimpleDateFormat(DATE_FORMAT);
		String text=format.format(date);
		return text;
	}

	
	public String getOrderNumber() {
		return orderNumber;
	}


	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}


	public Date getDeliveryStartTime() {
		return deliveryStartTime;
	}


	public void setDeliveryStartTime(Date deliveryStartTime) {
		this.deliveryStartTime = deliveryStartTime;
	}


	public Date getDeliveryEndTime() {
		return deliveryEndTime;
	}


	public void setDeliveryEndTime(Date deliveryEndTime) {
		this.deliveryEndTime = deliveryEndTime;
	}


	public ErpComplaintModel getComplaint() {
		return complaint;
	}


	public void setComplaint(ErpComplaintModel complaint) {
		this.complaint = complaint;
	}


	public String getPasswdLink() {
		return passwdLink;
	}


	public void setPasswdLink(String passwdLink) {
		this.passwdLink = passwdLink;
	}


	public Date getPasswdExpDateTime() {
		return passwdExpDateTime;
	}


	public void setPasswdExpDateTime(Date passwdExpDateTime) {
		this.passwdExpDateTime = passwdExpDateTime;
	}


	public Date getDeliveryCutoffTime() {
		return deliveryCutoffTime;
	}


	public void setDeliveryCutoffTime(Date deliveryCutoffTime) {
		this.deliveryCutoffTime = deliveryCutoffTime;
	}


	public boolean isDeliveryPass() {
		return isDeliveryPass;
	}


	public void setDeliveryPass(boolean isDeliveryPass) {
		this.isDeliveryPass = isDeliveryPass;
	}


	public TEmailRecipe getRecipe() {
		return recipe;
	}


	public void setRecipe(TEmailRecipe recipe) {
		this.recipe = recipe;
	}
		

}

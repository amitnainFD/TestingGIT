package com.freshdirect.fdstore.customer.ejb;

import java.util.Date;

import com.freshdirect.common.address.PhoneNumber;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.customer.FDCustomerModel;
import com.freshdirect.framework.core.ModelI;
import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.sms.EnumSMSAlertStatus;

/**
 * 
 * @author ksriram
 * Customer's store specific default delivery address, payment method and depot location info.
 */
public class FDCustomerEStoreModel extends ModelSupport{

	private static final long serialVersionUID = -8890913479519201820L;
	
	private String fdCustomerPk;
	private String defaultShipToAddressPK;
	private String defaultPaymentMethodPK;
	private String defaultDepotLocationPK;
	private EnumEStoreId eStoreId;
	private String orderNotices;
	private String orderExceptions;
	private String offers;
	private String partnerMessages;
	private String smsPreferenceflag;
	private Date smsOptinDate;
	private PhoneNumber mobileNumber;
	private String fdxOrderNotices;
	private String fdxOrderExceptions;
	private String fdxOffers;
	private String fdxPartnerMessages;
	private String fdxSmsPreferenceflag;
	private Date fdxSmsOptinDate;
	private PhoneNumber fdxMobileNumber;
	private Boolean deliveryNotification;
	private Boolean fdxdeliveryNotification;
	private Boolean offersNotification;
	private Boolean fdxOffersNotification;
	private String crmStore;
	private Boolean emailOptIn = false;
	private Boolean fdxEmailOptIn = false;
	private Boolean tcAcknowledge;

	
	

	public String getCrmStore() {
		return crmStore;
	}

	public void setCrmStore(String crmStore) {
		this.crmStore = crmStore;
	}

	public Boolean getFdxOffersNotification() {
		return fdxOffersNotification;
	}

	public void setFdxOffersNotification(Boolean fdxOffersNotification) {
		this.fdxOffersNotification = fdxOffersNotification;
	}

	public Boolean getFdxdeliveryNotification() {
		return fdxdeliveryNotification;
	}

	public void setFdxdeliveryNotification(Boolean fdxdeliveryNotification) {
		this.fdxdeliveryNotification = fdxdeliveryNotification;
	}

	public Boolean getDeliveryNotification() {
		return deliveryNotification;
	}

	public void setDeliveryNotification(Boolean deliveryNotification) {
		this.deliveryNotification = deliveryNotification;
	}

	public Boolean getOffersNotification() {
		return offersNotification;
	}

	public void setOffersNotification(Boolean offersNotification) {
		this.offersNotification = offersNotification;
	}

	

	
	
	public PhoneNumber getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(PhoneNumber mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	
	public PhoneNumber getFdxMobileNumber() {
		return fdxMobileNumber;
	}

	public void setFdxMobileNumber(PhoneNumber fdxMobileNumber) {
		this.fdxMobileNumber = fdxMobileNumber;
	}


	public String getFdxOrderNotices() {
		return fdxOrderNotices;
	}

	public void setFdxOrderNotices(String fdxOrderNotices) {
		this.fdxOrderNotices = fdxOrderNotices;
	}

	public String getFdxOrderExceptions() {
		return fdxOrderExceptions;
	}

	public void setFdxOrderExceptions(String fdxOrderExceptions) {
		this.fdxOrderExceptions = fdxOrderExceptions;
	}

	public String getFdxOffers() {
		return fdxOffers;
	}

	public void setFdxOffers(String fdxOffers) {
		this.fdxOffers = fdxOffers;
	}

	public String getFdxPartnerMessages() {
		return fdxPartnerMessages;
	}

	public void setFdxPartnerMessages(String fdxPartnerMessages) {
		this.fdxPartnerMessages = fdxPartnerMessages;
	}

	public String getFdxSmsPreferenceflag() {
		return fdxSmsPreferenceflag;
	}

	public void setFdxSmsPreferenceflag(String fdxSmsPreferenceflag) {
		this.fdxSmsPreferenceflag = fdxSmsPreferenceflag;
	}

	public Date getFdxSmsOptinDate() {
		return fdxSmsOptinDate;
	}

	public void setFdxSmsOptinDate(Date fdxSmsOptinDate) {
		this.fdxSmsOptinDate = fdxSmsOptinDate;
	}

	public String getOrderNotices() {
		return orderNotices;
	}

	public void setOrderNotices(String orderNotices) {
		this.orderNotices = orderNotices;
	}

	public String getOrderExceptions() {
		return orderExceptions;
	}

	public void setOrderExceptions(String orderExceptions) {
		this.orderExceptions = orderExceptions;
	}

	public String getOffers() {
		return offers;
	}

	public void setOffers(String offers) {
		this.offers = offers;
	}

	public String getPartnerMessages() {
		return partnerMessages;
	}

	public void setPartnerMessages(String partnerMessages) {
		this.partnerMessages = partnerMessages;
	}

	public String getSmsPreferenceflag() {
		return smsPreferenceflag;
	}

	public void setSmsPreferenceflag(String smsPreferenceflag) {
		this.smsPreferenceflag = smsPreferenceflag;
	}

	public Date getSmsOptinDate() {
		return smsOptinDate;
	}

	public void setSmsOptinDate(Date smsOptinDate) {
		this.smsOptinDate = smsOptinDate;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	/**
	 * @return the fdCustomerPk
	 */
	public String getFdCustomerPk() {
		return fdCustomerPk;
	}
	/**
	 * @param fdCustomerPk the fdCustomerPk to set
	 */
	public void setFdCustomerPk(String fdCustomerPk) {
		this.fdCustomerPk = fdCustomerPk;
	}
	/**
	 * @return the defaultShipToAddressPK
	 */
	public String getDefaultShipToAddressPK() {
		return defaultShipToAddressPK;
	}
	/**
	 * @param defaultShipToAddressPK the defaultShipToAddressPK to set
	 */
	public void setDefaultShipToAddressPK(String defaultShipToAddressPK) {
		this.defaultShipToAddressPK = defaultShipToAddressPK;
	}
	/**
	 * @return the defaultPaymentMethodPK
	 */
	public String getDefaultPaymentMethodPK() {
		return defaultPaymentMethodPK;
	}
	/**
	 * @param defaultPaymentMethodPK the defaultPaymentMethodPK to set
	 */
	public void setDefaultPaymentMethodPK(String defaultPaymentMethodPK) {
		this.defaultPaymentMethodPK = defaultPaymentMethodPK;
	}
	/**
	 * @return the defaultDepotLocationPK
	 */
	public String getDefaultDepotLocationPK() {
		return defaultDepotLocationPK;
	}
	/**
	 * @param defaultDepotLocationPK the defaultDepotLocationPK to set
	 */
	public void setDefaultDepotLocationPK(String defaultDepotLocationPK) {
		this.defaultDepotLocationPK = defaultDepotLocationPK;
	}
	/**
	 * @return the eStoreId
	 */
	public EnumEStoreId geteStoreId() {
		return eStoreId;
	}
	/**
	 * @param eStoreId the eStoreId to set
	 */
	public void seteStoreId(EnumEStoreId eStoreId) {
		this.eStoreId = eStoreId;
	}

	public Boolean getTcAcknowledge() {
		return tcAcknowledge;
	}

	public void setTcAcknowledge(Boolean tcAcknowledge) {
		this.tcAcknowledge = tcAcknowledge;
	}


	public Boolean getEmailOptIn() {
		return emailOptIn;
	}

	public void setEmailOptIn(Boolean emailOptIn) {
		this.emailOptIn = emailOptIn;
	}

	public Boolean getFdxEmailOptIn() {
		return fdxEmailOptIn;
	}

	public void setFdxEmailOptIn(Boolean fdxEmailOptIn) {
		this.fdxEmailOptIn = fdxEmailOptIn;
	}

	
	

}

package com.freshdirect.fdstore.mail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;
import org.dom4j.Document;

import com.freshdirect.ErpServicesProperties;
import com.freshdirect.common.address.PhoneNumber;
import com.freshdirect.customer.EnumDeliveryType;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpComplaintModel;
import com.freshdirect.customer.ErpCustomerEmailModel;
import com.freshdirect.customer.ErpCustomerInfoModel;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.BookRetailer;
import com.freshdirect.fdstore.content.Image;
import com.freshdirect.fdstore.content.MediaI;
import com.freshdirect.fdstore.content.Recipe;
import com.freshdirect.fdstore.content.RecipeSource;
import com.freshdirect.fdstore.customer.FDCSContactHours;
import com.freshdirect.fdstore.customer.FDCSContactHoursUtil;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.customer.FDOrderInfoI;
import com.freshdirect.fdstore.customer.adapter.FDOrderAdapter;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;
import com.freshdirect.framework.mail.EmailAddress;
import com.freshdirect.framework.mail.EmailSupport;
import com.freshdirect.framework.mail.XMLEmailI;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDEmailFactory {
	
	private static FDEmailFactory _sharedInstance; // singleton holder
	
	private static final Category LOGGER = LoggerFactory.getInstance(FDEmailFactory.class);
	
	public static final SimpleDateFormat df = new SimpleDateFormat("EEEE, MMM d yyyy");
	public static final SimpleDateFormat DT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	public static DateFormat serverTimeFormat = new SimpleDateFormat("hh:mm aaa");

	public static final String GENERAL_LABEL = "FreshDirect";
	public static final String GENERAL_CS_EMAIL = FDStoreProperties.getCustomerServiceEmail();
	public static final String PRODUCT_EMAIL = FDStoreProperties.getProductEmail();
	public static final String FEEDBACK_EMAIL = FDStoreProperties.getFeedbackEmail();
	public static final String CHEFSTABLE_EMAIL = FDStoreProperties.getChefsTableEmail();
	public static final String VENDING_EMAIL = FDStoreProperties.getVendingEmail();
	public static final String CRM_SECURITY_CC_EMAIL = FDStoreProperties.getCrmCCSecurityEmail();
	
	public static final String FDX_GENERAL_LABEL = "FoodKick";
	public static final String FDX_ANNOUNCE_EMAIL = FDStoreProperties.getAnnounceEmailFDX();
	public static final String FDX_ORDER_EMAIL = FDStoreProperties.getOrderEmailFDX();
	public static final String FDX_ACTSERVICE_EMAIL = FDStoreProperties.getActServiceEmailFDX();
	public static final String FDX_SIDEKICKS_EMAIL = FDStoreProperties.getSidekicksEmailFDX();


	// default instance getter
	public static FDEmailFactory getInstance() {
		if (_sharedInstance == null) {
			_sharedInstance = new FDEmailFactory();
		}
		return _sharedInstance;
	}


	public XMLEmailI createFinalAmountEmail(FDCustomerInfo customer, FDOrderAdapter order) {
		boolean isFdxOrder = order.getEStoreId().equals(EnumEStoreId.FDX);
		FDTransactionalEmail email = null;

		if (isFdxOrder) {
			email = new FDTransactionalEmail(customer, order, order.getEStoreId());
			
			email.setXslPath("h_final_amount_confirm_fdx.xsl", "h_final_amount_confirm_fdx.xsl"); //no text version
			email.setFromEmail(FDX_ORDER_EMAIL); //add to email's data for footer text
			email.setFromAddress(new EmailAddress(FDX_GENERAL_LABEL, FDX_ORDER_EMAIL));
			email.setSubject("Order Up! We're Coming At Ya");
		} else {
			email = new FDTransactionalEmail(customer, order);
			email.setXslPath("h_final_amount_confirm_v2.xsl", "x_final_amount_confirm_v2.xsl");
			
			email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
			
			if(order.getShortedItems().size() > 0) {
				if(order.getShortedItems().size() == 1)
					email.setSubject("Service Alert: Your Order is Missing 1 Item");
				else
					email.setSubject("Service Alert: Your Order is Missing " + order.getShortedItems().size() + " Items");
			} else if (EnumDeliveryType.PICKUP.equals(order.getDeliveryType())) {
				email.setSubject("Your order for " + df.format(order.getRequestedDate()) + " is being prepared for pick-up.");
			} else if(order.getDeliveryReservation() != null && order.getDeliveryReservation().getDeliveryETA() != null
					&& order.getDeliveryReservation().getDeliveryETA().isEmailETAenabled() && order.getDeliveryReservation().getDeliveryETA().getStartTime() != null
					&& order.getDeliveryReservation().getDeliveryETA().getEndTime() != null) {
				email.setSubject("Your order ETA is between " + serverTimeFormat.format(order.getDeliveryReservation().getDeliveryETA().getStartTime()) +"  to "
						+ serverTimeFormat.format(order.getDeliveryReservation().getDeliveryETA().getEndTime()));
			} else {
				email.setSubject("Your order for " + df.format(order.getRequestedDate()) + " is on its way");
			}
		}

		return email;
	}

	public XMLEmailI createConfirmOrderEmail(FDCustomerInfo customer, FDOrderI order) {
		boolean isFdxOrder = order.getEStoreId().equals(EnumEStoreId.FDX);
		FDTransactionalEmail email = null;

		if (isFdxOrder) {
			email = new FDTransactionalEmail(customer, order, order.getEStoreId());
	
			email.setXslPath("h_order_confirm_fdx.xsl", "h_order_confirm_fdx.xsl"); //no text version
			email.setFromEmail(FDX_ORDER_EMAIL); //add to email's data for footer text
			email.setFromAddress(new EmailAddress(FDX_GENERAL_LABEL, FDX_ORDER_EMAIL));
			email.setSubject("We Got Your Order. Get Excited!");
		} else {
			email = new FDTransactionalEmail(customer, order);
	
			email.setXslPath("h_order_confirm_v1.xsl", "x_order_confirm_v1.xsl");
			email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
			email.setSubject("Your order for " + df.format(order.getRequestedDate()));
		}
		
		return email;
	}

	public XMLEmailI createModifyOrderEmail(FDCustomerInfo customer, FDOrderI order) {
		boolean isFdxOrder = order.getEStoreId().equals(EnumEStoreId.FDX);
		FDTransactionalEmail email = null;

		if (isFdxOrder) {
			email = new FDTransactionalEmail(customer, order, EnumEStoreId.FDX);
			
			email.setXslPath("h_order_change_fdx.xsl", "h_order_change_fdx.xsl"); //no text version
			email.setFromEmail(FDX_ORDER_EMAIL); //add to email's data for footer text
			email.setFromAddress(new EmailAddress(FDX_GENERAL_LABEL, FDX_ORDER_EMAIL));
			email.setSubject("We Got Your Changes");
		} else {
			email = new FDTransactionalEmail(customer, order);
			
			email.setXslPath("h_order_change_v1.xsl", "x_order_change_v1.xsl");
			email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
			email.setSubject("Your order information has been updated");
		}

		return email;
	}

	public XMLEmailI createChargeOrderEmail(FDCustomerInfo customer, FDOrderI order, double additionalCharge) {
		FDTransactionalEmail email = new FDTransactionalEmail(customer, order);

		email.setXslPath("h_order_charge_v1.xsl", "x_order_charge_v1.xsl");
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		String subject = "Your order has been charged";
		if (order.getPaymentMethod() != null) {
			subject += " to your " + order.getPaymentMethod().getPaymentMethodType().getDescription().toLowerCase(); 
		}		
		email.setSubject(subject);

		return email;
	}

	public XMLEmailI createCancelOrderEmail(FDCustomerInfo customer, String orderNumber, Date startTime, Date endTime, EnumEStoreId eStoreId) {
		boolean isFdxOrder = eStoreId.equals(EnumEStoreId.FDX);
		FDCancelOrderConfirmEmail email = null;
		
		if (isFdxOrder) {
			email = new FDCancelOrderConfirmEmail(customer, orderNumber, startTime, endTime);
			
			email.setXslPath("h_order_cancel_fdx.xsl", "h_order_cancel_fdx.xsl"); //no text version
			email.setFromEmail(FDX_ORDER_EMAIL); //add to email's data for footer text
			email.setFromAddress(new EmailAddress(FDX_GENERAL_LABEL, FDX_ORDER_EMAIL));
			email.setSubject("Order "+orderNumber+": Officially Canceled");
		} else {
			email = new FDCancelOrderConfirmEmail(customer, orderNumber, startTime, endTime);

			email.setXslPath("h_order_cancel_v1.xsl", "x_order_cancel_v1.xsl");
			email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
			email.setSubject("Cancellation receipt");
		}
		return email;
	}
	
	public XMLEmailI createOrderIvrContactEmail(FDCustomerInfo customer, String orderNumber, Collection<String> ccLst, Collection<String> bccLst) {
		FDOrderDeliveryIVRContactEmail email = new FDOrderDeliveryIVRContactEmail(customer, orderNumber);

		email.setXslPath("h_delivery_ivrcontact_v1.xsl", "x_delivery_ivrcontact_v1.xsl");
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		
		if(ccLst != null && ccLst.size() > 0) {
			email.setCCList(ccLst);
		}
		if(bccLst != null && bccLst.size() > 0) {
			email.setBCCList(bccLst);
		}
		email.setSubject("FreshDirect is trying to reach you");
		return email;
	}

	public XMLEmailI createConfirmCreditEmail(FDCustomerInfo customer, String saleId, ErpComplaintModel complaint, EnumEStoreId eStoreId) {
		FDConfirmCreditEmail email = null;
		boolean isFdxOrder = eStoreId.equals(EnumEStoreId.FDX);

		if (isFdxOrder) {
			email = new FDConfirmCreditEmail(customer, saleId, complaint, FDCSContactHoursUtil.getFDXCSHours());
			
			email.setXslPath("h_credit_general_fdx.xsl", "h_credit_general_fdx.xsl"); //no text version
			email.setFromAddress(new EmailAddress(FDX_GENERAL_LABEL, FDX_ACTSERVICE_EMAIL));
			email.setFromEmail(FDX_SIDEKICKS_EMAIL); //add to email's data for footer text
			email.setSubject("Store Credit Issued");
			
		} else {

			email = new FDConfirmCreditEmail(customer, saleId, complaint, FDCSContactHoursUtil.getFDCSHours());
			//email.setXslPath("h_credit_confirm_v1.xsl", "x_credit_confirm_v1.xsl");
			
			// get the xslpath from the email object in the complaint.
			ErpCustomerEmailModel custEmailObj = complaint.getCustomerEmail();
			email.setXslPath(custEmailObj.getHtmlXslPath(),custEmailObj.getPlainTextXslPath());
			email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
			email.setSubject("We've issued your credits");
		}

		return email;
	}

	public String getForgotPasswordLink(FDCustomerInfo customer, String requestId, EnumEStoreId eStoreId) {
		String passwordLink = ErpServicesProperties.getForgotPasswordPage(); //default FD
		
		if (eStoreId.equals(EnumEStoreId.FDX)) {
			passwordLink = ErpServicesProperties.getForgotPasswordPageFDX(); //change to FDX
		}
		
		passwordLink += ((passwordLink.indexOf('?') == -1) ? "?" : "&") + "email=" + customer.getEmailAddress() + "&link=" + requestId; 
		
		return passwordLink;
	}
	
	public XMLEmailI createForgotPasswordEmail(FDCustomerInfo customer, String requestId, Date expiration, List ccList, EnumEStoreId eStoreId) {
		LOGGER.debug("createForgotPasswordEmail() EStoreID:"+eStoreId);
		boolean isFdxOrder = eStoreId.equals(EnumEStoreId.FDX);
		FDForgotPasswordEmail email = null;
		String passwordLink = getForgotPasswordLink(customer, requestId, eStoreId);
		
		if (isFdxOrder) {
			LOGGER.debug("createForgotPasswordEmail() EStoreID (in isFdxOrder):"+eStoreId);
			
			email = new FDForgotPasswordEmail(customer, passwordLink, expiration);

			email.setXslPath("h_password_link_fdx.xsl", "h_password_link_fdx.xsl"); //no text version
			email.setFromEmail(FDX_ACTSERVICE_EMAIL); //add to email's data for footer text
			email.setFromAddress(new EmailAddress(FDX_GENERAL_LABEL, FDX_ACTSERVICE_EMAIL));
			email.setCCList(ccList);
			email.setSubject("Forgetting something?");
			
		} else {
			email = new FDForgotPasswordEmail(customer, passwordLink, expiration);

			email.setXslPath("h_password_link_v1.xsl", "x_password_link_v1.xsl");
			email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
			email.setCCList(ccList);
			email.setSubject("Important message from FreshDirect");
		}

		return email;
	}

	public XMLEmailI createConfirmSignupEmail(FDCustomerInfo customer, EnumEStoreId eStoreId) {
		LOGGER.debug("createConfirmSignupEmail() EStoreID:"+eStoreId);
		boolean isFdxOrder = eStoreId.equals(EnumEStoreId.FDX);
		FDInfoEmail email = null;
		
		if (isFdxOrder) {
			LOGGER.debug("createConfirmSignupEmail() EStoreID (in isFdxOrder):"+eStoreId);
			email = new FDInfoEmail(customer);
			
			email.setXslPath("h_signup_confirm_fdx.xsl", "h_signup_confirm_fdx.xsl"); //no text version
			email.setSubject("Congrats! You're In!");
			email.setFromEmail(FDX_ANNOUNCE_EMAIL); //add to email's data for footer text
			email.setFromAddress(new EmailAddress(FDX_GENERAL_LABEL, FDX_ANNOUNCE_EMAIL));
		} else {
			email = new FDInfoEmail(customer);
			/*if (customer.isPickupOnly()) {
				email.setXslPath("h_pickup_signup_confirm.xsl", "x_pickup_signup_confirm.xsl");
			} else if (customer.isCorporateUser()) {
				email.setXslPath("h_signup_confirm_corp.xsl", "x_signup_confirm_corp.xsl");
			} else {
				email.setXslPath("h_signup_confirm_v2.xsl", "x_signup_confirm_v2.xsl");
			}*/
			
			if (customer.isCorporateUser()) {
				email.setXslPath("h_signup_confirm_corp.xsl", "x_signup_confirm_corp.xsl");
				email.setSubject("Welcome to FreshDirect At The Office");
			} else {
				email.setXslPath("h_signup_confirm_v2.xsl", "x_signup_confirm_v2.xsl");
				email.setSubject("Welcome to FreshDirect");
			}
			
			email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		}
		
		return email;
	}
	
	public XMLEmailI createAuthorizationFailedEmail(FDCustomerInfo customer, String orderNumber, Date startTime, Date endTime, Date cutoffTime, int authFailType){
		
		FDAuthorizationFailedEmail email = new FDAuthorizationFailedEmail(customer, orderNumber, startTime, endTime, cutoffTime,FDCSContactHoursUtil.getFDCSHours());
		email.setXslPath("h_authorization_failure_V"+authFailType+".xsl", "x_authorization_failure_V"+authFailType+".xsl");
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		email.setSubject("Credit Card Authorization Failure");
		

		return email; 
	}
	/*
	 * Added for APPDEV-89 . Sending a seperate Auth failed email to auto renew DP customers.
	 * AR - Stands for Auto Renew DP
	 */
	public XMLEmailI createARAuthorizationFailedEmail(FDCustomerInfo customer, String orderNumber, Date startTime, Date endTime, Date cutoffTime){
		FDAuthorizationFailedEmail email = new FDAuthorizationFailedEmail(customer, orderNumber, startTime, endTime, cutoffTime,FDCSContactHoursUtil.getFDCSHours());
		email.setXslPath("h_ar_authorization_failure.xsl", "x_ar_authorization_failure.xsl");
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		email.setSubject("Credit Card Authorization Failure");
		

		return email; 
	}
	public XMLEmailI createReminderEmail(FDCustomerInfo customer, boolean sendToAltEmail) {
		FDInfoEmail email = new FDInfoEmail(customer);
		email.setXslPath("h_reminder_service.xsl", "x_reminder_service.xsl");
		if(sendToAltEmail){
			List<String> cc = new ArrayList<String>();
			cc.add(customer.getAltEmailAddress());
			email.setCCList(cc);
		}
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		email.setSubject("A friendly reminder from FreshDirect.");
		return email;
	}

	public XMLEmailI createGenericEmail(FDCustomerInfo customer, String subject, Document body) {
		FDGenericEmail email = new FDGenericEmail(customer, body);

		email.setXslPath("h_generic.xsl", "x_generic.xsl");
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		email.setSubject(subject);

		return email;
	}

	public XMLEmailI createRecipeEmail(FDCustomerInfo customer, Recipe recipe) {
		FDRecipeEmail email = new FDRecipeEmail(customer, recipe);

		email.setXslPath("h_recipe.xsl", "x_recipe.xsl");
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		email.setSubject("FreshDirect - Recipe for " + recipe.getName());

		return email;
	}

	public XMLEmailI createTellAFriendEmail(TellAFriend mailInfo, boolean preview) {
		FDTellAFriendEmail email = new FDTellAFriendEmail(mailInfo, preview);
		email.setXslPath(mailInfo.getXsltPath());

		return email;
	}

	public XMLEmailI createProductRequestEmail(FDCustomerInfo customerInfo, String subject, String body) {
		FDProductRequestEmail email = new FDProductRequestEmail(body);

		if (customerInfo == null) {
			email.setFromAddress(new EmailAddress("Unidentified Customer", GENERAL_CS_EMAIL));
		} else {
			email.setFromAddress(
				new EmailAddress(customerInfo.getFirstName() + " " + customerInfo.getLastName(), customerInfo.getEmailAddress()));
		}

		email.setSubject(subject);

		return email;
	}

	public XMLEmailI createChefsTableEmail(FDCustomerInfo customerInfo, String subject, String body) {
		ChefsTableEmail email = new ChefsTableEmail(body);

		if (customerInfo == null) {
			email.setFromAddress(new EmailAddress("Unidentified Customer", GENERAL_CS_EMAIL));
		} else {
			email.setFromAddress(
				new EmailAddress(customerInfo.getFirstName() + " " + customerInfo.getLastName(), customerInfo.getEmailAddress()));
		}

		email.setSubject(subject);

		return email;
	}

	public XMLEmailI createContactServiceEmail(FDCustomerInfo customerInfo, String subject, String body) {
		FDContactServiceEmail email = new FDContactServiceEmail(body);

		if (customerInfo == null) {
			email.setFromAddress(new EmailAddress("Unidentified Customer", GENERAL_CS_EMAIL));
		} else {
			email.setFromAddress(
				new EmailAddress(customerInfo.getFirstName() + " " + customerInfo.getLastName(), customerInfo.getEmailAddress()));
		}

		email.setSubject(subject);

		return email;
	}
	
	public XMLEmailI createVendingEmail(FDCustomerInfo customerInfo, String subject, String body) {
		VendingEmail email = new VendingEmail(body);

		if (customerInfo == null) {
			email.setFromAddress(new EmailAddress("Unidentified Customer", GENERAL_CS_EMAIL));
		} else {
			email.setFromAddress(
				new EmailAddress(customerInfo.getFirstName() + " " + customerInfo.getLastName(), customerInfo.getEmailAddress()));
		}

		email.setSubject(subject);

		return email;
	}
	
	public XMLEmailI createCorporateServiceInterestEmail(ErpCustomerInfoModel erpCustomerInfo, String subject, String body) {
		FDContactServiceEmail email = new CorporateServiceEmail(body);

		if (erpCustomerInfo == null) {
			email.setFromAddress(new EmailAddress("Unidentified Customer", GENERAL_CS_EMAIL));
		} else {
			email.setFromAddress(
				new EmailAddress(erpCustomerInfo.getFirstName() + " " + erpCustomerInfo.getLastName(), erpCustomerInfo.getEmail()));
		}

		email.setSubject(subject);

		return email;
	}
	
	public XMLEmailI createCateringEmail(ErpCustomerInfoModel erpCustomerInfo, String subject, String body) {
		FDContactServiceEmail email = new CateringEmail(body);

		if (erpCustomerInfo == null) {
			email.setFromAddress(new EmailAddress("Unidentified Customer", GENERAL_CS_EMAIL));
		} else {
			email.setFromAddress(
				new EmailAddress(erpCustomerInfo.getFirstName() + " " + erpCustomerInfo.getLastName(), erpCustomerInfo.getEmail()));
		}

		email.setSubject(subject);

		return email;
	}
	
	public XMLEmailI createFeedbackEmail(FDCustomerInfo customerInfo, String subject, String body) {
		FeedbackEmail email = new FeedbackEmail(body);
			
		if (customerInfo == null) {
			email.setFromAddress(new EmailAddress("Unidentified Customer", GENERAL_CS_EMAIL));
		} else {
			email.setFromAddress(
				new EmailAddress(customerInfo.getFirstName() + " " + customerInfo.getLastName(), customerInfo.getEmailAddress()));
		}
	
		email.setSubject(subject);

			return email;
		}
	
	public XMLEmailI createCrmCCSecurityEmail(CrmSecurityCCCheckEmailVO emailVO) {
		CrmSecurityCCCheckEmail email = new CrmSecurityCCCheckEmail(emailVO);
		email.setFromAddress(new EmailAddress("CRM Security", GENERAL_CS_EMAIL));		
		email.setSubject(FDStoreProperties.getCrmCCSecurityEmailSubject());

		return email;
	}
	
	public XMLEmailI createReferAFriendInvitationEmail(String name, String userMessage, String systemMessage, String legal, String refLink, String offerText) {
		FDReferAFriendInvEmail email = new FDReferAFriendInvEmail(name, userMessage, systemMessage, legal, refLink, offerText);
		return email;
	}
	
	public XMLEmailI createReferAFriendCreditEmail(String referralName, String message) {
		FDReferAFriendCreditEmail email = new FDReferAFriendCreditEmail(referralName, message);
		return email;
	}
	
	////////

	protected String getFromAddress(String depotCode) {
		if (depotCode == null || "".equals(depotCode)) {
			return GENERAL_CS_EMAIL;
		}
		try {
			return FDDeliveryManager.getInstance().getCustomerServiceEmail(depotCode);
		} catch (FDResourceException re) {
			LOGGER.warn("Could not get the correct email", re);
			return GENERAL_CS_EMAIL;
		}
	}

	private static class FDGenericEmail extends FDInfoEmail {
		private final Document body;

		public FDGenericEmail(FDCustomerInfo customer, Document body) {
			super(customer);
			this.body = body;
		}

		/**
		 * @see com.freshdirect.fdstore.mail.FDInfoEmail#decorateMap(java.util.Map)
		 */
		protected void decorateMap(Map map) {
			super.decorateMap(map);
			map.put("body", this.body);
		}

	}

	private static class FDRecipeEmail extends FDInfoEmail {
		private final Recipe   recipe;

		public FDRecipeEmail(FDCustomerInfo customer, Recipe recipe) {
			super(customer);
			this.recipe = recipe;
		}

		/**
		 *  Load all details of the recipe, and put it into the supplied map.
		 *  
		 *  @param map the map to fill with the details of the recipe.
		 *  @see com.freshdirect.fdstore.mail.FDInfoEmail#decorateMap(java.util.Map)
		 */
		protected void decorateMap(Map map) {
			super.decorateMap(map);
			
			MediaI			media;
			
			map.put("recipeId", recipe.getContentName());
			map.put("recipeName", recipe.getName());
			map.put("trackingCode", "eau");
			
			// TODO pass "efr" for send-recipe-to-friend
			map.put("trackingCode", "eau");
			
			if (recipe.getSource() != null) {
				RecipeSource		source      = recipe.getSource();
				List 			retailers   = source.getBookRetailers();
				Set				retailerSet = new HashSet();
				
				map.put("sourceId", source.getContentKey().getId());
				map.put("sourceName", source.getName());
				map.put("sourceIsbn", source.getIsbn());
				
				Image photo = source.getZoomImage();
				if (photo != null) {
					map.put("photoUrl", photo.getPath());
					map.put("photoWidth", Integer.toString(photo.getWidth()));
					map.put("photoHeight", Integer.toString(photo.getHeight()));
				}
				
				for (Iterator it = retailers.iterator(); it.hasNext();) {
					BookRetailer retailer    = (BookRetailer) it.next();
					Map			 retailerMap = new HashMap();
					
					retailerMap.put("id", retailer.getContentKey().getId());
					retailerMap.put("name", retailer.getName());
					Image logo = retailer.getLogo();
					if (logo != null) {
						retailerMap.put("logo", logo.getPath());
						retailerMap.put("logoWidth", Integer.toString(logo.getWidth()));
						retailerMap.put("logoHeight", Integer.toString(logo.getHeight()));
					}
					retailerSet.add(retailerMap);
				}
				
				if (!retailerSet.isEmpty()) {
					map.put("retailer", retailerSet);
				}
			}
			
			map.put("authorNames", recipe.getAuthorNames());
			map.put("recipeDescription", loadMedia(recipe.getDescription()));
			
			
			media = recipe.getIngredientsMedia();
			if (media != null) {
				map.put("ingredientsMedia", loadMedia(media));
			}
			
			media = recipe.getPreparationMedia();
			if (media != null) {
				map.put("preparationMedia", loadMedia(media));
			}
			
			media = recipe.getCopyrightMedia();
			if (media != null) {
				map.put("copyrightMedia", loadMedia(media));
			}
			
		}

		/**
		 *  Resolve a path.
		 *  
		 *  @param rootPath the base path
		 *  @param childPath the path to resolve, relative to rootPath
		 *  @return a full URL to childPath, in relation to rootPath
		 */
		public static URL resolve(String rootPath, String childPath) throws IOException {
			URL url = new URL(rootPath);
			if (childPath.startsWith("/")) {
				childPath = childPath.substring(1, childPath.length());
			}
			url = new URL(url, childPath);

			if (!url.toString().startsWith(rootPath)) {
				throw new IOException("Child path not under root");
			}

			return url;
		}
		
		public boolean isHtmlEmail() {
			return true;
		}
		
		/**
		 *  Load media contents.
		 *  
		 *  @param media the media to load
		 *  @return the contents of the media, as a string,
		 *          or an empty string on errors
		 */
		private String loadMedia(MediaI media) {
			if (media == null) {
				return "";
			}
			
			InputStream     in  = null;
			StringBuffer		out = new StringBuffer(); 
			try {
	
				URL url = resolve(FDStoreProperties.getMediaPath(), media.getPath());
				in = url.openStream();
				if (in == null) {
					return "";
				}
	
				byte[] buf = new byte[4096];
				int i;
				while ((i = in.read(buf)) != -1) {
					out.append(new String(buf, 0, i));
				}
	
				return out.toString();
	
			} catch (FileNotFoundException e) {
				LOGGER.warn("Media file not found " + media.getPath());
				return "";
	
			} catch (IOException e) {
				LOGGER.warn("Failed to load resource", e);
	
				return "";
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException ex) {
				}
			}
		}
		
	}

	private static class FDForgotPasswordEmail extends FDInfoEmail {

		private String passwordLink;
		private Date expirationTime;

		public FDForgotPasswordEmail(FDCustomerInfo customer, String passwordLink, Date expirationTime) {
			super(customer);
			this.passwordLink = passwordLink;
			this.expirationTime = expirationTime;
		}

		/**
		 * @see com.freshdirect.fdstore.mail.FDInfoEmail#decorateMap(java.util.Map)
		 */
		protected void decorateMap(Map map) {
			super.decorateMap(map);
			map.put("passwordLink", this.passwordLink);
			map.put("expirationTime", DT_FORMATTER.format(this.expirationTime));
		}

	}

	private static class FDCancelOrderConfirmEmail extends FDInfoEmail {

		private String orderNumber;
		private Date deliveryStartTime;
		private Date deliveryEndTime;

		public FDCancelOrderConfirmEmail(FDCustomerInfo customer, String orderNumber, Date startTime, Date endTime) {
			super(customer);
			this.orderNumber = orderNumber;
			this.deliveryStartTime = startTime;
			this.deliveryEndTime = endTime;
		}

		/**
		 * @see com.freshdirect.fdstore.mail.FDInfoEmail#decorateMap(java.util.Map)
		 */
		protected void decorateMap(Map map) {
			super.decorateMap(map);
			map.put("orderNumber", this.orderNumber);
			map.put("deliveryStartTime", DT_FORMATTER.format(this.deliveryStartTime));
			map.put("deliveryEndTime", DT_FORMATTER.format(this.deliveryEndTime));
		}
	}
	
	private static class FDOrderDeliveryIVRContactEmail extends FDInfoEmail {

		private String orderNumber;
		
		public FDOrderDeliveryIVRContactEmail(FDCustomerInfo customer, String orderNumber) {
			super(customer);
			this.orderNumber = orderNumber;
		}

		/**
		 * @see com.freshdirect.fdstore.mail.FDInfoEmail#decorateMap(java.util.Map)
		 */
		protected void decorateMap(Map map) {
			super.decorateMap(map);
			map.put("orderNumber", this.orderNumber);
		}

	}
	
	static class FDAuthorizationFailedEmail extends FDInfoEmail {

		private String orderNumber;
		private Date deliveryStartTime;
		private Date deliveryEndTime;
		private Date cutoffTime;
		private List<FDCSContactHours> contactHours;

		public FDAuthorizationFailedEmail(FDCustomerInfo customer, String orderNumber, Date startTime, Date endTime, Date cutoffTime) {
			super(customer);
			this.orderNumber = orderNumber;
			this.deliveryStartTime = startTime;
			this.deliveryEndTime = endTime;
			this.cutoffTime = cutoffTime;
		}

		public FDAuthorizationFailedEmail(FDCustomerInfo customer, String orderNumber, Date startTime, Date endTime, Date cutoffTime,List<FDCSContactHours> contactHours) {
			super(customer);
			this.orderNumber = orderNumber;
			this.deliveryStartTime = startTime;
			this.deliveryEndTime = endTime;
			this.cutoffTime = cutoffTime;
			this.contactHours = contactHours;
		}
		
		/**
		 * @see com.freshdirect.fdstore.mail.FDInfoEmail#decorateMap(java.util.Map)
		 */
		protected void decorateMap(Map map) {
			super.decorateMap(map);
			map.put("orderNumber", this.orderNumber);
			map.put("deliveryStartTime", DT_FORMATTER.format(this.deliveryStartTime));
			map.put("deliveryEndTime", DT_FORMATTER.format(this.deliveryEndTime));
			map.put("cutoffTime", DT_FORMATTER.format(this.cutoffTime));
			map.put("contactHours", this.contactHours);
		}

	}

	protected static class FDConfirmCreditEmail extends FDInfoEmail {
		private ErpComplaintModel complaint;
		private String saleId;
		private List<FDCSContactHours> contactHours;

		public FDConfirmCreditEmail(FDCustomerInfo customer, String saleId, ErpComplaintModel complaint) {
			super(customer);
			this.saleId = saleId;
			this.complaint = complaint;
			this.getBCCList().addAll(FDStoreProperties.getIssueCreditBccAddresses());
		}
		
		public FDConfirmCreditEmail(FDCustomerInfo customer, String saleId, ErpComplaintModel complaint, List<FDCSContactHours> contactHours) {
			super(customer);
			this.saleId = saleId;
			this.complaint = complaint;
			this.getBCCList().addAll(FDStoreProperties.getIssueCreditBccAddresses());
			this.contactHours = contactHours;
		}

		protected void decorateMap(Map map) {
			super.decorateMap(map);
			map.put("complaint", this.complaint);
			map.put("saleId", this.saleId);
			map.put("contactHours", this.contactHours);
		}
	}
	
	private static class FDTellAFriendEmail extends EmailSupport implements XMLEmailI {

		private String xslPath;
		private TellAFriend mailInfo;
		private boolean preview;

		public FDTellAFriendEmail(TellAFriend mailInfo, boolean preview) {
			this.setFromAddress(
				new EmailAddress(
					mailInfo.getCustomerFirstName() + " " + mailInfo.getCustomerLastName(),
					mailInfo.getCustomerEmail()));

			this.setRecipient(mailInfo.getFriendEmail());
			this.setSubject(getSubject(mailInfo));

			this.mailInfo = mailInfo;
			this.preview = preview;
		}

		protected void decorateMap(Map map) {

			map.put("preview", Boolean.valueOf(this.preview));
			map.put("mailInfo", this.mailInfo);
			map.put("productEmail", Boolean.valueOf(mailInfo instanceof TellAFriendProduct));
			map.put("tellAFriendEmail", Boolean.TRUE);
			
			if (mailInfo instanceof TellAFriendRecipe) {
				TellAFriendRecipe	tafr   = (TellAFriendRecipe) mailInfo;
				Recipe              recipe = tafr.getRecipe();
				
				// this is the same code as in FDRecipeEmail
				// TODO: somehow refactor to avoid code duplication
				MediaI			media;
				
				map.put("recipeId", recipe.getContentName());
				map.put("recipeName", recipe.getName());
				map.put("trackingCode", "efr");
				
				if (recipe.getSource() != null) {
					RecipeSource		source      = recipe.getSource();
					List 			retailers   = source.getBookRetailers();
					Set				retailerSet = new HashSet();
					
					map.put("sourceId", source.getContentKey().getId());
					map.put("sourceName", source.getName());
					map.put("sourceIsbn", source.getIsbn());
					
					Image photo = source.getZoomImage();
					if (photo != null) {
						map.put("photoUrl", photo.getPath());
						map.put("photoWidth", Integer.toString(photo.getWidth()));
						map.put("photoHeight", Integer.toString(photo.getHeight()));
					}
					
					for (Iterator it = retailers.iterator(); it.hasNext();) {
						BookRetailer retailer    = (BookRetailer) it.next();
						Map			 retailerMap = new HashMap();
						
						retailerMap.put("id", retailer.getContentKey().getId());
						retailerMap.put("name", retailer.getName());
						Image logo = retailer.getLogo();
						if (logo != null) {
							retailerMap.put("logo", logo.getPath());
							retailerMap.put("logoWidth", Integer.toString(logo.getWidth()));
							retailerMap.put("logoHeight", Integer.toString(logo.getHeight()));
						}
						retailerSet.add(retailerMap);
					}
					
					if (!retailerSet.isEmpty()) {
						map.put("retailer", retailerSet);
					}
				}
				
				map.put("authorNames", recipe.getAuthorNames());
				map.put("recipeDescription", loadMedia(recipe.getDescription()));
				
				
				media = recipe.getIngredientsMedia();
				if (media != null) {
					map.put("ingredientsMedia", loadMedia(media));
				}
				
				media = recipe.getPreparationMedia();
				if (media != null) {
					map.put("preparationMedia", loadMedia(media));
				}
				
				media = recipe.getCopyrightMedia();
				if (media != null) {
					map.put("copyrightMedia", loadMedia(media));
				}				
			}
		}
		
		/**
		 *  Resolve a path.
		 *  
		 *  @param rootPath the base path
		 *  @param childPath the path to resolve, relative to rootPath
		 *  @return a full URL to childPath, in relation to rootPath
		 */
		public static URL resolve(String rootPath, String childPath) throws IOException {
			// this is the same code as in FDRecipeEmail
			// TODO: somehow refactor to avoid code duplication
			
			URL url = new URL(rootPath);
			if (childPath.startsWith("/")) {
				childPath = childPath.substring(1, childPath.length());
			}
			url = new URL(url, childPath);

			if (!url.toString().startsWith(rootPath)) {
				throw new IOException("Child path not under root");
			}

			return url;
		}
				
		/**
		 *  Load media contents.
		 *  
		 *  @param media the media to load
		 *  @return the contents of the media, as a string,
		 *          or an empty string on errors
		 */
		private String loadMedia(MediaI media) {
			// this is the same code as in FDRecipeEmail
			// TODO: somehow refactor to avoid code duplication
			
			if (media == null) {
				return "";
			}
			
			InputStream     in  = null;
			StringBuffer		out = new StringBuffer(); 
			try {
	
				URL url = resolve(FDStoreProperties.getMediaPath(), media.getPath());
				in = url.openStream();
				if (in == null) {
					return "";
				}
	
				byte[] buf = new byte[4096];
				int i;
				while ((i = in.read(buf)) != -1) {
					out.append(new String(buf, 0, i));
				}
	
				return out.toString();
	
			} catch (FileNotFoundException e) {
				LOGGER.warn("Media file not found " + media.getPath());
				return "";
	
			} catch (IOException e) {
				LOGGER.warn("Failed to load resource", e);
	
				return "";
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException ex) {
				}
			}
		}

		public final String getXML() {
			FDXMLSerializer s = new FDXMLSerializer();
			Map map = new HashMap();
			this.decorateMap(map);
			return s.serialize("fdemail", map);
		}

		public String getXslPath() {
			return this.xslPath;
		}

		public void setXslPath(String xslPath) {
			this.xslPath = ErpServicesProperties.getMailerXslHome() + xslPath;
		}

		public boolean isHtmlEmail() {
			return true;
		}

		private static String getSubject(TellAFriend mailInfo) {
			String subject = "";
			if (mailInfo instanceof TellAFriendProduct) {
				TellAFriendProduct tafp = (TellAFriendProduct) mailInfo;
				subject = "Your friend " + tafp.getCustomerFirstName() + " has sent you " + tafp.getProductTitle() + "!";
			} else if (mailInfo instanceof TellAFriendRecipe) {
				TellAFriendRecipe	tafr   = (TellAFriendRecipe) mailInfo;
				Recipe              recipe = tafr.getRecipe();
				subject = "Your friend " + mailInfo.getCustomerFirstName() + " has sent you " + recipe.getName();
			} else {
				subject = "Your friend " + mailInfo.getCustomerFirstName() + " would like you to try FreshDirect";
			}
			return subject;
		}

	}

	private static class FDContactServiceEmail extends EmailSupport implements XMLEmailI {

		private final String body;

		public FDContactServiceEmail(String body) {
			this.setRecipient(GENERAL_CS_EMAIL);
			this.body = body;
		}

		public String getXslPath() {
			return ErpServicesProperties.getMailerXslHome() + "x_contact_service.xsl";
		}

		public boolean isHtmlEmail() {
			return false;
		}

		public String getXML() {
			FDXMLSerializer s = new FDXMLSerializer();
			Map map = new HashMap();
			map.put("body", this.body);
			return s.serialize("fdemail", map);
		}

	}

	private static class FDProductRequestEmail extends FDContactServiceEmail implements XMLEmailI {

		public FDProductRequestEmail(String body) {
			super(body);
			this.setRecipient(PRODUCT_EMAIL);
		}

	}
	
	private static class CorporateServiceEmail extends FDContactServiceEmail implements XMLEmailI {

		public CorporateServiceEmail(String body) {
			super(body);
			this.setRecipient("corporateservices@freshdirect.com");
		}

	}
	
	private static class CateringEmail extends FDContactServiceEmail implements XMLEmailI {

		public CateringEmail(String body) {
			super(body);
			this.setRecipient("catering@freshdirect.com");
		}

	}
	
	private static class FeedbackEmail extends FDContactServiceEmail implements XMLEmailI {

		public FeedbackEmail(String body) {
			super(body);
			this.setRecipient(FEEDBACK_EMAIL);
		}

	}
	private static class ChefsTableEmail extends FDContactServiceEmail implements XMLEmailI {

		public ChefsTableEmail(String body) {
			super(body);
			this.setRecipient(CHEFSTABLE_EMAIL);
		}

	}	
	
	private static class VendingEmail extends FDContactServiceEmail implements XMLEmailI {

		public VendingEmail(String body) {
			super(body);
			this.setRecipient(VENDING_EMAIL);
		}

	}
	
	protected static class CrmSecurityCCCheckEmail  extends FDContactServiceEmail implements XMLEmailI{
		private CrmSecurityCCCheckEmailVO emailVO;		

		public CrmSecurityCCCheckEmail(CrmSecurityCCCheckEmailVO emailVO) {
			super("");
			this.emailVO=emailVO;
			this.setRecipient(CRM_SECURITY_CC_EMAIL);
		}
		
		public String getXslPath() {
			return ErpServicesProperties.getMailerXslHome() + "x_cc_check_security_v1.xsl";
		}
		
		public String getXML() {
			FDXMLSerializer s = new FDXMLSerializer();
			Map map = new HashMap();
			map.put("emailvo", this.emailVO);
			return s.serialize("fdemail", map);
		}
	}
	
	private static class FDDPCreditEmail extends FDInfoEmail {
		
		private final String saleId;
		private final int creditCount;
		private final String dpName;

		public FDDPCreditEmail(FDCustomerInfo customer,String saleId,int creditCount, String dpName) {
			
			super(customer);
			this.saleId=saleId;
			this.creditCount=creditCount;
			this.dpName=dpName;
		}

		/**
		 * @see com.freshdirect.fdstore.mail.FDInfoEmail#decorateMap(java.util.Map)
		 */
		protected void decorateMap(Map map) {
			super.decorateMap(map);
			map.put("saleId", this.saleId);
			map.put("creditCount", String.valueOf(this.creditCount));
			map.put("dpName", this.dpName);
		}

	}
	
	
	public XMLEmailI createDPCreditEmail(FDCustomerInfo customer, String saleId, int creditCount, String dpName) {
		FDDPCreditEmail email = new FDDPCreditEmail(customer, saleId, creditCount,dpName);

		email.setXslPath("h_dp_credits_v1.xsl", "x_dp_credits_v1.xsl");
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		email.setSubject("We've credited your DeliveryPass.");
		return email;
	}

	public XMLEmailI createStandingOrderErrorEmail(FDCustomerInfo customer, FDStandingOrder standingOrder) {
		FDStandingOrderErrorEmail email = new FDStandingOrderErrorEmail(customer, standingOrder);

		email.setXslPath("h_standing_order_error_v1.xsl", "x_standing_order_error_v1.xsl");

		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));

		email.setSubject("A problem with your standing order for " + df.format(standingOrder.getNextDeliveryDate()));

		return email;
	}

	public XMLEmailI createConfirmStandingOrderEmail(FDCustomerInfo customer, FDOrderI order, FDStandingOrder standingOrder, List<FDCartLineI> unavCartItems) {
		FDStandingOrderEmail email = new FDStandingOrderEmail(customer, order, standingOrder, unavCartItems);

		email.setXslPath("h_standing_order_confirm_v1.xsl", "x_standing_order_confirm_v1.xsl");

		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));

		StringBuilder subject = new StringBuilder("Your standing order for ");
		subject.append(df.format(order.getRequestedDate()));
		if (unavCartItems.size() > 0)
			subject.append(" (some items unavailable)");
		email.setSubject(subject.toString());

		return email;
	}

	public XMLEmailI createConfirmDeliveryStandingOrderEmail(FDCustomerInfo customer, FDOrderI order, FDStandingOrder standingOrder, List<FDCartLineI> unavCartItems) {
		FDStandingOrderEmail email = new FDStandingOrderEmail(customer, order, standingOrder, unavCartItems);

		email.setXslPath("h_standing_order_delivery_v1.xsl", "x_standing_order_delivery_v1.xsl");

		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));

		email.setSubject("Reminder, your standing order for " + df.format(order.getRequestedDate()));

		return email;
	}
	public XMLEmailI createSettlementFailedEmail(FDCustomerInfo customer, String orderNumber, Date startTime, Date endTime, Date cutoffTime, EnumEStoreId eStoreId){
		boolean isFdxOrder = eStoreId.equals(EnumEStoreId.FDX);
		FDSettlementFailedEmail email = null;
		
		if (isFdxOrder) {
			email = new FDSettlementFailedEmail(customer, orderNumber, startTime, endTime, cutoffTime, FDCSContactHoursUtil.getFDXCSHours());

			email.setXslPath("h_settlement_failure_fdx.xsl", "h_settlement_failure_fdx.xsl"); //no text version
			email.setFromAddress(new EmailAddress(FDX_GENERAL_LABEL, FDX_ACTSERVICE_EMAIL));
			email.setFromEmail(FDX_ACTSERVICE_EMAIL); //add to email's data for footer text
			email.setSubject("We Were Unable to Process Your Payment");
		} else {
			email = new FDSettlementFailedEmail(customer, orderNumber, startTime, endTime, cutoffTime, FDCSContactHoursUtil.getFDCSHours());

			email.setXslPath("h_settlement_failure.xsl", "x_settlement_failure.xsl");
			email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
			email.setSubject("e-Check Settlement Failure");
		}
		
		return email; 
	}
	
	public XMLEmailI createDuplicateSOInstanceEmail(FDCustomerInfo customer, FDStandingOrder sOrder, List<FDOrderInfoI> orders) {
		
		FDStandingOrderInstancesEmail email=new FDStandingOrderInstancesEmail(customer, sOrder, orders);
		
		email.setXslPath("h_standing_order_instances.xsl", "x_standing_order_instances.xsl");
		
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		
		email.setSubject("Multiple deliveries for your standing order");
		
		return email;
	}
	
	static class FDSettlementFailedEmail extends FDInfoEmail {

		private String orderNumber;
		private Date deliveryStartTime;
		private Date deliveryEndTime;
		private Date cutoffTime;
		private List<FDCSContactHours> contactHours;

		public FDSettlementFailedEmail(FDCustomerInfo customer, String orderNumber, Date startTime, Date endTime, Date cutoffTime) {
			super(customer);
			this.orderNumber = orderNumber;
			this.deliveryStartTime = startTime;
			this.deliveryEndTime = endTime;
			this.cutoffTime = cutoffTime;
		}

		public FDSettlementFailedEmail(FDCustomerInfo customer, String orderNumber, Date startTime, Date endTime, Date cutoffTime, List<FDCSContactHours> contactHours) {
			super(customer);
			this.orderNumber = orderNumber;
			this.deliveryStartTime = startTime;
			this.deliveryEndTime = endTime;
			this.cutoffTime = cutoffTime;
			this.contactHours = contactHours;
		}
		/**
		 * @see com.freshdirect.fdstore.mail.FDInfoEmail#decorateMap(java.util.Map)
		 */
		protected void decorateMap(Map map) {
			super.decorateMap(map);
			map.put("orderNumber", this.orderNumber);
			map.put("deliveryStartTime", DT_FORMATTER.format(this.deliveryStartTime));
			map.put("deliveryEndTime", DT_FORMATTER.format(this.deliveryEndTime));
			map.put("cutoffTime", DT_FORMATTER.format(this.cutoffTime));
			map.put("contactHours", this.contactHours);
		}

	}
	
	
	
	public XMLEmailI createAutoRenewDPCCExpiredEmail(FDCustomerInfo customer) {
		FDInfoEmail email = new FDInfoEmail(customer);

		email.setXslPath("h_ar_ccexpire_v1.xsl", "x_ar_ccexpire_v1.xsl");

		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));

		email.setSubject("We were unable to renew your FreshDirect DeliveryPass");

		return email;
	}
	
	public XMLEmailI createUserIdChangeEmail(FDCustomerInfo customer, String oldUserId, String newUserId, EnumEStoreId eStoreId) {
		boolean isFdxOrder = eStoreId.equals(EnumEStoreId.FDX);
		FDInfoEmail email = null;

		if (isFdxOrder) {
			//START : APPBUG-2900 - Added Logger for checking duplicate emails.
			LOGGER.debug("FDEmailFactory :: createUserIdChangeEmail (FDX) ===> Entered ");
			email = new FDInfoEmail(customer);
			email.setXslPath("h_user_id_change_fdx.xsl", "h_user_id_change_fdx.xsl");

			email.setSubject("Email Address Updated");
			email.setFromEmail(FDX_ACTSERVICE_EMAIL); //add to email's data for footer text
			email.setFromAddress(new EmailAddress(FDX_GENERAL_LABEL, FDX_ACTSERVICE_EMAIL));
			
		} else {
			
			//START : APPBUG-2900 - Added Logger for checking duplicate emails.
			LOGGER.debug("FDEmailFactory :: createUserIdChangeEmail (FD) ===> Entered ");
			email = new FDInfoEmail(customer);
			email.setXslPath("h_user_id_change_V1.xsl", "x_user_id_change_V1.xsl");
			email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
			//Added changes for Email Subject
			email.setSubject("FreshDirect: Change in your Email Id");
		}
		
		email.setRecipient(oldUserId);	
		List<String> ccAddress = Arrays.asList(oldUserId, newUserId);
		email.setCCList(ccAddress);
		
		return email;
	}
	
	private static class FDCustomerInfoEmail extends FDInfoEmail {
		Map<String,Object> attributes = null;

		public FDCustomerInfoEmail(FDCustomerInfo customer, Map<String,Object> attributes) {
			super(customer);
			
			attributes.put("customer", customer);
			this.attributes = attributes;
		}
		
		@Override
		protected void decorateMap(Map map) {
			map.put("fromEmail", this.getFromEmail()); 
			map.put("curYear", Calendar.getInstance().get(Calendar.YEAR)); //used in footer
			map.putAll(this.attributes);
		}
	}
	
	public XMLEmailI createShippingAddressChangeEmail(FDCustomerInfo customer, ErpAddressModel erpAddress, EnumEStoreId eStoreId){
		boolean isFdxOrder = eStoreId.equals(EnumEStoreId.FDX);
		FDCustomerInfoEmail email = null;

		Map<String,Object> attributeMap = new HashMap<String,Object>();
		attributeMap.put("erpAddressModel", erpAddress);
		attributeMap.put("phone", PhoneNumber.format(erpAddress.getPhone().getPhone()));
		if(null!=erpAddress.getAltContactPhone()){
			attributeMap.put("alternatePhone", PhoneNumber.format(erpAddress.getAltContactPhone().getPhone()));
		}
		
		if (isFdxOrder) {
			email = new FDCustomerInfoEmail(customer,attributeMap);

			email.setXslPath("h_user_edit_delv_address_fdx.xsl", "h_user_edit_delv_address_fdx.xsl"); //no text version
			email.setSubject("Delivery Address Updated");
			email.setFromEmail(FDX_ACTSERVICE_EMAIL); //add to email's data for footer text
			email.setFromAddress(new EmailAddress(FDX_GENERAL_LABEL, FDX_ACTSERVICE_EMAIL));
		} else {
			email = new FDCustomerInfoEmail(customer,attributeMap);
			email.setXslPath("h_user_edit_delv_address_V1.xsl", "x_user_edit_delv_address_V1.xsl");
			//Added changes for Email Subject
			email.setSubject("FreshDirect: Shipping Address changed to your account");
			email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));		
		}
		
		//START : APPBUG-2898 - Email functionality is not working when user add/ update the delivery address. 
		if(customer.getEmailAddress() != null){
			email.setRecipient(customer.getEmailAddress());	
			List<String> ccAddress = Arrays.asList(customer.getEmailAddress());
			//email.setCCList(ccAddress);
		}
		//END : APPBUG-2898 - Email functionality is not working when user add/ update the delivery address.
		
		return email;
	}
	
	public XMLEmailI createShippingAddressAdditionEmail(FDCustomerInfo customer, ErpAddressModel erpAddress, EnumEStoreId eStoreId){
		boolean isFdxOrder = eStoreId.equals(EnumEStoreId.FDX);
		FDCustomerInfoEmail email = null;
		
		Map<String,Object> attributeMap = new HashMap<String,Object>();
		attributeMap.put("erpAddressModel", erpAddress);
		attributeMap.put("phone", PhoneNumber.format(erpAddress.getPhone().getPhone()));
		if(null!=erpAddress.getAltContactPhone()){
			attributeMap.put("alternatePhone", PhoneNumber.format(erpAddress.getAltContactPhone().getPhone()));
		}

		if (isFdxOrder) {
			email = new FDCustomerInfoEmail(customer,attributeMap);

			email.setXslPath("h_user_new_delv_address_fdx.xsl", "h_user_new_delv_address_fdx.xsl"); //no text version
			email.setSubject("Delivery Address Updated");
			email.setFromEmail(FDX_ACTSERVICE_EMAIL); //add to email's data for footer text
			email.setFromAddress(new EmailAddress(FDX_GENERAL_LABEL, FDX_ACTSERVICE_EMAIL));
			
		} else {			
			email = new FDCustomerInfoEmail(customer,attributeMap);
			email.setXslPath("h_user_new_delv_address_V1.xsl", "x_user_new_delv_address_V1.xsl");
			//Added changes for Email Subject
			email.setSubject("FreshDirect: New Shipping Address added to your account");
			email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
			
		}
		
		//START : APPBUG-2898 - Email functionality is not working when user add/ update the delivery address.
		if(customer.getEmailAddress() != null){
			email.setRecipient(customer.getEmailAddress());	
			List<String> ccAddress = Arrays.asList(customer.getEmailAddress());
			//email.setCCList(ccAddress);
		}
		//END : APPBUG-2898 - Email functionality is not working when user add/ update the delivery address.
		
		return email;
	}
}

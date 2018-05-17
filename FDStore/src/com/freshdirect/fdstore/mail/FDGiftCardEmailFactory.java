/**
 * 
 */
package com.freshdirect.fdstore.mail;

import java.util.Date;
import java.util.Map;

import com.freshdirect.common.pricing.ZoneInfo;
import com.freshdirect.customer.ErpComplaintModel;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDCachedFactory;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.FDCSContactHoursUtil;
import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.framework.mail.EmailAddress;
import com.freshdirect.framework.mail.XMLEmailI;
import com.freshdirect.giftcard.ErpGCDlvInformationHolder;

/**
 * @author ksriram
 * FDGiftCardEmailFactory <extends> FDEmailFactory. This class is meant for sending all the transaction emails related to just Gift Cards.
 */
public class FDGiftCardEmailFactory extends FDEmailFactory{

	private static FDGiftCardEmailFactory _sharedInstance = new FDGiftCardEmailFactory(); 
	public static FDGiftCardEmailFactory getInstance() {		
		return _sharedInstance;
	}
	
	//Private Constructor for the singleton class.
	private FDGiftCardEmailFactory(){
		
	}
	
	/**
	 * To send Gift Card order confirmation email to Purchaser.
	 * @param customer
	 * @param order
	 * @return
	 */
	public XMLEmailI createGiftCardOrderConfirmationEmail(FDCustomerInfo customer, FDOrderI order, boolean isBulkOrder){
		FDTransactionalEmail email = new FDTransactionalEmail(customer, order);		
		if(isBulkOrder){
			email.setXslPath("h_gc_bulk_order_confirm_v1.xsl", "x_gc_bulk_order_confirm_v1.xsl");
		}else{
			email.setXslPath("h_gc_order_confirm_v1.xsl", "x_gc_order_confirm_v1.xsl");
		}
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		email.setSubject("Your Gift Card order for " + df.format(order.getRequestedDate()));
		return email;
	}
	
	public XMLEmailI createGiftCardRecipientEmail(){
		return null;
	}
	
	/**
	 * To send Gift Card order cancellation email to the purchaser.
	 * @param customer
	 * @param order
	 * @return
	 */
	public XMLEmailI createGiftCardCancellationPurchaserEmail(FDCustomerInfo customer, FDOrderI order){
		FDTransactionalEmail email = new FDTransactionalEmail(customer, order);
		email.setXslPath("h_gc_order_cancel_purchaser.xsl", "x_gc_order_cancel_purchaser.xsl");
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		email.setSubject("Service Advisory: Your recent Gift Card purchase");
		return email;
	}
	
	/* (non-Javadoc)
	 * @see com.freshdirect.fdstore.mail.FDEmailFactory#createAuthorizationFailedEmail(com.freshdirect.fdstore.customer.FDCustomerInfo, java.lang.String, java.util.Date, java.util.Date, java.util.Date)
	 */
	
	public XMLEmailI createAuthorizationFailedEmail(FDCustomerInfo customer,
			String orderNumber, Date startTime, Date endTime, Date cutoffTime) {
		FDAuthorizationFailedEmail email = new FDAuthorizationFailedEmail(customer, orderNumber, startTime, endTime, cutoffTime,FDCSContactHoursUtil.getFDCSHours());
		email.setXslPath("h_gc_authorization_failure.xsl", "x_gc_authorization_failure.xsl");
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		email.setSubject("Gift Card Authorization Failure");		

		return email; 
	}

	/**
	 * To send Gift Card order cancellation email to the recipient.
	 * @return
	 */
	public XMLEmailI createGiftCardCancellationRecipientEmail(FDCustomerInfo customer, final ErpGCDlvInformationHolder gcDlvInfo){
		FDInfoEmail email = new FDInfoEmail(customer){
			protected void decorateMap(Map map) {
				map.put("gcDlvInfo", gcDlvInfo);
			}			
		};
		email.setXslPath("h_gc_order_cancel_recipient.xsl", "x_gc_order_cancel_recipient.xsl");		
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, FDStoreProperties.getCustomerServiceEmail()));
		email.setSubject("Your Gift Card order Cancellation");
		return email;
	}
	
	public XMLEmailI createGiftCardCancellationRecipientEmail(FDCustomerInfo customer, final ErpGCDlvInformationHolder gcDlvInfo, final String newRecpEmail){
		FDInfoEmail email = new FDInfoEmail(customer){
			protected void decorateMap(Map map) {
				map.put("gcDlvInfo", gcDlvInfo);
			}	
			
			public String getRecipient(){
				return newRecpEmail;
			}
		};
		email.setXslPath("h_gc_order_cancel_recipient.xsl", "x_gc_order_cancel_recipient.xsl");		
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, FDStoreProperties.getCustomerServiceEmail()));
		email.setSubject("Your Gift Card order Cancellation");
		return email;
	}
	/*public XMLEmailI createGiftCardBulkOrderConfirmationEmail(FDCustomerInfo customer, FDOrderI order, FDBulkRecipientList bulkRecipientList){
		FDGiftCardBulkOrderEmail email = new FDGiftCardBulkOrderEmail(customer, order, bulkRecipientList);
		email.setXslPath("h_gc_bulk_order_confirm_v1.xsl", "x_gc_bulk_order_confirm_v1.xsl");		
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		email.setSubject("Your Gift Card order for " + df.format(order.getRequestedDate()));
		return email;
	}*/
		
	/**
	 * To send Gift Card Issue Cash Back email to the purchaser.
	 * @return
	 */
	public XMLEmailI createGiftCardIssueCashBackEmail(){
		return null;
	}
	
	/**
	 * To send Gift Card Balance Transfer email to the purchaser.
	 * @return
	 */
	public XMLEmailI createGiftCardBalanceTransferEmail(FDCustomerInfo customer, final String recipientName){
		FDInfoEmail email = new FDInfoEmail(customer){
			protected void decorateMap(Map map) {
				map.put("recipientName", recipientName);
			}			
		};
		email.setXslPath("h_gc_balance_transfer.xsl", "x_gc_balance_transfer.xsl");		
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, FDStoreProperties.getCustomerServiceEmail()));
		email.setSubject("Your Gift Card order Balance Transfer");
		return email;
	}

	@Override
	public XMLEmailI createConfirmCreditEmail(FDCustomerInfo customer,
			String saleId, ErpComplaintModel complaint, EnumEStoreId eStoreId) {
		FDConfirmCreditEmail email = new FDConfirmCreditEmail(customer, saleId, complaint);
		//email.setXslPath("h_credit_confirm_v1.xsl", "x_credit_confirm_v1.xsl");
		
		// get the xslpath from the email object in the complaint.
//		ErpCustomerEmailModel custEmailObj = complaint.getCustomerEmail();
//		email.setXslPath(custEmailObj.getHtmlXslPath(),custEmailObj.getPlainTextXslPath());
		email.setXslPath("h_gc_credit_general.xsl","");
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		email.setSubject("We've issued your credits");

		return email;
		
	}
	
	public XMLEmailI createRobinHoodOrderConfirmEmail(FDCustomerInfo customer, final FDOrderI order){
		
		FDTransactionalEmail email = new FDTransactionalEmail(customer, order){
			protected void decorateMap(Map map) {
				super.decorateMap(map);
				map.put("qty", order.getOrderLine(0).getOrderedQuantity());
				try {
					FDProductInfo productInfo = FDCachedFactory.getProductInfo(FDStoreProperties.getRobinHoodSkucode());
					ProductModel productModel = ContentFactory.getInstance().getProduct(FDStoreProperties.getRobinHoodSkucode());
					ZoneInfo zoneInfo =  order.getOrderLine(0).getUserContext().getPricingContext().getZoneInfo();
					map.put("defaultPrice",productInfo.getZonePriceInfo(zoneInfo).getDefaultPrice());
					map.put("defaultPriceUnit",productInfo.getDefaultPriceUnit().toLowerCase());
					map.put("productFullName", productModel.getFullName());
				} catch (FDResourceException e) {
					e.printStackTrace();
				} catch (FDSkuNotFoundException e) {
					e.printStackTrace();
				}
			}
		};		
		email.setXslPath("h_rh_order_confirm_v1.xsl", "x_rh_order_confirm_v1.xsl");		
		email.setFromAddress(new EmailAddress(GENERAL_LABEL, getFromAddress(customer.getDepotCode())));
		email.setSubject("Thank you for giving to Robin Hood and helping New Yorkers in need.");
		return email;
	}
}

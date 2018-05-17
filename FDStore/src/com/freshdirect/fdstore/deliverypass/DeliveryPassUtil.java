package com.freshdirect.fdstore.deliverypass;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import org.apache.log4j.Category;

import com.freshdirect.common.pricing.Discount;
import com.freshdirect.customer.ErpAbstractOrderModel;
import com.freshdirect.customer.ErpDiscountLineModel;
import com.freshdirect.customer.ErpOrderLineModel;
import com.freshdirect.deliverypass.DeliveryPassInfo;
import com.freshdirect.deliverypass.DeliveryPassModel;
import com.freshdirect.deliverypass.DeliveryPassType;
import com.freshdirect.deliverypass.DlvPassConstants;
import com.freshdirect.deliverypass.EnumDlvPassStatus;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartLineModel;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDModifyCartModel;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.framework.core.ServiceLocator;
import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.framework.util.log.LoggerFactory;



public class DeliveryPassUtil {
	private static Category LOGGER = LoggerFactory.getInstance(DeliveryPassUtil.class);
	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy");
	
	private final static ServiceLocator LOCATOR = new ServiceLocator();
	/**
	 * This method returns the delivery pass constructed from the orderline of the
	 * specified order.
	 * @param orderModel
	 * @return
	 */
	public static DeliveryPassModel constructDeliveryPassFromOrder(String customerPk, String orderPk, ErpAbstractOrderModel orderModel) {
		DeliveryPassModel model = null;
		List orderLines = orderModel.getOrderLines();
		Iterator iter = orderLines.iterator();
		while(iter.hasNext()){
			ErpOrderLineModel orderLine = (ErpOrderLineModel)iter.next();
			if(orderLine.isDeliveryPass()){
				DeliveryPassType type = DeliveryPassType.getEnum(orderLine.getSku().getSkuCode());
				String description = orderLine.getDescription();
				String purchaseOrderId = orderPk;
				Date purchaseDate = new Date();
				double amount = 0.0;
				int totalNoofDlvs = 0;
				Date expDate = null;
				if(!type.isUnlimited()){
					//BSGS Pass.
					totalNoofDlvs = type.getNoOfDeliveries();
					amount = orderLine.getPrice();
				}else{
					//Unlimited Pass. AmountPaid = originalPrice - any discount applied.
					amount = orderLine.getPrice() - getAppliedDiscount(orderModel.getDiscounts());
					
					Calendar cal = Calendar.getInstance(Locale.US);
					//Add duration to today's date to calculate expiration date.
					cal.set(Calendar.HOUR, 11);
					cal.set(Calendar.MINUTE, 59);
					cal.set(Calendar.SECOND, 59);
					cal.set(Calendar.AM_PM, Calendar.PM);
					expDate = DateUtil.addDays(cal.getTime(), type.getDuration());
					
				}
				model = new DeliveryPassModel(null, customerPk, type, description, purchaseOrderId, purchaseDate, amount, totalNoofDlvs, totalNoofDlvs, expDate, 0, EnumDlvPassStatus.PENDING);
				break;
			}
		}
		return model;
	}
	
   public static boolean isEligibleStatus(EnumDlvPassStatus status){
	   if(EnumDlvPassStatus.NONE.equals(status) || EnumDlvPassStatus.EXPIRED.equals(status) || 
			   EnumDlvPassStatus.CANCELLED.equals(status) || EnumDlvPassStatus.SHORT_SHIPPED.equals(status)  || 
			   EnumDlvPassStatus.ORDER_CANCELLED.equals(status) || EnumDlvPassStatus.PASS_RETURNED.equals(status)){
		   return true;
	   }
	   return false;
   }
   
   public static boolean isDlvPassExistsStatus(EnumDlvPassStatus status){
	   if(EnumDlvPassStatus.ACTIVE.equals(status) || EnumDlvPassStatus.PENDING.equals(status) || EnumDlvPassStatus.EXPIRED_PENDING.equals(status)){
		   return true;
	   }
	   return false;
   }
   
   public static String getAutoRenewalDate(FDUserI user) {
	   
	   try {
		   Calendar renewalDate=Calendar.getInstance();
		   String expDate=getExpDate(user);
		   if(!expDate.equals("")) {
			   renewalDate.setTime( DATE_FORMATTER.parse(getExpDate(user)));
			   renewalDate.add(Calendar.DATE, 1);
			   return DATE_FORMATTER.format(renewalDate.getTime());
		   }
		   else {
			   return "";
		   }
	   }
	   catch(Exception e) {
			  LOGGER.error(e);
			  return "";
	   }
   }
   
   public static Date getDPExpDate(FDUserI user) {

	   Calendar exp_date=Calendar.getInstance();
	   try {
		   Map passInfo=FDCustomerManager.getDeliveryPassesInfo(user);
		   
		   DeliveryPassInfo dp=(DeliveryPassInfo) passInfo.get(DlvPassConstants.ACTIVE_ITEM);
		   if(dp==null)
			   return null;
		   
		   DeliveryPassModel dpModel=null;
		   exp_date.setTime(dp.getExpirationDate());
		   
		   List passes=(List)passInfo.get(DlvPassConstants.PASS_HISTORY);
		   int days=0;
		   if(passes!=null) {
			   for(int i=0;i<passes.size();i++) {
				   dp=(DeliveryPassInfo)passes.get(i);
				   dpModel=dp.getModel();
				   if((dpModel.getStatus()==EnumDlvPassStatus.READY_TO_USE)||(dpModel.getStatus()==EnumDlvPassStatus.PENDING)) {
					   days=days+dpModel.getType().getDuration();
				   }
			   }
		   }
		   exp_date.add(Calendar.DATE, days);
	   }
	   catch(Exception e) {
		  LOGGER.error(e);
		  return null;
	   }
	   return exp_date.getTime();
   }
   
   public static String getExpDate(FDUserI user) {

	   Date exp_date=getDPExpDate(user);
	   //String returnVal=String.valueOf(renew_date.get(Calendar.MONTH)+1)+"/"+String.valueOf(renew_date.get(Calendar.DATE))+"/"+String.valueOf(renew_date.get(Calendar.YEAR));
	   if(exp_date!=null) {
		   return DATE_FORMATTER.format(exp_date);
	   }
	   else {
		   return "";
	   }

   }
   
   public static String getDlvPassAppliedMessage(FDUserI user){
	   boolean isUnlimited = true;
	   
	   if(user.isDlvPassActive()){
		   //Get the info from active pass.
		   isUnlimited = user.getDlvPassInfo().isUnlimited();
	   }else {
			List orderLines = user.getShoppingCart().getOrderLines();
			if(orderLines.size() == 0){
				
				//Order lines were cleared from the cart after submission. So pull
				//it out from dlvpass info in user object.
				isUnlimited = user.getDlvPassInfo().isUnlimited();
			} else {
				Iterator iter = orderLines.iterator();
				while(iter.hasNext()){
					FDCartLineModel cartLine = (FDCartLineModel)iter.next();
					if(cartLine.lookupFDProduct().isDeliveryPass()){
						DeliveryPassType type = DeliveryPassType.getEnum(cartLine.getSku().getSkuCode());
						isUnlimited = type.isUnlimited();
						break;
					}		
				}
			}
	   }
	   
	 
	   if(isUnlimited){
		   return DlvPassConstants.UNLIMITED_APPLIED_MSG;
	   }else{
		   return DlvPassConstants.BSGS_APPLIED_MSG;
	   }
	}
   public static boolean isOriginalOrder(FDUserI user){
		boolean isOriginalOrder = false;
		FDCartModel cart = user.getShoppingCart();
		if(cart instanceof FDModifyCartModel) {
			String saleId = ((FDModifyCartModel)cart).getOriginalOrder().getErpSalesId();
			isOriginalOrder = saleId.equals(user.getDlvPassInfo().getOriginalOrderId());
		}
		return isOriginalOrder;
   }
   
   public static Map getRemainingMonthsAndDays(Date expirationDate){
	   //Convert no.of days into Months and days.
	   Date today = new Date();
	   double actualNoOfMonths = DateUtil.getDiffInDays(expirationDate, today)/30.43675;
	   long noOfMonths = new Double(actualNoOfMonths).intValue();
	   double diffInMonths = actualNoOfMonths - noOfMonths;
	   long noOfDays = Math.round(diffInMonths * 30.43675);
	   Map monthsAndDays=  new HashMap();
	   monthsAndDays.put("MONTHS", new Integer((int)noOfMonths));
	   monthsAndDays.put("DAYS", new Integer((int)noOfDays));
	   return monthsAndDays;
	   
   }
   
   public static double getPricePaid(DeliveryPassInfo passInfo)throws FDResourceException {
	   
	   
	   //Get the Tax Rate and original retail price of the delivery pass purchased from the orderlines.
	   double taxRate = 0.0;
	   double retailPrice = 0.0;
	   FDOrderI order = FDCustomerManager.getOrder(passInfo.getPurchaseOrderId());
	   List orderLines = order.getOrderLines();
		for (Iterator it = orderLines.iterator(); it.hasNext();) {
			FDCartLineI cartLine = (FDCartLineI) it.next();
			if(cartLine.lookupFDProduct().isDeliveryPass()){
				taxRate = cartLine.getTaxRate();
				retailPrice = cartLine.getPrice();
				break;
			}
		}
		double taxPaid = taxRate * retailPrice;
		double pricePaid = passInfo.getAmount() + taxPaid;
		return pricePaid;
	   
   }
   
   public static double calculateRefund(DeliveryPassInfo passInfo) throws FDResourceException{
	   //Refund formula
	   //(Retail Price paid Discount, Unlimited pass only) x (Remaining # days/ Bought & Credited # of days)
	   double refundAmt = 0.0;
	   //Get the Tax Rate and original retail price of the delivery pass purchased from the orderlines.
	   double taxRate = 0.0;
	   double retailPrice = 0.0;
	   FDOrderI order = FDCustomerManager.getOrder(passInfo.getPurchaseOrderId());
	   List orderLines = order.getOrderLines();
		for (Iterator it = orderLines.iterator(); it.hasNext();) {
			FDCartLineI cartLine = (FDCartLineI) it.next();
			if(cartLine.lookupFDProduct().isDeliveryPass()){
				taxRate = cartLine.getTaxRate();
				retailPrice = cartLine.getPrice();
				break;
			}
		}
		double taxPaid = taxRate * retailPrice;
		
	   if(passInfo.isUnlimited()){
		   //For UNLIMITED Pass.PricePaid = ((RetailPrice - Discount) + taxPaid)
		   double pricePaid = passInfo.getAmount() + taxPaid;
		   Date d1 = passInfo.getExpirationDate();
		   if(null==d1) {
			   return pricePaid;
		   }
		   Date d2 = new Date();
		   double remDays = DateUtil.getDiffInDaysFloor(d1, d2);
		   int daysCredited = 7 * passInfo.getExtendedWeeks();
		   int daysBought = passInfo.getModel().getType().getDuration();
		   int totalDays =  daysBought + daysCredited;
		   if(passInfo.getModel().getType().isAutoRenewDP()&& passInfo.getModel().getUsageCount()==0) {
			   return pricePaid;
		   }
		   refundAmt =  (pricePaid *  remDays) / totalDays ;
	   } else {
		   //For BSGS Pass. PricePaid = (RetailPrice + taxPaid)
		   double pricePaid = passInfo.getAmount() + taxPaid;
		   long remDlvs = passInfo.getRemainingDlvs();
		   int dlvsCredited = passInfo.getCreditCount();
		   long dlvsBought = passInfo.getTotalDlvs() + dlvsCredited;
		   refundAmt =  (pricePaid *  remDlvs) / dlvsBought ;
	   }

	   return refundAmt;
   }
   
      
   /**
    * This method returns any discount amount applied for Unlimited pass.
    * @param discounts
    * @return double
    */
   private static double getAppliedDiscount(List discounts) {
	   Iterator iter = discounts.iterator();
	   double promoAmt = 0.0;
	   while(iter.hasNext()) {
		   ErpDiscountLineModel model = (ErpDiscountLineModel) iter.next();
		   Discount discount = model.getDiscount();
		   String dlvPassPromoPrefix = FDStoreProperties.getDlvPassPromoPrefix();
		   if(discount.getPromotionCode().startsWith(dlvPassPromoPrefix)) {
			   //Then the customer received a delivery pass promo on this order.
			   promoAmt = discount.getAmount();
			   break;	
		   }
	   }
	   return promoAmt;
   }
   public static  boolean isReadyToUse(EnumDlvPassStatus dpStatus) {
		if(EnumDlvPassStatus.READY_TO_USE.equals(dpStatus)) {
			return true;
		}
		else {
			return false;
		}
	}
   
   public static String getAsText(int value) {
	   
	   
	   switch(value) {
	   
			   case 1: return "one";
			   case 2:return "two";
			   case 3: return "three";
			   case 4: return "four";
			   case 5: return "five";
			   case 6: return "six";
			   case 7: return "seven";
			   case 8: return "eight";
			   case 9: return "nine";
			   case 10: return "ten";
			   default: return String.valueOf(value);
	   }
   }
   
   public static int getDaysSinceLastDPExpiry(FDUserI user) {
	   
	   Calendar now=Calendar.getInstance();
	   try {
		   Map passInfo=FDCustomerManager.getDeliveryPassesInfo(user);
		   DeliveryPassInfo dp=null;
		   DeliveryPassModel dpModel=null;
		   List passes=(List)passInfo.get(DlvPassConstants.PASS_HISTORY);
		   int days=0;
		   if(passes!=null) {
			   int _tempDays=0;
			   for(int i=0;i<passes.size();i++) {
				   dp=(DeliveryPassInfo)passes.get(i);
				   dpModel=dp.getModel();
				   if((EnumDlvPassStatus.ACTIVE==dpModel.getStatus())||(EnumDlvPassStatus.CANCELLED==dpModel.getStatus())) {
					   _tempDays=DateUtil.getDiffInDays(dpModel.getExpirationDate(), now.getTime());
					   if(days==0||(days>_tempDays)) {
						   days=_tempDays;
					   }
				   }
			   }
		   }
		   return days*-1;
	   }
	   catch(Exception e) {
		  LOGGER.error(e);
		  return 0;
	   }
   }
   
   
   
}

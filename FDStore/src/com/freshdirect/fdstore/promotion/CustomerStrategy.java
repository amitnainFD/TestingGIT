package com.freshdirect.fdstore.promotion;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;



import com.freshdirect.common.customer.EnumCardType;
import com.freshdirect.customer.ErpPaymentMethodI;
import com.freshdirect.customer.ErpTransactionException;
import com.freshdirect.delivery.EnumComparisionType;
import com.freshdirect.deliverypass.EnumDlvPassStatus;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.ewallet.EnumEwalletType;
import com.freshdirect.framework.util.StringUtil;
import com.freshdirect.payment.BINCache;
import com.freshdirect.payment.gateway.BillingInfo;
import com.freshdirect.payment.gateway.CreditCard;
import com.freshdirect.payment.gateway.Gateway;
import com.freshdirect.payment.gateway.GatewayType;
import com.freshdirect.payment.gateway.Merchant;
import com.freshdirect.payment.gateway.PaymentMethod;
import com.freshdirect.payment.gateway.Request;
import com.freshdirect.payment.gateway.Response;
import com.freshdirect.payment.gateway.TransactionType;
import com.freshdirect.payment.gateway.impl.BillingInfoFactory;
import com.freshdirect.payment.gateway.impl.GatewayFactory;
import com.freshdirect.payment.gateway.impl.PaymentMethodFactory;
import com.freshdirect.payment.gateway.impl.RequestFactory;

public class CustomerStrategy implements PromotionStrategyI {
	private Set<String> cohorts;
	private Set<String> dpTypes;
	private int orderRangeStart;
	private int orderRangeEnd;
	private EnumDlvPassStatus dpStatus;
	private Date dpStartDate;
	private Date dpEndDate;
	private Set<EnumCardType> paymentTypes;
	private int priorEcheckUse;
	private Set<EnumOrderType> allowedOrderTypes;
	private EnumComparisionType eCheckMatchType;
	
	public CustomerStrategy() {
		
	}
	
	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {
		
		//Evaluate Cohorts
		if(cohorts != null && cohorts.size() > 0 && !cohorts.contains(context.getUser().getCohortName())) return DENY;
		
		if(  dpTypes != null && dpTypes.size() > 0) {
			if(context.getUser()==null || context.getUser().getDlvPassInfo()==null)return DENY;
			else if( context.getUser().getDlvPassInfo().getTypePurchased()==null)return DENY;
			else if( !dpTypes.contains(context.getUser().getDlvPassInfo().getTypePurchased().getCode()))
				return DENY;
		}
		
		//Evaluate Order Range. range is not defined properly. DENY
		if((orderRangeStart > 0 && orderRangeEnd <= 0) || (orderRangeStart <= 0 && orderRangeEnd > 0)) return DENY;
		if(orderRangeStart > 0 && orderRangeEnd > 0){
			int currentOrder = context.getAdjustedValidOrderCount() + 1;
			if(currentOrder  < orderRangeStart || currentOrder > orderRangeEnd) return DENY;
		}
		
		//Evaluate Delivery Pass Status
		if(dpStatus != null){
			if(!context.getUser().getDeliveryPassStatus().equals(dpStatus)) return DENY;
			if(dpStartDate != null && dpEndDate != null) {
				Date dpExpDate = context.getUser().getDlvPassInfo().getExpDate();
				if(dpExpDate.before(dpStartDate) || dpExpDate.after(dpEndDate)) return DENY;
			}
		}
		
		//Evaluate Order Types
		if(!evaluateOrderType(context.getOrderType())) {
			context.getUser().addPromoErrorCode(promotionCode, PromotionErrorType.NO_ELIGIBLE_ADDRESS_SELECTED.getErrorCode());
			return DENY;
		}
		
		//Evaluate Payment Types
		FDCartModel cart = context.getShoppingCart();
		if(paymentTypes != null && paymentTypes.size() > 0 ) {
			if(cart.getPaymentMethod() == null){
				context.getUser().addPromoErrorCode(promotionCode, PromotionErrorType.NO_PAYMENT_METHOD_SELECTED.getErrorCode());
				return DENY;
			}
			EnumCardType currentCardType = cart.getPaymentMethod().getCardType();
			/**
			 * Check debit card and add.
			 * 
			 */
			
			if(!paymentTypes.contains(currentCardType) ) {
				
				if(paymentTypes.contains(EnumCardType.DEBIT)) {
					
					if(currentCardType.equals(EnumCardType.VISA)||currentCardType.equals(EnumCardType.MC)) {
						BINCache binCache=BINCache.getInstance();
						String profileId=cart.getPaymentMethod().getProfileID();
						String accNum="";
						if(!StringUtil.isEmpty(profileId) /*&& StringUtil.isEmpty(cart.getPaymentMethod().getAccountNumber())*/) {
							accNum=getAccountNumber(profileId);
							if(!binCache.isDebitCard(accNum, currentCardType)) {
								context.getUser().addPromoErrorCode(promotionCode, PromotionErrorType.NO_ELIGIBLE_PAYMENT_SELECTED.getErrorCode());
								return DENY;
							}
						} else {
							context.getUser().addPromoErrorCode(promotionCode, PromotionErrorType.NO_ELIGIBLE_PAYMENT_SELECTED.getErrorCode());
							return DENY;
						}
						
					} else {
						context.getUser().addPromoErrorCode(promotionCode, PromotionErrorType.NO_ELIGIBLE_PAYMENT_SELECTED.getErrorCode());
						return DENY;
					}
					
				} else if(paymentTypes.contains(EnumCardType.MASTERPASS)) {
					if(null !=cart.getPaymentMethod().geteWalletID()){
						Integer eWalletId = null;
						try {
							eWalletId = Integer.parseInt(cart.getPaymentMethod().geteWalletID());
						} catch (NumberFormatException e) {
							//Ignore
						}
						 EnumEwalletType eWalletType = (null != eWalletId? EnumEwalletType.getEnum(eWalletId): null);
						 if(null ==eWalletType || !eWalletType.getName().equals(EnumCardType.MASTERPASS.getFdName())){
							 context.getUser().addPromoErrorCode(promotionCode, PromotionErrorType.NO_ELIGIBLE_PAYMENT_SELECTED.getErrorCode());
							 return DENY;
						 }
					}else{
						context.getUser().addPromoErrorCode(promotionCode, PromotionErrorType.NO_ELIGIBLE_PAYMENT_SELECTED.getErrorCode());
						return DENY;
					}
					
				} else {
					context.getUser().addPromoErrorCode(promotionCode, PromotionErrorType.NO_ELIGIBLE_PAYMENT_SELECTED.getErrorCode());
					return DENY;
				}
			}
//			if(priorEcheckUse > 0 && currentCardType.equals(EnumCardType.ECP)){
			if(currentCardType.equals(EnumCardType.ECP)) {
				int validEcheckOrderCount = context.getSettledECheckOrderCount();
				if((EnumComparisionType.EQUAL.equals(eCheckMatchType) && validEcheckOrderCount  != priorEcheckUse)
						||(EnumComparisionType.GREATER_OR_EQUAL.equals(eCheckMatchType) &&  validEcheckOrderCount  < priorEcheckUse)
						||(EnumComparisionType.LESS_OR_EQUAL.equals(eCheckMatchType) && validEcheckOrderCount  > priorEcheckUse)){
					return DENY;
				}
//				if(validEcheckOrderCount  < priorEcheckUse) return DENY;
			}
		}
		
		// Voucher redemption promotion :  should not allow promotion for new customer who has profile as 
		// Voucher holder in or out delivery zone
		try {
			if(  (context.getAdjustedValidOrderCount() ==0)
					&& (context.getUser().isVHInDelivery() || context.getUser().isVHOutOfDelivery())){
				 return DENY;
			}
		} catch (FDResourceException e) {
			e.printStackTrace();
		}

		
		return ALLOW;
	}

	private String getAccountNumber(String profileId) {
		String accNum="";
		Request _request=RequestFactory.getRequest(TransactionType.GET_PROFILE);
		CreditCard cc=PaymentMethodFactory.getCreditCard();
		cc.setBillingProfileID(profileId);
		BillingInfo billinginfo=BillingInfoFactory.getBillingInfo(Merchant.FRESHDIRECT,cc);
		_request.setBillingInfo(billinginfo);
		Gateway gateway=GatewayFactory.getGateway(GatewayType.PAYMENTECH);
		try {
			Response _response=gateway.getProfile(_request);
			PaymentMethod pm=_response.getBillingInfo()!=null?_response.getBillingInfo().getPaymentMethod():null;
			if(pm!=null) {
				accNum=pm.getAccountNumber();
			}
		} catch (ErpTransactionException e) {
				
		}
		return accNum;
	}
	public boolean evaluateOrderType(EnumOrderType orderType){
		if(allowedOrderTypes != null && allowedOrderTypes.size() > 0){
			for(Iterator<EnumOrderType> it = allowedOrderTypes.iterator();it.hasNext();){
				if(it.next().getName().equals(orderType.getName())) return true;
			}
		}
		return false;
	}
	
	@Override
	public int getPrecedence() {
		return 2000;
	}

	public String toString() {
		return "CustomerStrategy[...]";
	}


	public String getCohortNames() {
		return convertToCohortNames(this.cohorts);
	}

	public Set<String> getCohorts() {
		return this.cohorts;
	}
	
	public int getOrderRangeStart() {
		return orderRangeStart;
	}


	public int getOrderRangeEnd() {
		return orderRangeEnd;
	}


	public EnumDlvPassStatus getDPStatus() {
		return dpStatus;
	}


	public Date getDPStartDate() {
		return dpStartDate;
	}


	public Date getDPEndDate() {
		return dpEndDate;
	}


	public String getPaymentTypeNames() {
		return convertToPaymentTypeNames(this.paymentTypes);
	}

	public Set getPaymentTypes() {
		return this.paymentTypes;
	}

	
	public int getPriorEcheckUse() {
		return priorEcheckUse;
	}


	public void setCohorts(String cohortNames) {
		this.cohorts = convertToCohorts(cohortNames);

	}

	/*
	 * Utility method to convert comma seperated values into Set.
	 */
	private Set<String> convertToCohorts(String value){
		StringTokenizer tokens = new StringTokenizer(value, ",");
		Set<String> returnSet = new HashSet<String>();
		
		while(tokens.hasMoreTokens()){
			returnSet.add(tokens.nextToken());
		}
		return returnSet;
	}
	
	/*
	 * Utility method to convert Set to comma seperated values.
	 */
	private String convertToCohortNames(Set<String> values){
		if(values == null) return null;
		StringBuffer buf = new StringBuffer();
		for(Iterator<String> it = values.iterator(); it.hasNext();){
			buf.append(it.next());
			if(it.hasNext())
				buf.append(",");
		}
		return buf.toString();
	}
	
	/*
	 * Utility method to convert comma seperated values into Set.
	 */
	private Set<EnumCardType> convertToPaymentTypes(String value){
		StringTokenizer tokens = new StringTokenizer(value,",");
		Set<EnumCardType> returnSet = new HashSet<EnumCardType>();
		
		while(tokens.hasMoreTokens()){
			returnSet.add(EnumCardType.getEnum(tokens.nextToken()));
		}
		return returnSet;
	}
	
	/*
	 * Utility method to convert Set to comma seperated values.
	 */
	private String convertToPaymentTypeNames(Set<EnumCardType> values){
		if(values == null) return null;
		StringBuffer buf = new StringBuffer();
		for(Iterator<EnumCardType> it = values.iterator(); it.hasNext();){
			buf.append(it.next());
			if(it.hasNext())
				buf.append(",");
		}
		return buf.toString();
	}
	
	public void setOrderStartRange(int orderRangeStart) {
		this.orderRangeStart = orderRangeStart;
	}


	public void setOrderEndRange(int orderRangeEnd) {
		this.orderRangeEnd = orderRangeEnd;
	}


	public void setDPStatus(EnumDlvPassStatus dpStatus) {
		this.dpStatus = dpStatus;
	}


	public void setDPStartDate(Date dpStartDate) {
		this.dpStartDate = dpStartDate;
	}


	public void setDPEndDate(Date dpEndDate) {
		this.dpEndDate = dpEndDate;
	}


	public void setPaymentTypes(String paymentTypes) {
		this.paymentTypes = convertToPaymentTypes(paymentTypes);
	}


	public void setPriorEcheckUse(int priorEcheckUse) {
		this.priorEcheckUse = priorEcheckUse;
	}

	public Set<EnumOrderType> getAllowedOrderTypes() {
		return allowedOrderTypes;
	}

	public void setAllowedOrderTypes(Set<EnumOrderType> allowedOrderTypes) {
		this.allowedOrderTypes = allowedOrderTypes;
	}
	
	public boolean evaluateByPayment(ErpPaymentMethodI paymentMethod, PromotionContextI context){
		if(paymentMethod==null) return false;
		EnumCardType cardType=paymentMethod.getCardType();
		boolean isEligible = true;
		if(paymentTypes != null && paymentTypes.size() > 0 ) {			
			if(!paymentTypes.contains(cardType)) {
				
				if(paymentTypes.contains(EnumCardType.DEBIT)) {
					
					if(cardType.equals(EnumCardType.VISA)||cardType.equals(EnumCardType.MC)) {
						
						String profileId=paymentMethod.getProfileID();
						String accNum="";
								
						if(!StringUtil.isEmpty(profileId)) {
							accNum=getAccountNumber(profileId);
							BINCache binCache=BINCache.getInstance();
							if(!binCache.isDebitCard(accNum, cardType)) {
								isEligible = false;
						    }
						} else {
							isEligible = false;
						}
						
					} else {
						isEligible = false;
					}
					
				} else {
					isEligible = false;
				}
				
				//isEligible = false;
			}
			else if(cardType.equals(EnumCardType.ECP)) {
				int validEcheckOrderCount = context.getSettledECheckOrderCount();
				if((EnumComparisionType.EQUAL.equals(eCheckMatchType) && validEcheckOrderCount  != priorEcheckUse)
						||(EnumComparisionType.GREATER_OR_EQUAL.equals(eCheckMatchType) &&  validEcheckOrderCount  < priorEcheckUse)
						||(EnumComparisionType.LESS_OR_EQUAL.equals(eCheckMatchType) && validEcheckOrderCount  > priorEcheckUse)){
					isEligible = false;
				}
			}
		}
		return isEligible;
	}
	
	
	public void setDpTypes(String dpTypes) {
		this.dpTypes = convertToCohorts(dpTypes);

	}
	
	public String getDpTypesNames() {
		return convertToCohortNames(this.dpTypes);
	}

	public Set<String> getDpTypes() {
		return this.dpTypes;
	}

	public EnumComparisionType getECheckMatchType() {
		return eCheckMatchType;
	}

	public void setECheckMatchType(EnumComparisionType checkMatchType) {
		eCheckMatchType = checkMatchType;
	}

	
	@Override
	public boolean isStoreRequired() {
		return false;
	}
}

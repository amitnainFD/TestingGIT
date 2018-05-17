/*
 * PaymentMethodName.java
 *
 * Created on September 27, 2001, 8:00 PM
 */

package com.freshdirect.fdstore.ewallet;

/**
 *
 * @author  jmccarter
 * @version
 */
public interface PaymentMethodName {
    //
    //	Credit Card constants
    //
    public final static String CARD_BRAND     	= "cardBrand";
    public final static String CARD_EXP_MONTH  	= "cardMonth";
    public final static String CARD_EXP_YEAR  	= "cardYear";

    // JCN: must change parameter names
    public final static String ACCOUNT_NUMBER		= "cardNum";
    public final static String ACCOUNT_NUMBER_VERIFY = "cardNumVerify";
    public final static String ACCOUNT_HOLDER     	= "cardHolderName";
    public final static String BANK_NAME     		= "bankName";
    public final static String ABA_ROUTE_NUMBER     = "abaRouteNumber";
    public final static String BANK_ACCOUNT_TYPE    = "bankAccountType";
    public final static String PAYMENT_METHOD_TYPE  = "paymentMethodType";

    public final static String TERMS  				= "terms";
    public final static String BYPASS_BAD_ACCOUNT_CHECK	= "bypassBadAccountCheck";
    
    public final static String CSV="csv";
    
    public static final String PHONE = "phone";
    
}


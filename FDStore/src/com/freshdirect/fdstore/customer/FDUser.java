package com.freshdirect.fdstore.customer;

import java.text.ChoiceFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Category;

import com.freshdirect.FDCouponProperties;
import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.context.FulfillmentContext;
import com.freshdirect.common.context.MasqueradeContext;
import com.freshdirect.common.context.StoreContext;
import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.common.pricing.ZoneInfo;
import com.freshdirect.customer.ActivityLog;
import com.freshdirect.customer.EnumAccountActivityType;
import com.freshdirect.customer.EnumAlertType;
import com.freshdirect.customer.EnumChargeType;
import com.freshdirect.customer.EnumDeliveryType;
import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.customer.EnumSaleType;
import com.freshdirect.customer.EnumTransactionSource;
import com.freshdirect.customer.ErpActivityRecord;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpClientCode;
import com.freshdirect.customer.ErpCustomerInfoModel;
import com.freshdirect.customer.ErpCustomerModel;
import com.freshdirect.customer.ErpDiscountLineModel;
import com.freshdirect.customer.ErpPaymentMethodI;
import com.freshdirect.customer.ErpPromotionHistory;
import com.freshdirect.customer.OrderHistoryI;
import com.freshdirect.deliverypass.DeliveryPassModel;
import com.freshdirect.deliverypass.DlvPassConstants;
import com.freshdirect.deliverypass.EnumDPAutoRenewalType;
import com.freshdirect.deliverypass.EnumDlvPassProfileType;
import com.freshdirect.deliverypass.EnumDlvPassStatus;
import com.freshdirect.fdlogistics.model.FDDeliveryServiceSelectionResult;
import com.freshdirect.fdlogistics.model.FDDeliveryZoneInfo;
import com.freshdirect.fdlogistics.model.FDInvalidAddressException;
import com.freshdirect.fdlogistics.model.FDReservation;
import com.freshdirect.fdstore.EnumCheckoutMode;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.OncePerRequestDateCache;
import com.freshdirect.fdstore.ZonePriceListing;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.EnumWinePrice;
import com.freshdirect.fdstore.content.HolidayGreeting;
import com.freshdirect.fdstore.content.MyFD;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.ProductReference;
import com.freshdirect.fdstore.content.StoreModel;
import com.freshdirect.fdstore.customer.adapter.PromotionContextAdapter;
import com.freshdirect.fdstore.deliverypass.FDUserDlvPassInfo;
import com.freshdirect.fdstore.ecoupon.EnumCouponContext;
import com.freshdirect.fdstore.ecoupon.FDCustomerCoupon;
import com.freshdirect.fdstore.ecoupon.model.FDCustomerCouponWallet;
import com.freshdirect.fdstore.giftcard.FDGiftCardInfoList;
import com.freshdirect.fdstore.giftcard.FDGiftCardModel;
import com.freshdirect.fdstore.lists.CclUtils;
import com.freshdirect.fdstore.lists.FDCustomerListInfo;
import com.freshdirect.fdstore.lists.FDListManager;
import com.freshdirect.fdstore.promotion.AssignedCustomerParam;
import com.freshdirect.fdstore.promotion.EnumPromotionType;
import com.freshdirect.fdstore.promotion.FDPromotionVisitor;
import com.freshdirect.fdstore.promotion.PromoVariantModel;
import com.freshdirect.fdstore.promotion.PromotionFactory;
import com.freshdirect.fdstore.promotion.PromotionI;
import com.freshdirect.fdstore.promotion.SignupDiscountRule;
import com.freshdirect.fdstore.promotion.WaiveDeliveryCharge;
import com.freshdirect.fdstore.promotion.management.FDPromotionNewManager;
import com.freshdirect.fdstore.referral.FDReferralManager;
import com.freshdirect.fdstore.rules.EligibilityCalculator;
import com.freshdirect.fdstore.rules.FDRulesContextImpl;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;
import com.freshdirect.fdstore.survey.EnumSurveyType;
import com.freshdirect.fdstore.survey.FDSurvey;
import com.freshdirect.fdstore.survey.FDSurveyConstants;
import com.freshdirect.fdstore.survey.FDSurveyFactory;
import com.freshdirect.fdstore.survey.FDSurveyResponse;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.fdstore.util.IgnoreCaseString;
import com.freshdirect.fdstore.util.TimeslotLogic;
import com.freshdirect.fdstore.zone.FDZoneInfoManager;
import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.util.NVL;
import com.freshdirect.framework.util.StringUtil;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.giftcard.EnumGiftCardType;
import com.freshdirect.giftcard.ErpGCDlvInformationHolder;
import com.freshdirect.logistics.analytics.model.SessionEvent;
import com.freshdirect.logistics.delivery.dto.CustomerAvgOrderSize;
import com.freshdirect.logistics.delivery.model.EnumDeliveryStatus;
import com.freshdirect.logistics.delivery.model.EnumRegionServiceType;
import com.freshdirect.logistics.delivery.model.FulfillmentInfo;
import com.freshdirect.logistics.delivery.model.SalesArea;
import com.freshdirect.smartstore.fdstore.CohortSelector;
import com.freshdirect.smartstore.fdstore.DatabaseScoreFactorProvider;

public class FDUser extends ModelSupport implements FDUserI {
	private final static Category LOGGER = LoggerFactory.getInstance(FDUser.class);
	public static final String ROBOT_USER_NAME = "robot";

	private static final long serialVersionUID = 8492744405934393676L;

	public static final String SERVICE_EMAIL = "service@freshdirect.com";
	public final static int CAMPAIGN_MSG_VIEW_LIMIT = 4;

	private EnumTransactionSource application;
    private String depotCode;

    private Set availableServices = new HashSet();
    private Collection<EnumServiceType> servicesBasedOnAddress;
    private EnumServiceType selectedServiceType = null;
    // for new COS customers
    private EnumServiceType userServiceType = null;
    // added for zone pricing to capture zp service type.
    private EnumServiceType zpServiceType = null;
    
    private FDIdentity identity;
    private SessionEvent event;
    
    private AddressModel address;
    private FDReservation reservation;
    private FDCartModel shoppingCart = initializeCart();
    
    //Creating a dummy cart for gift card processing.
    private FDCartModel dummyCart = new FDCartModel();
    private FDRecipientList recipientList;
    private FDBulkRecipientList bulkRecipientList;
    
    private SignupDiscountRule signupDiscountRule;
    private boolean promotionAddressMismatch = false;
	private String redeemedPromotionCode;

    private String cookie;
    private boolean loggedIn = false;

    private boolean surveySkipped = false;

    private transient ErpCustomerInfoModel customerInfoModel;
    private transient FDOrderHistory cachedOrderHistory;
    private transient CustomerAvgOrderSize historicOrderSize;
    private transient FDCustomerModel cachedFDCustomer;
	protected transient FDPromotionEligibility promotionEligibility;
	private transient Boolean checkEligible;
	private transient Boolean referrerEligible;
	private transient String regRefTrackingCode;
	private transient List<FDCustomerListInfo> cclListInfos;
	private transient List<FDCustomerListInfo> soListInfos;

	private String lastRefTrackingCode;

	private String lastRefProgId=null;
	private String lastRefTrkDtls=null;
	private String lastRefProgInvtId=null;

	private String userId;

	private boolean active = false;
	private boolean receiveFDemails = true;

	private boolean isHomePageLetterVisited=false;
	private int campaignMsgViewed = 0;

	//Contains user specific Delivery Pass Details.
	private FDUserDlvPassInfo dlvPassInfo;

	private Map<String,AssignedCustomerParam> assignedCustomerParams;

	private EnumWinePrice preferredWinePrice = null;

	/*
	 * This attribute caches the list of product keys that are already
	 * evaluated for DCPD along with its DCPD promo info. Only used by
	 * Web tier for applying promotions. So transient.
	 */
	private transient DCPDPromoProductCache dcpdPromoProductCache;
	//New Promotion History cache. PERF-22.
	private transient ErpPromotionHistory cachedPromoHistory;

	// Cohort ID
	private String cohortName;

	//Eligible Promo Variant Map
	private Map promoVariantMap;
	private String savingsVariantId;
	private boolean savingsVariantFound;

	private boolean isPostPromoConflictEnabled;
	private boolean isPromoConflictResolutionApplied;
	
	private FDGiftCardInfoList cachedGiftCards;
	
	//Create a dummy cart for Donation Orders.
	private FDCartModel donationCart = new FDCartModel();
	private Integer donationTotalQuantity = 0;
	
	private Map<String, ErpGCDlvInformationHolder> cachedRecipientInfo = null;
	
	private UserContext userContext;
	
	protected Boolean isSOEligible = null;
	protected Boolean hasEBTAlert = null;

	protected Boolean cliCodeEligible = null;
	private Set<String> allAppliedPromos = new HashSet<String>();
	
	protected SortedSet<IgnoreCaseString> clientCodesHistory = null; 
	private Map<String, Integer> promoErrorCodes = new ConcurrentHashMap <String, Integer>();
	
	private MasqueradeContext masqueradeContext;
	
	private int ctSlots;
	private double percSlotsSold;
	
	private Date registrationDate;

	private static final Date EPOCH = new Date(0);

	//mergePendingOrder (APPDEV-2031)
	private boolean showPendingOrderOverlay = true;
	private boolean suspendShowPendingOrderOverlay = false;
	private FDCartModel mergePendCart = new FDCartModel();

	/*Appdev-1888 */
	private String referralLink;
	private String referralPrgmId;
	private String referralCustomerId;
	List<PromotionI> referralPromoList = new ArrayList<PromotionI>();
	Double totalCredit = null;
	Boolean referralFlag = null;
	boolean referralFraud = false;
	public String tsaPromoCode = null;

	private EnumGiftCardType giftCardType = null;
	
	private boolean ebtAccepted = false;
	
    private List<ErpAddressModel> cachedAllHomeAddresses;
    private List<ErpAddressModel> cachedAllCorporateAddresses;
    private boolean robot; //true if user object represents a search bot or crawler
    
	private Set<String> steeringSlotIds = new HashSet<String>();

	private Set<ExternalCampaign> externalPromoCampaigns = new HashSet<ExternalCampaign>();

	private FDCustomerCouponWallet couponWallet;

	private ExternalCampaign externalCampaign;
	
	private String defaultListId = null;
	
	private boolean anyNewOrder = false;
	
	private String clientIp;
	private String serverName;
	
	/* APPDEV-3756 */
	private boolean isGlobalNavTutorialSeen = false;
	private List<ProductReference> productSamples;
	private Date firstOrderDate = null;
	private Date firstOrderDateByStore = null;
	
	private boolean crmMode;
	private String rafClickId;
	private String rafPromoCode;
	
	/* APPDEV-4381 */
	private Date tcAcknowledgeDate=null;
	private boolean tcAcknowledge=false;
	
	private boolean vHPopupDisplay=false;
	
	public Date getTcAcknowledgeDate() {
		return tcAcknowledgeDate;
	}

	public void setTcAcknowledgeDate(Date tcAcknowledgeDate) {
		this.tcAcknowledgeDate = tcAcknowledgeDate;
		
	}

	public boolean getTcAcknowledge() {
		//Until T&C is enabled, treat customers have accepted T&C. 
		if(!FDStoreProperties.isTCEnabled()){
			return true;
		}
		if(this.cachedFDCustomer!=null &&this.cachedFDCustomer.getCustomerEStoreModel()!=null){
			return this.cachedFDCustomer.getCustomerEStoreModel().getTcAcknowledge();	
		}else {
			return false;
		}
		
	}

	public void setTcAcknowledge(boolean tcAcknowledge) {
		//this.tcAcknowledge = tcAcknowledge;
		this.cachedFDCustomer.getCustomerEStoreModel().setTcAcknowledge(true);
	}




	public String getTsaPromoCode() {
		return tsaPromoCode;
	}

	private FDCartModel initializeCart() {
		if(this.shoppingCart == null){
			this.shoppingCart =  new FDCartModel();
			this.shoppingCart.setEStoreId(this.getUserContext().getStoreContext().getEStoreId());
		}
		return this.shoppingCart;
	}
    		

	public void setTsaPromoCode(String tsaPromoCode) {
		this.tsaPromoCode = tsaPromoCode;
	}

	public FDUserDlvPassInfo getDlvPassInfo() {
		return dlvPassInfo;
	}

	public void setDlvPassInfo(FDUserDlvPassInfo dlvPassInfo) {
		this.dlvPassInfo = dlvPassInfo;
	}

	public FDUser(PrimaryKey pk) {
		super();
		this.setPK(pk);
	}

	public FDUser() {
		super();
	}

	public EnumTransactionSource getApplication() {
		return application;
	}

	public void setApplication(EnumTransactionSource source) {
		this.application = source;
	}

	public String getCookie() {
        return this.cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
        this.invalidateCache();
    }
    
    public String getState() {
        return this.address == null ? null : this.address.getState();
    }

    public String getZipCode() {
        return this.address == null ? null : this.address.getZipCode();
    }

    public void setZipCode(String zipCode) {
        AddressModel a = new AddressModel();
        a.setZipCode(zipCode);
        this.address = a;
        this.invalidateCache();
        this.userContext=null;
        this.userContext=getUserContext();
    }

    public void setAddress(AddressModel a) {
    	AddressModel old=this.address;
        this.address = a;
        this.invalidateCache();
        this.userContext=null;
        this.userContext=getUserContext();
        
    }

    
    public AddressModel getAddress() {
        return this.address;
    }

    public String getPrimaryKey() {
    	return super.getId();
    }

    public FDIdentity getIdentity() {
        return this.identity;
    }

    public void setIdentity(FDIdentity identity) {
        this.identity = identity;
        this.invalidateCache();
        
    }

    public int getLevel() {
        if (identity == null) {
            return GUEST;
        } else if ((identity != null) && !loggedIn) {
            return RECOGNIZED;
        } else if ((identity != null) && loggedIn) {
            return SIGNED_IN;
        }
        return -1;
    }

    public boolean isInZone() {
        return this.isDeliverableUser();
    }

    public boolean isDeliverableUser() {
    	return this.availableServices.contains(EnumServiceType.HOME) || this.availableServices.contains(EnumServiceType.DEPOT) || this.availableServices.contains(EnumServiceType.CORPORATE);
    }

    public void isLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public FDCartModel getShoppingCart() {
        return this.shoppingCart;
    }

    public void setShoppingCart(FDCartModel cart) {
        this.shoppingCart = cart;
    }

    public boolean isSurveySkipped() {
        return surveySkipped;
    }

    public void setSurveySkipped(boolean surveySkipped) {
        this.surveySkipped = surveySkipped;
    }

    public boolean isFraudulent() throws FDResourceException {
    	if (this.identity==null) {
    		return false;
    	}
		return !this.getFDCustomer().isEligibleForSignupPromo();
    }

	public FDCustomerModel getFDCustomer() throws FDResourceException {
		if (this.identity==null) {
			throw new IllegalStateException("No identity");
		}
		if (this.cachedFDCustomer==null) {
			this.cachedFDCustomer = FDCustomerFactory.getFDCustomer(this.identity);
		}
		return this.cachedFDCustomer;

	}

    public double getMaxSignupPromotion() {
        if (this.signupDiscountRule != null) {
            return this.signupDiscountRule.getMaxAmount();
        } else {
            return 0.0;
        }
    }

	public SignupDiscountRule getSignupDiscountRule(){
		if (this.promotionEligibility == null) {
			updateUserState();
		}
		return this.signupDiscountRule;
	}

    public void setSignupDiscountRule(SignupDiscountRule discountRule){
        this.signupDiscountRule = discountRule;
    }

    public boolean isPromotionAddressMismatch() {
        return promotionAddressMismatch;
    }

    public void setPromotionAddressMismatch(boolean b) {
        promotionAddressMismatch = b;
    }

	public void setRedeemedPromotion(PromotionI promotion) {
		this.redeemedPromotionCode = promotion==null ? null : promotion.getPromotionCode();
	}

	public PromotionI getRedeemedPromotion() {
		return this.redeemedPromotionCode == null
			? null
			: PromotionFactory.getInstance().getPromotion(this.redeemedPromotionCode);
	}

	/*APPDEV-1888*/
	public List<PromotionI> getReferralPromoList() {
		return referralPromoList;
	}

	public void setReferralPromoList() {
		// load referral promotion only to refer a friend target customers
		if(this.getIdentity() != null && FDStoreProperties.isExtoleRafEnabled() ? this.getRafPromoCode() != null : this.getReferralCustomerId() != null)
			try {
				referralPromoList = FDPromotionNewManager.getReferralPromotions(this.getIdentity().getErpCustomerPK());
			} catch (FDResourceException e) {
				LOGGER.error("Error getting referral promotions.",e);
			}
	}

    public void updateUserState(){
		try {
			this.getShoppingCart().recalculateTaxAndBottleDeposit(getZipCode());
			this.getShoppingCart().updateSurcharges(new FDRulesContextImpl(this));
			/*APPDEV-1888*/
			if (this.getReferralPromoList().size() == 0) {
				this.setReferralPromoList();
			}			
			this.applyPromotions();
			this.applyOrderMinimum();
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e.getMessage());
		}
    }
    
    public void applyOrderMinimum() {
    	if(this.getReservation()!=null)
    		TimeslotLogic.applyOrderMinimum(this, this.getReservation().getTimeslot());
    	if(this.getShoppingCart()!=null && this.getShoppingCart().getDeliveryReservation()!=null)
    		TimeslotLogic.applyOrderMinimum(this, this.getShoppingCart().getDeliveryReservation().getTimeslot());
    }

	private void applyPromotions(){
    	// clear previous promotions
    	this.setSignupDiscountRule(null);
		this.setPromotionAddressMismatch(false);

		this.getShoppingCart().clearSampleLines();
		this.getShoppingCart().setDiscounts(new ArrayList<ErpDiscountLineModel>());
		this.getShoppingCart().clearSkuCount();
		this.getShoppingCart().clearLineItemDiscounts();
		this.clearPromoErrorCodes();
		this.getShoppingCart().setDlvPassExtn(null);
		this.getShoppingCart().setDlvPromotionApplied(false);
		if((this.getShoppingCart().getDeliveryPassCount()>0)||(this.isDlvPassActive())) {
			this.getShoppingCart().setDlvPassApplied(true);
		}
		
		// evaluate special dlv charge override
		WaiveDeliveryCharge.apply(this);
		
		this.promotionEligibility = new FDPromotionEligibility();
		// apply promotions
		this.promotionEligibility =FDPromotionVisitor.evaluateAndApplyPromotions(new PromotionContextAdapter(this), promotionEligibility);
		//Add all applied promotion codes so far to this list. Used by MaxRedemptionStrategy
		this.allAppliedPromos.addAll(promotionEligibility.getAppliedPromotionCodes());
    }


    public String getFirstName() throws FDResourceException {
    	ErpCustomerInfoModel info = getCustomerInfoModel();
		if (info == null) {
			return "";
		}
		return info.getFirstName();
    }

    public String getLastName() throws FDResourceException {
    	ErpCustomerInfoModel info = getCustomerInfoModel();
		if (info == null) {
			return "";
		}
		return info.getLastName();
    }

    public String getDepotCode() {
        return this.depotCode;
    }

    public void setDepotCode(String depotCode) {
        this.depotCode = depotCode;
        this.invalidateCache();
    }

    public boolean isDepotUser() {
        return depotCode != null;
    }

	public boolean isCorporateUser() {
		return EnumServiceType.CORPORATE.equals(this.selectedServiceType); //  || this.availableServices.contains(EnumServiceType.CORPORATE);
	}

    public void invalidateCache() {
    	//Commented as part of PERF-22 task.
        //this.cachedOrderHistory = null;
        this.signupDiscountRule = null;
        this.cachedFDCustomer = null;
        this.customerInfoModel = null;
		this.promotionEligibility = null;
		this.checkEligible = null;
		this.referrerEligible = null;
		this.regRefTrackingCode = null;
		this.cclListInfos = null;
		this.soListInfos = null;
		this.cachedPromoHistory = null;
		this.promoVariantMap = null;
		this.isSOEligible = null;
		this.hasEBTAlert = null;
		this.vHPopupDisplay=false;
		
    }
    /*
     * This method was introduced as part of PERF-22 task.
     * Seperate invalidation of Order History Cache from other caches.
     */
    public void invalidateOrderHistoryCache() {
    	this.cachedOrderHistory = null;
    }

    public OrderHistoryI getOrderHistory() throws FDResourceException {
        if (this.cachedOrderHistory==null) {
            this.cachedOrderHistory = FDCustomerManager.getOrderHistoryInfo(this.identity);
        }
        
        return this.cachedOrderHistory;
    }
    
    public Date getFirstOrderDate() throws FDResourceException{
    	if(null == firstOrderDate){
    		OrderHistoryI orderHistory = this.getOrderHistory();
    		if(null != orderHistory){
    			firstOrderDate = orderHistory.getFirstOrderDate();
    		}
    	}
    	return firstOrderDate;
    }

  /* private OrderHistoryI getOrderHistoryInfo() throws FDResourceException {
	   
	    * This change is rollbacked temporarily.
    	if(EnumTransactionSource.CUSTOMER_REP.equals(application)){
    		//If CRM load entire order history.
    		return FDCustomerManager.getOrderHistoryInfo(this.identity);
    	} else {
    		//Load only Order History Summary.
    		return FDCustomerManager.getWebOrderHistoryInfo(this.identity);
    	}
    	
	   //Load Entire order history inspite of CRM or WEB.
	   return FDCustomerManager.getOrderHistoryInfo(this.identity);
    }*/

   public CustomerAvgOrderSize getHistoricOrderSize() throws FDResourceException {
       if (this.historicOrderSize==null && this.identity!=null) {
    	   this.historicOrderSize = FDCustomerManager.getHistoricOrderSize(this.identity.getErpCustomerPK());
       }
       return this.historicOrderSize;
   }
   
   public int getOrderCountForChefsTableEligibility() throws FDResourceException {
	   OrderHistoryI orderHistory=getOrderHistory();
	   return (int)orderHistory.getOrderCountForChefsTableEligibility();
   }

   public String getOrderTotalForChefsTableEligibility() throws FDResourceException {
	   OrderHistoryI orderHistory=getOrderHistory();

	   return NumberFormat.getCurrencyInstance(Locale.US).format(orderHistory.getOrderSubTotalForChefsTableEligibility());
   }

   public String getOrderCountRemainingForChefsTableEligibility() throws FDResourceException {
	   ChoiceFormat fmt = new ChoiceFormat(
	      "1#one |2#two |3#three | 4#four | 5#five");

		int orderCount = getOrderCountForChefsTableEligibility();
		if(orderCount == 0 || (orderCount >= CHEFS_TABLE_ORDER_COUNT_QUALIFIER) ||
				(CHEFS_TABLE_ORDER_COUNT_QUALIFIER - orderCount > CHEFS_TABLE_GETTING_CLOSE_COUNT)) {
			return "";
		}
		return fmt.format(CHEFS_TABLE_ORDER_COUNT_QUALIFIER - orderCount);
	}

	public String getOrderTotalRemainingForChefsTableEligibility() throws FDResourceException  {
		OrderHistoryI orderHistory=getOrderHistory();
		double orderTotal = orderHistory.getOrderSubTotalForChefsTableEligibility();
		if(orderTotal == 0.0 || (orderTotal >= CHEFS_TABLE_ORDER_TOTAL_QUALIFIER) ||
				CHEFS_TABLE_ORDER_TOTAL_QUALIFIER - orderTotal > CHEFS_TABLE_GETTING_CLOSE_TOTAL) {
			return "";
		}

		return new DecimalFormat("$#0").format(CHEFS_TABLE_ORDER_TOTAL_QUALIFIER - orderTotal);
	}

	public boolean isCloseToCTEligibilityByOrderCount() throws FDResourceException {
		int orderCount = getOrderCountForChefsTableEligibility();
		if( (CHEFS_TABLE_ORDER_COUNT_QUALIFIER - orderCount <= CHEFS_TABLE_GETTING_CLOSE_COUNT) &&
				(CHEFS_TABLE_ORDER_COUNT_QUALIFIER - orderCount > 0) ) {
			return true;
		}
		return false;
	}

	public boolean isCloseToCTEligibilityByOrderTotal() throws FDResourceException {
		double orderTotal = getOrderHistory().getOrderSubTotalForChefsTableEligibility();
		if( (CHEFS_TABLE_ORDER_TOTAL_QUALIFIER - orderTotal <= CHEFS_TABLE_GETTING_CLOSE_TOTAL) &&
				(CHEFS_TABLE_ORDER_TOTAL_QUALIFIER - orderTotal > 0.0 ) ) {
			return true;
		}
		return false;
	}

	public boolean hasQualifiedForCT() throws FDResourceException {
		double orderTotal = getOrderHistory().getOrderSubTotalForChefsTableEligibility();
		int orderCount = getOrderCountForChefsTableEligibility();

		if( (orderTotal >= CHEFS_TABLE_ORDER_TOTAL_QUALIFIER) ||
			(orderCount >= CHEFS_TABLE_ORDER_COUNT_QUALIFIER) ) {
				return true;
		}

		return false;
 	}



	public boolean isOkayToDisplayCTEligibility() throws FDResourceException {
		if(!isCloseToCTEligibilityByOrderTotal() && !isCloseToCTEligibilityByOrderCount()) {
			return false;
		}
		Calendar cal = new GregorianCalendar(Locale.getDefault());
		cal = Calendar.getInstance();
		int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		if( lastDay == cal.get(Calendar.DAY_OF_MONTH) ||
				cal.get(Calendar.DAY_OF_MONTH) <= 2) {
			return false;
		}
		return true;
	}

	public String getEndChefsTableQualifyingDate() throws FDResourceException {
		Calendar cal = new GregorianCalendar(Locale.getDefault());
		cal = Calendar.getInstance();
		int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, lastDay);
		return new SimpleDateFormat("MMMMM d, yyyy").format(cal.getTime());
	}

    public ErpPromotionHistory getPromotionHistory() throws FDResourceException {
        if (this.cachedPromoHistory==null) {
            this.cachedPromoHistory = FDCustomerManager.getPromoHistoryInfo(this.identity);
        }
        return this.cachedPromoHistory;
    }

    /**
     * @return number of valid orders, corrected in modify order mode
     */
    public int getAdjustedValidOrderCount() throws FDResourceException {
    	int orderCount = this.getOrderHistory().getValidOrderCount();
        if (this.getShoppingCart() instanceof FDModifyCartModel) {
            // we're in modify order mode, subtract one
            orderCount--;
        }
        return orderCount;
    }

    /**
     * @return number of valid ECheck orders, corrected in modify order mode
     */
    public int getAdjustedValidECheckOrderCount() throws FDResourceException {
    	int orderCount = this.getOrderHistory().getValidECheckOrderCount();
        if (this.getShoppingCart() instanceof FDModifyCartModel) {
            // we're in modify order mode, subtract one
            orderCount--;
        }
        return orderCount;
    }
    /**
     * @return number of delivered orders
     */
    public int getDeliveredOrderCount() throws FDResourceException {
        int orderCount = this.getOrderHistory().getDeliveredOrderCount();
        return orderCount;
    }

    /**
     * @return number of phone orders
     */
    public int getValidPhoneOrderCount() throws FDResourceException {
        return this.getOrderHistory().getValidPhoneOrderCount();
    }

	public FDPromotionEligibility getPromotionEligibility(){
		if (this.promotionEligibility==null) {
			this.updateUserState();
		}

		return this.promotionEligibility;
	}

	public boolean isEligibleForSignupPromotion(){
		return this.getPromotionEligibility().isEligibleForType(EnumPromotionType.SIGNUP);
	}

	public PromotionI getEligibleSignupPromotion(){
		Set promoSet = this.getPromotionEligibility().getEligiblePromotionCodes(EnumPromotionType.SIGNUP);
		if (null == promoSet || promoSet.isEmpty()) {
			return null;
		}
		String code = (String) promoSet.iterator().next();
		return PromotionFactory.getInstance().getPromotion(code);
	}

    /**
     * @return true if the order minimum has been met (FDUserI.MINIMUM_ORDER_AMOUNT)
     */
	public boolean isOrderMinimumMet() throws FDResourceException {
		return this.isOrderMinimumMet(false, null);
	}

	public boolean isOrderMinimumMet(Double overrideMinimumAmount) throws FDResourceException {
		return this.isOrderMinimumMet(false, overrideMinimumAmount);
	}
	
    public boolean isOrderMinimumMet(boolean alcohol) throws FDResourceException {
    	return isOrderMinimumMet(alcohol, null);
    }

    public boolean isOrderMinimumMet(boolean alcohol, Double overrideMinimumAmount) throws FDResourceException {
		double subTotal = alcohol ? this.shoppingCart.getSubTotalWithoutAlcohol() : this.shoppingCart.getSubTotal();
		return subTotal >= (overrideMinimumAmount == null ? getMinimumOrderAmount() : overrideMinimumAmount);
    }

    public boolean isOrderMinimumMetWithoutWine() throws FDResourceException {
		double subTotal = this.shoppingCart.getSubTotalWithoutWineAndSpirit();
		return subTotal >=  getMinimumOrderAmount();
    }
    public double getMinimumOrderAmount() {
    	if (getShoppingCart() != null && getShoppingCart().getDeliveryAddress() != null){
			try {
				String county = FDDeliveryManager.getInstance().getCounty(getShoppingCart().getDeliveryAddress());
				String zip = getShoppingCart().getDeliveryAddress().getZipCode();
				String zipcodes = FDStoreProperties.getSuffolkZips();
				if("SUFFOLK".equalsIgnoreCase(county) && (zipcodes.indexOf(zip)==-1) ){
					return 99;
				}
			} catch (FDResourceException e) {
				throw new FDRuntimeException(e);
			}
		}
		return EnumServiceType.CORPORATE.equals(this.getSelectedServiceType()) ? MIN_CORP_ORDER_AMOUNT 
							: ((EnumEStoreId.FDX.equals(this.getUserContext().getStoreContext().getEStoreId())) ? FDX_MINIMUM_ORDER_AMOUNT : MINIMUM_ORDER_AMOUNT);
	}
    
	public float getQuantityMaximum(ProductModel product) {
		float pMax = 200;
		
		if (this.getMasqueradeContext() != null) {
			return pMax;
		} else {
			return product.enforceQuantityMax() || (!this.isCorporateUser())
				? product.getQuantityMaximum() : pMax;
		}
		
	}

    public boolean isPickupUser() {
    	return this.availableServices.contains(EnumServiceType.PICKUP) || this.availableServices.contains(EnumServiceType.HOME) || this.availableServices.contains(EnumServiceType.CORPORATE);
    }

    public boolean isPickupOnly() {
    	return !this.availableServices.contains(EnumServiceType.DEPOT) && !this.availableServices.contains(EnumServiceType.CORPORATE) &&
    	!this.availableServices.contains(EnumServiceType.HOME) && this.availableServices.contains(EnumServiceType.PICKUP);
    }

    public  boolean isNotServiceable() {
    	return this.availableServices.isEmpty();
    }

    public boolean isHomeUser() {
    	return this.availableServices.contains(EnumServiceType.HOME);
    }

    public FDReservation getReservation(){
    	Date now = new Date();
    	if(this.reservation != null){
    		if(reservation.getExpirationDateTime().before(now) || reservation.getExpirationDateTime().equals(now) ){
    			return null;
    		}
    	}

    	return this.reservation;
    }

    public void setReservation(FDReservation reservation){
    	this.reservation = reservation;
    }

	public boolean isChefsTable() throws FDResourceException {
	    if (this.identity == null) {
			return false;
		}
		FDCustomerModel customer = this.getFDCustomer();
		if (customer == null || customer.getProfile() == null) {
			return false;
		} else {
			return customer.getProfile().isChefsTable();
		}
	}

	public boolean isVoucherHolder() throws FDResourceException {
		return isVHInDelivery() || isVHOutOfDelivery();
	}
	
	public boolean isVHInDelivery() throws FDResourceException {
	    if (this.identity == null) {
			return false;
		}
		FDCustomerModel customer = this.getFDCustomer();
		if (customer == null || customer.getProfile() == null) {
			return false;
		} else {
			return customer.getProfile().isVHInDelivery();
		}
	}
	
	public boolean isVHOutOfDelivery() throws FDResourceException {
	    if (this.identity == null) {
			return false;
		}
		FDCustomerModel customer = this.getFDCustomer();
		if (customer == null || customer.getProfile() == null) {
			return false;
		} else {
			return customer.getProfile().isVHOutOfDelivery();
		}
	}
	
	public String getChefsTableInduction() throws FDResourceException {

		FDCustomerModel customer = this.getFDCustomer();
		if (customer == null || customer.getProfile() == null) {
			return "0";
		} else {
			return customer.getProfile().getChefsTableInduction();
		}
	}

	public String getWinback() throws FDResourceException {
		if (this.identity == null) {
			return "false";
		}
		FDCustomerModel customer = this.getFDCustomer();
		if (null == customer || null == customer.getProfile() || null == customer.getProfile().getWinback()) {
			return "false";
		} else {
			String value = customer.getProfile().getWinback().trim();
			if("".equals(value)) {
				return "false";
			}
			return value;
		}
	}

	public String getWinbackPath() throws FDResourceException {
		// winback path is in the form of "YYMMDD_winback_segment"
		String winback = getWinback();
		if(winback.equals("false"))
			return winback;

		StringTokenizer st = new StringTokenizer(winback, "_");
		int countTokens = st.countTokens();
		if (countTokens < 3)
			return "false";
		st.nextToken(); // date token which we don't need
		return FDStoreProperties.getWinbackRoot() + st.nextToken()+ "/" + st.nextToken() + ".html";
	}

	public String getMarketingPromoPath() throws FDResourceException {
		// marketingPromo path is in the form of "campaign_campaign2_segment"
		// a valid marketing promo value is in the form of "mktg_deli_default"		
		String mktgPromo = getMarketingPromo();
		if(mktgPromo.equals("false"))
			return mktgPromo;

		StringTokenizer st = new StringTokenizer(mktgPromo, "_");
		int countTokens = st.countTokens();
		if (countTokens < 3)
			return "false";
		return FDStoreProperties.getMarketingPromoRoot() + st.nextToken() + "/" + st.nextToken()+ "/" + st.nextToken() + ".html";
	}

	public String getMarketingPromo() throws FDResourceException {

		if (this.identity == null) {
			return "false";
		}
		FDCustomerModel customer = this.getFDCustomer();
		if (null == customer || null == customer.getProfile() || null == customer.getProfile().getMarketingPromo()) {
			return "false";
		} else {
			String value = customer.getProfile().getMarketingPromo().trim();
			if("".equals(value)) {
				return "false";
			}
			return value;
		}
	}


	public boolean isEligibleForPreReservation() throws FDResourceException {
	    if (this.identity == null) {
			return false;
		}
		FDCustomerModel customer = this.getFDCustomer();
		if (customer == null || customer.getProfile() == null) {
			return false;
		}
		ProfileModel p = customer.getProfile();
		return p.isVIPCustomer() || p.isChefsTable() || p.isCOSPilot();
	}

	public EnumServiceType getSelectedServiceType() {
		AddressModel address = this.shoppingCart.getDeliveryAddress();
		// FDX-2029 API - COS Delivery fee and order minimum used instead of FK
		if(userContext != null && userContext.getStoreContext() != null && EnumEStoreId.FDX.equals(userContext.getStoreContext().getEStoreId())) {
			return EnumServiceType.HOME;
		} else {
			return address != null  ? address.getServiceType() : this.selectedServiceType ;
		}
	}


	public EnumServiceType getUserServiceType(){
		return this.userServiceType != null ? this.userServiceType : (null !=getSelectedServiceType()?getSelectedServiceType():EnumServiceType.HOME);//EnumServiceType.HOME ;
	}

	public void setUserServiceType(EnumServiceType serviceType) {
		this.userServiceType = serviceType;
	}

	public EnumServiceType getZPServiceType(){
		return this.zpServiceType != null ? this.zpServiceType : EnumServiceType.HOME ;
	}

	public void setZPServiceType(EnumServiceType serviceType) {
		this.zpServiceType = serviceType;
	}
	
	public void setSelectedServiceType(EnumServiceType serviceType) {
		this.selectedServiceType = serviceType;
	}

	public void setAvailableServices(Set availableServices) {
		this.availableServices = Collections.unmodifiableSet(availableServices);
	}
	
	@Override
        public boolean hasServiceBasedOnUserAddress(EnumServiceType type) {
            try {
                return type != null && getUserServicesBasedOnAddresses().contains(type);
            } catch (FDResourceException e) {
                throw new FDRuntimeException(e);
            }
        }

	public String getCustomerServiceContact() {
		try {
			String state="";
			String contactNumber="1-212-796-8002";//DEFAULT
			if (this.isChefsTable()) {
				contactNumber= "1-866-511-1240";
			}else{
				state= extractStateFromAddress();
				if("PA".equalsIgnoreCase(state)){
					contactNumber= "1-215-825-5726";
				}
			}
			return contactNumber;
			
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
	}

	private String extractStateFromAddress() {
		String state="";
		if (this.getShoppingCart() != null && this.getShoppingCart().getDeliveryAddress() != null) {
			state= this.getShoppingCart().getDeliveryAddress().getState();
		} else if (this.getState() != null) {
			state= this.getState();
		} 
		return state;
	}
	
	public String getCustomerServiceContactMediaPath() {

		try {
			String state="";
			String contactMedia="/media/editorial/site_pages/contact_serivce_number.html";//DEFAULT
			if (this.isChefsTable()) {
				contactMedia= "/media/editorial/site_pages/chef_contact_serivce_number.html";
			}else{
				state= extractStateFromAddress();
				if("PA".equalsIgnoreCase(state)){
					contactMedia= "/media/editorial/site_pages/contact_serivce_number_PA.html";
				}
			}
			return contactMedia;
			
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
		
	}


	/**
	 * Returns the appropriate customer service email address
	 *
	 * @return serviceEmail email address
	 */
	public String getCustomerServiceEmail() throws FDResourceException {
		String serviceEmail = SERVICE_EMAIL;
		if (isDepotUser()){
			serviceEmail = FDDeliveryManager.getInstance().getCustomerServiceEmail(getDepotCode());
		} else if(isCorporateUser()){
			serviceEmail = "corporateservice@freshdirect.com";
		}
		if (isChefsTable()){
		    serviceEmail = FDStoreProperties.getChefsTableEmail();
		}
		return serviceEmail;
	}


	public boolean isCheckEligible()  {
		//return true;
		if (checkEligible == null) {
			EligibilityCalculator calc = new EligibilityCalculator("ECHECK");
			checkEligible = Boolean.valueOf(calc.isEligible(new FDRulesContextImpl(this)));
		}
		return checkEligible.booleanValue();
    }

	public Collection<ErpPaymentMethodI> getPaymentMethods() {
		try {
			return FDCustomerManager.getPaymentMethods(this.identity);
		} catch (FDResourceException e) {
			return new ArrayList<ErpPaymentMethodI>(); // empty list
		}
	}

	public String getUserId() {
		try {
			// load user id 'user@host.com' lazily
			if (this.identity != null && (userId == null || "".equals(userId))) {
				ErpCustomerModel model = FDCustomerFactory.getErpCustomer(this.identity);
				userId = (model != null) ? model.getUserId() : "";
			}
		} catch (FDResourceException e) {
			userId =  ""; // empty string
		}
		return userId;
	}

	public String getLastRefTrackingCode() {
		return this.lastRefTrackingCode;
	}

	public void setLastRefTrackingCode (String lastRefTrackingCode) {
		this.lastRefTrackingCode = lastRefTrackingCode;
	}

	public boolean isReferrerRestricted() throws FDResourceException {
	    if (this.identity == null) {
			return false;
		}
		return FDCustomerManager.isReferrerRestricted(this.identity);
	}

	public boolean isReferrerEligible() throws FDResourceException {
		if (referrerEligible == null) {
			EligibilityCalculator calc = new EligibilityCalculator("REFERRER");
			referrerEligible = Boolean.valueOf(calc.isEligible(new FDRulesContextImpl(this)));
		}
		return referrerEligible.booleanValue();
	}
	
	public boolean isECheckRestricted() throws FDResourceException {
	    if (this.identity == null) {
			return false;
		}
		return FDCustomerManager.isECheckRestricted(this.identity);
	}

	public String getRegRefTrackingCode() {
		try {
			if (identity != null && regRefTrackingCode == null) {
				ErpCustomerModel model = FDCustomerFactory.getErpCustomer(this.identity);
				regRefTrackingCode = (model != null) ? model.getCustomerInfo().getRegRefTrackingCode() : "";
			}
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
		return regRefTrackingCode;
	}

	public String getDefaultCounty() throws FDResourceException{
		String county = null;

		//if user is pickup user default county = 'PICKUP'
		if(EnumServiceType.PICKUP.equals(this.getSelectedServiceType()) || EnumServiceType.DEPOT.equals(this.getSelectedServiceType())){
			county = EnumServiceType.PICKUP.getName();
		}

		//check address on cart, recognize user handles all the complex logic with defaultAddresses
		if(county == null && this.shoppingCart != null){
			county = FDDeliveryManager.getInstance().getCounty(this.getShoppingCart().getDeliveryAddress());
		}

		//if we got nothing so far return county of zipcode on zipcheck
		if(this.getZipCode() != null && (county == null || "".equals(county))){
			county = FDDeliveryManager.getInstance().lookupCountyByZip(this.getZipCode());
		}

		return county;
	}
	
	public String getDefaultState() throws FDResourceException{
		String state = null;		

		//check address on cart, recognize user handles all the complex logic with defaultAddresses
		if(this.shoppingCart != null && this.shoppingCart.getDeliveryAddress()!=null){
			state = NVL.apply(this.shoppingCart.getDeliveryAddress().getState(), "");
		}

		//if we got nothing so far return state of zipcode on zipcheck
		if(this.getZipCode() != null && (state == null || "".equals(state))){
			state = FDDeliveryManager.getInstance().lookupStateByZip(this.getZipCode());
		}

		return state;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return this.active;
	}

	public boolean isReceiveFDEmails(){
		return this.receiveFDemails;
	}

	public void setReceiveFDEmails(boolean receiveFDEmails) {
		this.receiveFDemails = receiveFDEmails;
	}

	/**
	 * @return Returns the deliveryPassStatus.
	 */
	public EnumDlvPassStatus getDeliveryPassStatus() {
		if(dlvPassInfo != null){
			return dlvPassInfo.getStatus();
		}
		//Return Default value;
		return EnumDlvPassStatus.NONE;

	}
	public boolean isDlvPassNone(){
		return (dlvPassInfo == null) || (EnumDlvPassStatus.NONE.equals(dlvPassInfo.getStatus()));
	}

	public boolean isDlvPassActive(){
		if(EnumEStoreId.FDX.equals(this.getUserContext().getStoreContext().getEStoreId())) {
			return false;
		}
		if(dlvPassInfo == null){
			return false;
		}
		if(!dlvPassInfo.isUnlimited()){
			//BSGS Pass
			/* 2nd Condition happens only for BSGS pass.
			 * Let say user places order A using a last delivery of a BSGS pass. The pass
			 * goes to expired pending. The next day the user modifies the order still the BSGS
			 * pass should be applied even if the status is expired pending.
			 * Thats why this.getShoppingCart().isDlvPassAlreadyApplied() check is made.
			 */
			return (EnumDlvPassStatus.ACTIVE.equals(dlvPassInfo.getStatus()) ||
					(this.isDlvPassExpiredPending() && this.getShoppingCart().isDlvPassAlreadyApplied()));
		}
		//Unlimited Pass.
		if(EnumDlvPassStatus.ACTIVE.equals(dlvPassInfo.getStatus())) {
			Date today = new Date();
			return today.before(dlvPassInfo.getExpDate());
		} else {
			return false;
		}

	}

	public boolean isDlvPassExpired(){
		if(dlvPassInfo == null){
			return false;
		}
		if(!dlvPassInfo.isUnlimited()){
			//BSGS Pass
			return EnumDlvPassStatus.EXPIRED.equals(dlvPassInfo.getStatus()) ;
		}
		//Unlimited Pass.
		if(EnumDlvPassStatus.EXPIRED.equals(dlvPassInfo.getStatus())) {
			return true;
		}else{
			//Safe Check - Go by expiration date.
			if(null==dlvPassInfo.getExpDate()) {
				return false;
			}
			Date today = new Date();
			return today.after(dlvPassInfo.getExpDate());
		}
	}

	public boolean isDlvPassPending(){
		if(dlvPassInfo == null){
			return false;
		}
		return EnumDlvPassStatus.PENDING.equals(dlvPassInfo.getStatus());

	}

	public boolean isDlvPassExpiredPending(){
		if(dlvPassInfo == null){
			return false;
		}
		return EnumDlvPassStatus.EXPIRED_PENDING.equals(dlvPassInfo.getStatus());

	}

	public boolean isDlvPassCancelled(){
		if(dlvPassInfo == null){
			return false;
		}
		return (EnumDlvPassStatus.CANCELLED.equals(dlvPassInfo.getStatus())  ||
				EnumDlvPassStatus.ORDER_CANCELLED.equals(dlvPassInfo.getStatus()));
	}

	public boolean isDlvPassReturned(){
		if(dlvPassInfo == null){
			return false;
		}
		return EnumDlvPassStatus.PASS_RETURNED.equals(dlvPassInfo.getStatus());
	}

	public boolean isDlvPassShortShipped(){
		if(dlvPassInfo == null){
			return false;
		}
		return EnumDlvPassStatus.SHORT_SHIPPED.equals(dlvPassInfo.getStatus());
	}

	public boolean isDlvPassSettlementFailed(){
		if(dlvPassInfo == null){
			return false;
		}
		return EnumDlvPassStatus.SETTLEMENT_FAILED.equals(dlvPassInfo.getStatus());
	}

	public void performDlvPassStatusCheck()  throws FDResourceException {
		if (this.isDlvPassActive()){
			if(!(this.getShoppingCart().isChargeWaived(EnumChargeType.DELIVERY))){
				//If delivery promotion was applied, do not reapply the waiving of dlv charge.
				this.getShoppingCart().setChargeWaived(EnumChargeType.DELIVERY,true, DlvPassConstants.PROMO_CODE,  this.isWaiveDPFuelSurCharge(false));
				this.getShoppingCart().setDlvPassApplied(true);
			}
		} else if ((this.getShoppingCart() instanceof FDModifyCartModel)&&(this.getDlvPassInfo().isUnlimited())) {

			String dpId=((FDModifyCartModel)this.getShoppingCart()).getOriginalOrder().getDeliveryPassId();
			if(dpId!=null && !dpId.equals("")) {
				List passes=FDCustomerManager.getDeliveryPassesByStatus(this.getIdentity(), EnumDlvPassStatus.ACTIVE);
				DeliveryPassModel dlvPass=null;
				Date today = new Date();
				for(int i=0;i<passes.size();i++) {
					dlvPass=(DeliveryPassModel)passes.get(i);

					if(today.after(dlvPass.getExpirationDate()) && EnumDlvPassStatus.ACTIVE.equals(dlvPass.getStatus()) &&dlvPass.getId().equals(dpId)){
						this.getShoppingCart().setChargeWaived(EnumChargeType.DELIVERY,true, DlvPassConstants.PROMO_CODE,  this.isWaiveDPFuelSurCharge(false));
						this.getShoppingCart().setDlvPassApplied(true);
						this.getShoppingCart().setDlvPassPremiumAllowedTC(dlvPass.getPurchaseDate().after(FDStoreProperties.getDlvPassNewTCDate()));
						break;

					}
				}
			}
		}
	}

	public boolean isEligibleForDeliveryPass() throws FDResourceException {
		EnumDlvPassProfileType profileType=getEligibleDeliveryPass();
		if(profileType.equals(EnumDlvPassProfileType.NOT_ELIGIBLE))
			return false;
		return true;

	}

	public  EnumDlvPassProfileType getEligibleDeliveryPass() throws FDResourceException {
	    if (this.identity != null) {
			FDCustomerModel customer = this.getFDCustomer();
			String customerPK=customer.getErpCustomerPK();

			if (customer != null && customer.getProfile() != null) {
				ProfileModel p = customer.getProfile();
				String profileValue = p.getDeliveryPass();
				if((profileValue==null)||(profileValue!=null && profileValue.trim().equals(""))) {
					//return EnumDlvPassProfileType.NOT_ELIGIBLE;
					return EnumDlvPassProfileType.UNLIMITED;
				}
				if(isEligibleForBSGS(profileValue,customerPK)) {
					return EnumDlvPassProfileType.BSGS;
				}

				if(profileValue != null && profileValue.trim().indexOf(FDStoreProperties.getUnlimitedAmazonPrimeProfile()) != -1) {
					if(isEligibleForAmazonPrime(profileValue.trim(),customerPK))
						return EnumDlvPassProfileType.AMAZON_PRIME;
					else
						return EnumDlvPassProfileType.UNLIMITED;
				}
				if(profileValue != null && profileValue.trim().indexOf(FDStoreProperties.getUnlimitedPromotionalProfile()) != -1) {

					if(isEligibleForPromotionalProfile(profileValue.trim(),customerPK))
						return EnumDlvPassProfileType.PROMOTIONAL_UNLIMITED;
					else
						return EnumDlvPassProfileType.UNLIMITED;
				}
				if(profileValue != null && profileValue.trim().indexOf(FDStoreProperties.getUnlimitedProfilePosfix()) != -1)
					return EnumDlvPassProfileType.UNLIMITED;
			}
		}
		//return EnumDlvPassProfileType.NOT_ELIGIBLE;
	    return EnumDlvPassProfileType.UNLIMITED;
	}

	private boolean isEligibleForBSGS(String profileValue, String customerID) throws FDResourceException {

		boolean isEligible=false;
		if(profileValue != null && profileValue.trim().indexOf(FDStoreProperties.getBSGSProfilePosfix()) != -1) {
			isEligible=true;
		}
		return isEligible;
	}

	private boolean isEligibleForAmazonPrime(String profileValue, String customerID) throws FDResourceException {

		boolean isEligible=false;
		if(profileValue != null && profileValue.trim().indexOf(FDStoreProperties.getUnlimitedAmazonPrimeProfile()) != -1) {
			isEligible=!FDCustomerManager.hasPurchasedPass(customerID);//,getDeliveryPassType(FDStoreProperties.getUnlimitedAmazonPrimeProfile()).getCode()
		}
		return isEligible;
	}

	private boolean isEligibleForPromotionalProfile(String profileValue, String customerID) throws FDResourceException {

		boolean isEligible=false;
		if(profileValue != null && profileValue.trim().indexOf(FDStoreProperties.getUnlimitedPromotionalProfile()) != -1) {
			isEligible=!FDCustomerManager.hasPurchasedPass(customerID);
		}
		return isEligible;
	}

	public String getDlvPassProfileValue() throws FDResourceException {
	    if (this.identity != null) {
			FDCustomerModel customer = this.getFDCustomer();
			if (customer != null && customer.getProfile() != null) {
				ProfileModel p = customer.getProfile();
				if(p.getDeliveryPass() != null )
					return p.getDeliveryPass();
			}
		}
		return "";
	}

	public void updateDlvPassInfo() throws FDResourceException {
		try{
			FDUserDlvPassInfo dpInfo = FDCustomerManager.getDeliveryPassInfo(this);
			this.setDlvPassInfo(dpInfo);
		}catch(FDResourceException fe){
			throw fe;
		}
	}

	public void setLastRefProgramId(String progId) {
		this.lastRefProgId=progId;
	}

	public String getLastRefProgId() {
		return this.lastRefProgId;
	}

	public void setLastRefTrkDtls(String trkDtls) {
		this.lastRefTrkDtls=trkDtls;
	}

	public String getLastRefTrkDtls() {
		return this.lastRefTrkDtls;
	}

    public void setLastRefProgInvtId (String progInvtId)
    {
    	this.lastRefProgInvtId=progInvtId;
    }


	public ErpCustomerInfoModel getCustomerInfoModel() throws FDResourceException {
		if(identity == null) {
			return null;
		} else {
			if(customerInfoModel == null) {
				customerInfoModel = FDCustomerFactory.getErpCustomerInfo(identity);
			}
			return customerInfoModel;
		}
	}
	
    public void resetCustomerInfoModel() throws FDResourceException{
    	customerInfoModel = FDCustomerFactory.getErpCustomerInfo(identity);
    }

	
	public String getLastRefProgInvtId()
	{
		return this.lastRefProgInvtId;
	}

	public double getBaseDeliveryFee() {
		return BASE_DELIVERY_FEE;
	}

	public double getMinCorpOrderAmount() {
		return MIN_CORP_ORDER_AMOUNT; 
	}

	public double getCorpDeliveryFee() {
		return CORP_DELIVERY_FEE;
	}

	public double getCorpDeliveryFeeMonday() {
		return CORP_DELIVERY_FEE_MONDAY;
	}

	public int getUsableDeliveryPassCount() {
		if(dlvPassInfo!=null)
			return dlvPassInfo.getUsablePassCount();
		else
			return 0;
	}


	public boolean isProduceRatingEnabled() {
		return true;
	}
	
	public boolean isGiftCardsEnabled() {
		return true;
	}


	public boolean isCCLEnabled() {
		return true;
	}

	public boolean isCCLInExperienced() {
		return CclUtils.isCCLInExperienced(this, getCustomerCreatedListInfos());
	}



	// -- DYF --- //


	public boolean isDYFEnabled() {
		return true;
	}

	//Zone Pricing
	public boolean isZonePricingEnabled() {
		return true;
	}
	
	public EnumDPAutoRenewalType hasAutoRenewDP() throws FDResourceException {
	    if (this.identity != null) {
			FDCustomerModel customer = this.getFDCustomer();
			String customerPK=customer.getErpCustomerPK();

			return FDCustomerManager.hasAutoRenewDP(customerPK);
	    }
	    return EnumDPAutoRenewalType.NONE;
	}

	public AssignedCustomerParam getAssignedCustomerParam(String promoId) {
		if(assignedCustomerParams != null) {
			return (AssignedCustomerParam)this.assignedCustomerParams.get(promoId);
		}
		return null;
	}

	@Override
	public List<FDCustomerListInfo> getCustomerCreatedListInfos() {
		if (getLevel() == FDUserI.GUEST) {
			// We don't have an identity
			return null;
		}
		if (cclListInfos == null) {
			try {
				cclListInfos = FDListManager.getCustomerCreatedListInfos(this);
			} catch (Exception e) {
				throw new FDRuntimeException(e);
			}
		}
		return cclListInfos;
	}

	@Override
	public List<FDCustomerListInfo> getStandingOrderListInfos() {
		if (getLevel() == FDUserI.GUEST) {
			// We don't have an identity
			return null;
		}
		if (soListInfos == null) {
			try {
				soListInfos = FDListManager.getStandingOrderListInfos(this);
			} catch (Exception e) {
				throw new FDRuntimeException(e);
			}
		}
		return soListInfos;
	}


	public void setAssignedCustomerParams(Map<String, AssignedCustomerParam> assignedCustomerParams) {
		this.assignedCustomerParams = assignedCustomerParams;
	}

	public DCPDPromoProductCache getDCPDPromoProductCache(){
		if(this.dcpdPromoProductCache == null){
			this.dcpdPromoProductCache = new DCPDPromoProductCache();
		}
		return dcpdPromoProductCache;
	}

	public boolean isHomePageLetterVisited() {
		return isHomePageLetterVisited;
	}

	public void setHomePageLetterVisited(boolean isHomePageLetterVisited) {
		this.isHomePageLetterVisited = isHomePageLetterVisited;
	}

	public boolean isCampaignMsgLimitViewed() {
		if(getCampaignMsgViewed() >= FDStoreProperties.getImpressionLimit())
			return true;
		return false;
	}

	public int getCampaignMsgViewed() {
		return campaignMsgViewed;
	}

	public void setCampaignMsgViewed(int campaignMsgViewed) {
		this.campaignMsgViewed = campaignMsgViewed;
	}
	/**
	 * Returns user's cohort ID
	 *
	 * @param user
	 * @return
	 */
	public String getCohortName() {
		return this.cohortName;
	}

	public void setCohortName(String cohortName) {
		this.cohortName = cohortName;
	}

	public void createCohortName() throws FDResourceException {
		this.cohortName = CohortSelector.getInstance().getCohortName(getPrimaryKey());
		FDCustomerManager.storeCohortName(this);
	}
	
	public int getTotalCartSkuQuantity(String args[]){
		Collection<String> c = Arrays.asList(args);
		Set<String> argSet = new HashSet<String>(c);
        if(args==null) {
                    //System.out.println("** args :"+args);
                    return 0;
        }

        if(this.shoppingCart==null || this.shoppingCart.getOrderLines()==null) return 0;
            int count=0;
                    for (Iterator j = this.shoppingCart.getOrderLines().iterator(); j.hasNext();) {
                                FDCartLineI line = (FDCartLineI) j.next();
                                for(Iterator<String> i=argSet.iterator();i.hasNext();)
                                {
                                            String sku=i.next();
                                            if (sku.equals(line.getSkuCode()))
                                            {
                                                        count += line.getQuantity();
                                            }
                                }
           }

        return count;

	}

	public Map getPromoVariantMap(){
		if (this.promoVariantMap==null) {
			this.updateUserState();
		}
		return this.promoVariantMap;
	}

	public void setPromoVariantMap(Map pvMap) {
		this.promoVariantMap = pvMap;
	}

	public PromoVariantModel getPromoVariant(String variantId) {
		return (PromoVariantModel) this.getPromoVariantMap().get(variantId);
	}

	public String getSavingsVariantId() {
		return savingsVariantId;
	}
	public void setSavingsVariantId(String savingsVariantId) {
		this.savingsVariantId = savingsVariantId;
	}

	public boolean isSavingsVariantFound() {
		return savingsVariantFound;
	}

	public void setSavingsVariantFound(boolean savingsVariantFound) {
		this.savingsVariantFound = savingsVariantFound;
	}


	/**
	 * @return Always returns null
	 * @see com.freshdirect.fdstore.customer.FDUserI#getFavoriteTabFeature()
	 */
	public String getFavoriteTabFeature() {
		return null;
	}

	/**
	 * Calling this function has no effect (only FDSessionUser implements it)
	 *
	 * @param feature ignored
	 * @see com.freshdirect.fdstore.customer.FDUserI#setFavoriteTabFeature(java.lang.String)
	 */
	public void setFavoriteTabFeature(String feature) {
		// has no effect
	}

	public boolean isPostPromoConflictEnabled() {
		return isPostPromoConflictEnabled;
	}

	public void setPostPromoConflictEnabled(boolean isPostPromoConflictEnabled) {
		this.isPostPromoConflictEnabled = isPostPromoConflictEnabled;
	}

	public boolean isPromoConflictResolutionApplied() {
		return isPromoConflictResolutionApplied;
	}

	public void setPromoConflictResolutionApplied(
			boolean isPromoConflictResolutionApplied) {
		this.isPromoConflictResolutionApplied = isPromoConflictResolutionApplied;
	}

	public FDGiftCardInfoList getGiftCardList() {
		if (getLevel() == FDUserI.GUEST) {
			// We don't have an identity 
			return null;
		}
		if (cachedGiftCards == null) {
			try {
				cachedGiftCards = FDCustomerManager.getGiftCards(identity);
//				getGCRecipientInfo();
				
			} catch (Exception e) {
				throw new FDRuntimeException(e);
			}
		}
		return cachedGiftCards;
	}

	private void getGCRecipientInfo() throws FDResourceException {
		if(null != cachedGiftCards && null != cachedGiftCards.getGiftcards() && !cachedGiftCards.getGiftcards().isEmpty()){
			List<String> saleIds = new ArrayList<String>();
			for (Iterator iterator = cachedGiftCards.getGiftcards().iterator(); iterator
					.hasNext();) {
				FDGiftCardModel lFDGiftCardModel = (FDGiftCardModel) iterator.next();
				if(!saleIds.contains(lFDGiftCardModel.getGiftCardModel().getPurchaseSaleId())){
					saleIds.add(lFDGiftCardModel.getGiftCardModel().getPurchaseSaleId());
				}
				
			}
			if(!saleIds.isEmpty()){
				//Get the recipient info for all these distinct saleIds.
				cachedRecipientInfo = FDCustomerManager.getGiftCardRecepientsForOrders(saleIds);
			}
		}
	}
	
	public String getGCSenderName(String certNum, String saleId){
		if (getLevel() == FDUserI.GUEST) {
			// We don't have an identity 
			return null;
		}
		ErpGCDlvInformationHolder holder = null;
		try {
			if (null == cachedRecipientInfo){
				cachedRecipientInfo = new ConcurrentHashMap<String, ErpGCDlvInformationHolder>();
			}

			if (null !=cachedRecipientInfo){
				if(cachedRecipientInfo.containsKey(certNum)){
					 holder = cachedRecipientInfo.get(certNum);
				}else{
					holder = FDCustomerManager.GetGiftCardRecipentByCertNum(certNum);
					cachedRecipientInfo.put(certNum, holder);				
				}
				if(null != holder){
					return holder.getRecepientModel().getSenderName();
				}
			}
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
		return null;
	}
	
	public void invalidateGiftCards() {
		this.cachedGiftCards = null;
	}
	
	public double getGiftcardBalance() {
		if(this.getGiftCardList() == null) return 0.0;
		if(this.getShoppingCart() instanceof FDModifyCartModel) {
			return this.getGiftCardList().getTotalBalance();
		} else {
			//Clear all hold amounts.
			if(null!=this.getGiftCardList()){
				this.getGiftCardList().clearAllHoldAmount();
				return this.getGiftCardList().getTotalBalance();
			}
			return 0;
		}
		
	}
	
	public FDCartModel getGiftCart() {
		return this.dummyCart;
	}
	
	public void setGiftCart(FDCartModel dcart) {
		this.dummyCart = dcart;
	}
	
	public FDRecipientList getRecipientList(){
		if( null == this.recipientList) {
			this.recipientList = new FDRecipientList();
		}
		return this.recipientList;
	}
	
	public void setRecipientList(FDRecipientList r) {
		this.recipientList = r;
	}
	
    public boolean isGCOrderMinimumMet() {
		double subTotal = this.getRecipientList().getSubtotal(null);
		return subTotal >= this.getMinimumOrderAmount();
    }
    
    public double getGCMinimumOrderAmount() {
		return MIN_GC_ORDER_AMOUNT;
	}
    

	public FDBulkRecipientList getBulkRecipentList(){
		if( null == this.bulkRecipientList) {
			this.bulkRecipientList = new FDBulkRecipientList();
		}
		return this.bulkRecipientList;
	}
	
	public void setBulkRecipientList(FDBulkRecipientList r) {
		this.bulkRecipientList = r;
	}
    
	public Integer getDonationTotalQuantity(){
		return donationTotalQuantity;
	}
	
	public void setDonationTotalQuantity(Integer donationTotalQuantity){
		this.donationTotalQuantity = donationTotalQuantity;
	}

	
	public FDCartModel getDonationCart() {		
		return donationCart;
	}

	
	public void setDonationCart(FDCartModel dcart) {
		this.donationCart = dcart;		
	}

	public double getGiftcardsTotalBalance(){
		if(this.getGiftCardList() == null) return 0.0;
		if(this.getShoppingCart() instanceof FDModifyCartModel) {
			return this.getGiftCardList().getGiftcardsTotalBalance();
		} else {
			//Clear all hold amounts.
			if(null!=this.getGiftCardList()){
				this.getGiftCardList().clearAllHoldAmount();
				return this.getGiftCardList().getGiftcardsTotalBalance();
			}
			return 0;
		}
	}
	
	public int getTotalRegularOrderCount() throws FDResourceException {
		return this.getOrderHistory().getTotalRegularOrderCount();
	}

	public Collection<EnumServiceType> getUserServicesBasedOnAddresses() throws FDResourceException {
		if (servicesBasedOnAddress==null) {
		    ErpCustomerModel erpCustomer = FDCustomerFactory.getErpCustomer(getIdentity().getErpCustomerPK());	     
		    List<ErpAddressModel> shipToAddresses = erpCustomer.getShipToAddresses();
		    servicesBasedOnAddress = new HashSet<EnumServiceType> ();
		    for (ErpAddressModel m : shipToAddresses) {
		        servicesBasedOnAddress.add(m.getServiceType());
		    }
		}
	    return servicesBasedOnAddress;
	}


	// Profile key
	private static String PROFILE_SO_KEY = "so.enabled"; 
	

	/**
	 * Ensures StandingOrder feature is enabled for the customer
	 * 
	 * 1. Check personal availability in profile attributes
	 * 2. Check global availability in fdstore.properties
	 * 
	 */
	protected boolean isSOEnabled() {
		// Check personal flag in user profile
		try {
			boolean isEnabledInProfile = Boolean.valueOf(getFDCustomer().getProfile().getAttribute(PROFILE_SO_KEY)).booleanValue();
			
			if (isEnabledInProfile) {
				LOGGER.debug("SO enabled in customer profile");
				return true;
			}
		} catch (FDResourceException e) {
		}
		
		// Check global flag
		final boolean standingOrdersEnabled = FDStoreProperties.isStandingOrdersEnabled();
		LOGGER.debug("Standing Orders " + (standingOrdersEnabled ? "" : "NOT") + " enabled globally");
		return standingOrdersEnabled;
	}


	/**
	 * Ensures customer is eligible for StandingOrder feature.
	 * 
	 * 1. Check if so is enabled (globally or for the user)
	 * 2. Check if customer satisfies every requirement.
	 * 
	 */
	@Override
	public boolean isEligibleForStandingOrders() {
		if (isSOEligible == null) {
			isSOEligible = Boolean.FALSE;

			if (isSOEnabled()) {
				isSOEligible = hasCorporateOrder() || isCorporateUser();
			}
		}

		//LOGGER.debug("Customer eligible for SO: " + isSOEligible);

		return isSOEligible.booleanValue();
	}

	private Boolean hasCorporateOrder() {
		try {
			FDOrderHistory h = (FDOrderHistory) getOrderHistory();
			
			// System.err.println("order info: " + h.getFDOrderInfos().size() + " == total " + h.getTotalOrderCount());
			for (FDOrderInfoI i : h.getFDOrderInfos()) {
				//LOGGER.debug("Sale ID=" + i.getErpSalesId() + "; DLV TYPE=" + i.getDeliveryType() + "; SO ID=" + i.getStandingOrderId());
				if (EnumDeliveryType.CORPORATE.equals( i.getDeliveryType() ) || i.getStandingOrderId() != null ) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		} catch (FDResourceException e) {
			LOGGER.error("Order info crashed; exc="+e);
			return null;
		}
	}

	@Override
	public boolean isEligibleForClientCodes() {
		if (cliCodeEligible == null)
			cliCodeEligible = FDStoreProperties.isClientCodesEnabled() && hasCorporateOrder();

		//LOGGER.debug("Customer eligible for Client Codes: " + cliCodeEligible);
		
		return cliCodeEligible != null ? cliCodeEligible : false;
	}
	
	@Override
	public FDStandingOrder getCurrentStandingOrder() {
		throw new IllegalArgumentException( "Calling getCurrentStandingOrder() in FDUser is not allowed." );
	}

	@Override
	public EnumCheckoutMode getCheckoutMode() {
		return EnumCheckoutMode.NORMAL;
	}

	/** use getUserContext().getPricingContext().getZoneId() instead */
	@Deprecated
	public String getPricingZoneId(){
		 return "";
	}
	
	/** use getUserContext().getPricingContext() instead */
	@Deprecated
	public PricingContext getPricingContext() {
		return getUserContext().getPricingContext();
	}

	public UserContext getUserContext(){
		try {
			
			if(userContext == null){
				userContext = new UserContext();
				
		StoreContext storeContext = StoreContext.createStoreContext(EnumEStoreId.valueOfContentId((ContentFactory.getInstance().getStoreKey().getId())));
				userContext.setStoreContext(storeContext); //TODO this should be changed once FDX_CMS is merged!!! also check StoreContext.createDefault()
				
				
				
				
				userContext.setFdIdentity(getIdentity()); //TODO maybe FDIdentity should be removed from FDUser	
				ErpAddressModel address=null;
				if(identity!=null)
					address=getFulfillmentAddress(identity,storeContext.getEStoreId());
				else if(this.getAddress()!=null) 
					address=new ErpAddressModel(this.getAddress());
				
				userContext=setFulfillmentAndPricingContext(userContext,address);
			}
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e, e.getMessage());
		}
		
		
		return userContext;
	}
	
	
    private boolean isAddressValidForFulfillmentCheck(ErpAddressModel address) {
    	
    	return (address!=null && !StringUtil.isEmpty(address.getZipCode()))?true:false;
    }
    
    private FulfillmentInfo getFulfillmentInfo(ErpAddressModel address) {
    	
    	try {
			FDDeliveryZoneInfo deliveryZoneInfo=FDDeliveryManager.getInstance().getZoneInfo(address, today(), getHistoricOrderSize(), this.getRegionSvcType(address.getId()));
			if(deliveryZoneInfo!=null)
				return deliveryZoneInfo.getFulfillmentInfo();
			
		} catch (FDInvalidAddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FDResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    
	private UserContext setFulfillmentAndPricingContext( UserContext userContext,ErpAddressModel address) throws FDResourceException {                                  
			                                            
		
		FulfillmentContext fulfillmentContext= new FulfillmentContext();
		if(this.getZipCode() != null && FDStoreProperties.isAlcoholRestrictionByContextEnabled()) {
			//[APPDEV-2857] Blocking Alcohol for customers outside of Alcohol Delivery Area
			boolean alcoholRestrictedByContext=FDUserUtil.isAlcoholRestricted(this.getZipCode());
			fulfillmentContext.setAlcoholRestricted(alcoholRestrictedByContext);					
		}
		
		String pricingZoneId=FDZoneInfoManager.findZoneId(getZPServiceType().getName(), address!=null?address.getZipCode():getZipCode());
		
		FulfillmentInfo fulfillmentInfo=null;
		ZoneInfo zoneInfo=null;
		
		if(isAddressValidForFulfillmentCheck(address)) {
			fulfillmentInfo=getFulfillmentInfo(address);
		}
		
		if(fulfillmentInfo==null) {
				//default the fulfillments
				if(EnumEStoreId.FDX.equals(userContext.getStoreContext().getEStoreId())) {
					fulfillmentContext.setPlantId("1300");
					zoneInfo=new ZoneInfo(pricingZoneId,"1300","01",ZoneInfo.PricingIndicator.BASE, new ZoneInfo(pricingZoneId,"0001","01"));
				} else {
					fulfillmentContext.setPlantId("1000");
					zoneInfo=new ZoneInfo(pricingZoneId,"0001","01");
				}
				
		 } else {
			 	fulfillmentContext.setPlantId(fulfillmentInfo.getPlantCode());
				zoneInfo=getZoneInfo(pricingZoneId,fulfillmentInfo.getSalesArea());
		 }
		userContext.setFulfillmentContext(fulfillmentContext);
		userContext.setPricingContext(new PricingContext(zoneInfo));
		return userContext;
	}	

	/** use getUserContext().setPricingContext() instead */
	@Deprecated
	protected void setPricingContext(PricingContext pricingContext) {
		getUserContext().setPricingContext(pricingContext);
	}

	/** use getUserContext().resetPricingContext() instead */
	@Deprecated
	public void resetPricingContext(){
		getUserContext().resetPricingContext();
	}
	
	/** Added for Junit testing.<br>Use getUserContext().setDefaultPricingContext() instead */
	@Deprecated
	public void setDefaultPricingContext() {
		getUserContext().setDefaultPricingContext();
	}
	
	public String constructZoneIdForQueryString(){
		String zoneIdParam = "";
		try {
			String zoneId = FDZoneInfoManager.findZoneId(getZPServiceType().getName(), getZipCode());
			if(zoneId.equalsIgnoreCase(ZonePriceListing.MASTER_DEFAULT_ZONE)){
				zoneIdParam = "zonelevel=true && mzid="+zoneId;
			}else if(zoneId.equalsIgnoreCase(ZonePriceListing.RESIDENTIAL_DEFAULT_ZONE)||zoneId.equalsIgnoreCase(ZonePriceListing.CORPORATE_DEFAULT_ZONE)){
				zoneIdParam = "zonelevel=true && szid="+zoneId+"mzid="+ZonePriceListing.MASTER_DEFAULT_ZONE;
			}else{
				zoneIdParam = "zonelevel=true && zid="+zoneId;
				zoneId = FDZoneInfoManager.findZoneId(getZPServiceType().getName(),null);
				zoneIdParam = zoneIdParam + "&& szid="+zoneId+"mzid="+ZonePriceListing.MASTER_DEFAULT_ZONE;
			}
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e.getMessage());
		}
		return zoneIdParam;
	}
	
	@Override
	public SortedSet<IgnoreCaseString> getClientCodesHistory() {
		if (clientCodesHistory == null) {
			clientCodesHistory = new TreeSet<IgnoreCaseString>();
			try {
				clientCodesHistory.addAll(FDCustomerManager.getOrderClientCodesForUser(getIdentity()));
			} catch (FDResourceException e) {
				LOGGER.warn("unable to retrieve order client codes for: " + getIdentity(), e);
			}
			clientCodesHistory.addAll(getCartClientCodes());
		}
		return clientCodesHistory;
	}

	private Collection<? extends IgnoreCaseString> getCartClientCodes() {
		Set<IgnoreCaseString> ccs = new HashSet<IgnoreCaseString>();
		for (FDCartLineI ol : getShoppingCart().getOrderLines())
			for (ErpClientCode cc : ol.getClientCodes())
				ccs.add(new IgnoreCaseString(cc.getClientCode()));
		return ccs;
	}

	public Set<String> getAllAppliedPromos() {
		return allAppliedPromos;
	}

	public void addPromoErrorCode(String promoCode, int errorCode) {
		promoErrorCodes.put(promoCode, errorCode);
	}

	public int getPromoErrorCode(String promoCode) {
		Integer code = this.promoErrorCodes.get(promoCode);
		if(code == null) return 0;
		return code;
	}
	
	public void clearPromoErrorCodes(){
		this.promoErrorCodes.clear();
	}

	public void clearAllAppliedPromos(){
		this.allAppliedPromos.clear();
	}
	
	@Override
	public void setMasqueradeContext(MasqueradeContext ctx) {
		this.masqueradeContext = ctx;
	}
	
	@Override
	public MasqueradeContext getMasqueradeContext() {
		return masqueradeContext;
	}
	
		@Deprecated
		@Override
        public EnumWinePrice getPreferredWinePrice() {
	    if (identity == null) {
	        return EnumWinePrice.ONE;
	    }
            if (preferredWinePrice == null) {
                preferredWinePrice = DatabaseScoreFactorProvider.getInstance().getPreferredWinePrice(identity.getErpCustomerPK());
                if (preferredWinePrice == null) {
                    preferredWinePrice = EnumWinePrice.ONE;
                }
            }
            return preferredWinePrice;
        }

	public int getTotalCTSlots(){
		return ctSlots;
	}	
	public void setTotalCTSlots(int slots){
		this.ctSlots = slots;
	}
	
	public double getPercSlotsSold() {
		return percSlotsSold;
	}

	public void setPercSlotsSold(double percSlotsSold) {
		this.percSlotsSold = percSlotsSold;
	}

	@Override
	public String getGreeting() throws FDResourceException {
		StoreModel store = (StoreModel) ContentFactory.getInstance().getContentNode("Store", "FreshDirect");
		FDSurvey customerProfileSurvey = FDSurveyFactory.getInstance().getSurvey(EnumSurveyType.CUSTOMER_PROFILE_SURVEY, EnumServiceType.HOME);
		FDSurveyResponse surveyResponse= FDSurveyFactory.getCustomerProfileSurveyInfo(getIdentity(), EnumServiceType.HOME);
		if (customerProfileSurvey != null && surveyResponse != null) {
		    String[] answers = surveyResponse.getAnswer("Occasions_Events");
		    if (answers != null && answers.length != 0) {
		    	boolean found = false;
		    	for (String answer : answers)
		    		if ("birthdays".equals(answer)) {
		    			found = true;
		    			break;
		    		}
		    	if (found) {
					String[] birthday = surveyResponse.getAnswer(FDSurveyConstants.BIRTHDAY);
					if (birthday != null && birthday.length == 2)
						try {
							String month = birthday[0];
							int day = Integer.parseInt(birthday[1]);
							Calendar cal = Calendar.getInstance();
							Date now = new Date();
							cal.setTime(now);
							String thisMonth = new SimpleDateFormat("MMM").format(now);
							if (thisMonth.equalsIgnoreCase(month) && cal.get(Calendar.DAY_OF_MONTH) == day)
								return "Happy birthday, " + getFirstName() + "!";
						} catch (NumberFormatException e) {
						}
		    	}
		    }
		}
		MyFD myfd = null;
		if (store != null) {
			myfd = store.getMyFD();
			if (myfd != null) {
				List<HolidayGreeting> greetings = myfd.getHolidayGreetings();
				Date now = new Date();
				for (HolidayGreeting greeting : greetings) {
					Date start = greeting.getIntervalStartDate();
					Date end = greeting.getIntervalEndDate();
					if (start == null || end == null)
						continue;
					if (now.before(start) || now.after(end))
						continue;
				    if (customerProfileSurvey == null || surveyResponse == null)
				    	continue;
					// SELECT a.name
					// FROM CUST.survey_qa qa
					// INNER JOIN CUST.survey_question q
					// ON qa.question = q.id
					// INNER JOIN cust.survey_answer a
					// ON qa.answer = a.id
					// WHERE q.name = 'Occasions_Events';
				    String[] answers = surveyResponse.getAnswer("Occasions_Events");
				    if (answers == null || answers.length == 0)
				    	continue;
				    boolean found = false;
				    String code = greeting.getCode();
				    for (String a : answers)
				    	if (a != null && a.equals(code)) {
				    		found = true;
				    		break;
				    	}
				    if (!found) {
				    	continue;
				    }
					String text = greeting.getGreetingText();
					text = text.replace("%firstname", getFirstName());
					return text;
				}
			}
		}
		return "Hello " + getFirstName() + "!";
	}

	@Override
	public Date getRegistrationDate() {
		if (registrationDate == null) {
			if (getIdentity() != null && getIdentity().getErpCustomerPK() != null) {
				ErpActivityRecord template = new ErpActivityRecord();
				template.setCustomerId(getIdentity().getErpCustomerPK());
				template.setActivityType(EnumAccountActivityType.CREATE_ACCOUNT);
				try {
					Collection<ErpActivityRecord> results = ActivityLog.getInstance().findActivityByTemplate(template);
					if (results.size() > 0) {
						registrationDate = results.iterator().next().getDate();
					} else {
						registrationDate = EPOCH;
					}
				} catch (FDResourceException e) {
					registrationDate = EPOCH;
				}
			} else {
				registrationDate = EPOCH;
			}
		}
		if (registrationDate != EPOCH) {
			return (Date) registrationDate;
		} else {
			return null;
		}
        }
	@Override
	public SessionEvent getSessionEvent() {
		return event;
	}

	@Override
	public void setSessionEvent(SessionEvent event) {
		this.event = event;
	}
	
    public boolean getShowPendingOrderOverlay() {
        boolean showOverlay = false;
        boolean isAddOnOrder = getMasqueradeContext() != null && getMasqueradeContext().isAddOnOrderEnabled();
        if (!isAddOnOrder) {
            showOverlay = FDStoreProperties.isPendingOrderPopupEnabled()
                    && (FDStoreProperties.isPendingOrderPopupMocked() || (this.showPendingOrderOverlay && !this.suspendShowPendingOrderOverlay))
                    && (getMasqueradeContext() == null || (getMasqueradeContext() != null && getMasqueradeContext().isEmptyMakeGoodOrderLineIdQuantities()));
        }
        return showOverlay;
    }

	public void setShowPendingOrderOverlay(boolean showPendingOrderOverlay) {
		this.showPendingOrderOverlay = showPendingOrderOverlay;
	}
	
	@Override
	public void setSuspendShowPendingOrderOverlay(boolean suspendShowPendingOrderOverlay) {
		this.suspendShowPendingOrderOverlay = suspendShowPendingOrderOverlay;
	}
	
	@Override
	public boolean isSupendShowPendingOrderOverlay() {
		return this.suspendShowPendingOrderOverlay;
	}
	
	/* check if user has a valid (regular) pending order (so exclude all other types) */
	public boolean hasPendingOrder() throws FDResourceException {
		return hasPendingOrder(false, false);
	}
	
	/* check if user has a valid pending order. inclusions optional. */
	public boolean hasPendingOrder(boolean incGiftCardOrds, boolean incDonationOrds) throws FDResourceException {		
		List<FDOrderInfoI> orderHistoryInfo = new ArrayList<FDOrderInfoI>(getPendingOrders(incGiftCardOrds, incDonationOrds, true));
		
		return (orderHistoryInfo.size() > 0) ? true : false;
	}

	/* return List of orderInfos for all pending orders (regular orders only), sorted. */
	public List<FDOrderInfoI> getPendingOrders() throws FDResourceException {
		return getPendingOrders(false, false, true);
	}
	
	/* return List of orderInfos for all pending orders inclusions optional. */
	public List<FDOrderInfoI> getPendingOrders(boolean incGiftCardOrds, boolean incDonationOrds, boolean sorted) throws FDResourceException {
		
		FDOrderHistory history = (FDOrderHistory) getOrderHistory();//Changed to fetch from cache.  
		EnumEStoreId eStore=getUserContext().getStoreContext().getEStoreId();
		Date currentDate = new Date();
		List<FDOrderInfoI> orderHistoryInfo = new ArrayList<FDOrderInfoI>(history.getFDOrderInfos(EnumSaleType.REGULAR,eStore));
		
		if (incGiftCardOrds) {
			//Add gift cards orders too.
			orderHistoryInfo.addAll(history.getFDOrderInfos(EnumSaleType.GIFTCARD,eStore));
		}

		if (incDonationOrds) {
			//ADD Donation Orders too-for Robin Hood.
			orderHistoryInfo.addAll(history.getFDOrderInfos(EnumSaleType.DONATION,eStore));
		}
		
		List<FDOrderInfoI> validPendingOrders = new ArrayList<FDOrderInfoI> ();

		//LOGGER.debug("getPendingOrders(incGiftCardOrds:"+incGiftCardOrds+", incDonationOrds:"+incDonationOrds+", sorted:"+sorted+")");
		for (Iterator<FDOrderInfoI> hIter = orderHistoryInfo.iterator(); hIter.hasNext(); ) {
			FDOrderInfoI orderInfo = hIter.next();
			

			//LOGGER.debug("SaleId:"+orderInfo.getErpSalesId()+", getOrderStatus:"+orderInfo.getOrderStatus().toString()
					//+", isModifiable:"+orderInfo.isModifiable()+", getDeliveryCutoffTime:"+orderInfo.getDeliveryCutoffTime());
            
			if (orderInfo.isModifiable()) {
				
				if (orderInfo.getOrderStatus() == EnumSaleStatus.REFUSED_ORDER )
					continue;
        	   
				/*
					if we wanted individual types of GC or donation orders, use this code
	        	    	String ordDeliveryType = orderInfo.getDeliveryType().toString();
	        	    	
	        	    	//gift cards
						String gcCodePersonal = EnumDeliveryType.GIFT_CARD_PERSONAL.getCode();
						String gcCodeCorporate = EnumDeliveryType.GIFT_CARD_CORPORATE.getCode();
						
						if ((ordDeliveryType).equals(gcCodePersonal))
							continue;
						if ((ordDeliveryType).equals(gcCodeCorporate))
							continue;
						
	        	    	//robin hood
						String donatePersonal = EnumDeliveryType.DONATION_INDIVIDUAL.getCode();
						String donateCorporate = EnumDeliveryType.DONATION_BUSINESS.getCode();
						
						if ((ordDeliveryType).equals(donatePersonal))
							continue;
						if ((ordDeliveryType).equals(donateCorporate))
							continue;
				 */

				if (currentDate.before(orderInfo.getDeliveryCutoffTime())) {
					validPendingOrders.add(orderInfo);
				}
			}else if(orderInfo.isNewOrder() && EnumSaleType.REGULAR.equals(orderInfo.getSaleType())){
				this.setAnyNewOrder(true);
			}
		}
		
		if (sorted) {
			Collections.sort(orderHistoryInfo, ORDER_COMPARATOR);
		}
		
		return validPendingOrders;
	}
	
	/** Sorts orders by dlv. start time, descending */
	private final static Comparator<FDOrderInfoI> ORDER_COMPARATOR = new Comparator<FDOrderInfoI>() {
		public int compare(FDOrderInfoI o1, FDOrderInfoI o2) {
			return (o2).getRequestedDate().compareTo((o1).getRequestedDate());
		}
	};
	
	public FDCartModel getMergePendCart() {
		return (this.mergePendCart == null) ? this.mergePendCart = new FDCartModel() : this.mergePendCart;
	}
	
	public void setMergePendCart(FDCartModel mergePendCart) {
		if ( mergePendCart == null || mergePendCart.getClass().isAssignableFrom(getMergePendCart().getClass()) ) {
			this.mergePendCart = mergePendCart;
		}
	}

	
	public void setReferralLink() {
		try {
			this.referralLink = FDReferralManager.getReferralLink(this
					.getIdentity().getErpCustomerPK());
		} catch (FDResourceException e) {
			LOGGER.error("Exception getting referralLink", e);
		};
	}

	public String getReferralLink() {
		if(referralLink == null)
			setReferralLink();
		return referralLink;
	}

	public void setReferralPrgmId(String referralPrgmId) {
		this.referralPrgmId = referralPrgmId;
	}

	public String getReferralPrgmId() {
		return referralPrgmId;
	}

	public void setReferralCustomerId(String referralCustomerId) {
		this.referralCustomerId = referralCustomerId;
	}

	public String getReferralCustomerId() {
		return referralCustomerId;
	}

	public double getAvailableCredit() {
		if (totalCredit == null)
			setAvailableCredit();
		return totalCredit.doubleValue();
	}

	public void setAvailableCredit() {
		try {
			totalCredit = FDReferralManager.getAvailableCredit(this
					.getIdentity().getErpCustomerPK());
		} catch (FDResourceException e) {
			LOGGER.error("Exception getting totalCredit", e);
		}
	}

	public boolean isReferralProgramAvailable() {
		if (referralFlag == null)
			setReferralPromoAvailable();
		return referralFlag.booleanValue();
	}

	public void setReferralPromoAvailable() {
		try {			
			referralFlag = FDReferralManager.getReferralDisplayFlag(this.getIdentity().getErpCustomerPK());
			LOGGER.debug("Getting ref display for :" + this.getIdentity().getErpCustomerPK() + "-and flag is:" + referralFlag);
		} catch (FDResourceException e) {
			LOGGER.error("Exception getting totalCredit", e);
		}
	}
	
	public void setReferralPromotionFraud(boolean fraud) {
		this.referralFraud = fraud;
	}
	
	public boolean isReferralPromotionFraud() {
		return referralFraud;
	}
	
	
	public boolean isEligibleForDDPP() throws FDResourceException {
		if(null == identity){
			return false;
		}
		return this.getFDCustomer().isEligibleForDDPP();
	}
	
    @Override
    public boolean isPopUpPendingOrderOverlay() {
        boolean showOverlay = false;
        try {
            showOverlay = getShowPendingOrderOverlay() && getLevel() >= RECOGNIZED && (getMasqueradeContext() == null || (getMasqueradeContext() != null && getMasqueradeContext().isEmptyMakeGoodOrderLineIdQuantities()))
                    && (hasPendingOrder() || FDStoreProperties.isPendingOrderPopupMocked());
        } catch (FDResourceException e) {
            LOGGER.debug("a really unexpected and really unnecessarily delegated exception", e);
        }
        return showOverlay;
    }
	
	public EnumGiftCardType getGiftCardType() {
		return giftCardType;
	}

	public void setGiftCardType(EnumGiftCardType giftCardType) {
		this.giftCardType = giftCardType;
	}

	public boolean isEbtAccepted() {
		return ebtAccepted;
	}
	
	public void setEbtAccepted(boolean ebtAccepted) {
		this.ebtAccepted = ebtAccepted;
	}

	public boolean isDpNewTcBlocking() {
		return isDpNewTcBlocking(true);
	}
	
	public boolean isDpNewTcBlocking(boolean includeViewCount) {
		boolean isBlocking = false;
		
		//guestAllowed pages will have a null identity
		if (identity == null) { return isBlocking; }
		
		try {
			ErpCustomerInfoModel cm = FDCustomerFactory.getErpCustomerInfo(identity);

			int dpTcViewCount = cm.getDpTcViewCount();
			Date dpTcAgreeDate = cm.getDpTcAgreeDate();
			Date dpNewTcStartDate = FDStoreProperties.getDlvPassNewTCDate();
			Calendar calNow = Calendar.getInstance();
			Calendar calNewTcStart = Calendar.getInstance();
				calNewTcStart.setTime(dpNewTcStartDate);
			Calendar calAgree = null;
			if (dpTcAgreeDate != null) {
				calAgree = Calendar.getInstance();
				calAgree.setTime(dpTcAgreeDate);
			}
			
			if (this.isDlvPassActive() && calNewTcStart.getTime().after(this.getDlvPassInfo().getPurchaseDate())) { //exclude users with no pass, and ones that purchased after new terms start
	    		if (calNow.getTime().after(dpNewTcStartDate)) { //check that new terms should be in effect
		    		if ( dpTcAgreeDate == null || ( calAgree != null && calAgree.getTime().before(dpNewTcStartDate) ) ) { //either never agreed, or agree before new terms
			    		if (dpTcViewCount < FDStoreProperties.getDpTcViewLimit() || Boolean.FALSE.equals(includeViewCount)) { //check view count
			    			isBlocking = true;
			    		}
		    		}
	    		}
			}
		} catch (FDResourceException e) {
			LOGGER.error("Error checking isDpNewTcBlocking in FDUser.",e);
		}
		
		return isBlocking;
	}
	public boolean isWaiveDPFuelSurCharge(boolean includeViewCount) {
		boolean isBlocking = false;
		
		//guestAllowed pages will have a null identity
		if (identity == null) { return isBlocking; }
		
		try {
			ErpCustomerInfoModel cm = FDCustomerFactory.getErpCustomerInfo(identity);

			
			int dpTcViewCount = cm.getDpTcViewCount();
			Date dpTcAgreeDate = cm.getDpTcAgreeDate();
			Date dpNewTcStartDate = FDStoreProperties.getDlvPassNewTCDate();
			Calendar calNow = Calendar.getInstance();
			Calendar calNewTcStart = Calendar.getInstance();
				calNewTcStart.setTime(dpNewTcStartDate);
			Calendar calAgree = null;
			if (dpTcAgreeDate != null) {
				calAgree = Calendar.getInstance();
				calAgree.setTime(dpTcAgreeDate);
			}
			
			if (this.isDlvPassActive()) { //exclude users with no pass, and ones that purchased after new terms start
	    		if (calNow.getTime().after(dpNewTcStartDate)) { //check that new terms should be in effect
		    		if (((this.getDlvPassInfo().getPurchaseDate()!=null && this.getDlvPassInfo().getPurchaseDate().before(calNewTcStart.getTime())) 
		    				&& calAgree ==null) ||( calAgree != null && calAgree.getTime().before(dpNewTcStartDate))) { //either never agreed, or agree before new terms
			    		if (dpTcViewCount < FDStoreProperties.getDpTcViewLimit() || Boolean.FALSE.equals(includeViewCount)) { //check view count
			    			isBlocking = true;
			    		}
		    		}
	    		}
			}
		} catch (FDResourceException e) {
			LOGGER.error("Error checking isDpNewTcBlocking in FDUser.",e);
		}
		
		return isBlocking;
	}
	
	
	
	
	public boolean hasEBTAlert() {
		if (hasEBTAlert == null) {
			hasEBTAlert = Boolean.FALSE;

			try {
				if (null !=getIdentity() && FDCustomerManager.isOnAlert(getIdentity().getErpCustomerPK(), EnumAlertType.EBT.getName())) {
					hasEBTAlert = Boolean.TRUE;;
				}
			} catch (FDResourceException e) {
				LOGGER.error("Error checking hasEBTAlert in FDUser.",e);
			}
		}

		LOGGER.debug("Customer has an EBT Alert: " + hasEBTAlert);

		return hasEBTAlert.booleanValue();
	}

	public Set<String> getSteeringSlotIds() {
		return steeringSlotIds;
	}

	public void setSteeringSlotIds(Set<String> steeringSlotIds) {
		this.steeringSlotIds = steeringSlotIds;
	}

	@Override
	public synchronized List<ErpAddressModel> getAllHomeAddresses() throws FDResourceException {
		if (identity!=null && cachedAllHomeAddresses==null){
			cachedAllHomeAddresses = getAllAddressesForServiceType(EnumServiceType.HOME);
		}
		return cachedAllHomeAddresses; 
	}

	@Override
	public synchronized List<ErpAddressModel> getAllCorporateAddresses() throws FDResourceException {
		if (identity!=null && cachedAllCorporateAddresses==null){
			cachedAllCorporateAddresses = getAllAddressesForServiceType(EnumServiceType.CORPORATE);
		}
		return cachedAllCorporateAddresses; 
	}

	private List<ErpAddressModel> getAllAddressesForServiceType(EnumServiceType serviceType) throws FDResourceException {
		List<ErpAddressModel> addresses = new ArrayList<ErpAddressModel>();
		
		for (ErpAddressModel erpAddress : FDCustomerFactory.getErpCustomer(identity.getErpCustomerPK()).getShipToAddresses()){
			if (erpAddress!=null && serviceType.equals(erpAddress.getServiceType())) {
				addresses.add(erpAddress);
			}
		}
		return addresses;
	}
	
	@Override
	public synchronized void invalidateAllAddressesCaches() {
		cachedAllHomeAddresses = null;
		cachedAllCorporateAddresses = null;
	}

	/** returns address used by location handler bar*/
	@Override
	public AddressModel getSelectedAddress() { 
		FDCartModel cart = getShoppingCart();
		
		if(cart!=null){
			AddressModel cartAddress = cart.getDeliveryAddress();
			if (cartAddress!=null){
				return cartAddress;
			}
		}
		return address;
	}

	@Override
	public EnumDeliveryStatus getDeliveryStatus() throws FDResourceException{
		FDDeliveryServiceSelectionResult serviceResult = null;
		
		AddressModel selectedAddress = getSelectedAddress();
		String address1 = selectedAddress.getAddress1();
		if (address1!=null && address1.length()>0){ //only check by address if necessary
			try {
				serviceResult = FDDeliveryManager.getInstance().getDeliveryServicesByAddress(selectedAddress);
			} catch (FDInvalidAddressException e) {
				//this should never occur as address has already been validated
				LOGGER.error("invalid address, fallback to zip code check",e);
			}
		}
		if (serviceResult==null){
			serviceResult = FDDeliveryManager.getInstance().getDeliveryServicesByZipCode(selectedAddress.getZipCode(), this.getUserContext().getStoreContext().getEStoreId());
		}
	
		return serviceResult.getServiceStatus(getUserServiceType());
	}

	public Set<ExternalCampaign> getExternalPromoCampaigns() {
		return externalPromoCampaigns;
	}

	public void setExternalPromoCampaigns(Set<ExternalCampaign> externalPromoCampaigns) {
		this.externalPromoCampaigns = externalPromoCampaigns;
	}

	@Override
	public void setExternalCampaign(ExternalCampaign externalCampaign) {
		this.externalCampaign = externalCampaign;
	}

	@Override
	public ExternalCampaign getExternalCampaign() {
		return externalCampaign;
	}	
	public FDCustomerCouponWallet getCouponWallet() {
		return couponWallet;
	}

	public void setCouponWallet(FDCustomerCouponWallet couponWallet) {
		this.couponWallet = couponWallet;
	}
	
	//Get Coupon Customer based on UPC
	public FDCustomerCoupon getCustomerCoupon(String upc, EnumCouponContext ctx) {			
		return FDUserCouponUtil.getCustomerCoupon(this,upc, ctx, couponWallet);
	}
	
	public FDCustomerCoupon getCustomerCoupon(FDProductInfo prodInfo, EnumCouponContext ctx,String catId,String prodId) {		
		return FDUserCouponUtil.getCustomerCoupon(this,prodInfo, ctx, catId, prodId, couponWallet);
	}

	public FDCustomerCoupon getCustomerCoupon(FDCartLineI cartLine, EnumCouponContext ctx) {
		return FDUserCouponUtil.getCustomerCoupon(this,cartLine, ctx, couponWallet);		
	}
	//Get Coupon Customer based on CartLine
	public FDCustomerCoupon getCustomerCoupon(FDCartLineI cartLine, EnumCouponContext ctx,String catId,String prodId) {
		return FDUserCouponUtil.getCustomerCoupon(this,cartLine, ctx, catId, prodId, couponWallet);
	}	
					
	public void updateClippedCoupon(String couponId){
		FDUserCouponUtil.updateClippedCoupon(couponId, couponWallet);
	}
	
	public boolean isEligibleForCoupons() throws FDResourceException {
		boolean isEligible = false;
		if(FDCouponProperties.isCouponsEnabled() ||( null != identity && this.getFDCustomer().isEligibleForCoupons())){
			isEligible =true;
		}
		return isEligible;
	}
	
	public boolean isCouponsSystemAvailable() throws FDResourceException {
		boolean isCouponsSystemAvailable = true;
		if(isEligibleForCoupons() && FDCouponProperties.isCouponsBlackHoleEnabled()){
			isCouponsSystemAvailable =false;
		}
		return isCouponsSystemAvailable;
	}

	public boolean isCouponEvaluationRequired() {
		return null !=getCouponWallet()?getCouponWallet().isCouponEvaluationRequired():false;
	}

	public void setCouponEvaluationRequired(boolean couponEvaluationRequired) {
		if(null !=getCouponWallet()){
			getCouponWallet().setCouponEvaluationRequired(couponEvaluationRequired);
		}
	}
	
	public boolean isRefreshCouponWalletRequired() {
		return null !=getCouponWallet()?getCouponWallet().isRefreshCouponWalletRequired():false;
	}
	
	public void setRefreshCouponWalletRequired(boolean refreshCouponWalletRequired) {
		if(null !=getCouponWallet()){
			getCouponWallet().setRefreshCouponWalletRequired(refreshCouponWalletRequired);
		}
	}	

	public boolean isRobot(){
		return robot;
	}
	
	public void setRobot(boolean robot){
		this.robot = robot;
	}

	@Override
	public String getDefaultListId() {
		return defaultListId;
	}

	@Override
	public void setDefaultListId( String listId ) {
		defaultListId = listId;
	}

	public EnumRegionServiceType getRegionSvcType(String addressId){
			
			if(this.getShoppingCart()!=null && this.getShoppingCart().getDeliveryReservation()!=null && this.getShoppingCart().getDeliveryReservation().getAddressId()!=null
					&& this.getShoppingCart().getDeliveryReservation().getAddressId().equals(addressId)){
	    		return this.getShoppingCart().getDeliveryReservation().getRegionSvcType();
	    	}else if(this.getReservation()!=null && this.getReservation().getAddressId()!=null && this.getReservation().getAddressId().equals(addressId)){
	    		return this.getReservation().getRegionSvcType();
	    	}else{
	    		return null;
	    	}
	}
		
	public boolean isPaymentechEnabled() {
		if(FDStoreProperties.isPaymentechGatewayEnabled())
			return true;
		try {
			if (getIdentity() != null && !StringUtil.isEmpty(getIdentity().getErpCustomerPK())) {
				return FDCustomerManager.isFeatureEnabled(getIdentity().getErpCustomerPK() , EnumSiteFeature.PAYMENTECH_GATEWAY);
			}
		} catch (FDResourceException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static FDUser createRobotUser(){
		FDUser robotUser = new FDUser(new PrimaryKey(ROBOT_USER_NAME));
        robotUser.setRobot(true);
        return robotUser;
	}

	@Override
	public double getMinHomeOrderAmount() {

    	if (getShoppingCart() != null && getShoppingCart().getDeliveryAddress() != null){
			try {
				String county = FDDeliveryManager.getInstance().getCounty(getShoppingCart().getDeliveryAddress());
				String zip = getShoppingCart().getDeliveryAddress().getZipCode();
				String zipcodes = FDStoreProperties.getSuffolkZips();
				if("SUFFOLK".equalsIgnoreCase(county) && (zipcodes.indexOf(zip)==-1) ){
					return 99;
				}
			} catch (FDResourceException e) {
				throw new FDRuntimeException(e);
			}
		}
		return MINIMUM_ORDER_AMOUNT;
	
	}

	public boolean isAnyNewOrder() {
		return anyNewOrder;
	}

	public void setAnyNewOrder(boolean anyNewOrder) {
		this.anyNewOrder = anyNewOrder;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * Calling this function always returns false (only FDSessionUser implements it)
	 */
	public boolean hasJustLoggedIn() {
		return false;
	}

	/**
	 * Calling this function always returns false (only FDSessionUser implements it)
	 */
	public boolean hasJustLoggedIn(boolean clear) {
		return false;
	}

	/**
	 * Calling this function has no effect (only FDSessionUser implements it)
	 */
	public void setJustLoggedIn(boolean val) {		
	}

	/**
	 * Calling this function always returns false (only FDSessionUser implements it)
	 */
	public boolean hasJustSignedUp() {
		return false;
	}

	/**
	 * Calling this function always returns false (only FDSessionUser implements it)
	 */
	public boolean hasJustSignedUp(boolean clear) {
		return false;
	}

	/**
	 * Calling this function always returns false (only FDSessionUser implements it)
	 */
	public void setJustSignedUp(boolean val) {
	}
	
	/**
	 * Calling this function always returns false (only FDSessionUser implements it)
	 */
	public boolean isRafFriendSignedUp() {
		return false;
	}
	
	/**
	 * Calling this function always returns false (only FDSessionUser implements it)
	 */
	public boolean isRafFriendSignedUp(boolean clear) {
		return false;
	}

	/**
	 * Calling this function always returns false (only FDSessionUser implements it)
	 */
	public void setRafFriendSignedUp(boolean val) {
	}

	public boolean isGlobalNavTutorialSeen() {
		return isGlobalNavTutorialSeen;
	}

	public void setGlobalNavTutorialSeen(boolean isGlobalNavTutorialSeen) {
		this.isGlobalNavTutorialSeen = isGlobalNavTutorialSeen;
	}
	

	public List<ProductReference> getProductSamples() {
		return this.productSamples;
	}

	public void setProductSample(List<ProductReference> productSamples) {
		this.productSamples = productSamples;
	}
	

	/* return List of orderInfos for all orders in En-route status. */
	public List<FDOrderInfoI> getScheduledOrdersForDelivery(boolean sorted) throws FDResourceException {
		LOGGER.debug("getScheduledOrdersForDelivery: " + sorted);
		
		FDOrderHistory history = (FDOrderHistory) getOrderHistory();//Changed to fetch from cache.
		List<FDOrderInfoI> orderHistoryInfo = new ArrayList<FDOrderInfoI>(history.getFDOrderInfos(EnumSaleType.REGULAR));
		
		List<FDOrderInfoI> validScheduledOrders = new ArrayList<FDOrderInfoI> ();
		if(orderHistoryInfo != null) {
			for (Iterator<FDOrderInfoI> hIter = orderHistoryInfo.iterator(); hIter.hasNext(); ) {
				FDOrderInfoI orderInfo = hIter.next();
				if (EnumSaleStatus.ENROUTE.equals(orderInfo.getOrderStatus())) {
					validScheduledOrders.add(orderInfo);
				}
			}
		}
		LOGGER.debug("Total Orders scheduled for delivery: " + validScheduledOrders.size());
		if (sorted && !validScheduledOrders.isEmpty()) {
			Collections.sort(validScheduledOrders, ORDER_DELIVERY_STARTTIME_COMPARATOR);
		}
		
		return validScheduledOrders;
	}
	
	/** Sorts orders by dlv. start time, ascending */
	private final static Comparator<FDOrderInfoI> ORDER_DELIVERY_STARTTIME_COMPARATOR = new Comparator<FDOrderInfoI>() {
		public int compare(FDOrderInfoI o1, FDOrderInfoI o2) {
			return (o1).getDeliveryStartTime().compareTo((o2).getDeliveryStartTime());
		}
	};

	@Override
	public boolean isProductSample(ProductReference prodRef) {
		List<ProductReference> productSamples =getProductSamples();
		if(null != productSamples && !productSamples.isEmpty())
			for(ProductReference prod :productSamples){
			if(null !=prod && prod.equals(prodRef)){
				return true;
			}			
		}
		return false;
	}

	private ErpAddressModel getFulfillmentAddress(FDIdentity identity,EnumEStoreId eStoreId) throws FDResourceException {
		
		ErpAddressModel address=this.getShoppingCart().getDeliveryAddress();
		if(address==null)
			address=FDCustomerManager.getLastOrderAddress(identity, eStoreId);
		
		if(EnumEStoreId.FDX.equals(eStoreId)&& address==null) {
				address=FDCustomerManager.getLastOrderAddress(identity, EnumEStoreId.FD);
		}
		return address;
	}
	
	private static Date today() {
		Date d = OncePerRequestDateCache.getToday();
		if(d == null){
			d = new Date();
		}
		return d;
	}
	
	private  static ZoneInfo getZoneInfo(String pricingZone,SalesArea salesArea) {
		if(salesArea.getDefaultSalesArea()!=null && salesArea.getDefaultSalesArea().getSalesOrg()!=null) {
			return new ZoneInfo(pricingZone, salesArea.getSalesOrg(),salesArea.getDistChannel(),ZoneInfo.PricingIndicator.BASE.getValue().equals(salesArea.getPricingIndicator())?ZoneInfo.PricingIndicator.BASE:ZoneInfo.PricingIndicator.SALE,getZoneInfo(pricingZone,salesArea.getDefaultSalesArea()));
		} else {
			return new ZoneInfo(pricingZone, salesArea.getSalesOrg(),salesArea.getDistChannel());
		}
	}
	@Override
	public void resetUserContext() {
		this.userContext=null;
	}

	@Override
	public boolean isCrmMode() {
		return crmMode;
	}
	
	@Override
	public void setCrmMode(boolean flag) {
		crmMode = flag;
	}

	@Override
	public boolean isVHPopupDisplay() {
		// TODO Auto-generated method stub
		return this.vHPopupDisplay;
	}

	@Override
	public void setVHPopupDisplay(boolean flag) {
           this.vHPopupDisplay=flag;
	}
	

	/**
	 * @return the rafClickId
	 */
	public String getRafClickId() {
		return rafClickId;
	}

	/**
	 * @param rafClickId the rafClickId to set
	 */
	public void setRafClickId(String rafClickId) {
		this.rafClickId = rafClickId;
	}

	/**
	 * @return the rafPromoCode
	 */
	public String getRafPromoCode() {
		return rafPromoCode;
	}

	/**
	 * @param rafPromoCode the rafPromoCode to set
	 */
	public void setRafPromoCode(String rafPromoCode) {
		this.rafPromoCode = rafPromoCode;
	}

	@Override
	public Date getFirstOrderDateByStore(EnumEStoreId eStoreId)
			throws FDResourceException {
		if(null == firstOrderDateByStore){
    		OrderHistoryI orderHistory = this.getOrderHistory();
    		if(null != orderHistory){
    			firstOrderDateByStore = orderHistory.getFirstOrderDateByStore(eStoreId);
    		}
    	}
    	return firstOrderDateByStore;
	}

	
}

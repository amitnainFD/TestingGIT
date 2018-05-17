package com.freshdirect.fdstore.customer;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.context.MasqueradeContext;
import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.customer.EnumTransactionSource;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpPaymentMethodI;
import com.freshdirect.customer.ErpPromotionHistory;
import com.freshdirect.customer.OrderHistoryI;
import com.freshdirect.deliverypass.EnumDPAutoRenewalType;
import com.freshdirect.deliverypass.EnumDlvPassProfileType;
import com.freshdirect.deliverypass.EnumDlvPassStatus;
import com.freshdirect.fdlogistics.model.FDReservation;
import com.freshdirect.fdstore.EnumCheckoutMode;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.EnumWinePrice;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.ProductReference;
import com.freshdirect.fdstore.deliverypass.FDUserDlvPassInfo;
import com.freshdirect.fdstore.ecoupon.EnumCouponContext;
import com.freshdirect.fdstore.ecoupon.FDCustomerCoupon;
import com.freshdirect.fdstore.ecoupon.model.FDCustomerCouponWallet;
import com.freshdirect.fdstore.giftcard.FDGiftCardInfoList;
import com.freshdirect.fdstore.lists.FDCustomerListInfo;
import com.freshdirect.fdstore.promotion.AssignedCustomerParam;
import com.freshdirect.fdstore.promotion.PromoVariantModel;
import com.freshdirect.fdstore.promotion.PromotionI;
import com.freshdirect.fdstore.promotion.SignupDiscountRule;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;
import com.freshdirect.fdstore.util.IgnoreCaseString;
import com.freshdirect.giftcard.EnumGiftCardType;
import com.freshdirect.logistics.analytics.model.SessionEvent;
import com.freshdirect.logistics.delivery.dto.CustomerAvgOrderSize;
import com.freshdirect.logistics.delivery.model.EnumDeliveryStatus;
import com.freshdirect.logistics.delivery.model.EnumRegionServiceType;


public interface FDUserI extends java.io.Serializable {


    /** order minimum (before taxes and promotions are applied) */
    public final static double MINIMUM_ORDER_AMOUNT = 30.00;
    public final static double FDX_MINIMUM_ORDER_AMOUNT = 20.00;
    public final static double MIN_CORP_ORDER_AMOUNT = 50.00;
   //TODO : need to check with Tiru
    public final static double FDX_MIN_ORDER_AMOUNT = 30.00;

    public final static double BASE_DELIVERY_FEE = 4.99; // Used for site text
    public final static double CORP_DELIVERY_FEE = 9.99; // Used for site text
    public final static double CORP_DELIVERY_FEE_MONDAY = 14.99;  // Used for site text
    public final static int CHEFS_TABLE_ORDER_COUNT_QUALIFIER = 12;
    public final static double CHEFS_TABLE_ORDER_TOTAL_QUALIFIER = 1500.00;
    public final static int CHEFS_TABLE_GETTING_CLOSE_COUNT = 5;
    public final static double CHEFS_TABLE_GETTING_CLOSE_TOTAL = 625.00;

    public final static double MIN_GC_ORDER_AMOUNT = 20.00;
    
	public final static int GUEST = 0;              // anonymously cookied user who have not registered
	public final static int RECOGNIZED = 1;         // cookied user who has registered and has a known identity
	public final static int SIGNED_IN = 2;          // cookied user who has registered, has a known identity and is currently signed in
	public final static boolean ZIPCHECK = true;

	public EnumTransactionSource getApplication();

    public String getCookie();

    public void setCookie(String cookie);

	public String getZipCode();

	public void setZipCode(String zipCode);

	public void setAddress(AddressModel a);

	public AddressModel getAddress();

	public FDIdentity getIdentity();

	public String getPrimaryKey();

	public void setIdentity(FDIdentity identity);

	public int getLevel();

	public boolean isInZone();
	
	public void setProductSample(List<ProductReference> list);
	
	public List<ProductReference> getProductSamples();
	
    public void isLoggedIn(boolean loggedId);

	public FDCartModel getShoppingCart();

	public void setShoppingCart(FDCartModel cart);

    public boolean isSurveySkipped();

    public void setSurveySkipped(boolean skipped);

	public boolean isFraudulent() throws FDResourceException;

	public FDPromotionEligibility getPromotionEligibility();

	/** @deprecated */
	@Deprecated
	public double getMaxSignupPromotion();

	public SignupDiscountRule getSignupDiscountRule();

	public boolean isPromotionAddressMismatch();

	public void setRedeemedPromotion(PromotionI promotion);

	public PromotionI getRedeemedPromotion();

	public void updateUserState();

	public String getFirstName() throws FDResourceException;
	public String getLastName() throws FDResourceException;

    public FDCustomerModel getFDCustomer() throws FDResourceException;

    public String getDepotCode();

	public void setDepotCode(String depotCode);

    public boolean isDepotUser();

    public boolean isCorporateUser();

	/**
	 * Invalidate cached order history & promotions.
	 */
	public void invalidateCache();

	public OrderHistoryI getOrderHistory() throws FDResourceException;
	
	public Date getFirstOrderDate() throws FDResourceException;
	
	public Date getFirstOrderDateByStore(EnumEStoreId eStoreId) throws FDResourceException;

	public int getOrderCountForChefsTableEligibility() throws FDResourceException;

	public String getOrderTotalForChefsTableEligibility() throws FDResourceException;

	public String getOrderCountRemainingForChefsTableEligibility() throws FDResourceException;

	public String getOrderTotalRemainingForChefsTableEligibility() throws FDResourceException;

	public boolean isCloseToCTEligibilityByOrderCount() throws FDResourceException;

	public boolean isCloseToCTEligibilityByOrderTotal() throws FDResourceException;

	public boolean isOkayToDisplayCTEligibility() throws FDResourceException;

	public boolean hasQualifiedForCT() throws FDResourceException;

	public String getEndChefsTableQualifyingDate() throws FDResourceException;

    public int getAdjustedValidOrderCount() throws FDResourceException;

    public int getValidPhoneOrderCount() throws FDResourceException;

	public boolean isEligibleForSignupPromotion();

	public PromotionI getEligibleSignupPromotion();

    public boolean isOrderMinimumMet() throws FDResourceException;
    
    public boolean isOrderMinimumMetWithoutWine() throws FDResourceException;    

    public double getMinimumOrderAmount();
    
	public boolean isOrderMinimumMet(boolean excludeBeer) throws FDResourceException;

	public float getQuantityMaximum(ProductModel product);

    public boolean isPickupOnly();

    public boolean isPickupUser();

    public boolean isNotServiceable();

    public boolean isDeliverableUser();

    public boolean isHomeUser();

    public boolean hasServiceBasedOnUserAddress(EnumServiceType type);
    
    
    public FDReservation getReservation();

    public void setReservation(FDReservation reservation);

	public boolean isChefsTable() throws FDResourceException;

	public String getChefsTableInduction() throws FDResourceException;
	
	public boolean isVHInDelivery() throws FDResourceException;
	
	public boolean isVHOutOfDelivery() throws FDResourceException;
	
	public boolean isVoucherHolder() throws FDResourceException;

	public String getWinback() throws FDResourceException;

	public String getWinbackPath() throws FDResourceException;

	public String getMarketingPromo() throws FDResourceException;

	public String getMarketingPromoPath() throws FDResourceException;

	public boolean isEligibleForPreReservation() throws FDResourceException;

	public EnumServiceType getSelectedServiceType();

	public void setSelectedServiceType(EnumServiceType serviceType);

	public String getCustomerServiceContact();
	
	public String getCustomerServiceContactMediaPath();

	public String getCustomerServiceEmail() throws FDResourceException;

	public boolean isCheckEligible();

	public Collection<ErpPaymentMethodI> getPaymentMethods();

	public String getUserId ();

	public void setLastRefTrackingCode (String lastRefTrackingCode);

	public String getLastRefTrackingCode();

	public void setLastRefProgramId (String progId);

	public String getLastRefProgId();

	public void setLastRefTrkDtls(String trkDtls);

	public String getLastRefTrkDtls();

    public void setLastRefProgInvtId (String progId);

	public String getLastRefProgInvtId();


	public boolean isReferrerRestricted() throws FDResourceException;

	public boolean isReferrerEligible() throws FDResourceException;

	public boolean isECheckRestricted() throws FDResourceException;



	public String getDefaultCounty() throws FDResourceException;
	
	public String getDefaultState() throws FDResourceException;

	public boolean isActive();

	public boolean isReceiveFDEmails();

	public boolean isDlvPassNone();

	public boolean isDlvPassActive();

	public boolean isDlvPassPending();

	public boolean isDlvPassExpiredPending();

	public boolean isDlvPassExpired();

	public boolean isDlvPassCancelled();

	public boolean isDlvPassReturned();

	public boolean isDlvPassShortShipped();

	public boolean isDlvPassSettlementFailed();

	public EnumDlvPassStatus getDeliveryPassStatus();

	public void performDlvPassStatusCheck()throws FDResourceException;

	public boolean isEligibleForDeliveryPass() throws FDResourceException;

	public EnumDlvPassProfileType getEligibleDeliveryPass() throws FDResourceException;

	public String getDlvPassProfileValue() throws FDResourceException;

	public FDUserDlvPassInfo getDlvPassInfo();

	public void updateDlvPassInfo() throws FDResourceException;

	public double getBaseDeliveryFee();

	public double getMinCorpOrderAmount();
	
	public double getMinHomeOrderAmount();

	public double getCorpDeliveryFee();

	public double getCorpDeliveryFeeMonday();

	public int getUsableDeliveryPassCount();

	public EnumDPAutoRenewalType hasAutoRenewDP() throws FDResourceException;

	public AssignedCustomerParam getAssignedCustomerParam(String promoId);

	public boolean isProduceRatingEnabled();

	public boolean isGiftCardsEnabled();

	/* CCL */
	public boolean isCCLEnabled();

	public boolean isCCLInExperienced();

	public List<FDCustomerListInfo> getCustomerCreatedListInfos();

	public List<FDCustomerListInfo> getStandingOrderListInfos();

	public DCPDPromoProductCache getDCPDPromoProductCache();

	public ErpPromotionHistory getPromotionHistory() throws FDResourceException;

	/* SmartStore DYF */
	public boolean isDYFEnabled();

	public boolean isHomePageLetterVisited();

	public boolean isCampaignMsgLimitViewed();

	public int getCampaignMsgViewed();

	public void setCampaignMsgViewed(int campaignMsgViewed);

    /*
     * This method was introduced as part of PERF-22 task.
     * Separate invalidation of Order History Cache from other caches.
     */
    public void invalidateOrderHistoryCache();

    public int getAdjustedValidECheckOrderCount()  throws FDResourceException;

    /*
     * This method was introduced as part of new COS-changes
     * This will return the service type of FDuser table entry
     */

    public EnumServiceType getUserServiceType();

    public void setHomePageLetterVisited(boolean isHomePageLetterVisited);

    public String getCohortName();

    public void setCohortName(String cohortName);

    public int getTotalCartSkuQuantity(String args[]);

    /**
     * @return the user's favorite site feature for a cart tab
     */
    public String getFavoriteTabFeature();

    /**
     * sets the user's favorite site feature for a cart tab
     *
     * @param feature
     */
    public void setFavoriteTabFeature(String feature);

    public Map getPromoVariantMap();

    public void setPromoVariantMap(Map pvMap);

	public String getSavingsVariantId();

	public void setSavingsVariantId(String savingsVariantId) ;

	public void setSavingsVariantFound(boolean savingsVariantFound) ;

	public boolean isSavingsVariantFound() ;

    public PromoVariantModel getPromoVariant(String variantId);

    public boolean isPostPromoConflictEnabled();

	public void setPromotionAddressMismatch(boolean b);

	public void setSignupDiscountRule(SignupDiscountRule discountRule);

	public void setPostPromoConflictEnabled(boolean isPostPromoConflictEnabled);

	public boolean isPromoConflictResolutionApplied();

	public void setPromoConflictResolutionApplied(boolean isPromoConflictResolutionApplied);
	
	public FDGiftCardInfoList getGiftCardList();
	
	public FDCartModel getGiftCart();
	
	public void setGiftCart(FDCartModel dcart);
	
	public FDRecipientList getRecipientList();
	
	public void setRecipientList(FDRecipientList r);
	
	public double getGiftcardBalance();
	
	public boolean isGCOrderMinimumMet();
	
	public double getGCMinimumOrderAmount();
	
	public void invalidateGiftCards();
	
	public FDBulkRecipientList getBulkRecipentList();
	
	
	public void setBulkRecipientList(FDBulkRecipientList r);

	public FDCartModel getDonationCart();
	
	public void setDonationCart(FDCartModel dcart);
	
	public Integer getDonationTotalQuantity();
	
	public void setDonationTotalQuantity(Integer donationTotalQuantity);
	
	public double getGiftcardsTotalBalance();
	
	public String getGCSenderName(String certNum, String custId);
	
	/** Is customer eligible for Standing Orders service? */
	public boolean isEligibleForStandingOrders();

	/** Is customer eligible for Client Codes service? */
	public boolean isEligibleForClientCodes();
	
	public FDStandingOrder getCurrentStandingOrder();
	
	/** Tells checkout controller the way of work */
	public EnumCheckoutMode getCheckoutMode();
	
	/** use getUserContext().getPricingContext().getZoneId() instead */
	@Deprecated
	public String getPricingZoneId();

	/** use getUserContext().getPricingContext() instead */
	@Deprecated
	public PricingContext getPricingContext();
	
	/** use getUserContext().resetPricingContext() instead */
	@Deprecated
	public void resetPricingContext();

	public UserContext getUserContext();
	
	public EnumServiceType getZPServiceType();

	public void setZPServiceType(EnumServiceType serviceType);
	
	public Set<String> getAllAppliedPromos();

	public SortedSet<IgnoreCaseString> getClientCodesHistory();
	public void clearAllAppliedPromos();
	
	public void addPromoErrorCode(String promoCode, int errorCode);

	public int getPromoErrorCode(String promoCode);
	
	public void clearPromoErrorCodes();
	
	public void setMasqueradeContext(MasqueradeContext ctx);
	
	public MasqueradeContext getMasqueradeContext();
	
	@Deprecated
	public EnumWinePrice getPreferredWinePrice();
	
	public String getGreeting() throws FDResourceException;
	
	public Date getRegistrationDate();

	public int getTotalCTSlots();
	public void setTotalCTSlots(int slots);
	public double getPercSlotsSold();
	public void setPercSlotsSold(double percSlotsSold);
	
	public SessionEvent getSessionEvent();

	public void setSessionEvent(SessionEvent event);

	//mergePendingOrder (APPDEV-2031)
	public boolean getShowPendingOrderOverlay();
	public void setShowPendingOrderOverlay(boolean showPendingOrderOverlay);
	// suspend showing pending order overlay
	public boolean isSupendShowPendingOrderOverlay();
	public void setSuspendShowPendingOrderOverlay(boolean suspendShowPendingOrderOverlay);
	
	public boolean hasPendingOrder() throws FDResourceException;
	public boolean hasPendingOrder(boolean incGiftCardOrds, boolean incDonationOrds) throws FDResourceException;
	public List<FDOrderInfoI> getPendingOrders() throws FDResourceException;
	public List<FDOrderInfoI> getPendingOrders(boolean incGiftCardOrds, boolean incDonationOrds, boolean sorted) throws FDResourceException;
	public FDCartModel getMergePendCart();
	public void setMergePendCart(FDCartModel mergePendCart);	
	
	public void setReferralLink();

	public String getReferralLink();

	public void setReferralPrgmId(String referralPrgmId);

	public String getReferralPrgmId();

	public void setReferralCustomerId(String referralCustomerId);

	public String getReferralCustomerId();
	
	public void setReferralPromoList();
	
	public List<PromotionI> getReferralPromoList();
	
	public double getAvailableCredit();
	
	public boolean isReferralProgramAvailable();
	
	public void setReferralPromotionFraud(boolean fraud);
	
	public boolean isReferralPromotionFraud();
	
	public String getTsaPromoCode();

	public void setTsaPromoCode(String tsaPromoCode);
	
	public boolean isPopUpPendingOrderOverlay();
		
	public EnumGiftCardType getGiftCardType();

	public void setGiftCardType(EnumGiftCardType giftCardType);
	
	public boolean isEbtAccepted();

	public void setEbtAccepted(boolean ebtAccepted);
	
	public boolean isDpNewTcBlocking();
	
	public boolean isDpNewTcBlocking(boolean includeViewCount);
	
	public boolean isWaiveDPFuelSurCharge(boolean includeViewCount);
		
	public boolean hasEBTAlert() ;
	
	public Set<String> getSteeringSlotIds();

	public void setSteeringSlotIds(Set<String> steeringSlotIds);

	public List<ErpAddressModel> getAllHomeAddresses() throws FDResourceException;
	
	public List<ErpAddressModel> getAllCorporateAddresses() throws FDResourceException;
	
    public void invalidateAllAddressesCaches();

    public AddressModel getSelectedAddress();
    
    public EnumDeliveryStatus getDeliveryStatus() throws FDResourceException;

	public Set<ExternalCampaign> getExternalPromoCampaigns();
	
	public void setExternalCampaign(ExternalCampaign campaign);
	
	public ExternalCampaign getExternalCampaign();

	public void setExternalPromoCampaigns(Set<ExternalCampaign> externalCampaigns);
	
	public boolean isRobot();
	public void setRobot(boolean robot);

	public FDCustomerCouponWallet getCouponWallet();
	public void setCouponWallet(FDCustomerCouponWallet couponWallet);
	
	public FDCustomerCoupon getCustomerCoupon(String upc, EnumCouponContext ctx);
	public FDCustomerCoupon getCustomerCoupon(FDCartLineI cartLine, EnumCouponContext ctx);
	
	public void updateClippedCoupon(String couponId);
	public boolean isEligibleForCoupons() throws FDResourceException ;
	public boolean isCouponsSystemAvailable() throws FDResourceException ;
	public FDCustomerCoupon getCustomerCoupon(FDProductInfo prodInfo, EnumCouponContext ctx,String catId,String prodId) ;
	public FDCustomerCoupon getCustomerCoupon(FDCartLineI cartLine, EnumCouponContext ctx,String catId,String prodId) ;
	public boolean isCouponEvaluationRequired();
	public void setCouponEvaluationRequired(boolean couponEvaluationRequired);
	public boolean isRefreshCouponWalletRequired() ;
	public void setRefreshCouponWalletRequired(boolean refreshCouponWalletRequired) ;	
	
	public String getDefaultListId();
	public void setDefaultListId( String listId );
	public CustomerAvgOrderSize getHistoricOrderSize() throws FDResourceException;
	public EnumRegionServiceType getRegionSvcType(String addressId);
	public boolean isPaymentechEnabled();
	public void applyOrderMinimum();
	public boolean isAnyNewOrder();
	public void setAnyNewOrder(boolean anyNewOrder);
	
	public boolean hasJustLoggedIn();
	public boolean hasJustLoggedIn(boolean clear);
	public void setJustLoggedIn(boolean val);
	
	public boolean hasJustSignedUp();
	public boolean hasJustSignedUp(boolean clear);
	public void setJustSignedUp(boolean val);
	public boolean isGlobalNavTutorialSeen();
    public void setGlobalNavTutorialSeen(boolean isGlobalNavTutorialSeen);
	public List<FDOrderInfoI> getScheduledOrdersForDelivery(boolean sorted) throws FDResourceException;
	//public OrderHistoryI getOrderHistory(EnumEStoreId eStore) throws FDResourceException;
	public void resetUserContext();

	public boolean isProductSample(ProductReference prodRef);


	/**
	 * Customer objects are treated with some restrictions in CRM.
	 * Set to true if customer lives in CRM
	 */
	public boolean isCrmMode();
	public void setCrmMode(boolean flag);
	public boolean getTcAcknowledge();
	
	public boolean isVHPopupDisplay();
	public void setVHPopupDisplay(boolean flag);
	

	
	public void setRafPromoCode(String rafPromoCode);
	public String getRafPromoCode();
	public void setRafClickId(String rafClickId);
	public String getRafClickId(); 
	
	
	
}

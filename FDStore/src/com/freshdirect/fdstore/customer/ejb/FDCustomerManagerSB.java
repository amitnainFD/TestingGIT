package com.freshdirect.fdstore.customer.ejb;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.ejb.EJBObject;

import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.crm.CrmAgentModel;
import com.freshdirect.crm.CrmAgentRole;
import com.freshdirect.crm.CrmClick2CallModel;
import com.freshdirect.crm.CrmSystemCaseInfo;
import com.freshdirect.customer.CustomerRatingI;
import com.freshdirect.customer.DlvSaleInfo;
import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.customer.EnumSaleType;
import com.freshdirect.customer.EnumTransactionSource;
import com.freshdirect.customer.ErpAbstractOrderModel;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpAddressVerificationException;
import com.freshdirect.customer.ErpAuthorizationException;
import com.freshdirect.customer.ErpAuthorizationModel;
import com.freshdirect.customer.ErpClientCodeReport;
import com.freshdirect.customer.ErpComplaintException;
import com.freshdirect.customer.ErpComplaintModel;
import com.freshdirect.customer.ErpCreateOrderModel;
import com.freshdirect.customer.ErpCustomerAlertModel;
import com.freshdirect.customer.ErpCustomerInfoModel;
import com.freshdirect.customer.ErpCustomerModel;
import com.freshdirect.customer.ErpDuplicateAddressException;
import com.freshdirect.customer.ErpDuplicateDisplayNameException;
import com.freshdirect.customer.ErpDuplicateUserIdException;
import com.freshdirect.customer.ErpFraudException;
import com.freshdirect.customer.ErpInvoiceModel;
import com.freshdirect.customer.ErpModifyOrderModel;
import com.freshdirect.customer.ErpOrderHistory;
import com.freshdirect.customer.ErpPaymentMethodException;
import com.freshdirect.customer.ErpPaymentMethodI;
import com.freshdirect.customer.ErpPromotionHistory;
import com.freshdirect.customer.ErpSaleModel;
import com.freshdirect.customer.ErpSaleNotFoundException;
import com.freshdirect.customer.ErpShippingInfo;
import com.freshdirect.customer.ErpTransactionException;
import com.freshdirect.customer.OrderHistoryI;
import com.freshdirect.delivery.ReservationException;
import com.freshdirect.deliverypass.DeliveryPassException;
import com.freshdirect.deliverypass.DeliveryPassModel;
import com.freshdirect.deliverypass.DlvPassUsageInfo;
import com.freshdirect.deliverypass.DlvPassUsageLine;
import com.freshdirect.deliverypass.EnumDlvPassStatus;
import com.freshdirect.fdstore.EnumEStoreId;

import com.freshdirect.fdlogistics.model.FDReservation;
import com.freshdirect.fdlogistics.model.FDTimeslot;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.URLRewriteRule;
import com.freshdirect.fdstore.atp.FDAvailabilityI;
import com.freshdirect.fdstore.customer.CustomerCreditModel;
import com.freshdirect.fdstore.customer.EnumIPhoneCaptureType;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.customer.FDAuthenticationException;
import com.freshdirect.fdstore.customer.FDCartonInfo;
import com.freshdirect.fdstore.customer.FDCustomerCreditHistoryModel;
import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.fdstore.customer.FDCustomerModel;
import com.freshdirect.fdstore.customer.FDCustomerOrderInfo;
import com.freshdirect.fdstore.customer.FDCustomerRequest;
import com.freshdirect.fdstore.customer.FDCustomerSearchCriteria;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.customer.FDOrderSearchCriteria;
import com.freshdirect.fdstore.customer.FDPaymentInadequateException;
import com.freshdirect.fdstore.customer.FDUser;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.customer.PasswordNotExpiredException;
import com.freshdirect.fdstore.customer.ProfileAttributeName;
import com.freshdirect.fdstore.customer.RegistrationResult;
import com.freshdirect.fdstore.customer.SavedRecipientModel;
import com.freshdirect.fdstore.customer.ejb.FDCustomerManagerSessionBean.ReservationInfo;
import com.freshdirect.fdstore.deliverypass.FDUserDlvPassInfo;
import com.freshdirect.fdstore.iplocator.IpLocatorEventDTO;
import com.freshdirect.fdstore.request.FDProductRequest;
import com.freshdirect.fdstore.survey.FDSurveyResponse;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.fdstore.util.IgnoreCaseString;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.mail.FTLEmailI;
import com.freshdirect.framework.mail.XMLEmailI;
import com.freshdirect.giftcard.CardInUseException;
import com.freshdirect.giftcard.CardOnHoldException;
import com.freshdirect.giftcard.ErpGCDlvInformationHolder;
import com.freshdirect.giftcard.ErpGiftCardModel;
import com.freshdirect.giftcard.InvalidCardException;
import com.freshdirect.giftcard.ServiceUnavailableException;
import com.freshdirect.logistics.analytics.model.TimeslotEvent;
import com.freshdirect.logistics.delivery.dto.CustomerAvgOrderSize;
import com.freshdirect.logistics.delivery.model.EnumReservationType;

/**
 *
 *
 * @version $Revision:58$
 * @author $Author:Kashif Nadeem$
 */
public interface FDCustomerManagerSB extends EJBObject {
    
    
    /**
     * Register and log in a new customer.
     *
     * @param ErpCustomerModel erpCustomer
     * @param FDCustomerModel fdCustomer
     * @param String cookie
     *
     * @return the resulting FDIdentity
     * @throws FDResourceException if an error occured using remote resources
     * @throws ErpDuplicateUserIdException if user enters an email address already in the system
     */
	public RegistrationResult register(
		FDActionInfo info,
		ErpCustomerModel erpCustomer,
		FDCustomerModel fdCustomer,
		String cookie,
		boolean pickupOnly, 
		boolean eligibleForPromotion,
		FDSurveyResponse survey, EnumServiceType serviceType)
		throws FDResourceException, ErpDuplicateUserIdException, RemoteException;
	
	public RegistrationResult register(
			FDActionInfo info,
			ErpCustomerModel erpCustomer,
			FDCustomerModel fdCustomer,
			String cookie,
			boolean pickupOnly, 
			boolean eligibleForPromotion,
			FDSurveyResponse survey, EnumServiceType serviceType, boolean isGiftCardBuyer)
			throws FDResourceException, ErpDuplicateUserIdException, RemoteException; 
    
    public FDUser createNewUser(String zipCode, EnumServiceType serviceType, EnumEStoreId eStoreId) throws FDResourceException, RemoteException;
    
    public FDUser createNewUser(AddressModel address, EnumServiceType serviceType, EnumEStoreId eStoreId) throws FDResourceException, RemoteException;
    
    public FDUser createNewDepotUser(String depotCode, EnumServiceType serviceType, EnumEStoreId eStoreId) throws FDResourceException, RemoteException;
    
    public void createAddress(ErpAddressModel addressModel, String customerId) throws FDResourceException, RemoteException;
    
    public FDUser recognize(FDIdentity identity) throws FDAuthenticationException, FDResourceException, RemoteException;
    
    public FDUser recognize(FDIdentity identity, EnumEStoreId eStoreId) throws FDAuthenticationException, FDResourceException, RemoteException;
    
    public FDUser recognize(FDIdentity identity, EnumEStoreId eStoreId, final boolean lazy) throws FDAuthenticationException, FDResourceException, RemoteException;

    public FDUser recognize(String cookie, EnumEStoreId eStoreId) throws FDAuthenticationException, FDResourceException, RemoteException;
    
    public FDUser recognizeByEmail(String email, EnumEStoreId eStoreId) throws FDAuthenticationException, FDResourceException, RemoteException;
    
    public ErpAddressModel assumeDeliveryAddress(FDIdentity identity, String lastOrderId) throws FDResourceException, RemoteException;
    
    /**
     * Authenticate and log in a customer.
     *
     * @param userId
     * @param password
     *
     * @return user identity reference
     *
     * @throws FDAuthenticationException if the userId/password was not found
     * @throws FDResourceException if an error occured using remote resources
     */
    public FDIdentity login(String userId, String password) throws FDAuthenticationException, FDResourceException, RemoteException;
    public FDIdentity login(String userId) throws FDAuthenticationException, FDResourceException, RemoteException;
    
    public PrimaryKey getCustomerId(String userId) throws FDResourceException, RemoteException;
    
    public FDCustomerInfo getCustomerInfo(FDIdentity identity) throws FDResourceException, RemoteException;
    public FDCustomerInfo getSOCustomerInfo(FDIdentity identity) throws FDResourceException, RemoteException;
    
    /**
     * Get all the payment methods of the customer.
     *
     * @param identity the customer's identity reference
     *
     * @return collection of ErpPaymentMethodModel objects
     * @throws FDResourceException if an error occured using remote resources
     */
    public Collection<ErpPaymentMethodI> getPaymentMethods(FDIdentity identity) throws FDResourceException, RemoteException;
    
    
    /**
     * Add a payment method for the customer.
     *
     * @param identity the customer's identity reference
     * @param paymentMethod ErpPaymentMethodI to add
     *
     * @throws FDResourceException if an error occured using remote resources
     */
    public void addPaymentMethod(FDActionInfo info, ErpPaymentMethodI paymentMethod, boolean paymentechEnabled) throws FDResourceException, RemoteException, ErpPaymentMethodException;
   
    public void setDefaultPaymentMethod(FDActionInfo info, PrimaryKey paymentMethodPK) throws FDResourceException,RemoteException;
    
    /****
     * return the Pk Id that is stored in the defaultPaymentMethodPK field
     *@ param Identity the customers Identity
     *
     *Throws FDresourceException
     */
    public String getDefaultPaymentMethodPK(FDIdentity identity) throws FDResourceException,RemoteException;

    /**
     * update a payment method for the customer
     *
     * @param identity the customer's identity reference
     * @param paymentMethod ErpPaymentMethodI to update
     *
     * @throws FDResourceException if an error occured using remote resources
     */
    public void updatePaymentMethod(FDActionInfo info, ErpPaymentMethodI paymentMethod) throws FDResourceException, RemoteException,  ErpPaymentMethodException;
    
    
    /**
     * remove a payment method for the customer
     *
     * @param identity the customer's identity reference
     * @param pk PrimaryKey of the paymentMethod to remove
     *
     * throws FDResourceException if an error occured using remote resources
     */
    public void removePaymentMethod(FDActionInfo info, ErpPaymentMethodI paymentMethod) throws FDResourceException, RemoteException;

	public boolean checkBillToAddressFraud(FDActionInfo info, ErpPaymentMethodI paymentMethod) throws FDResourceException, RemoteException;
    
    /**
     * update customer info
     *
     * @param identity the customer's identity reference
     * @param customerInfo ErpCustomerInfoModel of the updated customer info
     *
     * throws FDResourceException if an error occured using remote resources
     */
    public boolean updateCustomerInfo(FDActionInfo info, ErpCustomerInfoModel customerInfo) throws FDResourceException, RemoteException;
    
    public void updateUserId(FDActionInfo info, String userId) throws FDResourceException, ErpDuplicateUserIdException, RemoteException;
    
    public void updatePasswordHint(FDIdentity identity, String passwordHint) throws FDResourceException, RemoteException;
    
    /**
     * Get the customer's every ship to address.
     *
     * @param identity the customer's identity reference
     *
     * @return collection of ErpAddresModel objects
     *
     * @throws FDResourceException if an error occured using remote resources
     */
    public Collection<ErpAddressModel> getShipToAddresses(FDIdentity identity) throws FDResourceException, RemoteException;
    
    
    /**
     * Add a ship to address for the customer.
     *
     * @param identity the customer's identity reference
     * @param address ErpAddressModel to add
     *
     * @throws FDResourceException if an error occured using remote resources
     */
    public boolean addShipToAddress(FDActionInfo info, boolean checkUniqueness, ErpAddressModel address) throws FDResourceException, ErpDuplicateAddressException, RemoteException;
    
    /**
     *	update a ship to address for the customer
     *
     * @param identity the customer's identity reference
     * @param address ErpAddressModel to update
     *
     * @throws FDResourceException if an error occured usong remote resources
     */
    public boolean updateShipToAddress(FDActionInfo info, boolean checkUniqueness, ErpAddressModel address) throws FDResourceException, ErpDuplicateAddressException, RemoteException;
    
    /**
     * remove a ship to address for the customer
     *
     * @param identity the customer's identity reference
     * @param pk PrimayKey of the address to remove
     *
     * @throws FDResourceException if an error occured using remote resources
     */
    public void removeShipToAddress(FDActionInfo info, PrimaryKey pk) throws FDResourceException, RemoteException;
    
    /***
     *Set the default Ship To address PK on the customer
     *
     *@Param identity the customer's Identity
     *@param PK Primary Key of the Ship To Address
     **/
    public void setDefaultShipToAddressPK(FDIdentity identity, String shipToAddressPK) throws FDResourceException,RemoteException;
    
    /***
     *Get the default Ship To address PK on the customer
     *
     *@Param identity the customer's Identity
     **/
    public String getDefaultShipToAddressPK(FDIdentity identity) throws FDResourceException,RemoteException;
    
    
    /**
     * Store the user.
     *
     * @param user the customer's user object
     *
     * @throws FDResourceException if an error occured using remote resources
     */
    public void storeUser(FDUser user) throws FDResourceException, RemoteException;
    
    // [APPREQ-369] store Cohort ID that belongs to user
    public void storeCohortName(FDUser user) throws FDResourceException, RemoteException;
    
    public void storeSavedRecipients(FDUser user, List<SavedRecipientModel> recipientList) throws FDResourceException, RemoteException;
    
    public void storeSavedRecipient(FDUser user, SavedRecipientModel srm) throws FDResourceException, RemoteException;
    
    public void updateSavedRecipient(FDUser user, SavedRecipientModel srm) throws FDResourceException, RemoteException;
    
    public void deleteSavedRecipients(FDUser user) throws FDResourceException, RemoteException;
    
    public void deleteSavedRecipient(String savedRecipientId) throws FDResourceException, RemoteException;
    
    public List<SavedRecipientModel> loadSavedRecipients(FDUser user) throws FDResourceException, RemoteException;
    
    public List<DlvSaleInfo> getOrdersByTruck(String truckNumber, Date dlvDate) throws FDResourceException, RemoteException; 

	public FDOrderI getOrderForCRM(FDIdentity identity, String saleId) throws FDResourceException, RemoteException;

	public FDOrderI getOrder(FDIdentity identity, String saleId) throws FDResourceException, RemoteException;
	
	public List<FDOrderI> getOrders(List<String> saleIds) throws FDResourceException, RemoteException;
    
    public FDOrderI getOrderForCRM(String saleId) throws FDResourceException, RemoteException;

    public FDOrderI getOrder(String saleId) throws FDResourceException, RemoteException;
    
    public ErpSaleModel getErpSaleModel(String saleId) throws FDResourceException, RemoteException;
    
    /**
     * Get lightweight info about a customer's orders.
     *
     * @param identity the customer's identity reference
     */
    public ErpOrderHistory getOrderHistoryInfo(FDIdentity identity) throws FDResourceException, RemoteException;
    
    
    /**
     * Get lightweight info about a customer's used promotions.
     *
     * @param identity the customer's identity reference
     */
    public ErpPromotionHistory getPromoHistoryInfo(FDIdentity identity) throws FDResourceException, RemoteException;
    
    /**
     * Place an order (send msg to SAP, persist order).
     * @param isFriendReferred 
     *
     * @param identity the customer's identity reference
     * @return String sale id
     * @throws FDResourceException if an error occured while accessing remote resources
     */
	public String placeOrder(
		FDActionInfo info,
		ErpCreateOrderModel createOrder,
		Set<String> usedPromotionCodes,
		String rsvId,
		boolean sendEmail,
		CustomerRatingI cra,
		CrmAgentRole agentRole,
		EnumDlvPassStatus status, boolean isFriendReferred)
		throws FDResourceException,
		ErpFraudException,
		ErpAuthorizationException,
		ErpAddressVerificationException,
		ReservationException,
		DeliveryPassException,
		FDPaymentInadequateException,
		ErpTransactionException,
		InvalidCardException,
		RemoteException;

	/**
     * Cancel an order (cancel & send msg to SAP).
     *
     * @param identity the customer's identity reference
     * @throws FDResourceException if an error occured while accessing remote resources
     */
    public FDReservation cancelOrder(FDActionInfo info, String saleId, boolean sendEmail, int currentDPExtendDays, boolean restoreReservation) throws FDResourceException, ErpTransactionException, DeliveryPassException, RemoteException;
    
    /**
     * Modify an order (modify & send msg to SAP).
     *
     * @param identity the customer's identity reference
     * @throws FDResourceException if an error occured while accessing remote resources
     */
    public void modifyOrder(
		FDActionInfo info,
		String saleId,
		ErpModifyOrderModel order,
		Set<String> usedPromotionCodes,
		String oldReservationId,
		boolean sendEmail,
		CustomerRatingI cra,
		CrmAgentRole agentRole,
		EnumDlvPassStatus status,boolean hasCoupons)
		throws FDResourceException,
		ErpFraudException,
		ErpAuthorizationException,
		ErpTransactionException,
		DeliveryPassException,
		FDPaymentInadequateException,
		ErpAddressVerificationException,
		InvalidCardException,
		RemoteException;

    /**
     * Modify an auto renew order (modify, don't send msg to SAP).
     *
     * @param identity the customer's identity reference
     * @throws FDResourceException if an error occured while accessing remote resources
     */
    public void modifyAutoRenewOrder(
		FDActionInfo info,
		String saleId,
		ErpModifyOrderModel order,
		Set<String> usedPromotionCodes,
		String oldReservationId,
		boolean sendEmail,
		CustomerRatingI cra,
		CrmAgentRole agentRole,
		EnumDlvPassStatus status)
		throws FDResourceException,
		ErpFraudException,
		ErpAuthorizationException,
		ErpTransactionException,
		DeliveryPassException,
		FDPaymentInadequateException,
		RemoteException;

    /**
     * Charge an order (modify & send msg to SAP).
     *
     * @param identity the customer's identity reference
     * @throws FDResourceException if an error occured while accessing remote resources
     */
    public void chargeOrder(
		FDIdentity identity,
		String saleId,
		ErpPaymentMethodI paymentMethod,
		boolean sendEmail,
		CustomerRatingI cra,
		CrmAgentModel agent,
		double additionalCharge)
		throws FDResourceException,
		ErpFraudException,
		ErpAuthorizationException,
		ErpTransactionException,
		FDPaymentInadequateException,
		ErpAddressVerificationException,
		RemoteException;

    /**
     * Adds a complaint to the user's list of complaints and begins the associated credit issuing process
     *
     * @param ErpComplaintModel represents the complaint
     * @param String the PK of the sale to which the complaint is to be added
     * @throws FDResourceException if an error occured while accessing remote resources
     * @throws ErpComplaintException if order was not in proper state to accept complaints
     */
    public PrimaryKey addComplaint(ErpComplaintModel complaint, String saleId,String erpCustomerId,String fdCustomerId, boolean autoApproveAuthorized, Double limit  ) throws FDResourceException, ErpComplaintException, RemoteException;
    
    public void approveComplaint(String complaintId, boolean isApproved, String csrId, boolean sendMail,Double limit) throws FDResourceException, ErpComplaintException, RemoteException;
    
    public List<String> getComplaintsForAutoApproval() throws FDResourceException, ErpComplaintException, RemoteException;
    
    /**
     * Check availability of an order.
     *
     * @return Map of order line number / FDAvailabilityI objects
     */
    public Map<String, FDAvailabilityI> checkAvailability(FDIdentity identity, ErpCreateOrderModel createOrder, long timeout) throws FDResourceException, RemoteException;
    
    /**
     * CallCenter method for locating all customers meeting the given criteria
     *
     * @param custNumber
     * @param firstName
     * @param middleName
     * @param lastName
     * @param email
     * @param phone
     * @return Collection of CustomerSearchResult objects
     * @throws FDResourceException if an error occured while accessing remote resources
     */
    public List<FDCustomerOrderInfo> locateCustomers(FDCustomerSearchCriteria criteria) throws FDResourceException, RemoteException;
    
    public List<FDCustomerOrderInfo> locateOrders(FDOrderSearchCriteria criteria) throws FDResourceException, RemoteException;
    
    public void setActive(FDActionInfo info, boolean active) throws RemoteException, FDResourceException;
    
    public void doEmail(XMLEmailI email) throws RemoteException, FDResourceException;
    
    public boolean sendPasswordEmail(String emailAddress, boolean tAltEmail) throws RemoteException, FDResourceException, PasswordNotExpiredException ;
    
    public boolean isCorrectPasswordHint(String emailAddress, String hint) throws FDResourceException, ErpFraudException, RemoteException;
    
    public boolean isPasswordRequestExpired(String emailAddress, String passReq) throws FDResourceException, RemoteException;
    
    public void changePassword(FDActionInfo info, String emailAddress, String password) throws FDResourceException, RemoteException;
    
    public void setProfileAttribute(FDIdentity identity, String key, String value, FDActionInfo info) throws RemoteException, FDResourceException;
    
    public void removeProfileAttribute(FDIdentity identity, String key, FDActionInfo info) throws RemoteException, FDResourceException;

    public void setSignupPromotionEligibility(FDActionInfo info, boolean eligible) throws FDResourceException, RemoteException;
    
    public String getDefaultDepotLocationPK(FDIdentity identity) throws FDResourceException, RemoteException;
    
    public void setDefaultDepotLocationPK(FDIdentity identity, String locationId) throws FDResourceException, RemoteException;
    
    public String getDepotCode(FDIdentity identity) throws FDResourceException, RemoteException;
    
    public void setDepotCode(FDIdentity identity, String depotCode) throws FDResourceException, RemoteException;
    
    public String generatePasswordRequest(PrimaryKey fdCustomerPk, java.util.Date expiration) throws FDResourceException, RemoteException;
        
	public Map<String, Integer> getProductPopularity() throws FDResourceException, RemoteException;
        
//	public List loadPromotions() throws FDResourceException, RemoteException;

	public List<String> getReminderListForToday() throws FDResourceException, RemoteException;

	public void sendReminderEmail(PrimaryKey custPk) throws FDResourceException, RemoteException;
	
	public void authorizeSale(String salesId)throws FDResourceException, RemoteException;
	
	public void createCase(CrmSystemCaseInfo caseInfo) throws RemoteException, FDResourceException;

	public FDCustomerCreditHistoryModel getCreditHistory(FDIdentity identity) throws FDResourceException, RemoteException;
	
	public FDReservation makeReservation(FDUserI user, String timeslotId, EnumReservationType rsvType, String addressId, FDActionInfo aInfo, boolean chefsTable, TimeslotEvent event, boolean isForced) throws FDResourceException, ReservationException, RemoteException;
	
	public void updateWeeklyReservation(FDIdentity identity, FDTimeslot timeslot, String addressId, FDActionInfo aInfo) throws FDResourceException, RemoteException;
	
	//public FDReservation changeReservation(FDIdentity identity, FDReservation oldReservation, String timeslotId, EnumReservationType rsvType, String addressId, FDActionInfo aInfo, boolean chefstable, TimeslotEvent event) throws FDResourceException, ReservationException, RemoteException;
	
	public void cancelReservation (FDIdentity identity, FDReservation reservation, EnumReservationType type, FDActionInfo actionInfo, TimeslotEvent event) throws FDResourceException, RemoteException;
	
	public List<ReservationInfo> getRecurringReservationList() throws FDResourceException, RemoteException;
	
	public List<ReservationInfo> getRecurringReservationList(int day_of_week) throws FDResourceException, RemoteException;

	public void storeCustomerRequest(FDCustomerRequest cr) throws FDResourceException, RemoteException;
	
	public boolean isECheckRestricted(FDIdentity identity)  throws FDResourceException, RemoteException;
	
	public boolean isReferrerRestricted(FDIdentity identity)  throws FDResourceException, RemoteException;
	
	public String getNextId(String schema, String sequence) throws FDResourceException, RemoteException;
	
	public Map<String, ProfileAttributeName> loadProfileAttributeNames() throws FDResourceException, RemoteException;
	
	public List<String> loadProfileAttributeNameCategories()	throws FDResourceException, RemoteException;
		
	public void setAlert(FDActionInfo info, ErpCustomerAlertModel customerAlert, boolean isOnAlert) throws RemoteException;
	
	public boolean isOnAlert(PrimaryKey pk, String alertType) throws RemoteException;
	
	public boolean isOnAlert(PrimaryKey pk)  throws RemoteException;
		
	public boolean isCustomerActive(PrimaryKey pk) throws RemoteException;
	
	public List<ErpCustomerAlertModel> getAlerts(PrimaryKey pk)  throws RemoteException;
	
	public List<URLRewriteRule> loadRewriteRules() throws FDResourceException, RemoteException;
	
	public List<DeliveryPassModel> getDeliveryPasses(FDIdentity identity) throws RemoteException;
	
	public Map<String, DlvPassUsageInfo> getDlvPassesUsageInfo(FDIdentity identity) throws RemoteException;
	
	public List<DeliveryPassModel> getDeliveryPassesByStatus(FDIdentity identity, EnumDlvPassStatus status) throws RemoteException;
	
	public ErpOrderHistory getOrdersByDlvPassId(FDIdentity identity, String dlvPassId) throws RemoteException;
	
	public FDUserDlvPassInfo getDeliveryPassInfo(FDUserI user) throws FDResourceException, RemoteException;
	
	public List<DlvPassUsageLine> getRecentOrdersByDlvPassId(FDIdentity identity, String dlvPassId, int noOfDaysOld) throws FDResourceException, RemoteException;
	
	public Map<String, List<FDCustomerOrderInfo>> cancelOrders(FDActionInfo actionInfo, List<FDCustomerOrderInfo> customerOrders, boolean sendEmail) throws RemoteException;
	
	public void storeRetentionSurvey(FDIdentity fdIdentity, String profileAttr
			, String profileValue, CrmSystemCaseInfo caseInfo) throws RemoteException, FDResourceException;
	public boolean hasPurchasedPass(String customerPK) throws RemoteException, FDResourceException ;
	
	public int getValidOrderCount(FDIdentity identity) throws RemoteException, FDResourceException;
	
	public String getLastOrderID(FDIdentity identity) throws RemoteException, FDResourceException;
	
	public String hasAutoRenewDP(String customerPK) throws FDResourceException, RemoteException;
	
	public void setHasAutoRenewDP(String customerPK, EnumTransactionSource source, String initiator,boolean autoRenew)throws FDResourceException, RemoteException;
	
	public FDOrderI getLastNonCOSOrderUsingCC(String customerID,EnumSaleType saleType, EnumSaleStatus saleStatus) throws FDResourceException, RemoteException,ErpSaleNotFoundException;
	
	/**
     * Place an order (send msg to SAP, persist order).

     *
     * @param identity the customer's identity reference
     * @return String sale id
     * @throws FDResourceException if an error occured while accessing remote resources
     */
	public String placeSubscriptionOrder( FDActionInfo info,
		                      ErpCreateOrderModel createOrder,
		                      Set<String> usedPromotionCodes,
		                      String rsvId,
		                      boolean sendEmail,
		                      CustomerRatingI cra,
		                      CrmAgentRole agentRole,
		                      EnumDlvPassStatus status
		                    ) throws FDResourceException,
		                             ErpFraudException,
		                             //ReservationException,
		                             DeliveryPassException,
		                             FDPaymentInadequateException,
		                             RemoteException;	

	
	public String placeGiftCardOrder( FDActionInfo info,
            ErpCreateOrderModel createOrder,
            Set<String> usedPromotionCodes,
            String rsvId,
            boolean sendEmail,
            CustomerRatingI cra,
            CrmAgentRole agentRole,
            EnumDlvPassStatus status,boolean isBulkOrder
          ) throws ServiceUnavailableException, FDResourceException,
          ErpFraudException,
          ErpAuthorizationException,
          ErpAddressVerificationException,
          RemoteException;
	

	public void addAndReconcileInvoice(String saleId, ErpInvoiceModel invoice, ErpShippingInfo shippingInfo)
	throws ErpTransactionException, RemoteException;
	
	public void authorizeSale(String erpCustomerID, String saleID, EnumSaleType type,CustomerRatingI cra) throws FDResourceException, ErpSaleNotFoundException, RemoteException;
	public  Object[] getAutoRenewalInfo()throws FDResourceException, RemoteException;

	public boolean isOrderBelongsToUser(FDIdentity identity, String saleId) throws RemoteException, FDResourceException;
	/**
     * Get lightweight info object about a customer's past orders.
     *
     * @param identity the customer's identity reference
     */
    public OrderHistoryI getWebOrderHistoryInfo(FDIdentity identity) throws FDResourceException, RemoteException;
        
    public  String getAutoRenewSKU(String customerPK)throws FDResourceException, RemoteException;
    
    public void logCustomerVariant(String saleId, FDIdentity identity, String feature, String variantId) throws RemoteException, FDResourceException;

    public ErpAddressModel getLastOrderAddress(String lastOrderId) throws FDResourceException, RemoteException, SQLException;
    
    public void storeProductRequest(List<FDProductRequest> productRequest,FDSurveyResponse survey) throws RemoteException, FDResourceException;

    public void storeProductRequest(List<FDProductRequest> productRequest) throws RemoteException, FDResourceException;
    
    public ErpAddressModel getAddress(FDIdentity identity,String id) throws FDResourceException, RemoteException;


	public void assignAutoCaseToComplaint(PrimaryKey complaintPk, PrimaryKey autoCasePK) throws RemoteException, FDResourceException;
    
    //For Gift Cards
    public ErpGiftCardModel applyGiftCard(FDIdentity identity, String givexNum, FDActionInfo info) throws ServiceUnavailableException, InvalidCardException, CardInUseException, CardOnHoldException, FDResourceException, RemoteException;
    
    /**
     * Get all the Gift cards of the customer.
     *
     * @param identity the customer's identity reference
     *
     * @return collection of ErpPaymentMethodModel objects
     * @throws FDResourceException if an error occured using remote resources
     */
    public Collection<ErpGiftCardModel> getGiftCards(FDIdentity identity) throws FDResourceException, RemoteException;
    
    public List<ErpGiftCardModel> verifyStatusAndBalance(List<ErpGiftCardModel> giftcards, boolean reloadBalance ) throws FDResourceException, RemoteException;
    
    public ErpGiftCardModel verifyStatusAndBalance(ErpGiftCardModel giftcard, boolean reloadBalance ) throws FDResourceException, RemoteException;
    
    public List getGiftCardRecepientsForCustomer(FDIdentity identity) throws FDResourceException, RemoteException;

    public ErpGCDlvInformationHolder getRecipientDlvInfo(FDIdentity identity, String saleId, String certificationNum) throws FDResourceException, RemoteException;
    
    public boolean resendEmail(String saleId, String certificationNum, String resendEmailId, String recipName, String personMsg, EnumTransactionSource source) throws FDResourceException, RemoteException;
    
    public boolean resendEmail(String saleId, String certificationNum, String resendEmailId, String recipName, String personMsg, boolean toPurchaser, boolean toLastRecipient, EnumTransactionSource source) throws FDResourceException, RemoteException;
    
    public double getOutStandingBalance(ErpAbstractOrderModel order) throws FDResourceException, RemoteException;
    
    public EnumIPhoneCaptureType iPhoneCaptureEmail(String emailId, EnumTransactionSource source) throws FDResourceException, RemoteException;

    public void doEmail(FTLEmailI email) throws RemoteException, FDResourceException;
    
    public void preAuthorizeSales(String salesId) throws RemoteException, FDResourceException;
    
	public List getGiftCardOrdersForCustomer(FDIdentity identity) throws RemoteException, FDResourceException;

	public Object getGiftCardRedemedOrders(FDIdentity identity, String certNum) throws RemoteException, FDResourceException;
	
	public Object getGiftCardRedemedOrders(String certNum) throws RemoteException, FDResourceException;

	public List getDeletedGiftCardForCustomer(FDIdentity identity) throws RemoteException, FDResourceException;
    
    public List getGiftCardRecepientsForOrder(String saleId) throws FDResourceException, RemoteException;
    
    public ErpGiftCardModel validateAndGetGiftCardBalance(String givexNum) throws FDResourceException, RemoteException;
    
    public void transferGiftCardBalance(FDIdentity identity,String fromGivexNum,String toGivexNum,double amount) throws FDResourceException, RemoteException;
    
    public String[] sendGiftCardCancellationEmail(String saleId, String certNum, boolean toRecipient, boolean toPurchaser, boolean newRecipient, String newRecipientEmail) throws RemoteException, FDResourceException;
    
    public String placeDonationOrder( FDActionInfo info,
            ErpCreateOrderModel createOrder,
            Set<String> usedPromotionCodes,
            String rsvId,
            boolean sendEmail,
            CustomerRatingI cra,
            CrmAgentRole agentRole,
            EnumDlvPassStatus status, boolean isOptIn
          ) throws RemoteException, FDResourceException,
                   ErpFraudException,
                   ErpAuthorizationException ;
    
    public double getPerishableBufferAmount(ErpAbstractOrderModel order) throws RemoteException, FDResourceException;
    
    public Map getGiftCardRecepientsForOrders(List saleIds) throws RemoteException, FDResourceException ;
    
    public ErpGCDlvInformationHolder GetGiftCardRecipentByCertNum(String certNum) throws RemoteException,FDResourceException ;
    
    public void saveDonationOptIn(String custId, String saleId, boolean optIn)throws RemoteException,FDResourceException ;
    
    public void resubmitGCOrders() throws RemoteException, FDResourceException;
    
    public List<String> getTopFaqs() throws FDResourceException, RemoteException;
    
    public List<HashMap<String, String>> productRequestFetchAllDepts() throws FDResourceException, RemoteException;
    public List<HashMap<String, String>> productRequestFetchAllCats() throws FDResourceException, RemoteException;
    public List<HashMap<String, String>> productRequestFetchAllMappings() throws FDResourceException, RemoteException;
    
    public CrmClick2CallModel getClick2CallInfo() throws FDResourceException, RemoteException;

	public List<ErpClientCodeReport> findClientCodesBySale(String saleId) throws FDResourceException, RemoteException;
	public List<ErpClientCodeReport> findClientCodesByDateRange(FDIdentity customerId, Date start, Date end) throws FDResourceException, RemoteException;

	public SortedSet<IgnoreCaseString> getOrderClientCodesForUser(FDIdentity identity) throws FDResourceException, RemoteException;
	
	public void createCounter( String customerId, String counterId, int initialValue ) throws FDResourceException, RemoteException;
	public void updateCounter( String customerId, String counterId, int newValue ) throws FDResourceException, RemoteException;
	public Integer getCounter( String customerId, String counterId ) throws FDResourceException, RemoteException;
	public void sendSettlementFailedEmail(String saleID) throws FDResourceException, RemoteException;
	
	public void bulkModifyOrder(
			String saleId,
			FDIdentity identity,			
			FDActionInfo info,
			ErpModifyOrderModel order,
			String oldReservationId,
			Set<String> appliedPromos,
			CrmAgentRole agentRole,
			boolean sendEmail,
			boolean hasCoupons)
			throws FDResourceException, 
			ErpTransactionException, 
			ErpFraudException, 
			ErpAuthorizationException, 
			DeliveryPassException, 
			ErpAddressVerificationException,
			InvalidCardException,
			FDPaymentInadequateException,
			SQLException,
			RemoteException;
	
	public ErpAuthorizationModel verify(FDActionInfo info,ErpPaymentMethodI paymentMethod) throws FDResourceException,ErpAuthorizationException, RemoteException;
	
	public String recordReferral(String customerId, String referralId, String customerEmail) throws FDResourceException, RemoteException;
	
	public String dupeEmailAddress(String email) throws FDResourceException, RemoteException;
	
	public void storeMobilePreferences(String fdcustomerId, String mobileNumber, String textOffers, String textDelivery, String orderNotices, String orderExceptions, String offers, String partnerMessages, EnumEStoreId eStoreId) throws FDResourceException, RemoteException;
	
	public void storeGoGreenPreferences(String customerId, String goGreen) throws FDResourceException, RemoteException;
	public boolean loadGoGreenPreference(String customerId) throws FDResourceException, RemoteException;
	
	public void storeMobilePreferencesNoThanks(String customerId) throws FDResourceException, RemoteException;
	public void storeSmsPreferencesNoThanks(String fdCustomerId, EnumEStoreId eStoreId) throws FDResourceException, RemoteException;
	
	public void storeAllMobilePreferences(String customerId, String fdCustomerId, String mobileNumber, String textOffers, String textDelivery, String goGreen, String phone, String ext, boolean isCorpUser,EnumEStoreId eStoreId) throws FDResourceException, RemoteException;
	
	public void authorizeSale(String salesId, boolean force) throws FDResourceException, RemoteException;
	
	public boolean isDisplayNameUsed(String displayName,String custId) throws ErpDuplicateDisplayNameException, RemoteException;
	
	public void storeSMSWindowDisplayedFlag(String customerId) throws FDResourceException, RemoteException;
	
	/* APPDEV-2475 DP T&C */
	public void storeDPTCViews(String customerId, int dpTcViewCount) throws FDResourceException, RemoteException;
	public void storeDPTCAgreeDate(String customerId, Date dpTcAgreeDate) throws FDResourceException, RemoteException;
	public void storeDPTCAgreeDate(FDActionInfo info, String customerId, Date dpTcAgreeDate) throws FDResourceException, RemoteException;
	
	public List<CustomerCreditModel> getCustomerReprotedLates() throws FDResourceException, RemoteException;
	
	public List<CustomerCreditModel> getDriverReportedLates() throws FDResourceException, RemoteException;
	
	public List<CustomerCreditModel> getScanReportedLates() throws FDResourceException, RemoteException;
	
	public void storeLists(Set cmList) throws FDResourceException, RemoteException;
	
	public List<FDCartonInfo> getCartonDetailsForSale(FDOrderI order) throws FDResourceException, RemoteException;
	
	public Map getAssignedCustomerParams(FDUser user) throws FDResourceException, RemoteException;

	public FDUserI saveExternalCampaign(FDUserI user) throws FDResourceException, RemoteException;
	
	public void logIpLocatorEvent(IpLocatorEventDTO ipLocatorEventDTO) throws FDResourceException, RemoteException;
	
	public IpLocatorEventDTO loadIpLocatorEvent (String fdUserId) throws FDResourceException, RemoteException;
	public boolean  isFeatureEnabled(String customerId, EnumSiteFeature feature) throws FDResourceException, RemoteException;

	public CustomerAvgOrderSize getHistoricOrderSize(String customerId) throws FDResourceException, RemoteException;
	public FDUser getFDUserWithCart(FDIdentity identity, EnumEStoreId eStoreId) throws FDAuthenticationException, FDResourceException, RemoteException;
	
	public void storeSmsPrefereceFlag(String fdCustomerId, String flag, EnumEStoreId eStoreId) throws FDResourceException, RemoteException;
	
	//4125 coremetrics
    public String getCustomersProfileValue(String customerId) throws FDResourceException, RemoteException;

    public String getCustomersCounty(String customerId) throws FDResourceException, RemoteException;

	public List<String> getUsedReservations(String customerId) throws FDResourceException, RemoteException;
	
	public String getLastOrderID(FDIdentity identity, EnumEStoreId eStoreId)	throws FDResourceException, RemoteException;

	public void updateOrderInModifyState(ErpSaleModel sale)	throws FDResourceException, RemoteException;

	public boolean isReadyForPick(String orderNum)	throws FDResourceException, RemoteException;

	public void updateOrderInProcess(String orderNum) throws FDResourceException, RemoteException;

	public void releaseModificationLock(String orderId)	throws FDResourceException, RemoteException;

	public void setFdxSmsPreferences(FDCustomerEStoreModel customerSmsPreferenceModel, String erpCustomerPk)throws FDResourceException, RemoteException;
	public boolean setAcknowledge(FDIdentity identity, boolean acknowledge,String ackType)throws FDResourceException, RemoteException;
	
	public String getParentOrderAddressId(String parentOrderAddressId)throws FDResourceException, RemoteException;
	
	public boolean getAddonOrderCount(String orderId)throws FDResourceException, RemoteException;
	public boolean reSendInvoiceEmail(String orderId)throws FDResourceException, RemoteException;

	public boolean iPhoneCaptureEmail(String email, String zipCode,
			String serviceType)throws FDResourceException, RemoteException;

	public void storeEmailPreferenceFlag(String fdCustomerId, String flag, EnumEStoreId eStoreId) throws FDResourceException, RemoteException;
}


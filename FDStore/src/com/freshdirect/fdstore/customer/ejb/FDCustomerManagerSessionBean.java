package com.freshdirect.fdstore.customer.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.freshdirect.ErpServicesProperties;
import com.freshdirect.affiliate.ErpAffiliate;
import com.freshdirect.common.address.AddressInfo;
import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.address.ContactAddressModel;
import com.freshdirect.common.address.PhoneNumber;
import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.common.pricing.Discount;
import com.freshdirect.crm.CrmAgentModel;
import com.freshdirect.crm.CrmAgentRole;
import com.freshdirect.crm.CrmCaseSubject;
import com.freshdirect.crm.CrmClick2CallModel;
import com.freshdirect.crm.CrmClick2CallTimeModel;
import com.freshdirect.crm.CrmSystemCaseInfo;
import com.freshdirect.customer.CustomerRatingI;
import com.freshdirect.customer.DlvSaleInfo;
import com.freshdirect.customer.EnumAccountActivityType;
import com.freshdirect.customer.EnumAlertType;
import com.freshdirect.customer.EnumComplaintLineMethod;
import com.freshdirect.customer.EnumComplaintStatus;
import com.freshdirect.customer.EnumDeliverySetting;
import com.freshdirect.customer.EnumDeliveryType;
import com.freshdirect.customer.EnumFraudReason;
import com.freshdirect.customer.EnumPaymentResponse;
import com.freshdirect.customer.EnumPaymentType;
import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.customer.EnumSaleType;
import com.freshdirect.customer.EnumTransactionSource;
import com.freshdirect.customer.ErpAbstractOrderModel;
import com.freshdirect.customer.ErpActivityRecord;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpAddressVerificationException;
import com.freshdirect.customer.ErpAuthorizationException;
import com.freshdirect.customer.ErpAuthorizationModel;
import com.freshdirect.customer.ErpCartonDetails;
import com.freshdirect.customer.ErpCartonInfo;
import com.freshdirect.customer.ErpClientCodeReport;
import com.freshdirect.customer.ErpComplaintException;
import com.freshdirect.customer.ErpComplaintInfoModel;
import com.freshdirect.customer.ErpComplaintLineModel;
import com.freshdirect.customer.ErpComplaintModel;
import com.freshdirect.customer.ErpCreateOrderModel;
import com.freshdirect.customer.ErpCustomerAlertModel;
import com.freshdirect.customer.ErpCustomerEmailModel;
import com.freshdirect.customer.ErpCustomerInfoModel;
import com.freshdirect.customer.ErpCustomerModel;
import com.freshdirect.customer.ErpDepotAddressModel;
import com.freshdirect.customer.ErpDuplicateAddressException;
import com.freshdirect.customer.ErpDuplicateDisplayNameException;
import com.freshdirect.customer.ErpDuplicateUserIdException;
import com.freshdirect.customer.ErpFraudException;
import com.freshdirect.customer.ErpGiftCardComplaintLineModel;
import com.freshdirect.customer.ErpInvoiceModel;
import com.freshdirect.customer.ErpModifyOrderModel;
import com.freshdirect.customer.ErpOrderHistory;
import com.freshdirect.customer.ErpOrderLineModel;
import com.freshdirect.customer.ErpPaymentMethodException;
import com.freshdirect.customer.ErpPaymentMethodI;
import com.freshdirect.customer.ErpPaymentMethodModel;
import com.freshdirect.customer.ErpPromotionHistory;
import com.freshdirect.customer.ErpSaleInfo;
import com.freshdirect.customer.ErpSaleModel;
import com.freshdirect.customer.ErpSaleNotFoundException;
import com.freshdirect.customer.ErpShippingInfo;
import com.freshdirect.customer.ErpTransactionException;
import com.freshdirect.customer.OrderHistoryI;
import com.freshdirect.customer.ejb.ErpCustomerEB;
import com.freshdirect.customer.ejb.ErpCustomerManagerSB;
import com.freshdirect.customer.ejb.ErpFraudPreventionSB;
import com.freshdirect.customer.ejb.ErpLogActivityCommand;
import com.freshdirect.customer.ejb.ErpSaleEB;
import com.freshdirect.delivery.ReservationException;
import com.freshdirect.delivery.sms.SMSAlertManager;
import com.freshdirect.deliverypass.DeliveryPassException;
import com.freshdirect.deliverypass.DeliveryPassModel;
import com.freshdirect.deliverypass.DeliveryPassType;
import com.freshdirect.deliverypass.DlvPassConstants;
import com.freshdirect.deliverypass.DlvPassUsageInfo;
import com.freshdirect.deliverypass.DlvPassUsageLine;
import com.freshdirect.deliverypass.EnumDlvPassExtendReason;
import com.freshdirect.deliverypass.EnumDlvPassStatus;
import com.freshdirect.deliverypass.ejb.DlvPassManagerSB;
import com.freshdirect.erp.ejb.ATPFailureDAO;
import com.freshdirect.erp.model.ATPFailureInfo;
import com.freshdirect.erp.model.ErpInventoryModel;
import com.freshdirect.fdlogistics.model.FDDeliveryDepotModel;
import com.freshdirect.fdlogistics.model.FDInvalidAddressException;
import com.freshdirect.fdlogistics.model.FDReservation;
import com.freshdirect.fdlogistics.model.FDTimeslot;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.URLRewriteRule;
import com.freshdirect.fdstore.Util;
import com.freshdirect.fdstore.ZonePriceListing;
import com.freshdirect.fdstore.atp.FDAvailabilityI;
import com.freshdirect.fdstore.atp.FDAvailabilityInfo;
import com.freshdirect.fdstore.atp.FDCompositeAvailability;
import com.freshdirect.fdstore.atp.FDStockAvailability;
import com.freshdirect.fdstore.atp.FDStockAvailabilityInfo;
import com.freshdirect.fdstore.atp.NullAvailability;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.AddressDAO;
import com.freshdirect.fdstore.customer.CustomerCreditModel;
import com.freshdirect.fdstore.customer.EnumIPhoneCaptureType;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.customer.FDAuthenticationException;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDCartonDetail;
import com.freshdirect.fdstore.customer.FDCartonInfo;
import com.freshdirect.fdstore.customer.FDCustomerCreditHistoryModel;
import com.freshdirect.fdstore.customer.FDCustomerCreditModel;
import com.freshdirect.fdstore.customer.FDCustomerFactory;
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
import com.freshdirect.fdstore.customer.ProfileModel;
import com.freshdirect.fdstore.customer.RegistrationResult;
import com.freshdirect.fdstore.customer.SavedRecipientModel;
import com.freshdirect.fdstore.customer.adapter.CustomerRatingAdaptor;
import com.freshdirect.fdstore.customer.adapter.FDOrderAdapter;
import com.freshdirect.fdstore.deliverypass.DeliveryPassUtil;
import com.freshdirect.fdstore.deliverypass.FDUserDlvPassInfo;
import com.freshdirect.fdstore.ecoupon.EnumCouponTransactionStatus;
import com.freshdirect.fdstore.ecoupon.EnumCouponTransactionType;
import com.freshdirect.fdstore.ecoupon.FDCouponManager;
import com.freshdirect.fdstore.ecoupon.model.ErpCouponTransactionModel;
import com.freshdirect.fdstore.ecoupon.model.FDCouponActivityContext;
import com.freshdirect.fdstore.iplocator.IpLocatorEventDTO;
import com.freshdirect.fdstore.mail.FDEmailFactory;
import com.freshdirect.fdstore.mail.FDGiftCardEmailFactory;
import com.freshdirect.fdstore.promotion.PromotionFactory;
import com.freshdirect.fdstore.promotion.PromotionI;
import com.freshdirect.fdstore.promotion.ejb.FDPromotionNewDAO;
import com.freshdirect.fdstore.referral.ejb.FDReferAFriendDAO;
import com.freshdirect.fdstore.request.FDProductRequest;
import com.freshdirect.fdstore.request.FDProductRequestDAO;
import com.freshdirect.fdstore.survey.FDSurveyResponse;
import com.freshdirect.fdstore.temails.TEmailConstants;
import com.freshdirect.fdstore.temails.TEmailsUtil;
import com.freshdirect.fdstore.temails.ejb.TEmailInfoHome;
import com.freshdirect.fdstore.temails.ejb.TEmailInfoSB;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.fdstore.util.IgnoreCaseString;
import com.freshdirect.fdstore.util.TimeslotLogic;
import com.freshdirect.fdstore.zone.FDZoneInfoManager;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.core.SequenceGenerator;
import com.freshdirect.framework.mail.FTLEmailI;
import com.freshdirect.framework.mail.XMLEmailI;
import com.freshdirect.framework.util.DateRange;
import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.framework.util.GenericSearchCriteria;
import com.freshdirect.framework.util.MD5Hasher;
import com.freshdirect.framework.util.QueryStringBuilder;
import com.freshdirect.framework.util.StringUtil;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.giftcard.CardInUseException;
import com.freshdirect.giftcard.CardOnHoldException;
import com.freshdirect.giftcard.EnumGCDeliveryMode;
import com.freshdirect.giftcard.EnumGiftCardFailureType;
import com.freshdirect.giftcard.ErpGCDlvInformationHolder;
import com.freshdirect.giftcard.ErpGiftCardModel;
import com.freshdirect.giftcard.ErpGiftCardUtil;
import com.freshdirect.giftcard.ErpRecipentModel;
import com.freshdirect.giftcard.GiftCardApplicationStrategy;
import com.freshdirect.giftcard.InvalidCardException;
import com.freshdirect.giftcard.ServiceUnavailableException;
import com.freshdirect.giftcard.ejb.GiftCardManagerSB;
import com.freshdirect.giftcard.ejb.GiftCardPersistanceDAO;
import com.freshdirect.logistics.analytics.model.TimeslotEvent;
import com.freshdirect.logistics.delivery.dto.CustomerAvgOrderSize;
import com.freshdirect.logistics.delivery.model.EnumCompanyCode;
import com.freshdirect.logistics.delivery.model.EnumOrderAction;
import com.freshdirect.logistics.delivery.model.EnumOrderType;
import com.freshdirect.logistics.delivery.model.EnumReservationType;
import com.freshdirect.logistics.delivery.model.OrderContext;
import com.freshdirect.mail.EmailUtil;
import com.freshdirect.mail.EnumEmailType;
import com.freshdirect.mail.EnumTranEmailType;
import com.freshdirect.mail.ErpEmailFactory;
import com.freshdirect.mail.ejb.MailerGatewayHome;
import com.freshdirect.mail.ejb.MailerGatewaySB;
import com.freshdirect.payment.EnumPaymentMethodType;
import com.freshdirect.payment.GatewayAdapter;
import com.freshdirect.payment.ejb.PaymentManagerSB;
import com.freshdirect.payment.fraud.PaymentFraudManager;
import com.freshdirect.payment.fraud.RestrictedPaymentMethodModel;
import com.freshdirect.payment.gateway.Gateway;
import com.freshdirect.payment.gateway.GatewayType;
import com.freshdirect.payment.gateway.Request;
import com.freshdirect.payment.gateway.Response;
import com.freshdirect.payment.gateway.impl.GatewayFactory;
import com.freshdirect.referral.extole.RafUtil;
import com.freshdirect.referral.extole.model.FDRafTransModel;
import com.freshdirect.sap.command.SapCartonInfoForSale;
import com.freshdirect.sap.ejb.SapException;
import com.freshdirect.sms.SmsPrefereceFlag;
import com.freshdirect.temails.TEmailRuntimeException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;


/**
 * @version $Revision:78$
 * @author $Author:Kashif Nadeem$
 */
public class FDCustomerManagerSessionBean extends FDSessionBeanSupport {

	private static final long serialVersionUID = 8227926148253099807L;

	private final static Logger LOGGER = LoggerFactory.getInstance(FDCustomerManagerSessionBean.class);

	public RegistrationResult register(FDActionInfo info,
			ErpCustomerModel erpCustomer, FDCustomerModel fdCustomer,
			String cookie, boolean pickupOnly, boolean eligibleForPromotion,
			FDSurveyResponse survey, EnumServiceType serviceType)
			throws FDResourceException, ErpDuplicateUserIdException {

		return register(info, erpCustomer, fdCustomer, cookie, pickupOnly,
				eligibleForPromotion, survey, serviceType, false);
	}

	/**
	 * Register and log in a new customer.
	 * 
	 * @param ErpCustomerModel
	 *            erpCustomer
	 * @param FDCustomerModel
	 *            fdCustomer
	 * 
	 * @return the resulting FDIdentity
	 * @throws FDResourceException
	 *             if an error occured using remote resources
	 * @throws ErpDuplicateUserIdException
	 *             if user enters an email address already in the system
	 */
	public RegistrationResult register(FDActionInfo info,
			ErpCustomerModel erpCustomer, FDCustomerModel fdCustomer,
			String cookie, boolean pickupOnly, boolean eligibleForPromotion,
			FDSurveyResponse survey, EnumServiceType serviceType,
			boolean isGiftCardBuyer) throws FDResourceException,
			ErpDuplicateUserIdException {

		//System.out.println("FDCustomerManagerSessionBean: In register");
		Connection conn = null;
		try {
			//
			// do registration fraud check
			//
			ErpFraudPreventionSB fraudSB = getErpFraudHome().create();
			Set<EnumFraudReason> fraudResults = null;
			if (!isGiftCardBuyer) {
				fraudResults = fraudSB.checkRegistrationFraud(erpCustomer);
			}
			//System.out.println("FDCustomerManagerSessionBean: Fraud check done");
			erpCustomer.setActive(true);
			
			ErpCustomerManagerSB erpCustomerManagerSB = this
					.getErpCustomerManagerHome().create();
			LOGGER.debug("Creating customer in ERPS");
			//System.out.println("FDCustomerManagerSessionBean: Creating customer in ERPS");
			PrimaryKey pk = erpCustomerManagerSB.createCustomer(erpCustomer,
					isGiftCardBuyer);
			LOGGER.debug("Created customer in ERPS");
			//System.out.println("FDCustomerManagerSessionBean: Created customer in ERPS");

			String erpCustomerId = pk.getId();
			LOGGER.debug("ERPS customer ID: " + erpCustomerId);
			//System.out.println("FDCustomerManagerSessionBean: ERPS customer ID: " + erpCustomerId);

			fdCustomer.setErpCustomerPK(erpCustomerId);
			fdCustomer.setProfile(new ProfileModel());
			fdCustomer.getCustomerEStoreModel().seteStoreId(info.geteStore());
			
			FDCustomerEB fdCustomerEB = getFdCustomerHome().create(fdCustomer);

			conn = getConnection();
			java.sql.PreparedStatement ps = conn
					.prepareStatement("update cust.fduser set fdcustomer_id=? where cookie=?");
			ps.setString(1, fdCustomerEB.getPK().getId());
			ps.setString(2, cookie);
			if (ps.executeUpdate() != 1) {
				throw new FDResourceException(
						"There was trouble updating a previously anonymous user with a registered id.");
			}
			ps.close();
			ps = null;
			
			//System.out.println("FDCustomerManagerSessionBean: All set and ready to send email.");

			// now send email
			ErpCustomerInfoModel erpInfo = erpCustomer.getCustomerInfo();
			FDCustomerInfo emailInfo = new FDCustomerInfo(erpInfo
					.getFirstName(), erpInfo.getLastName());
			emailInfo.setCorporateUser(EnumServiceType.CORPORATE
					.equals(serviceType));
			emailInfo.setHtmlEmail(!erpInfo.isEmailPlaintext());
			emailInfo.setEmailAddress(erpInfo.getEmail());
			emailInfo.setDepotCode(fdCustomer.getDepotCode());
			emailInfo.setPickupOnly(pickupOnly);
			emailInfo.setEligibleForPromotion(eligibleForPromotion);

			/*this.doEmail(FDEmailFactory.getInstance().createConfirmSignupEmail(
					emailInfo));
			*/
			//System.out.println("-------------------------------------"+TEmailsUtil.isTransEmailEnabled(EnumTranEmailType.CUST_SIGNUP));
			boolean isUseOtherEmailMode=true;
			if(TEmailsUtil.isTransEmailEnabled(EnumTranEmailType.CUST_SIGNUP)){
				LOGGER.debug("-------------------------------------------Doing Transaction email");
				try {	
					Map input=new HashMap();
					//input.put(TEmailConstants.ORDER_INP_KEY, order);
					input.put(TEmailConstants.CUSTOMER_ID_INP_KEY, emailInfo);
					input.put(TEmailConstants.EMAIL_TYPE, emailInfo.isHtmlEmail()?EnumEmailType.HTML.getName():EnumEmailType.TEXT.getName());
					
					//APPDEV-2451 - fill in OAS parameters here
			        if (FDStoreProperties.isAdServerEnabled()) {
			        	String zipCode = "";
			        	String cohort = "";
			        	conn = getConnection();
						java.sql.PreparedStatement ps1 = conn
								.prepareStatement("select zipcode, cohort_id from cust.fduser where fdcustomer_id=?");
						ps1.setString(1, fdCustomerEB.getPK().getId());
						ResultSet rs = ps1.executeQuery();						
						if (rs.next()) {
							zipCode = rs.getString("zipcode");
							cohort = rs.getString("cohort_id");
						}
						ps1.close();
						rs.close();
			        	String oasQuery = getOASQueryString(serviceType, fdCustomer.getDepotCode(), zipCode, cohort);
			        	input.put("oasQuery", oasQuery);
			        }
					
					this.doEmail(EnumTranEmailType.CUST_SIGNUP,  input);
					isUseOtherEmailMode=false;
				}catch(TEmailRuntimeException e){
					LOGGER.error("Error in using Transaction Email :",e);					 
				}					
			}
			
			EnumEStoreId estoreId = EnumEStoreId.valueOfContentId((ContentFactory.getInstance().getStoreKey().getId()));
			if(isUseOtherEmailMode) this.doEmail(FDEmailFactory.getInstance().createConfirmSignupEmail(emailInfo, estoreId));
		

			FDIdentity identity = new FDIdentity(fdCustomer.getErpCustomerPK(),
					fdCustomerEB.getPK().getId());

			info.setIdentity(identity);
			this.logActivity(info
					.createActivity(EnumAccountActivityType.CREATE_ACCOUNT));

			if (null != fraudResults && !fraudResults.isEmpty()) {
				info.setSource(EnumTransactionSource.SYSTEM);
				StringBuffer fraudNote = new StringBuffer();
				for (EnumFraudReason fr : fraudResults) {
					if (fraudNote.length() > 1)
						fraudNote.append(" and ");
					fraudNote.append(fr.getDescription());
				}
				this.logActivity(info.createActivity(
						EnumAccountActivityType.CREATE_ACCOUNT, fraudNote
								.toString()));
				this.setSignupPromotionEligibility(info, false);
			}

			if (survey != null) {
				// recreate survey with proper identity
				FDSurveyResponse s = new FDSurveyResponse(identity, survey
						.getKey());
				s.setAnswers(survey.getAnswers());
				LOCATOR.getSurveySessionBean().storeSurvey(s);
			}
			if (isGiftCardBuyer) {
				return new RegistrationResult(identity);
			}
			return new RegistrationResult(identity, fraudResults.size() > 0);

		} catch (java.sql.SQLException sqle) {
			throw new FDResourceException(sqle);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} finally {
			close(conn);
		}
	}

	public FDUser createNewUser(String zipCode, EnumServiceType serviceType, EnumEStoreId eStoreId)
			throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			FDUser user = FDUserDAO.createUser(conn, zipCode, serviceType, eStoreId);

			return user;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}

	public FDUser createNewUser(AddressModel address, EnumServiceType serviceType, EnumEStoreId eStoreId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			FDUser user = null;
			if(address != null){
				user = FDUserDAO.createUser(conn, address.getZipCode(), serviceType, eStoreId);
			}else{
				user = FDUserDAO.createUser(conn, serviceType, eStoreId);
			}

			return user;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}

	public FDUser createNewDepotUser(String depotCode, EnumServiceType serviceType, EnumEStoreId eStoreId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			FDUser user = FDUserDAO.createDepotUser(conn, depotCode, serviceType, eStoreId);

			return user;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	

	public void createAddress(ErpAddressModel addressModel, String customerId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			AddressDAO addressDAO = new AddressDAO();
			addressDAO.create(conn, addressModel, customerId);
			return;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}

	public FDUser recognize(FDIdentity identity) throws FDAuthenticationException, FDResourceException {
		return recognize(identity, null);
	}

	public FDUser recognize(FDIdentity identity, EnumEStoreId eStoreId) throws FDAuthenticationException, FDResourceException {
		return recognize(identity, eStoreId, false);
	}
	
	public FDUser recognize(FDIdentity identity, EnumEStoreId eStoreId, final boolean lazy) throws FDAuthenticationException, FDResourceException {

		Connection conn = null;
		try {
			conn = getConnection();

			FDUser user = FDUserDAO.recognizeWithIdentity(conn, identity, eStoreId, lazy);

			if (user.isAnonymous()) {
				throw new FDAuthenticationException("Unrecognized user");
			}
			// Load Promo Audience Details for this customer.
			user.setAssignedCustomerParams(getAssignedCustomerParams(user,conn));

			user.setDlvPassInfo(getDeliveryPassInfo(user));
			return user;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}

	}
	
	public FDUser getFDUserWithCart(FDIdentity identity, EnumEStoreId eStoreId) throws FDAuthenticationException, FDResourceException {

		Connection conn = null;
		try {
			conn = getConnection();

			FDUser user = FDUserDAO.recognizeWithIdentity(conn, identity, eStoreId, false);

			if (user.isAnonymous()) {
				throw new FDAuthenticationException("Unrecognized user");
			}			
			return user;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}

	}

	public FDUser recognizeByEmail(String email, EnumEStoreId eStoreId)
			throws FDAuthenticationException, FDResourceException {

		Connection conn = null;
		try {
			conn = getConnection();

			FDUser user = FDUserDAO.recognizeWithEmail(conn, email, eStoreId);

			if (user.isAnonymous()) {
				throw new FDAuthenticationException("Unrecognized user");
			}
			// Load Promo Audience Details for this customer.
			user.setAssignedCustomerParams(getAssignedCustomerParams(user,conn));

			user.setDlvPassInfo(getDeliveryPassInfo(user));
			return user;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}

	}

	public FDUserDlvPassInfo getDeliveryPassInfo(FDUserI user)
			throws FDResourceException {
		FDUserDlvPassInfo dlvPassInfo = null;
		try {
			FDIdentity identity = user.getIdentity();
			dlvPassInfo = getDlvPassInfo(identity);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dlvPassInfo;
	}

	private FDUserDlvPassInfo getDlvPassInfo(FDIdentity identity)
			throws CreateException, RemoteException, FDResourceException, SQLException {
		FDUserDlvPassInfo dlvPassInfo;
		if (identity != null) {
			String customerPk = identity.getErpCustomerPK();
			DlvPassManagerSB sb = this.getDlvPassManagerHome().create();
			Map<Comparable,Serializable> statusMap = sb.getAllStatusMap(customerPk);
			EnumDlvPassStatus dlvPassStatus = EnumDlvPassStatus.NONE;

			if (statusMap != null && statusMap.size() > 0) {
				if (statusMap.get(EnumDlvPassStatus.ACTIVE) != null) {
					// User has a active delivery pass.
					dlvPassStatus = EnumDlvPassStatus.ACTIVE;
				} else if (statusMap.get(EnumDlvPassStatus.READY_TO_USE) != null) {
					dlvPassStatus = EnumDlvPassStatus.READY_TO_USE;
				}// -added for DP1.1
				else if (statusMap.get(EnumDlvPassStatus.EXPIRED_PENDING) != null) {
					// User has a expired pending delivery pass.
					dlvPassStatus = EnumDlvPassStatus.EXPIRED_PENDING;
				}// --placed this if check before PENDING check.
				else if (statusMap.get(EnumDlvPassStatus.PENDING) != null) {
					// User has a pending delivery pass.
					dlvPassStatus = EnumDlvPassStatus.PENDING;
				} else if (statusMap
						.get(EnumDlvPassStatus.SETTLEMENT_FAILED) != null) {
					// User's delivery pass got cancelled because settlement
					// failed.
					dlvPassStatus = EnumDlvPassStatus.SETTLEMENT_FAILED;
				} else if (statusMap.get(EnumDlvPassStatus.EXPIRED) != null) {
					// User has a expired delivery pass.
					dlvPassStatus = EnumDlvPassStatus.EXPIRED;
				} else if (statusMap.get(EnumDlvPassStatus.SHORT_SHIPPED) != null) {
					// User's delivery pass was short shipped.
					dlvPassStatus = EnumDlvPassStatus.SHORT_SHIPPED;
				} else if (statusMap.get(EnumDlvPassStatus.CANCELLED) != null) {
					// User's delivery pass was cancelled.
					dlvPassStatus = EnumDlvPassStatus.CANCELLED;
				} else if (statusMap.get(EnumDlvPassStatus.ORDER_CANCELLED) != null) {
					// User's delivery pass was cancelled.
					dlvPassStatus = EnumDlvPassStatus.ORDER_CANCELLED;
				} else if (statusMap.get(EnumDlvPassStatus.PASS_RETURNED) != null) {
					// User's delivery pass was cancelled.
					dlvPassStatus = EnumDlvPassStatus.PASS_RETURNED;
				}
				/*
				 * If none of the above conditions are satisfied then the
				 * user either no delivery pass in which case the
				 * dlvPassStatus is defaulted to NONE. In other words he is
				 * eligible for buying a new Pass.
				 */

			}

			DeliveryPassType type = null;
			Date expDate = null;
			String originalOrderId = null;
			int remDlvs = 0;
			int usedDlvs = 0;
			DeliveryPassModel model = null;
			if (!EnumDlvPassStatus.NONE.equals(dlvPassStatus)) {
				String recentDPId = (String) statusMap.get(dlvPassStatus);

				model = sb.getDeliveryPassInfo(recentDPId);
				if (model == null) {
					throw new FDResourceException(
							"Unable to locate the delivery pass for this user's account.");
				}

				if (DeliveryPassUtil.isReadyToUse(dlvPassStatus)) {
					if (model.getType().isUnlimited()
							&& statusMap.get(EnumDlvPassStatus.EXPIRED) != null) {
						String oldDPId = (String) statusMap
								.get(EnumDlvPassStatus.EXPIRED);
						DeliveryPassModel oldPass = sb
								.getDeliveryPassInfo(oldDPId);
						if (oldPass.getType().isUnlimited()) {
							if (model.getPurchaseDate().before(
									oldPass.getExpirationDate())) {
								model.setExpirationDate(DateUtil.addDays(
										oldPass.getExpirationDate(), model
												.getType().getDuration()));
								model.setOrgExpirationDate(model
										.getExpirationDate());

							}
						} else {
							Calendar cal = Calendar.getInstance(Locale.US);
							// Add duration to today's date to calculate
							// expiration date.
							cal.set(Calendar.HOUR, 11);
							cal.set(Calendar.MINUTE, 59);
							cal.set(Calendar.SECOND, 59);
							cal.set(Calendar.AM_PM, Calendar.PM);

							model.setExpirationDate(DateUtil.addDays(cal
									.getTime(), model.getType()
									.getDuration()));
							model.setOrgExpirationDate(model
									.getExpirationDate());
						}
					} else if (model.getType().isUnlimited()
							&& statusMap.get(EnumDlvPassStatus.CANCELLED) != null) {
						String oldDPId = (String) statusMap
								.get(EnumDlvPassStatus.CANCELLED);
						DeliveryPassModel oldPass = sb
								.getDeliveryPassInfo(oldDPId);
						if (oldPass.getType().isUnlimited()) {
							if (model.getPurchaseDate().before(
									oldPass.getExpirationDate())) {

								Calendar cal = Calendar
										.getInstance(Locale.US);
								cal.setTime(oldPass.getExpirationDate());
								cal.set(Calendar.HOUR, 11);
								cal.set(Calendar.MINUTE, 59);
								cal.set(Calendar.SECOND, 59);
								cal.set(Calendar.AM_PM, Calendar.PM);
								model.setExpirationDate(DateUtil.addDays(
										cal.getTime(), model.getType()
												.getDuration()));
								model.setOrgExpirationDate(model
										.getExpirationDate());
							}
						}
					}

					// Activate the Ready To Use pass.
					sb.activateReadyToUsePass(model);
					dlvPassStatus = EnumDlvPassStatus.ACTIVE;
				}// -added for DP1.1
				// Get the pass type and expiration date if unlimited.
				type = model.getType();
				if (type.isUnlimited()) {
					expDate = model.getExpirationDate();
				}
				originalOrderId = model.getPurchaseOrderId();
				remDlvs = model.getRemainingDlvs();
				usedDlvs = model.getUsageCount();
			}
			// Create FDUserDlvPassInfo object.
			int usablePassCount = Integer.parseInt(statusMap.get(
					DlvPassConstants.USABLE_PASS_COUNT).toString());
			int autoRenewUsablePassCount = Integer.parseInt(statusMap.get(
					DlvPassConstants.AUTORENEW_USABLE_PASS_COUNT)
					.toString());
			boolean isFreeTrialRestricted = ((Boolean) statusMap
					.get(DlvPassConstants.IS_FREE_TRIAL_RESTRICTED))
					.booleanValue();
			Double autoRenewDPPrice = (Double) statusMap
					.get(DlvPassConstants.AUTORENEW_DP_PRICE);
			DeliveryPassType autoRenewDPType = null;
			if (statusMap.get(DlvPassConstants.AUTORENEW_DP_TYPE) != null) {
				autoRenewDPType = (DeliveryPassType) statusMap
						.get(DlvPassConstants.AUTORENEW_DP_TYPE);
			}
				
			dlvPassInfo = new FDUserDlvPassInfo(dlvPassStatus, type,
					expDate, originalOrderId, remDlvs, usedDlvs,
					usablePassCount, isFreeTrialRestricted,
					autoRenewUsablePassCount, autoRenewDPType,
					autoRenewDPPrice.doubleValue(),(model == null)?null:model.getPurchaseDate());
			if (!EnumDlvPassStatus.NONE.equals(dlvPassStatus)
					&& (type.isUnlimited())
					&& (EnumDlvPassStatus.CANCELLED.equals(dlvPassStatus) || EnumDlvPassStatus.EXPIRED
							.equals(dlvPassStatus))) {
				dlvPassInfo.setDaysSinceDPExpiry(sb
						.getDaysSinceDPExpiry(customerPk));
			} else if (!EnumDlvPassStatus.NONE.equals(dlvPassStatus)
					&& type.isUnlimited()) {
				dlvPassInfo.setDaysToDPExpiry(sb.getDaysToDPExpiry(
						customerPk, model.getId()));
			}

			double savings = 0.0;
			String result="";
			// Get the Active delivery Pass for this account.
			DlvPassManagerSB dlvpsb = this.getDlvPassManagerHome().create();
			List<DeliveryPassModel> dlvPasses = dlvpsb.getDlvPassesByStatus(customerPk,EnumDlvPassStatus.ACTIVE);
			if (dlvPasses == null
					|| (dlvPasses != null && dlvPasses.size() == 0)) {
				dlvPassInfo.setDPSavings(0.0);
		} else {
				// Get the Active delivery pass from the list.
				DeliveryPassModel dlvPass = dlvPasses.get(0);	
				Connection conn = null;
				conn = getConnection();
				result = FDCustomerOrderInfoDAO.getActiveDeliveryPassSavings(conn, customerPk, dlvPass.getPK().getId());
				close(conn);
				savings = Double.parseDouble(result);
				dlvPassInfo.setDPSavings(savings);
			}
		} else {
			// Identity will be null when he/she is a anonymous user. Create
			// a default info object.
			dlvPassInfo = new FDUserDlvPassInfo(EnumDlvPassStatus.NONE,
					null, null, null, 0, 0, 0, false, 0, null, 0,null);
			dlvPassInfo.setDPSavings(0.0);
		}
		return dlvPassInfo;
	}

	public FDUser recognize(String cookie, EnumEStoreId eStoreId) throws FDAuthenticationException,
			FDResourceException {

		Connection conn = null;
		try {
			conn = getConnection();

			FDUser user = FDUserDAO.reconnizeWithCookie(conn, cookie, eStoreId);

			LOGGER.debug("got FDUser via cookie id"+ user.getUserServiceType());

			if (user.isAnonymous()) {
				throw new FDAuthenticationException("Unrecognized user");
			}

			// Load Promo Audience Details for this customer.
			user.setAssignedCustomerParams(getAssignedCustomerParams(user,conn));

			user.setDlvPassInfo(getDeliveryPassInfo(user));
			return user;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	public Map getAssignedCustomerParams(FDUser user) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return getAssignedCustomerParams(user,conn);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}

	/**
	 * Returns a Map containing the audience promo ID and audience details.
	 * promoId --> FDPromoAudienceInfo.
	 * 
	 * @param user
	 * @return
	 * @throws SQLException
	 */
	private Map getAssignedCustomerParams(FDUser user, Connection conn) throws SQLException {
		Map assignedParams = null;
		FDIdentity identity = user.getIdentity();
		if (identity != null) {
			assignedParams = FDPromotionNewDAO.loadAssignedCustomerParams(conn,
					identity.getErpCustomerPK(), user.getUserId());
		} else {
			assignedParams = new HashMap();
		}
		return assignedParams;
	}

	public ErpAddressModel assumeDeliveryAddress(FDIdentity identity, String lastOrderId) throws FDResourceException {
		try {
			FDCustomerEB eb = getFdCustomerHome().findByPrimaryKey(
					new PrimaryKey(identity.getFDCustomerPK()));
			String addressId = eb.getDefaultShipToAddressPK();
			ErpAddressModel address = null;
			if (addressId != null) {
				address = this.getShipToAddress(addressId);
			}
			// if default address has been deleted, use address of last order
			if (address == null) {
				if (lastOrderId != null) {
					address = this.getLastOrderAddress(lastOrderId);
				}
			}
			if (address != null) {
				try{
					FDDeliveryManager.getInstance().scrubAddress(address);
				}catch (FDInvalidAddressException e) {
					//TODO Ignore the Invalid Address Exception for scrub logic
					LOGGER.info("Exception while geocoding the address");
					//throw new FDResourceException(e);
				}
			}
			return address;
		} catch (FinderException fe) {
			throw new FDResourceException(fe);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		}/* catch (FDInvalidAddressException e) {
			throw new FDResourceException(e);
		}*/
	}

	private static final String SHIP_TO_ADDRESS_QUERY = "SELECT * FROM CUST.ADDRESS WHERE ID = ?";

	private ErpAddressModel getShipToAddress(String addressId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement(SHIP_TO_ADDRESS_QUERY);
			ps.setString(1, addressId);
			rs = ps.executeQuery();
			ErpAddressModel address = null;
			if (rs.next()) {
				address = this.loadDeliveryAddressFromResultSet(rs);
				address.setPK(new PrimaryKey(rs.getString("ID")));
				address.setCompanyName(rs.getString("COMPANY_NAME"));
				address.setServiceType(EnumServiceType.getEnum(rs
						.getString("SERVICE_TYPE")));
			}
			return address;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			close(conn);
		}
	}

	private static final String LAST_ORDER_ADDRESS_QUERY = "select di.*, sa.customer_id "
			+ "from cust.sale s, cust.salesaction sa, cust.deliveryinfo di "
			+ "where s.id = ? and s.id = sa.sale_id and sa.id = di.salesaction_id "
			+ "and s.type = 'REG' "
			+ "and sa.action_type in ('CRO', 'MOD') "
			+ "and sa.action_date = (select max(action_date) from cust.salesaction where s.id = sale_id and action_type in ('CRO', 'MOD'))";

	public ErpAddressModel getLastOrderAddress(String lastOrderId) throws SQLException, FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement(LAST_ORDER_ADDRESS_QUERY);
			ps.setString(1, lastOrderId);
			rs = ps.executeQuery();
			if (rs.next()) {
				return this.loadAddressFromResultSet(rs);
			} else {
				throw new SQLException("Cannot find address for order ID: "
						+ lastOrderId);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			close(conn);
		}
	}

	private ErpAddressModel loadAddressFromResultSet(ResultSet rs) throws SQLException, FDResourceException {
		ErpAddressModel address;
		if (rs.getString("DEPOTLOCATION_ID") == null){
			address = loadDeliveryAddressFromResultSet(rs);
		}else {
			address = loadDepotAddressFromResultSet(rs);
		}
		return address;
	}
	
	private ErpAddressModel loadDeliveryAddressFromResultSet(ResultSet rs) throws SQLException {
		ErpAddressModel address = new ErpAddressModel();
		address.setFirstName(rs.getString("FIRST_NAME"));
		address.setLastName(rs.getString("LAST_NAME"));
		address.setAddress1(rs.getString("ADDRESS1"));
		address.setAddress2(rs.getString("ADDRESS2"));
		address.setApartment(rs.getString("APARTMENT"));
		address.setCity(rs.getString("CITY"));
		address.setState(rs.getString("STATE"));
		address.setZipCode(rs.getString("ZIP"));
		address.setCountry(rs.getString("COUNTRY"));
		address.setPhone(new PhoneNumber(rs.getString("PHONE")));
		address.setAltFirstName(rs.getString("ALT_FIRST_NAME"));
		address.setAltLastName(rs.getString("ALT_LAST_NAME"));
		address.setAltApartment(rs.getString("ALT_APARTMENT"));
		address.setAltPhone(new PhoneNumber(rs.getString("ALT_PHONE")));
		address.setAltContactPhone(new PhoneNumber(rs
				.getString("ALT_CONTACT_PHONE")));
		address.setInstructions(rs.getString("DELIVERY_INSTRUCTIONS"));
		address.setAltDelivery(EnumDeliverySetting.getDeliverySetting(rs
				.getString("ALT_DEST")));
		address.setScrubbedStreet(rs.getString("SCRUBBED_ADDRESS"));
		address.setCustomerId(rs.getString("CUSTOMER_ID"));
		

		return address;
	}
	
	private ErpDepotAddressModel loadDepotAddressFromResultSet(ResultSet rs) throws SQLException, FDResourceException {
		ErpDepotAddressModel address = new ErpDepotAddressModel();
		address.setLocationId( rs.getString("DEPOTLOCATION_ID"));
		address.setFirstName(rs.getString("FIRST_NAME"));
		address.setLastName(rs.getString("LAST_NAME"));
		address.setAddress1(rs.getString("ADDRESS1"));
		address.setAddress2(rs.getString("ADDRESS2"));
		address.setApartment(rs.getString("APARTMENT"));
		address.setCity(rs.getString("CITY"));
		address.setState(rs.getString("STATE"));
		address.setZipCode(rs.getString("ZIP"));
		address.setCountry(rs.getString("COUNTRY"));
		address.setPhone(new PhoneNumber(rs.getString("PHONE")));
		address.setAltFirstName(rs.getString("ALT_FIRST_NAME"));
		address.setAltLastName(rs.getString("ALT_LAST_NAME"));
		address.setAltApartment(rs.getString("ALT_APARTMENT"));
		address.setAltPhone(new PhoneNumber(rs.getString("ALT_PHONE")));
		address.setAltContactPhone(new PhoneNumber(rs
				.getString("ALT_CONTACT_PHONE")));
		address.setInstructions(rs.getString("DELIVERY_INSTRUCTIONS"));
		address.setAltDelivery(EnumDeliverySetting.getDeliverySetting(rs
				.getString("ALT_DEST")));
		address.setScrubbedStreet(rs.getString("SCRUBBED_ADDRESS"));
		address.setCustomerId(rs.getString("CUSTOMER_ID"));

		// TODO Reconsider this solution!
        for (FDDeliveryDepotModel pickupModel : FDDeliveryManager.getInstance().getPickupDepots()) {
            if (pickupModel.getLocation(rs.getString("DEPOTLOCATION_ID")) != null) {
                address.setPickup(pickupModel.isPickup());
            }
        }
		
		return address;
	}

	/**
	 * Authenticate and log in a customer.
	 * 
	 * @param userId
	 * @param password
	 * 
	 * @return user identity reference
	 * 
	 * @throws FDAuthenticationException
	 *             if the userId/password was not found
	 * @throws FDResourceException
	 *             if an error occured using remote resources
	 */
	public FDIdentity login(String userId, String password) throws FDAuthenticationException, FDResourceException {
		try {
			// find ERPCustomerEB by userid & password
			ErpCustomerEB erpCustomerEB;
			try {
				erpCustomerEB = this.getErpCustomerHome()
						.findByUserIdAndPasswordHash(userId,
								MD5Hasher.hash(password));
			} catch (ObjectNotFoundException ex) {
				throw new FDAuthenticationException(
						"Invalid username or password");
			}
			
			// check the active flag
			if (!erpCustomerEB.isActive()) {
				
				/*FDCustomerEB fdCustomerEB=getFdCustomerHome().findByErpCustomerId(erpCustomerEB.getPK().getId());
				if(fdCustomerEB!=null && fdCustomerEB.getPymtVerifyAttempts()>=FDStoreProperties.getPaymentMethodVerificationLimit()) {
					
				}
				else {
					throw new FDAuthenticationException("Account disabled");
				}*/
				throw new FDAuthenticationException("Account disabled");
			}
			// find respective FDCustomerEB
			String erpCustId = erpCustomerEB.getPK().getId();
			FDCustomerEB fdCustomerEB = this.getFdCustomerHome()
					.findByErpCustomerId(erpCustomerEB.getPK().getId());
			fdCustomerEB.incrementLoginCount();

			// String cookie = this.translate( erpCustomerEB.getUserId() );
			return new FDIdentity(erpCustId, fdCustomerEB.getPK().getId());

		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}
	
	public FDIdentity login(String userId) throws FDAuthenticationException, FDResourceException {
		try {
			// find ERPCustomerEB by userid & password
			ErpCustomerEB erpCustomerEB;
			try {
				
				erpCustomerEB = this.getErpCustomerHome().findByUserId(userId);
				
			} catch (ObjectNotFoundException ex) {
				throw new FDAuthenticationException(
						"Invalid username or password");
			}
			
			// check the active flag
			if (!erpCustomerEB.isActive()) {
				
				/*FDCustomerEB fdCustomerEB=getFdCustomerHome().findByErpCustomerId(erpCustomerEB.getPK().getId());
				if(fdCustomerEB!=null && fdCustomerEB.getPymtVerifyAttempts()>=FDStoreProperties.getPaymentMethodVerificationLimit()) {
					
				}
				else {
					throw new FDAuthenticationException("Account disabled");
				}*/
				throw new FDAuthenticationException("Account disabled");
			}
			// find respective FDCustomerEB
			String erpCustId = erpCustomerEB.getPK().getId();
			FDCustomerEB fdCustomerEB = this.getFdCustomerHome()
					.findByErpCustomerId(erpCustomerEB.getPK().getId());
			fdCustomerEB.incrementLoginCount();

			// String cookie = this.translate( erpCustomerEB.getUserId() );
			return new FDIdentity(erpCustId, fdCustomerEB.getPK().getId());

		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}


	
	
	public FDCustomerInfo getCustomerInfo(FDIdentity identity) throws FDResourceException {
		try {
			String erpCustomerPK = identity.getErpCustomerPK();
			ErpCustomerEB eb = getErpCustomerHome().findByPrimaryKey(
					new PrimaryKey(erpCustomerPK));

			FDUser fduser = this.recognize(identity);

			ErpCustomerInfoModel erpCustomerInfo = eb.getCustomerInfo();
			FDCustomerInfo fdInfo = new FDCustomerInfo(erpCustomerInfo
					.getFirstName(), erpCustomerInfo.getLastName());
			fdInfo.setHtmlEmail(!erpCustomerInfo.isEmailPlaintext());
			fdInfo.setEmailAddress(erpCustomerInfo.getEmail());			

			if(identity.getFDCustomerPK() != null) {
				String depotCode = this.getDepotCode(identity);
				fdInfo.setDepotCode(depotCode);
			}
			fdInfo.setChefsTable(fduser.isChefsTable());
			fdInfo.setCustomerServiceContact(fduser.getCustomerServiceContact());
			
			/*APPDEV-2114*/
			fdInfo.setGoGreen(erpCustomerInfo.isGoGreen());
			
			return fdInfo;

		} catch (FinderException fe) {
			throw new FDResourceException(fe);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FDAuthenticationException au) {
			throw new FDResourceException(au);
		}
	}
	
	
	public FDCustomerInfo getSOCustomerInfo(FDIdentity identity) throws FDResourceException {
		try {
			String erpCustomerPK = identity.getErpCustomerPK();
			ErpCustomerEB eb = getErpCustomerHome().findByPrimaryKey(
					new PrimaryKey(erpCustomerPK));

			ErpCustomerInfoModel erpCustomerInfo = eb.getCustomerInfo();
			FDCustomerInfo fdInfo = new FDCustomerInfo(erpCustomerInfo
					.getFirstName(), erpCustomerInfo.getLastName());
			fdInfo.setHtmlEmail(!erpCustomerInfo.isEmailPlaintext());
			fdInfo.setEmailAddress(erpCustomerInfo.getEmail());			

			if(identity.getFDCustomerPK() != null) {
				String depotCode = this.getDepotCode(identity);
				fdInfo.setDepotCode(depotCode);
			}
			/*APPDEV-2114*/
			fdInfo.setGoGreen(erpCustomerInfo.isGoGreen());
			
			return fdInfo;

		} catch (FinderException fe) {
			throw new FDResourceException(fe);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}
	

	/**
	 * Get all the payment methods of the customer.
	 * 
	 * @param identity
	 *            the customer's identity reference
	 * 
	 * @return collection of ErpPaymentMethodModel objects
	 * @throws FDResourceException
	 *             if an error occured using remote resources
	 */
	public Collection<ErpPaymentMethodI> getPaymentMethods(FDIdentity identity) throws FDResourceException {
		try {
			ErpCustomerEB erpCustomerEB = this.getErpCustomerHome()
					.findByPrimaryKey(
							new PrimaryKey(identity.getErpCustomerPK()));
			return erpCustomerEB.getPaymentMethods();
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}

	/**
	 * Add a payment method for the customer.
	 * 
	 * @param identity
	 *            the customer's identity reference
	 * @param paymentMethod
	 *            ErpPaymentMethodI to add
	 * 
	 * @throws FDResourceException
	 *             if an error occured using remote resources
	 */
	public void addPaymentMethod(FDActionInfo info, ErpPaymentMethodI paymentMethod, boolean paymentechEnabled) 
		throws FDResourceException, ErpPaymentMethodException {
		try {
			
			ErpCustomerEB erpCustomerEB = checkPaymentMethodModification(info, paymentMethod);

			Gateway gateway = null;
			
			if(paymentechEnabled &&( EnumPaymentMethodType.CREDITCARD.equals(paymentMethod.getPaymentMethodType())||
					                 EnumPaymentMethodType.ECHECK.equals(paymentMethod.getPaymentMethodType())
					)){ //add payment method profile to orbital. we need to add it as part of this transaction.
				try {				
					//first verify the payment method with orbital/paymentech. if success then add the profile.
					gateway = GatewayFactory.getGateway(GatewayType.PAYMENTECH);
					ErpAuthorizationModel authModel = gateway.verify(ErpAffiliate.getPrimaryAffiliate(info.geteStore()).getMerchant(paymentMethod.getCardType()),paymentMethod);
					
					if(authModel.isApproved()) {
						Request request = GatewayAdapter.getAddProfileRequest(paymentMethod);
						Response response = gateway.addProfile(request);
						if(response!=null&& response.isRequestProcessed() && !response.isError()){
							
							if(!StringUtil.isEmpty(response.getBillingInfo().getPaymentMethod().getBillingProfileID())) {
								paymentMethod.setProfileID(response.getBillingInfo().getPaymentMethod().getBillingProfileID());
							} else {
								throw new ErpTransactionException(response.getStatusMessage());
							}
						} else {
							
							throw new ErpTransactionException(response.getStatusMessage());
						}
					}else{
						
						if(StringUtils.isEmpty(authModel.getAuthCode())) {
							throw new ErpTransactionException();
						} 
						if(!authModel.isZipMatch())
							throw new ErpTransactionException("AVS");
						else if(!authModel.isCVVMatch())
							throw new ErpTransactionException("CVV");
						else 
							throw new ErpTransactionException();
						
					}
				} catch (ErpTransactionException e) {
					this.getSessionContext().setRollbackOnly();
					throw new ErpPaymentMethodException(e.getMessage());
				}
			}
			
			erpCustomerEB.addPaymentMethod(paymentMethod);
			
			String note = paymentMethod.getMaskedAccountNumber() + (paymentMethod.getIsTermsAccepted() ? ", enrollment agreement" : "");
			
			note = getByPassAvsNotes(info, paymentMethod, note);
			
			this.logActivity(info.createActivity(EnumAccountActivityType.ADD_PAYMENT_METHOD, note));

		} catch (RemoteException re) {
			deleteProfile(paymentMethod);
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		} catch (CreateException ex) {
			deleteProfile(paymentMethod);
			throw new FDResourceException(ex);
		}
	}

	private void deleteProfile(ErpPaymentMethodI paymentMethod){
		if(paymentMethod.getProfileID()!=null){ //payment method profile exists in orbital. we need to remove it as part of this transaction.
			try {
			Gateway g = GatewayFactory.getGateway(GatewayType.PAYMENTECH);
			Request request=GatewayAdapter.getDeleteProfileRequest(paymentMethod);
			g.deleteProfile(request);
			} catch (ErpTransactionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(); //ignoring it if the delete profile fails. we still allow deleting the payment in DB.
			}
		}
	}
	private String getByPassAvsNotes(FDActionInfo info, ErpPaymentMethodI paymentMethod, String note)
	{
		try
		{
			if(paymentMethod.getAddress()!=null && paymentMethod.isBypassAVSCheck())
			{
				FDUserI user = recognize(info.getIdentity());
				
				ContactAddressModel address = paymentMethod.getAddress();
				note = note + " Bypass AVS Check:Y, Customer email: "+user.getUserId()
						+ " Address: "+address.getAddress1()+ " "+address.getApartment()+ " "+address.getAddress2()
						+" "+address.getCity()+" "+address.getState()+" "+address.getZipCode();
			}
		}
		catch(Exception e)
		{
			LOGGER.debug(e.getMessage());
		}
		return note;
		
	}
	/**
	 * set the default payment method on the customer
	 * 
	 * @param identy
	 *            the customer's identity
	 * @param PK
	 *            primary key of the paymentMethod that will be referenced.
	 * 
	 *            Throws FDResourceException
	 */
	public void setDefaultPaymentMethod(FDActionInfo info,
			PrimaryKey paymentMethodPK) throws FDResourceException {
		try {
			FDCustomerEB eb = this.getFdCustomerHome().findByPrimaryKey(
					new PrimaryKey(info.getIdentity().getFDCustomerPK()));
			eb.setDefaultPaymentMethodPK(paymentMethodPK.getId());
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException fe) {
			throw new FDResourceException(fe);
		}
	}

	public boolean checkBillToAddressFraud(FDActionInfo info,
			ErpPaymentMethodI paymentMethod) throws FDResourceException {
		try {
			ErpFraudPreventionSB fraudSB = this.getErpFraudHome().create();
			if (fraudSB.checkBillToAddressFraud(info.getIdentity()
					.getErpCustomerPK(), paymentMethod.getAddress())) {
				info.setSource(EnumTransactionSource.SYSTEM);
				this.setSignupPromotionEligibility(info, false);
				return true;
			}
			return false;
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}

	/***************************************************************************
	 * return the Pk Id that is stored in the defaultPaymentMethodPK field @
	 * param Identity the customers Identity
	 * 
	 * Throws FDresourceException
	 */
	public String getDefaultPaymentMethodPK(FDIdentity identity)
			throws FDResourceException {
		try {
			FDCustomerEB fdCustomerEB = this.getFdCustomerHome()
					.findByPrimaryKey(
							new PrimaryKey(identity.getFDCustomerPK()));
			return fdCustomerEB.getDefaultPaymentMethodPK();
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}

	public void setDefaultDepotLocationPK(FDIdentity identity, String locationId)
			throws FDResourceException {
		try {
			FDCustomerEB fdCustomerEB = this.getFdCustomerHome()
					.findByPrimaryKey(
							new PrimaryKey(identity.getFDCustomerPK()));
			fdCustomerEB.setDefaultDepotLocationPK(locationId);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}

	public String getDefaultDepotLocationPK(FDIdentity identity)
			throws FDResourceException {
		try {
			FDCustomerEB fdCustomerEB = this.getFdCustomerHome()
					.findByPrimaryKey(
							new PrimaryKey(identity.getFDCustomerPK()));
			return fdCustomerEB.getDefaultDepotLocationPK();
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}

	/**
	 * update a payment method for the customer
	 * 
	 * @param identity
	 *            the customer's identity reference
	 * @param paymentMethod
	 *            ErpPaymentMethodI to update
	 * 
	 * @throws FDResourceException
	 *             if an error occured using remote resources
	 */
	public void updatePaymentMethod(FDActionInfo info,
			ErpPaymentMethodI paymentMethod) throws FDResourceException,
			 ErpPaymentMethodException {
		try {
			ErpCustomerEB erpCustomerEB = checkPaymentMethodModification(info, paymentMethod);

			if(EnumPaymentMethodType.EBT.equals(paymentMethod.getPaymentMethodType())) {
				ErpPaymentMethodModel _payment=(ErpPaymentMethodModel)paymentMethod;
				_payment.setAccountNumLast4("");
			}
			erpCustomerEB.updatePaymentMethod(paymentMethod);
			
			if(!StringUtil.isEmpty(paymentMethod.getProfileID())){ //update payment method profile to orbital. we need to update it as part of this transaction.
				try {
				Gateway g = GatewayFactory.getGateway(GatewayType.PAYMENTECH);
				
				//first verify the payment method with orbital/paymentech. if success then add the profile.
				ErpAuthorizationModel authModel = g.verify(ErpAffiliate.getPrimaryAffiliate(info.geteStore()).getMerchant(paymentMethod.getCardType()),paymentMethod);
				if(authModel.isApproved()) {
					Request request = GatewayAdapter.getUpdateProfileRequest(paymentMethod);
					Response response = g.updateProfile(request);
					if(response!=null&& response.isRequestProcessed())
						authModel.setProfileID(response.getBillingInfo().getPaymentMethod().getBillingProfileID());
				}else{
					if(!authModel.isZipMatch())
						throw new ErpTransactionException("AVS");
					else if(!authModel.isCVVMatch())
						throw new ErpTransactionException("CVV");
					else 
						throw new ErpTransactionException();
				}
				} catch (ErpTransactionException e) {
					this.getSessionContext().setRollbackOnly();
					throw new ErpPaymentMethodException(e.getMessage());
				}
			}
			
			String note = paymentMethod.getPK().getId();
			
			note = getByPassAvsNotes(info, paymentMethod, note);
			
			this.logActivity(info.createActivity(EnumAccountActivityType.UPDATE_PAYMENT_METHOD, note));

		} catch (RemoteException ex) {
			throw new FDResourceException(ex);
		} catch (FinderException ex) {
			throw new FDResourceException(ex);
		} catch (CreateException ex) {
			throw new FDResourceException(ex);
		}

	}
	
    private ErpCustomerEB checkPaymentMethodModification(FDActionInfo info, ErpPaymentMethodI paymentMethod) throws FinderException, RemoteException,
            CreateException, ErpPaymentMethodException {
        ErpCustomerEB erpCustomerEB = this.getErpCustomerHome().findByPrimaryKey(
        	new PrimaryKey(info.getIdentity().getErpCustomerPK()));

        //
        // DO FRAUD CHECK
        //
       //Removing the duplicate payment method check. we dont require this as the new gateway supports adding the same payment method for multiple accounts.
       /* ErpFraudPreventionSB fraudSB = getErpFraudHome().create();
        boolean foundFraud = fraudSB.checkDuplicatePaymentMethodFraud(info.getIdentity().getErpCustomerPK(), paymentMethod);
        if (foundFraud) {
        	this.getSessionContext().setRollbackOnly();
        	throw new ErpDuplicatePaymentMethodException("Duplicate account information.");
        }*/

        //
        // Check external negative file for E-Checks, Credit Cards will alway return APPROVED
        //
        /*boolean foundFraud = false;
        try {
        	PaymentFraudManager paymentFraudManager = new PaymentFraudManager();
        	if (!paymentFraudManager.verifyAccountExternal(paymentMethod)) {
        		foundFraud = true;
        	}
        } catch (ErpTransactionException e) {
        	foundFraud = false;  // if there's an exception (can't communication with external source) --> allow user to add payment method
        }
        if (foundFraud) {
        	this.getSessionContext().setRollbackOnly();
        	throw new ErpPaymentMethodException("Account is not valid");
        }*/
        return erpCustomerEB;
    }


	/**
	 * remove a payment method for the customer
	 * 
	 * @param identity
	 *            the customer's identity reference
	 * @param pk
	 *            PrimaryKey of the paymentMethod to remove
	 * 
	 *            throws FDResourceException if an error occured using remote
	 *            resources
	 */
	public void removePaymentMethod(FDActionInfo info,  ErpPaymentMethodI paymentMethod)
			throws FDResourceException {
		try {
			ErpCustomerEB erpCustomerEB = this.getErpCustomerHome()
					.findByPrimaryKey(
							new PrimaryKey(info.getIdentity()
									.getErpCustomerPK()));
			erpCustomerEB.removePaymentMethod(paymentMethod.getPK());
			
			RestrictedPaymentMethodModel restrictedPymtMethod=PaymentFraudManager.getRestrictedPaymentMethodByPaymentMethodId(paymentMethod.getPK().getId(),null);
			if(restrictedPymtMethod!=null) {
				PaymentFraudManager.removeRestrictedPaymentMethod(restrictedPymtMethod.getPK(), EnumTransactionSource.SYSTEM.getCode());
			}
			
			/*if(paymentMethod.getProfileID()!=null){ //payment method profile exists in orbital. we need to remove it as part of this transaction.
				try {
				Gateway g = GatewayFactory.getGateway(GatewayType.PAYMENTECH);
				Request request=GatewayAdapter.getDeleteProfileRequest(paymentMethod);
				g.deleteProfile(request);
				} catch (ErpTransactionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(); //ignoring it if the delete profile fails. we still allow deleting the payment in DB.
				}
			}*/
			if(StringUtil.isEmpty(paymentMethod.getProfileID())) {
					this.logActivity(info.createActivity(
							EnumAccountActivityType.DELETE_PAYMENT_METHOD, paymentMethod.getPK().getId()));
			} else {
				this.logActivity(info.createActivity(
						EnumAccountActivityType.DELETE_PAYMENT_METHOD, paymentMethod.getPK().getId()+" Profile ID :"+paymentMethod.getProfileID()));
			}

		} catch (RemoteException ex) {
			throw new FDResourceException(ex);
		} catch (FinderException ex) {
			throw new FDResourceException(ex);
		}
	}

	
	public boolean isDisplayNameUsed(String displayName,String custId) throws ErpDuplicateDisplayNameException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try{
			con = this.getConnection();
			ps = con.prepareStatement("SELECT 1 FROM CUST.CUSTOMERINFO WHERE UPPER(DISPLAY_NAME) = UPPER(?) and CUSTOMER_ID <> ?");

			ps.setString(1, displayName);
			ps.setString(2, custId);
			
			rs = ps.executeQuery();

			if(rs.next()){
				throw new ErpDuplicateDisplayNameException("That Display Name is already in use. Please try entering a different Display Name.");
			}
			
		}catch(SQLException se){
			
		} finally {
			try {
				if(rs != null) rs.close();
				if(ps != null) ps.close();
				if(con != null) con.close();
			} catch(SQLException ex) {
				//eat it for the time being
			}
		}
		return false;
	}
	/**
	 * update the customer info
	 * 
	 * @param identity
	 *            the customer's identity reference
	 * @param ErpCustomerInfoModel
	 *            to update
	 * @param String
	 *            password hint
	 * 
	 * @throws FDResourceException
	 *             if an error occured using remote resources
	 */
	public boolean updateCustomerInfo(FDActionInfo info,
			ErpCustomerInfoModel customerInfo) throws FDResourceException {
		try {

			Collection<PhoneNumber> phones = new ArrayList<PhoneNumber>();
			if (customerInfo.getHomePhone() != null)
				phones.add(customerInfo.getHomePhone());
			if (customerInfo.getBusinessPhone() != null)
				phones.add(customerInfo.getBusinessPhone());
			if (customerInfo.getCellPhone() != null)
				phones.add(customerInfo.getCellPhone());
			if (customerInfo.getOtherPhone() != null)
				phones.add(customerInfo.getOtherPhone());
			LOGGER.debug("Found " + phones.size()
					+ " non-null phone numbers for this account.");

			ErpFraudPreventionSB fraudSB = getErpFraudHome().create();
			boolean foundFraud = fraudSB.checkPhoneFraud(info.getIdentity()
					.getErpCustomerPK(), phones);

			LOGGER.debug("updating ErpCustomerInfo...");
			ErpCustomerEB erpCustomerEB = this.getErpCustomerHome()
					.findByPrimaryKey(
							new PrimaryKey(info.getIdentity()
									.getErpCustomerPK()));
			erpCustomerEB.setCustomerInfo(customerInfo);

			if (foundFraud) {
				// !!! override tx source
				this.logActivity(info.createActivity(
						EnumAccountActivityType.UPDATE_ERP_CUSTOMERINFO,
						EnumFraudReason.DUP_PHONE.getDescription()));
//				this.setSignupPromotionEligibility(info, false);
			}

			this.logActivity(info.createActivity(EnumAccountActivityType.UPDATE_ERP_CUSTOMERINFO));

			return foundFraud;

		} catch (RemoteException ex) {
			throw new FDResourceException(ex);
		} catch (FinderException ex) {
			throw new FDResourceException(ex);
		} catch (CreateException ex) {
			throw new FDResourceException(ex);
		}
		
	}

	public void updateUserId(FDActionInfo info, String userId)
			throws FDResourceException, ErpDuplicateUserIdException {
		try {

			ErpCustomerEB erpCustomerEB = this.getErpCustomerHome()
					.findByPrimaryKey(
							new PrimaryKey(info.getIdentity()
									.getErpCustomerPK()));
			erpCustomerEB.updateUserId(userId);

			this.logActivity(info.createActivity(EnumAccountActivityType.UPDATE_ERP_CUSTOMER));

		} catch (RemoteException ex) {
			throw new FDResourceException(ex);
		} catch (FinderException ex) {
			throw new FDResourceException(ex);
		}
	}

	public void updatePasswordHint(FDIdentity identity, String passwordHint)
			throws FDResourceException {
		try {
			LOGGER.debug("updating FDCustomer...");
			FDCustomerEB fdCustomerEB = this.getFdCustomerHome()
					.findByPrimaryKey(
							new PrimaryKey(identity.getFDCustomerPK()));
			fdCustomerEB.updatePasswordHint(passwordHint);

		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}

	/**
	 * Get the customer's every ship to address.
	 * 
	 * @param identity
	 *            the customer's identity reference
	 * 
	 * @return collection of ErpAddresModel objects
	 * 
	 * @throws FDResourceException
	 *             if an error occured using remote resources
	 */
	public Collection<ErpAddressModel> getShipToAddresses(FDIdentity identity)
			throws FDResourceException {
		try {
			ErpCustomerEB erpCustomerEB = this.getErpCustomerHome()
					.findByPrimaryKey(
							new PrimaryKey(identity.getErpCustomerPK()));
			return erpCustomerEB.getShipToAddresses();
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}

	/**
	 * Add a ship to address for the customer.
	 * 
	 * @param identity
	 *            the customer's identity reference
	 * @param address
	 *            ErpAddressModel to add
	 * 
	 * @throws FDResourceException
	 *             if an error occured using remote resources
	 */
	public boolean addShipToAddress(FDActionInfo info, boolean checkUniqueness,
			ErpAddressModel address) throws FDResourceException,
			ErpDuplicateAddressException {
		try {
			//
			// Do fraud check
			//
			ErpFraudPreventionSB fraudSB = getErpFraudHome().create();

			String erpCustomerId = info.getIdentity().getErpCustomerPK();

			boolean foundFraud = false;
			if (checkUniqueness) {
				foundFraud = fraudSB.checkShipToAddressFraud(erpCustomerId,
						address);
				if (foundFraud) {
					// !!! override tx source
					this.logActivity(info
							.createActivity(
									EnumAccountActivityType.ADD_DLV_ADDRESS,
									EnumFraudReason.DUP_SHIPTO_ADDRESS
											.getDescription()));
					this.setSignupPromotionEligibility(info, false);
				}
			}

			ErpCustomerEB erpCustomerEB = this.getErpCustomerHome()
					.findByPrimaryKey(new PrimaryKey(erpCustomerId));
			erpCustomerEB.addShipToAddress(address);

			return foundFraud;

		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ex) {
			throw new FDResourceException(ex);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}
	
	/**
	 * update a ship to address for the customer
	 * 
	 * @param identity
	 *            the customer's identity reference
	 * @param address
	 *            ErpAddressModel to update
	 * 
	 * @throws FDResourceException
	 *             if an error occured using remote resources
	 */
	public boolean updateShipToAddress(FDActionInfo info,
			boolean checkUniqueness, ErpAddressModel address)
			throws FDResourceException, ErpDuplicateAddressException {
		try {
			String erpCustomerId = info.getIdentity().getErpCustomerPK();
			//
			// Do fraud check
			//
			boolean foundFraud = false;
			if (checkUniqueness) {
				ErpFraudPreventionSB fraudSB = getErpFraudHome().create();
				foundFraud = fraudSB.checkShipToAddressFraud(erpCustomerId,
						address);
			}

			ErpCustomerEB erpCustomerEB = this.getErpCustomerHome()
					.findByPrimaryKey(new PrimaryKey(erpCustomerId));
			erpCustomerEB.updateShipToAddress(address);

			if (foundFraud) {
				// !!! override tx source
				this.logActivity(info.createActivity(
						EnumAccountActivityType.UPDATE_DLV_ADDRESS,
						EnumFraudReason.DUP_SHIPTO_ADDRESS.getDescription()));
				this.setSignupPromotionEligibility(info, false);
			}

			this.logActivity(info.createActivity(
					EnumAccountActivityType.UPDATE_DLV_ADDRESS, address.getPK()
							.getId()));

			return foundFraud;

		} catch (RemoteException ex) {
			throw new FDResourceException(ex);
		} catch (FinderException ex) {
			throw new FDResourceException(ex);
		} catch (CreateException ex) {
			throw new FDResourceException(ex);
		}
	}

	/**
	 * set the default ship to Address PK on the customer
	 * 
	 * @param identy
	 *            the customer's identity
	 * @param PK
	 *            primary key of the Ship-To-Address.
	 * 
	 *            Throws FDResourceException
	 */
	public void setDefaultShipToAddressPK(FDIdentity identity,
			String shipToAddressPK) throws FDResourceException {
		try {
			FDCustomerEB fdCustomerEB = this.getFdCustomerHome()
					.findByPrimaryKey(
							new PrimaryKey(identity.getFDCustomerPK()));
			fdCustomerEB.setDefaultShipToAddressPK(shipToAddressPK);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}

	/***************************************************************************
	 * return the Pk Id that is stored in the defaultPShipToAddressPK field @
	 * param Identity the customers Identity
	 * 
	 * Throws FDresourceException
	 */
	public String getDefaultShipToAddressPK(FDIdentity identity)
			throws FDResourceException {
		try {
			FDCustomerEB fdCustomerEB = this.getFdCustomerHome()
					.findByPrimaryKey(
							new PrimaryKey(identity.getFDCustomerPK()));
			return fdCustomerEB.getDefaultShipToAddressPK();
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}
	
	public void setFdxSmsPreferences(FDCustomerEStoreModel customerSmsPreferenceModel, String ErpCustomerPK) throws FDResourceException {
		try {
			
			FDCustomerEB fdCustomerEB = this.getFdCustomerHome()
					.findByErpCustomerId(ErpCustomerPK);
			FDCustomerModel model = (FDCustomerModel) fdCustomerEB.getModel();
			model.setCustomerSmsPreferenceModel(customerSmsPreferenceModel);
			fdCustomerEB.setFromModel(model);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}
	
	

	/**
	 * remove a ship to address for the customer
	 * 
	 * @param identity
	 *            the customer's identity reference
	 * @param pk
	 *            PrimaryKey of the address to remove
	 * 
	 * @throws FDResourceException
	 *             if an error occured using remote resources
	 */

	public void removeShipToAddress(FDActionInfo info, PrimaryKey pk)
			throws FDResourceException {
		try {
			ErpCustomerEB erpCustomerEB = this.getErpCustomerHome()
					.findByPrimaryKey(
							new PrimaryKey(info.getIdentity()
									.getErpCustomerPK()));
			erpCustomerEB.removeShipToAddress(pk);

			this.logActivity(info.createActivity(
					EnumAccountActivityType.DELETE_DLV_ADDRESS, pk.getId()));

		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}

	public String placeOrder(FDActionInfo info,
			ErpCreateOrderModel createOrder, Set<String> usedPromotionCodes,
			String reservationId, boolean sendEmail, CustomerRatingI cra,
			CrmAgentRole agentRole, EnumDlvPassStatus status,boolean isFriendReferred)
			throws FDResourceException,
			ErpFraudException,
			ErpAuthorizationException,
			ErpAddressVerificationException,
			ReservationException,
			DeliveryPassException,
			FDPaymentInadequateException,
			ErpTransactionException,
			InvalidCardException {

		PrimaryKey pk = null;
		FDIdentity identity = info.getIdentity();
		String zoneId = null;
		String orderMobileNumber=null;
		FDCustomerEStoreModel customerSmsPreferenceModel=null;
		boolean isSent=false;
		try
		{
			zoneId = createOrder.getDeliveryInfo().getDeliveryAddress().getAddressInfo().getZoneId();
			FDCustomerModel FDCustomerModel=FDCustomerFactory.getFDCustomer(identity);
							customerSmsPreferenceModel = FDCustomerModel.getCustomerSmsPreferenceModel();
				
			if(EnumEStoreId.FDX.name().equalsIgnoreCase(createOrder.geteStoreId().name()) && createOrder.getDeliveryInfo().getOrderMobileNumber()!=null && customerSmsPreferenceModel.getFdxMobileNumber()!=null)
				orderMobileNumber=createOrder.getDeliveryInfo().getOrderMobileNumber().getPhone();
				else if(EnumEStoreId.FDX.name().equalsIgnoreCase(createOrder.geteStoreId().name()) && createOrder.getDeliveryInfo().getOrderMobileNumber()==null && customerSmsPreferenceModel.getFdxMobileNumber()!=null){
						orderMobileNumber=customerSmsPreferenceModel.getFdxMobileNumber().getPhone();
						createOrder.getDeliveryInfo().setOrderMobileNumber(customerSmsPreferenceModel.getFdxMobileNumber());
						}
				
			//update original cutoff in deliveryinfo if the order is placed via Masquerade
			if(createOrder.geteStoreId()!=null && EnumEStoreId.FDX.name().equals(createOrder.geteStoreId().name())){
				if((info.getSource()!=null && EnumTransactionSource.CUSTOMER_REP.getCode().equalsIgnoreCase(info.getSource().getCode()) 
						&& createOrder.getDeliveryInfo().getOriginalCutoffTime()!=null 
						&& createOrder.getDeliveryInfo().getDeliveryCutoffTime()!=null 
						 && createOrder.getDeliveryInfo().getDeliveryCutoffTime().after(createOrder.getDeliveryInfo().getOriginalCutoffTime()))){
					createOrder.getDeliveryInfo().setDeliveryCutoffTime(createOrder.getDeliveryInfo().getOriginalCutoffTime());
				}
			}
		}
		catch(Exception e)
		{
			
			// do nothing
		}
		TimeslotEvent event = new TimeslotEvent((info.getSource()!=null)?info.getSource().getCode():"", 
				createOrder.isDlvPassApplied(),createOrder.getDeliverySurcharge(), Util.isDlvChargeWaived(createOrder), false, info.getFdUserId(), "fd");
		
		if(createOrder.hasCouponDiscounts()){
			Date date = new Date();
			ErpCouponTransactionModel transModel = new ErpCouponTransactionModel();
			transModel.setTranStatus(EnumCouponTransactionStatus.PENDING);
			transModel.setTranType(EnumCouponTransactionType.CREATE_ORDER);
			transModel.setCreateTime(date);
			transModel.setTranTime(date);
			createOrder.setCouponTransModel(transModel);
		}
		
		//add code for RAF flow and validation to check to see if we have atleast 1 settled order 
		//also logic to insert only referred records to raf trans table
		
		
	  if(isFriendReferred){
		  FDRafTransModel rafTransModel = RafUtil.getPurchaseTransModel();
		  createOrder.setRafTransModel(rafTransModel);
	  }
		
		try {

			try {
				FDDeliveryManager.getInstance().getReservationById(event.getTransactionSource(), reservationId);
			} catch (FDResourceException e) {
				this.getSessionContext().setRollbackOnly();
				throw new ReservationException(e.getMessage());
			}
			GiftCardApplicationStrategy strategy = new GiftCardApplicationStrategy(
					createOrder, null);
			strategy.generateAppliedGiftCardsInfo();
			// Check if payment received is enough to process GC only orders.
			if (createOrder.getSelectedGiftCards() != null
					&& createOrder.getSelectedGiftCards().size() > 0) {
				// Generate Applied gift cards info.

				if (strategy.getRemainingBalance() > 0
						&& createOrder.getPaymentMethod().isGiftCard()) {
					// No CC or EC available on the account. Balance to be paid
					// by other mode of payment
					throw new FDPaymentInadequateException(
							"Payment indequate. Please provide a different mode of payment.");
				}
				createOrder.setAppliedGiftcards(strategy.getAppGiftCardInfo());

			}
			createOrder.setBufferAmt(strategy.getPerishableBufferAmount());
			// place order in SAP and ERP
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			String customerPk = identity.getErpCustomerPK();
			// Begin Handle Delivery Pass.
			/*
			 * Check if there is a active delivery pass and delivery pass was
			 * applied. If yes create order by passing the delivery pass id.
			 */
			if (EnumDlvPassStatus.ACTIVE.equals(status)
					&& createOrder.isDlvPassApplied()) {
				// Get the Active delivery Pass for this account.
				DlvPassManagerSB dlvpsb = this.getDlvPassManagerHome().create();
				List<DeliveryPassModel> dlvPasses = dlvpsb
						.getDlvPassesByStatus(customerPk,
								EnumDlvPassStatus.ACTIVE);
				if (dlvPasses == null
						|| (dlvPasses != null && dlvPasses.size() == 0)) {
					throw new FDResourceException(
							"Unable to locate the Active DeliveryPass for this customer.");
				}
				// Get the Active delivery pass from the list.
				DeliveryPassModel dlvPass = dlvPasses.get(0);
				// pass has to be applied to this order.
				dlvpsb.apply(dlvPass);
				pk = sb.placeOrder(new PrimaryKey(customerPk), createOrder,
						usedPromotionCodes, cra, agentRole, dlvPass.getPK()
								.getId(), EnumSaleType.REGULAR);
				if (createOrder.getDeliveryPassCount() > 0) {
					// order contains delivery pass.
					DeliveryPassModel newPass = DeliveryPassUtil
							.constructDeliveryPassFromOrder(customerPk, pk
									.getId(), createOrder);
					dlvpsb.create(newPass);
				}
			} else if (EnumDlvPassStatus.ACTIVE.equals(status)
					&& createOrder.isDlvPromotionApplied()) {

				pk = sb.placeOrder(new PrimaryKey(customerPk), createOrder,
						usedPromotionCodes, cra, agentRole, null,
						EnumSaleType.REGULAR);
				DlvPassManagerSB dlvpsb = this.getDlvPassManagerHome().create();
				if (createOrder.getDeliveryPassCount() > 0) {
					// order contains delivery pass.
					DeliveryPassModel newPass = DeliveryPassUtil
							.constructDeliveryPassFromOrder(customerPk, pk
									.getId(), createOrder);
					dlvpsb.create(newPass);
				}
				DeliveryPassModel dlvPass = getActiveDPForCustomer(customerPk,
						dlvpsb);
				if (dlvPass.getType().isUnlimited()
						&& EnumDeliveryType.HOME.equals(createOrder
								.getDeliveryInfo().getDeliveryType())) {
					extendDeliveryPass(dlvpsb, dlvPass, 1, pk.getId(),
							"Extend DP by a week.",
							EnumDlvPassExtendReason.DLV_PROMOTION_APPLIED
									.getName());

				}

			} else {
				/*
				 * create order by passing dlvPassid as null. Check if order
				 * contains delivery pass. If yes create delivery pass.And if
				 * pass was applied then apply the delivery pass and update the
				 * order with dlv pass id.
				 */
				pk = sb.placeOrder(new PrimaryKey(customerPk), createOrder,
						usedPromotionCodes, cra, agentRole, null,
						EnumSaleType.REGULAR);
				LOGGER.debug("Order : "+createOrder.getPK().toString()+" deliveryPassCount=>"+createOrder.getDeliveryPassCount());
				if (createOrder.getDeliveryPassCount() > 0) {
					// order contains delivery pass.
					DeliveryPassModel newPass = DeliveryPassUtil
							.constructDeliveryPassFromOrder(customerPk, pk
									.getId(), createOrder);
					DlvPassManagerSB dlvpsb = this.getDlvPassManagerHome().create();
					String dlvPassId = dlvpsb.create(newPass);
					newPass.setPK(new PrimaryKey(dlvPassId));

					if (createOrder.isDlvPassApplied()) {
						LOGGER.debug("Inside dlv pass applied ");
						// pass was applied to this order.
						dlvpsb.applyNew(newPass);
						LOGGER.debug("Dlv PAss ID " + dlvPassId);
						// Update the dlvPassId to the newly created order.
						sb.updateDlvPassIdToSale(pk.getId(), dlvPassId);
					} else if (createOrder.isDlvPromotionApplied()
							&& newPass.getType().isUnlimited()
							&& EnumDeliveryType.HOME.equals(createOrder
									.getDeliveryInfo().getDeliveryType())) {
						extendDeliveryPass(dlvpsb, newPass, 1, pk.getId(),
								"Extend DP by a week.",
								EnumDlvPassExtendReason.DLV_PROMOTION_APPLIED
										.getName());
					}
				}
			}
			// End Handle Delivery Pass.
			
			//Begin apply delivery pass extension promotion.
			int dpExtendDays =  createOrder.getDlvPassExtendDays();
			if (dpExtendDays > 0  && EnumDlvPassStatus.ACTIVE.equals(status)){
				DlvPassManagerSB dlvpsb = this.getDlvPassManagerHome().create();
				List<DeliveryPassModel> dlvPasses = dlvpsb.getDlvPassesByStatus(customerPk,EnumDlvPassStatus.ACTIVE);
				if (dlvPasses == null|| (dlvPasses != null && dlvPasses.size() == 0)) {
					throw new FDResourceException(
					"Unable to locate the Active DeliveryPass for this customer.");
				}
				// Get the Active delivery pass from the list.
				DeliveryPassModel dlvPass = dlvPasses.get(0);
				// pass has to be applied to this order.
				extendDeliveryPassByDays(dlvpsb, dlvPass, dpExtendDays , pk.getId(),
						"Extended DP by "+dpExtendDays+" days",
						EnumDlvPassExtendReason.EXTEND_DP_PROMOTION_APPLIED
								.getName());
			}
			//End apply delivery pass extension promotion.
			LOGGER.info("Before commiting the reservation "+reservationId);
			FDDeliveryManager.getInstance().commitReservation(reservationId, 
					identity.getErpCustomerPK(), 
					getOrderContext(EnumOrderAction.CREATE, EnumOrderType.REGULAR, pk.getId()),
					createOrder.getDeliveryInfo().getDeliveryAddress(), info.isPR1(), event);
			
			
			LOGGER.info("After commiting the reservation "+reservationId);
			
			if (null != createOrder.getSelectedGiftCards()
					&& createOrder.getSelectedGiftCards().size() > 0) {
				// Verify status of gift cards being applied on this order.
				List<ErpGiftCardModel> verifiedList = verifyStatusAndBalance(createOrder.getSelectedGiftCards(), false);
				List badGiftCards = ErpGiftCardUtil.checkForBadGiftcards(verifiedList);
				if (badGiftCards.size() > 0) {
					// Issues with gift cards

					// Send GC Authorization Email.
					/*
					 * FDOrderI order = getOrder(pk.getId()); FDCustomerInfo
					 * custInfo = this.getCustomerInfo(identity);
					 * 
					 * int orderCount = getValidOrderCount(identity);
					 * custInfo.setNumberOfOrders(orderCount); Calendar cal =
					 * calculateCutOffTime(order); //To get the cutoff time for
					 * replacing the order.
					 * this.doEmail(FDGiftCardEmailFactory.getInstance
					 * ().createAuthorizationFailedEmail(custInfo, pk.getId(),
					 * order.getDeliveryReservation().getStartTime(),
					 * order.getDeliveryReservation().getEndTime(),
					 * cal.getTime()));
					 */

					// throw exception after sending the authorization failed
					// email.
					throw new InvalidCardException("Certificate Invalid");
				}
				// Initiate pre authorization.
				GiftCardManagerSB gcSB = this.getGiftCardGManagerHome().create();
				gcSB.initiatePreAuthorization(pk.getId());

			}
			// AUTH sale in CYBER SOURCE
			PaymentManagerSB paymentManager = this.getPaymentManagerHome()
					.create();
			paymentManager.authorizeSaleRealtime(pk.getId());

			ErpActivityRecord rec = info.createActivity(EnumAccountActivityType.PLACE_ORDER);
			rec.setChangeOrderId(pk.getId());
			this.logActivity(rec);
			
			try {
				if (sendEmail) {
					LOGGER.info("sending the email to customer "+pk.getId());
					FDOrderI order = getOrder(pk.getId());
					Collections.sort(order.getOrderLines(), FDCartModel.PRODUCT_SAMPLE_COMPARATOR);
					if(FDStoreProperties.isPromoLineItemEmailDisplay()){
						setPromotionDescriptionForEmail(order);
					}
					FDCustomerInfo fdInfo = this.getCustomerInfo(identity);

					int orderCount = getValidOrderCount(identity);
					fdInfo.setNumberOfOrders(orderCount);
					fdInfo.setUserGiftCardsBalance(calculateGiftCardsBalance(this.getGiftCards(identity)));

					this.doEmail(FDEmailFactory.getInstance().createConfirmOrderEmail(fdInfo, order));
				}
			} catch (Exception e) {
				LOGGER.warn("Error Sending email for Order Confirmantion: "+ pk.getId(), e);
			}
			try {
				FDOrderI order = getOrder(pk.getId());
				if(FDStoreProperties.getSmsOrderConfirmation() && "S".equalsIgnoreCase(customerSmsPreferenceModel.getFdxOrderNotices()))
				 isSent = SMSAlertManager.getInstance().smsOrderConfirmation(info.getFdUserId(), orderMobileNumber, order.getErpSalesId(), EnumEStoreId.FDX.name());
				
			} catch (FDResourceException e) {
				// TODO Auto-generated catch block
				LOGGER.warn("Error Sending FDXSMS for Order Confirmantion: ", e);
			}		
		
			
			try {
				if(null!=createOrder.getCouponTransModel()){
					createOrder.getCouponTransModel().setSaleId(pk.getId());
					FDCouponActivityContext context = new FDCouponActivityContext(info.getSource(), info.getInitiator(), info.getAgent());
					FDCouponManager.postCouponOrder(createOrder.getCouponTransModel(),context);
				}
			} catch (Exception e) {
				LOGGER.warn("Error submitting the coupons order to vendor: ", e);
			}
			//Moving the commit reservation section to the end of place order flow as reservation can be committed to logistics and still place order fails because of the auth.
			/*LOGGER.info("Before commiting the reservation "+reservationId);
			FDDeliveryManager.getInstance().commitReservation(reservationId, 
					identity.getErpCustomerPK(), 
					getOrderContext(EnumOrderAction.CREATE, EnumOrderType.REGULAR, pk.getId()),
					createOrder.getDeliveryInfo().getDeliveryAddress(), info.isPR1(), event);
			LOGGER.info("After commiting the reservation "+reservationId);*/
			
			return pk.getId();

		} catch (DeliveryPassException de) {
			LOGGER.warn("Error placing the order.", de);
			throw de;
		} catch (CreateException ce) {
			LOGGER.warn("Cannot Create ErpCustomerManagerSessionBean", ce);
			throw new FDResourceException(ce);
		} catch (ErpAddressVerificationException re) {
			try{
				FDDeliveryManager.getInstance().releaseReservation(reservationId, createOrder.getDeliveryInfo().getDeliveryAddress(), event, true);// release the reservation that is committed but auth failed becuase of AVS as this causes transaction rollback
			}catch(Exception e){
				LOGGER.info("There was an error releasing the reservation" + reservationId);
			}
			throw re;
		} catch (ErpAuthorizationException re) {
			try{
				FDDeliveryManager.getInstance().releaseReservation(reservationId, createOrder.getDeliveryInfo().getDeliveryAddress(), event, true);// release the reservation that is committed but auth failed becuase of AVS as this causes transaction rollback
			}catch(Exception e){
				LOGGER.info("There was an error releasing the reservation" + reservationId);
			}
			throw re;
		} catch (RemoteException re) {
			try{
				FDDeliveryManager.getInstance().releaseReservation(reservationId, createOrder.getDeliveryInfo().getDeliveryAddress(), event, true);// release the reservation that is committed but auth failed becuase of AVS as this causes transaction rollback
			}catch(Exception e){
				LOGGER.info("There was an error releasing the reservation" + reservationId);
			}
			Exception ex=(Exception)re.getCause();
			if(ex instanceof ErpAddressVerificationException){
				throw (ErpAddressVerificationException)ex;
			}
			
			throw new FDResourceException(re);
		}
	}

	private OrderContext getOrderContext(EnumOrderAction action,
			EnumOrderType type, String id) {
		OrderContext orderContext = new OrderContext();
		orderContext.setAction(action);
		orderContext.setType(type);
		orderContext.setOrderId(id);
		return orderContext;
		
	}

	private void setPromotionDescriptionForEmail(FDOrderI order) {
		if(null !=order){
			List<FDCartLineI> orderLines = order.getOrderLines();
			if(null!= orderLines){
				for (Iterator iterator = orderLines.iterator(); iterator.hasNext();) {
					FDCartLineI cartLineI = (FDCartLineI) iterator.next();
					Discount discount = cartLineI.getDiscount();
					if(null != discount){
						PromotionI promotion = PromotionFactory.getInstance().getPromotion(discount.getPromotionCode());
						if(null != promotion && null !=promotion.getDescription()){
							discount.setPromotionDescription(promotion.getDescription());
						}
					}						
				}
			}
		}
	}

	public void setProfileAttribute(FDIdentity identity, String key,
			String value, FDActionInfo info) throws FDResourceException {
		try {
			FDCustomerEB eb = getFdCustomerHome().findByPrimaryKey(
					new PrimaryKey(identity.getFDCustomerPK()));
			eb.setProfileAttribute(key, value);
			if (info != null) {
				this
						.logActivity(info
								.createActivity(EnumAccountActivityType.SET_CUSTOMER_PROFILE));
			}
		} catch (FinderException fe) {
			throw new FDResourceException(fe,
					"Could not update customer profile");
		} catch (RemoteException re) {
			throw new FDResourceException(re,
					"Having problem talking to FDCustomerEntityBean");
		}

	}

	public void removeProfileAttribute(FDIdentity identity, String key,
			FDActionInfo info) throws FDResourceException {
		try {
			FDCustomerEB eb = getFdCustomerHome().findByPrimaryKey(
					new PrimaryKey(identity.getFDCustomerPK()));
			eb.removeProfileAttribute(key);
			if (info != null) {
				this
						.logActivity(info
								.createActivity(EnumAccountActivityType.REMOVE_CUSTOMER_PROFILE));
			}
		} catch (FinderException fe) {
			throw new FDResourceException(fe,
					"Could not update customer profile");
		} catch (RemoteException re) {
			throw new FDResourceException(re,
					"Having problem talking to FDCustomerEntityBean");
		}

	}

	private boolean existsDPInOrder(List<DeliveryPassModel> deliveryPasses) {

		if (deliveryPasses != null && deliveryPasses.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	private void cancelDeliveryPass(DlvPassManagerSB dlvpassSB,
			DeliveryPassModel dlvPass, EnumDlvPassStatus status)
			throws RemoteException {

		dlvPass.setStatus(status);
		dlvpassSB.cancel(dlvPass);
	}

	private void revokeDeliveryPassUsage(DlvPassManagerSB dlvPassSB,
			String appliedPass_ID) throws DeliveryPassException,
			RemoteException {

		DeliveryPassModel appliedPass = dlvPassSB
				.getDeliveryPassInfo(appliedPass_ID);
		if (appliedPass.getType().isUnlimited()) {
			dlvPassSB.revoke(appliedPass);
		} else {
			DeliveryPassModel activePass = getActivePass(dlvPassSB, appliedPass
					.getCustomerId());
			if (activePass != null) {
				String activePass_ID = activePass.getPK().getId();
				if (!activePass_ID.equals(appliedPass_ID)) {
					dlvPassSB.revoke(appliedPass, activePass);
				} else {
					dlvPassSB.revoke(appliedPass);
				}
			} else {
				dlvPassSB.revoke(appliedPass);
			}
		}
	}

	private boolean isValued(String input) {

		if ((null == input) || "".equals(input.trim())) {
			return false;
		}
		return true;
	}

	private boolean isPurchasedDPAppliedOnOrder(String purchasedDP_ID,
			String appliedDP_ID) {

		if (purchasedDP_ID.equals(appliedDP_ID)) {
			return true;
		}
		return false;
	}

	private DeliveryPassModel getActivePass(DlvPassManagerSB dlvpsb,
			String customerID) throws RemoteException {

		List<DeliveryPassModel> dlvPasses = dlvpsb.getDlvPassesByStatus(
				customerID, EnumDlvPassStatus.ACTIVE);
		if (dlvPasses != null && dlvPasses.size() > 0)
			return dlvPasses.get(0);
		else
			return null;

	}

	public FDReservation cancelOrder(FDActionInfo info, String saleId,
			boolean sendEmail, int currentDPExtendDays, boolean restoreReservation) throws FDResourceException,
			ErpTransactionException, DeliveryPassException {
		String orderMobileNumber=null;
		FDCustomerEStoreModel customerSmsPreferenceModel=null;
		boolean isSent=false;
		try {
			TimeslotEvent event = new TimeslotEvent((info.getSource()!=null)?info.getSource().getCode():"", 
					false,0.00, false, false, info.getFdUserId(), EnumCompanyCode.fd.name());
			
			// !!! verify that the sale belongs to the customer
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			String initiator = info.getAgent() == null ? null : info.getAgent()
					.getUserId();
			String reservationId = sb.cancelOrder(saleId, info.getSource(),
					initiator);
			FDOrderI order = getOrder(saleId);
			
			FDCustomerModel FDCustomerModel=FDCustomerFactory.getFDCustomer(info.getIdentity());
			 customerSmsPreferenceModel = FDCustomerModel.getCustomerSmsPreferenceModel();
				
				if(EnumEStoreId.FDX.name().equalsIgnoreCase(order.getEStoreId().name()) && order.getDeliveryInfo().getOrderMobileNumber()!=null && customerSmsPreferenceModel.getFdxMobileNumber()!=null)
							orderMobileNumber= order.getDeliveryInfo().getOrderMobileNumber().getPhone();
				
				if(EnumEStoreId.FDX.name().equalsIgnoreCase(order.getEStoreId().name()) && order.getDeliveryInfo().getOrderMobileNumber()==null && customerSmsPreferenceModel.getFdxMobileNumber()!=null)
							orderMobileNumber=customerSmsPreferenceModel.getFdxMobileNumber().getPhone();
			
			String appliedPass_ID = order.getDeliveryPassId();
			// Begin Handle Delivery Pass.
			// Cancel any delivery pass linked with the order.
			DlvPassManagerSB dlvpsb = this.getDlvPassManagerHome().create();
			List<DeliveryPassModel> deliveryPasses = dlvpsb
					.getDlvPassesByOrderId(saleId);

			if (existsDPInOrder(deliveryPasses)) {

				/*
				 * Customer has purchased delivery pass in the order. CANCEL the
				 * purchased pass.
				 */
				DeliveryPassModel purchasedPass = deliveryPasses.get(0);
				cancelDeliveryPass(dlvpsb, purchasedPass,
						EnumDlvPassStatus.ORDER_CANCELLED);
				if (isValued(appliedPass_ID)
						&& !isPurchasedDPAppliedOnOrder(purchasedPass.getPK()
								.getId(), appliedPass_ID)) {

					/*
					 * Delivery pass is applied on the order and the applied
					 * delivery pass is not the purchased pass
					 */
					revokeDeliveryPassUsage(dlvpsb, appliedPass_ID);
				} else if (isDeliveryPromotionApplied(order)) {
					cancelPassExtension(dlvpsb, null, order);
				}
			} else if (isValued(appliedPass_ID)) {
				revokeDeliveryPassUsage(dlvpsb, appliedPass_ID);
			} else if (isDeliveryPromotionApplied(order)) {
				cancelPassExtension(dlvpsb, null, order);
			}
			// End Handle Delivery Pass.
			
			if (currentDPExtendDays > 0){
				//If extend delivery promo already there reverse extension.
				List<DeliveryPassModel> dlvPasses = dlvpsb.getDlvPassesByStatus(order.getCustomerId(),EnumDlvPassStatus.ACTIVE);
				if (dlvPasses == null|| (dlvPasses != null && dlvPasses.size() == 0)) {
					throw new FDResourceException(
					"Unable to locate the Active DeliveryPass for this customer.");
				}
				// Get the Active delivery pass from the list.
				DeliveryPassModel dlvPass = dlvPasses.get(0);
				// pass has to be applied to this order.
				extendDeliveryPassByDays(dlvpsb, dlvPass, -currentDPExtendDays , saleId,
						"DP Extension Promotion Reversed "+currentDPExtendDays+" days",
						EnumDlvPassExtendReason.EXTEND_DP_PROMOTION_REMOVED
								.getName());
			}
			
			boolean isRestored = false;
			if (EnumSaleType.REGULAR.equals(order.getOrderType())) {
				// DlvManagerSB dlvSB = this.getDlvManagerHome().create();

				isRestored = FDDeliveryManager.getInstance()
						.releaseReservation(reservationId,
								order.getDeliveryAddress(), event, restoreReservation);
			}
			if (null != order.getAppliedGiftCards()
					&& order.getAppliedGiftCards().size() > 0) {
				// Cancel/Reverse GC pre authorization.
				GiftCardManagerSB gcSB = this.getGiftCardGManagerHome().create();
				gcSB.initiateCancelAuthorizations(saleId);
			}
			ErpActivityRecord rec = info
					.createActivity(EnumAccountActivityType.CANCEL_ORDER);
			rec.setChangeOrderId(saleId);
			rec.setStandingOrderId(order.getStandingOrderId());
			this.logActivity(rec);

			if (sendEmail) {
				FDCustomerInfo fdInfo = this
						.getCustomerInfo(info.getIdentity());
				this.doEmail(FDEmailFactory.getInstance()
						.createCancelOrderEmail(fdInfo, saleId,
								order.getDeliveryReservation().getStartTime(),
								order.getDeliveryReservation().getEndTime(),
								order.getEStoreId())
				);
			}
			//Start:: Add FDX SMS for order Cancelled
			
			try {
				if(FDStoreProperties.getSmsOrderCancel() && EnumEStoreId.FDX.name().equalsIgnoreCase(order.getEStoreId().name())&& "S".equalsIgnoreCase(customerSmsPreferenceModel.getFdxOrderExceptions()))
					isSent = SMSAlertManager.getInstance().smsOrderCancel(info.getFdUserId(), orderMobileNumber, saleId, EnumEStoreId.FDX.name());
			} catch (FDResourceException e) {
				// TODO Auto-generated catch block
				LOGGER.warn("Error Sending FDXSMS for Order Cancelled: ", e);
			}
			
			//End:: Add FDX SMSfor order Cancelled
			return isRestored ? order.getDeliveryReservation() : null;

		} catch (CreateException ex) {
			throw new FDResourceException(ex);
		} catch (RemoteException ex) {
			throw new FDResourceException(ex);
		} /*
		 * catch (FinderException e) { throw new FDResourceException(e); }
		 */
	}

	private void cancelPassExtension(DlvPassManagerSB dlvPassSB,
			DeliveryPassModel dlvPass, FDOrderI order) throws RemoteException {

		if (dlvPass == null) {

			List<DeliveryPassModel> dlvPasses = dlvPassSB.getDlvPassesByStatus(
					order.getCustomerId(), EnumDlvPassStatus.ACTIVE);
			if (dlvPasses != null && dlvPasses.size() > 0) {
				dlvPass = dlvPasses.get(0);
				if (dlvPass.getType().isUnlimited()) {
					extendDeliveryPass(dlvPassSB, dlvPass, -1, order
							.getErpSalesId(),
							"Curtail DP by a week. Order cancelled.",
							EnumDlvPassExtendReason.DLV_PROMOTION_REMOVED
									.getName());
				}
			}
		} else if (dlvPass.getType().isUnlimited()) {
			extendDeliveryPass(dlvPassSB, dlvPass, -1, order.getErpSalesId(),
					"Curtail DP by a week. Order cancelled.",
					EnumDlvPassExtendReason.DLV_PROMOTION_REMOVED.getName());
		}
	}
	
	private void cancelPassExtensionDays(DlvPassManagerSB dlvPassSB,
			DeliveryPassModel dlvPass, FDOrderI order, int reverseDays) throws RemoteException {

		if (dlvPass == null) {

			List<DeliveryPassModel> dlvPasses = dlvPassSB.getDlvPassesByStatus(
					order.getCustomerId(), EnumDlvPassStatus.ACTIVE);
			if (dlvPasses != null && dlvPasses.size() > 0) {
				dlvPass = dlvPasses.get(0);
				if (dlvPass.getType().isUnlimited()) {
					extendDeliveryPass(dlvPassSB, dlvPass, -reverseDays/7, order
							.getErpSalesId(),
							"DP Extension Promotion Reversed",
							EnumDlvPassExtendReason.EXTEND_DP_PROMOTION_REMOVED
									.getName());
				}
			}
		} else if (dlvPass.getType().isUnlimited()) {
			extendDeliveryPass(dlvPassSB, dlvPass, -reverseDays/7, order.getErpSalesId(),
					"DP Extension Promotion Reversed",
					EnumDlvPassExtendReason.EXTEND_DP_PROMOTION_REMOVED
							.getName());
		}
	}

	private boolean isDeliveryPromotionApplied(FDOrderI order) {

		if (order.isDeliveryChargeWaived()
				&& !isValued(order.getDeliveryPassId())
				&& EnumDeliveryType.HOME.equals(order.getDeliveryType())) {

			return true;
		}
		return false;
	}

	/**
	 * Modify an order (modify & send msg to SAP).
	 * 
	 * @param identity
	 *            the customer's identity reference
	 * @throws FDResourceException
	 *             if an error occured while accessing remote resources
	 */
	public void modifyOrder(FDActionInfo info, String saleId,
			ErpModifyOrderModel order, Set<String> usedPromotionCodes,
			String oldReservationId, boolean sendEmail, CustomerRatingI cra,
			CrmAgentRole agentRole, EnumDlvPassStatus status, boolean hasCoupons)
			throws FDResourceException, ErpTransactionException,
			ErpFraudException, ErpAuthorizationException,
			DeliveryPassException, FDPaymentInadequateException,
			InvalidCardException, ErpAddressVerificationException {

		String zoneId = null;
		String orderMobileNumber=null;
		FDCustomerEStoreModel customerSmsPreferenceModel=null;
		boolean isSent=false;
		try
		{
			
			zoneId = order.getDeliveryInfo().getDeliveryAddress().getAddressInfo().getZoneId();
			
			FDCustomerModel FDCustomerModel=FDCustomerFactory.getFDCustomer(info.getIdentity());
			 customerSmsPreferenceModel = FDCustomerModel.getCustomerSmsPreferenceModel();
	
				if(EnumEStoreId.FDX.name().equalsIgnoreCase(order.geteStoreId().name()) && order.getDeliveryInfo().getOrderMobileNumber()!=null && customerSmsPreferenceModel.getFdxMobileNumber()!=null)
							orderMobileNumber= order.getDeliveryInfo().getOrderMobileNumber().getPhone();
				
				if(EnumEStoreId.FDX.name().equalsIgnoreCase(order.geteStoreId().name()) && order.getDeliveryInfo().getOrderMobileNumber()==null && customerSmsPreferenceModel.getFdxMobileNumber()!=null)
					{  orderMobileNumber=customerSmsPreferenceModel.getFdxMobileNumber().getPhone();
						order.getDeliveryInfo().setOrderMobileNumber(customerSmsPreferenceModel.getFdxMobileNumber());
					}
			
				//update original cutoff in deliveryinfo if the order is placed via Masquerade
				if(order.geteStoreId()!=null && EnumEStoreId.FDX.name().equals(order.geteStoreId().name())){
					if((info.getSource()!=null && EnumTransactionSource.CUSTOMER_REP.getCode().equalsIgnoreCase(info.getSource().getCode())) ||
							(order.getDeliveryInfo().getDeliveryCutoffTime()!=null 
							&& order.getDeliveryInfo().getOriginalCutoffTime()!=null 
							&& order.getDeliveryInfo().getDeliveryCutoffTime().after(order.getDeliveryInfo().getOriginalCutoffTime()))){
						order.getDeliveryInfo().setDeliveryCutoffTime(order.getDeliveryInfo().getOriginalCutoffTime());
					}
				}
		}
		catch(Exception e)
		{
			
			// do nothing
		}
		TimeslotEvent event = new TimeslotEvent((info.getSource()!=null)?info.getSource().getCode():"", 
				order.isDlvPassApplied(),order.getDeliverySurcharge(), Util.isDlvChargeWaived(order), false, info.getFdUserId(), EnumCompanyCode.fd.name());
		
		if(hasCoupons){
			Date date = new Date();
			ErpCouponTransactionModel transModel = new ErpCouponTransactionModel();
			transModel.setTranStatus(EnumCouponTransactionStatus.PENDING);
			transModel.setCreateTime(date);
			transModel.setTranTime(date);
			transModel.setTranType(EnumCouponTransactionType.MODIFY_ORDER);							
			order.setCouponTransModel(transModel);
		}
		
		FDIdentity identity = info.getIdentity();
		try {
			// !!! verify that the sale belongs to the customer

			// !!! update profile ?

			// !!! log activity

			FDOrderI fdOrder = getOrder(identity, saleId);
			
			if (EnumSaleStatus.NOT_SUBMITTED.equals(fdOrder.getOrderStatus())
					&& EnumTransactionSource.WEBSITE.equals(order
							.getTransactionSource())) {
				throw new ErpTransactionException(
						"Customers may not modify orders in this state. Please contact Customer Service to continue.");
			}
			GiftCardApplicationStrategy strategy = new GiftCardApplicationStrategy(
					order, null);
			strategy.generateAppliedGiftCardsInfo();
			// Check if payment received is enough to process GC only orders.
			if (order.getSelectedGiftCards() != null
					&& order.getSelectedGiftCards().size() > 0) {
				// Generate Applied gift cards info.

				if (strategy.getRemainingBalance() > 0
						&& order.getPaymentMethod().isGiftCard()) {
					// No CC or EC available on the account. Balance to be paid
					// by other mode of payment
					throw new FDPaymentInadequateException(
							"Payment indequate. Please provide a different mode of payment.");
				}
				order.setAppliedGiftcards(strategy.getAppGiftCardInfo());
			}
			order.setBufferAmt(strategy.getPerishableBufferAmount());
			// Modify order in SAP and ERP
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			sb.modifyOrder(saleId, order, usedPromotionCodes, cra, agentRole,
					true);

			// Begin Handle Delivery Pass.
			/*
			 * Check if there is a active delivery pass and delivery pass was
			 * applied. Also check if the modified order does not have any dlv
			 * pass applied on it during create. If yes then apply the delivery
			 * pass.
			 */
			DeliveryPassModel appliedDlvPass = null;
			if (EnumDlvPassStatus.ACTIVE.equals(status)
					&& order.isDlvPassApplied()
					&& fdOrder.getDeliveryPassId() == null) {
				/*
				 * Customer has an ACTIVE DP. Order has DP applied during order
				 * modification. Delivery promo was applied originally.
				 */
				// Get the Active delivery Pass for this account.
				DlvPassManagerSB dlvpsb = this.getDlvPassManagerHome().create();
				List<DeliveryPassModel> dlvPasses = dlvpsb
						.getDlvPassesByStatus(identity.getErpCustomerPK(),
								EnumDlvPassStatus.ACTIVE);
				if (dlvPasses == null || dlvPasses.size() == 0) {
					throw new FDResourceException(
							"Unable to locate the Active DeliveryPass for this customer.");
				}
				// Get the Active delivery pass from the list.
				DeliveryPassModel dlvPass = dlvPasses.get(0);
				// pass was applied to this order.
				dlvpsb.apply(dlvPass);
				sb.updateDlvPassIdToSale(saleId, dlvPass.getPK().getId());
				appliedDlvPass = dlvPass;
				// revert back the extension of the pass.(promo was applied
				// originally)
				if (dlvPass.getType().isUnlimited()
						&& fdOrder.isDeliveryChargeWaived()) {
					extendDeliveryPass(dlvpsb, dlvPass, -1, saleId,
							"Curtail DP by a week.",
							EnumDlvPassExtendReason.DLV_PROMOTION_REMOVED
									.getName());
				}
			} else if ((EnumDlvPassStatus.ACTIVE.equals(status) || EnumDlvPassStatus.EXPIRED_PENDING
					.equals(status))
					&& !order.isDlvPassApplied()
					&& fdOrder.getDeliveryPassId() != null) {
				/*
				 * Note: Expired pending state is valid only for BSGS pass. Then
				 * it means dlvPass was applied during create order and now user
				 * wants to either apply delivery promotion/changed the address
				 * to corporate address. So revoke the delivery pass that was
				 * applied to the order. Then nullify the dlvPassId in sale
				 * table.
				 */
				DlvPassManagerSB dlvpsb = this.getDlvPassManagerHome().create();
				DeliveryPassModel dlvPass = dlvpsb.getDeliveryPassInfo(fdOrder
						.getDeliveryPassId());
				if (dlvPass == null) {
					throw new DeliveryPassException(
							"Unable to locate the delivery pass that was used for this order.");
				}

				dlvpsb.revoke(dlvPass);
				sb.updateDlvPassIdToSale(saleId, null);
				if (order.isDlvPromotionApplied()
						&& dlvPass.getType().isUnlimited()) {
					extendDeliveryPass(dlvpsb, dlvPass, 1, saleId,
							"Extend DP by a week.",
							EnumDlvPassExtendReason.DLV_PROMOTION_APPLIED
									.getName());
				}
			}
			/*
			 * Customer has ACTIVE Delivery pass. The order was previously HOME
			 * delivery with Delivery promotion applied. The order is modified
			 * to PICKUP delivery type. Revert back the Unlimited pass
			 * extension.
			 */
			else if (EnumDlvPassStatus.ACTIVE.equals(status)
					&& fdOrder.isDeliveryChargeWaived()
					&& EnumDeliveryType.HOME.equals(fdOrder.getDeliveryType())
					&& EnumDeliveryType.PICKUP.equals(order.getDeliveryInfo()
							.getDeliveryType())) {

				// Get the Active delivery Pass for this account.
				DlvPassManagerSB dlvpsb = this.getDlvPassManagerHome().create();
				DeliveryPassModel dlvPass = getActiveDPForCustomer(identity
						.getErpCustomerPK(), dlvpsb);

				// revert back the extension of the pass.(promo was applied
				// originally)
				if (dlvPass.getType().isUnlimited()) {
					extendDeliveryPass(dlvpsb, dlvPass, -1, saleId,
							"Curtail DP by a week.",
							EnumDlvPassExtendReason.DLV_PROMOTION_REMOVED
									.getName());
				}

			}
			/*
			 * Customer has ACTIVE Delivery pass. The order was previously
			 * PICKUP delivery type. The order is modified to HOME delivery with
			 * Delivery promotion applied. Extend the unlimited Delivery pass by
			 * a week.
			 */
			else if (EnumDlvPassStatus.ACTIVE.equals(status)
					&& order.isDlvPromotionApplied()
					&& EnumDeliveryType.PICKUP
							.equals(fdOrder.getDeliveryType())
					&& EnumDeliveryType.HOME.equals(order.getDeliveryInfo()
							.getDeliveryType())) {
				// Get the Active delivery Pass for this account.
				DlvPassManagerSB dlvpsb = this.getDlvPassManagerHome().create();
				DeliveryPassModel dlvPass = getActiveDPForCustomer(identity
						.getErpCustomerPK(), dlvpsb);
				if (dlvPass.getType().isUnlimited()) {
					extendDeliveryPass(dlvpsb, dlvPass, 1, saleId,
							"Extend DP by a week.",
							EnumDlvPassExtendReason.DLV_PROMOTION_APPLIED
									.getName());
				}

			}
			// else {
			handleDeliveryPass(identity, saleId, order, fdOrder,
					appliedDlvPass, status);
			// }
			// End Handle Delivery Pass.

			//Begin apply delivery pass extension promotion.
			int dpCurrentExtendDays =  order.getCurrentDlvPassExtendDays();
			if (dpCurrentExtendDays > 0){
				//If extend delivery promo already there reverse extension.
				DlvPassManagerSB dlvpsb = this.getDlvPassManagerHome().create();
				List<DeliveryPassModel> dlvPasses = dlvpsb.getDlvPassesByStatus(identity.getErpCustomerPK(),EnumDlvPassStatus.ACTIVE);
				if (dlvPasses == null|| (dlvPasses != null && dlvPasses.size() == 0)) {
					throw new FDResourceException(
					"Unable to locate the Active DeliveryPass for this customer.");
				}
				// Get the Active delivery pass from the list.
				DeliveryPassModel dlvPass = dlvPasses.get(0);
				// pass has to be applied to this order.
				extendDeliveryPassByDays(dlvpsb, dlvPass, -dpCurrentExtendDays , saleId,
						"DP Extension Promotion Reversed "+dpCurrentExtendDays+" days",
						EnumDlvPassExtendReason.EXTEND_DP_PROMOTION_REMOVED
								.getName());
			}
			
			int dpExtendDays =  order.getDlvPassExtendDays();
			if (dpExtendDays > 0  && EnumDlvPassStatus.ACTIVE.equals(status)){
				DlvPassManagerSB dlvpsb = this.getDlvPassManagerHome().create();
				List<DeliveryPassModel> dlvPasses = dlvpsb.getDlvPassesByStatus(identity.getErpCustomerPK(),EnumDlvPassStatus.ACTIVE);
				if (dlvPasses == null|| (dlvPasses != null && dlvPasses.size() == 0)) {
					throw new FDResourceException(
					"Unable to locate the Active DeliveryPass for this customer.");
				}
				// Get the Active delivery pass from the list.
				DeliveryPassModel dlvPass = dlvPasses.get(0);
				// pass has to be applied to this order.
				extendDeliveryPassByDays(dlvpsb, dlvPass, dpExtendDays , saleId,
						"Extended DP by "+dpExtendDays+" days",
						EnumDlvPassExtendReason.EXTEND_DP_PROMOTION_APPLIED
								.getName());
			}
			//End apply delivery pass extension promotion.

			// Deal with Reservation in DLV
						String newReservationId = order.getDeliveryInfo()
								.getDeliveryReservationId();
						if (!newReservationId.equals(oldReservationId)) {
							// DlvManagerSB dlvSB = this.getDlvManagerHome().create();

							// reservation has changed so release old reservation
							FDDeliveryManager.getInstance().releaseReservation(
									oldReservationId, fdOrder.getDeliveryAddress(), event, true);
							// now commit the new Reservation
							// dlvSB.commitReservation(newReservationId,
							// identity.getErpCustomerPK(), saleId);
							
							FDDeliveryManager.getInstance().commitReservation(
									newReservationId, identity.getErpCustomerPK(), 
									getOrderContext(EnumOrderAction.MODIFY, EnumOrderType.REGULAR, saleId),
									order.getDeliveryInfo().getDeliveryAddress(), info.isPR1(), event);
						}
		
			if (order.getSelectedGiftCards() != null
					&& order.getSelectedGiftCards().size() > 0) {
				// Verify status of gift cards being applied on this order.
				List<ErpGiftCardModel> verifiedList = verifyStatusAndBalance(order
						.getSelectedGiftCards(), false);
				List badGiftCards = ErpGiftCardUtil
						.checkForBadGiftcards(verifiedList);
				if (badGiftCards.size() > 0) {
					// Issues with gift cards
					throw new InvalidCardException("Certificate Invalid");
				}
			}
			GiftCardManagerSB gcSB = this.getGiftCardGManagerHome().create();
			gcSB.initiatePreAuthorization(saleId);

			/*
			 * else { ErpAbstractOrderModel originalOrder = sb.getOrder(new
			 * PrimaryKey(saleId)).getCurrentOrder();
			 * if(originalOrder.getAppliedGiftcards() != null &&
			 * originalOrder.getAppliedGiftcards().size() > 0) { //Original
			 * order GC was used. now removed. cancel the pre-auths on GC
			 * GiftCardManagerSB gcSB = (GiftCardManagerSB)
			 * this.getGiftCardGManagerHome().create();
			 * gcSB.initiateCancelAuthorizations(saleId); } }
			 */
			// authorize the sale
			PaymentManagerSB paymentManager = this.getPaymentManagerHome()
					.create();
			if(EnumSaleStatus.AUTHORIZATION_FAILED.equals(fdOrder.getOrderStatus())) {
				
				if(EnumTransactionSource.WEBSITE.equals(order.getTransactionSource()))
					paymentManager.authorizeSaleRealtime(saleId);
				else 
					paymentManager.authorizeSale(saleId, true);
			} else {
				paymentManager.authorizeSaleRealtime(saleId);
			}

			ErpActivityRecord rec = info.createActivity(EnumAccountActivityType.MODIFY_ORDER);
			rec.setChangeOrderId(saleId);
			rec.setStandingOrderId(fdOrder.getStandingOrderId());
			this.logActivity(rec);
			
			//release the IN_MODIFY LOCK
			releaseModificationLock(saleId);
			
			if (sendEmail) {

				fdOrder = getOrder(saleId);
				if(FDStoreProperties.isPromoLineItemEmailDisplay()){
					setPromotionDescriptionForEmail(fdOrder);
				}
				FDCustomerInfo fdInfo = this.getCustomerInfo(identity);
				int orderCount = getValidOrderCount(identity);
				fdInfo.setNumberOfOrders(orderCount);

				this.doEmail(FDEmailFactory.getInstance()
						.createModifyOrderEmail(fdInfo, fdOrder));
				
				//if its fdx estore then make a call to logistics to store fdx order.
				if(EnumEStoreId.FDX.name().equalsIgnoreCase(order.geteStoreId().name())){
					FDDeliveryManager.getInstance().modifyOrder(saleId, null,
							order.getTip(), newReservationId, orderMobileNumber);
				}
			}
			
			// Order Modification for SMS
			try {
				if(FDStoreProperties.getSmsOrderModification() && EnumEStoreId.FDX.name().equalsIgnoreCase(order.geteStoreId().getContentId()) && "S".equalsIgnoreCase(customerSmsPreferenceModel.getFdxOrderExceptions()))
					 isSent = SMSAlertManager.getInstance().smsOrderModification(info.getFdUserId() ,orderMobileNumber, saleId, EnumEStoreId.FDX.name());
				} catch (FDResourceException e) {
				LOGGER.warn("Error Sending FDXSMS for Order Modification: ", e);
				}

			try {
				if(null!=order.getCouponTransModel()){
					order.getCouponTransModel().setSaleId(saleId);
					FDCouponActivityContext context = new FDCouponActivityContext(info.getSource(), info.getInitiator(), info.getAgent());
					FDCouponManager.postCouponOrder(order.getCouponTransModel(),context);
				}
			} catch (Exception e) {
				LOGGER.warn("Error submitting the coupons order to vendor: ", e);
			}
							
			//Moving the commit reservation section to the end of modify order flow as reservation can be committed to logistics and still modify order fails because of the auth.
			// Deal with Reservation in DLV
						/*newReservationId = order.getDeliveryInfo()
								.getDeliveryReservationId();
						if (!newReservationId.equals(oldReservationId)) {
							// DlvManagerSB dlvSB = this.getDlvManagerHome().create();

							// reservation has changed so release old reservation
							FDDeliveryManager.getInstance().releaseReservation(
									oldReservationId, fdOrder.getDeliveryAddress(), event, true);
							// now commit the new Reservation
							// dlvSB.commitReservation(newReservationId,
							// identity.getErpCustomerPK(), saleId);
							
							FDDeliveryManager.getInstance().commitReservation(
									newReservationId, identity.getErpCustomerPK(), 
									getOrderContext(EnumOrderAction.MODIFY, EnumOrderType.REGULAR, saleId),
									order.getDeliveryInfo().getDeliveryAddress(), info.isPR1(), event);
						}*/

		} catch (DeliveryPassException de) {
			throw de;
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (ErpAddressVerificationException re) {
			try{
				if (order.getDeliveryInfo().getDeliveryReservationId()!= null && !order.getDeliveryInfo().getDeliveryReservationId().equals(oldReservationId)){
					FDDeliveryManager.getInstance().releaseReservation(order.getDeliveryInfo().getDeliveryReservationId(), 
						order.getDeliveryInfo().getDeliveryAddress(), event, true);// release the reservation that is committed but auth failed becuase of AVS as this causes transaction rollback
				}
			}catch(Exception e){
				LOGGER.info("There was an error releasing the reservation" + order.getDeliveryInfo().getDeliveryReservationId());
			}
			throw re;
		} catch (ErpAuthorizationException re) {
			try{
				if (order.getDeliveryInfo().getDeliveryReservationId()!= null && !order.getDeliveryInfo().getDeliveryReservationId().equals(oldReservationId)){
					FDDeliveryManager.getInstance().releaseReservation(order.getDeliveryInfo().getDeliveryReservationId(), 
						order.getDeliveryInfo().getDeliveryAddress(), event, true);// release the reservation that is committed but auth failed becuase of AVS as this causes transaction rollback
				}
			}catch(Exception e){
				LOGGER.info("There was an error releasing the reservation" + order.getDeliveryInfo().getDeliveryReservationId());
			}
			throw re;
		}  catch (RemoteException re) {
			try{
				if (order.getDeliveryInfo().getDeliveryReservationId()!= null && !order.getDeliveryInfo().getDeliveryReservationId().equals(oldReservationId)){
					FDDeliveryManager.getInstance().releaseReservation(order.getDeliveryInfo().getDeliveryReservationId(), 
						order.getDeliveryInfo().getDeliveryAddress(), event, true);// release the reservation that is committed but auth failed becuase of AVS as this causes transaction rollback
				}
			}catch(Exception e){
				LOGGER.info("There was an exception releasing the reservation.");
			}
			Exception ex=(Exception)re.getCause();
			if(ex instanceof ErpAddressVerificationException){
				throw (ErpAddressVerificationException)ex;
			}
			throw new FDResourceException(re);
		} catch (ReservationException re) {
			throw new FDResourceException(re);
		} /*
		 * catch (FinderException e) { throw new FDResourceException(e); }
		 */
	}

	public void modifyAutoRenewOrder(FDActionInfo info, String saleId,
			ErpModifyOrderModel order, Set<String> usedPromotionCodes,
			String oldReservationId, boolean sendEmail, CustomerRatingI cra,
			CrmAgentRole agentRole, EnumDlvPassStatus status)
			throws FDResourceException, ErpTransactionException,
			ErpFraudException, ErpAuthorizationException, DeliveryPassException {

		FDIdentity identity = info.getIdentity();
		try {

			FDOrderI fdOrder = getOrder(identity, saleId);
			order.setRequestedDate(Calendar.getInstance().getTime());
			// Modify order, don't send to SAP
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			sb.modifyOrder(saleId, order, usedPromotionCodes, cra, agentRole,
					false);

			ErpActivityRecord rec = info.createActivity(EnumAccountActivityType.MODIFY_ORDER);
			rec.setChangeOrderId(saleId);
			this.logActivity(rec);
			if (sendEmail) {

				fdOrder = getOrder(saleId);
				FDCustomerInfo fdInfo = this.getCustomerInfo(identity);
				int orderCount = getValidOrderCount(identity);
				fdInfo.setNumberOfOrders(orderCount);

				this.doEmail(FDEmailFactory.getInstance()
						.createModifyOrderEmail(fdInfo, fdOrder));
			}

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}

	}

	private void handleDeliveryPass(FDIdentity identity, String saleId,
			ErpModifyOrderModel order, FDOrderI fdOrder,
			DeliveryPassModel appliedDlvPass, EnumDlvPassStatus status)
			throws CreateException, RemoteException, DeliveryPassException {
		// Handle Delivery Pass.
		DlvPassManagerSB dlvpsb = this.getDlvPassManagerHome().create();
		ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();

		String customerPk = identity.getErpCustomerPK();

		// Check if order contains a delivery pass.
		if (order.getDeliveryPassCount() > 0) {
			// Check if it was added during modify order.
			List<DeliveryPassModel> dlvPasses = dlvpsb
					.getDlvPassesByOrderId(saleId);

			if (dlvPasses == null || dlvPasses.size() == 0) {
				/*
				 * The delivery pass does not exist for this order.
				 */
				DeliveryPassModel newPass = DeliveryPassUtil
						.constructDeliveryPassFromOrder(customerPk, saleId,
								order);
				String pk = dlvpsb.create(newPass);
				newPass.setPK(new PrimaryKey(pk));
				if (order.isDlvPassApplied()
						&& fdOrder.getDeliveryPassId() == null
						&& appliedDlvPass == null) {
					/*
					 * fdOrder.getDeliveryPassId() == null this check make sures
					 * the modified order does not have any acitve delivery pass
					 * applied during create order.
					 */
					dlvpsb.applyNew(newPass);
					sb.updateDlvPassIdToSale(saleId, newPass.getPK().getId());
				} else if (EnumDlvPassStatus.NONE.equals(status)
						&& order.isDlvPromotionApplied()
						&& newPass.getType().isUnlimited()) {// ?should the
																// check be for
																// NONE?
					/*
					 * Customer has purchased Unlimited DP using Delivery promo.
					 * Extend by a week.
					 */
					extendDeliveryPass(dlvpsb, newPass, 1, saleId,
							"Extend DP by a week.",
							EnumDlvPassExtendReason.DLV_PROMOTION_APPLIED
									.getName());
				}
			} else {
				/*
				 * The delivery pass already exists for this order. Check if it
				 * was modified to a new one.
				 */
				// Get the exisitng pass from the list.
				DeliveryPassModel existingPass = dlvPasses.get(0);
				DeliveryPassModel modifyPass = DeliveryPassUtil
						.constructDeliveryPassFromOrder(customerPk, saleId,
								order);
				if (modifyPass.getType() != existingPass.getType()) {
					if (existingPass.getPK().getId().equals(
							fdOrder.getDeliveryPassId())) {
						/*
						 * The existing pass was used for this order. So nullify
						 * the exisitng dlv pass id in sale table before moving
						 * to new delivery pass.
						 */
						sb.updateDlvPassIdToSale(saleId, null);
					}// Else it was a different pass that was applied. Possibly
						// the active one. So Nothing to worry:)

					String dlvPassId = dlvpsb.modify(saleId, modifyPass);
					modifyPass.setPK(new PrimaryKey(dlvPassId));
					if (order.isDlvPassApplied()
							&& fdOrder.getDeliveryPassId() == null) {
						/*
						 * fdOrder.getDeliveryPassId() == null this check make
						 * sures the modified order does not have any active
						 * delivery pass applied during create order.
						 */

						dlvpsb.applyNew(modifyPass);
						sb.updateDlvPassIdToSale(saleId, modifyPass.getPK()
								.getId());
					}
				} else {
					// The pass was not modified during modify order.
					/*
					 * If Unlimited pass, check if there was a discount applied
					 * during modify order. If yes update the new price to the
					 * DP table.
					 */
					if (modifyPass.getType().isUnlimited()
							&& (modifyPass.getAmount() != existingPass
									.getAmount())) {
						// Update the new price to the DP table.
						dlvpsb
								.updatePrice(existingPass, modifyPass
										.getAmount());

					}
					if (order.isDlvPassApplied()
							&& fdOrder.getDeliveryPassId() == null) {
						/*
						 * Then it means dlvPass was not applied during create
						 * order. So apply now.
						 */
						dlvpsb.applyNew(existingPass);
						sb.updateDlvPassIdToSale(saleId, existingPass.getPK()
								.getId());
					} else if (!order.isDlvPassApplied()
							&& (fdOrder.getDeliveryPassId() != null)
							&& (order.isDlvPromotionApplied())) {
						/*
						 * Then it means dlvPass was applied during create order
						 * and now user wants to apply delivery
						 * promotion/changed to corporate address. So revoke the
						 * delivery pass that was applied to the order. Then
						 * nullify the dlvPassId in sale table.
						 */
						if (existingPass.getPK().getId().equals(
								fdOrder.getDeliveryPassId())) {
							dlvpsb.revoke(existingPass);
							/*
							 * Customer has purchased Unlimited DP using
							 * Delivery promo. Extend by a week.
							 */
							if (existingPass.getType().isUnlimited()) {
								extendDeliveryPass(
										dlvpsb,
										existingPass,
										1,
										saleId,
										"Extend DP by a week.",
										EnumDlvPassExtendReason.DLV_PROMOTION_APPLIED
												.getName());
							}
						}

						sb.updateDlvPassIdToSale(saleId, null);

					}
				}

			}
		} else {
			/*
			 * The order does not contain a delivery pass. Make sure it was not
			 * removed during modify order.
			 */
			List<DeliveryPassModel> dlvPasses = dlvpsb
					.getDlvPassesByOrderId(saleId);
			if (dlvPasses != null && dlvPasses.size() > 0) {

				/*
				 * If the delivery pass removed from the cart is the same as the
				 * applied delivery pass, Order doesn't use delivery pass
				 * anymore
				 */
				DeliveryPassModel purchasedPass = dlvPasses.get(0);
				if (purchasedPass.getPK().getId().equals(
						fdOrder.getDeliveryPassId())) {
					sb.updateDlvPassIdToSale(saleId, null);
				}

				/*
				 * Delivery pass already exists for this order. So it was
				 * removed now. So remove it from the database as well and
				 * nullify the dlvPassId in sale table.
				 */
				// So nullify the exisitng dlv pass id in sale table before
				// removing the delivery pass.

				dlvpsb.remove(purchasedPass);

			}
		}
	}

	/**
	 * charge an order (modify & send msg to SAP) due to Insufficent funds.
	 * 
	 * @param identity
	 *            the customer's identity reference
	 * @throws FDResourceException
	 *             if an error occured while accessing remote resources
	 */
	public void chargeOrder(FDIdentity identity, String saleId,
			ErpPaymentMethodI paymentMethod, boolean sendEmail,
			CustomerRatingI cra, CrmAgentModel agent, double additionalCharge)
			throws FDResourceException, ErpTransactionException,
			ErpFraudException, ErpAuthorizationException, ErpAddressVerificationException {

		try {
			FDOrderAdapter fdOrder = (FDOrderAdapter) getOrder(identity, saleId);
			ErpSaleModel sale = fdOrder.getSale();
			EnumSaleStatus status = sale.getStatus();

			if (!EnumSaleStatus.SETTLEMENT_FAILED.equals(status)
					&& !EnumSaleStatus.PAYMENT_PENDING.equals(status)) {
				throw new ErpTransactionException(
						"This order cannot be charged because payment is not pending or settlement did not fail.");
			}

			ErpCustomerManagerSB custManager = this.getErpCustomerManagerHome()
					.create();

			PaymentManagerSB paymentManager = this.getPaymentManagerHome()
					.create();

			// void all previous captures WITHOUT being in an explicit
			// transaction
			if (EnumSaleStatus.PAYMENT_PENDING.equals(status)) {
				paymentManager.voidCapturesNoTrans(saleId);
			}

			ErpPaymentMethodModel oldPaymentMethod = (ErpPaymentMethodModel) sale
					.getCurrentOrder().getPaymentMethod();

			ErpModifyOrderModel modifyOrder = new ErpModifyOrderModel();
			// want to add a NEW modify order with new payment method, so we
			// need to set all primary keys to null
			modifyOrder.set(sale.getCurrentOrder(), true);
			modifyOrder.setPaymentMethod(paymentMethod);
			modifyOrder.setTransactionDate(new Date());
			modifyOrder.setTransactionInitiator(agent.getUserId());
			modifyOrder
					.setTransactionSource(EnumTransactionSource.CUSTOMER_REP);

			// this changes the payment information for the order
			custManager.modifyOrder(saleId, modifyOrder, sale
					.getUsedPromotionCodes(), cra, agent.getRole(), false);

			if (additionalCharge > 0) {
				// create a charge invoice sales action
				custManager.addChargeInvoice(saleId, additionalCharge);
			}

			// authorize the sale with new payment method
			List<ErpAuthorizationModel> auths = paymentManager.authorizeSaleRealtime(saleId);
			// capture the sale authorization
			paymentManager.captureAuthorizations(saleId, auths);

			// create a case only if the payment method used to re charge order
			// is not the payment method that failed
			if (!oldPaymentMethod.getPK().equals(
					(ErpPaymentMethodModel) paymentMethod)) {
				CrmSystemCaseInfo caseInfo = this.buildBadAccountReviewCase(
						sale.getCustomerPk().getId(), saleId);
				custManager.createCase(caseInfo, false);
			}

			if (sendEmail) {
				FDCustomerInfo fdInfo = this.getCustomerInfo(identity);
				int orderCount = getValidOrderCount(identity);
				fdInfo.setNumberOfOrders(orderCount);
				// get order again with new payment method
				fdOrder = (FDOrderAdapter) getOrder(identity, saleId);
				this.doEmail(FDEmailFactory.getInstance()
						.createChargeOrderEmail(fdInfo, fdOrder,
								additionalCharge));
			}
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			Exception ex=(Exception)re.getCause();
			if(ex instanceof ErpAddressVerificationException) throw (ErpAddressVerificationException)ex;
			
			throw new FDResourceException(re);
		}
	}

	private CrmSystemCaseInfo buildBadAccountReviewCase(String customerPK,
			String saleId) {
		CrmCaseSubject subject = CrmCaseSubject
				.getEnum(CrmCaseSubject.CODE_BAD_ACCOUNT_REVIEW);
		String summary = "Order # "
				+ saleId
				+ " has been re-charged after initial settlement failure.  Please review and possibly remove from Bad Accounts and Customer Alert.";
		PrimaryKey salePK = saleId != null ? new PrimaryKey(saleId) : null;
		return new CrmSystemCaseInfo(new PrimaryKey(customerPK), salePK,
				subject, summary);
	}

	/**
	 * Adds a complaint to the user's list of complaints and begins the
	 * associated credit issuing process
	 * 
	 * @param ErpComplaintModel
	 *            represents the complaint
	 * @param String
	 *            the PK of the sale to which the complaint is to be added
	 * @throws ErpComplaintException
	 *             if order was not in proper state to accept complaints
	 */
	public PrimaryKey addComplaint(ErpComplaintModel complaint, String saleId,
			String erpCustomerId, String fdCustomerId, boolean autoApproveAuthorized, Double limit )
			throws FDResourceException, ErpComplaintException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();

			PrimaryKey cPk = sb.addComplaint(complaint, saleId,  autoApproveAuthorized, limit );
			ErpComplaintModel alteredComplaint = sb.getComplaintInfo(saleId,
					cPk.getId()).getComplaint();
			
			//System.out.println(alteredComplaint.okToSendEmailOnCreate());
			//System.out.println(alteredComplaint.getStatus());
			//System.out.println(alteredComplaint.okToSendEmailOnCreate());
			//System.out.println(alteredComplaint.okToSendEmailOnApproval());
			
			if (alteredComplaint.okToSendEmailOnCreate()
					|| (alteredComplaint.getStatus().equals(
							EnumComplaintStatus.APPROVED) && (alteredComplaint
							.okToSendEmailOnCreate() || alteredComplaint
							.okToSendEmailOnApproval()))) {
				FDCustomerInfo fdInfo = getCustomerInfo(new FDIdentity(
						erpCustomerId, fdCustomerId));
				FDOrderI order = getOrder(saleId);
				EnumEStoreId estoreId = order.getEStoreId();
				if (null != order
						&& EnumSaleType.GIFTCARD.equals(order.getOrderType())) {
					modifyGiftCardComplaint(saleId, order, alteredComplaint);
					this.doEmail(FDGiftCardEmailFactory.getInstance()
							.createConfirmCreditEmail(fdInfo, saleId,
									alteredComplaint,estoreId));
				} else {
					this.doEmail(FDEmailFactory.getInstance()
							.createConfirmCreditEmail(fdInfo, saleId,
									alteredComplaint,estoreId));
				}
				alteredComplaint.getCustomerEmail().setMailSent(true);
				sb.updateEmailSentFlag(alteredComplaint.getCustomerEmail());
			}
			return cPk;
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re.getMessage());
		}
	}

	/**
	 * Adds a complaint to the user's list of complaints and begins the
	 * associated credit issuing process
	 * 
	 * @param ErpComplaintModel
	 *            represents the complaint
	 * @param String
	 *            the PK of the sale to which the complaint is to be added
	 * @throws ErpComplaintException
	 *             if order was not in proper state to accept complaints
	 */
	public void storeCustomerRecipents(List recipentList, String saleId, String erpCustomerId) throws FDResourceException {
		Connection conn = null;

		try {
			conn = this.getConnection();
			GiftCardPersistanceDAO.storeRecipents(conn, erpCustomerId, saleId, recipentList);
		} catch (Exception e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	private static final String PENDING_COMPLAINT_QUERY = "select c.id as complaint_id "
			+ "from cust.sale s, cust.complaint c "
			+ "where s.status = 'PPG' and c.sale_id=s.id and c.amount <= ? and c.status = 'PEN' "
			+ "and c.id not in (select complaint_id from cust.complaintline where c.id = complaint_id and method = 'CSH') "
			+ "union all "
			+ "select c.id as complaint_id "
			+ "from cust.sale s, cust.complaint c "
			+ "where s.status = 'STL' and c.sale_id=s.id and c.amount <= ? and c.status = 'PEN' ";

	public List<String> getComplaintsForAutoApproval() throws FDResourceException, ErpComplaintException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			PreparedStatement ps = conn.prepareStatement(PENDING_COMPLAINT_QUERY);
			//ps.setDouble(1, ErpServicesProperties.getCreditAutoApproveAmount());
			ps.setBigDecimal(1, new java.math.BigDecimal(ErpServicesProperties.getCreditAutoApproveAmount()));
			//ps.setDouble(2, ErpServicesProperties.getCreditAutoApproveAmount());
			ps.setBigDecimal(2, new java.math.BigDecimal(ErpServicesProperties.getCreditAutoApproveAmount()));
			ResultSet rs = ps.executeQuery();
			List<String> lst = new ArrayList<String>();

			while (rs.next()) {
				lst.add(rs.getString("COMPLAINT_ID"));
			}

			rs.close();
			ps.close();

			return lst;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public void approveComplaint(String complaintId, boolean isApproved,
			String csrId, boolean sendMail,Double limit) throws FDResourceException,
			ErpComplaintException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			String saleId = sb.approveComplaint(complaintId, isApproved, csrId,limit);
			if (isApproved) {
				ErpComplaintInfoModel complaintInfo = sb.getComplaintInfo(
						saleId, complaintId);
				FDCustomerEB eb = getFdCustomerHome().findByErpCustomerId(
						complaintInfo.getCustomerId());
				FDCustomerInfo fdInfo = getCustomerInfo(new FDIdentity(
						complaintInfo.getCustomerId(), eb.getPK().getId()));
				ErpCustomerEmailModel cem = complaintInfo.getComplaint()
						.getCustomerEmail();

				boolean otherEmailConditions = (cem != null
						&& !cem.isMailSent() && !complaintInfo.getComplaint()
						.dontSendEmail());
				if (sendMail && otherEmailConditions) {
					EnumEStoreId estoreId = EnumEStoreId.valueOfContentId((ContentFactory.getInstance().getStoreKey().getId()));
					this.doEmail(FDEmailFactory.getInstance()
							.createConfirmCreditEmail(fdInfo, saleId,
									complaintInfo.getComplaint(), estoreId));
					complaintInfo.getComplaint().getCustomerEmail()
							.setMailSent(true);
					sb.updateEmailSentFlag(complaintInfo.getComplaint()
							.getCustomerEmail());
				}
			}
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (FinderException fe) {
			throw new FDResourceException(fe);
		} catch (RemoteException re) {
			throw new FDResourceException(re.getMessage());
		}
	}

	/**
	 * Check availability of an order.
	 * 
	 * @return Map of order line number / FDAvailabilityI objects
	 */
	public Map<String, FDAvailabilityI> checkAvailability(FDIdentity identity,
			ErpCreateOrderModel createOrder, long timeout)
			throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			Map<String,List<ErpInventoryModel>> erpInvs = sb.checkAvailability(new PrimaryKey(identity
					.getErpCustomerPK()), createOrder, timeout);

			Map<String, FDAvailabilityI> fdInvMap = buildAvailability(
					createOrder, erpInvs);

			logATPFailures(createOrder, fdInvMap, identity.getErpCustomerPK());

			return fdInvMap;
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	private Map<String, FDAvailabilityI> buildAvailability(
			ErpCreateOrderModel createOrder, Map erpInvs)
			throws FDResourceException {
		Map<String, FDAvailabilityI> fdInvMap = new HashMap<String, FDAvailabilityI>();
		for (Iterator i = createOrder.getOrderLines().iterator(); i.hasNext();) {
			ErpOrderLineModel ol = (ErpOrderLineModel) i.next();
			List<ErpInventoryModel> inventories = (List<ErpInventoryModel>) erpInvs
					.get(ol.getOrderLineNumber());

			FDAvailabilityI fdInv;
			switch (inventories.size()) {
			case 0:
				fdInv = NullAvailability.AVAILABLE;
				break;
			case 1:
				fdInv = buildStockAvailability(ol, inventories.get(0));
				break;
			default:
				fdInv = buildCompositeAvailability(ol, inventories);

			}

			fdInvMap.put(ol.getOrderLineNumber(), fdInv);
		}
		return fdInvMap;
	}

	private FDAvailabilityI buildStockAvailability(ErpOrderLineModel ol,
			ErpInventoryModel erpInv) throws FDResourceException {
		try {
			ProductModel p = ContentFactory.getInstance().getProduct(
					ol.getSku().getSkuCode());
			
			return new FDStockAvailability(erpInv, 
						 ol.getQuantity(), p.getQuantityMinimum(), p.getQuantityIncrement());
			
		} catch (FDSkuNotFoundException e) {
			throw new FDResourceException(e);
		}
	}

	private FDAvailabilityI buildCompositeAvailability(ErpOrderLineModel ol,
			List<ErpInventoryModel> inventories) throws FDResourceException {
		Map<String, FDAvailabilityI> avails = new HashMap<String, FDAvailabilityI>(
				inventories.size());

		boolean header = true;
		for (ErpInventoryModel erpInv : inventories) {
			FDAvailabilityI av = buildStockAvailability(ol, erpInv);
			if (header) {
				avails.put(null, av);
				header = false;
			} else {
				avails.put(erpInv.getSapId(), av);
			}
		}

		return new FDCompositeAvailability(avails);
	}

	private void logATPFailures(ErpCreateOrderModel createOrder,
			Map<String, FDAvailabilityI> fdInvMap, String erpCustomerId)
			throws FDResourceException {
		List<ATPFailureInfo> lst = new ArrayList<ATPFailureInfo>();
		String plantId="1000";
		if(createOrder.getDeliveryInfo()!=null && createOrder.getDeliveryInfo().getDeliveryPlantInfo()!=null) {
			plantId=createOrder.getDeliveryInfo().getDeliveryPlantInfo().getPlantId();
		}
		
		DateRange requestedRange = new DateRange(createOrder.getDeliveryInfo()
				.getDeliveryStartTime(), createOrder.getDeliveryInfo()
				.getDeliveryEndTime());

		for (Iterator<String> i = fdInvMap.keySet().iterator(); i.hasNext();) {
			String key = i.next();
			FDAvailabilityI inv = fdInvMap.get(key);

			FDAvailabilityInfo info = inv.availableCompletely(requestedRange);

			if (!info.isAvailable() && info instanceof FDStockAvailabilityInfo) {
				FDStockAvailabilityInfo sInfo = (FDStockAvailabilityInfo) info;
				ErpOrderLineModel ol = createOrder.getOrderLine(key);
				ATPFailureInfo fi = new ATPFailureInfo(requestedRange
						.getStartDate(), ol.getMaterialNumber(), ol
						.getQuantity(), ol.getSalesUnit(), sInfo.getQuantity(),
						erpCustomerId,plantId);
				lst.add(fi);
			}
		}

		this.storeATPFailureInfos(lst);
	}

	public FDOrderI getOrderForCRM(FDIdentity identity, String saleId)
			throws FDResourceException {

		FDOrderI order = getOrderForCRM(saleId);
		if (!order.getCustomerId().equals(identity.getErpCustomerPK())) {
			throw new FDResourceException("Sale doesn't belong to customer");
		}
		return order;
	}


	public FDOrderI getOrder(FDIdentity identity, String saleId)
			throws FDResourceException {

		FDOrderI order = getOrder(saleId);
		if (!order.getCustomerId().equals(identity.getErpCustomerPK())) {
			throw new FDResourceException("Sale doesn't belong to customer");
		}
		return order;
	}


	public FDOrderI getOrderForCRM(String saleId) throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			ErpSaleModel saleModel = sb.getOrder(new PrimaryKey(saleId));

			LOGGER.debug(new String("ordernum: "
					+ saleId
					+ "   rsrvID: "
					+ saleModel.getRecentOrderTransaction().getDeliveryInfo()
							.getDeliveryReservationId()));
			
			return new FDOrderAdapter(saleModel, true);

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}		
	}

	
	public FDOrderI getOrder(String saleId) throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			ErpSaleModel saleModel = sb.getOrder(new PrimaryKey(saleId));

			LOGGER.debug(new String("ordernum: "
					+ saleId
					+ "   rsrvID: "
					+ saleModel.getRecentOrderTransaction().getDeliveryInfo()
							.getDeliveryReservationId()));
			
			return new FDOrderAdapter(saleModel, false);
			

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}
	
	public List<FDOrderI> getOrders(List<String> saleIds) throws FDResourceException, RemoteException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			List<PrimaryKey> keys = new ArrayList<PrimaryKey>();
			for(String saleId: saleIds){
				keys.add(new PrimaryKey(saleId));
			}
			List<ErpSaleModel> saleModels = sb.getOrders(keys);

			LOGGER.debug(new String("ordernums: "
					+ saleIds));
			
			List<FDOrderI> adapters = new ArrayList<FDOrderI>();
			for(ErpSaleModel model: saleModels){
				adapters.add(new FDOrderAdapter(model));
			}
			return adapters;
			

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public ErpSaleModel getErpSaleModel(String saleId) throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			return sb.getOrder(new PrimaryKey(saleId));
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}
	
	public List<String> getUsedReservations(String customerId) throws FDResourceException{

		Connection conn = null;
		try {
			conn = getConnection();

			PreparedStatement ps = conn
					.prepareStatement("SELECT DI.RESERVATION_ID  FROM CUST.SALE S, CUST.SALESACTION SA, CUST.DELIVERYINFO DI WHERE S.ID = SA.SALE_ID AND " +
							" S.CROMOD_DATE = SA.ACTION_DATE AND SA.ACTION_TYPE IN ('CRO','MOD') AND SA.REQUESTED_DATE > SYSDATE " +
							" AND S.TYPE ='REG' AND S.STATUS <> 'CAN'  AND DI.SALESACTION_ID = SA.ID AND S.CUSTOMER_ID = ?");
			ps.setString(1, customerId);
			ResultSet rs = ps.executeQuery();

			Set<String> rsvIds = new HashSet<String>();
			while (rs.next()) {
				rsvIds.add(rs.getString("RESERVATION_ID"));
			}

			rs.close();
			ps.close();

			return new ArrayList<String>(rsvIds);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	
	}

	public List<DlvSaleInfo> getOrdersByTruck(String truckNumber, Date dlvDate) throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			return sb.getOrdersByTruckNumber(truckNumber, dlvDate);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public ErpOrderHistory getOrderHistoryInfo(FDIdentity identity) throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			return sb.getOrderHistoryInfo(new PrimaryKey(identity
					.getErpCustomerPK()));

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public ErpPromotionHistory getPromoHistoryInfo(FDIdentity identity) throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			return sb.getPromoHistoryInfo(new PrimaryKey(identity
					.getErpCustomerPK()));

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	/**
	 * 
	 * @return Map of String (productId) -> Integer (score)
	 * @throws FDResourceException
	 */
	public Map<String, Integer> getProductPopularity() throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			PreparedStatement ps = conn
					.prepareStatement("select substr(content_key, instr(content_key, ':')+1) as product_id, score from cust.popularity where content_key like 'Product:%'");
			ResultSet rs = ps.executeQuery();

			Map<String, Integer> m = new HashMap<String, Integer>();
			while (rs.next()) {
				m.put(rs.getString("product_id"), new Integer(rs.getInt("score")));
			}

			rs.close();
			ps.close();

			return m;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	/**
	 * Locate customer records matching the specified criteria
	 * 
	 * @param custNumber
	 * @param firstName
	 * @param middleName
	 * @param lastName
	 * @param email
	 * @param phone
	 * 
	 * @return Collection of CustomerSearchResult objects
	 * 
	 * @throws FDResourceException
	 *             if an error occured using remote resources
	 */
	public List<FDCustomerOrderInfo> locateCustomers(FDCustomerSearchCriteria criteria) throws FDResourceException {

		Connection conn = null;

		try {
			conn = getConnection();
			return FDCustomerOrderInfoDAO.findCustomersByCriteria(conn, criteria);

		} catch (SQLException e) {
			throw new FDResourceException(e, "Could not find customers matching criteria entered.");
		} finally {
			close(conn);
		}
	}

	public List<FDCustomerOrderInfo> locateOrders(FDOrderSearchCriteria criteria) throws FDResourceException {

		Connection conn = null;

		try {
			conn = getConnection();

			return FDCustomerOrderInfoDAO.findOrdersByCriteria(conn, criteria);

		} catch (SQLException e) {
			e.printStackTrace();
			throw new FDResourceException(e, "Could not find order matching criteria entered.");
		} finally {
			close(conn);
		}
	}

	public void setActive(FDActionInfo info, boolean active)
			throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			sb.setActive(new PrimaryKey(info.getIdentity().getErpCustomerPK()), active);
			if(true==active) {
				FDCustomerEB fdCustomerEB=getFdCustomerHome().findByErpCustomerId(info.getIdentity().getErpCustomerPK());
				fdCustomerEB.resetPymtVerifyAttempts();
			}
			this.logActivity(info.createActivity(active ? EnumAccountActivityType.ACTIVATE_ACCOUNT : 
														EnumAccountActivityType.DEACTIVATE_ACCOUNT));

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException e) {
			throw new FDResourceException(e);
		}
	}

	public void doEmail(XMLEmailI email) throws FDResourceException {
		try {
			MailerGatewaySB mailer = getMailerHome().create();
			mailer.enqueueEmail(email);
		} catch (CreateException ce) {
			throw new FDResourceException(ce, "Cannot create MailerGatewayBean");
		} catch (RemoteException re) {
			throw new FDResourceException(re, "Cannot talk to MailerGatewayBean");
		}
	}

	public String generatePasswordRequest(PrimaryKey fdCustomerPk,
			java.util.Date expiration) throws FDResourceException {
		try {
			FDCustomerEB eb = getFdCustomerHome() .findByPrimaryKey(fdCustomerPk);
			return eb.generatePasswordRequest(expiration);
		} catch (FinderException fe) {
			throw new FDResourceException("Cannot find customer for pk: " + fdCustomerPk);
		} catch (RemoteException re) {
			throw new FDResourceException("Cannot talk to customer bean");
		}
	}

	public boolean sendPasswordEmail(String emailAddress, boolean toAltEmail)
			throws FDResourceException, PasswordNotExpiredException {
		try {

			FDCustomerEB fdCustomerEB = getFdCustomerHome().findByUserId( emailAddress);
			FDCustomerModel fdCustomer = (FDCustomerModel) fdCustomerEB.getModel();

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, 2);
			java.util.Date expiration = cal.getTime();

			if (fdCustomer.getPasswordRequestExpiration() != null
					&& new java.util.Date().before(fdCustomer
							.getPasswordRequestExpiration())) {
				throw new PasswordNotExpiredException(
						"The password request has not expired yet.");
			}

			String requestId = fdCustomerEB.generatePasswordRequest(expiration);

			ArrayList<String> ccList = new ArrayList<String>();
			if (toAltEmail) {
				ErpCustomerEB erpCustomerEB = getErpCustomerHome()
						.findByPrimaryKey(
								new PrimaryKey(fdCustomer.getErpCustomerPK()));
				ErpCustomerInfoModel erpCustomerInfo = erpCustomerEB
						.getCustomerInfo();
				ccList.add(erpCustomerInfo.getAlternateEmail());
			}

			FDCustomerInfo fdInfo = this.getCustomerInfo(new FDIdentity(
					fdCustomer.getErpCustomerPK(), fdCustomer.getPK().getId()));
			EnumEStoreId estoreId = EnumEStoreId.valueOfContentId((ContentFactory.getInstance().getStoreKey().getId()));
			
			this.doEmail(FDEmailFactory.getInstance()
					.createForgotPasswordEmail(fdInfo, requestId, expiration, ccList, estoreId));

			return true;

		} catch (FinderException ex) {
			throw new FDResourceException(ex);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public boolean isCorrectPasswordHint(String emailAddress, String hint)
			throws FDResourceException, ErpFraudException {
		try {
			FDCustomerEB custEB = getFdCustomerHome()
					.findByUserId(emailAddress);
			//
			// Check for correct password hint
			//
			FDCustomerModel fdCustomer = (FDCustomerModel) custEB.getModel();
			boolean validHint = hint.equalsIgnoreCase(fdCustomer
					.getPasswordHint());
			if (!validHint) {
				//
				// Check that we're within acceptable number of guesses
				//
				if (custEB.incrementPasswordRequestAttempts() > 5)
					throw new ErpFraudException(
							EnumFraudReason.MAX_PASSWORD_HINT);
			}
			return validHint;

		} catch (FinderException ex) {
			throw new FDResourceException(ex);
		} catch (RemoteException ex) {
			throw new FDResourceException(ex);
		}
	}

	public boolean isPasswordRequestExpired(String emailAddress, String passReq)
			throws FDResourceException {
		try {
			FDCustomerEB custEB = getFdCustomerHome()
					.findByUserIdAndPasswordRequest(emailAddress, passReq);
			java.util.Date currentDate = new java.util.Date();
			return currentDate.after(custEB.getPasswordRequestExpiration());

		} catch (FinderException ex) {
			throw new FDResourceException(ex);
		} catch (RemoteException ex) {
			throw new FDResourceException(ex);
		}
	}

	public void changePassword(FDActionInfo info, String emailAddress,
			String password) throws FDResourceException {
		try {
			//
			// Get ErpCustomerEB
			//
			ErpCustomerEB erpCustomerEB = this.getErpCustomerHome()
					.findByUserId(emailAddress);
			erpCustomerEB.setPasswordHash(MD5Hasher.hash(password));
			String erpCustId = erpCustomerEB.getPK().getId();

			//
			// Remove password request
			//
			FDCustomerEB custEB = getFdCustomerHome().findByErpCustomerId(
					erpCustId);
			custEB.erasePasswordRequest();

			FDIdentity identity = new FDIdentity(erpCustId, custEB.getPK()
					.getId());
			info.setIdentity(identity);
			this.logActivity(info
					.createActivity(EnumAccountActivityType.CHANGE_PASSWORD));

		} catch (FinderException ex) {
			throw new FDResourceException(ex);
		} catch (RemoteException ex) {
			throw new FDResourceException(ex);
		}
	}

	/**
	 * Store the user.
	 * 
	 * @param user
	 *            the customer's user object
	 * 
	 * @throws FDResourceException
	 *             if an error occured using remote resources
	 */
	public void storeUser(FDUser user) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.storeUser(conn, user);

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle, "Unable to store FDUser");
		} finally {
			close(conn);
		}
	}

	
	public void updateOrderInModifyState(ErpSaleModel sale)	throws FDResourceException{

		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.updateOrderInModifyState(conn, sale.getId());
			 
		} catch (SQLException sqle) {
			LOGGER.info("LOCKED THE ORDER FOR MODIFICATION FAILED... "+sale.getId());
			throw new FDResourceException(sqle, "Unable to store FDUser");
		} finally {
			close(conn);
		}
	
	}
	
	public void updateOrderInProcess(String orderNum)	throws FDResourceException{
		LOGGER.info("UPDATING THE ORDER IN PROCESS ... "+orderNum);
		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.updateOrderInProcess(conn, orderNum);
			 
		} catch (SQLException sqle) {
			LOGGER.info("UPDATE THE ORDER IN PROCESS FAILED... "+orderNum);
			throw new FDResourceException(sqle, "Unable to store FDUser");
		} finally {
			close(conn);
		}
	
	}
	

	public boolean isReadyForPick(String orderNum) throws FDResourceException{
		LOGGER.info("CHECKING THE ORDER IS READY FOR PICKING... "+orderNum);
		Connection conn = null;
		try {
			conn = getConnection();
			return FDUserDAO.isReadyForPick(conn, orderNum);
			 
		} catch (SQLException sqle) {
			LOGGER.info("CHECKING THE ORDER IS READY FOR PICKING FAILED... "+orderNum);
			throw new FDResourceException(sqle, "Unable to store FDUser");
		} finally {
			close(conn);
		}
	
	}
	

	public void releaseModificationLock(String orderId)	throws FDResourceException{

		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.releaseModificationLock(conn, orderId);
			 
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle, "Unable to store FDUser");
		} finally {
			close(conn);
		}
	
	}
	
	
	
	public void storeCohortName(FDUser user) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.storeCohortName(conn, user);

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle,
					"Unable to store Cohort ID for user");
		} finally {
			close(conn);
		}
	}

	public void storeSavedRecipients(FDUser user, List<SavedRecipientModel> recipientList) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			SavedRecipientDAO.storeSavedRecipients(conn, user.getPrimaryKey(),recipientList);

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle,
					"Unable to store saved recipient list for user");
		} finally {
			close(conn);
		}
	}

	public void storeSavedRecipient(FDUser user, SavedRecipientModel model) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			SavedRecipientDAO.storeSavedRecipient(conn, user.getPrimaryKey(),
					model);

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle,
					"Unable to store saved recipient  for user");
		} finally {
			close(conn);
		}
	}

	public void updateSavedRecipient(FDUser user, SavedRecipientModel model) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			SavedRecipientDAO.updateSavedRecipient(conn, user.getPrimaryKey(),
					model);

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle,
					"Unable to store saved recipient  for user");
		} finally {
			close(conn);
		}
	}

	public void deleteSavedRecipients(FDUser user) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			SavedRecipientDAO.deleteSavedRecipients(conn, user.getPrimaryKey(), user.getGiftCardType() != null ? user.getGiftCardType().getName() : null);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle,
					"Unable to delete saved recipient  for user");
		} finally {
			close(conn);
		}
	}

	public void deleteSavedRecipient(String savedRecipientId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			SavedRecipientDAO.deleteSavedRecipient(conn, savedRecipientId);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle,
					"Unable to delete saved recipient  for user");
		} finally {
			close(conn);
		}
	}

	public List<SavedRecipientModel> loadSavedRecipients(FDUser user) throws FDResourceException {
		Connection conn = null;
		List<SavedRecipientModel> list = null;
		try {
			conn = getConnection();
			list = SavedRecipientDAO.loadSavedRecipients(conn, user
					.getPrimaryKey());
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle,
					"unable to load saved recipient list");
		} finally {
			close(conn);
		}
		return list;
	}

	public void setSignupPromotionEligibility(FDActionInfo info, boolean eligible) throws FDResourceException {
		try {
			FDCustomerEB fdCustomerEB = this.getFdCustomerHome()
					.findByErpCustomerId(info.getIdentity().getErpCustomerPK());
			fdCustomerEB.setProfileAttribute("signup_promo_eligible",
					eligible ? "allow" : "deny");

			if (info != null) {
				logActivity(info
						.createActivity(eligible ? EnumAccountActivityType.ENABLE_SIGNUP_PROMO
								: EnumAccountActivityType.DISABLE_SIGNUP_PROMO));
			}
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}
	
	public String getDepotCode(FDIdentity identity) throws FDResourceException {
		try {
			FDCustomerEB eb = getFdCustomerHome().findByPrimaryKey(
					new PrimaryKey(identity.getFDCustomerPK()));
			return eb.getDepotCode();
		} catch (FinderException fe) {
			throw new FDResourceException(fe);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public void setDepotCode(FDIdentity identity, String depotCode)	throws FDResourceException {
		try {
			FDCustomerEB eb = getFdCustomerHome().findByPrimaryKey(
					new PrimaryKey(identity.getFDCustomerPK()));
			eb.setDepotCode(depotCode);
		} catch (FinderException fe) {
			throw new FDResourceException(fe);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public List<String> getReminderListForToday() throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			ps = conn
					.prepareStatement("select customer_id from cust.customerinfo where reminder_frequency > 0 and reminder_last_send + reminder_frequency < ? and reminder_day_of_week = ? ");
			Calendar today = Calendar.getInstance();
			ps.setTimestamp(1,
					new java.sql.Timestamp(today.getTime().getTime()));
			ps.setInt(2, today.get(Calendar.DAY_OF_WEEK));
			rs = ps.executeQuery();
			List<String> lst = new ArrayList<String>();
			while (rs.next()) {
				lst.add(rs.getString("CUSTOMER_ID"));
			}
			return lst;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				LOGGER.warn("error while cleanup", e);
			}
			close(conn);
		}
	}

	public void sendReminderEmail(PrimaryKey custPk) throws FDResourceException {

		try {
			FDCustomerEB fdCustomer = getFdCustomerHome().findByErpCustomerId(
					custPk.getId());
			FDIdentity identity = new FDIdentity(custPk.getId(), fdCustomer
					.getPK().getId());
			ErpCustomerEB erpCustomer = getErpCustomerHome().findByPrimaryKey(
					custPk);

			ErpCustomerInfoModel custInfo = erpCustomer.getCustomerInfo();
			FDCustomerInfo fdInfo = getCustomerInfo(identity);
			if (custInfo.isReminderAltEmail()) {
				fdInfo.setAltEmailAddress(custInfo.getAlternateEmail());
			}
			String lastOrderID = getLastOrderID(identity);
			fdInfo.setLastOrderId(lastOrderID);
			this.doEmail(FDEmailFactory.getInstance().createReminderEmail(
					fdInfo, custInfo.isReminderAltEmail()));

			custInfo.setLastReminderEmailSend(new java.util.Date());
			erpCustomer.setCustomerInfo(custInfo);

		} catch (RemoteException e) {
			throw new FDResourceException(e);
		} catch (FinderException e) {
			throw new FDResourceException(e);
		}
	}

	public void authorizeSale(String salesId) throws FDResourceException {

		try {
			PaymentManagerSB sb = this.getPaymentManagerHome().create();
			EnumPaymentResponse response = sb.authorizeSale(salesId,false);

			if (!EnumPaymentResponse.APPROVED.equals(response)
					&& !EnumPaymentResponse.ERROR.equals(response)) {
				sendAuthFailedEmail(salesId);
			}
		} catch (RemoteException e) {
			throw new FDResourceException(e);
		} catch (CreateException e) {
			throw new FDResourceException(e);
		}
	}
	public void authorizeSale(String salesId, boolean force) throws FDResourceException {

		try {
			PaymentManagerSB sb = this.getPaymentManagerHome().create();
			EnumPaymentResponse response = sb.authorizeSale(salesId,force);

			if (!EnumPaymentResponse.APPROVED.equals(response)
					&& !EnumPaymentResponse.ERROR.equals(response)) {
				sendAuthFailedEmail(salesId);
			}
		} catch (RemoteException e) {
			throw new FDResourceException(e);
		} catch (CreateException e) {
			throw new FDResourceException(e);
		}
	}

	private void sendAuthFailedEmail(String saleID) throws FDResourceException {

		try {
			FDOrderI order = this.getOrder(saleID);
			int authFailType=getAuthFailEmailTemplate(order.getAuthFailDescription());
			FDCustomerEB fdCustomer = getFdCustomerHome().findByErpCustomerId(
					order.getCustomerId());
			FDIdentity identity = new FDIdentity(order.getCustomerId(),
					fdCustomer.getPK().getId());
			FDCustomerInfo custInfo = this.getCustomerInfo(identity);
			Calendar cal = calculateCutOffTime(order);
			this.doEmail(FDEmailFactory.getInstance()
					.createAuthorizationFailedEmail(custInfo, saleID,
							order.getDeliveryReservation().getStartTime(),
							order.getDeliveryReservation().getEndTime(),
							cal.getTime(),authFailType));
		} catch (FinderException e) {
			throw new FDResourceException(e);
		} catch (RemoteException e) {
			throw new FDResourceException(e);
		}
	}

	/**
	 * @param order
	 * @return
	 */
	private Calendar calculateCutOffTime(FDOrderI order) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(order.getDeliveryReservation().getCutoffTime());
		cal.add(Calendar.HOUR_OF_DAY, -1 * ErpServicesProperties.getCancelOrdersB4Cutoff());
		return cal;
	}

	/*
	 * Added for APPDEV-89 . Sending a seperate Auth failed email to auto renew
	 * DP customers. AR - Stands for Auto Renew DP
	 */
	private void sendARAuthFailedEmail(String saleID)
			throws FDResourceException {

		try {
			FDOrderI order = this.getOrder(saleID);
			FDCustomerEB fdCustomer = getFdCustomerHome().findByErpCustomerId(
					order.getCustomerId());
			FDIdentity identity = new FDIdentity(order.getCustomerId(),
					fdCustomer.getPK().getId());
			FDCustomerInfo custInfo = this.getCustomerInfo(identity);
			Calendar cal = calculateCutOffTime(order);
			this.doEmail(FDEmailFactory.getInstance()
					.createARAuthorizationFailedEmail(custInfo, saleID,
							order.getDeliveryReservation().getStartTime(),
							order.getDeliveryReservation().getEndTime(),
							cal.getTime()));
		} catch (FinderException e) {
			throw new FDResourceException(e);
		} catch (RemoteException e) {
			throw new FDResourceException(e);
		}
	}

	private void storeATPFailureInfos(List<ATPFailureInfo> infos)
			throws FDResourceException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			ATPFailureDAO dao = new ATPFailureDAO();
			dao.create(conn, infos);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	//Logistics ReDesign - Removed cancellation handled internally in logistics
	/*public FDReservation changeReservation(FDIdentity identity,
			FDReservation oldReservation, String timeslotId,
			EnumReservationType rsvType, String addressId, FDActionInfo aInfo, boolean chefstable, TimeslotEvent event)
			throws FDResourceException, ReservationException {
		this.cancelReservation(identity, oldReservation, rsvType, aInfo, event);
		aInfo.setNote("Make Pre-Reservation");
		
		return this.makeReservation(identity, timeslotId, rsvType, addressId,
				aInfo, chefstable, event, false);
	}
*/
	//Logistics Redesign - commenting the logic that is handled in logistics
	public FDReservation makeReservation(FDUserI user,
			String timeslotId, EnumReservationType rsvType, String addressId,
			FDActionInfo aInfo, boolean chefsTable, TimeslotEvent event, boolean isForced) throws FDResourceException,
			ReservationException {
		
		FDIdentity identity = user.getIdentity();
		ErpAddressModel address=getAddress(identity, addressId);
		/*geocodeAddress(address);
		FDTimeslot timeslot = FDDeliveryManager.getInstance().getTimeslotsById(timeslotId, address.getBuildingId(), false);
		
		long duration = timeslot.getCutoffDateTime().getTime()
				- System.currentTimeMillis()
				- (FDStoreProperties.getPreReserveHours() * DateUtil.HOUR);
		if (duration < 0) {
			duration = Math.min(timeslot.getCutoffDateTime().getTime()
					- System.currentTimeMillis(), DateUtil.HOUR);
		}
*/
		FDReservation rsv=FDDeliveryManager.getInstance().reserveTimeslot(timeslotId, identity.getErpCustomerPK(), rsvType, 
				TimeslotLogic.encodeCustomer(address, user), chefsTable, null, isForced, event, false, null);

		/*if (EnumReservationType.RECURRING_RESERVATION.equals(rsvType)) {
			this.updateRecurringReservation(identity,
					timeslot.getBegDateTime(), timeslot.getEndDateTime(),
					addressId);
		}*/
		this.logActivity(getReservationActivityLog(rsv.getTimeslot(), aInfo,
				EnumAccountActivityType.MAKE_PRE_RESERVATION, rsvType));
		
		return rsv;

	}

	
	/**
	 * @return ErpAddressModel for the specified user and addressId, null if the address is not found.
	 */
	public ErpAddressModel getAddress( FDIdentity identity, String addressId ) throws FDResourceException {
		
		if ( identity == null || addressId == null )
			return null;

		Collection<ErpAddressModel> addressList = getShipToAddresses( identity );
		for ( ErpAddressModel address : addressList ) {
			if ( addressId.equals( address.getId() ) ) 
				return address;
		}
		return null;
	}
	

	public void updateWeeklyReservation(FDIdentity identity,
			FDTimeslot timeslot, String addressId, FDActionInfo aInfo)
			throws FDResourceException {
		this.updateRecurringReservation(identity, timeslot.getStartDateTime(),
				timeslot.getEndDateTime(), addressId);
		this.logActivity(getReservationActivityLog(timeslot, aInfo,
				EnumAccountActivityType.UPDATE_WEEKLY_RESERVATION,
				EnumReservationType.RECURRING_RESERVATION));
	}

	private void updateRecurringReservation(FDIdentity identity,
			Date startTime, Date endTime, String addressId)
			throws FDResourceException {
		ErpCustomerEB eb;
		try {
			eb = this.getErpCustomerHome().findByPrimaryKey(
					new PrimaryKey(identity.getErpCustomerPK()));
			int dayOfWeek = startTime != null ? DateUtil.toCalendar(startTime)
					.get(Calendar.DAY_OF_WEEK) : 0;
			ErpCustomerInfoModel info = eb.getCustomerInfo();
			info.setRsvDayOfWeek(dayOfWeek);
			info.setRsvStartTime(startTime);
			info.setRsvEndTime(endTime);
			info.setRsvAddressId(addressId);
			eb.setCustomerInfo(info);
		} catch (RemoteException e) {
			throw new FDResourceException(e);
		} catch (FinderException e) {
			throw new FDResourceException(e);
		}
	}

	public void cancelReservation(FDIdentity identity,
			FDReservation reservation, EnumReservationType rsvType,
			FDActionInfo actionInfo, TimeslotEvent event) throws FDResourceException {

		if (reservation != null) {
			
			ErpAddressModel address = getAddress(identity, reservation
					.getAddressId());
			
			//restore reservation is false
			FDDeliveryManager.getInstance().releaseReservation(reservation.getId(), address , event, false);
		
			this.logActivity(getReservationActivityLog(reservation
					.getTimeslot(), actionInfo,
					EnumAccountActivityType.CANCEL_PRE_RESERVATION, reservation
							.getReservationType()));
		}
		/*
		 * catch (RemoteException e) { throw new FDResourceException(e); } catch
		 * (CreateException e) { throw new FDResourceException(e); }
		 */
	}

	private static final String RECURRING_RSV_QUERY = "SELECT CI.CUSTOMER_ID, CI.EMAIL, CI.RSV_DAY_OF_WEEK, CI.RSV_START_TIME, CI.RSV_END_TIME, "
			+ "A.ID AS ADDRESS_ID, A.ADDRESS1, A.ADDRESS2, A.APARTMENT, A.CITY, A.STATE, A.ZIP, A.SCRUBBED_ADDRESS, A.LONGITUDE, A.LATITUDE, A.SERVICE_TYPE,A.FIRST_NAME,A.LAST_NAME, FDC.ID FDCID  "
			+ "FROM CUST.CUSTOMERINFO CI, CUST.ADDRESS A, CUST.FDCUSTOMER FDC "
			+ "WHERE RSV_ADDRESS_ID IS NOT NULL AND CI.RSV_ADDRESS_ID = A.ID and CI.CUSTOMER_ID = FDC.ERP_CUSTOMER_ID and CI.RSV_DAY_OF_WEEK = ?";

	public List<ReservationInfo> getRecurringReservationList() throws FDResourceException {
		Calendar cal = Calendar.getInstance();
		int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
		return getRRList(day_of_week);
	}

	public List<ReservationInfo> getRecurringReservationList(int day_of_week) throws FDResourceException {
		return getRRList(day_of_week);
	}

	private List<ReservationInfo> getRRList(int day_of_week) throws FDResourceException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			PreparedStatement ps = conn.prepareStatement(RECURRING_RSV_QUERY);
			ps.setInt(1, day_of_week);
			ResultSet rs = ps.executeQuery();
			List<ReservationInfo> rsvInfo = new ArrayList<ReservationInfo>();
			while (rs.next()) {
				String customerId = rs.getString("CUSTOMER_ID");
				String fdCustomerId = rs.getString("FDCID");
				int dayOfWeek = rs.getInt("RSV_DAY_OF_WEEK");
				Date startTime = rs.getTimestamp("RSV_START_TIME");
				Date endTime = rs.getTimestamp("RSV_END_TIME");
				rsvInfo.add(new ReservationInfo(customerId, fdCustomerId, dayOfWeek,
						startTime, endTime, getAddressFromResultSet(rs)));
			}
			rs.close();
			ps.close();
			return rsvInfo;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	private ContactAddressModel getAddressFromResultSet(ResultSet rs)
			throws SQLException {
		ContactAddressModel address = new ContactAddressModel();
		address.setPK(new PrimaryKey(rs.getString("ADDRESS_ID")));
		address.setAddress1(rs.getString("ADDRESS1"));
		address.setAddress2(rs.getString("ADDRESS2"));
		address.setApartment(rs.getString("APARTMENT"));
		address.setCity(rs.getString("CITY"));
		address.setState(rs.getString("STATE"));
		address.setZipCode(rs.getString("ZIP"));
		address.setServiceType(EnumServiceType.getEnum(rs
				.getString("SERVICE_TYPE")));

		AddressInfo info = new AddressInfo();
		info.setScrubbedStreet(rs.getString("SCRUBBED_ADDRESS"));
		info.setLatitude(Double.parseDouble((rs.getBigDecimal("LATITUDE")!=null)?rs.getBigDecimal("LATITUDE").toString():"0"));
		info.setLongitude(Double.parseDouble((rs.getBigDecimal("LONGITUDE")!=null)?rs.getBigDecimal("LONGITUDE").toString():"0"));
		address.setAddressInfo(info);
		address.setFirstName(rs.getString("FIRST_NAME"));
		address.setLastName(rs.getString("LAST_NAME"));
		address.setCustomerId(rs.getString("CUSTOMER_ID"));
		return address;
	}

	/**
	 * Template method that returns the cache key to use for caching resources.
	 * 
	 * @return the bean's home interface name
	 */
	protected String getResourceCacheKey() {
		return "com.freshdirect.fdstore.customer.ejb.FDCustomerManagerHome";
	}

	public void createCase(CrmSystemCaseInfo caseInfo)
			throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			sb.createCase(caseInfo, false);
		} catch (RemoteException e) {
			throw new FDResourceException(e);
		} catch (CreateException e) {
			throw new FDResourceException(e);
		}
	}

	public FDCustomerCreditHistoryModel getCreditHistory(FDIdentity identity)
			throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = this.getConnection();
			ps = conn
					.prepareStatement("SELECT ct.sale_id, s.type, ct.id AS complaint_id, cc.amount, SUM(cl.amount) AS original_amount, cl.method, cd.name AS department, "
							+ "cc.create_date, ct.STATUS, ct.CREATED_BY, ct.APPROVED_BY "
							+ "FROM (select SUM(amount) as amount, complaint_id, create_date from cust.customercredit where customer_id= ? GROUP BY complaint_id, create_date) cc, CUST.COMPLAINT ct, "
							+ "CUST.COMPLAINTLINE cl, CUST.COMPLAINT_DEPT_CODE cdc, CUST.COMPLAINT_DEPT cd, CUST.SALE s "
							+ "WHERE cc.COMPLAINT_ID = ct.id AND ct.id=cl.complaint_id "
							+ "AND cl.complaint_dept_code_id = cdc.id "
							+ "AND cdc.comp_dept = cd.code and ct.sale_id = s.id "
							+ "and cl.method = 'FDC' and ct.status='APP' "
							+ "GROUP BY ct.sale_id, s.type, ct.id, cc.amount, cl.method, cd.name, cc.create_date, ct.status, ct.created_by, ct.approved_by "
							+ "UNION "
							+ "SELECT ct.sale_id, sa.type, ct.id AS complaint_id, 0 as amount, cl.amount as ORIGINAL_AMOUNT, cl.method, cd.name AS department, ct.create_date, "
							+ "ct.status, ct.created_by, ct.approved_by "
							+ "FROM CUST.COMPLAINT ct, "
							+ "CUST.COMPLAINTLINE cl, CUST.SALE sa, "
							+ "CUST.COMPLAINT_DEPT_CODE cdc, CUST.COMPLAINT_DEPT cd "
							+ "WHERE sa.customer_id = ? "
							+ "AND sa.id = ct.sale_id AND ct.id = cl.complaint_id AND cl.complaint_dept_code_id = cdc.id "
							+ "and cl.method = 'CSH' and ct.status='APP' "
							+ "AND cdc.comp_dept = cd.code "
							+ "GROUP BY ct.sale_id, sa.type, ct.id, cl.amount, cl.method, ct.create_date, ct.status, cd.name, ct.created_by, ct.approved_by "
							+ "ORDER BY create_date DESC, complaint_id, department, method");
			ps.setString(1, identity.getErpCustomerPK());
			ps.setString(2, identity.getErpCustomerPK());
			rs = ps.executeQuery();

			List<FDCustomerCreditModel> lst = new ArrayList<FDCustomerCreditModel>();
			FDCustomerCreditModel prevCredit = null;

			while (rs.next()) {

				FDCustomerCreditModel credit = new FDCustomerCreditModel();

				// Block to handle a single complaint across multiple
				// departments
				if (prevCredit != null
						&& rs.getString("COMPLAINT_ID").equals(
								prevCredit.getComplaintPk())
						&& prevCredit.getMethod().getStatusCode().equals(
								rs.getString("METHOD"))) {
					if (!prevCredit.getDepartment().equals(
							rs.getString("DEPARTMENT"))
							&& prevCredit.getDepartment().indexOf(
									rs.getString("DEPARTMENT")) < 0) {
						prevCredit.setDepartment(prevCredit.getDepartment()
								+ ", " + rs.getString("DEPARTMENT"));
					}
					prevCredit.setOriginalAmount(prevCredit.getOriginalAmount()
							+ rs.getDouble("ORIGINAL_AMOUNT"));
					continue;
				}

				credit.setSaleId(rs.getString("SALE_ID"));
				credit.setComplaintPk(new PrimaryKey(rs
						.getString("COMPLAINT_ID")));
				credit.setRemainingAmount(rs.getDouble("AMOUNT"));
				credit.setOriginalAmount(rs.getDouble("ORIGINAL_AMOUNT"));
				credit.setMethod(EnumComplaintLineMethod
						.getComplaintLineMethod(rs.getString("METHOD")));
				credit.setDepartment(rs.getString("DEPARTMENT"));
				credit.setCreateDate(rs.getTimestamp("CREATE_DATE"));
				credit.setStatus(EnumComplaintStatus.getComplaintStatus(rs
						.getString("STATUS")));
				credit.setIssuedBy(rs.getString("CREATED_BY"));
				credit.setApprovedBy(rs.getString("APPROVED_BY"));
				credit.setOrderType(rs.getString("TYPE"));
				
				if("Referral".equals(rs.getString("DEPARTMENT"))) {
					//Get the order number that helped to earn this credit
					credit.setRefSaleId(getReferralSaleID(identity.getErpCustomerPK(), rs.getString("COMPLAINT_ID")));
				}

				lst.add(credit);
				prevCredit = credit;
			}
			FDCustomerCreditHistoryModel creditHistory = new FDCustomerCreditHistoryModel(
					identity, lst);
			return creditHistory;

		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				LOGGER.warn("error while cleanup", e);
			}
			close(conn);

		}
	}

	private String getReferralSaleID(String customerId, String complaintId) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = this.getConnection();
			ps = conn.prepareStatement("select ci.credit_sale_id from cust.customer_invites ci where CI.REFERRAL_CUSTOMER_ID = ?  and CI.COMPLAINT_ID = ?");
			ps.setString(1, customerId);
			ps.setString(2, complaintId);
			rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getString(1);
			}
				
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {				
			}
			close(conn);
		}
		return "";
	}

	public void storeCustomerRequest(FDCustomerRequest cr)
			throws FDResourceException, RemoteException {
		Connection conn = null;

		try {
			conn = this.getConnection();
			String id = this.getNextId(conn, "CUST");
			FDCustomerRequestDAO.storeCustomerRequest(conn, id, cr);
		} catch (Exception e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	private void logActivity(ErpActivityRecord record) {
		new ErpLogActivityCommand(LOCATOR, record).execute();
	}

	public String getNextId(String schema, String sequence)
			throws FDResourceException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return SequenceGenerator.getNextId(conn, schema, sequence);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public Map<String, ProfileAttributeName> loadProfileAttributeNames() throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement("SELECT * FROM CUST.PROFILE_ATTR_NAME");
			rs = ps.executeQuery();
			Map<String, ProfileAttributeName> profileNames = new HashMap<String, ProfileAttributeName>();

			while (rs.next()) {
				ProfileAttributeName profileName = new ProfileAttributeName();
				profileName.setName(rs.getString("NAME"));
				profileName.setDescription(rs.getString("DESCRIPTION"));
				profileName.setCategory(rs.getString("CATEGORY"));
				profileName.setAttributeValueType(rs.getString("ATTR_VALUE_TYPE"));
				profileName.setIsEditable("X".equalsIgnoreCase(rs.getString("IS_EDITABLE")));
				profileNames.put(profileName.getName(), profileName);
			}

			return profileNames;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				LOGGER.warn("SQLException while cleaning: ", e);
			}
			close(conn);
		}
	}

	public List<String> loadProfileAttributeNameCategories() throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement("SELECT DISTINCT CATEGORY FROM CUST.PROFILE_ATTR_NAME WHERE IS_EDITABLE = 'X'");
			rs = ps.executeQuery();
			List<String> lst = new ArrayList<String>();

			while (rs.next()) {
				lst.add(rs.getString("CATEGORY"));
			}
			
			Collections.sort(lst);
			
			return lst;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				LOGGER.warn("SQLException while cleaning: ", e);
			}
			close(conn);
		}
	}

	/**
	 * Set this customer as alert on/alert off.
	 * 
	 * @param PrimaryKey
	 *            customer indentifier
	 * @param boolean indicates active/deactivated status
	 */

	public void setAlert(FDActionInfo info,
			ErpCustomerAlertModel customerAlert, boolean isOnAlert) {
		try {
			ErpCustomerManagerSB sb = (ErpCustomerManagerSB) this
					.getErpCustomerManagerHome().create();
			if (sb.setAlert(new PrimaryKey(info.getIdentity()
					.getErpCustomerPK()), customerAlert, isOnAlert)) {
				this
						.logActivity(info
								.createActivity(isOnAlert ? EnumAccountActivityType.PLACE_ALERT
										: EnumAccountActivityType.REMOVE_ALERT));
			}
		} catch (RemoteException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		} catch (CreateException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		}
	}

	public List<ErpCustomerAlertModel> getAlerts(PrimaryKey pk) {
		try {
			ErpCustomerManagerSB sb = (ErpCustomerManagerSB) this.getErpCustomerManagerHome().create();
			return sb.getAlerts(pk);
		} catch (RemoteException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		} catch (CreateException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		}
	}

	public boolean isOnAlert(PrimaryKey pk, String alertType) {
		try {
			ErpCustomerManagerSB sb = (ErpCustomerManagerSB) this.getErpCustomerManagerHome().create();
			return sb.isOnAlert(pk, alertType);
		} catch (RemoteException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		} catch (CreateException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		}
	}

	public boolean isOnAlert(PrimaryKey pk) {
		try {
			ErpCustomerManagerSB sb = (ErpCustomerManagerSB) this.getErpCustomerManagerHome().create();
			return sb.isOnAlert(pk);
		} catch (RemoteException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		} catch (CreateException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		}
	}

	public boolean isCustomerActive(PrimaryKey pk) {
		try {
			ErpCustomerManagerSB sb = (ErpCustomerManagerSB) this
					.getErpCustomerManagerHome().create();
			return sb.isCustomerActive(pk);
		} catch (RemoteException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		} catch (CreateException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		}
	}

	public boolean isECheckRestricted(FDIdentity identity)
			throws FDResourceException {
		try {
			ErpCustomerEB eb = getErpCustomerHome().findByPrimaryKey(
					new PrimaryKey(identity.getErpCustomerPK()));
			// if customer has any restricted payment methods --> disable
			// eChecks
			List<ErpPaymentMethodI> paymentMethodList = eb.getPaymentMethods();
			if (paymentMethodList != null && paymentMethodList.size() > 0) {
				for (ErpPaymentMethodI paymentMethod : paymentMethodList) {
					if (PaymentFraudManager.checkBadAccount(paymentMethod,
							false)) {
						return true;
					}
				}
			}
			// if customer is on alert --> disable eChecks
			if (isOnAlert(new PrimaryKey(identity.getErpCustomerPK()),
					EnumAlertType.ECHECK.getName())) {
				return true;
			}
			return false;
		} catch (Exception e) {
			throw new FDResourceException(e);
		}
	}

	public boolean isReferrerRestricted(FDIdentity identity)
			throws FDResourceException {
		return isOnAlert(new PrimaryKey(identity.getErpCustomerPK()),
				EnumAlertType.REFERRER.getName());
	}

	public List<URLRewriteRule> loadRewriteRules() throws FDResourceException {
		Connection conn = null;

		try {
			conn = this.getConnection();
			PreparedStatement ps = conn
					.prepareStatement("SELECT ID, NAME, DISABLED, FROM_URL, REDIRECT, COMMENTS, OPTIONS, PRIORITY FROM CUST.URL_REWRITE_RULES ORDER BY PRIORITY");
			ResultSet rs = ps.executeQuery();
			List<URLRewriteRule> lst = new ArrayList<URLRewriteRule>();
			while (rs.next()) {
				URLRewriteRule rule = new URLRewriteRule();
				rule.setPK(new PrimaryKey(rs.getString("ID")));
				rule.setName(rs.getString("NAME"));
				rule.setDisabled("X".equals(rs.getString("DISABLED")));
				rule
						.setOptions(this.getRewriteOptions(rs
								.getString("OPTIONS")));
				rule.setFrom(rs.getString("FROM_URL"));
				rule.setRedirect(rs.getString("REDIRECT"));
				rule.setComments(rs.getString("COMMENTS"));
				rule.setPriority(rs.getInt("PRIORITY"));

				if (rule.isValid()) {
					lst.add(rule);
				}
			}

			rs.close();
			ps.close();

			return lst;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	private List<String> getRewriteOptions(String options) {
		if (options == null || "".equals(options.trim())) {
			return Collections.<String> emptyList();
		}
		String[] tokens = options.split("\\,", -2);
		List<String> lst = new ArrayList<String>();
		for (int i = 0; i < tokens.length; i++) {
			lst.add(tokens[i].trim());
		}

		return lst;
	}

	public static class ReservationInfo implements Serializable {

		private static final long serialVersionUID = 5124744242444131295L;

		private final String customerId;
		
		private final String fdCustomerId;

		private final int dayOfWeek;

		private final Date startTime;

		private final Date endTime;

		private final ContactAddressModel address;

		public ReservationInfo(String customerId, String fdCustomerId, int dayOfWeek,
				Date startTime, Date endTime, ContactAddressModel address) {
			this.customerId = customerId;
			this.fdCustomerId = fdCustomerId;
			this.dayOfWeek = dayOfWeek;
			this.startTime = startTime;
			this.endTime = endTime;
			this.address = address;
		}

		public ContactAddressModel getAddress() {
			return address;
		}

		public String getCustomerId() {
			return customerId;
		}

		public int getDayOfWeek() {
			return dayOfWeek;
		}

		public Date getEndTime() {
			return endTime;
		}

		public Date getStartTime() {
			return startTime;
		}

		public String getFdCustomerId() {
			return fdCustomerId;
		}
		
	}

	/**
	 * This method returns the list of delivery passes that are linked to this
	 * customer's account.
	 * 
	 * @param customerId
	 * @return java.util.List
	 */
	public List<DeliveryPassModel> getDeliveryPasses(FDIdentity identity) {
		List<DeliveryPassModel> deliveryPasses = null;
		try {
			DlvPassManagerSB sb = (DlvPassManagerSB) this
					.getDlvPassManagerHome().create();
			deliveryPasses = sb.getDeliveryPasses(identity.getErpCustomerPK());
		} catch (RemoteException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		} catch (CreateException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		}
		return deliveryPasses;
	}

	/**
	 * This method returns Map containing dlvPassId as key and DlvPassUsageInfo
	 * as value.
	 * 
	 * @param identity
	 * @return
	 */
	public Map<String, DlvPassUsageInfo> getDlvPassesUsageInfo(FDIdentity identity) {
		Map<String, DlvPassUsageInfo> mapInfo = null;
		try {
			ErpCustomerManagerSB customerManagerSB = this.getErpCustomerManagerHome().create();
			mapInfo = customerManagerSB.getDlvPassesUsageInfo(identity.getErpCustomerPK());
		} catch (RemoteException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		} catch (CreateException ce) {
			LOGGER.warn(ce);
			throw new EJBException(ce);

		}
		return mapInfo;

	}

	/**
	 * This method returns the list of orders that used the specified delivery
	 * pass.
	 * 
	 * @param dlvPassId
	 * @return java.util.List
	 */
	public ErpOrderHistory getOrdersByDlvPassId(FDIdentity identity,
			String dlvPassId) {
		ErpOrderHistory orders = null;
		try {
			ErpCustomerManagerSB customerManagerSB = this
					.getErpCustomerManagerHome().create();
			orders = customerManagerSB.getOrdersByDlvPassId(identity
					.getErpCustomerPK(), dlvPassId);
		} catch (RemoteException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		} catch (CreateException ce) {
			LOGGER.warn(ce);
			throw new EJBException(ce);

		}
		return orders;
	}

	public List<DlvPassUsageLine> getRecentOrdersByDlvPassId(FDIdentity identity,
			String dlvPassId, int noOfDaysOld) throws FDResourceException {
		try {
			ErpCustomerManagerSB erpCustomerManagerSB = this.getErpCustomerManagerHome().create();
			return erpCustomerManagerSB.getRecentOrdersByDlvPassId(identity.getErpCustomerPK(), dlvPassId, noOfDaysOld);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	/**
	 * This method returns the one or more delivery passes that are linked to
	 * this customer's account for the specified delivery pass status.
	 * 
	 * @param customerId
	 * @return java.util.List
	 */
	public List<DeliveryPassModel> getDeliveryPassesByStatus(
			FDIdentity identity, EnumDlvPassStatus status) {
		List<DeliveryPassModel> deliveryPasses = null;
		try {
			DlvPassManagerSB sb = (DlvPassManagerSB) this
					.getDlvPassManagerHome().create();
			deliveryPasses = sb.getDlvPassesByStatus(identity
					.getErpCustomerPK(), status);
		} catch (RemoteException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		} catch (CreateException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		}
		return deliveryPasses;
	}

	public Map<String, List<FDCustomerOrderInfo>> cancelOrders(
			FDActionInfo actionInfo, List<FDCustomerOrderInfo> customerOrders,
			boolean sendEmail) {

		List<FDCustomerOrderInfo> successOrders = new ArrayList<FDCustomerOrderInfo>();
		List<FDCustomerOrderInfo> failureOrders = new ArrayList<FDCustomerOrderInfo>();
		String saleId = null;
		for (FDCustomerOrderInfo orderInfo : customerOrders) {
			try {
				FDIdentity identity = orderInfo.getIdentity();
				// Set it to actionInfo object to write to the activity log.
				actionInfo.setIdentity(identity);
				saleId = orderInfo.getSaleId();
				FDOrderI order = getOrder(saleId);
				this.cancelOrder(actionInfo, saleId, sendEmail, 0, false);
				successOrders.add(orderInfo);
				ErpActivityRecord rec = actionInfo.createActivity(EnumAccountActivityType.CANCEL_ORDER);
				rec.setNote("Order Cancelled (w/ " + customerOrders.size() + " others)");
				rec.setChangeOrderId(saleId);
				rec.setStandingOrderId(order.getStandingOrderId());
				this.logActivity(rec);
			} catch (FDResourceException fe) {
				LOGGER
						.error("System Error occurred while processing Sale ID : "
								+ saleId + "\n" + fe.getMessage());
				failureOrders.add(orderInfo);
			} catch (ErpTransactionException te) {
				LOGGER
						.error("Transaction Error occurred while processing Sale ID : "
								+ saleId + "\n" + te.getMessage());
				failureOrders.add(orderInfo);
			} catch (DeliveryPassException de) {
				LOGGER
						.error("Delivery Pass Error occurred while processing Sale ID : "
								+ saleId + "\n" + de.getMessage());
				failureOrders.add(orderInfo);
			}
		}
		Map<String, List<FDCustomerOrderInfo>> results = new HashMap<String, List<FDCustomerOrderInfo>>();
		results.put("SUCCESS_ORDERS", successOrders);
		results.put("FAILURE_ORDERS", failureOrders);
		return results;
	}

	public void storeRetentionSurvey(FDIdentity fdIdentity, String profileAttr,
			String profileValue, CrmSystemCaseInfo caseInfo)
			throws RemoteException, FDResourceException {

		setProfileAttribute(fdIdentity, profileAttr, profileValue, null);
		if (caseInfo != null) {
			createCase(caseInfo);
		}
	}

	/**
	 * SmartStore
	 * 
	 * @param saleId
	 *            order ID
	 * @param FDIdentity
	 *            Customer identity
	 * @param feature
	 *            Site Feature (eg. 'DYF')
	 * @param variantId
	 *            Variant ID
	 * @throws FDResourceException
	 */
	public void logCustomerVariant(String saleId, FDIdentity identity,
			String feature, String variantId) throws RemoteException,
			FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			ps = conn
					.prepareStatement("INSERT INTO CUST.LOG_CUSTOMER_VARIANTS(TIMESTAMP, CUSTOMER_ID, SALE_ID, VARIANT_ID, FEATURE) "
							+ "VALUES(SYSDATE,?,?,?,?)");
			ps.setString(1, identity.getErpCustomerPK());
			ps.setString(2, saleId);
			ps.setString(3, variantId);
			ps.setString(4, feature);

			rs = ps.executeQuery();
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				LOGGER.warn("SQLException occured: ", e);
			}
			close(conn);
		}
	}

	private DeliveryPassModel getActiveDPForCustomer(String customerPK,
			DlvPassManagerSB dlvPassSB) throws FDResourceException,
			RemoteException {

		List<DeliveryPassModel> dlvPasses = dlvPassSB.getDlvPassesByStatus(
				customerPK, EnumDlvPassStatus.ACTIVE);
		if (dlvPasses == null || dlvPasses.size() == 0) {
			throw new FDResourceException(
					"Unable to locate the Active DeliveryPass for this customer.");
		}
		return dlvPasses.get(0);
	}

	private void extendDeliveryPass(DlvPassManagerSB dlvpsb,
			DeliveryPassModel dlvPass, int numOfWeeks, String saleID,
			String note, String reason) throws RemoteException {

		EnumAccountActivityType action = null;
		if (numOfWeeks < 0) {
			action = EnumAccountActivityType.REDUCE_DLV_PASS;
			/**
			 * Under this scenario, you shouldn't reduce the expiration period.
			 * 1) The purchased pass might have been pending during the order
			 * being made(with delivery promo) and "active" when you cancel the
			 * order. Since you've not given an extension for the pass, dont
			 * reduce. 2) The pass purchased is pending, Delivery promo applied
			 * on subsequent order before the pass being activated. If that
			 * order(using delivery promo) is cancelled after the DP is active,
			 * dont curtail his expiration date.
			 * 
			 */
			if ((dlvPass.getOrgExpirationDate() != null)
					&& (dlvPass.getOrgExpirationDate().compareTo(
							dlvPass.getExpirationDate()) == 0)) {
				return;
			}
		} else if (numOfWeeks > 0) {
			action = EnumAccountActivityType.EXTEND_DLV_PASS;
		} else {
			return;
		}
		dlvpsb.extendExpirationPeriod(dlvPass, numOfWeeks * 7);

		// Create a activity log to track the delivery credits.
		ErpActivityRecord activityRecord = createActivity(action, note,
				dlvPass, saleID, reason);
		logActivity(activityRecord);

	}
	
	private void extendDeliveryPassByDays(DlvPassManagerSB dlvpsb,
			DeliveryPassModel dlvPass, int noOfDays, String saleID,
			String note, String reason) throws RemoteException {

		EnumAccountActivityType action = null;
		if (noOfDays < 0) {
			action = EnumAccountActivityType.REDUCE_DLV_PASS;
			/**
			 * Under this scenario, you shouldn't reduce the expiration period.
			 * 1) The purchased pass might have been pending during the order
			 * being made(with delivery promo) and "active" when you cancel the
			 * order. Since you've not given an extension for the pass, dont
			 * reduce. 2) The pass purchased is pending, Delivery promo applied
			 * on subsequent order before the pass being activated. If that
			 * order(using delivery promo) is cancelled after the DP is active,
			 * dont curtail his expiration date.
			 * 
			 */
			if ((dlvPass.getOrgExpirationDate() != null)
					&& (dlvPass.getOrgExpirationDate().compareTo(
							dlvPass.getExpirationDate()) == 0)) {
				return;
			}
		} else if (noOfDays > 0) {
			action = EnumAccountActivityType.EXTEND_DLV_PASS;
		} else {
			return;
		}
		dlvpsb.extendExpirationPeriod(dlvPass, noOfDays);

		// Create a activity log to track the delivery credits.
		ErpActivityRecord activityRecord = createActivity(action, note,
				dlvPass, saleID, reason);
		logActivity(activityRecord);

	}

	private ErpActivityRecord createActivity(EnumAccountActivityType type,
			String note, DeliveryPassModel dlvPass, String saleId,
			String reasonCode) {

		ErpActivityRecord rec = new ErpActivityRecord();
		rec.setActivityType(type);
		rec.setSource(EnumTransactionSource.SYSTEM);
		rec.setInitiator("SYSTEM");
		rec.setCustomerId(dlvPass.getCustomerId());
		StringBuffer sb = new StringBuffer();
		if (note != null) {
			sb.append(note);
		}
		rec.setNote(sb.toString());
		rec.setDeliveryPassId(dlvPass.getPK().getId());
		rec.setChangeOrderId(saleId);
		rec.setReason(reasonCode);
		return rec;
	}

	public boolean hasPurchasedPass(String customerPK)
			throws FDResourceException {

		boolean hasPurchased = false;
		try {
			DlvPassManagerSB sb = (DlvPassManagerSB) this
					.getDlvPassManagerHome().create();
			hasPurchased = sb.hasPurchasedPass(customerPK);
		} catch (CreateException ex) {
			LOGGER.warn(ex);
			throw new EJBException(ex);
		} catch (RemoteException e) {
			throw new FDResourceException(e);
		}

		return hasPurchased;
	}

	public int getValidOrderCount(FDIdentity identity)
			throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = (ErpCustomerManagerSB) this
					.getErpCustomerManagerHome().create();
			return sb.getValidOrderCount(new PrimaryKey(identity
					.getErpCustomerPK()));

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public String getLastOrderID(FDIdentity identity)
			throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = (ErpCustomerManagerSB) this
					.getErpCustomerManagerHome().create();
			return sb
					.getLastOrderID(new PrimaryKey(identity.getErpCustomerPK()));

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public String hasAutoRenewDP(String customerPK) throws FDResourceException {

		ErpCustomerEB eb;
		try {
			eb = this.getErpCustomerHome().findByPrimaryKey(
					new PrimaryKey(customerPK));
			ErpCustomerInfoModel info = eb.getCustomerInfo();
			return info.getHasAutoRenewDP();
		} catch (RemoteException e) {
			throw new FDResourceException(e);
		} catch (FinderException e) {
			throw new FDResourceException(e);
		}

	}

	public void setHasAutoRenewDP(String customerPK,
			EnumTransactionSource source, String initiator, boolean autoRenew)
			throws FDResourceException {
		// public void flipAutoRenewDP(String customerPK)throws
		// FDResourceException {
		ErpCustomerEB eb;
		try {
			eb = this.getErpCustomerHome().findByPrimaryKey(
					new PrimaryKey(customerPK));
			ErpCustomerInfoModel info = eb.getCustomerInfo();
			String value = info.getHasAutoRenewDP();
			if (value != null && !value.equals("")) {
				ErpActivityRecord rec = new ErpActivityRecord();
				if (autoRenew) {
					info.setHasAutoRenewDP("Y");
					rec
							.setActivityType(EnumAccountActivityType.AUTORENEW_DP_FLAG_ON);
				} else {
					info.setHasAutoRenewDP("N");
					rec
							.setActivityType(EnumAccountActivityType.AUTORENEW_DP_FLAG_OFF);
				}

				eb.setCustomerInfo(info);

				rec.setCustomerId(customerPK);
				rec.setSource(source);
				rec.setInitiator(initiator);
				logActivity(rec);

			}
		} catch (RemoteException e) {
			throw new FDResourceException(e);
		} catch (FinderException e) {
			throw new FDResourceException(e);
		}
	}

	public boolean isOrderBelongsToUser(FDIdentity identity, String saleId)
			throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			return sb.isOrderBelongsToUser(new PrimaryKey(identity
					.getErpCustomerPK()), saleId);

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public OrderHistoryI getWebOrderHistoryInfo(FDIdentity identity)
			throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			return sb.getWebOrderHistoryInfo(new PrimaryKey(identity
					.getErpCustomerPK()));

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public FDOrderI getLastNonCOSOrderUsingCC(String customerID,
			EnumSaleType saleType, EnumSaleStatus saleStatus)
			throws FDResourceException, ErpSaleNotFoundException {

		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			ErpSaleModel saleModel = sb.getLastNonCOSOrder(customerID,
					saleType, saleStatus, EnumPaymentMethodType.CREDITCARD);
			return new FDOrderAdapter(saleModel);

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	/**
	 * Place an order (send msg to SAP, persist order).
	 * 
	 * @param identity
	 *            the customer's identity reference
	 * @return String sale id
	 * @throws FDResourceException
	 *             if an error occured while accessing remote resources
	 */
	public String placeSubscriptionOrder(FDActionInfo info,
			ErpCreateOrderModel createOrder, Set<String> usedPromotionCodes,
			String rsvId, boolean sendEmail, CustomerRatingI cra,
			CrmAgentRole agentRole, EnumDlvPassStatus status)
			throws FDResourceException, ErpFraudException,
			DeliveryPassException {

		FDIdentity identity = info.getIdentity();
		PrimaryKey pk = null;
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			String customerPk = identity.getErpCustomerPK();
			pk = sb.placeOrder(new PrimaryKey(customerPk), createOrder,
					usedPromotionCodes, cra, agentRole, null,
					EnumSaleType.SUBSCRIPTION);
			LOGGER.debug("In Place order getDeliveryPassCount "
					+ createOrder.getDeliveryPassCount());
			if (createOrder.getDeliveryPassCount() > 0) {
				// order contains delivery pass.
				DeliveryPassModel newPass = DeliveryPassUtil
						.constructDeliveryPassFromOrder(customerPk, pk.getId(),
								createOrder);
				DlvPassManagerSB dlvpsb = this.getDlvPassManagerHome().create();
				String dlvPassId = dlvpsb.create(newPass);
				newPass.setPK(new PrimaryKey(dlvPassId));
			}
			ErpActivityRecord rec = info.createActivity(EnumAccountActivityType.PLACE_SUBS_ORDER);
			rec.setChangeOrderId(pk.getId());
			this.logActivity(rec);
			if (sendEmail) {
				FDOrderI order = getOrder(pk.getId());
				FDCustomerInfo fdInfo = this.getCustomerInfo(identity);

				int orderCount = getValidOrderCount(identity);
				fdInfo.setNumberOfOrders(orderCount);

				this.doEmail(FDEmailFactory.getInstance().createConfirmOrderEmail(fdInfo, order));
			}

			return pk.getId();

		} catch (DeliveryPassException de) {
			LOGGER.warn("Error placing the order.", de);
			throw de;
		} catch (CreateException ce) {
			LOGGER.warn("Cannot Create ErpCustomerManagerSessionBean", ce);
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	/**
	 * Place an order (send msg to SAP, persist order).
	 * 
	 * @param identity
	 *            the customer's identity reference
	 * @return String sale id
	 * @throws FDResourceException
	 *             if an error occured while accessing remote resources
	 * @throws ErpAuthorizationException
	 */
	public String placeGiftCardOrder(FDActionInfo info,
			ErpCreateOrderModel createOrder, Set<String> usedPromotionCodes,
			String rsvId, boolean sendEmail, CustomerRatingI cra,
			CrmAgentRole agentRole, EnumDlvPassStatus status,
			boolean isBulkOrder) throws ServiceUnavailableException,
			FDResourceException, ErpFraudException, ErpAuthorizationException,
			ErpAddressVerificationException {

		FDIdentity identity = info.getIdentity();
		PrimaryKey pk = null;
		try {
			if (FDStoreProperties.isGivexBlackHoleEnabled()
					&& createOrder.getSubTotal() <= 0.0) {
				// THis is $0 card with no value created for balance transfer.
				// At this point reject the
				// user action since register transaction will not go through.
				throw new ServiceUnavailableException(
						"This service is unavailable at this time. Please try again later.");
			}
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			String customerPk = identity.getErpCustomerPK();
			pk = sb.placeOrder(new PrimaryKey(customerPk), createOrder,
					usedPromotionCodes, cra, agentRole, null,
					EnumSaleType.GIFTCARD);
			// ErpSaleEB eb = this.getErpSaleHome().findByPrimaryKey(new
			// PrimaryKey(pk.getId()));
			// store giftcard recipent record
			// storeCustomerRecipents(recipentList,
			// eb.getCurrentOrder().getId(), customerPk);
			ErpActivityRecord rec = info.createActivity(EnumAccountActivityType.PLACE_GC_ORDER);
			rec.setChangeOrderId(pk.getId());
			this.logActivity(rec);
			FDOrderI order = getOrder(pk.getId());
			if (sendEmail) {
				
				FDCustomerInfo fdInfo = this.getCustomerInfo(identity);

				int orderCount = getValidOrderCount(identity);
				fdInfo.setNumberOfOrders(orderCount);
				/*
				 * boolean isBulkOrder = false; FDUser fdUser =
				 * recognize(identity); FDBulkRecipientList bulkList =
				 * fdUser.getBulkRecipentList(); if(null!= bulkList && null
				 * !=bulkList.getRecipents()&&
				 * !bulkList.getRecipents().isEmpty()){ isBulkOrder = true; }
				 * if(isBulkOrder){
				 * this.doEmail(FDGiftCardEmailFactory.getInstance
				 * ().createGiftCardBulkOrderConfirmationEmail(fdInfo, order,
				 * bulkList)); }else{
				 */
				this.doEmail(FDGiftCardEmailFactory.getInstance()
						.createGiftCardOrderConfirmationEmail(fdInfo, order,
								isBulkOrder));
				// }

			}

			// AUTH sale in CYBER SOURCE
			PaymentManagerSB paymentManager = this.getPaymentManagerHome()
					.create();
			List auths = paymentManager.authorizeSaleRealtime(pk.getId(),
					EnumSaleType.GIFTCARD);
			if (order.getTotal()<=0 ||(auths != null && auths.size() > 0)) {
				// Only when it has a valid auth.
				ErpCustomerManagerSB erpCMsb = this.getErpCustomerManagerHome().create();
				String sapCustomerId = erpCMsb.getSapCustomerId(customerPk);
				// Only if the customer id is available in SAP.
				if (null != sapCustomerId && sapCustomerId.length() > 0) {
					erpCMsb.sendCreateOrderToSAP(customerPk, pk.getId(),
							EnumSaleType.GIFTCARD, cra);
				}
			}

			return pk.getId();

		} catch (ErpSaleNotFoundException se) {
			LOGGER.warn("Unable to locate Order ", se);
			throw new FDResourceException(se);
		} catch (CreateException ce) {
			LOGGER.warn("Cannot Create ErpCustomerManagerSessionBean", ce);
			throw new FDResourceException(ce);
		}
		// catch (FinderException re) {
		// throw new FDResourceException(re);
		// }
		catch (RemoteException re) {
			Exception ex = (Exception) re.getCause();
			if (ex instanceof ErpAddressVerificationException)
				throw (ErpAddressVerificationException) ex;

			throw new FDResourceException(re);
		}/*
		 * catch(FDAuthenticationException fdae){ throw new
		 * FDResourceException(fdae); }
		 */
	}

	
	public void addAndReconcileInvoice(String saleId, ErpInvoiceModel invoice, ErpShippingInfo shippingInfo) throws ErpTransactionException {
	
	    try {
	        this.getErpCustomerManagerHome().create().addAndReconcileInvoice(saleId, invoice, shippingInfo);
	    } catch (ErpTransactionException e) {
	        throw e;
	    } catch (Exception e) {
	        LOGGER.warn("Unexpected Exception while trying to process invoice for order#: " + saleId, e);
	        throw new EJBException("Unexpected Exception while trying to process invoice for order#: " + saleId, e);
	    }
	}

	public void authorizeSale(String erpCustomerID, String saleID,
			EnumSaleType type, CustomerRatingI cra) throws FDResourceException,
			ErpSaleNotFoundException {

		if (EnumSaleType.REGULAR.equals(type)) {
			authorizeSale(saleID);
			return;
		} else if (EnumSaleType.SUBSCRIPTION.equals(type)) {
			try {
				PaymentManagerSB sb = this.getPaymentManagerHome().create();
				EnumPaymentResponse response = sb.authorizeSale(saleID,false);

				if (!EnumPaymentResponse.APPROVED.equals(response)
						&& !EnumPaymentResponse.ERROR.equals(response)) {
					sendARAuthFailedEmail(saleID);// Should we send email?

				} else if (EnumPaymentResponse.APPROVED.equals(response)) {
					// what should be done in case of Error response??.TBD.
					ErpCustomerManagerSB erpCMsb = this.getErpCustomerManagerHome().create();
					erpCMsb.sendCreateOrderToSAP(erpCustomerID, saleID, type,
							cra);
				}

			} catch (CreateException ce) {
				LOGGER.warn("Cannot Create ErpCustomerManagerSessionBean", ce);
				throw new FDResourceException(ce);
			} catch (RemoteException re) {
				throw new FDResourceException(re);
			}
		}
	}

	public Object[] getAutoRenewalInfo() throws FDResourceException {

		try {
			DlvPassManagerSB sb = this.getDlvPassManagerHome().create();
			return sb.getAutoRenewalInfo();

		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}

	}

	public String getAutoRenewSKU(String customerPK) throws FDResourceException {
		String arSKU = null;
		ErpCustomerEB eb;
		try {
			eb = this.getErpCustomerHome().findByPrimaryKey(
					new PrimaryKey(customerPK));
			ErpCustomerInfoModel info = eb.getCustomerInfo();
			arSKU = info.getAutoRenewDPSKU();
			return arSKU;

		} catch (RemoteException e) {
			throw new FDResourceException(e);
		} catch (FinderException e) {
			throw new FDResourceException(e);
		}
	}

	private ErpActivityRecord getReservationActivityLog(FDTimeslot timeslot,
			FDActionInfo aInfo, EnumAccountActivityType activityType,
			EnumReservationType rsvType) {
		ErpActivityRecord activityRecord = aInfo.createActivity(activityType);

		StringBuffer strBuf = new StringBuffer();
		if (timeslot != null) {
			strBuf.append(DateUtil.formatDay(timeslot.getDeliveryDate()));
			strBuf.append("  ");
			strBuf.append(DateUtil.formatDate(timeslot.getDeliveryDate()));
			strBuf.append(" ");
			strBuf.append(DateUtil.formatTime(timeslot.getStartDateTime()));
			strBuf.append("-");
			strBuf.append(DateUtil.formatTime(timeslot.getEndDateTime()));
		}

		if (rsvType != null) {
			strBuf.append(" ");
			strBuf.append(rsvType.getDescription());
		}
		activityRecord.setNote(strBuf.toString());

		return activityRecord;
	}

	public void storeProductRequest(List<FDProductRequest> productRequest,
			FDSurveyResponse survey) throws FDResourceException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			String id = "";
			for (int i = 0; i < productRequest.size(); i++) {
				id = this.getNextId(conn, "CUST");
				FDProductRequest prodReq = productRequest.get(i);
				prodReq.setId(id);
			}
			if (productRequest.size() > 0) {
				FDProductRequestDAO.storeRequest(conn, productRequest);
			}

			if (survey != null && !survey.getAnswers().isEmpty()) {
				LOCATOR.getSurveySessionBean().storeSurvey(survey);
			}

		} catch (SQLException se) {
			throw new FDResourceException(se, "Could not store product request");
		} catch (RemoteException e) {
			throw new FDResourceException(e, "Could not store product request");
		} finally {
			close(conn);
		}
	}

    public void storeProductRequest(List<FDProductRequest> productRequest) throws FDResourceException {
        Connection conn = null;
        try {
            conn = this.getConnection();
            String id = "";
            for (int i = 0; i < productRequest.size(); i++) {
                id = this.getNextId(conn, "CUST");
                FDProductRequest prodReq = productRequest.get(i);
                prodReq.setId(id);
            }
            if (productRequest.size() > 0) {
                FDProductRequestDAO.storeRequest(conn, productRequest);
            }
        } catch (SQLException se) {
            throw new FDResourceException(se, "Could not store product request");
        //} catch (RemoteException e) {
            //throw new FDResourceException(e, "Could not store product request");
        } finally {
            close(conn);
        }
    }

    public List<HashMap<String, String>> productRequestFetchAllDepts() throws FDResourceException {
        Connection conn = null;
        try {
            conn = this.getConnection();

            return FDProductRequestDAO.fetchAllDepts(conn);

        } catch (SQLException se) {
            throw new FDResourceException(se, "Could not fetch all Depts for product request");
        //} catch (RemoteException e) {
        //    throw new FDResourceException(e, "Could not fetch all Depts for product request");
        } finally {
            close(conn);
        }
    }

    public List<HashMap<String, String>> productRequestFetchAllCats() throws FDResourceException {
        Connection conn = null;
        try {
            conn = this.getConnection();

            return FDProductRequestDAO.fetchAllCats(conn);

        } catch (SQLException se) {
            throw new FDResourceException(se, "Could not fetch all Cats for product request");
        //} catch (RemoteException e) {
        //    throw new FDResourceException(e, "Could not fetch all Cats for product request");
        } finally {
            close(conn);
        }
    }

    public List<HashMap<String, String>> productRequestFetchAllMappings() throws FDResourceException {
        Connection conn = null;
        try {
            conn = this.getConnection();

            return FDProductRequestDAO.fetchAllMappings(conn);

        } catch (SQLException se) {
            throw new FDResourceException(se, "Could not fetch all Mappings for product request");
        //} catch (RemoteException e) {
            //throw new FDResourceException(e, "Could not fetch all Mappings for product request");
        } finally {
            close(conn);
        }
    }



	public void assignAutoCaseToComplaint(PrimaryKey complaintPk,
			PrimaryKey autoCasePK) throws FDResourceException {
		try {
			ErpCustomerManagerSB erpCustomerManagerSB = this
					.getErpCustomerManagerHome().create();
			erpCustomerManagerSB.assignAutoCaseToComplaint(complaintPk,
					autoCasePK);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public ErpGiftCardModel applyGiftCard(FDIdentity identity, String givexNum,
			FDActionInfo info) throws ServiceUnavailableException,
			InvalidCardException, CardInUseException, CardOnHoldException,
			FDResourceException {

		try {
			if (FDStoreProperties.isGivexBlackHoleEnabled()) {
				throw new ServiceUnavailableException(
						"This service is unavailable at this time. Please try again later.");
			}
			// Check if this gift card is already attached to a customer
			// account.
			GiftCardManagerSB sb = this.getGiftCardGManagerHome().create();
			ErpGiftCardModel giftCard = sb.validate(givexNum);
			// Clear Gift Card PK before adding to the customer Account.
			giftCard.setPK(null);
			ErpCustomerEB eb = getErpCustomerHome().findByPrimaryKey(
					new PrimaryKey(identity.getErpCustomerPK()));

			giftCard.setName(eb.getCustomerInfo().getFirstName() + " "
					+ eb.getCustomerInfo().getLastName());
			eb.addPaymentMethod(giftCard);
			giftCard = sb.validateAndGetGiftCardBalance(givexNum);
			// Log the activity
			this.logActivity(info
					.createActivity(EnumAccountActivityType.ADD_GIFT_CARD));
			return giftCard;
		} catch (InvalidCardException ie) {
			// ie.printStackTrace();
			// Log the activity
			ErpActivityRecord rec = info.createActivity(
					EnumAccountActivityType.GC_APPLY_FAILED,
					"Invalid Card number");
			rec.setReason(EnumGiftCardFailureType.INVALID_GIFT_CERTIFICATE
					.getName());
			this.logActivity(rec);
			throw ie;
		} catch (CardInUseException ce) {
			// ce.printStackTrace();
			// Log the activity
			ErpActivityRecord rec = info.createActivity(
					EnumAccountActivityType.GC_APPLY_FAILED, "Card In Use");
			rec.setReason(EnumGiftCardFailureType.CARD_IN_USE.getName());
			this.logActivity(rec);
			throw ce;
		} catch (FinderException fe) {
			// fe.printStackTrace();
			throw new FDResourceException(fe);
		} catch (RemoteException re) {
			// re.printStackTrace();
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			// ce.printStackTrace();
			throw new FDResourceException(ce);
		}
	}

	public Collection<ErpGiftCardModel> getGiftCards(FDIdentity identity)
			throws FDResourceException {
		try {
			ErpCustomerEB erpCustomerEB = this.getErpCustomerHome()
					.findByPrimaryKey(
							new PrimaryKey(identity.getErpCustomerPK()));
			// return erpCustomerEB.getGiftCards();
			List<ErpGiftCardModel> giftCards = erpCustomerEB.getGiftCards();
			if (FDStoreProperties.isGivexBlackHoleEnabled()) {
				return giftCards;
			} else {
				return verifyStatusAndBalance(giftCards, true);
			}
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}

	public List<ErpGiftCardModel> verifyStatusAndBalance(
			List<ErpGiftCardModel> giftcards, boolean reloadBalance)
			throws FDResourceException {
		try {
			// Check if this gift card is already attached to a customer
			// account.
			GiftCardManagerSB sb = this.getGiftCardGManagerHome().create();
			return sb.verifyStatusAndBalance(giftcards, reloadBalance);
			// Log the activity
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}

	public ErpGiftCardModel verifyStatusAndBalance(ErpGiftCardModel giftcard,
			boolean reloadBalance) throws FDResourceException {
		try {

			// Check if this gift card is already attached to a customer
			// account.
			GiftCardManagerSB sb = this.getGiftCardGManagerHome().create();
			return sb.verifyStatusAndBalance(giftcard, reloadBalance);
			// Log the activity
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}

	public List getGiftCardRecepientsForCustomer(FDIdentity identity)
			throws FDResourceException {
		try {

			// Check if this gift card is already attached to a customer
			// account.
			GiftCardManagerSB sb = this.getGiftCardGManagerHome().create();
			return sb.getGiftCardRecepientsForCustomer(identity.getErpCustomerPK());
			// Log the activity
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}

	public Map getGiftCardRecepientsForOrders(List saleIds)
			throws FDResourceException {
		try {

			// Check if this gift card is already attached to a customer
			// account.
			GiftCardManagerSB sb = this.getGiftCardGManagerHome().create();
			return sb.getGiftCardRecepientsForOrders(saleIds);
			// Log the activity
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}

	public ErpGCDlvInformationHolder getRecipientDlvInfo(FDIdentity identity,
			String saleId, String certificationNum) throws FDResourceException {
		FDOrderI order = getOrder(identity, saleId);
		List gcDlvList = order.getGCDeliveryInfo().getDlvInfoTranactionList();
		for (Iterator it = gcDlvList.iterator(); it.hasNext();) {
			ErpGCDlvInformationHolder holder = (ErpGCDlvInformationHolder) it
					.next();
			if (holder.getCertificationNumber().equals(certificationNum)) {
				return holder;
			}
		}
		return null;
	}

	public boolean resendEmail(String saleId, String certificationNum,
			String resendEmailId, String recipName, String personMsg,
			EnumTransactionSource source) throws FDResourceException {
		ErpGCDlvInformationHolder holder = null;
		FDOrderI order = getOrder(saleId);
		List gcDlvList = order.getGCDeliveryInfo().getDlvInfoTranactionList();
		for (Iterator it = gcDlvList.iterator(); it.hasNext();) {
			holder = (ErpGCDlvInformationHolder) it.next();
			if (holder.getCertificationNumber().equals(certificationNum)) {
				break;
			}
		}
		if (holder == null)
			return false; // failure to resend
		try {
			if (holder != null) {
				ErpGCDlvInformationHolder newHolder = (ErpGCDlvInformationHolder) holder
						.deepCopy();
				newHolder.getRecepientModel().setRecipientEmail(resendEmailId);
				newHolder.getRecepientModel().setRecipientName(recipName);
				newHolder.getRecepientModel().setPersonalMessage(personMsg);
				newHolder.getRecepientModel().setDeliveryMode(
						EnumGCDeliveryMode.EMAIL);
				List<ErpGCDlvInformationHolder> recipientList = new ArrayList<ErpGCDlvInformationHolder>();
				recipientList.add(newHolder);
				GiftCardManagerSB sb = this.getGiftCardGManagerHome().create();
				sb.resendGiftCard(saleId, recipientList, source);
			}
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
		return true; // success
	}

	public boolean resendEmail(String saleId, String certificationNum,
			String resendEmailId, String recipName, String personMsg,
			boolean toPurchaser, boolean toLastRecipient,
			EnumTransactionSource source) throws FDResourceException {
		ErpGCDlvInformationHolder holder = null;
		FDOrderI order = getOrder(saleId);
		List gcDlvList = order.getGCDeliveryInfo().getDlvInfoTranactionList();
		for (Iterator it = gcDlvList.iterator(); it.hasNext();) {
			holder = (ErpGCDlvInformationHolder) it.next();
			if (holder.getCertificationNumber().equals(certificationNum)) {
				break;
			}
		}
		if (holder == null)
			return false; // failure to resend
		try {
			if (holder != null) {
				List<ErpGCDlvInformationHolder> recipientList = new ArrayList<ErpGCDlvInformationHolder>();
				if (toPurchaser) {
					String custId = order.getCustomerId();
					FDCustomerEB custEB = getFdCustomerHome()
							.findByErpCustomerId(custId);
					FDIdentity identity = new FDIdentity(custId, custEB.getPK()
							.getId());
					FDCustomerInfo custInfo = this.getCustomerInfo(identity);
					ErpGCDlvInformationHolder purchaserHolder = (ErpGCDlvInformationHolder) holder
							.deepCopy();
					purchaserHolder.getRecepientModel().setRecipientEmail(
							custInfo.getEmailAddress());
					purchaserHolder.getRecepientModel().setRecipientName(
							custInfo.getLastName());
					purchaserHolder.getRecepientModel().setDeliveryMode(
							EnumGCDeliveryMode.EMAIL);
					recipientList.add(purchaserHolder);
				}

				if (toLastRecipient) {
					ErpGCDlvInformationHolder newHolder = (ErpGCDlvInformationHolder) holder
							.deepCopy();
					newHolder.getRecepientModel().setRecipientEmail(
							resendEmailId);
					newHolder.getRecepientModel().setRecipientName(recipName);
					newHolder.getRecepientModel().setPersonalMessage(personMsg);
					newHolder.getRecepientModel().setDeliveryMode(
							EnumGCDeliveryMode.EMAIL);
					recipientList.add(newHolder);
				}
				if (recipientList.isEmpty()) {
					return false; // No recipients to send.
				}
				GiftCardManagerSB sb = this.getGiftCardGManagerHome().create();
				sb.resendGiftCard(saleId, recipientList, source);
			}
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (FinderException fe) {
			throw new FDResourceException(fe);
		}
		return true; // success
	}

	public double getOutStandingBalance(ErpAbstractOrderModel order)
			throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			return sb.getOutStandingBalance(order);

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public void preAuthorizeSales(String salesId) throws FDResourceException {

		try {
			GiftCardManagerSB sb = this.getGiftCardGManagerHome().create();
			List errorList = sb.preAuthorizeSales(salesId);

			if (errorList.size() > 0) {
				// Send GC Authorization Email.
				FDOrderI order = getOrder(salesId);
				String custId = order.getCustomerId();
				FDCustomerEB custEB = getFdCustomerHome().findByErpCustomerId(
						custId);
				FDIdentity identity = new FDIdentity(custId, custEB.getPK()
						.getId());
				FDCustomerInfo custInfo = this.getCustomerInfo(identity);

				int orderCount = getValidOrderCount(identity);
				custInfo.setNumberOfOrders(orderCount);
				Calendar cal = calculateCutOffTime(order); // To get the cutoff
															// time for
															// replacing the
															// order.
				this.doEmail(FDGiftCardEmailFactory.getInstance()
						.createAuthorizationFailedEmail(custInfo, salesId,
								order.getDeliveryReservation().getStartTime(),
								order.getDeliveryReservation().getEndTime(),
								cal.getTime()));
			}
		} catch (RemoteException e) {
			throw new FDResourceException(e);
		} catch (CreateException e) {
			throw new FDResourceException(e);
		} catch (FinderException fe) {
			throw new FDResourceException(fe);
		}
	}

	/**
	 * Captures email address from non-customer visiting iphone app
	 * 
	 * @param emailId
	 * @return
	 * @throws FDResourceException
	 */
	public EnumIPhoneCaptureType iPhoneCaptureEmail(String emailId, EnumTransactionSource source)
			throws FDResourceException {

		if (null == emailId || "".equals(emailId)) {
			return EnumIPhoneCaptureType.INVALID_EMAIL;
		}

		try {
			// Check if email format is correct. @ with . domain
			if (!EmailUtil.isValidEmailAddress(emailId.trim())) {
				LOGGER.info("invalid iphone capture email: " + emailId);
				return EnumIPhoneCaptureType.INVALID_EMAIL;
			}
			// Check if email already exists in customer base
			FDCustomerEB custEB = getFdCustomerHome().findByUserId(emailId,
					EnumServiceType.IPHONE);
			if (custEB != null) {
				LOGGER.info("existing iphone capture email: " + emailId);
				return EnumIPhoneCaptureType.EXISTING;
			}

			LOGGER.info("valid iphone capture email: " + emailId);
			// If unknown email, save it in dlv.zonenotification table
			FDDeliveryManager.getInstance().saveFutureZoneNotification(emailId,
					source.getCode(), (source != null && EnumTransactionSource.ANDROID_WEBSITE.equals(source) 
												? EnumServiceType.ANDROID : EnumServiceType.IPHONE));

			// Send notification email with content managed in CMS.
			this.doEmail(ErpEmailFactory.getInstance().createIPhoneEmail(
					emailId));
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException fe) {
			throw new FDResourceException(fe);
		}
		return EnumIPhoneCaptureType.UNREGISTERED; // success
	}

	public void doEmail(FTLEmailI email) throws FDResourceException {
		try {
			MailerGatewaySB mailer = getMailerHome().create();
			mailer.enqueueEmail(email);
		} catch (CreateException ce) {
			throw new FDResourceException(ce, "Cannot create MailerGatewayBean");
		} catch (RemoteException re) {
			throw new FDResourceException(re,
					"Cannot talk to MailerGatewayBean");
		}
	}

	public List getGiftCardOrdersForCustomer(FDIdentity identity)
			throws FDResourceException {

		try {

			// Check if this gift card is already attached to a customer
			// account.
			GiftCardManagerSB sb = this.getGiftCardGManagerHome().create();
			return sb.getGiftCardOrdersForCustomer(identity.getErpCustomerPK());
			// Log the activity
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}

	}

	public Object getGiftCardRedemedOrders(FDIdentity identity, String certNum)
			throws FDResourceException {
		try {

			// Check if this gift card is already attached to a customer
			// account.
			GiftCardManagerSB sb = this.getGiftCardGManagerHome().create();
			return sb.getGiftCardRedeemedOrders(identity.getErpCustomerPK(),
					certNum);
			// Log the activity
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}

	}

	public Object getGiftCardRedemedOrders(String certNum)
			throws FDResourceException {
		try {

			// Check if this gift card is already attached to a customer
			// account.
			GiftCardManagerSB sb = this.getGiftCardGManagerHome().create();
			return sb.getGiftCardRedeemedOrders(certNum);
			// Log the activity
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}

	}

	public List getDeletedGiftCardForCustomer(FDIdentity identity)
			throws FDResourceException {
		try {

			// Check if this gift card is already attached to a customer
			// account.
			GiftCardManagerSB sb = (GiftCardManagerSB) this
					.getGiftCardGManagerHome().create();
			return sb.getAllDeletedGiftCard(identity.getErpCustomerPK());
			// Log the activity
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}

	}

	public List getGiftCardRecepientsForOrder(String saleId)
			throws FDResourceException {
		try {

			// Check if this gift card is already attached to a customer
			// account.
			GiftCardManagerSB sb = (GiftCardManagerSB) this
					.getGiftCardGManagerHome().create();
			return sb.getGiftCardRecepientsForOrder(saleId);
			// Log the activity
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}

	public ErpGiftCardModel validateAndGetGiftCardBalance(String givexNum)
			throws FDResourceException {

		try {

			GiftCardManagerSB sb = (GiftCardManagerSB) this
					.getGiftCardGManagerHome().create();
			return sb.validateAndGetGiftCardBalance(givexNum);

		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (InvalidCardException ice) {
			throw new FDResourceException(ice);
		}
	}

	public void transferGiftCardBalance(FDIdentity identity,
			String fromGivexNum, String toGivexNum, double amount)
			throws FDResourceException {
		try {

			GiftCardManagerSB sb = (GiftCardManagerSB) this
					.getGiftCardGManagerHome().create();
			sb.transferGiftCardBalance(fromGivexNum, toGivexNum, amount);

			sendGiftCardBalanceTransferEmail(identity, fromGivexNum, sb);

		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (InvalidCardException ce) {
			throw new FDResourceException(ce);
		}
	}

	/**
	 * @param identity
	 * @param fromGivexNum
	 * @param sb
	 * @throws RemoteException
	 * @throws FDResourceException
	 */
	private void sendGiftCardBalanceTransferEmail(FDIdentity identity,
			String fromGivexNum, GiftCardManagerSB sb) throws RemoteException,
			FDResourceException {
		ErpGCDlvInformationHolder erpGCDlvInformationHolder = sb
				.loadGiftCardRecipentByGivexNum(fromGivexNum);

		if (null != erpGCDlvInformationHolder) {
			FDCustomerInfo customer = this.getCustomerInfo(identity);
			ErpRecipentModel recipientModel = erpGCDlvInformationHolder
					.getRecepientModel();
			String recipientName = recipientModel.getRecipientName();
			this
					.doEmail(FDGiftCardEmailFactory.getInstance()
							.createGiftCardBalanceTransferEmail(customer,
									recipientName));
		}
	}

	private void modifyGiftCardComplaint(String saleId, FDOrderI order,
			ErpComplaintModel complaintModel) throws FDResourceException {
		List recipients = getGiftCardRecepientsForOrder(saleId);
		List<ErpComplaintLineModel> gcComplaintLines = new ArrayList<ErpComplaintLineModel>();
		if (null != recipients) {

			for (Iterator iterator = recipients.iterator(); iterator.hasNext();) {
				ErpGCDlvInformationHolder gcHolder = (ErpGCDlvInformationHolder) iterator
						.next();
				ErpRecipentModel recipientModel = gcHolder.getRecepientModel();
				String orderLineNumber = recipientModel.getOrderLineId();
				ErpOrderLineModel erpOrderLine = order
						.getOrderLineByNumber(orderLineNumber);
				ErpComplaintLineModel erpComplaintLineModel = complaintModel
						.getComplaintLine(erpOrderLine.getPK().getId());
				if (null != erpComplaintLineModel) {
					ErpGiftCardComplaintLineModel gcComplaintLine = new ErpGiftCardComplaintLineModel(
							erpComplaintLineModel);
					gcComplaintLine.setCertificateNumber(gcHolder
							.getCertificationNumber());
					gcComplaintLine.setTemplateId(recipientModel
							.getTemplateId());
					gcComplaintLine.setGivexNumber(gcHolder.getGivexNum());
					gcComplaintLines.add(gcComplaintLine);
				}

			}
			complaintModel.setComplaintLines(gcComplaintLines);
		}
	}

	public String[] sendGiftCardCancellationEmail(String saleId,
			String certNum, boolean toRecipient, boolean toPurchaser,
			boolean newRecipient, String newRecipientEmail)
			throws FDResourceException {
		String[] sentEmailAddresses = null;
		String givexNum = null;
		LOGGER
				.debug("Validating and sending the giftcard cancellation emails..");
		try {
			GiftCardManagerSB sb = (GiftCardManagerSB) this
					.getGiftCardGManagerHome().create();
			GenericSearchCriteria criteria = new GenericSearchCriteria(
					com.freshdirect.framework.util.EnumSearchType.GIFTCARD_SEARCH);
			criteria.setCriteriaMap("certNum", certNum);
			List list = sb.getGiftCardModel(criteria);
			if (null != list && !list.isEmpty()) {
				ErpGCDlvInformationHolder holder = (ErpGCDlvInformationHolder) list
						.get(0);
				givexNum = holder.getGivexNum();
				ErpGiftCardModel model = sb.validate(givexNum);
			}
			// ErpGiftCardModel model = sb.validate(givexNum);
			LOGGER.debug("GiftCard is not yet cancelled.");

		} catch (RemoteException e) {
			throw new FDResourceException(e);
		} catch (InvalidCardException e) {
			throw new FDResourceException(e);
		} catch (CardInUseException e) {
			throw new FDResourceException(e);
		} catch (CardOnHoldException e) {
			LOGGER
					.debug(" Card is On Hold..so sending the gift card cancellation emails.");
			System.out.println(e.getMessage());
			try {
				sentEmailAddresses = new String[] { "", "", "" };
				FDOrderI order = getOrder(saleId);
				String custId = order.getCustomerId();
				FDCustomerEB custEB = getFdCustomerHome().findByErpCustomerId(
						custId);
				FDIdentity identity = new FDIdentity(custId, custEB.getPK()
						.getId());
				FDCustomerInfo custInfo = this.getCustomerInfo(identity);

				if (toPurchaser) {
					sentEmailAddresses[1] = custInfo.getEmailAddress();
					this.doEmail(FDGiftCardEmailFactory.getInstance()
							.createGiftCardCancellationPurchaserEmail(custInfo,
									order));
				}
				if (toRecipient) {
					ErpGCDlvInformationHolder gcDlvInfo = order
							.getGCDlvInformationHolder(givexNum);
					sentEmailAddresses[0] = gcDlvInfo.getRecepientModel()
							.getRecipientEmail();
					this.doEmail(FDGiftCardEmailFactory.getInstance()
							.createGiftCardCancellationRecipientEmail(custInfo,
									order.getGCDlvInformationHolder(givexNum)));
				}
				if (newRecipient && null != newRecipientEmail
						&& !"".equalsIgnoreCase(newRecipientEmail.trim())) {
					sentEmailAddresses[2] = newRecipientEmail;
					this.doEmail(FDGiftCardEmailFactory.getInstance()
							.createGiftCardCancellationRecipientEmail(custInfo,
									order.getGCDlvInformationHolder(givexNum),
									newRecipientEmail));
				}
			} catch (RemoteException e1) {
				throw new FDResourceException(e);
			} catch (FinderException e1) {
				throw new FDResourceException(e);
			}
		} catch (CreateException e) {
			throw new FDResourceException(e);
		}
		return sentEmailAddresses;
	}

	private double calculateGiftCardsBalance(
			Collection<ErpGiftCardModel> giftCards) {
		double balance = 0.0;
		if (null != giftCards && !giftCards.isEmpty()) {
			for (ErpGiftCardModel erpGiftCardModel : giftCards) {
				balance += erpGiftCardModel.getBalance();

			}
		}
		return balance;
	}

	public String placeDonationOrder(FDActionInfo info,
			ErpCreateOrderModel createOrder, Set<String> usedPromotionCodes,
			String rsvId, boolean sendEmail, CustomerRatingI cra,
			CrmAgentRole agentRole, EnumDlvPassStatus status, boolean isOptIn)
			throws FDResourceException, ErpFraudException,
			ErpAuthorizationException {
		PrimaryKey pk = null;
		FDIdentity identity = info.getIdentity();
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			String customerPk = identity.getErpCustomerPK();
			pk = sb.placeOrder(new PrimaryKey(customerPk), createOrder,
					usedPromotionCodes, cra, agentRole, null,
					EnumSaleType.DONATION);
			// To store the optIn Indicator
			Connection conn = this.getConnection();
			FDDonationOptinDAO.insert(conn, customerPk, pk.getId(), isOptIn);

			// AUTH sale in CYBER SOURCE
			PaymentManagerSB paymentManager = this.getPaymentManagerHome()
					.create();
			List auths = paymentManager.authorizeSaleRealtime(pk.getId(),
					EnumSaleType.DONATION);
			if (auths != null || auths.size() > 0) {

				// Only when it has a valid auth.
				ErpCustomerManagerSB erpCMsb = this.getErpCustomerManagerHome().create();
				erpCMsb.sendCreateDonationOrderToSAP(customerPk, pk.getId(),
						EnumSaleType.DONATION, cra);
			}

			ErpSaleEB eb = this.getErpSaleHome().findByPrimaryKey(
					new PrimaryKey(pk.getId()));
			ErpActivityRecord rec = info.createActivity(EnumAccountActivityType.PLACE_DON_ORDER);
			rec.setChangeOrderId(pk.getId());
			this.logActivity(rec);
			if (sendEmail) {
				FDOrderI order = getOrder(pk.getId());
				FDCustomerInfo fdInfo = this.getCustomerInfo(identity);
				int orderCount = getValidOrderCount(identity);
				fdInfo.setNumberOfOrders(orderCount);
				this.doEmail(FDGiftCardEmailFactory.getInstance()
						.createRobinHoodOrderConfirmEmail(fdInfo, order));
				// }

			}
			return pk.getId();

		} catch (ErpSaleNotFoundException se) {
			LOGGER.warn("Unable to locate Order ", se);
			throw new FDResourceException(se);
		} catch (ErpTransactionException te) {
			LOGGER.warn("Unable to process order create message ", te);
			throw new FDResourceException(te);
		} catch (CreateException ce) {
			LOGGER.warn("Cannot Create ErpCustomerManagerSessionBean", ce);
			throw new FDResourceException(ce);
		} catch (FinderException re) {
			throw new FDResourceException(re);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (SQLException se) {
			throw new FDResourceException(se);
		} catch ( ErpAddressVerificationException e ) {
			throw new FDResourceException( e );
		}
	}

	public double getPerishableBufferAmount(ErpAbstractOrderModel order)
			throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = (ErpCustomerManagerSB) this
					.getErpCustomerManagerHome().create();
			return sb.getPerishableBufferAmount(order);

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public ErpGCDlvInformationHolder GetGiftCardRecipentByCertNum(String certNum)
			throws FDResourceException {
		try {

			GiftCardManagerSB sb = (GiftCardManagerSB) this
					.getGiftCardGManagerHome().create();
			return sb.loadGiftCardRecipentByCertNum(certNum);
			// Log the activity
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}

	public void saveDonationOptIn(String custId, String saleId, boolean optIn)
			throws RemoteException, FDResourceException {
		try {
			Connection conn = this.getConnection();
			FDDonationOptinDAO.update(conn, custId, saleId, optIn);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		}
	}

	public void resubmitGCOrders() throws FDResourceException {
		// Get GC NSM Orders
		// For each order,
		// -Get customer info from customer id.
		// -Get order info from order id.
		// -Create CustomerRatingInfo instance
		// -Invoke ErpCustomerManagerSessionBean.sendCreateOrderToSAP

		try {
			ErpCustomerManagerSB sb = (ErpCustomerManagerSB) this.getErpCustomerManagerHome().create();
			List<ErpSaleInfo> nsmOrders = sb.getNSMOrdersForGC();
			if (null != nsmOrders) {
				for ( ErpSaleInfo erpSaleInfo : nsmOrders ) {
					String saleId = erpSaleInfo.getSaleId();
					String customerId = erpSaleInfo.getErpCustomerId();
					String sapCustomerId = sb.getSapCustomerId(customerId);
					LOGGER.info("Customer ID:"+customerId+"-Sap Customer ID:"+sapCustomerId+"-");
						if(null != sapCustomerId && sapCustomerId.trim().length() > 0){
							FDOrderI order = this.getOrder(saleId);
							FDCustomerModel fdCustomer = FDCustomerFactory.getFDCustomerFromErpId(customerId);
							int orderCount =sb.getValidOrderCount(new PrimaryKey(customerId));
							String sapOrderNumber = order.getSapOrderId();
						// int orderCount = erpOrderHistory.getValidOrderCount();
						if (sapOrderNumber != null) {
							orderCount--;
						}
						ErpAddressModel address = order.getDeliveryAddress();
						boolean isCorporateUser = false;
						if (null != address) {
							isCorporateUser = EnumServiceType.CORPORATE.equals(address.getServiceType());
						}
							CustomerRatingAdaptor cra = new CustomerRatingAdaptor(fdCustomer.getProfile(),isCorporateUser,orderCount); 
							try {
								sb.sendCreateOrderToSAP(customerId, saleId,order.getOrderType(),cra);
							} catch (ErpSaleNotFoundException e) {
								LOGGER.warn("Order not found to submit to SAP.", e);								
							}
					}

				}
			}
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}
	
	private static final String TOP_FAQS =
		"select CMSNODE_ID from CUST.TOP_FAQS where TIME_STAMP = (select max(TIME_STAMP) from CUST.TOP_FAQS)";
	public List<String> getTopFaqs() throws FDResourceException, RemoteException{
		Connection conn = null;
		try {
			conn = this.getConnection();
			List<String> lst = new ArrayList<String>();
			PreparedStatement ps = conn.prepareStatement(TOP_FAQS);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){			
				lst.add(rs.getString("CMSNODE_ID"));
			}
			rs.close();
			ps.close();

			return lst;
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
	}
	
	private static final String CLICK2CALL_QUERY = "select * from CUST.CLICK2CALL where cro_mod_date = (select max(cro_mod_date) from CUST.CLICK2CALL)";
	public CrmClick2CallModel getClick2CallInfo() throws FDResourceException{
		Connection conn = null;
		CrmClick2CallModel click2callModel = new CrmClick2CallModel();
		try{
			conn = this.getConnection();
			PreparedStatement ps = conn.prepareStatement(CLICK2CALL_QUERY);
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()){
				click2callModel.setId(rs.getString(1));
				click2callModel.setStatus(("Y".equalsIgnoreCase(rs.getString(2)))?true:false);
				click2callModel.setEligibleCustomers(rs.getString(3));
				click2callModel.setNextDayTimeSlot(("Y".equalsIgnoreCase(rs.getString(5)))?true:false);
				click2callModel.setUserId(rs.getString(6));
				click2callModel.setCroModDate(rs.getTimestamp(7));
				Array array = rs.getArray(4);
				String[] zoneCodes = (String[])array.getArray();	
				click2callModel.setDeliveryZones(zoneCodes);
				PreparedStatement ps1 = conn.prepareStatement("select * from CUST.CLICK2CALL_TIME where click2call_id="+rs.getString(1));
				ResultSet rs1 = ps1.executeQuery();
				CrmClick2CallTimeModel[] click2CallTimeModel = new CrmClick2CallTimeModel[7];
				int i=0;
				while(rs1.next()){
					click2CallTimeModel[i++] = new CrmClick2CallTimeModel(rs1.getString(1),rs1.getString(2),rs1.getString(3),("Y".equalsIgnoreCase(rs1.getString(4)))?true:false,rs1.getString(5));					
				}
				click2callModel.setDays(click2CallTimeModel);
				rs1.close();
				ps1.close();
			}
			rs.close();
			ps.close();
		}catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
		
		return click2callModel;
	}

	private static final String CC_BY_SALE_QUERY = "SELECT CLIENT_CODE, QUANTITY, SALE_ID, UNIT_PRICE, TAX_RATE, " +
			"PRODUCT_DESCRIPTION, DELIVERY_DATE FROM CUST.ORDERLINE_CLIENTCODE WHERE SALE_ID = ? ORDER BY CLIENT_CODE, PRODUCT_DESCRIPTION";

	public List<ErpClientCodeReport> findClientCodesBySale(String saleId)
			throws FDResourceException {
		Connection conn = null;
		List<ErpClientCodeReport> ccs = new ArrayList<ErpClientCodeReport>();
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(CC_BY_SALE_QUERY);
			ps.setString(1, saleId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				ErpClientCodeReport item = new ErpClientCodeReport();
				item.setClientCode(rs.getString(1));
				item.setQuantity(rs.getInt(2));
				item.setOrderId(rs.getString(3));
				item.setUnitPrice(rs.getDouble(4));
				item.setTaxRate(rs.getDouble(5));
				item.setProductDescription(rs.getString(6));
				item.setDeliveryDate(rs.getDate(7));
				ccs.add(item);
			}
			rs.close();
			ps.close();
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
		return ccs;
	}

	private static final String CC_BY_DATE_RANGE_QUERY_1 = "SELECT CLIENT_CODE, QUANTITY, SALE_ID, UNIT_PRICE, TAX_RATE, " +
			"PRODUCT_DESCRIPTION, DELIVERY_DATE FROM CUST.ORDERLINE_CLIENTCODE WHERE CUSTOMER_ID = ? AND DELIVERY_DATE >= ? " +
			"ORDER BY SALE_ID, CLIENT_CODE, PRODUCT_DESCRIPTION";
	private static final String CC_BY_DATE_RANGE_QUERY_2 = "SELECT CLIENT_CODE, QUANTITY, SALE_ID, UNIT_PRICE, TAX_RATE, " +
			"PRODUCT_DESCRIPTION, DELIVERY_DATE FROM CUST.ORDERLINE_CLIENTCODE WHERE CUSTOMER_ID = ? AND DELIVERY_DATE BETWEEN ? AND ?" +
			"ORDER BY SALE_ID, CLIENT_CODE, PRODUCT_DESCRIPTION";
	
	public List<ErpClientCodeReport> findClientCodesByDateRange(FDIdentity customerId, Date start, Date end) throws FDResourceException {
		Connection conn = null;
		List<ErpClientCodeReport> ccs = new ArrayList<ErpClientCodeReport>();
		try {
			conn = getConnection();
			String query;
			if (start == null)
				throw new FDResourceException("start date is mandatory");
			else if (end == null)
				query = CC_BY_DATE_RANGE_QUERY_1;
			else
				query = CC_BY_DATE_RANGE_QUERY_2;
			
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, customerId.getErpCustomerPK());
			ps.setDate(2, new java.sql.Date(start.getTime()));
			if (end != null)
				ps.setDate(3, new java.sql.Date(end.getTime()));

			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				ErpClientCodeReport item = new ErpClientCodeReport();
				item.setClientCode(rs.getString(1));
				item.setQuantity(rs.getInt(2));
				item.setOrderId(rs.getString(3));
				item.setUnitPrice(rs.getDouble(4));
				item.setTaxRate(rs.getDouble(5));
				item.setProductDescription(rs.getString(6));
				item.setDeliveryDate(rs.getDate(7));
				ccs.add(item);
			}
			rs.close();
			ps.close();
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
		return ccs;
	}

	public SortedSet<IgnoreCaseString> getOrderClientCodesForUser(FDIdentity identity) throws FDResourceException {
		Connection conn = null;
		SortedSet<IgnoreCaseString> ccs = new TreeSet<IgnoreCaseString>();
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT DISTINCT CLIENT_CODE FROM CUST.ORDERLINE_CLIENTCODE WHERE CUSTOMER_ID = ?");
			ps.setString(1, identity.getErpCustomerPK());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				ccs.add(new IgnoreCaseString(rs.getString(1)));
			}
			rs.close();
			ps.close();
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
		return ccs;
	}
	
	public void createCounter( String customerId, String counterId, int initialValue ) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerCounterDAO.createCounter( conn , customerId, counterId, initialValue );			
		} catch (SQLException e) {
			LOGGER.error( "createCounter() failed with SQLException: ", e );
		} finally {
			if ( conn != null ) {
				try { 
					conn.close();
				} catch (SQLException e) {
					LOGGER.error( "connection close failed with SQLException: ", e );
				}
			}
		}
	}
	
	public void updateCounter( String customerId, String counterId, int newValue ) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerCounterDAO.updateCounter( conn, customerId, counterId, newValue );			
		} catch (SQLException e) {
			LOGGER.error( "updateCounter() failed with SQLException: ", e );
		} finally {
			if ( conn != null ) {
				try { 
					conn.close();
				} catch (SQLException e) {
					LOGGER.error( "connection close failed with SQLException: ", e );
				}
			}
		}		
	}
	
	public Integer getCounter( String customerId, String counterId ) throws FDResourceException {
		Connection conn = null;
		Integer value = null;
		try {
			conn = getConnection();
			value = FDCustomerCounterDAO.getCounter( conn, customerId, counterId );			
		} catch (SQLException e) {
			LOGGER.error( "getCounter() failed with SQLException: ", e );
		} finally {
			if ( conn != null ) {
				try { 
					conn.close();
				} catch (SQLException e) {
					LOGGER.error( "connection close failed with SQLException: ", e );
				}
			}
		}
		return value;
	}
	public void sendSettlementFailedEmail(String saleID) throws FDResourceException {
		try {
			FDOrderI order = this.getOrder(saleID);
			FDCustomerEB fdCustomer = getFdCustomerHome().findByErpCustomerId(
					order.getCustomerId());
			FDIdentity identity = new FDIdentity(order.getCustomerId(),
					fdCustomer.getPK().getId());
			FDCustomerInfo custInfo = this.getCustomerInfo(identity);
			Calendar cal = calculateCutOffTime(order);
			this.doEmail(FDEmailFactory.getInstance()
					.createSettlementFailedEmail(custInfo, saleID,
							order.getDeliveryReservation().getStartTime(),
							order.getDeliveryReservation().getEndTime(),
							cal.getTime(), order.getEStoreId())
			);
		} catch (FinderException e) {
			throw new FDResourceException(e);
		} catch (RemoteException e) {
			throw new FDResourceException(e);
		}
	}
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
			SQLException {
			try {
				EnumDlvPassStatus status = getDlvPassInfo(identity).getStatus();
				FDCustomerModel fdCustomer = FDCustomerFactory.getFDCustomerFromErpId(identity.getErpCustomerPK());
				int orderCount = getValidOrderCount(identity);
				orderCount--;
				ErpAddressModel address = order.getDeliveryInfo().getDeliveryAddress();
				boolean isCorporateUser = false;
				if (null != address) {
					isCorporateUser = EnumServiceType.CORPORATE.equals(address.getServiceType());
				}
				CustomerRatingAdaptor cra = new CustomerRatingAdaptor(fdCustomer.getProfile(),isCorporateUser,orderCount);
				EnumPaymentType pt = order.getPaymentMethod().getPaymentType();
				if (EnumPaymentType.REGULAR.equals(pt) && (cra.isOnFDAccount()/*||EnumPaymentMethodType.EBT.equals(order.getPaymentMethod().getPaymentMethodType())*/)) {
					order.getPaymentMethod().setPaymentType(EnumPaymentType.ON_FD_ACCOUNT);
				}

				this.modifyOrder(info, saleId, order, appliedPromos, oldReservationId, sendEmail, cra, agentRole, status,hasCoupons);
				
			}catch (RemoteException re) {
				throw new FDResourceException(re);
			} catch (CreateException ce) {
				throw new FDResourceException(ce);
			}
		}
	
	public PrimaryKey getCustomerId(String userId) throws FDResourceException{
		try {
			FDCustomerEB custEB = getFdCustomerHome().findByUserId(userId);
			
			FDCustomerModel fdCustomer = (FDCustomerModel) custEB.getModel();
			return new PrimaryKey(fdCustomer.getErpCustomerPK());

		} catch (FinderException ex) {
			throw new FDResourceException(ex);
		} catch (RemoteException ex) {
			throw new FDResourceException(ex);
		}
	}
	
	public ErpAuthorizationModel verify(FDActionInfo action,ErpPaymentMethodI paymentMethod)throws FDResourceException,ErpAuthorizationException {
		PaymentManagerSB sb=null;
		ErpAuthorizationModel auth=null; 
		try {
			sb = this.getPaymentManagerHome().create();
			try {
				auth= sb.verify(ErpAffiliate.getPrimaryAffiliate(action.geteStore()).getMerchant(paymentMethod.getCardType()),paymentMethod);
				logCardVerificationActivity(action,paymentMethod,auth,"");
			} catch( ErpTransactionException te) {
				logCardVerificationActivity(action,paymentMethod,null, te.toString());
			}
			if(auth!=null && !EnumTransactionSource.CUSTOMER_REP.equals(action.getSource())) {
				FDCustomerEB fdCustomerEB=getFdCustomerHome().findByErpCustomerId(paymentMethod.getCustomerId());
				if(!auth.isApproved() || !auth.hasAvsMatched()|| !auth.isCVVMatch()) {
					int count=fdCustomerEB.incrementPymtVerifyAttempts();
					auth.setVerifyFailCount(count);
					if(count>=FDStoreProperties.getPaymentMethodVerificationLimit()) {
						action.setNote(new StringBuilder("Reached limit of ")
						                         .append(FDStoreProperties.getPaymentMethodVerificationLimit())
						                         .append(" unsuccessful credit card verifications.")
						                         .toString());
						this.setActive(action, false);
						//throw new ErpAuthorizationException("Your account has been locked.");
					}
				} else {
					fdCustomerEB.resetPymtVerifyAttempts();
				}
			}
			return auth;
				
		}catch (FinderException fe) {
			throw new FDResourceException(fe);
		}catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
		
	}
	
	/**
	 * Adds auth failures to cusotmer activity log.
	 */
	private void logCardVerificationActivity(FDActionInfo action, ErpPaymentMethodI paymentMethod, ErpAuthorizationModel auth, String desc ) {
		    
			if(null==paymentMethod.getCustomerId())
				return;
			
		    String customerId=paymentMethod.getCustomerId();
			ErpActivityRecord rec = new ErpActivityRecord();
			rec.setActivityType(EnumAccountActivityType.PAYMENT_METHOD_VERIFICATION);
			rec.setSource(action.getSource()!=null?action.getSource(): EnumTransactionSource.SYSTEM);
			rec.setInitiator(action.getInitiator());
			rec.setCustomerId(customerId);
			
			
			StringBuilder msg=new StringBuilder(100);
			if(auth==null) {
				msg.append(desc);
			}
			else {
				msg.append("Verified ")
				.append(auth.getCardType().getDisplayName())
				.append(" ending with ")
				.append(auth.getCcNumLast4())
				.append(" Address: ")
				.append(paymentMethod.getAddress1())
				.append(" ")
				.append(paymentMethod.getZipCode())
				.append(". ");
				
				msg.append("The auth was ")
				   .append(auth.isApproved()?" approved ":" declined. ");
				   
				
				if(auth.isApproved()) {
					
					msg.append(" with code =")
					.append(auth.getAuthCode())
					.append(" and CVV result = ")
					.append(auth.getCvvResponse())
					.append(". The AVS check ")
					.append(auth.hasAvsMatched()?"succeeded":"failed")
					.append(" with code = ")
					.append(auth.getAvs())
					.append(". Zip Match =").append(auth.getZipMatchResponse())
					.append(" and Address Match = ").append(auth.getAddressMatchResponse()).append(".");
				} else {
					msg.append(" Additional description :").append(auth.getDescription()).append(".");
				}
			}
			rec.setNote(msg.toString());
			new ErpLogActivityCommand(LOCATOR, rec, true).execute();
		
	}

	
	public String recordReferral(String customerId, String referralId, String customerEmail) throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDReferAFriendDAO.recordReferral(customerId, referralId, customerEmail, conn);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}		
	}
	
	public String dupeEmailAddress(String email) throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDReferAFriendDAO.dupeEmailAddress(email, conn);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}	
	
	public void storeMobilePreferences(String fdCustomerId, String mobileNumber, String textOffers, String textDelivery,
			String orderNotices, String orderExceptions, String offers, String partnerMessages, EnumEStoreId eStoreId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.storeMobilePreferences(conn, fdCustomerId, mobileNumber, textOffers, textDelivery,orderNotices, orderExceptions, offers, partnerMessages, eStoreId );
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	public void storeGoGreenPreferences(String customerId, String goGreen) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.storeGoGreenPreferences(conn, customerId, goGreen);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	public boolean loadGoGreenPreference(String customerId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDUserDAO.loadGoGreenPreferences(conn, customerId);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	public void storeMobilePreferencesNoThanks(String customerId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.storeMobilePreferencesNoThanks(conn, customerId);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	public void storeSmsPreferencesNoThanks(String fdCustomerId, EnumEStoreId eStoreId) throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.storeSmsPreferences(conn, fdCustomerId, SmsPrefereceFlag.NO_THANKS.getName(), eStoreId);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	public void storeAllMobilePreferences(String customerId, String fdCustomerId,  String mobileNumber, String textOffers, String textDelivery, String goGreen, String phone, String ext, boolean isCorpUser, EnumEStoreId eStoreId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.storeAllMobilePreferences(conn, customerId, fdCustomerId, mobileNumber, textOffers, textDelivery, goGreen, phone, ext, isCorpUser, eStoreId);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	public void doEmail(EnumTranEmailType type, Map input ) throws FDResourceException {
		//System.out.println("---------------------------calling doEmail");
		try {
			TEmailInfoHome home= getTMailerHome();
			TEmailInfoSB remote= home.create();			
			remote.sendEmail(type, input);
		} catch (CreateException ce) {
			throw new FDResourceException(ce, "Cannot create MailerGatewayBean");
		} catch (RemoteException re) {
			if(re.getCause() instanceof TEmailRuntimeException){
				TEmailRuntimeException rex=(TEmailRuntimeException)re.getCause();
				throw rex;
			}
		}
	}
	
	public void storeSMSWindowDisplayedFlag(String customerId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.storeSMSWindowDisplayedFlag(conn, customerId);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	/* APPDEV-2475 DP T&C */
	public void storeDPTCViews(String customerId, int dpTcViewCount) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.storeDPTCViews(conn, customerId, dpTcViewCount);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	public void storeDPTCAgreeDate(String customerId, Date dpTcAgreeDate) throws FDResourceException {
		storeDPTCAgreeDate(null, customerId, dpTcAgreeDate);
	}
	/* pass in info as non-null to log to activity log */
	public void storeDPTCAgreeDate(FDActionInfo info, String customerId, Date dpTcAgreeDate) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.storeDPTCAgreeDate(conn, customerId, dpTcAgreeDate);
			if (info != null) {
				this.logActivity(info.createActivity(EnumAccountActivityType.NEW_DP_TC_AGREE, EnumAccountActivityType.NEW_DP_TC_AGREE.getName()));
			}
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	public List<CustomerCreditModel> getCustomerReprotedLates() throws FDResourceException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		List<CustomerCreditModel> ccmList = new ArrayList<CustomerCreditModel>();
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(GET_CUST_LATES);
			rset = pstmt.executeQuery();
			while(rset.next()) {
				CustomerCreditModel ccm = new CustomerCreditModel();
				ccm.setSaleId(rset.getString("ID"));
				ccm.setRemType(rset.getString("REM_TYPE"));
				ccm.setCustomerId(rset.getString("CUSTOMER_ID"));
				ccm.setFirstName(rset.getString("FIRST_NAME"));
				ccm.setLastName(rset.getString("LAST_NAME"));
				ccm.setEmail(rset.getString("USER_ID"));
				ccm.setRemainingAmout(rset.getDouble("REM_AMT"));
				ccm.setOriginalAmount(rset.getDouble("AMOUNT"));
				ccm.setTaxRate(rset.getDouble("TAX_RATE"));
				ccm.setDlvPassId(rset.getString("DLV_PASS_ID"));
				ccm.setNewCode(rset.getString("NEW_COMP_CODE"));
				ccmList.add(ccm);
			}
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
		return ccmList;
	}
	
	public List<CustomerCreditModel> getDriverReportedLates() throws FDResourceException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		List<CustomerCreditModel> ccmList = new ArrayList<CustomerCreditModel>();
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(GET_DRIVER_REPORTED_LATES);
			rset = pstmt.executeQuery();
			Hashtable driverlates = new Hashtable();
			while(rset.next()) {
				driverlates.put(rset.getString("ID"), rset.getString("ID"));
			}
			
			//get details for the remaining orderids
			Enumeration eobj = driverlates.keys();
			while(eobj.hasMoreElements()) {
				Object saleId = eobj.nextElement();
				CustomerCreditModel ccm = getSaleDetails(conn, (String) saleId);
				if(ccm != null) {
					ccmList.add(ccm);
				}
			}
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
		return ccmList;
	}
	
	public CustomerCreditModel getSaleDetails(Connection conn, String saleId) throws FDResourceException {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		CustomerCreditModel ccm = null;
		//APPDEV-2893-if order is already credited, no need to get sale details
		if(!isOrderCreditedForLateDelivery(conn, saleId)) {
			try {			
				pstmt = conn.prepareStatement(GET_SALE_DETAILS);
				pstmt.setString(1, saleId);
				rset = pstmt.executeQuery();
				while(rset.next()) {
					boolean proceed = true;
					if(rset.getString("DLV_PASS_ID") != null) {
						//APPDEV-2893- check if dlv pass is already extended
						if(isDlvPassAlreadyExtended(conn, saleId, rset.getString("CUSTOMER_ID"))) {
							proceed = false;
						}
					}
					if(proceed) {
						ccm = new CustomerCreditModel();
						ccm.setSaleId(rset.getString("ID"));
						ccm.setRemType(rset.getString("REM_TYPE"));
						ccm.setCustomerId(rset.getString("CUSTOMER_ID"));
						ccm.setFirstName(rset.getString("FIRST_NAME"));
						ccm.setLastName(rset.getString("LAST_NAME"));
						ccm.setEmail(rset.getString("USER_ID"));
						ccm.setRemainingAmout(rset.getDouble("REM_AMT"));
						ccm.setOriginalAmount(rset.getDouble("AMOUNT"));
						ccm.setTaxRate(rset.getDouble("TAX_RATE"));
						ccm.setDlvPassId(rset.getString("DLV_PASS_ID"));
						ccm.setNewCode(rset.getString("NEW_COMP_CODE"));
					}
				}
			} catch (SQLException sqle) {
				throw new FDResourceException(sqle);
			} finally {
				if(pstmt != null)
					try {
						pstmt.close();
					} catch(Exception e) {}
			}
		}
		return ccm;
	}
	
	public boolean isOrderCreditedForLateDelivery(Connection conn, String saleId) throws FDResourceException {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		try {
			pstmt = conn.prepareStatement("select comp_code from cust.complaint_dept_code where id in " + 
			        "(select complaint_dept_code_id from cust.complaintline where complaint_id in " + 
		            "(select id from cust.complaint where sale_id=? " +
		                "and STATUS in ('PEN','APP')) " +
		        ") and comp_code in (select comp_code from CUST.LATE_DLV_COMPLAINT_CODES)");
			pstmt.setString(1, saleId);
			rset = pstmt.executeQuery();
			if(rset.next()) {
				return true;
			}
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			if(pstmt != null) {
				try {
					pstmt.close();
				} catch(Exception e) {}
			}
			if(rset != null) {
				try {
					rset.close();
				} catch(Exception e) {}
			}
		}
		return false;
	}	
	
	public boolean isDlvPassAlreadyExtended(Connection conn, String orderId, String customerId) throws FDResourceException {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		try {
			pstmt = conn.prepareStatement("SELECT reason FROM CUST.ACTIVITY_LOG where customer_id=? and sale_id=? and reason in (select comp_code from CUST.LATE_DLV_COMPLAINT_CODES)");
			pstmt.setString(1, customerId);
			pstmt.setString(2, orderId);
			rset = pstmt.executeQuery();
			if(rset.next()) {
				return true;
			}			
		} catch (SQLException sqle) {
			//sqle.printStackTrace();
			throw new FDResourceException(sqle);
		} finally {
			if(pstmt != null) {
				try {
					pstmt.close();
				} catch(Exception e) {}
			}
			if(rset != null) {
				try {
					rset.close();
				} catch(Exception e) {}
			}
		}
		return false;
	}
	
	public List<CustomerCreditModel> getScanReportedLates() throws FDResourceException {
		Connection conn = null;
		List<CustomerCreditModel> ccmList = new ArrayList<CustomerCreditModel>();
		try {
			
			List<String> driverLates = FDDeliveryManager.getInstance().getScanReportedLates();
			Iterator<String> it = driverLates.iterator();
			conn = getConnection();
			while(it.hasNext()) {
				String saleId = it.next();
				System.out.println("Getting scanlate for sale_id = " + saleId);
				CustomerCreditModel ccm = getSaleDetails(conn, saleId);
				if(ccm != null) {
					ccm.setNewCode("SCANLATE");
					ccmList.add(ccm);
				}
			}
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
		return ccmList;
	}
	
	public void storeLists(Set cmList) throws FDResourceException {
		Connection conn = null;
		PreparedStatement pstmt = null;		
		String autoID = "";
		if(cmList.size() > 0)
			autoID = insertAutoDlv();
		final int batchSize = 1000;
		int count = 0;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(INSERT_ORDERS);
			Iterator iter = cmList.iterator();
			while(iter.hasNext()) {
				CustomerCreditModel ccm = (CustomerCreditModel) iter.next();
				try {
					pstmt.setString(1, autoID);
					pstmt.setString(2, ccm.getSaleId());
					pstmt.setString(3, ccm.getCustomerId());
					pstmt.setDouble(4, ccm.getRemainingAmount());
					pstmt.setString(5, ccm.getRemType());
					pstmt.setDouble(6, ccm.getOriginalAmount());
					pstmt.setDouble(7, ccm.getTaxAmount());
					pstmt.setDouble(8, ccm.getTaxRate());
					pstmt.setString(9, ccm.getDlvPassId());
					pstmt.setString(10, ccm.getNewCode());
					pstmt.addBatch();
					
					if(++count % batchSize == 0) {
				        pstmt.executeBatch();
				    }
				} catch(Exception e) {					
						throw new FDResourceException(e);
					}
				}
			pstmt.executeBatch(); // insert remaining records
			pstmt.close();
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
		
	}
	
	public String insertAutoDlv()  throws FDResourceException {
		Connection conn = null;
		PreparedStatement pstmt = null;		
		try {
			conn = getConnection();
			String id = SequenceGenerator.getNextId(conn, "CUST");
			pstmt = conn.prepareStatement(INSERT_AUTO_CREDIT);
			pstmt.setString(1, id);
			pstmt.execute();
			return id;
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	public String getOASQueryString(EnumServiceType serviceType, String depotCode, String zipCode, String county) {
		QueryStringBuilder queryString = new QueryStringBuilder();
		String metalRating = "";
		int vip = 0;
		int chefsTable = 0;
		String test = "false";

		String type = "";
		String depotAffil = "";
		if (EnumServiceType.HOME.equals(serviceType)) {
			type = "home";
		} else if (EnumServiceType.DEPOT.equals(serviceType)) {
			type = "depot";
			depotAffil = depotCode;
		} else if (EnumServiceType.PICKUP.equals(serviceType)) {
			type = "pickup";
		} else if (EnumServiceType.CORPORATE.equals(serviceType)) {
			type = "cos";
		} else if (EnumServiceType.WEB.equals(serviceType)) {
			type = "web";
		}

		String pageType = "";
		queryString
				.addParam("v", metalRating)
				.addParam("hv", vip)
				.addParam("ct", chefsTable)
				.addParam("test", test)
				.addParam("zip", zipCode)
				.addParam("type", type)
				.addParam("depot", depotAffil)
				.addParam("nod", "")
				.addParam("do", 0)
				.addParam("win", 2);

		queryString.addParam("cart", "");
		

		queryString.addParam("list", "");
		

		queryString.addParam("lu", "1")
			.addParam("pt", pageType).addParam("id", "")
			.addParam("cohort", county)
			.addParam("recipe", true);

		queryString.addParam("ecppromo", "");
		
		if(FDStoreProperties.isZonePricingAdEnabled()){
			queryString.addParam("zonelevel","true");
			String zoneId = "";
			try {
				zoneId = FDZoneInfoManager.findZoneId(serviceType.getName(), zipCode);
			} catch (FDResourceException e) {
			}
			if(zoneId.equalsIgnoreCase(ZonePriceListing.MASTER_DEFAULT_ZONE)){
				queryString.addParam("mzid",zoneId);
			}else if(zoneId.equalsIgnoreCase(ZonePriceListing.RESIDENTIAL_DEFAULT_ZONE)||zoneId.equalsIgnoreCase(ZonePriceListing.CORPORATE_DEFAULT_ZONE)){
				queryString.addParam("szid",zoneId);
				queryString.addParam("mzid",ZonePriceListing.MASTER_DEFAULT_ZONE);				
			}else{
				queryString.addParam("zid",zoneId);				
				try {
					zoneId = FDZoneInfoManager.findZoneId(serviceType.getName(),null);
				} catch (FDResourceException e) {					
				}
				queryString.addParam("szid",zoneId);
				queryString.addParam("mzid",ZonePriceListing.MASTER_DEFAULT_ZONE);				
			}			
		}
		
		return queryString.toString();

	}
	
	public static String INSERT_AUTO_CREDIT = "insert into CUST.AUTO_LATE_DELIVERY(ID,ORDER_DATE,STATUS,APPROVED_BY) values (?,trunc(sysdate),null,null)";
	
	public static String INSERT_ORDERS = "insert into CUST.AUTO_LATE_DELIVERY_ORDERS(AUTO_LATE_DELIVERY_ID,SALE_ID,CUSTOMER_ID,REM_AMOUNT,REM_TYPE,ORIGINAL_AMOUNT, " +
											"TAX_AMOUNT,TAX_RATE,DLV_PASS_ID,complaint_code) values (?,?,?,?,?,?, ?,?,?,?)";
			
	//@TODO LOGISTICS REINTEGATION - LEAVING THIS HERE. TO DO THE RIGT WAY WE NEED TO STORE CARTON TRACKING INFORMATION IN STOREFRONT.
	public static String GET_DRIVER_REPORTED_LATES = 

		" SELECT CUST.SALE.ID "+
			" FROM CUST.SALE, CUST.SALESACTION, CUST.LATEISSUE_ORDERS, cust.deliveryinfo di "+ 
			" WHERE CUST.SALE.ID = CUST.SALESACTION.SALE_ID and CUST.SALE.status <> 'CAN' "+
            " and CUST.SALESACTION.id = di.salesaction_id and CUST.SALESACTION.ACTION_DATE=CUST.SALE.CROMOD_DATE "+ 
            " and CUST.LATEISSUE_ORDERS.SALE_ID = CUST.SALE.ID "+
            " AND CUST.SALESACTION.REQUESTED_DATE = trunc(sysdate)-1 "+
            " and not exists "+ 
            " ( "+
            " 	select 1 FROM DLV.CARTONTRACKING t "+ 
            " 	WHERE  t.CARTONSTATUS in ('DELIVERED','REFUSED') and T.WEBORDERNUM = CUST.SALE.ID " +
            "	and T.SCANDATE < (DI.ENDTIME + 1/1440) "+
            " ) ";
	
	/*APPDEV-2893 - Do not get details for pickup orders*/
	public static String GET_SALE_DETAILS = 
		"SELECT distinct " +
		  "CUST.SALE.ID, " +
		  "decode(cust.CHARGELINE.PROMOTION_AMT, null, 'F',1,'W') REM_TYPE, " +
		  "CUST.SALE.CUSTOMER_ID, " +
		  "CUST.CUSTOMERINFO.FIRST_NAME, " +
		  "CUST.CUSTOMERINFO.LAST_NAME, " +
		  "CUST.CUSTOMER.USER_ID, " +
		  "(CUST.CHARGELINE.AMOUNT + CUST.CHARGELINE.AMOUNT * nvl(CUST.CHARGELINE.TAX_RATE,0))*decode(((nvl(CHARGELINE.PROMOTION_AMT,2)-1) + decode(DLV_PASS_ID,null,1,0)),0,0,1,1,2,1) REM_AMT, " +
		  "CUST.CHARGELINE.AMOUNT, " +
		  "CUST.CHARGELINE.TAX_RATE, " +
		  "cust.sale.DLV_PASS_ID, " +
		  "'DRVRLATE' NEW_COMP_CODE " +
		"FROM " +
		  "CUST.SALE, " +
		  "CUST.SALESACTION, " +
		  "CUST.CUSTOMER, " +
		  "CUST.CUSTOMERINFO, " +
		  "CUST.CHARGELINE, " +
		  "CUST.DELIVERYINFO " +
		"WHERE " +
		   "CUST.SALE.ID= ? " +
		   "AND CUST.SALE.ID = CUST.SALESACTION.SALE_ID and CUST.SALE.status<>'CAN' " + 
		   "AND CUST.SALESACTION.REQUESTED_DATE = trunc(sysdate)-1 " +
		   "AND CUST.CUSTOMER.ID=CUST.CUSTOMERINFO.CUSTOMER_ID " +
		   "AND CUST.SALE.CUSTOMER_ID=CUST.CUSTOMER.ID " +
		   "AND CUST.SALE.CUSTOMER_ID=CUST.SALESACTION.CUSTOMER_ID " + 
		   "AND CUST.SALESACTION.ACTION_DATE=CUST.SALE.CROMOD_DATE " +
		   "AND CUST.SALESACTION.ID=CUST.CHARGELINE.SALESACTION_ID " +
		   "AND CUST.CHARGELINE.TYPE = 'DLV' " +
		   "and CUST.DELIVERYINFO.salesaction_id = cust.salesaction.id " +
           "and CUST.DELIVERYINFO.delivery_type != 'P' " +
		   "order by REM_TYPE";

	
	public static String GET_CUST_LATES = "SELECT distinct " +
						  "CUST.SALE.ID, " +
						  "decode(cust.CHARGELINE.PROMOTION_AMT, null, 'F',1,'W') REM_TYPE, " +
						  "CUST.SALE.CUSTOMER_ID, " +
						  "CUST.CUSTOMERINFO.FIRST_NAME, " +
						  "CUST.CUSTOMERINFO.LAST_NAME, " +
						  "CUST.CUSTOMER.USER_ID, " +
						  "(CUST.CHARGELINE.AMOUNT + CUST.CHARGELINE.AMOUNT * nvl(CUST.CHARGELINE.TAX_RATE,0))*decode(((nvl(CHARGELINE.PROMOTION_AMT,2)-1) + decode(DLV_PASS_ID,null,1,0)),0,0,1,1,2,1) REM_AMT, " +
						  "CUST.CHARGELINE.AMOUNT, " +
						  "CUST.CHARGELINE.TAX_RATE, " +
						  "cust.sale.DLV_PASS_ID, " +
						  "'CUSTLATE' NEW_COMP_CODE " +
						"FROM " +
						  "CUST.SALE, " +
						  "CUST.SALESACTION, " +
						  "CUST.CASE, " +
						  "CUST.CUSTOMER, " +
						  "CUST.CUSTOMERINFO, " +
						  "CUST.CHARGELINE " +
						"WHERE " +
						  "( CUST.SALE.ID=CUST.CASE.SALE_ID  ) " +
						  "AND  CUST.SALE.ID = CUST.SALESACTION.SALE_ID and CUST.SALE.status<>'CAN' " + 
						  "AND " +
						     "CUST.SALESACTION.REQUESTED_DATE = trunc(sysdate)-1  " +
						   "AND " +
						   "CUST.CASE.CASE_SUBJECT  In  ( 'LDQ-004','LDQ-005','LDQ-006','LDQ-007','LDQ-009','LDQ-010'  ) " +
						   "AND CUST.CUSTOMER.ID=CUST.CUSTOMERINFO.CUSTOMER_ID " +
						   "AND CUST.SALE.CUSTOMER_ID=CUST.CUSTOMER.ID " +
						   "AND CUST.SALE.CUSTOMER_ID=CUST.SALESACTION.CUSTOMER_ID " + 
						   "AND CUST.SALESACTION.ACTION_DATE=CUST.SALE.CROMOD_DATE " +
						   "AND CUST.SALESACTION.ID=CUST.CHARGELINE.SALESACTION_ID " +
						   "AND CUST.CHARGELINE.TYPE = 'DLV' " +
						   "order by REM_TYPE";
			 
	public List<FDCartonInfo> getCartonDetailsForSale(FDOrderI order) throws FDResourceException {

		List<FDCartonInfo> cartonInfo = new ArrayList<FDCartonInfo>();

		SapCartonInfoForSale bapi = new SapCartonInfoForSale("1000", 
												order.getRequestedDate(),
												order.getErpSalesId(),
												order.getSapOrderId(), 
												order.getShippingInfo().getWaveNumber()) ;		
		try {
			bapi.execute();	

			// Get ErpCartonInfo and create our adapters
			List<ErpCartonInfo> erpCartons = bapi.getCartonInfos();

			for (int i = 0; i < erpCartons.size(); i++) {
				ErpCartonInfo carton = erpCartons.get(i);
	
				List<FDCartonDetail> cartonDetails = new ArrayList<FDCartonDetail>();
				FDCartonInfo f = new FDCartonInfo(carton, cartonDetails);
				cartonInfo.add(f);
				for (int j = 0; j < carton.getDetails().size(); j++) {
					ErpCartonDetails detail = (ErpCartonDetails) carton.getDetails().get(j);

					FDCartLineI cartLine = null;
					for (int k = 0; k < order.getOrderLines().size(); k++) {
						FDCartLineI curCartLine = order.getOrderLines().get(k);
						if (curCartLine.getOrderLineNumber().equals(detail.getOrderLineNumber())) {
							cartLine = curCartLine;
							break;
						}
					}
					if (cartLine != null) {
						FDCartonDetail fdDetail = new FDCartonDetail(f, detail, cartLine);
						cartonDetails.add(fdDetail);
					}
			   }
			}
		} catch (SapException e) {
			e.printStackTrace();
			throw new FDResourceException(e);
		}
		return cartonInfo;
	}
	
	public FDUserI saveExternalCampaign(FDUserI user) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDUserDAO.saveExternalCampaign(conn, user);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	private static int getAuthFailEmailTemplate(String authFailMsg ) {
    	int template=4;
    	String code=authFailMsg.substring(0,3);
    	int val=0;
    	try {val=Integer.parseInt(code);}catch(Exception e) {}
    	switch(val) {
	    	case 606 :case 261 :case 267 :case 610:template=1;break;
	    	case 201 :case 591 :case 825 :case 304 :case 813:template=2;break;
	    	case 522 :case 595 :case 603 :case 605 :case 754 :case 903 :case 904:template=2;break;
    	}
    	return template;
    	
    }
	
	public void logIpLocatorEvent(IpLocatorEventDTO ipLocatorEventDTO) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.logIpLocatorEvent(conn, ipLocatorEventDTO);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	public IpLocatorEventDTO loadIpLocatorEvent (String fdUserId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDUserDAO.loadIpLocatorEvent(conn, fdUserId);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	public boolean  isFeatureEnabled(String customerId, EnumSiteFeature feature) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			boolean flag = FDUserDAO.isFeatureEnabled(conn, customerId, feature.getAttributeKey());

			return flag;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	public CustomerAvgOrderSize getHistoricOrderSize(String customerId) throws FDResourceException {

		Connection conn = null;
		try {
			conn = getConnection();
			return FDUserDAO.getHistoricOrderSize(conn, customerId);
		}catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	
	}
	
	public void storeSmsPrefereceFlag(String fdCustomerId, String flag, EnumEStoreId eStoreId)throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.storeSmsPreferences(conn,fdCustomerId,flag, eStoreId );
		}catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
		
		
	}	
	// for 4125 coremetrics
	public String getCustomersProfileValue(String customerId)throws FDResourceException{
		Connection conn = null;
		String profileValue = null;
		try {
			conn = getConnection();
			profileValue = ProfileDetailDAO.getCustomersProfile(conn,customerId);
		}catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
			return profileValue;
		}
	public String getCustomersCounty(String customerId)throws FDResourceException{
		Connection conn = null;
		String county = null;
		try {
			conn = getConnection();		
			county = ProfileDetailDAO.getCustomerCounty(conn,customerId);
		}catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
			return county;
	}
	public String getLastOrderID(FDIdentity identity,EnumEStoreId eStoreId)
			throws FDResourceException {
		try {
			ErpCustomerManagerSB sb = (ErpCustomerManagerSB) this
					.getErpCustomerManagerHome().create();
			return sb
					.getLastOrderID(new PrimaryKey(identity.getErpCustomerPK()),eStoreId);

		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}
	public boolean setAcknowledge(FDIdentity identity, boolean acknowledge,String ackType) throws FDResourceException{
		Connection conn = null;
		boolean status = true;
		try {
			conn = getConnection();
			status=FDUserDAO.storeFDTCAgreeDate(conn, identity.getFDCustomerPK(), ackType);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
		return status;
	}
	
	public String getParentOrderAddressId(String parentOrderId)throws FDResourceException{
		LOGGER.info("getParentOrderAddressId... "+parentOrderId);
		Connection conn = null;
		String parentOrderAddressId=null;
		try {
			
			ErpAddressModel deliveryAddressModel = null;

			conn = getConnection();
			
			deliveryAddressModel= this.getLastOrderAddress(parentOrderId);
			
			Collection<ErpAddressModel> addressList=FDUserDAO.getParentAddresscheck(conn, deliveryAddressModel);
			
			for ( ErpAddressModel address : addressList ) {
				
				if(((deliveryAddressModel.getScrubbedStreet()==null || address.getScrubbedStreet()==null))|| 
							(deliveryAddressModel.getScrubbedStreet()!=null || deliveryAddressModel.getScrubbedStreet().equalsIgnoreCase(address.getScrubbedStreet()))){
					if(((deliveryAddressModel.getAddress1()==null || address.getAddress1()==null)) || 
						(deliveryAddressModel.getAddress1()!=null && deliveryAddressModel.getAddress1().equalsIgnoreCase(address.getAddress1()))){
							if ((deliveryAddressModel.getApartment()==null && address.getApartment()==null) || 
									(deliveryAddressModel.getApartment()!=null && deliveryAddressModel.getApartment().equalsIgnoreCase(address.getApartment()))){
								parentOrderAddressId= address.getId();
						}
					}
				 }	
		       }
			
		} catch (SQLException sqle) {
			LOGGER.info("getParentOrderAddressId  FAILED... "+parentOrderId);
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
		return parentOrderAddressId;
	}
	
	
	public boolean getAddonOrderCount(String orderId)throws FDResourceException{
		LOGGER.info("addOnOrderCount checking for this orderID... "+orderId);
			Connection conn = null;
			boolean addOnOrderCount=false;
	try
		{
			conn = getConnection();
			int addOnOrderCountOfParent= FDUserDAO.getAddonOrdersCount(orderId, conn);
			if(addOnOrderCountOfParent<=4){
				addOnOrderCount=true;
				}
		} catch (SQLException sqle) {
			LOGGER.info("addOnOrderCountOfParent  FAILED... "+orderId);
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
		return addOnOrderCount ;
	}
	
	public boolean reSendInvoiceEmail(String orderId )throws FDResourceException{
		LOGGER.info("reSendInvoiceEmail checking for this orderID... "+orderId);
			MailerGatewaySB mailBean;
			boolean reSendInvoiceEmail=false;
	try {
			ErpSaleEB eb = this.getErpSaleHome().findByPrimaryKey(new PrimaryKey(orderId));
			
			ErpSaleModel saleModel = (ErpSaleModel)eb.getModel();
					saleModel = (ErpSaleModel)eb.getModel();
			FDOrderAdapter fdOrder = new FDOrderAdapter(saleModel);
			Collections.sort(fdOrder.getOrderLines(), FDCartModel.PRODUCT_SAMPLE_COMPARATOR);
			
			PrimaryKey customerPK = eb.getCustomerPk();
			
			ErpCustomerEB customerEB = this.getErpCustomerHome().findByPrimaryKey(customerPK);
			ErpCustomerInfoModel erpInfo = ((ErpCustomerModel)customerEB.getModel()).getCustomerInfo();
			FDCustomerInfo fdInfo = new FDCustomerInfo(erpInfo.getFirstName(), erpInfo.getLastName());
			fdInfo.setHtmlEmail(!erpInfo.isEmailPlaintext());
			fdInfo.setEmailAddress(erpInfo.getEmail());
			fdInfo.setGoGreen(erpInfo.isGoGreen());
			mailBean = this.getMailerGatewayHome().create();
			mailBean.enqueueEmail(FDEmailFactory.getInstance().createFinalAmountEmail(fdInfo, fdOrder));
			reSendInvoiceEmail=true;
		
		}
	
		catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.info("reSendInvoiceEmail  FAILED... "+orderId);
			throw new FDResourceException(e);
		}
		return reSendInvoiceEmail;
	}
	
	private MailerGatewayHome getMailerGatewayHome() {
		try {
			return (MailerGatewayHome) LOCATOR.getRemoteHome("freshdirect.mail.MailerGateway");
		} catch (NamingException e) {
			throw new EJBException(e);
		}
	}
	
	public boolean iPhoneCaptureEmail(String email, String zoneId, String serviceType) throws FDResourceException {

		if (null == email || "".equals(email)) {
			return false;
		}

		try {
			// Check if email format is correct. @ with . domain
			if (!EmailUtil.isValidEmailAddress(email.trim())) {
				LOGGER.info("invalid iphone capture email: " + email);
				return false;
			}
	

			LOGGER.info("valid iphone capture email: " + email);
			// If unknown email, save it in dlv.zonenotification table
			FDDeliveryManager.getInstance().saveFutureZoneNotification(email,
					zoneId, EnumServiceType.getEnum(serviceType));

		} catch (Exception e) {
			throw new FDResourceException(e);
		}
		return true; // success
	}
	
	
	public void storeEmailPreferenceFlag(String fdCustomerId, String flag, EnumEStoreId eStoreId)throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			FDUserDAO.storeEmailPreferenceFlag(conn,fdCustomerId,flag, eStoreId );
		}catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
		
		
	}
}


package com.freshdirect.fdstore.customer.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Category;

import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.address.PhoneNumber;
import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpDeliveryPlantInfoModel;
import com.freshdirect.customer.ErpOrderLineModel;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.customer.ExternalCampaign;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartLineModel;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDInvalidConfigurationException;
import com.freshdirect.fdstore.customer.FDRecipientList;
import com.freshdirect.fdstore.customer.FDUser;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.customer.SavedRecipientModel;
import com.freshdirect.fdstore.iplocator.IpLocatorEventDTO;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.core.SequenceGenerator;
import com.freshdirect.framework.util.NVL;
import com.freshdirect.framework.util.SqlUtil;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.logistics.delivery.dto.CustomerAvgOrderSize;
import com.freshdirect.logistics.fdstore.StateCounty;
import com.freshdirect.sms.EnumSMSAlertStatus;

public class FDUserDAO {

	private static final Category LOGGER = LoggerFactory.getInstance(FDUserDAO.class);


	private static java.util.Random rand = new java.util.Random();

	private static String newCookieId() {
		StringBuffer buff = new StringBuffer();
		for (int j = 0; j < 3; j++)
			buff.append(Long.toString(Math.abs(rand.nextLong()), 36).toUpperCase());
		return buff.toString();
	}

	private static FDUser createUser(Connection conn, String cookie, String street, String apt, String zipCode, String depotCode, EnumServiceType serviceType, EnumEStoreId eStoreId) throws SQLException {
		
		System.out.println("inside the create user .. creating the user");
		String id = SequenceGenerator.getNextId(conn, "CUST");
		PreparedStatement ps = conn.prepareStatement("INSERT INTO CUST.FDUSER (ID, COOKIE, ZIPCODE, DEPOT_CODE, SERVICE_TYPE, CREATED, ADDRESS1, APARTMENT,ZP_SERVICE_TYPE) values (?,?,?,?,?,SYSDATE,?,?,?)");
		ps.setString(1, id);
		ps.setString(2, cookie);
		if (zipCode != null) {
			ps.setString(3, zipCode);
		} else {
			ps.setNull(3, Types.VARCHAR);
		}
		if (depotCode != null) {
			ps.setString(4, depotCode);
		} else {
			ps.setNull(4, Types.VARCHAR);
		}
		if(serviceType != null){
			ps.setString(5, serviceType.getName());
		} else {
			ps.setNull(5, Types.VARCHAR);
		}
		if(street != null){
			ps.setString(6, street);
		} else {
			ps.setNull(6, Types.VARCHAR);
		}
		if(apt != null){
			ps.setString(7, apt);
		} else {
			ps.setNull(7, Types.VARCHAR);
		}
		//Add for zone pricing.
		if(serviceType != null){
			ps.setString(8, serviceType.getName());
		} else {
			ps.setNull(8, Types.VARCHAR);
		}
		
		
		if (ps.executeUpdate() != 1) {
			throw new SQLException("Row not created");
		}

		ps.close();

		createFDUserEStore(conn, zipCode, serviceType, eStoreId, id);
		FDUser user = new FDUser(new PrimaryKey(id));
		user.setCookie(cookie);
		user.setZipCode(zipCode);
		user.setDepotCode(depotCode);
		
		setAddressbyZipCode(conn, zipCode, user);
		
		// no need to create children, a new user doesn't have a cart yet
		return user;
	}

	/**
	 * @param conn
	 * @param zipCode
	 * @param serviceType
	 * @param eStoreId
	 * @param id
	 * @throws SQLException
	 */
	private static void createFDUserEStore(Connection conn, String zipCode,
			EnumServiceType serviceType, EnumEStoreId eStoreId, String id)
			throws SQLException {
		PreparedStatement ps;
		ps = conn.prepareStatement("INSERT INTO CUST.FDUSER_ESTORE (FDUSER_ID, E_STORE, ZIPCODE, SERVICE_TYPE, ZP_SERVICE_TYPE, CREATED) values (?,?,?,?,?,SYSDATE)");
		ps.setString(1, id);
		ps.setString(2, eStoreId.getContentId());
		if (zipCode != null) {
			ps.setString(3, zipCode);
		} else {
			ps.setNull(3, Types.VARCHAR);
		}
		if(serviceType != null){
			ps.setString(4, serviceType.getName());
		} else {
			ps.setNull(4, Types.VARCHAR);
		}
		//Add for zone pricing.
		if(serviceType != null){
			ps.setString(5, serviceType.getName());
		} else {
			ps.setNull(5, Types.VARCHAR);
		}		
		
		if (ps.executeUpdate() != 1) {
			throw new SQLException("Row not created");
		}

		ps.close();
	}

	private static void setAddressbyZipCode(Connection conn,
			String zipCode, FDUser user) {
		if(user.getAddress()!=null){
			StateCounty stateCounty= FDDeliveryManager.getInstance().getStateCountyByZipcode(zipCode);
			if (stateCounty!=null) {
				user.getAddress().setState(WordUtils.capitalizeFully(stateCounty.getState()));
				user.getAddress().setCity(WordUtils.capitalizeFully(stateCounty.getCity()));
			}
		}
	}

	
	private static List<ErpOrderLineModel> convertToErpOrderlines(List<FDCartLineI> cartlines) throws FDResourceException {

		int num = 0;
		List<ErpOrderLineModel> erpOrderlines = new ArrayList<ErpOrderLineModel>();
		for ( FDCartLineI cartline : cartlines ) {
			ErpOrderLineModel erpLines;
			try {
				erpLines = cartline.buildErpOrderLines(num);
			} catch (FDInvalidConfigurationException e) {
				LOGGER.warn("Skipping invalid cartline", e);
				continue;
			}
			erpOrderlines.add(erpLines);
			num += 1;
		}

		return erpOrderlines;
	}

	/**
	 * Load cart into user instance
	 * 
	 * @param conn JDBC connection
	 * @param user customer instance
	 * @param lazy Lazy mode == prevent orderlines accessing ProductModels. Good for CRM and other store-less apps
	 *  
	 * @throws SQLException
	 */
	private static void loadCart(Connection conn, FDUser user, final boolean lazy) throws SQLException {
		FDCartModel cart = new FDCartModel();

		List<ErpOrderLineModel> loadCartLines = FDCartLineDAO.loadCartLines(conn, user.getPK(), user.getUserContext().getStoreContext().getEStoreId());
		cart.addOrderLines(convertToCartLines(loadCartLines, lazy));

		cart.setEStoreId(user.getUserContext().getStoreContext().getEStoreId());
		ErpDeliveryPlantInfoModel delPlantInfo=new ErpDeliveryPlantInfoModel();
		delPlantInfo.setPlantId(user.getUserContext().getFulfillmentContext().getPlantId());
		delPlantInfo.setSalesOrg(user.getUserContext().getPricingContext().getZoneInfo().getSalesOrg());
		delPlantInfo.setDistChannel(user.getUserContext().getPricingContext().getZoneInfo().getDistributionChanel());
		cart.setDeliveryPlantInfo(delPlantInfo);
		user.setShoppingCart(cart);
	}
	
	
	private static List<FDCartLineI> convertToCartLines(List<ErpOrderLineModel> erplines, final boolean lazy) {

		List<FDCartLineI> cartlines = new ArrayList<FDCartLineI>();
		for (int i = 0; i < erplines.size(); i++) {
			ErpOrderLineModel ol = erplines.get(i);

			FDCartLineI cartLine;
			cartLine = new FDCartLineModel(ol, lazy);

			cartlines.add(cartLine);
		}

		return cartlines;
	}

	public static FDUser createDepotUser(Connection conn, String depotCode, EnumServiceType serviceType,EnumEStoreId eStoreId) throws SQLException {
		FDUser user = createUser(conn, newCookieId(), null, null, null, depotCode, serviceType, eStoreId);
		return user;
	}

	public static FDUser createUser(Connection conn, String zipCode, EnumServiceType serviceType,EnumEStoreId eStoreId) throws SQLException {
		FDUser user = createUser(conn, newCookieId(), null, null, zipCode, null, serviceType, eStoreId);
		return user;
	}
	
	public static FDUser createUser(Connection conn, EnumServiceType serviceType,EnumEStoreId eStoreId) throws SQLException {
		FDUser user = createUser(conn, newCookieId(), null, null, null, null, serviceType, eStoreId);
		return user;
	}	

	public static FDUser createUser(Connection conn, AddressModel address, EnumServiceType serviceType,EnumEStoreId eStoreId) throws SQLException {
		FDUser user = createUser(conn, newCookieId(), address.getAddress1(), address.getApartment(), address.getZipCode(), null, serviceType, eStoreId);
		return user;
	}
	
	private static final String LOAD_FROM_IDENTITY_QUERY =
		"SELECT fdu.ID as fduser_id, fdu.COOKIE, fdu.ADDRESS1, fdu.APARTMENT, NVL(fde.ZIPCODE,fdu.ZIPCODE) ZIPCODE, fdu.DEPOT_CODE, NVL(FDE.SERVICE_TYPE, fdu.SERVICE_TYPE) SERVICE_TYPE,fdu.HPLETTER_VISITED, fdu.CAMPAIGN_VIEWED, fdu.GLOBALNAVTUT_VIEWED, fdc.id as fdcust_id, erpc.id as erpcust_id, fdu.ref_tracking_code, " +
		"erpc.active, ci.receive_news,fdu.last_ref_prog_id, fdu.ref_prog_invt_id, fdu.ref_trk_key_dtls, fdu.COHORT_ID, fdu.ZP_SERVICE_TYPE " +
		",fdc.referer_customer_id, fdu.default_list_id, ci.fd_tc_agree, fdc.raf_click_id,fdc.raf_promo_code " +
		"FROM CUST.FDUSER fdu, CUST.fdcustomer fdc, CUST.customer erpc, CUST.customerinfo ci,CUST.FDUSER_ESTORE fde " +
		"WHERE fdu.FDCUSTOMER_ID=fdc.id and fdc.ERP_CUSTOMER_ID=erpc.ID and erpc.id=? " +
		"AND erpc.id = ci.customer_id AND  fdu.id= FDE.FDUSER_ID(+) and FDE.E_STORE(+)=?";

	/**
	 * Recognize customer with given identity
	 * 
	 * @param conn JDBC Connection
	 * @param identity Identity
	 * @param eStoreId 
	 * @param lazy Lazy mode for cartlines
	 * @return
	 * @throws SQLException
	 */
	public static FDUser recognizeWithIdentity(Connection conn, FDIdentity identity, EnumEStoreId eStoreId, final boolean lazy) throws SQLException, FDResourceException {
		LOGGER.debug("attempting to load FDUser from identity");
		PreparedStatement ps = conn.prepareStatement(LOAD_FROM_IDENTITY_QUERY);
		ps.setString(1, identity.getErpCustomerPK());
		ps.setString(2, null !=eStoreId?eStoreId.getContentId():EnumEStoreId.FD.getContentId());
		ResultSet rs = ps.executeQuery();
		FDUser user = loadUserFromResultSet(rs);
		setAddressbyZipCode(conn,user.getZipCode(),user);
		
		if (!user.isAnonymous()) {
			loadCart(conn, user, lazy);
			//Load GC recipients if any. 
			List<SavedRecipientModel> recipients = SavedRecipientDAO.loadSavedRecipients(conn, user.getPK().getId());
			FDRecipientList repList = new FDRecipientList(recipients);
			user.setRecipientList(repList);
			user.setExternalPromoCampaigns(loadExternalCampaigns(conn, identity.getErpCustomerPK()));
			
		}
		user.setEbtAccepted(FDDeliveryManager.getInstance().isZipCodeEbtAccepted(user.getZipCode()));
		
		rs.close();
		ps.close();

		return user;
	}

	private static final String LOAD_FROM_EMAIL_QUERY =
		"SELECT fdu.ID as fduser_id, fdu.COOKIE, fdu.ADDRESS1, fdu.APARTMENT, NVL(fde.zipcode,fdu.ZIPCODE) ZIPCODE, fdu.DEPOT_CODE, NVL(FDE.SERVICE_TYPE, fdu.SERVICE_TYPE) SERVICE_TYPE, fdu.HPLETTER_VISITED, fdu.GLOBALNAVTUT_VIEWED, fdu.CAMPAIGN_VIEWED, fdc.id as fdcust_id, erpc.id as erpcust_id, fdu.ref_tracking_code, " +
		"erpc.active, ci.receive_news,fdu.last_ref_prog_id, fdu.ref_prog_invt_id, fdu.ref_trk_key_dtls, fdu.COHORT_ID, fdu.ZP_SERVICE_TYPE  " +
		",fdc.referer_customer_id, fdu.default_list_id, ci.fd_tc_agree, fdc.raf_click_id,fdc.raf_promo_code " +
		"FROM CUST.FDUSER fdu, CUST.fdcustomer fdc, CUST.customer erpc, CUST.customerinfo ci, CUST.FDUSER_ESTORE fde " +
		"WHERE fdu.FDCUSTOMER_ID=fdc.id and fdc.ERP_CUSTOMER_ID=erpc.ID and erpc.user_id=? " +
		"AND erpc.id = ci.customer_id AND  fdu.id= FDE.FDUSER_ID(+) and FDE.E_STORE(+)=?";

	public static FDUser recognizeWithEmail(Connection conn, String email, EnumEStoreId eStoreId) throws SQLException, FDResourceException {
		LOGGER.debug("attempting to load FDUser based on user id (email)");
		PreparedStatement ps = conn.prepareStatement(LOAD_FROM_EMAIL_QUERY);
		ps.setString(1, email);
		ps.setString(2, null !=eStoreId?eStoreId.getContentId():EnumEStoreId.FD.getContentId());
		ResultSet rs = ps.executeQuery();
		FDUser user = loadUserFromResultSet(rs);
		setAddressbyZipCode(conn,user.getZipCode(),user);

		if (!user.isAnonymous()) {
			loadCart(conn, user, false);
			if(user.getIdentity()!=null)
				user.setExternalPromoCampaigns(loadExternalCampaigns(conn, user.getIdentity().getErpCustomerPK()));
			
		}
		user.setEbtAccepted(FDDeliveryManager.getInstance().isZipCodeEbtAccepted(user.getZipCode()));
		rs.close();
		ps.close();

		return user;
	}
	
	private final static String EXTERNAL_CAMPAIGN_Query = "select * from (select campaign_id from cust.ext_campaign where customer_id = ? order by entered_date desc) where rownum<=2";
	
	private static Set<ExternalCampaign> loadExternalCampaigns(Connection conn, String customer_id) throws SQLException {
		Set<ExternalCampaign> externalCampaigns = new HashSet<ExternalCampaign>();
		PreparedStatement ps = conn.prepareStatement(EXTERNAL_CAMPAIGN_Query);
		ps.setString(1, customer_id);
		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			ExternalCampaign extCampaign = new ExternalCampaign();
			extCampaign.setCampaignId(rs.getString("campaign_id"));
			extCampaign.setEntered(true);
			externalCampaigns.add(extCampaign);
		}
		rs.close();
		ps.close();

		return externalCampaigns;
	}

	private static final String LOAD_FROM_COOKIE_QUERY =
		"SELECT fdu.ID as fduser_id, fdu.COOKIE, fdu.ADDRESS1, fdu.APARTMENT, NVL(fde.zipcode,fdu.ZIPCODE) ZIPCODE, fdu.DEPOT_CODE, NVL(FDE.SERVICE_TYPE, fdu.SERVICE_TYPE) SERVICE_TYPE, fdu.HPLETTER_VISITED, fdu.GLOBALNAVTUT_VIEWED, fdu.CAMPAIGN_VIEWED, fdc.id as fdcust_id, erpc.id as erpcust_id, fdu.ref_tracking_code, " +
		"erpc.active, ci.receive_news, fdu.last_ref_prog_id, fdu.ref_prog_invt_id, fdu.ref_trk_key_dtls, fdu.COHORT_ID, fdu.ZP_SERVICE_TYPE " +
		",rl.referral_link, fdc.referer_customer_id, fdu.default_list_id, ci.fd_tc_agree, fdc.raf_click_id,fdc.raf_promo_code " +
		"FROM CUST.FDUSER fdu, CUST.fdcustomer fdc, CUST.customer erpc, CUST.customerinfo ci, CUST.REFERRAL_LINK rl, CUST.FDUSER_ESTORE fde  " +
		"WHERE fdu.cookie=? and fdu.FDCUSTOMER_ID=fdc.id(+) and fdc.ERP_CUSTOMER_ID=erpc.ID(+) " +
		"AND erpc.id = ci.customer_id(+) " +
		"and  RL.CUSTOMER_ID(+) = ERPC.ID AND  fdu.id= FDE.FDUSER_ID(+) and FDE.E_STORE(+)=?";

	public static FDUser reconnizeWithCookie(Connection conn, String cookie, EnumEStoreId eStoreId) throws SQLException, FDResourceException {
		LOGGER.debug("attempting to load FDUser from cookie");

		PreparedStatement ps = conn.prepareStatement(LOAD_FROM_COOKIE_QUERY);
		ps.setString(1, cookie);
		ps.setString(2, null !=eStoreId?eStoreId.getContentId():EnumEStoreId.FD.getContentId());
		ResultSet rs = ps.executeQuery();
		FDUser user = loadUserFromResultSet(rs);
		setAddressbyZipCode(conn,user.getZipCode(),user);

		if (!user.isAnonymous()) {
			loadCart(conn, user, false);
			//Load GC recipients if any. 
			List<SavedRecipientModel> recipients = SavedRecipientDAO.loadSavedRecipients(conn, user.getPK().getId());
			FDRecipientList repList = new FDRecipientList(recipients);
			user.setRecipientList(repList);
			
			if(user.getIdentity()!=null)
				user.setExternalPromoCampaigns(loadExternalCampaigns(conn, user.getIdentity().getErpCustomerPK()));
		}
		user.setEbtAccepted(FDDeliveryManager.getInstance().isZipCodeEbtAccepted(user.getZipCode()));
		
		rs.close();
		ps.close();

		return user;
	}

	private static FDUser loadUserFromResultSet(ResultSet rs) throws SQLException {
		FDUser user = null;
		if (rs.next()) {
			PrimaryKey pk = new PrimaryKey(rs.getString("FDUSER_ID"));
			user = new FDUser(pk);
			user.setCookie(rs.getString("COOKIE"));
			user.setZipCode(rs.getString("ZIPCODE"));
			user.setDepotCode(rs.getString("DEPOT_CODE"));
			user.setSelectedServiceType(EnumServiceType.getEnum(rs.getString("SERVICE_TYPE")));
			user.setLastRefTrackingCode(rs.getString("REF_TRACKING_CODE"));
            user.setLastRefProgramId(rs.getString("last_ref_prog_id"));
            user.setLastRefProgInvtId(rs.getString("ref_prog_invt_id"));
            user.setLastRefTrkDtls(rs.getString("ref_trk_key_dtls"));
            // for new COS customer
            user.setUserServiceType(EnumServiceType.getEnum(rs.getString("SERVICE_TYPE")));
            // for zone pricing
            //Default to service type if zp_service_type is null.
            String zpServiceType = rs.getString("ZP_SERVICE_TYPE") != null ? rs.getString("ZP_SERVICE_TYPE")  : rs.getString("SERVICE_TYPE");
            user.setZPServiceType(EnumServiceType.getEnum(zpServiceType));
            
			AddressModel addr = user.getAddress();
			if(addr != null) {
				addr.setAddress1(rs.getString("ADDRESS1"));
				addr.setApartment(rs.getString("APARTMENT"));
			}
			
			String fdcustId = rs.getString("FDCUST_ID");
			String erpcustId = rs.getString("ERPCUST_ID");
			if ((fdcustId != null) && (erpcustId != null)) {
				user.setIdentity(new FDIdentity(erpcustId, fdcustId));
			}
			user.setActive("1".equals(rs.getString("ACTIVE")));
			user.setReceiveFDEmails("X".equals(rs.getString("RECEIVE_NEWS")));
			user.setHomePageLetterVisited(NVL.apply(rs.getString("HPLETTER_VISITED"), "").equalsIgnoreCase("X")?true:false);
			user.setCampaignMsgViewed(rs.getInt("CAMPAIGN_VIEWED"));
			user.setGlobalNavTutorialSeen(NVL.apply(rs.getString("GLOBALNAVTUT_VIEWED"), "").equalsIgnoreCase("X")?true:false);

			// Smart Store - Cohort ID
			user.setCohortName(rs.getString("COHORT_ID"));
			
			//APPDEV-1888 referral info
			user.setReferralCustomerId(rs.getString("referer_customer_id"));
			
			user.setDefaultListId( rs.getString( "default_list_id" ) );
			//user.setTcAcknowledge(NVL.apply(rs.getString("fd_tc_agree"), "").equalsIgnoreCase("X")?true:false);
			user.setRafClickId(rs.getString("raf_click_id"));
			user.setRafPromoCode(rs.getString("raf_promo_code"));
			
		} else {
			user = new FDUser();
		}

		return user;
	}
		
	private static final String STORE_USER_SQL =
		"UPDATE CUST.FDUSER " +
		"SET COOKIE=?, ZIPCODE=?, FDCUSTOMER_ID=?, DEPOT_CODE=?, SERVICE_TYPE=?, ADDRESS1=?, APARTMENT=?, " +
		"LAST_REF_PROG_ID=?, REF_PROG_INVT_ID=?, REF_TRK_KEY_DTLS=?, HPLETTER_VISITED=?, GLOBALNAVTUT_VIEWED=?, CAMPAIGN_VIEWED=?, COHORT_ID=?, ZP_SERVICE_TYPE=?, DEFAULT_LIST_ID=? " + 
		"WHERE ID=?";


	public static void storeUser(Connection conn, FDUser user) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(STORE_USER_SQL);
		int index = 1;
		ps.setString(index++, user.getCookie());
		if (user.getZipCode() != null) {
			ps.setString(index++, user.getZipCode());
		} else {
			ps.setNull(index++, Types.VARCHAR);
		}
		FDIdentity identity = user.getIdentity();
		if ((identity != null) && (identity.getFDCustomerPK() != null)) {
			ps.setString(index++, identity.getFDCustomerPK());
		} else {
			ps.setNull(index++, Types.VARCHAR);
		}
		if (user.getDepotCode() != null) {
			ps.setString(index++, user.getDepotCode());
		} else {
			ps.setNull(index++, Types.VARCHAR);
		}
		if(user.getSelectedServiceType() == null){
			ps.setNull(index++, Types.VARCHAR);
		}else{
			ps.setString(index++, user.getSelectedServiceType().getName());
		}
		AddressModel addr = user.getAddress();
		if(addr != null) {
			if(addr.getAddress1() == null) {
				ps.setNull(index++, Types.VARCHAR);			
			} else {
				ps.setString(index++, addr.getAddress1());
			}
			if(addr.getApartment() == null) {
				ps.setNull(index++, Types.VARCHAR);			
			} else {
				ps.setString(index++, addr.getApartment());
			}
		} else {
			ps.setNull(index++, Types.VARCHAR);
			ps.setNull(index++, Types.VARCHAR);		
		}

		if(user.getLastRefProgId()!=null)			
		    ps.setString(index++, user.getLastRefProgId());
		else
			ps.setNull(index++, Types.VARCHAR);
		
		if(user.getLastRefProgInvtId()!=null)			
		    ps.setString(index++, user.getLastRefProgInvtId());
		else
			ps.setNull(index++, Types.VARCHAR);

		if(user.getLastRefTrkDtls()!=null)			
		    ps.setString(index++, user.getLastRefTrkDtls());
		else
			ps.setNull(index++, Types.VARCHAR);

		if(user.isHomePageLetterVisited())
		{
			ps.setString(index++, "X" );	
		}else{
			ps.setNull(index++, Types.VARCHAR);
		}
		
		if(user.isGlobalNavTutorialSeen())
		{
			ps.setString(index++, "X" );	
		}else{
			ps.setNull(index++, Types.VARCHAR);
		}

		ps.setInt(index++, user.getCampaignMsgViewed());
		
		// Smart Store - Cohort ID
		if (user.getCohortName() != null)
			ps.setString(index++, user.getCohortName());
		else
			ps.setNull(index++, Types.VARCHAR);
		
		if(user.getZPServiceType() == null){
			ps.setNull(index++, Types.VARCHAR);
		}else{
			ps.setString(index++, user.getZPServiceType().getName());
		}		
		
		if ( user.getDefaultListId() == null ) {
			ps.setNull( index++, Types.VARCHAR );
		} else {
			ps.setString( index++, user.getDefaultListId() );
		}
		
		// where id = ...
		ps.setString(index++, user.getPK().getId());
		
		if (ps.executeUpdate() != 1) {
			throw new SQLException("FDUser Row not updated :"+user.getPK().getId());
		}
		ps.close();
		
		ps = conn.prepareStatement("UPDATE CUST.FDUSER_ESTORE SET ZIPCODE=?, SERVICE_TYPE=?, ZP_SERVICE_TYPE=?, CREATED=SYSDATE WHERE FDUSER_ID=? AND E_STORE=?");
		
		if (user.getZipCode() != null) {
			ps.setString(1, user.getZipCode());
		} else {
			ps.setNull(1, Types.VARCHAR);
		}
		if(user.getSelectedServiceType() != null){
			ps.setString(2, user.getSelectedServiceType().getName());
		} else {
			ps.setNull(2, Types.VARCHAR);
		}
		if(user.getZPServiceType() != null){
			ps.setString(3, user.getZPServiceType().getName());
		} else {
			ps.setNull(3, Types.VARCHAR);
		}
		ps.setString(4, user.getId());
		ps.setString(5, user.getUserContext().getStoreContext().getEStoreId().getContentId());
		if (ps.executeUpdate() != 1) {
//			throw new SQLException("FDUser Row not updated :"+user.getPK().getId());
			createFDUserEStore(conn, user.getZipCode(),user.getSelectedServiceType(),user.getUserContext().getStoreContext().getEStoreId(),user.getId());
		}
		
		// store children

		ps = conn.prepareStatement("DELETE FROM CUST.FDCARTLINE WHERE FDUSER_ID = ? AND NVL(E_STORE,'FreshDirect')=?");
		ps.setString(1, user.getPK().getId());
		ps.setString(2, user.getUserContext().getStoreContext().getEStoreId().getContentId());
		ps.executeUpdate();
		ps.close();

		List<ErpOrderLineModel> erpOrderLine;
		try {
			erpOrderLine = convertToErpOrderlines(user.getShoppingCart().getOrderLines());
		} catch (FDResourceException e) {
			// !!! fix exception handling
			throw new FDRuntimeException(e);
		}
		
		FDCartLineDAO.storeCartLines(conn, user.getPK(), erpOrderLine,user.getUserContext().getStoreContext());
		//Store GC Recipient List if any.
		if(user.getRecipientList() != null){
			SavedRecipientDAO.storeSavedRecipients(conn, user.getPK().getId(), user.getRecipientList().getRecipients());
		}
	}

		
	
	/**
	 * [APPREQ-369] Store Cohort ID for the given user
	 * 
	 * @param conn
	 * @param user
	 * @throws SQLException
	 */
	public static void storeCohortName(Connection conn, FDUser user) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("UPDATE CUST.FDUSER SET COHORT_ID=? WHERE ID=?");

		if (user.getCohortName() != null)
			ps.setString(1, user.getCohortName());
		else
			ps.setNull(1, Types.VARCHAR);

		ps.setString(2, user.getPK().getId());

		if (ps.executeUpdate() != 1) {
			throw new SQLException("Row not updated");
		}
		ps.close();
	}
	
	public static void storeMobilePreferences(Connection conn, String fdCustomerId, String mobileNumber, String textOffers, String textDelivery,
			String orderNotices, String orderExceptions, String offers, String partnerMessages, EnumEStoreId eStoreId )  {
		PreparedStatement ps = null;
		PreparedStatement selectAlertStatus=null;
		String notice="N";
		String exceptions="N";
		String offer="N";
		String pMessage="N";
		String existingMobNum=null;
		PhoneNumber phone = new PhoneNumber(mobileNumber);
		
		try {
			try {
			
				selectAlertStatus= conn.prepareStatement("select ORDER_NOTIFICATION, ORDEREXCEPTION_NOTIFICATION, SMS_OFFERS_ALERT, PARTNERMESSAGE_NOTIFICATION, " +
						"MOBILE_NUMBER from cust.fdcustomer_estore where fdcustomer_id=? and e_store=? ");
				selectAlertStatus.setString(1,fdCustomerId);
				selectAlertStatus.setString(2, eStoreId.getContentId() );
				ResultSet rs=selectAlertStatus.executeQuery();
				if(rs.next()){
					notice = rs.getString("ORDER_NOTIFICATION") !=null ? rs.getString("ORDER_NOTIFICATION") : EnumSMSAlertStatus.NONE.value();
					exceptions = rs.getString("ORDEREXCEPTION_NOTIFICATION") != null? rs.getString("ORDEREXCEPTION_NOTIFICATION"): EnumSMSAlertStatus.NONE.value();
					offer = rs.getString("SMS_OFFERS_ALERT")!= null ? rs.getString("SMS_OFFERS_ALERT") : EnumSMSAlertStatus.NONE.value() ;
					pMessage = rs.getString("PARTNERMESSAGE_NOTIFICATION") != null ? rs.getString("PARTNERMESSAGE_NOTIFICATION") : EnumSMSAlertStatus.NONE.value();
					existingMobNum = rs.getString("MOBILE_NUMBER") != null ? rs.getString("MOBILE_NUMBER") : null;
				}
			} catch (Exception e) {
				LOGGER.error("Error updating mobile preferences", e);
			}finally {
				try {
					if(selectAlertStatus != null)
						selectAlertStatus.close();
				} catch (Exception e1) {}
			}
			
			Date optinDate=new Date();
			
			if(eStoreId.getContentId().contentEquals(EnumEStoreId.FD.getContentId())){
				
				if(existingMobNum!=null && phone.getPhone()!=null && !phone.getPhone().isEmpty() && phone.getPhone().length()!=0 && !phone.getPhone().equals(existingMobNum)){
					ps = conn.prepareStatement("UPDATE CUST.FDCUSTOMER_ESTORE set mobile_number=?, ORDER_NOTIFICATION=?, ORDEREXCEPTION_NOTIFICATION=?, SMS_OFFERS_ALERT=?, PARTNERMESSAGE_NOTIFICATION=?, " +
						"SMS_OPTIN_DATE=?, DELIVERY_NOTIFICATION=? , OFFERS_NOTIFICATION=? WHERE FDCUSTOMER_ID=?  AND E_STORE=? ");
					ps.setString(1, phone.getPhone());
					if((notice.equals(EnumSMSAlertStatus.SUBSCRIBED.value())||notice.equals(EnumSMSAlertStatus.PENDING.value())) && "y".equalsIgnoreCase(orderNotices)){
						ps.setString(2,EnumSMSAlertStatus.PENDING.value());
					} else {
						ps.setString(2, "Y".equals(orderNotices)?EnumSMSAlertStatus.PENDING.value():EnumSMSAlertStatus.NONE.value());
					}
					if((exceptions.equals(EnumSMSAlertStatus.SUBSCRIBED.value())||exceptions.equals(EnumSMSAlertStatus.PENDING.value())) && "y".equalsIgnoreCase(orderExceptions)){
						ps.setString(3, EnumSMSAlertStatus.PENDING.value());
					} else{
						ps.setString(3, "Y".equals(orderExceptions)?EnumSMSAlertStatus.PENDING.value():EnumSMSAlertStatus.NONE.value());
					}
					if((offer.equals(EnumSMSAlertStatus.SUBSCRIBED.value())||offer.equals(EnumSMSAlertStatus.PENDING.value())) && "y".equalsIgnoreCase(offers)){
						ps.setString(4, EnumSMSAlertStatus.PENDING.value());
					} else {
						ps.setString(4, "Y".equals(offers)?EnumSMSAlertStatus.PENDING.value():EnumSMSAlertStatus.NONE.value());
					}
					if(pMessage.equals(EnumSMSAlertStatus.SUBSCRIBED.value())||pMessage.equals(EnumSMSAlertStatus.PENDING.value())){
						ps.setString(5, EnumSMSAlertStatus.PENDING.value());
					} else{
						ps.setString(5, "Y".equals(partnerMessages)?EnumSMSAlertStatus.PENDING.value():EnumSMSAlertStatus.NONE.value());
					}
					ps.setTimestamp(6, new java.sql.Timestamp(optinDate.getTime()));
					ps.setString(7, "Y".equals(textOffers)?"Y":"N");
					ps.setString(8, "Y".equals(textDelivery)?"Y":"N");
					ps.setString(9, fdCustomerId);
					ps.setString(10, eStoreId.getContentId());
					ps.execute();				
				} else {
					boolean alreadyOptedIn=false;
					if((notice!=null && notice.equals(EnumSMSAlertStatus.SUBSCRIBED.value()))||
							(exceptions!=null && exceptions.equals(EnumSMSAlertStatus.SUBSCRIBED.value()))||
							(offer!=null && offer.equals(EnumSMSAlertStatus.SUBSCRIBED.value()))||
							(pMessage!=null && pMessage.equals(EnumSMSAlertStatus.SUBSCRIBED.value()))){
						
						alreadyOptedIn=true;
					}
					ps = conn.prepareStatement("UPDATE CUST.FDCUSTOMER_ESTORE set MOBILE_NUMBER=?, ORDER_NOTIFICATION=?, ORDEREXCEPTION_NOTIFICATION=?, SMS_OFFERS_ALERT=?, PARTNERMESSAGE_NOTIFICATION=?," +
						" SMS_OPTIN_DATE=?, DELIVERY_NOTIFICATION=?, OFFERS_NOTIFICATION=? WHERE FDCUSTOMER_ID=? AND E_STORE=?");
				
					ps.setString(1, phone.getPhone());
					if(notice.equals(EnumSMSAlertStatus.SUBSCRIBED.value()) && "Y".equals(orderNotices)){
						ps.setString(2, EnumSMSAlertStatus.SUBSCRIBED.value());
					} else{
						if(alreadyOptedIn && "Y".equals(orderNotices)){
							ps.setString(2,EnumSMSAlertStatus.SUBSCRIBED.value());
						}else{
							ps.setString(2, "Y".equals(orderNotices)?EnumSMSAlertStatus.PENDING.value():EnumSMSAlertStatus.NONE.value());
						}
					}
					if(exceptions.equals(EnumSMSAlertStatus.SUBSCRIBED.value()) && "Y".equals(orderExceptions)){
						ps.setString(3, EnumSMSAlertStatus.SUBSCRIBED.value());
					} else{
						if(alreadyOptedIn && "Y".equals(orderExceptions)){
							ps.setString(3,EnumSMSAlertStatus.SUBSCRIBED.value());
						}else{
							ps.setString(3, "Y".equals(orderExceptions)?EnumSMSAlertStatus.PENDING.value():EnumSMSAlertStatus.NONE.value());
						}
					}
					if(offer.equals(EnumSMSAlertStatus.SUBSCRIBED.value()) && "Y".equals(offers)){
						ps.setString(4, EnumSMSAlertStatus.SUBSCRIBED.value());
					} else{
						if(alreadyOptedIn && "Y".equals(offers)){
							ps.setString(4, EnumSMSAlertStatus.SUBSCRIBED.value());
						}else{
							ps.setString(4, "Y".equals(offers)?EnumSMSAlertStatus.PENDING.value():EnumSMSAlertStatus.NONE.value());
						}
					}
					if(pMessage.equals(EnumSMSAlertStatus.SUBSCRIBED.value()) && "Y".equals(partnerMessages)){
						ps.setString(5, EnumSMSAlertStatus.SUBSCRIBED.value());
					} else{
						if(alreadyOptedIn && "Y".equals(partnerMessages)){
							ps.setString(5, EnumSMSAlertStatus.SUBSCRIBED.value());
						} else{
							ps.setString(5, "Y".equals(partnerMessages)?EnumSMSAlertStatus.PENDING.value():EnumSMSAlertStatus.NONE.value());
						}
					}
					ps.setTimestamp(6, new java.sql.Timestamp(optinDate.getTime()));
					ps.setString(7, "Y".equals(textOffers)?"Y":"N");
					ps.setString(8, "Y".equals(textDelivery)?"Y":"N");
					ps.setString(9, fdCustomerId);
					ps.setString(10, eStoreId.getContentId());
					ps.execute();
				}
			} else {
				if(existingMobNum!=null && phone.getPhone()!=null && !phone.getPhone().isEmpty() && phone.getPhone().length()!=0 && !phone.getPhone().equals(existingMobNum)){
					ps = conn.prepareStatement("UPDATE CUST.FDCUSTOMER_ESTORE set mobile_number=?, ORDER_NOTIFICATION=?, ORDEREXCEPTION_NOTIFICATION=?, SMS_OFFERS_ALERT=?, PARTNERMESSAGE_NOTIFICATION=?, " +
							"SMS_OPTIN_DATE=?, DELIVERY_NOTIFICATION=? , OFFERS_NOTIFICATION=? WHERE FDCUSTOMER_ID=?  AND E_STORE=? ");
					ps.setString(1, phone.getPhone());
					if((notice.equals(EnumSMSAlertStatus.SUBSCRIBED.value()) && "y".equalsIgnoreCase(orderNotices))){
						ps.setString(2,EnumSMSAlertStatus.SUBSCRIBED.value());
					} else {
						ps.setString(2, "Y".equals(orderNotices)?EnumSMSAlertStatus.SUBSCRIBED.value():EnumSMSAlertStatus.NONE.value());
					}
					if((exceptions.equals(EnumSMSAlertStatus.SUBSCRIBED.value())&& "y".equalsIgnoreCase(orderExceptions))){
						ps.setString(3, EnumSMSAlertStatus.SUBSCRIBED.value());
					} else{
						ps.setString(3, "Y".equals(orderExceptions)?EnumSMSAlertStatus.SUBSCRIBED.value():EnumSMSAlertStatus.NONE.value());
					}
					if((offer.equals(EnumSMSAlertStatus.SUBSCRIBED.value()) && "y".equalsIgnoreCase(offers))){
						ps.setString(4, EnumSMSAlertStatus.SUBSCRIBED.value());
					} else {
						ps.setString(4, "Y".equals(offers)?EnumSMSAlertStatus.SUBSCRIBED.value():EnumSMSAlertStatus.NONE.value());
					}
					if(pMessage.equals(EnumSMSAlertStatus.SUBSCRIBED.value())){
						ps.setString(5, EnumSMSAlertStatus.SUBSCRIBED.value());
					} else{
						ps.setString(5, "Y".equals(partnerMessages)?EnumSMSAlertStatus.SUBSCRIBED.value():EnumSMSAlertStatus.NONE.value());
					}
					ps.setTimestamp(6, new java.sql.Timestamp(optinDate.getTime()));
					ps.setString(7, "Y".equals(textOffers)?"Y":"N");
					ps.setString(8, "Y".equals(textDelivery)?"Y":"N");
					ps.setString(9, fdCustomerId);
					ps.setString(10, eStoreId.getContentId());
					ps.execute();				
				} else {
					boolean alreadyOptedIn=false;
					if((notice!=null && notice.equals(EnumSMSAlertStatus.SUBSCRIBED.value()))||
							(exceptions!=null && exceptions.equals(EnumSMSAlertStatus.SUBSCRIBED.value()))||
							(offer!=null && offer.equals(EnumSMSAlertStatus.SUBSCRIBED.value()))||
							(pMessage!=null && pMessage.equals(EnumSMSAlertStatus.SUBSCRIBED.value()))){
						
						alreadyOptedIn=true;
					}
					ps = conn.prepareStatement("UPDATE CUST.FDCUSTOMER_ESTORE set MOBILE_NUMBER=?, ORDER_NOTIFICATION=?, ORDEREXCEPTION_NOTIFICATION=?, SMS_OFFERS_ALERT=?, PARTNERMESSAGE_NOTIFICATION=?," +
							" SMS_OPTIN_DATE=?, DELIVERY_NOTIFICATION=?, OFFERS_NOTIFICATION=? WHERE FDCUSTOMER_ID=? AND E_STORE=?");
					
					ps.setString(1, phone.getPhone());
					if(notice.equals(EnumSMSAlertStatus.SUBSCRIBED.value()) && "Y".equals(orderNotices)){
						ps.setString(2, EnumSMSAlertStatus.SUBSCRIBED.value());
					} else{
						if(alreadyOptedIn && "Y".equals(orderNotices)){
							ps.setString(2,EnumSMSAlertStatus.SUBSCRIBED.value());
						}else{
							ps.setString(2, "Y".equals(orderNotices)?EnumSMSAlertStatus.SUBSCRIBED.value():EnumSMSAlertStatus.NONE.value());
						}
					}
					if(exceptions.equals(EnumSMSAlertStatus.SUBSCRIBED.value()) && "Y".equals(orderExceptions)){
						ps.setString(3, EnumSMSAlertStatus.SUBSCRIBED.value());
					} else{
						if(alreadyOptedIn && "Y".equals(orderExceptions)){
							ps.setString(3,EnumSMSAlertStatus.SUBSCRIBED.value());
						}else{
							ps.setString(3, "Y".equals(orderExceptions)?EnumSMSAlertStatus.SUBSCRIBED.value():EnumSMSAlertStatus.NONE.value());
						}
					}
					if(offer.equals(EnumSMSAlertStatus.SUBSCRIBED.value()) && "Y".equals(offers)){
						ps.setString(4, EnumSMSAlertStatus.SUBSCRIBED.value());
					} else{
						if(alreadyOptedIn && "Y".equals(offers)){
							ps.setString(4, EnumSMSAlertStatus.SUBSCRIBED.value());
						}else{
							ps.setString(4, "Y".equals(offers)?EnumSMSAlertStatus.SUBSCRIBED.value():EnumSMSAlertStatus.NONE.value());
						}
					}
					if(pMessage.equals(EnumSMSAlertStatus.SUBSCRIBED.value()) && "Y".equals(partnerMessages)){
						ps.setString(5, EnumSMSAlertStatus.SUBSCRIBED.value());
					} else{
						if(alreadyOptedIn && "Y".equals(partnerMessages)){
							ps.setString(5, EnumSMSAlertStatus.SUBSCRIBED.value());
						} else{
							ps.setString(5, "Y".equals(partnerMessages)?EnumSMSAlertStatus.SUBSCRIBED.value():EnumSMSAlertStatus.NONE.value());
						}
					}
					ps.setTimestamp(6, new java.sql.Timestamp(optinDate.getTime()));
					ps.setString(7, "Y".equals(textOffers)?"Y":"N");
					ps.setString(8, "Y".equals(textDelivery)?"Y":"N");
					ps.setString(9, fdCustomerId);
					ps.setString(10, eStoreId.getContentId());
					ps.execute();
				}
			}
				
		} catch (Exception e) {
			LOGGER.error("Error updating mobile preferences", e);
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch (Exception e1) {}
		}
	}
	
	public static void storeGoGreenPreferences(Connection conn, String customerId, String goGreen) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update CUST.CUSTOMERINFO set go_green=? where customer_id=?");				
			ps.setString(1, "Y".equals(goGreen)?"Y":"N");
			ps.setString(2, customerId);
			ps.execute();
		} catch (Exception e) {
			LOGGER.error("Error updating mobile preferences", e);
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch (Exception e1) {}
		}
	}
	
	public static boolean loadGoGreenPreferences(Connection conn, String customerId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean goGreen = false;
		try {
			ps = conn.prepareStatement("select go_green from CUST.CUSTOMERINFO where customer_id=?");
			ps.setString(1, customerId);
			rs = ps.executeQuery();
			if (rs.next()) {
				goGreen = "Y".equals(rs.getString(1));
			}
		} catch (Exception e) {
			LOGGER.error("Error updating mobile preferences", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e1) {}
			try {
				if(ps != null) {
					ps.close();
				}
			} catch (Exception e1) {}
		}
		return goGreen;
	}
	
	public static void storeMobilePreferencesNoThanks(Connection conn, String customerId) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update CUST.CUSTOMERINFO set mobile_preference_flag=?,NO_THANKS_FLAG='Y' where customer_id=?");	
			ps.setString(1, EnumMobilePreferenceType.UPDATED_FROM_RECEIPT_PAGE.getName());
			ps.setString(2, customerId);
			ps.execute();
		} catch (Exception e) {
			LOGGER.error("Error updating mobile preferences", e);
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch (Exception e1) {}
		}
	}
	
	public static void storeSmsPreferences(Connection conn, String fdCustomerId, String flag, EnumEStoreId eStoreId){
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update CUST.FDCUSTOMER_ESTORE set SMS_PREFERENCE_FLAG=? where fdcustomer_id=? and e_store=?");
			ps.setString(1, flag!=null?flag:null);
			ps.setString(2, fdCustomerId);
			ps.setString(3, eStoreId.getContentId());
			ps.execute();
		} catch (Exception e) {
			LOGGER.error("Error updating sms preferences", e);
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch (Exception e1) {}
		}
	}
	
	public static void storeEmailPreferenceFlag(Connection conn, String fdCustomerId, String flag, EnumEStoreId eStoreId){
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update CUST.FDCUSTOMER_ESTORE set EMAIL_OPTIN=? where fdcustomer_id=? and e_store=?");
			ps.setString(1, flag!=null?flag:null);
			ps.setString(2, fdCustomerId);
			ps.setString(3, eStoreId.getContentId());
			ps.execute();
		} catch (Exception e) {
			LOGGER.error("Error updating email preferences(CUST.FDCUSTOMER_ESTORE.EMAIL_OPTIN)", e);
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch (Exception e1) {}
		}
	}
	
	
	public static void storeAllMobilePreferences(Connection conn, String customerId, String fdCustomerId, String mobileNumber, String textOffers, String textDelivery, String goGreen, String phone, String ext, boolean isCorpUser, EnumEStoreId eStoreid) {
		PreparedStatement ps = null,ps1 = null;
		PhoneNumber mphone = new PhoneNumber(mobileNumber);
		PhoneNumber bphone = new PhoneNumber(phone, ext);
		
		try {
			if(isCorpUser)
			{	ps = conn.prepareStatement("update CUST.CUSTOMERINFO set go_green=?,business_phone=replace(replace(replace(replace(replace(?,'('),')'),' '),'-'),'.'),business_ext=?,mobile_preference_flag=?  where customer_id=?");
				ps1= conn.prepareStatement("update CUST.FDCUSTOMER_ESTORE set mobile_number=?, offers_notification=?,delivery_notification=?  where FDCUSTOMER_ID=? and e_store=?");
			}
			else
			{
				ps = conn.prepareStatement("update CUST.CUSTOMERINFO set go_green=?, home_phone = replace(replace(replace(replace(replace(?,'('),')'),' '),'-'),'.'), home_ext = ?,mobile_preference_flag=? where customer_id=?");
				ps1= conn.prepareStatement("update CUST.FDCUSTOMER_ESTORE set mobile_number=?, offers_notification=?,delivery_notification=?  where FDCUSTOMER_ID=? and e_store=?");
			}
				
				
			ps1.setString(1, mphone.getPhone());
			ps1.setString(2, "Y".equals(textOffers)?"Y":"N");
			ps1.setString(3, "Y".equals(textDelivery)?"Y":"N");
			ps1.setString(4, fdCustomerId);
			ps1.setString(5, eStoreid.getContentId());
			ps1.execute();
			ps.setString(1, "Y".equals(goGreen)?"Y":"N");
			ps.setString(2, bphone.getPhone());
			ps.setString(3, bphone.getExtension());
			ps.setString(4, EnumMobilePreferenceType.UPDATED_FROM_RECEIPT_PAGE.getName());
			ps.setString(5, customerId);
			ps.execute();
		} catch (Exception e) {
			LOGGER.error("Error updating mobile preferences", e);
		} finally {
			try {
				if(ps != null)
					ps.close();
				if(ps1 != null)
					ps1.close();
			} catch (Exception e1) {}
		}
	}
	
	public static void storeSMSWindowDisplayedFlag(Connection conn, String customerId) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update CUST.CUSTOMERINFO set mobile_preference_flag=? where customer_id=?");	
			ps.setString(1, EnumMobilePreferenceType.SAW_MOBILE_PREF.getName());
			ps.setString(2, customerId);
			ps.execute();
		} catch (Exception e) {
			LOGGER.error("Error updating mobile preferences", e);
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch (Exception e1) {}
		}
	}
	
	/* APPDEV-2475 DP T&C */
	public static void storeDPTCViews(Connection conn, String customerId, int dpTcViewCount) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update CUST.CUSTOMERINFO set DP_TC_VIEWS=? where customer_id=?");	
			ps.setInt(1, dpTcViewCount);
			ps.setString(2, customerId);
			ps.execute();
		} catch (Exception e) {
			LOGGER.error("Error updating DPTCView count", e);
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch (Exception e1) {}
		}
	}
	
	public static void storeDPTCAgreeDate(Connection conn, String customerId, java.util.Date dpTcAgreeDate) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update CUST.CUSTOMERINFO set DP_TC_AGREE_DATE=? where customer_id=?");	
			ps.setTimestamp(1, new Timestamp(dpTcAgreeDate.getTime()));
			ps.setString(2, customerId);
			ps.execute();
		} catch (Exception e) {
			LOGGER.error("Error updating DP_TC_AGREE_DATE date", e);
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch (Exception e1) {}
		}
	}

	public static FDUserI saveExternalCampaign(Connection conn, FDUserI user) {
		PreparedStatement ps = null;
		try {
			Calendar d = Calendar.getInstance();
			ps = conn.prepareStatement("INSERT INTO CUST.EXT_CAMPAIGN(customer_id, campaign_id, terms_accepted, entered_date) VALUES (?,?,?,?)");	
			ps.setString(1, user.getIdentity().getErpCustomerPK());
			ps.setString(2, user.getExternalCampaign().getCampaignId());
			ps.setString(3, user.getExternalCampaign().isTermsAccepted()?"X":"");
			ps.setTimestamp(4, new java.sql.Timestamp(d.getTimeInMillis()));
			if(ps.executeUpdate()==1)
			{
				Set<ExternalCampaign> externalCampaigns = user.getExternalPromoCampaigns();
				externalCampaigns.add(user.getExternalCampaign());
				user.setExternalPromoCampaigns(externalCampaigns);
			}
		} catch (Exception e) {
			LOGGER.error("Error saving external campaign", e);
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch (Exception e1) {
				LOGGER.error("Error closing preparedstatement for external campaign", e1);
			}
		}
		return user;
	}
	
	public static void logIpLocatorEvent(Connection conn, IpLocatorEventDTO ipLocatorEventDTO) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT INTO MIS.IPLOCATOR_EVENT_LOG " +
					"(ID, INSERT_TIMESTAMP, IP, IPLOC_ZIPCODE, IPLOC_COUNTRY, IPLOC_REGION, IPLOC_CITY, FDUSER_ID, FD_ZIPCODE, FD_STATE, FD_CITY, USER_AGENT, UA_HASH_PERCENT, IPLOC_ROLLOUT_PERCENT) " +
					"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");	

			ipLocatorEventDTO.setId(SequenceGenerator.getNextId(conn, "MIS", "IPLOCATOR_EVENT_LOG_SEQ"));
			ipLocatorEventDTO.setTimestamp(Calendar.getInstance().getTimeInMillis());
			
			int i = 1;
			SqlUtil.setString(ps, i++, ipLocatorEventDTO.getId()); 				//ID
			SqlUtil.setTimestamp(ps, i++, ipLocatorEventDTO.getTimestamp()); 	//INSERT_TIMESTAMP
			SqlUtil.setString(ps, i++, ipLocatorEventDTO.getIp());				//IP
			SqlUtil.setString(ps, i++, ipLocatorEventDTO.getIpLocZipCode());	//IPLOC_ZIPCODE
			SqlUtil.setString(ps, i++, ipLocatorEventDTO.getIpLocCountry());	//IPLOC_COUNTRY
			SqlUtil.setString(ps, i++, ipLocatorEventDTO.getIpLocRegion());		//IPLOC_REGION
			SqlUtil.setString(ps, i++, ipLocatorEventDTO.getIpLocCity());		//IPLOC_CITY
			SqlUtil.setString(ps, i++, ipLocatorEventDTO.getFdUserId());		//FDUSER_ID
			SqlUtil.setString(ps, i++, ipLocatorEventDTO.getFdZipCode());		//FD_ZIPCODE
			SqlUtil.setString(ps, i++, ipLocatorEventDTO.getFdState());			//FD_STATE
			SqlUtil.setString(ps, i++, ipLocatorEventDTO.getFdCity());			//FD_CITY
			SqlUtil.setString(ps, i++, ipLocatorEventDTO.getUserAgent());		//USER_AGENT
			SqlUtil.setInt(ps, i++, ipLocatorEventDTO.getUaHashPercent());		//UA_HASH_PERCENT
			SqlUtil.setInt(ps, i++, ipLocatorEventDTO.getIplocRolloutPercent());//IPLOC_ROLLOUT_PERCENT

			ps.executeUpdate();
			
		} catch (Exception e) {
			LOGGER.error("Error in logIpLocatorEvent", e);
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch (Exception e1) {
				LOGGER.error("Error closing preparedstatement for logIpLocatorEvent", e1);
			}
		}
	}
	
	public static IpLocatorEventDTO loadIpLocatorEvent (Connection conn, String fdUserId) {
		
		IpLocatorEventDTO ipLocatorEventDTO = new IpLocatorEventDTO();
		PreparedStatement ps = null;
		ResultSet rs = null; 
		
		try {
			ps = conn.prepareStatement("SELECT ID, INSERT_TIMESTAMP, IP, IPLOC_ZIPCODE, IPLOC_COUNTRY, IPLOC_REGION, IPLOC_CITY, FDUSER_ID, FD_ZIPCODE, FD_STATE, FD_CITY, USER_AGENT, UA_HASH_PERCENT, IPLOC_ROLLOUT_PERCENT " +
					"FROM MIS.IPLOCATOR_EVENT_LOG WHERE FDUSER_ID = ?");	
			
			ps.setString(1, fdUserId);
			rs = ps.executeQuery();
			
			if (rs.next()) {
				ipLocatorEventDTO.setId(rs.getString("ID"));

				Time timestamp = rs.getTime("INSERT_TIMESTAMP");
				if (timestamp!=null){
					ipLocatorEventDTO.setTimestamp(timestamp.getTime());
				}
				
				ipLocatorEventDTO.setIp(rs.getString("IP"));
	    		ipLocatorEventDTO.setIpLocZipCode(rs.getString("IPLOC_ZIPCODE"));
	    		ipLocatorEventDTO.setIpLocCountry(rs.getString("IPLOC_COUNTRY"));
	    		ipLocatorEventDTO.setIpLocRegion(rs.getString("IPLOC_REGION"));
	    		ipLocatorEventDTO.setIpLocCity(rs.getString("IPLOC_CITY"));
		    	ipLocatorEventDTO.setFdUserId(rs.getString("FDUSER_ID"));
		    	ipLocatorEventDTO.setFdZipCode(rs.getString("FD_ZIPCODE"));
			    ipLocatorEventDTO.setFdState(rs.getString("FD_STATE"));
			    ipLocatorEventDTO.setFdCity(rs.getString("FD_CITY"));
	    		ipLocatorEventDTO.setUserAgent(rs.getString("USER_AGENT"));
	    		ipLocatorEventDTO.setUaHashPercent(rs.getInt("UA_HASH_PERCENT"));
	    		ipLocatorEventDTO.setIplocRolloutPercent(rs.getInt("IPLOC_ROLLOUT_PERCENT"));
			}
			
			
		} catch (Exception e) {
			LOGGER.error("Error in loadIpLocatorEvent", e);
		} finally {
			try {
				if(rs != null) {
					rs.close();
				}
			} catch (SQLException e1) {
				LOGGER.error("Error closing preparedstatement for loadIpLocatorEvent", e1);
			}
			try {
				if(ps != null) {
					ps.close();
				}
			} catch (SQLException e1) {
				LOGGER.error("Error closing preparedstatement for loadIpLocatorEvent", e1);
			}
		}
		
		return ipLocatorEventDTO;
	}
	private final static String PROFILE_FEATURE_CHECK_QUERY = "select p.profile_value from cust.customer c, cust.fdcustomer fdc, cust.profile p "+
			                                                  " where c.id=? and c.id=fdc.erp_customer_id and P.CUSTOMER_ID=fdc.id "+
			                                                  " and P.PROFILE_NAME=?";//'siteFeature.Paymentech'
	public static boolean isFeatureEnabled(Connection conn, String customerId, String feature)  throws SQLException{
		boolean isFeatureEnabled = false;
		PreparedStatement ps = conn.prepareStatement(PROFILE_FEATURE_CHECK_QUERY);
			ps.setString(1, customerId);
			ps.setString(2, feature);
			ResultSet rs = ps.executeQuery();
	
			if (rs.next()) {
				isFeatureEnabled = "true".equalsIgnoreCase(rs.getString(1))?true:false;
			}
			rs.close();
			ps.close();
		
		return isFeatureEnabled;
	}
	

	
	
	private static final String ORDERSIZE_ESTIMATION_QUERY = "select Ceil(Avg(tbl.NUM_REGULAR_CARTONS)) CCOUNT, " +
			"Ceil(Avg(tbl.NUM_FREEZER_CARTONS)) FCOUNT, Ceil(Avg(tbl.NUM_ALCOHOL_CARTONS)) ACOUNT " +
			"from (select NUM_REGULAR_CARTONS, NUM_FREEZER_CARTONS, NUM_ALCOHOL_CARTONS from cust.sale s " +
			"where s.CUSTOMER_ID = ? and s.STATUS = 'STL' and s.TYPE = 'REG' order by s.CROMOD_DATE desc) tbl where rownum <= ?";
	
	
	public static CustomerAvgOrderSize getHistoricOrderSize(Connection conn,
			String customerId) throws SQLException {

		PreparedStatement ps = conn
				.prepareStatement(ORDERSIZE_ESTIMATION_QUERY);
		ps.setString(1, customerId);
		ps.setInt(2, 5);

		ResultSet rs = ps.executeQuery();

		if (rs.next()) {
			CustomerAvgOrderSize model = new CustomerAvgOrderSize();
			model.setNoOfCartons(rs.getInt("CCOUNT") + rs.getInt("ACOUNT"));
			model.setNoOfFreezers(rs.getInt("FCOUNT"));
			return model;

		}
		rs.close();
		ps.close();

		return null;
	}

	private static final String UPDATEORDER_MODIFYSTATE = "update cust.sale set IN_MODIFY = ?, LOCK_TIMESTAMP = SYSDATE where id = ?";
			
	public static void updateOrderInModifyState(Connection conn, String id) throws SQLException {
			LOGGER.info("LOCKING THE ORDER FOR MODIFICATION... "+id);
			PreparedStatement ps = conn.prepareStatement(UPDATEORDER_MODIFYSTATE);
			ps.setString(1, "X");
			ps.setString(2, id);
			int rowcount = ps.executeUpdate();
			if(rowcount != 0)
				LOGGER.info("LOCKED THE ORDER FOR MODIFICATION SUCCESSFUL... "+id);
			ps.close();
		
	}

	private static final String IS_READY_FOR_PICK = "select 1 from cust.sale where IN_MODIFY is null AND status in ('AUT', 'AVE', 'SUB', 'PRC', 'PNA') and id = ?";

	public static boolean isReadyForPick(Connection conn, String id) throws SQLException {


		PreparedStatement ps = conn
				.prepareStatement(IS_READY_FOR_PICK);
		ps.setString(1, id);

		ResultSet rs = ps.executeQuery();

		if (rs.next()) {
			LOGGER.info("ORDER IS READY FOR PICKING... "+id);
			return true;
		}
		rs.close();
		ps.close();

		return false;
	
	}

	private static final String UPDATE_ORDER_INPROCESS = "update cust.sale set status = decode(status, 'SUB', 'PNA', 'PRC') where status in ('AUT', 'AVE', 'SUB') and id = ?";
	
	public static void updateOrderInProcess(Connection conn, String orderNum) throws SQLException {

		PreparedStatement ps = conn.prepareStatement(UPDATE_ORDER_INPROCESS);
		ps.setString(1, orderNum);
		int rowcount = ps.executeUpdate();
		if(rowcount != 0)
			LOGGER.info("UPDATE THE ORDER IN PROCESS SUCCESSFUL... "+orderNum);
		ps.close();
		
	}
	
	private static final String UNLOCKORDER_MODIFYSTATE = "update cust.sale set IN_MODIFY = NULL where id = ?";


	public static void releaseModificationLock(Connection conn, String orderId) throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement(UNLOCKORDER_MODIFYSTATE);
		ps.setString(1, orderId);
		int rowcount = ps.executeUpdate();
		if(rowcount != 0)
			LOGGER.info("UNLOCK THE ORDER FROM MODIFICATION SUCCESSFUL... "+orderId);
		ps.close();
	
	
	}

	public static boolean storeFDTCAgreeDate(Connection conn,String erpCustomerPK, Date fdTcAgreeDate) {

		boolean status = true;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update CUST.CUSTOMERINFO set FD_TC_AGREE_DATE=?,FD_TC_AGREE=? where customer_id=?");	
			ps.setTimestamp(1, new Timestamp(fdTcAgreeDate.getTime()));
			ps.setString(2, "X");
			ps.setString(3, erpCustomerPK);
			ps.execute();
		} catch (Exception e) {
			LOGGER.error("Error updating FD_TC_AGREE_DATE date", e);
			status=false;
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch (Exception e1) {}
		}
		return status;
		
	}
	public static boolean storeFDTCAgreeDate(Connection conn,String fdCustomerPK, String ackType) {

		boolean status = true;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("UPDATE CUST.FDCUSTOMER_ESTORE set TC_AGREE_DATE=?, TC_AGREE=? WHERE FDCUSTOMER_ID=? AND  e_store=?");	
			ps.setTimestamp(1, new Timestamp(new Date().getTime()));
			ps.setString(2, "X");
			ps.setString(3, fdCustomerPK);
			ps.setString(4, ackType);
			int count = ps.executeUpdate();
			if(count <= 0){
				ps = conn.prepareStatement("INSERT INTO CUST.FDCUSTOMER_ESTORE (FDCUSTOMER_ID, E_STORE, TC_AGREE_DATE, TC_AGREE) values (?,?,?,?)");
				ps.setString(1, fdCustomerPK);
				ps.setString(2, ackType);
				ps.setTimestamp(3, new Timestamp(new Date().getTime()));
				ps.setString(4, "X");
				ps.execute();			
			}
		} catch (Exception e) {
			LOGGER.error("Error updating FD_TC_AGREE IN FDCUSTOMER_ESTORE", e);
			status=false;
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch (Exception e1) {}
		}
		return status;
		
	}
	
	
	public static   Collection<ErpAddressModel> getParentAddresscheck(Connection conn, ErpAddressModel deliveryAddressModel )throws SQLException {

		PreparedStatement ps = null;
		ErpAddressModel addressModel=null;
			ps = conn.prepareStatement("select a.id, a.apartment, a.scrubbed_address, a.address1 from cust.address a where  a.customer_id=? and a.zip=? and a.city=?");	
					ps.setString(1, deliveryAddressModel.getCustomerId());
					ps.setString(2, deliveryAddressModel.getZipCode());
					ps.setString(3, deliveryAddressModel.getCity());
			ResultSet rs = ps.executeQuery();
			Collection<ErpAddressModel> addressList = new  ArrayList<ErpAddressModel>();
			while (rs.next()) {
				addressModel = new ErpAddressModel();					
				addressModel.setApartment(rs.getString("APARTMENT"));
				addressModel.setScrubbedStreet(rs.getString("SCRUBBED_ADDRESS"));
				addressModel.setPK(new PrimaryKey(rs.getString("ID")));
				addressModel.setAddress1((rs.getString("ADDRESS1")));
				addressList.add(addressModel);	
					}
			rs.close();
			ps.close();
			
			return addressList;
	}
	
	
	public static int getAddonOrdersCount(String orderId, Connection conn)throws SQLException{
		
		int addOnOrderCountOfParent=0;
		PreparedStatement pstmt=null;
				pstmt = conn.prepareStatement("select count(1) from CUST.SALE S, CUST.SALESACTION SA, CUST.PAYMENTINFO PI where S.ID = SA.SALE_ID AND S.CROMOD_DATE = SA.ACTION_DATE AND" +
							" SA.ACTION_TYPE IN ('CRO','MOD') AND S.TYPE = 'REG' AND SA.ID = PI.SALESACTION_ID AND  PI.ON_FD_ACCOUNT='O' AND PI.REFERENCED_ORDER = ? GROUP BY PI.ON_FD_ACCOUNT "); 
				pstmt.setString(1, orderId);
				ResultSet rs = pstmt.executeQuery();
				
				if(rs.next()){	
					addOnOrderCountOfParent=rs.getInt(1);
				 }
			rs.close();
			pstmt.close();
		return addOnOrderCountOfParent;
		
		}
	
}

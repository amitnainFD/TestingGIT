package com.freshdirect.fdstore.referral.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.jdbc.OracleTypes;

import org.apache.log4j.Category;

import com.freshdirect.affiliate.ErpAffiliate;
import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.customer.ErpCustomerCreditModel;
import com.freshdirect.fdstore.referral.ManageInvitesModel;
import com.freshdirect.fdstore.referral.ReferralPromotionModel;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * @author skanury
 * @created 01/08/2011
 * 
 */
public class FDReferAFriendDAO {
	private static final Category LOGGER = LoggerFactory
			.getInstance(FDReferAFriendDAO.class);
	
	public static final String GET_REFRRAL_PROMO = "select RP.ID, RP.GIVE_TEXT, RP.GET_TEXT, RP.REFERRAL_FEE, P.AUDIENCE_DESC, "
		    + "RP.SHARE_HEADER, RP.SHARE_TEXT, RP.GIVE_HEADER, RP.GET_HEADER, "
		    + "rp.FB_IMAGE_PATH, rp.FB_HEADLINE, rp.FB_TEXT, rp.TWITTER_TEXT, rp.RL_PAGE_TEXT, rp.RL_PAGE_LEGAL, "
		    + "rp.INV_EMAIL_SUBJECT, rp.INV_EMAIL_OFFER_TEXT, rp.INV_EMAIL_TEXT, rp.INV_EMAIL_LEGAL, "
		    + "rp.REF_CRE_EMAIL_SUB, rp.REF_CRE_EMAIL_TEXT, rp.sa_image_path " 
			+ "from CUST.REFERRAL_PRGM rp, "
			+ "CUST.REFERRAL_CUSTOMER_LIST rcl, "
			+ "CUST.PROMOTION_NEW p "
			+ "where RCL.ERP_CUSTOMER_ID = ? "
			+ "and   RCL.REFERAL_PRGM_ID = RP.ID "
			+ "and   trunc(RP.EXPIRATION_DATE) > trunc(sysdate) "
			+ "and   RP.PROMOTION_ID = P.ID "
			+ "and   P.STATUS = 'LIVE' "
			+ "and   trunc(sysdate) between P.START_DATE and P.EXPIRATION_DATE "
			+ "and   (rp.Delete_flag is null or rp.delete_flag != 'Y')";
	
	private static final String GET_DEFAULT_REFERRAL_PROMO = "select RP.ID, RP.GIVE_TEXT, RP.GET_TEXT, RP.REFERRAL_FEE, P.AUDIENCE_DESC, "
			+ "RP.SHARE_HEADER, RP.SHARE_TEXT, RP.GIVE_HEADER, RP.GET_HEADER, "
			+ "rp.FB_IMAGE_PATH, rp.FB_HEADLINE, rp.FB_TEXT, rp.TWITTER_TEXT, rp.RL_PAGE_TEXT, rp.RL_PAGE_LEGAL, "
			+ "rp.INV_EMAIL_SUBJECT, rp.INV_EMAIL_OFFER_TEXT, rp.INV_EMAIL_TEXT, rp.INV_EMAIL_LEGAL, "
			+ "rp.REF_CRE_EMAIL_SUB, rp.REF_CRE_EMAIL_TEXT, rp.sa_image_path "
			+ "from CUST.REFERRAL_PRGM rp, "
			+ "CUST.PROMOTION_NEW p "
			+ "where trunc(RP.EXPIRATION_DATE) > trunc(sysdate) "
			+ "and    RP.PROMOTION_ID = P.ID "
			+ "and    P.STATUS = 'LIVE' "
			+ "and    trunc(sysdate) between P.START_DATE and P.EXPIRATION_DATE "
			+ "and    RP.DEFAULT_PROMO = 'Y' "
			+ "and    (rp.Delete_flag is null or rp.delete_flag != 'Y')";

	public static ReferralPromotionModel getReferralPromotionDetails(
			Connection conn, String userId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(GET_REFRRAL_PROMO);
			ps.setString(1, userId);
			rs = ps.executeQuery();
			if (rs.next()) {
				return loadResultSet(rs);
			} else {
				//Get default promo
				ps = conn.prepareStatement(GET_DEFAULT_REFERRAL_PROMO);
				rs = ps.executeQuery();
				if (rs.next()) {
					return loadResultSet(rs);
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
		}
		return null;
	}
	
	private static final String GET_REFERRAL_PROMO_BY_ID = "select RP.ID, RP.GIVE_TEXT, RP.GET_TEXT, RP.REFERRAL_FEE, ' ' as AUDIENCE_DESC, " + 
            "RP.SHARE_HEADER, RP.SHARE_TEXT, RP.GIVE_HEADER, RP.GET_HEADER, " + 
            "rp.FB_IMAGE_PATH, rp.FB_HEADLINE, rp.FB_TEXT, rp.TWITTER_TEXT, rp.RL_PAGE_TEXT, rp.RL_PAGE_LEGAL, " +
            "rp.INV_EMAIL_SUBJECT, rp.INV_EMAIL_OFFER_TEXT, rp.INV_EMAIL_TEXT, rp.INV_EMAIL_LEGAL, " +
            "rp.REF_CRE_EMAIL_SUB, rp.REF_CRE_EMAIL_TEXT, rp.sa_image_path " +
            "from CUST.REFERRAL_PRGM rp " +
            "where RP.ID =  ?";
	
	public static ReferralPromotionModel getReferralPromotionDetailsById(
			Connection conn, String rpid) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(GET_REFERRAL_PROMO_BY_ID);
			ps.setString(1, rpid);
			rs = ps.executeQuery();
			if (rs.next()) {
				return loadResultSet(rs);
			} 
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
		}
		return null;
	}
	
	private static ReferralPromotionModel loadResultSet(ResultSet rs) throws SQLException {
		ReferralPromotionModel rpm = new ReferralPromotionModel();		
		rpm.setReferral_prgm_id(rs.getString("ID"));
		rpm.setGive_text(rs.getString("GIVE_TEXT"));
		rpm.setGet_text(rs.getString("GET_TEXT"));
		rpm.setReferral_fee(rs.getInt("REFERRAL_FEE"));
		rpm.setAudience_desc(rs.getString("AUDIENCE_DESC"));
		rpm.setShareHeader(rs.getString("SHARE_HEADER"));
		rpm.setShareText(rs.getString("SHARE_TEXT"));
		rpm.setGetHeader(rs.getString("GET_HEADER"));
		rpm.setGiveHeader(rs.getString("GIVE_HEADER"));
		rpm.setFbFile(rs.getString("FB_IMAGE_PATH"));
		rpm.setFbHeadline(rs.getString("FB_HEADLINE"));
		rpm.setFbText(rs.getString("FB_TEXT"));
		rpm.setTwitterText(rs.getString("TWITTER_TEXT"));
		rpm.setReferralPageText(rs.getString("RL_PAGE_TEXT"));
		rpm.setReferralPageLegal(rs.getString("RL_PAGE_LEGAL"));
		rpm.setInviteEmailSubject(rs.getString("INV_EMAIL_SUBJECT"));
		rpm.setInviteEmailOfferText(rs.getString("INV_EMAIL_OFFER_TEXT"));
		rpm.setInviteEmailText(rs.getString("INV_EMAIL_TEXT"));
		rpm.setInviteEmailLegal(rs.getString("INV_EMAIL_LEGAL"));
		rpm.setReferralCreditEmailSubject(rs.getString("REF_CRE_EMAIL_SUB"));
		rpm.setReferralCreditEmailText(rs.getString("REF_CRE_EMAIL_TEXT"));
		rpm.setSiteAccessImageFile(rs.getString("sa_image_path"));
		return rpm;
	}
	
	private static final String INSERT_INVITES = "insert into CUST.CUSTOMER_INVITES (REFERRAL_CUSTOMER_ID, FRIENDS_EMAIL, SENT_DATE) values(?,?,?)";

	public static void saveInvite(Connection conn, String recipient,
			String customerId) {
			PreparedStatement ps = null;
			try {
				ps = conn.prepareStatement(INSERT_INVITES);
				ps.setString(1, customerId);
				ps.setString(2, recipient.toUpperCase());
				ps.setTimestamp(3, new java.sql.Timestamp(new java.util.Date().getTime()));
				ps.execute();
			} catch (Exception e) {				
				//do nothing for exception here.
			} finally {
				try {
					if (ps != null)
						ps.close();
				} catch (Exception e1) {}
			}
	}
	
	private static final String GET_INVITE_LIST = "select ci.CREDIT_ISSUED, ci.FRIENDS_EMAIL, TO_CHAR (ci.sent_date, 'MM/DD/YYYY HH12:MI AM') SENT_DATE, " + 
											    "decode(NVL(c.ID, '-1'), c.ID, 'Y', 'N') status, c.ID, CI.CREDIT_ISSUED_DATE, FC.REFERER_CUSTOMER_ID " +
											    "from cust.CUSTOMER_INVITES ci, " +
											    "CUST.CUSTOMER c, " +
											    "CUST.FDCUSTOMER fc " +
											    "where ci.referral_customer_id = ? " +
											    "and   upper(CI.FRIENDS_EMAIL) = upper(C.USER_ID (+)) " +
											    "and   C.ID = FC.ERP_CUSTOMER_ID (+) " + 
											    "order by ci.sent_date desc";
	
	public static List<ManageInvitesModel> getManageInvites(Connection conn, String customerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rset = null;
		List<ManageInvitesModel> mimList = new ArrayList<ManageInvitesModel>();
		try {
			ps = conn.prepareStatement(GET_INVITE_LIST);
			ps.setString(1, customerId);
			rset = ps.executeQuery();
			while(rset.next()) {
				ManageInvitesModel mim = new ManageInvitesModel();
				mim.setRecipientEmail(rset.getString("FRIENDS_EMAIL"));
				mim.setSentDate(rset.getString("SENT_DATE"));
				mim.setCredit(rset.getString("CREDIT_ISSUED"));
				String registered = rset.getString("status");
				if(registered.equals("Y")) {
					//User is registered
					mim.setStatus("Signed Up");
					String referee_custId = rset.getString("ID");
					
					if(!customerId.equals(rset.getString("REFERER_CUSTOMER_ID"))) {
						//Customer is already registered via other RAF or regular signup
						mim.setStatus("Ineligible");
					} else {
					
						//Check if the user has completed order and used the referral promotion
						if(checkIfUserUsedReferralPromotion(conn, referee_custId) != null) {
							//The referrer used the referral promotion
							mim.setStatus("Offered Redeemed");
						} else {
							if(userUsedRegularPromotion(conn, referee_custId)) {
								mim.setStatus("Offered Not Redeemed");
							}
						}
					}
				} else {
					//User is not registered. 
					mim.setStatus("No Response");
				}
				mimList.add(mim);
			}
		} finally {
			if (ps != null)
				ps.close();
			if (rset != null)
				rset.close();
		}
		return mimList;
	}
	
	private static final String CHECK_PROMO_DETAILS = "select pp.sale_id from " +
                                                        "CUST.PROMOTION_PARTICIPATION pp, " + 
                                                        "CUST.REFERRAL_PRGM rp, " +
                                                        "CUST.SALE s " +
                                                        "where s.CUSTOMER_ID = ? " +
                                                          "and     PP.SALE_ID = S.ID " +
                                                          "and     S.STATUS = 'STL' " +
                                                          "and     RP.PROMOTION_ID = PP.PROMOTION_ID"; 
	
	public static String checkIfUserUsedReferralPromotion(Connection conn, String customerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rset = null;
		try {
			ps = conn.prepareStatement(CHECK_PROMO_DETAILS);
			ps.setString(1, customerId);
			rset = ps.executeQuery();
			if(rset.next()) {
				String sale_id = rset.getString(1);
				return sale_id;
			}
		} finally {
			if (ps != null)
				ps.close();
			if (rset != null)
				rset.close();
		}
		return null;
	}
	
	private static final String CHECK_SALE_DETAILS = "select pp.sale_id " + 
								"from  CUST.PROMOTION_PARTICIPATION pp, " +
								"CUST.SALE s " +
								"where s.customer_id =  ? " +
								"and     s.id = PP.SALE_ID " +
								"and     s.status = 'STL' " +
								"and     not exists (select 1 from CUST.REFERRAL_PRGM rp where RP.PROMOTION_ID = pp.promotion_id and trunc(RP.EXPIRATION_DATE) > trunc(sysdate))";  

	public static boolean userUsedRegularPromotion(Connection conn, String customerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rset = null;
		try {
			ps = conn.prepareStatement(CHECK_SALE_DETAILS);
			ps.setString(1, customerId);
			rset = ps.executeQuery();
			if(rset.next()) {
				return true;
			}
		} finally {
			if (ps != null)
				ps.close();
			if (rset != null)
				rset.close();
		}
		return false;
	}
	
	public static List<ManageInvitesModel> getManageInvitesForCRM(Connection conn, String customerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rset = null;
		List<ManageInvitesModel> mimList = new ArrayList<ManageInvitesModel>();
		try {
			ps = conn.prepareStatement(GET_INVITE_LIST);
			ps.setString(1, customerId);
			rset = ps.executeQuery();
			while(rset.next()) {
				ManageInvitesModel mim = new ManageInvitesModel();
				mim.setRecipientEmail(rset.getString("FRIENDS_EMAIL"));
				mim.setSentDate(rset.getString("SENT_DATE"));
				mim.setCredit(rset.getString("CREDIT_ISSUED"));
				String cid = rset.getString("ID");
				String ref_cust_id = rset.getString("REFERER_CUSTOMER_ID");
				mim.setRecipientCustId(cid);
				mim.setCreditIssuedDate(rset.getDate("CREDIT_ISSUED_DATE"));
				if(cid != null && ref_cust_id != null) {
					//User is registered via referral link
					mim.setStatus("Signed Up");
					//Check if the user has completed order and used the referral promotion
					String sale_id = checkIfUserUsedReferralPromotion(conn, cid);
					if(sale_id != null) {
						//The referrer used the referral promotion
						mim.setStatus("Offer Redeemed");
						mim.setSaleId(sale_id);
					} else {
						//Check if there is a settled order
						if(checkIfUserHasSettledOrder(conn, cid)) {
							//User has order that is settled without using promotion
							mim.setStatus("Offer Not Redeemed");
						}
					}
				} else if(cid != null && ref_cust_id == null) {
					//Existing customer
					mim.setStatus("Existing Customer");
				} else if(cid == null) {
					//No response
					mim.setStatus("No Response");
				}					
				mimList.add(mim);
			}
		} finally {
			if (ps != null)
				ps.close();
			if (rset != null)
				rset.close();
		}
		return mimList;
	}
	
	private static final String CHECK_FIRST_ORDER = "select count(*) from " + 
												    "CUST.SALE s,  " +
												    "CUST.CUSTOMER c " +
												    "where C.ID = ? " +                                                         
												    "and     S.CUSTOMER_ID = C.ID " +
												    "and     S.STATUS = 'STL'";
	
	public static boolean checkIfUserHasSettledOrder(Connection conn, String customerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rset = null;
		try {
			ps = conn.prepareStatement(CHECK_FIRST_ORDER);
			ps.setString(1, customerId);
			rset = ps.executeQuery();
			if(rset.next()) {
				int cnt = rset.getInt(1);
				if(cnt > 0)
					return true;
			}
		} finally {
			if (ps != null)
				ps.close();
			if (rset != null)
				rset.close();
		}
		return false;
	}
	
	private static final String GET_CREDIT_LIST = 
							"select cc.create_date, to_char(CC.CREATE_DATE, 'MM/DD/YYYY') formatted_create_date, " + 
							    "decode(CC.DEPARTMENT, 'RAF', 'Referral Credit', 'Store Credit') type, " + 
                                "C.SALE_ID as SALE_ID, CC.ORIGINAL_AMOUNT as Amount " +
                                "from CUST.CUSTOMERCREDIT cc, " +
                                "CUST.COMPLAINT c " +
                                "where CC.COMPLAINT_ID = C.ID " +   
                                "and     cc.CUSTOMER_ID = ? " +
                            "UNION ALL " +
                            "select SA.ACTION_DATE, to_char(SA.ACTION_DATE, 'MM/DD/YYYY') as formatted_create_date, " +
                                "'Redemption' as type, " +
                                "s.id as SALE_ID, AC.AMOUNT as Amount from " + 
                                "cust.customercredit cc, " +
                                "CUST.APPLIEDCREDIT ac, " +
                                "CUST.SALESACTION sa, " +
                                "cust.sale s " +
                                "where       cc.CUSTOMER_ID =  ? " +
                                "and  CC.ID = AC.CUSTOMERCREDIT_ID " + 
                                "and  AC.SALESACTION_ID = sa.id " +
                                "and S.CROMOD_DATE=sa.action_date " +
                                "and sa.action_type in ('CRO','MOD') " +
                                "and  sa.sale_id = s.id and S.CUSTOMER_ID=sa.customer_id  and s.status!='CAN' " + 
                            "order by 1 desc";

	
	public static List<ErpCustomerCreditModel> getUserCredits(Connection conn,
			String customerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ErpCustomerCreditModel> cmList = new ArrayList<ErpCustomerCreditModel>();
		try {
			ps = conn.prepareStatement(GET_CREDIT_LIST);
			ps.setString(1, customerId);
			ps.setString(2, customerId);
			rs = ps.executeQuery();
			double remaining_amt = 0;
			while (rs.next()) {
				ErpCustomerCreditModel cm = new ErpCustomerCreditModel();
				cm.setcDate(rs.getString("formatted_create_date"));
				cm.setDepartment(rs.getString("TYPE"));
				cm.setAmount(rs.getDouble("AMOUNT"));
				cm.setSaleId(rs.getString("SALE_ID"));
				cmList.add(cm);
			}
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return cmList;
	}
	
	public final static String GET_REFERRAL_CUSTID = "select customer_id from cust.referral_link where referral_link = ?";
	
	public static final String UPDATE_FDCUSTOMER = "update cust.fdcustomer set referer_customer_id = ? where erp_customer_id = ?";
	
	public static final String GET_REF_PRGM_ID = "select rp.ID " +
            "from CUST.REFERRAL_PRGM rp, " +
                 "CUST.REFERRAL_CUSTOMER_LIST rcl " +
            "where RCL.ERP_CUSTOMER_ID = ? " +
            "and   RCL.REFERAL_PRGM_ID = RP.ID " +
            "and   trunc(RP.EXPIRATION_DATE) > trunc(sysdate) " +
            "and   (rp.Delete_flag is null or rp.delete_flag != 'Y')";
	
	public static final String GET_DEFAULT_REF_PRGM_ID = "select rp.ID " +
    "from  CUST.REFERRAL_PRGM rp " +
    "where rp.default_promo = 'Y' " +
    "and   trunc(RP.EXPIRATION_DATE) > trunc(sysdate) " +
    "and   (rp.Delete_flag is null or rp.delete_flag != 'Y')";
	
	private static final String GET_CUST_INVITE_REC = "select CI.FRIENDS_EMAIL from " +
													  "CUST.CUSTOMER_INVITES ci " +													  
													  "where  upper(CI.FRIENDS_EMAIL) = upper(?) " + 
													  "and    CI.REFERRAL_CUSTOMER_ID = ?";	
	
	private static final String UPD_CUST_INVITE_REC = "update CUST.CUSTOMER_INVITES set FRIENDS_CUSTOMER_ID=? where upper(FRIENDS_EMAIL)=upper(?) and referral_customer_id=?";
	
	private static final String INS_CUST_INVITE_REC = "insert into CUST.CUSTOMER_INVITES(FRIENDS_CUSTOMER_ID, FRIENDS_EMAIL, REFERRAL_CUSTOMER_ID, SENT_DATE, REFERRED_REF_PRGM_ID) " +
													  "values(?,?,?,sysdate,?)";
	
	/*
	 * This method is to record the referral information in the newly
	 * registered customer record. It is done in 3 steps.
	 * (1) find referral's customer Id
	 * (2) find valid referral program at current time
	 * (3) save referral info in the fdcustomer record
	 */
	public static String recordReferral(String customerId, String referralId, String customerEmail, Connection conn) throws SQLException {
		LOGGER.debug("Recording the referral for customerId: " + customerId + "--referralId:" + referralId);
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		String refCustomerId = null;
		String referralPrgmId = null;
		try {
			ps = conn.prepareStatement(GET_REFERRAL_CUSTID);
			ps.setString(1, referralId);
			rs = ps.executeQuery();
			if (rs.next()) {
				refCustomerId = rs.getString(1);
			}
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		
		if(refCustomerId != null) {
			//record the referral details
			try {
				ps = conn.prepareStatement(GET_REF_PRGM_ID);
				ps.setString(1, refCustomerId);
				rs = ps.executeQuery();
				if (rs.next()) {
					referralPrgmId = rs.getString(1);
				} else {
					ps1 = conn.prepareStatement(GET_DEFAULT_REF_PRGM_ID);
					rs1 = ps1.executeQuery();
					if (rs1.next()) {
						referralPrgmId = rs1.getString(1);
					}
				}
			} finally {
				if (ps != null)
					ps.close();
				if (rs != null)
					rs.close();
				if (ps1 != null)
					ps1.close();
				if (rs1 != null)
					rs1.close();
			}
		}
		
		try {
			ps = conn.prepareStatement(UPDATE_FDCUSTOMER);
			ps.setString(1, refCustomerId);
			ps.setString(2, customerId);
			ps.execute();
		} finally {
			if (ps != null)
				ps.close();
		}
		
		//update customer info in invites lists
		try {
			ps = conn.prepareStatement(GET_CUST_INVITE_REC);
			ps.setString(1, customerEmail);
			ps.setString(2, refCustomerId);
			rs = ps.executeQuery();
			String email = null;
			if(rs.next()) {
				email = rs.getString(1);		
			}
			if(email != null) {
				//update
				ps = conn.prepareStatement(UPD_CUST_INVITE_REC);				
			} else {
				//insert
				ps = conn.prepareStatement(INS_CUST_INVITE_REC);
				ps.setString(4, referralPrgmId);
			}
			ps.setString(1, customerId);
			ps.setString(2, customerEmail);
			ps.setString(3, refCustomerId);
			ps.execute();
			return refCustomerId;
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
	}
	
	public static final String GET_DUPE_USER = "select ID from cust.customer where uppeR(user_id) = uppeR(?)";
	
	public static String dupeEmailAddress(String email, Connection conn) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(GET_DUPE_USER);
			ps.setString(1, email);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			}
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return null;
	}
	
	private static final String GET_TOTAL_CREDIT = "select sum(amount) from CUST.CUSTOMERCREDIT where CUSTOMER_ID = ? and department = 'RAF'";
	
	public static Double getAvailableCredit(Connection conn, String customerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(GET_TOTAL_CREDIT);
			ps.setString(1, customerId);
			rs = ps.executeQuery();
			if (rs.next()) {
				Double credit = new Double(rs.getDouble(1));
				return credit;
			}
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return new Double(0);
	}
	
	private static final String GET_STL_SALE = "select ID from cust.sale where customer_id = ? and status = 'STL'";
	
	public static Boolean getReferralDisplayFlag(Connection conn, String customerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(GET_STL_SALE);
			ps.setString(1, customerId);
			rs = ps.executeQuery();
			if (rs.next()) {				
				return new Boolean(true);
			}
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return new Boolean(false);
	}
	
	/*
	public static final String GET_SET_SALE_LIST =  "select distinct RP.PROMOTION_ID, S.ID as saleid, S.CUSTOMER_ID, F.REFERER_CUSTOMER_ID, f.ID as fdcustomerid from " + 
												    "cust.sale s, " +
													"CUST.PROMOTION_PARTICIPATION pp, " +
													"CUST.FDCUSTOMER f, " +
													"CUST.REFERRAL_PRGM rp, " +
													"CUST.CUSTOMER_INVITES ci " +
													"where S.STATUS = 'STL' " +
													"and     S.CUSTOMER_ID = F.ERP_CUSTOMER_ID " +
													"and     F.REFERER_CUSTOMER_ID is not null " +
													"and     S.ID = PP.SALE_ID " +
													"and     PP.PROMOTION_ID = RP.PROMOTION_ID " +
													"and     RP.EXPIRATION_DATE > sysdate " +
													"and     s.customer_id = CI.FRIENDS_CUSTOMER_ID " +
													"and     CI.CREDIT_ISSUED is null " + 
													"and     (rp.Delete_flag is null or rp.delete_flag != 'Y')";
	*/
	public static final String GET_SET_SALE_LIST = "select distinct RP.PROMOTION_ID, S.ID as saleid, S.CUSTOMER_ID, F.REFERER_CUSTOMER_ID, " +
												   "f.ID as fdcustomerid,sq1.erp_customer_id, sq1.ID, sq1.GIVE_TEXT, sq1.GET_TEXT, sq1.REFERRAL_FEE, " +
												   "sq1.AUDIENCE_DESC, sq1.SHARE_HEADER, sq1.SHARE_TEXT, sq1.GIVE_HEADER, sq1.GET_HEADER, "+ 
                                                   "sq1.FB_IMAGE_PATH, sq1.FB_HEADLINE, sq1.FB_TEXT, sq1.TWITTER_TEXT, sq1.RL_PAGE_TEXT, " +
												   "sq1.RL_PAGE_LEGAL, sq1.INV_EMAIL_SUBJECT,  sq1.INV_EMAIL_OFFER_TEXT, sq1.INV_EMAIL_TEXT, sq1.INV_EMAIL_LEGAL, " +
                                                   "sq1.REF_CRE_EMAIL_SUB, sq1.REF_CRE_EMAIL_TEXT, sq1.sa_image_path " +
                                                   "from " +
                                                   "cust.sale s, " + 
                                                   "CUST.PROMOTION_PARTICIPATION pp, " + 
                                                   "CUST.FDCUSTOMER f,  " +
                                                   "CUST.REFERRAL_PRGM rp,  " +
                                                   "CUST.CUSTOMER_INVITES ci ,  " +
                                                   "( " +
                                                            "select RCL.ERP_CUSTOMER_ID, RP.ID, RP.GIVE_TEXT, RP.GET_TEXT, RP.REFERRAL_FEE, P.AUDIENCE_DESC, " + 
                                                            "RP.SHARE_HEADER, RP.SHARE_TEXT, RP.GIVE_HEADER, RP.GET_HEADER,  " +
                                                            "rp.FB_IMAGE_PATH, rp.FB_HEADLINE, rp.FB_TEXT, rp.TWITTER_TEXT, rp.RL_PAGE_TEXT, rp.RL_PAGE_LEGAL, rp.INV_EMAIL_SUBJECT, " +    
                                                            "rp.INV_EMAIL_OFFER_TEXT, rp.INV_EMAIL_TEXT, rp.INV_EMAIL_LEGAL, rp.REF_CRE_EMAIL_SUB, rp.REF_CRE_EMAIL_TEXT, rp.sa_image_path  " +
                                                            "from CUST.REFERRAL_PRGM rp, " +
                                                            "CUST.REFERRAL_CUSTOMER_LIST rcl, " + 
                                                            "CUST.PROMOTION_NEW p " +
                                                            "where RCL.REFERAL_PRGM_ID = RP.ID " + 
                                                            "and   trunc(RP.EXPIRATION_DATE) > trunc(sysdate) " + 
                                                            "and   RP.PROMOTION_ID = P.ID " +
                                                            "and   P.STATUS = 'LIVE' " +
                                                            "and   trunc(sysdate) between P.START_DATE and P.EXPIRATION_DATE " + 
                                                            "and   (rp.Delete_flag is null or rp.delete_flag != 'Y') " +
                                                   ") sq1 " +
                                                   "where S.STATUS = 'STL' " + 
                                                   "and     S.CUSTOMER_ID = F.ERP_CUSTOMER_ID " + 
                                                   "and     F.REFERER_CUSTOMER_ID is not null " +
                                                   "and     S.ID = PP.SALE_ID " +
                                                   "and     PP.PROMOTION_ID = RP.PROMOTION_ID " + 
                                                   "and     RP.EXPIRATION_DATE > sysdate " +
                                                   "and     s.customer_id = CI.FRIENDS_CUSTOMER_ID " + 
                                                   "and     CI.CREDIT_ISSUED is null " +
                                                   "and     (rp.Delete_flag is null or rp.delete_flag != 'Y') " +
                                                   "and    referer_customer_id = sq1.erp_customer_id (+)";
	
	public static List<ReferralPromotionModel> getSettledSales(Connection conn) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ReferralPromotionModel> list = new ArrayList<ReferralPromotionModel>();
		ReferralPromotionModel defaultRefPromo = getReferralDefaultPromotionDetails(conn);
		try {
			ps = conn.prepareStatement(GET_SET_SALE_LIST);
			rs = ps.executeQuery();
			while (rs.next()) {
				ReferralPromotionModel rpm = new ReferralPromotionModel();
				rpm.setPromotion_id(rs.getString("PROMOTION_ID"));
				rpm.setSaleId(rs.getString("saleid"));
				rpm.setCustomerId(rs.getString("CUSTOMER_ID"));
				rpm.setRefCustomerId(rs.getString("REFERER_CUSTOMER_ID"));
				rpm.setFDCustomerId(rs.getString("fdcustomerid"));
				System.out.println("now, is this null?" + rs.getString("REFERER_CUSTOMER_ID"));
				//ReferralPromotionModel refDetails = getReferralPromotionDetails(conn, rs.getString("REFERER_CUSTOMER_ID"));
				ReferralPromotionModel refDetails = null;
				if(rs.getString("ID") == null) {
					refDetails = defaultRefPromo;
				} else {
					refDetails = loadResultSet(rs);
				}
				System.out.println("Is this null?" + refDetails);
				rpm.setReferral_fee(refDetails.getReferral_fee());
				rpm.setReferral_prgm_id(refDetails.getReferral_prgm_id());	
				rpm.setReferralCreditEmailSubject(refDetails.getReferralCreditEmailSubject());
				rpm.setReferralCreditEmailText(refDetails.getReferralCreditEmailText());
				list.add(rpm);
			}
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return list;
	}

	private static final String GET_REF_LINK = "select referral_link from cust.referral_link where customer_id=?";
	
	private static final String GET_REF_NAME = "select lower(CI.FIRST_NAME || CI.LAST_NAME) name " + 
											   "from CUST.CUSTOMERINFO ci " + 
											   "where CI.CUSTOMER_ID = ?";
	
	private static final String INSERT_REF_LINK = "insert into cust.referral_link(customer_id, referral_link) values(?,?)";
	
	private static final String GET_REF_NAME_CNT = "select count(*) from cust.referral_link where upper(referral_link) like upper(?)";
	
	public static String getReferralLink(Connection conn, String customerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(GET_REF_LINK);
			ps.setString(1, customerId);
			rs = ps.executeQuery();
			if (rs.next()) {				
				return rs.getString(1);
			}
			
			//Generate the referral link if it does not exist
			ps = conn.prepareStatement(GET_REF_NAME);
			ps.setString(1, customerId);
			rs = ps.executeQuery();
			String ref_name = "";
			if(rs.next()) {
				ref_name = rs.getString(1);
			}
			
			//store this refname to referral link table
			LOGGER.debug("Inserting users refname: " + ref_name);
			try {
				ps = conn.prepareStatement(INSERT_REF_LINK);
				ps.setString(1, customerId);
				ps.setString(2, ref_name);
				ps.execute();
			} catch (SQLException e) {
				if (e.getErrorCode() == 1) {
					//Got a unique constraint. Get the count of the name
					ps = conn.prepareStatement(GET_REF_NAME_CNT);
					ps.setString(1, ref_name+"%");
					rs = ps.executeQuery();
					if(rs.next()) {
						int cnt = rs.getInt(1);
						ref_name = ref_name + (cnt + 1);
						LOGGER.debug("Inserting updated users refname: " + ref_name);
						ps = conn.prepareStatement(INSERT_REF_LINK);
						ps.setString(1, customerId);
						ps.setString(2, ref_name);
						ps.execute();
					}
				}
			}
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return null;
	}
	
	public static String getLatestSTLSale(Connection conn, String customerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select sale_id from cust.salesaction where action_type = 'STL' and customer_id = ? order by action_Date desc");
			ps.setString(1, customerId);
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return "";
	}
	
	public static void saveCustomerCredit(Connection conn, String referral_customer_id, String customer_id, int ref_fee, String sale, String complaintId, String refPrgmId) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update  CUST.CUSTOMER_INVITES set credit_issued = ?, credit_issued_date = sysdate, CREDIT_SALE_ID =? , COMPLAINT_ID = ?, CREDIT_ERND_REF_PRGM_ID=? " +
										"where referral_customer_id = ? and friends_customer_id = ?");
			ps.setInt(1, ref_fee);						
			ps.setString(2, sale);
			ps.setString(3, complaintId);
			ps.setString(4, refPrgmId);
			ps.setString(5, referral_customer_id);
			ps.setString(6, customer_id);
			ps.execute();
		} finally {
			if (ps != null)
				ps.close();
		}
	}
	
	public static boolean isCustomerReferred(Connection conn, String customer_id) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select F.REFERER_CUSTOMER_ID from cust.fdcustomer f where erp_customer_id = ?");
			ps.setString(1, customer_id);
			rs = ps.executeQuery();
			if(rs.next()) {
				String rc = rs.getString("REFERER_CUSTOMER_ID");
				if(rc != null && rc.length() > 0) 
					return true;
			}
		} finally {
			if (ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return false;
	}
	
	public static String updateFDUser(Connection conn, String customer_id, String zipCode, EnumServiceType serviceType) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("update cust.fduser set zipcode=?, service_type=? where fdcustomer_id = (select ID from cust.fdcustomer where erp_customer_id = ?)");
			ps.setString(1, zipCode);
			if(serviceType != null)
				ps.setString(2, serviceType.getName());
			else
				ps.setNull(2, OracleTypes.VARCHAR);
			ps.setString(3, customer_id);
			ps.execute();
			
			ps.close();
			
			ps = conn.prepareStatement("select ID from cust.fdcustomer where erp_customer_id = ?");
			ps.setString(1, customer_id);
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
		} finally {
			if (ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return "";
	}

	public static void updateCustomerInfo(Connection conn, String customerId, String firstName, String lastName) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update cust.customerinfo set first_name=?, last_name=? where customer_id = ?");
			ps.setString(1, firstName);
			ps.setString(2, lastName);
			ps.setString(3, customerId);
			ps.execute();
		} finally {
			if (ps != null)
				ps.close();
		}
	}
	
	public static void updateCustomerPW(Connection conn, String customerId, String pwdHash) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update cust.customer set passwordhash=? where id = ?");
			ps.setString(1, pwdHash);
			ps.setString(2, customerId);
			ps.execute();
		} finally {
			if (ps != null)
				ps.close();
		}
	}
	
	public static void updateFdCustomer(Connection conn, String customerId, String pwdHint) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update cust.fdcustomer set password_hint=? where erp_customer_id = ?");
			ps.setString(1, pwdHint);
			ps.setString(2, customerId);
			ps.execute();
		} finally {
			if (ps != null)
				ps.close();
		}
	}
	
	public static void storeFailedAttempt(Connection conn, String email, String dupeCustID, String zipCode, String firstName, String lastName, String referral, String reason) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("insert into CUST.RAF_FAILED_ATTEMPTS (EMAIL,CUSTOMER_ID,FIRST_NAME,LAST_NAME,ZIPCODE,RAF_URL,REASON,ROW_ADDED_DATE)" +
												" values(?,?,?,?,?,?,?,sysdate)");
			ps.setString(1, email);
			ps.setString(2, dupeCustID);
			ps.setString(3, firstName);
			ps.setString(4, lastName);
			ps.setString(5, zipCode);
			ps.setString(6, referral);
			ps.setString(7, reason);
			ps.execute();
		} finally {
			if (ps != null)
				ps.close();
		}
	}
	
	public static boolean isUniqueFNLNZipCombo(Connection conn, String firstName, String lastName, String zipCode, String customerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rset = null;
		try {
			if(customerId != null) {
				ps = conn.prepareStatement("select ci.customer_id from cust.customerinfo ci, cust.address a " +
										   "where uppeR(CI.FIRST_NAME) = upper(?) " +
										   "and   upper(CI.LAST_NAME) = uppeR(?) " +
										   "and   CI.CUSTOMER_ID = A.CUSTOMER_ID " +
										   "and   A.ZIP = ? " +
										   "and ci.customer_id != ?");
				ps.setString(4, customerId);
			} else {
				ps = conn.prepareStatement("select ci.customer_id from cust.customerinfo ci, cust.address a " +
						   "where uppeR(CI.FIRST_NAME) = upper(?) " +
						   "and   upper(CI.LAST_NAME) = uppeR(?) " +
						   "and   CI.CUSTOMER_ID = A.CUSTOMER_ID " +
						   "and   A.ZIP = ?");				
			}
			ps.setString(1, firstName);
			ps.setString(2, lastName);
			ps.setString(3, zipCode);
			rset = ps.executeQuery();
			if(rset.next())
				return false;
		} finally {
			if (ps != null)
				ps.close();
		}
		return true;		
	}

	public static String getReferralName(Connection conn, String referralId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rset = null;
		String refName = null;
		try {
			ps = conn.prepareStatement("select CI.FIRST_NAME || ' ' || CI.LAST_NAME " + 
									   "from cust.customerinfo ci, " +
									   "cust.referral_link rl " +
									   "where RL.REFERRAL_LINK = ? " +
									   "and RL.CUSTOMER_ID = CI.CUSTOMER_ID");
			ps.setString(1, referralId);
			rset = ps.executeQuery();
			if(rset.next())
				return rset.getString(1);
		} finally {
			if (ps != null)
				ps.close();
		}
		return null;	
	}
	
	public static boolean isReferreSignUpComplete(Connection conn, String email) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rset = null;
		try {
			/*ps = conn.prepareStatement( "select count(*) from " + 
										"cust.customer c, " +
										"cust.fdcustomer fc " +
										"where upper(c.user_id) = upper(?) " +
										"and c.id = fc.erp_customer_id " +
										"and FC.REFERER_CUSTOMER_ID is not null");
*/		ps = conn.prepareStatement( "select count(*) from " + 
		"cust.customer c, " +
		"cust.fdcustomer fc " +
		"where upper(c.user_id) = upper(?) " +
		"and c.id = fc.erp_customer_id " +
		"and FC.RAF_CLICK_ID is not null");
			
			ps.setString(1, email);
			rset = ps.executeQuery();
			while(rset.next()) {
				int cnt = rset.getInt(1);
				if(cnt > 0)
					return true;
			}
		} finally {
			if (ps != null)
				ps.close();
		}
		return false;	
	}
	
	public static String getCustomerId(Connection conn, String referralId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rset = null;
		String refName = null;
		try {
			ps = conn.prepareStatement("select customer_id from CUST.REFERRAL_LINK where referral_link = ?");
			ps.setString(1, referralId);
			rset = ps.executeQuery();
			if(rset.next())
				return rset.getString(1);
		} finally {
			if (ps != null)
				ps.close();
		}
		return null;	
	}
	
	public static ReferralPromotionModel getReferralDefaultPromotionDetails(Connection conn) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			//Get default promo
			ps = conn.prepareStatement(GET_DEFAULT_REFERRAL_PROMO);
			rs = ps.executeQuery();
			if (rs.next()) {
				return loadResultSet(rs);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
		}
		return null;
	}
	
	// TODO : need to add the condition to select only active referred amount

	private static final String GET_SETTELED_TRANSACTION = "SELECT (SELECT C.ID FROM  CUST.CUSTOMER  C WHERE C.USER_ID=RC.ADVOCATE_EMAIL) AS ADVOCATE_USER_ID,"
			+ " (SELECT TC.ID FROM  CUST.CUSTOMER  TC WHERE TC.USER_ID=RC.FRIEND_EMAIL) AS FRIEND_USER_ID,"
			+ " RC.ADVOCATE_EMAIL,RC.FRIEND_EMAIL,RC.REWARD_VALUE,RC.STATUS,RC.REWARD_TYPE,RC.REWARD_SET_NAME,RC.REWARD_DETAIL"
			+ " FROM CUST.RAF_CREDIT RC  WHERE RC.STATUS in ('P','F') ";

	public static List<ReferralPromotionModel> getSettledTransaction(Connection conn) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ReferralPromotionModel> list = new ArrayList<ReferralPromotionModel>();
		// ReferralPromotionModel defaultRefPromo =
		// getReferralDefaultPromotionDetails(conn);
		try {
			ps = conn.prepareStatement(GET_SETTELED_TRANSACTION);
			rs = ps.executeQuery();
			while (rs.next()) {
				if(null!=rs.getString("ADVOCATE_USER_ID")){
				ReferralPromotionModel rpm = new ReferralPromotionModel();
				// rpm.setPromotion_id(rs.getString("PROMOTION_ID"));
				// rpm.setSaleId(rs.getString("saleid"));
				rpm.setCustomerId(rs.getString("FRIEND_USER_ID"));
				rpm.setRefCustomerId(rs.getString("ADVOCATE_USER_ID"));
				rpm.setFDCustomerId(rs.getString("FRIEND_USER_ID"));
			//	System.out.println("now, is this null?" + rs.getString("ADVOCATE_USER_ID"));

				rpm.setReferral_fee(Integer.valueOf(rs.getString("REWARD_VALUE")));
				// rpm.setReferral_prgm_id(refDetails.getReferral_prgm_id());
				// rpm.setReferralCreditEmailSubject(refDetails.getReferralCreditEmailSubject());
				// rpm.setReferralCreditEmailText(refDetails.getReferralCreditEmailText());
				rpm.setFriendEmail(rs.getString("FRIEND_EMAIL"));
				rpm.setAdvocateEmail(rs.getString("ADVOCATE_EMAIL"));

				list.add(rpm);
			}
			}
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return list;
	}

	// UPDATING THE STATUS BACK TO COMPLATED ONCE REWARD IS BEING GIVEN TO ADVOCATE
	private static final String UPDATE_SETTELED_REWARD = " UPDATE CUST.RAF_CREDIT SET STATUS='S',MODIFIED_TIME=SYSDATE WHERE"
			+ " FRIEND_EMAIL=? AND ADVOCATE_EMAIL=?";

	public static Map<String, String> updateSetteledRewardTransaction(Connection conn, List<ReferralPromotionModel> models)
			throws SQLException {
		Map<String, String> updatedTrasactionMap = new HashMap<String, String>();
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement(UPDATE_SETTELED_REWARD);
			for (ReferralPromotionModel model : models) {
				ps.setString(1, model.getFriendEmail());
				ps.setString(2, model.getAdvocateEmail());
				ps.addBatch();
				updatedTrasactionMap.put(model.getFriendEmail(), model.getAdvocateEmail());
			}
			ps.executeBatch();
		} finally {
			if (ps != null)
				ps.close();

		}
		return updatedTrasactionMap;
	}
}

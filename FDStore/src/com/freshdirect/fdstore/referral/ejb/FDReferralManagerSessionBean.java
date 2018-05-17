/*
 * Created on Jun 10, 2005
 *
 */
package com.freshdirect.fdstore.referral.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.customer.EnumAccountActivityType;
import com.freshdirect.customer.EnumTransactionSource;
import com.freshdirect.customer.ErpActivityRecord;
import com.freshdirect.customer.ErpCustomerCreditModel;
import com.freshdirect.customer.ErpCustomerInfoModel;
import com.freshdirect.customer.ejb.ErpCustomerEB;
import com.freshdirect.customer.ejb.ErpCustomerManagerSB;
import com.freshdirect.customer.ejb.ErpLogActivityCommand;
import com.freshdirect.deliverypass.DeliveryPassModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDCustomerFactory;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDUser;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.customer.ejb.FDSessionBeanSupport;
import com.freshdirect.fdstore.mail.FDEmailFactory;
import com.freshdirect.fdstore.mail.FDReferAFriendInvEmail;
import com.freshdirect.fdstore.referral.EnumReferralStatus;
import com.freshdirect.fdstore.referral.ManageInvitesModel;
import com.freshdirect.fdstore.referral.ReferralCampaign;
import com.freshdirect.fdstore.referral.ReferralChannel;
import com.freshdirect.fdstore.referral.ReferralHistory;
import com.freshdirect.fdstore.referral.ReferralObjective;
import com.freshdirect.fdstore.referral.ReferralPartner;
import com.freshdirect.fdstore.referral.ReferralProgram;
import com.freshdirect.fdstore.referral.ReferralProgramInvitaionModel;
import com.freshdirect.fdstore.referral.ReferralPromotionModel;
import com.freshdirect.fdstore.referral.ReferralSearchCriteria;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.core.ServiceLocator;
import com.freshdirect.framework.mail.EmailAddress;
import com.freshdirect.framework.mail.EmailSupport;
import com.freshdirect.framework.mail.XMLEmailI;
import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.mail.ErpMailSender;
import com.freshdirect.mail.ejb.MailerGatewaySB;

/**
 * @author jng
 * 
 */
public class FDReferralManagerSessionBean extends FDSessionBeanSupport {

	private final static Logger LOGGER = LoggerFactory
			.getInstance(FDReferralManagerSessionBean.class);

	private final static ServiceLocator LOCATOR = new ServiceLocator();

	public List loadAllReferralPrograms() throws FDResourceException,
			RemoteException {
		Connection conn = null;
		List list = null;
		try {
			conn = this.getConnection();
			list = FDReferralManagerDAO.loadAllReferralPrograms(conn);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}

		return list;
	}

	public List loadAllReferralChannels() throws FDResourceException,
			RemoteException {
		Connection conn = null;
		List list = null;
		try {
			conn = this.getConnection();
			list = FDReferralManagerDAO.loadAllReferralChannels(conn);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return list;
	}

	public List loadAllReferralpartners() throws FDResourceException,
			RemoteException {
		Connection conn = null;
		List list = null;
		try {
			conn = this.getConnection();
			list = FDReferralManagerDAO.loadAllReferralPartners(conn);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return list;
	}

	public List loadAllReferralObjective() throws FDResourceException,
			RemoteException {
		Connection conn = null;
		List list = null;
		try {
			conn = this.getConnection();
			list = FDReferralManagerDAO.loadAllReferralObjective(conn);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return list;

	}

	public List loadAllReferralCampaigns() throws FDResourceException,
			RemoteException {
		Connection conn = null;
		List list = null;
		try {
			conn = this.getConnection();
			list = FDReferralManagerDAO.loadAllReferralCampaigns(conn);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return list;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * createReferralChannel(com.freshdirect.fdstore.referral.ReferralChannel)
	 */
	public ReferralChannel createReferralChannel(ReferralChannel channel)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		PrimaryKey key = null;
		try {
			conn = this.getConnection();
			key = FDReferralManagerDAO.createReferralChannel(conn, channel);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}

		return channel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * createReferralPartner(com.freshdirect.fdstore.referral.ReferralPartner)
	 */
	public ReferralPartner createReferralPartner(ReferralPartner partner)
			throws FDResourceException, RemoteException {

		Connection conn = null;
		PrimaryKey key = null;
		try {
			conn = this.getConnection();
			key = FDReferralManagerDAO.createReferralpartner(conn, partner);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return partner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * createReferralObjective
	 * (com.freshdirect.fdstore.referral.ReferralObjective)
	 */
	public ReferralObjective createReferralObjective(ReferralObjective objective)
			throws FDResourceException, RemoteException {

		Connection conn = null;
		PrimaryKey key = null;
		try {
			conn = this.getConnection();
			key = FDReferralManagerDAO.createReferralObjective(conn, objective);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return objective;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * createReferralCampaign(com.freshdirect.fdstore.referral.ReferralCampaign)
	 */
	public ReferralCampaign createReferralCampaign(ReferralCampaign campaign)
			throws FDResourceException, RemoteException {

		Connection conn = null;
		PrimaryKey key = null;
		try {
			conn = this.getConnection();
			if (campaign.getObjective() != null
					&& campaign.getObjective().getPK() == null) {
				key = FDReferralManagerDAO.createReferralObjective(conn,
						campaign.getObjective());
				campaign.getObjective().setPK(key);
			}

			key = FDReferralManagerDAO.createReferralCampaign(conn, campaign);

		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return campaign;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * createReferralProgram(com.freshdirect.fdstore.referral.ReferralProgram)
	 */
	public ReferralProgram createReferralProgram(ReferralProgram program)
			throws FDResourceException, RemoteException {

		Connection conn = null;
		PrimaryKey key = null;
		try {
			conn = this.getConnection();
			if (program.getChannel() != null
					&& program.getChannel().getPK() == null) {
				createReferralChannel(program.getChannel());
			}
			if (program.getPartner() != null
					&& program.getPartner().getPK() == null) {
				createReferralPartner(program.getPartner());
			}
			if (program.getCampaign() != null
					&& program.getCampaign().getPK() == null) {
				createReferralCampaign(program.getCampaign());
			}

			key = FDReferralManagerDAO.createReferralProgram(conn, program);

		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return program;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * createReferralHistory(com.freshdirect.fdstore.referral.ReferralHistory)
	 */
	public ReferralHistory createReferralHistory(ReferralHistory history)
			throws FDResourceException, RemoteException {

		Connection conn = null;
		PrimaryKey key = null;
		try {
			conn = this.getConnection();
			key = FDReferralManagerDAO.createReferralHistory(conn, history);

		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return history;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * createReferralInvitee
	 * (com.freshdirect.fdstore.referral.ReferralProgramInvitaionModel,
	 * com.freshdirect.fdstore.customer.FDUser)
	 */
	public ReferralProgramInvitaionModel createReferralInvitee(
			ReferralProgramInvitaionModel referral, FDUserI user)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		PrimaryKey key = null;
		try {
			LOGGER.debug("Creating the ReferralInvitee****" + referral);
			Date today = new Date();
			referral.setReferralCreatedDate(today);

			if (referral.getReferralProgramId() == null) {
				ReferralProgram referralProgram = loadLastestActiveReferralProgram();
				if (referralProgram != null && referralProgram.getPK() != null) {
					referral.setReferralProgramId(referralProgram.getPK()
							.getId());
					LOGGER.debug("setting the Referral Program Id ****"
							+ referralProgram.getPK().getId());
				}
			}
			conn = this.getConnection();
			LOGGER.debug("getting the connection con ****" + conn);
			EnumReferralStatus status = EnumReferralStatus.REFERRED; // defaulted
																		// to
																		// email
																		// sent
			if (isReferrerRestricted(conn, user)) {
				status = EnumReferralStatus.RESTRICTED;
			} else if (!isReferrerEligible(conn, user)) {
				status = EnumReferralStatus.INELIGIBLE;
			} else if (isMaxedOutReferrals(conn, user)) {
				status = EnumReferralStatus.MAXED_REFERRALS;
			} else if (isCustomer(referral.getReferrelEmailAddress())) {
				status = EnumReferralStatus.REFERRAL_ALREADY_CUST;
			}
			referral.setStatus(status);
			LOGGER.debug("setting the referral status ****" + status);
			key = FDReferralManagerDAO.createReferral(conn, referral);

		} catch (SQLException e) {
			e.printStackTrace();
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return referral;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#storeReferral
	 * (com.freshdirect.fdstore.referral.ReferralProgramInvitaionModel,
	 * com.freshdirect.fdstore.customer.FDUser)
	 */
	public void storeReferral(ReferralProgramInvitaionModel referral,
			FDUser user) throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferralManagerDAO.storeReferral(conn, referral);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * loadReferralFromPK(java.lang.String)
	 */
	public ReferralProgramInvitaionModel loadReferralFromPK(String referralId)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferralManagerDAO.loadReferralFromPK(conn,
					new PrimaryKey(referralId));
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * loadReferralsFromReferralProgramId(java.lang.String)
	 */
	public List loadReferralsFromReferralProgramId(String referralProgramId)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferralManagerDAO.loadReferralsFromReferralProgramId(
					conn, referralProgramId);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * loadReferralsFromReferrerCustomerId(java.lang.String)
	 */
	public List loadReferralsFromReferrerCustomerId(String referrerCustomerId)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferralManagerDAO.loadReferralsFromReferrerCustomerId(
					conn, referrerCustomerId, null);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * loadReferralsFromReferralEmailAddress(java.lang.String)
	 */
	public List loadReferralsFromReferralEmailAddress(
			String referralEmailAddress) throws FDResourceException,
			RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferralManagerDAO.loadReferralsFromReferralEmailAddress(
					conn, referralEmailAddress);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * loadReferralReportFromReferrerCustomerId(java.lang.String)
	 */
	public List loadReferralReportFromReferrerCustomerId(
			String referrerCustomerId) throws FDResourceException,
			RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferralManagerDAO
					.loadReferralReportFromReferrerCustomerId(conn,
							referrerCustomerId);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * loadReferralReportFromReferralCustomerId(java.lang.String)
	 */
	public List loadReferralReportFromReferralCustomerId(
			String referralCustomerId) throws FDResourceException,
			RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferralManagerDAO
					.loadReferralReportFromReferralCustomerId(conn,
							referralCustomerId);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * loadReferrerNameFromReferralCustomerId(java.lang.String)
	 */
	public String loadReferrerNameFromReferralCustomerId(
			String referralCustomerId) throws FDResourceException,
			RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferralManagerDAO.loadReferrerNameFromReferralCustomerId(
					conn, referralCustomerId);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * loadReferralProgramFromPK(java.lang.String)
	 */
	public ReferralProgram loadReferralProgramFromPK(String referralProgramId)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferralManagerDAO.loadReferralProgramFromPK(conn,
					new PrimaryKey(referralProgramId));
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.freshdirect.fdstore.referral.ejb.FDReferralManagerTMPSB#
	 * loadLastestActiveReferralProgram()
	 */
	public ReferralProgram loadLastestActiveReferralProgram()
			throws FDResourceException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferralManagerDAO.loadLatestActiveReferralProgram(conn);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	private boolean isReferrerRestricted(Connection conn, FDUserI user)
			throws FDResourceException, SQLException {
		if (user != null && user.isReferrerRestricted()) {
			return true;
		}
		return false;
	}

	private boolean isReferrerEligible(Connection conn, FDUserI user)
			throws FDResourceException, SQLException {
		if (user != null && user.isReferrerEligible()) {
			return true;
		}
		return false;
	}

	private boolean isMaxedOutReferrals(Connection conn, FDUserI user)
			throws SQLException {
		int numDaysMaxReferrals = Integer.parseInt(FDStoreProperties
				.getNumDaysMaxReferrals());
		Date startDate = null;
		if (numDaysMaxReferrals > 0) {
			startDate = new Date();
			startDate = DateUtil.addDays(DateUtil.truncate(startDate),
					(numDaysMaxReferrals - 1) * -1);
		}
		List referralList = FDReferralManagerDAO
				.loadReferralsFromReferrerCustomerId(conn, user.getIdentity()
						.getErpCustomerPK(), startDate);
		if (referralList != null) {
			// check to make sure referrer didn't reach maximum number of
			// referrals
			int maxReferrals = Integer.parseInt(FDStoreProperties
					.getMaxReferrals());
			if (referralList.size() >= maxReferrals) {
				return true;
			}
		}
		return false;
	}

	private boolean isCustomer(String userId) throws FDResourceException {
		try {
			ErpCustomerEB eb;
			try {
				eb = this.getErpCustomerHome().findByUserId(userId);
			} catch (ObjectNotFoundException ex) {
				return false;
			}
			if (getOrderCount(eb.getPK().getId()) < 1) {
				return false;
			}
			return true;
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (FinderException ce) {
			throw new FDResourceException(ce);
		}
	}

	private int getOrderCount(String customerId) throws FDResourceException {
		ErpCustomerManagerSB sb;
		try {
			sb = this.getErpCustomerManagerHome().create();
			return sb.getValidOrderCount(new PrimaryKey(customerId));
		} catch (CreateException ex) {
			throw new FDResourceException(ex);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	protected String getResourceCacheKey() {
		return "com.freshdirect.fdstore.referral.ejb.FDReferralManagerHome";
	}

	public void updateReferralStatus(String referralId, String status)
			throws FDResourceException, RemoteException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferralManagerDAO.updateReferralStatus(conn, referralId, status);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}

	}

	public void updateReferralProgram(ReferralProgram refProgram)
			throws FDResourceException, RemoteException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferralManagerDAO.updateReferralProgram(conn, refProgram);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public void updateReferralChannel(ReferralChannel channel)
			throws FDResourceException, RemoteException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferralManagerDAO.updateReferralChannel(conn, channel);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}

	}

	public void updateReferralCampaign(ReferralCampaign campaign)
			throws FDResourceException, RemoteException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferralManagerDAO.updateReferralCampaign(conn, campaign);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public void updateReferralPartner(ReferralPartner partner)
			throws FDResourceException, RemoteException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferralManagerDAO.updateReferralPartner(conn, partner);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public void updateReferralObjective(ReferralObjective objective)
			throws FDResourceException, RemoteException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferralManagerDAO.updateReferralObjective(conn, objective);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public void removeReferralProgram(String refProgramId[])
			throws FDResourceException, RemoteException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferralManagerDAO.deleteReferralProgram(conn, refProgramId);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public void removeReferralChannel(String channelIds[])
			throws FDResourceException, RemoteException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferralManagerDAO.deleteReferralChannel(conn, channelIds);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public void removeReferralCampaign(String campaignIds[])
			throws FDResourceException, RemoteException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferralManagerDAO.deleteReferralCampaign(conn, campaignIds);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public void removeReferralPartner(String partnerIds[])
			throws FDResourceException, RemoteException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferralManagerDAO.deleteReferralPartner(conn, partnerIds);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public void removeReferralObjective(String objectiveIds[])
			throws FDResourceException, RemoteException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferralManagerDAO.deleteReferralObjective(conn, objectiveIds);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public ReferralChannel getReferralChannleModel(String refChaId)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		ReferralChannel channel = null;
		try {
			conn = this.getConnection();
			channel = FDReferralManagerDAO.loadReferralChannelFromPK(conn,
					new PrimaryKey(refChaId));
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return channel;
	}

	public ReferralCampaign getReferralCampaigneModel(String refCampId)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		ReferralCampaign campaign = null;
		try {
			conn = this.getConnection();
			campaign = FDReferralManagerDAO.loadReferralCampaignFromPK(conn,
					new PrimaryKey(refCampId));
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return campaign;
	}

	public ReferralObjective getReferralObjectiveModel(String refObjId)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		ReferralObjective objective = null;
		try {
			conn = this.getConnection();
			objective = FDReferralManagerDAO.loadReferralObjectiveFromPK(conn,
					new PrimaryKey(refObjId));
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return objective;
	}

	public ReferralPartner getReferralPartnerModel(String refPartId)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		ReferralPartner partner = null;
		try {
			conn = this.getConnection();
			partner = FDReferralManagerDAO.loadReferralpartnerFromPK(conn,
					new PrimaryKey(refPartId));
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return partner;
	}

	public ReferralProgram getReferralProgramModel(String refProgId)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		ReferralProgram program = null;
		try {
			conn = this.getConnection();
			program = FDReferralManagerDAO.loadReferralProgramFromPK(conn,
					new PrimaryKey(refProgId));
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return program;
	}

	public List getReferralProgarmforRefChannel(String refChaIds[])
			throws FDResourceException, RemoteException {
		Connection conn = null;
		List collection = null;
		try {
			conn = this.getConnection();
			collection = FDReferralManagerDAO.loadReferralProgramForChannel(
					conn, refChaIds);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return collection;
	}

	public List getReferralProgarmforRefPartner(String refPartnerIds[])
			throws FDResourceException, RemoteException {
		Connection conn = null;
		List collection = null;
		try {
			conn = this.getConnection();
			collection = FDReferralManagerDAO.loadReferralProgramForPartner(
					conn, refPartnerIds);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return collection;
	}

	public List getReferralProgarmforRefCampaign(String refCampaignIds[])
			throws FDResourceException, RemoteException {
		Connection conn = null;
		List collection = null;
		try {
			conn = this.getConnection();
			collection = FDReferralManagerDAO.loadReferralProgramForCampaign(
					conn, refCampaignIds);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return collection;
	}

	public List getReferralCampaignforRefObjective(String refObjIds[])
			throws FDResourceException, RemoteException {

		Connection conn = null;
		List collection = null;
		try {
			conn = this.getConnection();
			collection = FDReferralManagerDAO
					.loadReferralCampaignForRefObjective(conn, refObjIds);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return collection;
	}

	public boolean isReferralProgramNameExist(String refPrgName)
			throws FDResourceException, RemoteException {

		Connection conn = null;
		boolean isExists = false;
		try {
			conn = this.getConnection();
			int count = FDReferralManagerDAO.getTotalCountOfRefProgram(conn,
					refPrgName);
			if (count > 0)
				isExists = true;
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return isExists;
	}

	public boolean isReferralChannelNameAndTypeExist(String name, String type)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		boolean isExists = false;
		try {
			conn = this.getConnection();
			int count = FDReferralManagerDAO.getTotalCountOfRefChannel(conn,
					name, type);
			if (count > 0)
				isExists = true;
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return isExists;
	}

	public boolean isReferralObjectiveNameExist(String refObjName)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		boolean isExists = false;
		try {
			conn = this.getConnection();
			int count = FDReferralManagerDAO.getTotalCountOfRefObjective(conn,
					refObjName);
			if (count > 0)
				isExists = true;
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return isExists;
	}

	public boolean isReferralCampaignNameExist(String refCampName)
			throws FDResourceException, RemoteException {

		Connection conn = null;
		boolean isExists = false;
		try {
			conn = this.getConnection();
			int count = FDReferralManagerDAO.getTotalCountOfRefCampaign(conn,
					refCampName);
			if (count > 0)
				isExists = true;
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return isExists;
	}

	public boolean isReferralPartnerNameExist(String refPartName)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		boolean isExists = false;
		try {
			conn = this.getConnection();
			int count = FDReferralManagerDAO.getTotalCountOfRefPartner(conn,
					refPartName);
			if (count > 0)
				isExists = true;
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return isExists;
	}

	public List getReferralPrograms(ReferralSearchCriteria criteria)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		List collection = null;
		try {
			conn = this.getConnection();
			if (criteria.getStartIndex() == 0
					|| criteria.getTotalRcdSize() == 0) {
				criteria.setTotalRcdSize(FDReferralManagerDAO
						.getTotalReferralProgramCount(conn));
			}
			collection = FDReferralManagerDAO.getReferralPrograms(conn,
					criteria);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return collection;
	}

	public List getReferralChannels(ReferralSearchCriteria criteria)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		List collection = null;
		try {
			conn = this.getConnection();
			if (criteria.getStartIndex() == 0
					|| criteria.getTotalRcdSize() == 0) {
				criteria.setTotalRcdSize(FDReferralManagerDAO
						.getTotalReferralChannelCount(conn));
			}
			collection = FDReferralManagerDAO
					.getReferralChannel(conn, criteria);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return collection;

	}

	public List getReferralCampaigns(ReferralSearchCriteria criteria)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		List collection = null;
		try {
			conn = this.getConnection();
			if (criteria.getStartIndex() == 0
					|| criteria.getTotalRcdSize() == 0) {
				criteria.setTotalRcdSize(FDReferralManagerDAO
						.getTotalReferralCampaignCount(conn));
			}
			collection = FDReferralManagerDAO.getReferralCampaign(conn,
					criteria);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return collection;
	}

	public List getReferralPartners(ReferralSearchCriteria criteria)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		List collection = null;
		try {
			conn = this.getConnection();
			if (criteria.getStartIndex() == 0
					|| criteria.getTotalRcdSize() == 0) {
				criteria.setTotalRcdSize(FDReferralManagerDAO
						.getTotalReferralPartnerCount(conn));
			}
			collection = FDReferralManagerDAO
					.getReferralPartner(conn, criteria);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return collection;
	}

	public List getReferralObjective(ReferralSearchCriteria criteria)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		List collection = null;
		try {
			conn = this.getConnection();
			if (criteria.getStartIndex() == 0
					|| criteria.getTotalRcdSize() == 0) {
				criteria.setTotalRcdSize(FDReferralManagerDAO
						.getTotalReferralObjectiveCount(conn));
			}
			collection = FDReferralManagerDAO.getReferralObjective(conn,
					criteria);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return collection;

	}

	public ReferralPromotionModel getReferralPromotionDetails(String userId)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.getReferralPromotionDetails(conn, userId);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}

	}
	
	public ReferralPromotionModel getReferralPromotionDetailsByRefName(String referral)
		throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			String userId = FDReferAFriendDAO.getCustomerId(conn, referral);
			return FDReferAFriendDAO.getReferralPromotionDetails(conn, userId);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}	
	}

	public void sendMails(String recipient_list, String mail_message, FDUser user, String rpid, String serverName) throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			//Send message
			conn = this.getConnection();
			ReferralPromotionModel rpModel = FDReferAFriendDAO.getReferralPromotionDetailsById(conn, rpid);
			String name = user.getFirstName();
			String invEmailTxt = rpModel.getInviteEmailText();
			String refLink = "https://" + serverName + "/invite/" + user.getReferralLink();
			invEmailTxt = invEmailTxt.replaceAll("<personal url>", refLink);
			String offerText = rpModel.getInviteEmailOfferText();
			
			FDReferAFriendInvEmail xemail = (FDReferAFriendInvEmail) FDEmailFactory.getInstance().createReferAFriendInvitationEmail(name, mail_message, invEmailTxt, rpModel.getInviteEmailLegal(), refLink, offerText);
			
			String subject = rpModel.getInviteEmailSubject();
			subject = subject.replaceAll("<first name>", user.getFirstName());
			subject = subject.replaceAll("<last name>", user.getLastName());
			xemail.setSubject(subject);
			
			xemail.setFromAddress(new EmailAddress(name, user.getUserId()));
			
			MailerGatewaySB mailer = getMailerHome().create();
			
			StringTokenizer stokens = new StringTokenizer(recipient_list, ",");			
			while(stokens.hasMoreTokens()) {
				String recipient = stokens.nextToken().trim();
				System.out.println("\n\n\n trimmed_recipient:" + recipient);
				xemail.setRecipient(recipient);
				mailer.enqueueEmail(xemail);
				//Save the email to user's invite list
				FDReferAFriendDAO.saveInvite(conn, recipient, user.getIdentity().getErpCustomerPK());
			}
			//record activity
			ErpActivityRecord rec = new ErpActivityRecord();
			rec.setActivityType(EnumAccountActivityType.REFER_A_FRIEND);
			rec.setSource(EnumTransactionSource.WEBSITE);
			rec.setInitiator("CUSTOMER");
			rec.setCustomerId(user.getIdentity().getErpCustomerPK());
			rec.setDate(new Date());
			rec.setNote("Referral Invitation Sent to:" + recipient_list);
			new ErpLogActivityCommand(LOCATOR, rec).execute();
		} catch(Exception e) {
			LOGGER.error("Error sending emails", e);
		}
			 
	}

	public List<ManageInvitesModel> getManageInvites(String customerId)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.getManageInvites(conn, customerId);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public List<ErpCustomerCreditModel> getUserCredits(String customerId)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.getUserCredits(conn, customerId);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public List<ManageInvitesModel> getManageInvitesForCRM(String customerId)
			throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.getManageInvitesForCRM(conn, customerId);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	private ErpActivityRecord createActivity(EnumAccountActivityType type,
			String initiator, String note, String customerId) {
		ErpActivityRecord rec = new ErpActivityRecord();
		rec.setActivityType(type);

		rec.setSource(EnumTransactionSource.WEBSITE);
		rec.setInitiator(initiator);
		rec.setCustomerId(customerId);
		if (note != null) {
			rec.setNote(note);
		}
		return rec;
	}
	
	public Double getAvailableCredit(String customerId)throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.getAvailableCredit(conn, customerId);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public Boolean getReferralDisplayFlag(String customerId)throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.getReferralDisplayFlag(conn, customerId);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public List getSettledSales()throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.getSettledSales(conn);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public String getReferralLink(String customerId)throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.getReferralLink(conn, customerId);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public String getLatestSTLSale(String customerId)throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.getLatestSTLSale(conn, customerId);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public void saveCustomerCredit(String referral_customer_id, String customer_id, int ref_fee, String sale, String complaintId, String refPrgmId) throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferAFriendDAO.saveCustomerCredit(conn, referral_customer_id, customer_id, ref_fee, sale, complaintId, refPrgmId);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public boolean isCustomerReferred(String customerId) throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.isCustomerReferred(conn, customerId);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public String updateFDUser(String customerId, String zipCode, EnumServiceType serviceType)throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.updateFDUser(conn, customerId, zipCode, serviceType);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public void updateCustomerInfo(String customerId, String firstName, String lastName)throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferAFriendDAO.updateCustomerInfo(conn, customerId, firstName, lastName);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public void updateCustomerPW(String customerId, String pwdHash)throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferAFriendDAO.updateCustomerPW(conn, customerId, pwdHash);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public void updateFdCustomer(String customerId, String pwdHint)throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferAFriendDAO.updateFdCustomer(conn, customerId, pwdHint);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public void storeFailedAttempt(String email, String dupeCustID, String zipCode, String firstName, String lastName, String referral, String reason) throws FDResourceException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			FDReferAFriendDAO.storeFailedAttempt(conn, email, dupeCustID, zipCode, firstName, lastName, referral, reason);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public boolean isUniqueFNLNZipCombo(String firstName, String lastName, String zipCode, String customerId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.isUniqueFNLNZipCombo(conn, firstName, lastName, zipCode, customerId);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public String getReferralName(String referralId) throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.getReferralName(conn, referralId);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public boolean isReferreSignUpComplete(String email) throws FDResourceException, RemoteException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.isReferreSignUpComplete(conn, email);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	public List<ReferralPromotionModel>  getSettledTransaction() throws FDResourceException, RemoteException{
		Connection conn = null;
		try {
			conn = this.getConnection();
			return FDReferAFriendDAO.getSettledTransaction(conn);
		} catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}	
	public  Map<String,String> updateSetteledRewardTransaction(List<ReferralPromotionModel> models) throws FDResourceException, RemoteException{
		Connection conn=null;
		try{
			conn = this.getConnection();
			return FDReferAFriendDAO.updateSetteledRewardTransaction(conn, models);
		}catch (SQLException e) {
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

}

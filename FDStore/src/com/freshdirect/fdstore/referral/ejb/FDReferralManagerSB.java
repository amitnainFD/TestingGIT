/*
 * Created on Jun 10, 2005
 *
 */
package com.freshdirect.fdstore.referral.ejb;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBObject;

import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.customer.ErpCustomerCreditModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDUser;
import com.freshdirect.fdstore.customer.FDUserI;
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

/**
 * @author jng
 *
 */
public interface FDReferralManagerSB extends EJBObject {		
		
		
	public void updateReferralStatus(String referralId, String ststus)throws FDResourceException,  RemoteException;
	
	public void updateReferralProgram(ReferralProgram refProgram) throws FDResourceException, RemoteException;
	
	public void updateReferralChannel(ReferralChannel channel) throws FDResourceException, RemoteException;
	
	public void updateReferralCampaign(ReferralCampaign campaign) throws FDResourceException, RemoteException;
	
	public void updateReferralPartner(ReferralPartner partner) throws FDResourceException, RemoteException;
	
	public void updateReferralObjective(ReferralObjective objective) throws FDResourceException, RemoteException;
	
	
	public void removeReferralProgram(String refProgramId[]) throws FDResourceException, RemoteException;
	
	public void removeReferralChannel(String channelIds[]) throws FDResourceException, RemoteException;
	
	public void removeReferralCampaign(String campaignIds[]) throws FDResourceException, RemoteException;
	
	public void removeReferralPartner(String partnerIds[]) throws FDResourceException, RemoteException;
	
	public void removeReferralObjective(String objectiveIds[]) throws FDResourceException, RemoteException;
	
		
	public abstract ReferralChannel createReferralChannel(ReferralChannel channel) throws FDResourceException,  RemoteException;

	public abstract ReferralPartner createReferralPartner(ReferralPartner partner) throws FDResourceException,  RemoteException;

	public abstract ReferralObjective createReferralObjective(ReferralObjective objective) throws FDResourceException,  RemoteException;

	public abstract ReferralCampaign createReferralCampaign(ReferralCampaign campaign) throws FDResourceException, RemoteException;

	public abstract ReferralProgram createReferralProgram(ReferralProgram program) throws FDResourceException,  RemoteException;

	public abstract ReferralHistory createReferralHistory(ReferralHistory history) throws FDResourceException,  RemoteException;

	public abstract ReferralProgramInvitaionModel createReferralInvitee(ReferralProgramInvitaionModel referral, FDUserI user) throws FDResourceException,  RemoteException;

	public abstract void storeReferral(ReferralProgramInvitaionModel referral, FDUser user) throws FDResourceException, RemoteException;

	public abstract ReferralProgramInvitaionModel loadReferralFromPK(String referralId) throws FDResourceException, RemoteException;

	public abstract List loadReferralsFromReferralProgramId( String referralProgramId) throws FDResourceException, RemoteException;

	public abstract List loadReferralsFromReferrerCustomerId(String referrerCustomerId) throws FDResourceException, RemoteException;

	public abstract List loadReferralsFromReferralEmailAddress( String referralEmailAddress) throws FDResourceException, RemoteException;

	public abstract List loadReferralReportFromReferrerCustomerId(String referrerCustomerId) throws FDResourceException, RemoteException;

	public abstract List loadReferralReportFromReferralCustomerId( String referralCustomerId) throws FDResourceException,	RemoteException;
	
	public abstract List loadAllReferralPrograms() throws FDResourceException,	RemoteException;
	
	public abstract List loadAllReferralChannels() throws FDResourceException,	RemoteException;
	
	public abstract List loadAllReferralpartners() throws FDResourceException,	RemoteException;
	
	public abstract List loadAllReferralObjective() throws FDResourceException,	RemoteException;
	
	public abstract List loadAllReferralCampaigns() throws FDResourceException,	RemoteException;
	
	public abstract String loadReferrerNameFromReferralCustomerId( String referralCustomerId) throws FDResourceException, RemoteException;

	public abstract ReferralProgram loadReferralProgramFromPK(String referralProgramId) throws FDResourceException, RemoteException;

	public abstract ReferralProgram loadLastestActiveReferralProgram() throws FDResourceException, RemoteException;
	
	public abstract ReferralChannel getReferralChannleModel(String refChaId) throws FDResourceException,	RemoteException;
	
	public abstract ReferralCampaign getReferralCampaigneModel(String refChaId) throws FDResourceException,	RemoteException;
	
	public abstract ReferralObjective getReferralObjectiveModel(String refChaId) throws FDResourceException,	RemoteException;
	
	public abstract ReferralPartner getReferralPartnerModel(String refChaId) throws FDResourceException,	RemoteException;
	
	public abstract ReferralProgram getReferralProgramModel(String refChaId) throws FDResourceException,	RemoteException;
	
	
	public abstract List getReferralProgarmforRefChannel(String refChaIds[]) throws FDResourceException,	RemoteException;
	
	public abstract List getReferralProgarmforRefPartner(String refpartIds[]) throws FDResourceException,	RemoteException;
	
	public abstract List getReferralProgarmforRefCampaign(String refCampIds[]) throws FDResourceException,	RemoteException;
	
	public abstract List getReferralCampaignforRefObjective(String refObjIds[]) throws FDResourceException,	RemoteException;
	
	
	 public abstract boolean isReferralPartnerNameExist(String refPartName) throws FDResourceException,	RemoteException;
	 
	 public abstract boolean isReferralCampaignNameExist(String refCampName) throws FDResourceException,	RemoteException;
	 
	 public abstract boolean isReferralObjectiveNameExist(String refObjName) throws FDResourceException,	RemoteException;
	 
	 public abstract boolean isReferralChannelNameAndTypeExist(String name,String type)  throws FDResourceException,	RemoteException;
	 
	 public abstract boolean isReferralProgramNameExist(String refPrgName) throws FDResourceException, RemoteException;

	 public abstract List getReferralPrograms(ReferralSearchCriteria criteria) throws FDResourceException, RemoteException;
	  
	 public abstract List getReferralChannels(ReferralSearchCriteria criteria) throws FDResourceException, RemoteException;
	  
	 public abstract List getReferralCampaigns(ReferralSearchCriteria criteria)throws FDResourceException, RemoteException;
	  
	 public abstract List getReferralPartners(ReferralSearchCriteria criteria)throws FDResourceException, RemoteException;
	  
	 public abstract List getReferralObjective(ReferralSearchCriteria criteria)throws FDResourceException, RemoteException;
	 
	 public abstract ReferralPromotionModel getReferralPromotionDetails(String userId)throws FDResourceException, RemoteException;
	 
	 public abstract ReferralPromotionModel getReferralPromotionDetailsByRefName(String referral)throws FDResourceException, RemoteException;
	 
	 public abstract void sendMails(String recipient_list, String mail_message, FDUser identity, String rpid, String serverName) throws FDResourceException, RemoteException;
	 
	 public abstract List<ManageInvitesModel> getManageInvites(String customerId)throws FDResourceException, RemoteException;
	 
	 public abstract List<ErpCustomerCreditModel> getUserCredits(String customerId)throws FDResourceException, RemoteException;
	 
	 public abstract List<ManageInvitesModel> getManageInvitesForCRM(String customerId)throws FDResourceException, RemoteException;
	 
	 public abstract Double getAvailableCredit(String customerId)throws FDResourceException, RemoteException;
	 
	 public abstract Boolean getReferralDisplayFlag(String customerId)throws FDResourceException, RemoteException;
	 
	 public abstract List<ReferralPromotionModel> getSettledSales()throws FDResourceException, RemoteException;
	 
	 public abstract String getReferralLink(String customerId)throws FDResourceException, RemoteException;
	 
	 public abstract String getLatestSTLSale(String customerId)throws FDResourceException, RemoteException;
	 
	 public abstract void saveCustomerCredit(String referral_customer_id, String customer_id, int ref_fee, String sale, String complaintId, String refPrgmId) throws FDResourceException, RemoteException;
	 
	 public abstract boolean isCustomerReferred(String customerId)throws FDResourceException, RemoteException;
	 
	 public abstract String updateFDUser(String customerId, String zipCode, EnumServiceType serviceType)throws FDResourceException, RemoteException;
	 
	 public abstract void updateCustomerInfo(String customerId, String firstName, String lastName)throws FDResourceException, RemoteException;
	 
	 public abstract void updateCustomerPW(String customerId, String pwdHash)throws FDResourceException, RemoteException;
	 
	 public abstract void updateFdCustomer(String customerId, String pwdHint)throws FDResourceException, RemoteException;
	 
	 public abstract void storeFailedAttempt(String email, String dupeCustID, String zipCode, String firstName, String lastName, String referral, String reason) throws FDResourceException, RemoteException;
	 
	 public abstract boolean isUniqueFNLNZipCombo(String firstName, String lastName, String zipCode, String customerId) throws FDResourceException, RemoteException;
	 
	 public abstract String getReferralName(String referralId) throws FDResourceException, RemoteException;
	 
	 public abstract boolean isReferreSignUpComplete(String email) throws FDResourceException, RemoteException;
	 
	 public abstract List<ReferralPromotionModel>  getSettledTransaction() throws FDResourceException, RemoteException;

	public abstract Map<String,String> updateSetteledRewardTransaction(List<ReferralPromotionModel> models) throws FDResourceException, RemoteException;

}

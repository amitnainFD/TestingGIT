/*
 * Created on Jun 10, 2005
 *
 */
package com.freshdirect.fdstore.referral;

import java.rmi.RemoteException;

import java.util.List;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.customer.ErpCustomerCreditModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDUser;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.promotion.management.ejb.FDPromotionManagerNewSB;
import com.freshdirect.fdstore.referral.ejb.FDReferralManagerHome;
import com.freshdirect.fdstore.referral.ejb.FDReferralManagerSB;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * @author jng
 *
 */
public class FDReferralManager {
	private static Category LOGGER = LoggerFactory.getInstance(FDReferralManager.class);

	private static FDReferralManagerHome managerHome = null;

	
	
    public static ReferralChannel getReferralChannleModel(String refChaId) throws FDResourceException
    {
		lookupManagerHome();
		ReferralChannel channel=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
		    channel=sb.getReferralChannleModel(refChaId);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
    	return channel;
    }
	
	public static  ReferralCampaign getReferralCampaigneModel(String refChaId) throws FDResourceException
	{
		lookupManagerHome();
		ReferralCampaign campaign=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
		    campaign=sb.getReferralCampaigneModel(refChaId);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
    	return campaign;
	}
	
	public static ReferralObjective getReferralObjectiveModel(String refChaId) throws FDResourceException
	{
		lookupManagerHome();
		ReferralObjective objective=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
			objective=sb.getReferralObjectiveModel(refChaId);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
    	return objective;		
	}
	
	public static ReferralPartner getReferralPartnerModel(String refChaId) throws FDResourceException
	{
		lookupManagerHome();
		ReferralPartner partner=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
			partner=sb.getReferralPartnerModel(refChaId);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
    	return partner;		
	}
	
	public static  ReferralProgram getReferralProgramModel(String refChaId) throws FDResourceException {
		lookupManagerHome();
		ReferralProgram program=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
			program=sb.getReferralProgramModel(refChaId);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
    	return program;		
	}
	
	public static void removeReferralProgram(String refProgramId[]) throws FDResourceException {
		lookupManagerHome();
		try {
			FDReferralManagerSB sb = managerHome.create();
		    sb.removeReferralProgram(refProgramId);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}

	
	
	public static void removeReferralChannel(String channelIds[]) throws FDResourceException {
		lookupManagerHome();
		try {
			FDReferralManagerSB sb = managerHome.create();
		    sb.removeReferralChannel(channelIds);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}

	
	public static void removeReferralCampaign(String campaignIds[]) throws FDResourceException {
		lookupManagerHome();
		try {
			FDReferralManagerSB sb = managerHome.create();
		    sb.removeReferralCampaign(campaignIds);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}

	
	public static void removeReferralPartner(String partnerIds[]) throws FDResourceException {
		lookupManagerHome();
		try {
			FDReferralManagerSB sb = managerHome.create();
		    sb.removeReferralPartner(partnerIds);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}

	
	public static void removeReferralObjective(String objectiveIds[]) throws FDResourceException {
		lookupManagerHome();
		try {
			FDReferralManagerSB sb = managerHome.create();
		    sb.removeReferralObjective(objectiveIds);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}
	
	
	
	public static List loadAllReferralPrograms() throws FDResourceException {	
		lookupManagerHome();
		List list=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
		    list=sb.loadAllReferralPrograms();			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		return list;
	}
	
	
	public static List loadAllReferralChannels() throws FDResourceException {	
		lookupManagerHome();
		List list=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
		    list=sb.loadAllReferralChannels();			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		return list;
	}
	
	public static List loadAllReferralCampaigns() throws FDResourceException {	
		lookupManagerHome();
		List list=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
		    list=sb.loadAllReferralCampaigns();			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		return list;
	}


	
	public static List loadAllReferralObjective() throws FDResourceException {	
		lookupManagerHome();
		List list=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
		    list=sb.loadAllReferralObjective();			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		return list;
	}
	

	public static List loadAllReferralPartners() throws FDResourceException {	
		lookupManagerHome();
		List list=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
		    list=sb.loadAllReferralpartners();			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		return list;
	}

	
	public static void updateReferralProgram(ReferralProgram refProgram) throws FDResourceException {
		lookupManagerHome();
		try {
			FDReferralManagerSB sb = managerHome.create();
		    sb.updateReferralProgram(refProgram);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}


	public static void updateReferralPartner(ReferralPartner partner) throws FDResourceException {
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
		    sb.updateReferralPartner(partner);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}

	
	
	public static void updateReferralCampaign(ReferralCampaign campaign) throws FDResourceException {
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
		    sb.updateReferralCampaign(campaign);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}
	
	
	public static void updateReferralChannel(ReferralChannel channel) throws FDResourceException {
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
		    sb.updateReferralChannel(channel);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}

	public static void updateReferralObjective(ReferralObjective channel) throws FDResourceException {
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
		    sb.updateReferralObjective(channel);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}
	
	public static void updateReferralStatus(String referralId, String status) throws FDResourceException {
		lookupManagerHome();

		try {			
			FDReferralManagerSB sb = managerHome.create();
		    sb.updateReferralStatus(referralId,status);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		
	}
	
	
	public static void storeReferral(ReferralProgramInvitaionModel referral, FDUser user) throws FDResourceException {
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
		    sb.storeReferral(referral,user);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		
	}

	
	
	public static ReferralHistory createReferralHistory(ReferralHistory history) throws FDResourceException {
		lookupManagerHome();
		ReferralHistory historyNew=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
			historyNew=sb.createReferralHistory(history);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		return historyNew;
	}

	
	
	public static ReferralProgram createReferralProgram(ReferralProgram program) throws FDResourceException {
		lookupManagerHome();
		ReferralProgram programNew=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
			programNew=sb.createReferralProgram(program);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		return programNew;
	}

	
	
	public static ReferralCampaign createReferralCampaign(ReferralCampaign campaign) throws FDResourceException {
		lookupManagerHome();
		ReferralCampaign campaignNew=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
			campaignNew=sb.createReferralCampaign(campaign);

		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		return campaignNew;
	}


	public static ReferralObjective createReferralObjective(ReferralObjective objective) throws FDResourceException {
		lookupManagerHome();
		ReferralObjective objectiveNew=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
			objectiveNew=sb.createReferralObjective(objective);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		return objectiveNew;
	}

	
	
	public static ReferralChannel createReferralChannel(ReferralChannel channel) throws FDResourceException {
		lookupManagerHome();
		ReferralChannel channelNew=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
			channelNew=sb.createReferralChannel(channel);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		return channelNew;
	}

	
	
	public static ReferralPartner createReferralPartner(ReferralPartner partner) throws FDResourceException {
		lookupManagerHome();
		ReferralPartner partnerNew=null;
		try {
			FDReferralManagerSB sb = managerHome.create();
			partnerNew=sb.createReferralPartner(partner);			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		return partnerNew;
	}

	
	
	
	public static ReferralProgramInvitaionModel createReferralInvitee(ReferralProgramInvitaionModel referral, FDUserI user) throws FDResourceException {
		lookupManagerHome();
		ReferralProgramInvitaionModel referralNew=null;
		try {
			LOGGER.debug("inside ReferralProgramInvitaionModel createReferralInvitee");
			FDReferralManagerSB sb = managerHome.create();
			referralNew=sb.createReferralInvitee(referral,user);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		return referralNew;
	}
	
	
	
	

	public static ReferralProgramInvitaionModel loadReferralFromPK(String referralId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
			return sb.loadReferralFromPK(referralId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}

	public static List loadReferralsFromReferralProgramId(String referralProgramId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
			return sb.loadReferralsFromReferralProgramId(referralProgramId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}

	public static List loadReferralsFromReferrerCustomerId(String referrerCustomerId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
			return sb.loadReferralsFromReferrerCustomerId(referrerCustomerId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}
	
	public static List loadReferralsFromReferralEmailAddress(String referralEmailAddress) throws FDResourceException {
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
			return sb.loadReferralsFromReferralEmailAddress(referralEmailAddress);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}

	public static List loadReferralReportFromReferrerCustomerId(String referrerCustomerId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
			return sb.loadReferralReportFromReferrerCustomerId(referrerCustomerId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}

	public static List loadReferralReportFromReferralCustomerId(String referralCustomerId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
			return sb.loadReferralReportFromReferralCustomerId(referralCustomerId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}

	
	
	public static String loadReferrerNameFromReferralCustomerId(String referralCustomerId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
			return sb.loadReferrerNameFromReferralCustomerId(referralCustomerId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}

	public static ReferralProgram loadReferralProgramFromPK(String referralProgramId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
			return sb.loadReferralProgramFromPK(referralProgramId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static ReferralProgram loadLastestActiveReferralProgram() throws FDResourceException {
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
			return sb.loadLastestActiveReferralProgram();
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
		

	private static void invalidateManagerHome() {
		managerHome = null;
	}

	private static void lookupManagerHome() throws FDResourceException {
		if (managerHome != null) {
			return;
		}
		Context ctx = null;
		try {
			ctx = FDStoreProperties.getInitialContext();			
			managerHome = (FDReferralManagerHome) ctx.lookup(FDStoreProperties.getFDReferralManagerHome());
		} catch (NamingException ne) {
			throw new FDResourceException(ne);
		} finally {
			try {
				if (ctx != null) {
					ctx.close();
				}
			} catch (NamingException ne) {
				LOGGER.warn("Cannot close Context while trying to cleanup", ne);
			}
		}
	}
	
	public static List getReferralProgarmforRefChannel(String refChaIds[]) throws FDResourceException
	{
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
			return sb.getReferralProgarmforRefChannel(refChaIds);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		
	}
	
	public static List getReferralProgarmforRefCampaign(String refCampIds[]) throws FDResourceException
	{
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
			return sb.getReferralProgarmforRefCampaign(refCampIds);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}
	
	public static List getReferralProgarmforRefPartner(String refPartIds[]) throws FDResourceException
	{
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
			return sb.getReferralProgarmforRefPartner(refPartIds);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}
	

	public static List getReferralCampaignforRefObjective(String refObjIds[]) throws FDResourceException
	{
		lookupManagerHome();

		try {
			FDReferralManagerSB sb = managerHome.create();
			return sb.getReferralCampaignforRefObjective(refObjIds);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}

	
	
	
     public static  boolean isReferralPartnerNameExist(String refPartName) throws FDResourceException
     {
    	 lookupManagerHome();       
 		try {
 			FDReferralManagerSB sb = managerHome.create();
 			return sb.isReferralPartnerNameExist(refPartName);
 		} catch (CreateException ce) {
 			invalidateManagerHome();
 			throw new FDResourceException(ce, "Error creating session bean");
 		} catch (RemoteException re) {
 			invalidateManagerHome();
 			throw new FDResourceException(re, "Error talking to session bean");
 		}		
     }
	 
	 public static  boolean isReferralCampaignNameExist(String refCampName) throws FDResourceException
	 {
		 lookupManagerHome();       
	 		try {
	 			FDReferralManagerSB sb = managerHome.create();
	 			return sb.isReferralCampaignNameExist(refCampName);
	 		} catch (CreateException ce) {
	 			invalidateManagerHome();
	 			throw new FDResourceException(ce, "Error creating session bean");
	 		} catch (RemoteException re) {
	 			invalidateManagerHome();
	 			throw new FDResourceException(re, "Error talking to session bean");
	 		}		
	 }
	 
	 public static boolean isReferralObjectiveNameExist(String refObjName) throws FDResourceException
	 {
		    lookupManagerHome();       
	 		try {
	 			FDReferralManagerSB sb = managerHome.create();
	 			return sb.isReferralObjectiveNameExist(refObjName);
	 		} catch (CreateException ce) {
	 			invalidateManagerHome();
	 			throw new FDResourceException(ce, "Error creating session bean");
	 		} catch (RemoteException re) {
	 			invalidateManagerHome();
	 			throw new FDResourceException(re, "Error talking to session bean");
	 		}	
	 }
	 
	 public static boolean isReferralChannelNameAndTypeExist(String name,String type)  throws FDResourceException{
		    lookupManagerHome();       
	 		try {
	 			FDReferralManagerSB sb = managerHome.create();
	 			return sb.isReferralChannelNameAndTypeExist(name,type);
	 		} catch (CreateException ce) {
	 			invalidateManagerHome();
	 			throw new FDResourceException(ce, "Error creating session bean");
	 		} catch (RemoteException re) {
	 			invalidateManagerHome();
	 			throw new FDResourceException(re, "Error talking to session bean");
	 		}	
		 
	 }
	 
	 public static boolean isReferralProgramNameExist(String refPrgName) throws FDResourceException {
		    lookupManagerHome();       
	 		try {
	 			FDReferralManagerSB sb = managerHome.create();
	 			return sb.isReferralProgramNameExist(refPrgName);
	 		} catch (CreateException ce) {
	 			invalidateManagerHome();
	 			throw new FDResourceException(ce, "Error creating session bean");
	 		} catch (RemoteException re) {
	 			invalidateManagerHome();
	 			throw new FDResourceException(re, "Error talking to session bean");
	 		}	
		 
	 }
	

	  public static List getReferralPrograms(ReferralSearchCriteria criteria) throws FDResourceException{
		  lookupManagerHome();

			try {
				FDReferralManagerSB sb = managerHome.create();
				return sb.getReferralPrograms(criteria);
			} catch (CreateException ce) {
				invalidateManagerHome();
				throw new FDResourceException(ce, "Error creating session bean");
			} catch (RemoteException re) {
				invalidateManagerHome();
				throw new FDResourceException(re, "Error talking to session bean");
			}		
	  }
	  
	  public static List getReferralChannels(ReferralSearchCriteria criteria) throws FDResourceException{
		  lookupManagerHome();

			try {
				FDReferralManagerSB sb = managerHome.create();
				return sb.getReferralChannels(criteria);
			} catch (CreateException ce) {
				invalidateManagerHome();
				throw new FDResourceException(ce, "Error creating session bean");
			} catch (RemoteException re) {
				invalidateManagerHome();
				throw new FDResourceException(re, "Error talking to session bean");
			}		
	  }
	  
	  public static List getReferralCampaigns(ReferralSearchCriteria criteria) throws FDResourceException{
		  lookupManagerHome();

			try {
				FDReferralManagerSB sb = managerHome.create();
				return sb.getReferralCampaigns(criteria);
			} catch (CreateException ce) {
				invalidateManagerHome();
				throw new FDResourceException(ce, "Error creating session bean");
			} catch (RemoteException re) {
				invalidateManagerHome();
				throw new FDResourceException(re, "Error talking to session bean");
			}		

	  }
	  
	  public static List getReferralPartners(ReferralSearchCriteria criteria) throws FDResourceException{
		  lookupManagerHome();

			try {
				FDReferralManagerSB sb = managerHome.create();
				return sb.getReferralPartners(criteria);
			} catch (CreateException ce) {
				invalidateManagerHome();
				throw new FDResourceException(ce, "Error creating session bean");
			} catch (RemoteException re) {
				invalidateManagerHome();
				throw new FDResourceException(re, "Error talking to session bean");
			}		

	  }

	  public static List getReferralObjective(ReferralSearchCriteria criteria) throws FDResourceException{
		  lookupManagerHome();

			try {
				FDReferralManagerSB sb = managerHome.create();
				return sb.getReferralObjective(criteria);
			} catch (CreateException ce) {
				invalidateManagerHome();
				throw new FDResourceException(ce, "Error creating session bean");
			} catch (RemoteException re) {
				invalidateManagerHome();
				throw new FDResourceException(re, "Error talking to session bean");
			}		

	  }
	  
	  public static ReferralPromotionModel getReferralPromotionDetails(String userId) throws FDResourceException{
		  lookupManagerHome();

			try {
				FDReferralManagerSB sb = managerHome.create();
				return sb.getReferralPromotionDetails(userId);
			} catch (CreateException ce) {
				invalidateManagerHome();
				throw new FDResourceException(ce, "Error creating session bean");
			} catch (RemoteException re) {
				invalidateManagerHome();
				throw new FDResourceException(re, "Error talking to session bean");
			}		

	  }
	  
	  public static ReferralPromotionModel getReferralPromotionDetailsByRefName(String referral) throws FDResourceException{
		  lookupManagerHome();

			try {
				FDReferralManagerSB sb = managerHome.create();
				return sb.getReferralPromotionDetailsByRefName(referral);
			} catch (CreateException ce) {
				invalidateManagerHome();
				throw new FDResourceException(ce, "Error creating session bean");
			} catch (RemoteException re) {
				invalidateManagerHome();
				throw new FDResourceException(re, "Error talking to session bean");
			}		

	  }
	  
	  public static void sendMails(String recipient_list, String mail_message, FDUser user, String rpid, String serverName) throws FDResourceException{
		  lookupManagerHome();

			try {
				FDReferralManagerSB sb = managerHome.create();
				sb.sendMails(recipient_list, mail_message, user, rpid, serverName);
			} catch (CreateException ce) {
				invalidateManagerHome();
				throw new FDResourceException(ce, "Error creating session bean");
			} catch (RemoteException re) {
				invalidateManagerHome();
				throw new FDResourceException(re, "Error talking to session bean");
			}		

	  }
	  
	  public static List<ManageInvitesModel> getManageInvites(String customerId) throws FDResourceException{
		  lookupManagerHome();

			try {
				FDReferralManagerSB sb = managerHome.create();
				return sb.getManageInvites(customerId);
			} catch (CreateException ce) {
				invalidateManagerHome();
				throw new FDResourceException(ce, "Error creating session bean");
			} catch (RemoteException re) {
				invalidateManagerHome();
				throw new FDResourceException(re, "Error talking to session bean");
			}		

	  }
	  
	  public static List<ErpCustomerCreditModel> getUserCredits(String customerId) throws FDResourceException{
		  lookupManagerHome();

			try {
				FDReferralManagerSB sb = managerHome.create();
				return sb.getUserCredits(customerId);
			} catch (CreateException ce) {
				invalidateManagerHome();
				throw new FDResourceException(ce, "Error creating session bean");
			} catch (RemoteException re) {
				invalidateManagerHome();
				throw new FDResourceException(re, "Error talking to session bean");
			}		

	  }
	  
	  public static List<ManageInvitesModel> getManageInvitesForCRM(String customerId) throws FDResourceException{
		  lookupManagerHome();

			try {
				FDReferralManagerSB sb = managerHome.create();
				return sb.getManageInvitesForCRM(customerId);
			} catch (CreateException ce) {
				invalidateManagerHome();
				throw new FDResourceException(ce, "Error creating session bean");
			} catch (RemoteException re) {
				invalidateManagerHome();
				throw new FDResourceException(re, "Error talking to session bean");
			}		

	  }
	  
	  public static Double getAvailableCredit(String customerId) throws FDResourceException {
		  lookupManagerHome(); 

		  try {
			  FDReferralManagerSB sb = managerHome.create();
			  return sb.getAvailableCredit(customerId);
		  } catch (CreateException ce) {
			  invalidateManagerHome();
			  throw new FDResourceException(ce, "Error creating session bean");
		  } catch (RemoteException re) {
			  invalidateManagerHome();
			  throw new FDResourceException(re, "Error talking to session bean");
		  }		
	  }
	  
	  public static Boolean getReferralDisplayFlag(String customerId) throws FDResourceException {
		  lookupManagerHome(); 

		  try {
			  FDReferralManagerSB sb = managerHome.create();
			  return sb.getReferralDisplayFlag(customerId);
		  } catch (CreateException ce) {
			  invalidateManagerHome();
			  throw new FDResourceException(ce, "Error creating session bean");
		  } catch (RemoteException re) {
			  invalidateManagerHome();
			  throw new FDResourceException(re, "Error talking to session bean");
		  }		
	  }
	  
	  public static List<ReferralPromotionModel> getSettledSales() throws FDResourceException {
		  lookupManagerHome(); 

		  try {
			  FDReferralManagerSB sb = managerHome.create();
			  return sb.getSettledSales();
		  } catch (CreateException ce) {
			  invalidateManagerHome();
			  throw new FDResourceException(ce, "Error creating session bean");
		  } catch (RemoteException re) {
			  invalidateManagerHome();
			  throw new FDResourceException(re, "Error talking to session bean");
		  }		
	  }
	  
	  public static String getReferralLink(String customerId) throws FDResourceException {
		  lookupManagerHome(); 

		  try {
			  FDReferralManagerSB sb = managerHome.create();
			  return sb.getReferralLink(customerId);
		  } catch (CreateException ce) {
			  invalidateManagerHome();
			  throw new FDResourceException(ce, "Error creating session bean");
		  } catch (RemoteException re) {
			  invalidateManagerHome();
			  throw new FDResourceException(re, "Error talking to session bean");
		  }		
	  }
	  
	  public static String getLatestSTLSale(String customerId) throws FDResourceException {
		  lookupManagerHome(); 

		  try {
			  FDReferralManagerSB sb = managerHome.create();
			  return sb.getLatestSTLSale(customerId);
		  } catch (CreateException ce) {
			  invalidateManagerHome();
			  throw new FDResourceException(ce, "Error creating session bean");
		  } catch (RemoteException re) {
			  invalidateManagerHome();
			  throw new FDResourceException(re, "Error talking to session bean");
		  }		
	  }
	  
	  public static boolean isCustomerReferred(String customerId) throws FDResourceException {
		  lookupManagerHome(); 

		  try {
			  FDReferralManagerSB sb = managerHome.create();
			  return sb.isCustomerReferred(customerId);
		  } catch (CreateException ce) {
			  invalidateManagerHome();
			  throw new FDResourceException(ce, "Error creating session bean");
		  } catch (RemoteException re) {
			  invalidateManagerHome();
			  throw new FDResourceException(re, "Error talking to session bean");
		  }		
	  }
	  
	  public static String updateFDUser(String customerId, String zipCode, EnumServiceType serviceType) throws FDResourceException {
		  lookupManagerHome(); 

		  try {
			  FDReferralManagerSB sb = managerHome.create();
			  return sb.updateFDUser(customerId, zipCode, serviceType);
		  } catch (CreateException ce) {
			  invalidateManagerHome();
			  throw new FDResourceException(ce, "Error creating session bean");
		  } catch (RemoteException re) {
			  invalidateManagerHome();
			  throw new FDResourceException(re, "Error talking to session bean");
		  }		
	  }
	  
	  public static void updateCustomerInfo(String customerId, String firstName, String lastName) throws FDResourceException {
		  lookupManagerHome(); 

		  try {
			  FDReferralManagerSB sb = managerHome.create();
			  sb.updateCustomerInfo(customerId, firstName, lastName);
		  } catch (CreateException ce) {
			  invalidateManagerHome();
			  throw new FDResourceException(ce, "Error creating session bean");
		  } catch (RemoteException re) {
			  invalidateManagerHome();
			  throw new FDResourceException(re, "Error talking to session bean");
		  }		
	  }
	  
	  public static void updateCustomerPW(String customerId, String pwdHash) throws FDResourceException {
		  lookupManagerHome(); 

		  try {
			  FDReferralManagerSB sb = managerHome.create();
			  sb.updateCustomerPW(customerId, pwdHash);
		  } catch (CreateException ce) {
			  invalidateManagerHome();
			  throw new FDResourceException(ce, "Error creating session bean");
		  } catch (RemoteException re) {
			  invalidateManagerHome();
			  throw new FDResourceException(re, "Error talking to session bean");
		  }		
	  }
	  
	  public static void updateFdCustomer(String customerId, String pwdHint) throws FDResourceException {
		  lookupManagerHome(); 

		  try {
			  FDReferralManagerSB sb = managerHome.create();
			  sb.updateFdCustomer(customerId, pwdHint);
		  } catch (CreateException ce) {
			  invalidateManagerHome();
			  throw new FDResourceException(ce, "Error creating session bean");
		  } catch (RemoteException re) {
			  invalidateManagerHome();
			  throw new FDResourceException(re, "Error talking to session bean");
		  }		
	  }

	public static void storeFailedAttempt(String email, String dupeCustID, String zipCode, String firstName, String lastName, String referral, String reason) throws FDResourceException {
		lookupManagerHome(); 

		  try {
			  FDReferralManagerSB sb = managerHome.create();
			  sb.storeFailedAttempt(email, dupeCustID, zipCode, firstName, lastName, referral, reason);
		  } catch (CreateException ce) {
			  invalidateManagerHome();
			  throw new FDResourceException(ce, "Error creating session bean");
		  } catch (RemoteException re) {
			  invalidateManagerHome();
			  throw new FDResourceException(re, "Error talking to session bean");
		  }			
	}

	public static boolean isUniqueFNLNZipCombo(String firstName, String lastName, String zipCode, String customerId) throws FDResourceException {
		lookupManagerHome(); 

		  try {
			  FDReferralManagerSB sb = managerHome.create();
			  return sb.isUniqueFNLNZipCombo(firstName, lastName, zipCode, customerId);
		  } catch (CreateException ce) {
			  invalidateManagerHome();
			  throw new FDResourceException(ce, "Error creating session bean");
		  } catch (RemoteException re) {
			  invalidateManagerHome();
			  throw new FDResourceException(re, "Error talking to session bean");
		  }
	}
	
	public static String getReferralName(String referralId) throws FDResourceException {
		  lookupManagerHome(); 

		  try {
			  FDReferralManagerSB sb = managerHome.create();
			  return sb.getReferralName(referralId);
		  } catch (CreateException ce) {
			  invalidateManagerHome();
			  throw new FDResourceException(ce, "Error creating session bean");
		  } catch (RemoteException re) {
			  invalidateManagerHome();
			  throw new FDResourceException(re, "Error talking to session bean");
		  }		
	  }
	
	public static boolean isReferreSignUpComplete(String email) throws FDResourceException {
		  lookupManagerHome(); 

		  try {
			  FDReferralManagerSB sb = managerHome.create();
			  return sb.isReferreSignUpComplete(email);
		  } catch (CreateException ce) {
			  invalidateManagerHome();
			  throw new FDResourceException(ce, "Error creating session bean");
		  } catch (RemoteException re) {
			  invalidateManagerHome();
			  throw new FDResourceException(re, "Error talking to session bean");
		  }		
	  }


}

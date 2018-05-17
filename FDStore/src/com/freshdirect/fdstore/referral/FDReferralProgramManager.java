package com.freshdirect.fdstore.referral;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.freshdirect.fdstore.FDResourceException;

public class FDReferralProgramManager {
		
	
	public static Set getAllReferralPrograms() throws FDResourceException{
		TreeSet refPrgSet=new TreeSet((Collection)FDReferralManager.loadAllReferralPrograms());		   
		return refPrgSet;					 
	}
	
	public static Set getAllReferralChannels() throws FDResourceException{
	    TreeSet refChaSet=new TreeSet((Collection)FDReferralManager.loadAllReferralChannels());		   
		return refChaSet;
	}

	public static Set getAllReferralCampaigns() throws FDResourceException{
		TreeSet refCampSet=new TreeSet((Collection)FDReferralManager.loadAllReferralCampaigns());		   
		return refCampSet;			
	}
	
	public static Set getAllReferralObjectives() throws FDResourceException{
		TreeSet refObjSet=new TreeSet((Collection)FDReferralManager.loadAllReferralObjective());		   
		return refObjSet;		
	}

	public static Set getAllReferralPartners() throws FDResourceException{
		TreeSet refPartSet=new TreeSet((Collection)FDReferralManager.loadAllReferralPartners());		   
		return refPartSet;				
	}
	
	public static ReferralChannel getReferralChannleModel(String refChaId) throws FDResourceException{		  
		return FDReferralManager.getReferralChannleModel(refChaId);
	}

	public static ReferralCampaign getReferralCampaignModel(String refChaId) throws FDResourceException{		  
		return FDReferralManager.getReferralCampaigneModel(refChaId);
	}

	public static ReferralObjective getReferralObjectiveModel(String refChaId) throws FDResourceException{		  
		return FDReferralManager.getReferralObjectiveModel(refChaId);
	}

	public static ReferralPartner getReferralPartnerModel(String refChaId) throws FDResourceException{		  
		return FDReferralManager.getReferralPartnerModel(refChaId);
	}

	public static ReferralProgram getReferralProgramModel(String refChaId) throws FDResourceException{		  
		return FDReferralManager.getReferralProgramModel(refChaId);
	}
	
	public static void updateReferralChannel(ReferralChannel channel) throws FDResourceException{		  
		 FDReferralManager.updateReferralChannel(channel);
	}

	public static void updateReferralCampaign(ReferralCampaign campaign) throws FDResourceException{		  
		 FDReferralManager.updateReferralCampaign(campaign);
	}
	
	public static void updateReferralObjective(ReferralObjective objective) throws FDResourceException{		  
		 FDReferralManager.updateReferralObjective(objective);
	}
	
	public static void updateReferralPartner(ReferralPartner partner) throws FDResourceException{		  
		 FDReferralManager.updateReferralPartner(partner);
	}

	public static void updateReferralProgram(ReferralProgram program) throws FDResourceException{		  
		 FDReferralManager.updateReferralProgram(program);
	}
	
	public static ReferralProgram createReferralProgram(ReferralProgram program) throws FDResourceException{		  
		return FDReferralManager.createReferralProgram(program);
	}


	public static ReferralChannel createReferralChannel(ReferralChannel channel) throws FDResourceException{		  
		return FDReferralManager.createReferralChannel(channel);
	}
	
	public static ReferralCampaign createReferralCampaign(ReferralCampaign campaign) throws FDResourceException{		  
		return FDReferralManager.createReferralCampaign(campaign);
	}
	
	public static ReferralObjective createReferralObjective(ReferralObjective objective) throws FDResourceException{		  
		return FDReferralManager.createReferralObjective(objective);
	}
	
	public static ReferralPartner createReferralPartner(ReferralPartner partner) throws FDResourceException{		  
		return FDReferralManager.createReferralPartner(partner);
	}

	public static void removeReferralChannel(String channelIds[]) throws FDResourceException{		  
		   FDReferralManager.removeReferralChannel(channelIds);
	}
	
	public static void removeReferralObjective(String objIds[]) throws FDResourceException{		  
		   FDReferralManager.removeReferralObjective(objIds);
	}
	
	public static void removeReferralPartner(String partIds[]) throws FDResourceException{		  
		   FDReferralManager.removeReferralPartner(partIds);
	}

	public static void removeReferralCampaign(String campIds[]) throws FDResourceException{		  
		   FDReferralManager.removeReferralCampaign(campIds);
	}

	
	public static void removeReferralProgram(String prgIds[]) throws FDResourceException{		  
		   FDReferralManager.removeReferralProgram(prgIds);
	}
	
	
	public static List getReferralProgarmforRefChannel(String refChaIds[]) throws FDResourceException{
		return FDReferralManager.getReferralProgarmforRefChannel(refChaIds);
	}
	
	public static List getReferralProgarmforRefCampaign(String refCampIds[]) throws FDResourceException{
		return FDReferralManager.getReferralProgarmforRefCampaign(refCampIds);
	}
	
	public static List getReferralProgarmforRefPartner(String refPartIds[]) throws FDResourceException{
		return FDReferralManager.getReferralProgarmforRefPartner(refPartIds);
	}
	
	public static List getReferralCampaignforRefObjective(String refObjIds[]) throws FDResourceException{
		return FDReferralManager.getReferralCampaignforRefObjective(refObjIds);
	}
	

	
	 public static boolean isReferralPartnerNameExist(String refPartName) throws FDResourceException{
		return FDReferralManager.isReferralPartnerNameExist(refPartName);
	 }
	 
	 public static boolean isReferralCampaignNameExist(String refCampName) throws FDResourceException{
		 return FDReferralManager.isReferralCampaignNameExist(refCampName);
	 }
	 
	 public static boolean isReferralObjectiveNameExist(String refObjName) throws FDResourceException{
		 return FDReferralManager.isReferralObjectiveNameExist(refObjName);
	 }
	 
	 public static boolean isReferralChannelNameAndTypeExist(String name,String type)  throws FDResourceException{
		 return FDReferralManager.isReferralChannelNameAndTypeExist(name,type);
	 }
	 
	 public static boolean isReferralProgramNameExist(String refPrgName) throws FDResourceException {
		   return FDReferralManager.isReferralProgramNameExist(refPrgName);

	 }	
	 
	  public static List getReferralPrograms(ReferralSearchCriteria criteria) throws FDResourceException{
		  return FDReferralManager.getReferralPrograms(criteria);
	  }
	  
	  public static List getReferralChannels(ReferralSearchCriteria criteria) throws FDResourceException{
		  return FDReferralManager.getReferralChannels(criteria);
	  }
	  
	  public static List getReferralCampaigns(ReferralSearchCriteria criteria) throws FDResourceException{
		  return FDReferralManager.getReferralCampaigns(criteria);
	  }
	  
	  public static List getReferralPartners(ReferralSearchCriteria criteria) throws FDResourceException{
		  return FDReferralManager.getReferralPartners(criteria);
	  }

	  public static List getReferralObjective(ReferralSearchCriteria criteria) throws FDResourceException{
		  return FDReferralManager.getReferralObjective(criteria);
	  }

}

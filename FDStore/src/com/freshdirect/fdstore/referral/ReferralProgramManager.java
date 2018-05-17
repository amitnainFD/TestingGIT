package com.freshdirect.fdstore.referral;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDException;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.framework.util.ExpiringReference;
import com.freshdirect.framework.util.log.LoggerFactory;

public class ReferralProgramManager {		 	
		 			
	private List internalProgIdList=new ArrayList();
	private static ReferralProgramManager instance = null;
	
	private static Category LOGGER = LoggerFactory.getInstance(ReferralProgramManager.class);
	
	private ExpiringReference refPrgHolder = new ExpiringReference(10 * 60 * 1000) {
		protected Object load() {
			try {
				return loadAllReferralPrograms();
			} catch (FDResourceException e) {
				LOGGER.error("Could not load load Referral program due to: ", e);				
			}
			return Collections.EMPTY_MAP;
		}
	};
		
	private ReferralProgramManager(){		
	}
		
	
	public static synchronized ReferralProgramManager getInstance() throws FDException{
		if(instance==null){
			instance=new ReferralProgramManager();
		}
		return instance;		
	}
	
	public Map loadAllReferralPrograms() throws FDResourceException{		
		Map refPrgMap=new Hashtable(); 	
		List list=FDReferralManager.loadAllReferralPrograms();
		if(list!=null){
			for(int i=0;i<list.size();i++){
				ReferralProgram program=(ReferralProgram)list.get(i);								
				if(program.getChannel()!=null){
						String chaId=program.getChannel().getPK().getId();
						ReferralChannel channel=FDReferralManager.getReferralChannleModel(chaId);						
						program.setChannel(channel);
				}
				if(program.getPartner()!=null){
					String partId=program.getPartner().getPK().getId();
					ReferralPartner partner=FDReferralManager.getReferralPartnerModel(partId);					
					program.setPartner(partner);
				}
				if(program.getCampaign()!=null){
					String campId=program.getCampaign().getPK().getId();
					ReferralCampaign campaign=FDReferralManager.getReferralCampaigneModel(campId);					
					program.setCampaign(campaign);
				}											
				refPrgMap.put(program.getPK().getId(),program);
			}
		}
		 return refPrgMap;
	}								
	
	
	
	
	public boolean isCampaignFromInternalCompany(ReferralCampaign campaign)	{		
		return false;
	}
	
	
	public boolean isInternalReferralInvitee(String programId){
		if(programId==null || programId.trim().length()==0){
			return false;
		}
		
		if(internalProgIdList.contains(programId)){ 	 
			return true;
		}
				
		if(isValidReferralProgram(programId)){			
			Map refPrgMap = (Map) this.refPrgHolder.get();
			ReferralProgram program=(ReferralProgram)refPrgMap.get(programId);												
			
			if(program.getChannel()==null || !program.getChannel().isInternalReferralChannel()){
				return false;
			}
			
			if(program.getPartner()==null || !program.getPartner().isInternalReferralPartner()){
			    return false;		
			}
			
			if(program.getCampaign()==null || !program.getCampaign().isInternalReferralCampaign()){			
				return false;						
			}
			return true;
		}
			
		return false;	    		
	}
	
	
	
	public boolean isValidReferralProgram(String programId)
	{
		Map refPrgMap = (Map) this.refPrgHolder.get();
	    if(refPrgMap.get(programId)!=null){
	    	return true;
	    }
	    
	    return false;
	
	}
	
	public Collection getAllReferralPrograms(){
		  Map refPrgMap = (Map) this.refPrgHolder.get();	
	      return refPrgMap.values();	
	}
	
	
	public ReferralProgram getReferralProgram(String refPrgId) throws FDResourceException
	{	
		Map refPrgMap = (Map) this.refPrgHolder.get();
		return (ReferralProgram)refPrgMap.get(refPrgId);				
	}
					

	
}

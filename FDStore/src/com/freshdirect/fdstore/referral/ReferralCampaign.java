package com.freshdirect.fdstore.referral;

import java.math.BigDecimal;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

public class ReferralCampaign extends ModelSupport implements Comparable {

	
	private String name=null;
	private String description=null;
	private String unicaRefId=null;
	private ReferralObjective objective=null;
	private static final String INT_INVT_CAMP_NME[]={"TELL_A_FRIEND"};
	
	public ReferralCampaign(PrimaryKey key, String name,ReferralObjective objective){
		// check for null and throw exception
	    this.setPK(key);
		this.name=name;
		this.objective=objective;
	}
	
	public ReferralCampaign(PrimaryKey key){
		super();
		this.setPK(key);
		this.name="";
		this.description="";
		this.objective=new ReferralObjective();
	}
	
	public ReferralCampaign(){
		this.name="";
		this.description="";
		this.objective=new ReferralObjective();
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	public boolean isInternalReferralCampaign()
	{
		for(int i=0;i<INT_INVT_CAMP_NME.length;i++){
			if(INT_INVT_CAMP_NME[i].equalsIgnoreCase(this.getName())){
				return true;
			}
		}
		return false;
	}
	
	
	
	public String getName() {
		return name;
	}
	
	

	public ReferralObjective getObjective() {
		return objective;
	}

	public boolean equals(Object o) {
		// TODO Auto-generated method stub

		if(o instanceof ReferralCampaign)
		{
			ReferralCampaign program=(ReferralCampaign)o;						 
			 if(this.getName().equalsIgnoreCase(program.getName()))
			 {
				 return true;
			 }
			 else
		       return super.equals(o);
		}
		else
			return false;
	}
	
	
	public int hashCode() {
		// TODO Auto-generated method stub
		return name.hashCode();
	}

	public String toString() {
		// TODO Auto-generated method stub
		StringBuffer buffer=new StringBuffer();
		buffer.append("\n");
		buffer.append("--------------------------");
		buffer.append("\n");
		buffer.append("id :"+this.getPK());
		buffer.append("\n");
		buffer.append("name :"+this.name);
		buffer.append("\n");
		buffer.append("description :"+this.description);
		buffer.append("\n");
		buffer.append("-------Objective :----------"+this.objective);
		buffer.append("\n");
		buffer.append("--------------------------");		
		return buffer.toString();
	}
	
	public int compareTo(Object o)
	{

		if(o instanceof ReferralCampaign)
		{
			ReferralCampaign program=(ReferralCampaign)o;
			if(this.getPK()!= null && program.getPK()!=null)
			{
			   return new BigDecimal(this.getPK().getId()).compareTo(new BigDecimal(program.getPK().getId()));				   
			}
			else
			{
				return program.getName().compareTo(this.name);
			}
		}
		else
			return -1;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setObjective(ReferralObjective objective) {
		this.objective = objective;
	}

	public String getUnicaRefId() {
		return unicaRefId;
	}

	public void setUnicaRefId(String unicaRefId) {
		this.unicaRefId = unicaRefId;
	}
	
}

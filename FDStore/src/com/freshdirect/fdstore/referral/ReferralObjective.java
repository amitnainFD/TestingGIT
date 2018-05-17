package com.freshdirect.fdstore.referral;



import java.math.BigDecimal;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

public class ReferralObjective extends ModelSupport implements Comparable {
	
	
	private String name=null;
	private String description=null;
	
	public ReferralObjective(PrimaryKey key, String name){
		// check for null and throw exception
		this.setPK(key);		
		this.name=name;
	}

	public ReferralObjective(PrimaryKey key){
		this.setPK(key);
		this.name="";
		this.description="";
	}

	
	public ReferralObjective(){
		this.name="";
		this.description="";
	}
	
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(o instanceof ReferralObjective)
		{
			ReferralObjective program=(ReferralObjective)o;
			 if(this.getName().equalsIgnoreCase(program.getName()))
			 {
				 return true;
			 }
			 else
		       return super.equals(o);
		}
		else{
			return false;
		}
	}

	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
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
		buffer.append("--------------------------");		
		return buffer.toString();
	}

	public int compareTo(Object o){
		if(o instanceof ReferralObjective){
			ReferralObjective program=(ReferralObjective)o;
			if(this.getPK()!=null && program.getPK()!=null){
				return new BigDecimal(this.getPK().getId()).compareTo(new BigDecimal(program.getPK().getId()));
			}
			else{
					return this.name.compareTo(program.getName());
			}
			
		}
		else{
			return -1;
		}
	}
	
}

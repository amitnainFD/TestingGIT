package com.freshdirect.fdstore.referral;

import java.math.BigDecimal;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

public class ReferralPartner extends ModelSupport implements Comparable {
	
	private static final String INT_INVT_COMP_NME[]={"FD"};
	private String name=null;
	private String description=null;
	
	public ReferralPartner(PrimaryKey key, String name)	{
		// check for null and throw exception
		this.setPK(key);
		this.name=name;
	}

	public ReferralPartner(PrimaryKey key){
		// check for null and throw exception
		this.setPK(key);
		this.name="";
		this.description="";
	}

	
	public ReferralPartner()
	{
		this.name="";
		this.description="";
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isInternalReferralPartner(){
		for(int i=0;i<INT_INVT_COMP_NME.length;i++)
			if(INT_INVT_COMP_NME[i].equalsIgnoreCase(this.getName())){
			   return true;
			}
		return false;
	}	
	
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(o instanceof ReferralPartner)
		{
			 ReferralPartner program=(ReferralPartner)o;
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
		buffer.append("\n");
		return buffer.toString();
	}

	public int compareTo(Object o){
		if(o instanceof ReferralPartner)
		{
			ReferralPartner program=(ReferralPartner)o;
			if(this.getPK()!=null && program.getPK()!=null)
			{
				return new BigDecimal(this.getPK().getId()).compareTo(new BigDecimal(program.getPK().getId()));
			}
				else
					return this.name.compareTo(program.getName());
			
		}
		else{
			return -1;
		}
	}

	public void setName(String name) {
		this.name = name;
	}
	
}

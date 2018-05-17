package com.freshdirect.fdstore.referral;

import java.math.BigDecimal;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

public class ReferralChannel extends ModelSupport implements Comparable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	
	private String name=null;
	private String type=null;
	private String description=null;
	
	private static final String INT_INVT_CHANNEL_NME[]={"EMAIL"};
	private static final String INT_INVT_CHANNEL_TYPE[]={"INTERNAL"};

	public ReferralChannel(PrimaryKey key,String name){
		super();
		this.setPK(key);		
		this.name=name;
		
	}	
	
	public ReferralChannel(PrimaryKey key){
		super();
		this.setPK(key);	
		this.name="";
		this.type="";
		this.description="";
	}
	
	public ReferralChannel(){
		this.name="";
		this.type="";
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
	public boolean isInternalReferralChannel(){
		for(int i=0;i<INT_INVT_CHANNEL_NME.length;i++){
			if(INT_INVT_CHANNEL_NME[i].equalsIgnoreCase(this.getName())){
				for(int j=0;j<INT_INVT_CHANNEL_TYPE.length;j++){
				  if(INT_INVT_CHANNEL_TYPE[j].equalsIgnoreCase(this.getType())){
					  return true;	  
				  }
				}
			 }
		 }
	
		return false;
	}
	
	
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(o instanceof ReferralChannel)
		{
			 ReferralChannel program=(ReferralChannel)o;
			 if(this.getName().equalsIgnoreCase(program.getName()))
			 {
				 if(this.getType().equalsIgnoreCase(program.getType()))
				 {
				      return true;
				 }
				 return false;
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
		buffer.append("type :"+this.type);
		buffer.append("\n");
		buffer.append("description :"+this.description);
		buffer.append("\n");
		buffer.append("--------------------------");
		buffer.append("\n");
		return buffer.toString();
	}

	
	public int compareTo(Object o)
	{
		if(o instanceof ReferralChannel)
		{
			ReferralChannel program=(ReferralChannel)o;
			if(this.getPK()!=null && program.getPK()!=null)
			{
			
				return new BigDecimal(this.getPK().getId()).compareTo(new BigDecimal(program.getPK().getId()));
			}
			else
				return this.getName().compareTo(program.getName());			
			
		}
		else
			return -1;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}

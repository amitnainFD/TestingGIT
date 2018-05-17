package com.freshdirect.fdstore.referral;

import java.math.BigDecimal;
import java.util.Date;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

public class ReferralProgram extends ModelSupport implements Comparable {


	private String name=null;
	private String description=null;
	private Date startDate=null;
	private Date expDate=null;
	private String creativeDesc=null;
	private ReferralCampaign campaign=null;
	private ReferralChannel channel=null;
	private ReferralPartner partner=null;
	private EnumReferralProgramStatus status=null;
	private String promotionCode=null;
	private String creativeUrl=null;
	
	public ReferralProgram(PrimaryKey key ,String name,ReferralChannel channel,ReferralCampaign campaign,ReferralPartner partner){
		// check for null and throw exception
	    this.setPK(key);
	    this.name=name;
	    this.partner=partner;
	    this.campaign=campaign;
	    this.channel=channel;
	}
	
	public ReferralProgram(){
		this.name="";
		this.description="";	
		this.creativeDesc="";
		this.promotionCode="";
		this.channel=new ReferralChannel();
		this.partner=new ReferralPartner();
		this.campaign=new ReferralCampaign();
		this.creativeUrl="";
	}
	
	public ReferralProgram(PrimaryKey key)
	{
		this.setPK(key);
	}
	
	public ReferralCampaign getCampaign() {
		return campaign;
	}
	
	public ReferralChannel getChannel() {
		return channel;
	}
	
	public String getCreativeDesc() {
		return creativeDesc;
	}
	public void setCreativeDesc(String creativeDesc) {
		this.creativeDesc = creativeDesc;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getExpDate() {
		return expDate;
	}
	public void setExpDate(Date expDate) {
		this.expDate = expDate;
	}
	
	public String getName() {
		return name;
	}
	
	public ReferralPartner getPartner() {
		return partner;
	}

	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public EnumReferralProgramStatus getStatus() {
		return status;
	}

	public void setStatus(EnumReferralProgramStatus status) {
		this.status = status;
	}
	
	public String getPromotionCode() {
		return promotionCode;
	}

	public void setPromotionCode(String promotionCode) {
		this.promotionCode = promotionCode;
	}

	

	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(o instanceof ReferralProgram)
		{
			 ReferralProgram program=(ReferralProgram)o;
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
		buffer.append("--------Referral Program------------------");
		buffer.append("\n");
		buffer.append("id :"+this.getPK());
		buffer.append("\n");
		buffer.append("name :"+this.name);
		buffer.append("\n");
		buffer.append("description :"+this.description);
		buffer.append("\n");
		buffer.append("status :"+this.status);
		buffer.append("\n");
		buffer.append("creativeDesc :"+this.creativeDesc);
		buffer.append("\n");
		buffer.append("-----Campaign :----"+this.campaign);
		buffer.append("\n");
		buffer.append("------Channel :-----"+this.channel);
		buffer.append("\n");
		buffer.append("------Partner :-----"+this.partner);
		buffer.append("\n");
		buffer.append("------Promotion :-----"+this.promotionCode);
		buffer.append("\n");
		buffer.append("--------------------------");		
		return buffer.toString();
	}

	public int compareTo(Object o)
	{
		if(o instanceof ReferralProgram)
		{
			ReferralProgram program=(ReferralProgram)o;
			if(this.getPK()!=null && program.getPK()!=null)
			{
				return new BigDecimal(this.getPK().getId()).compareTo(new BigDecimal(program.getPK().getId()));
			}
				else
					return this.name.compareTo(program.getName());
			
		}
		else
			return -1;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCampaign(ReferralCampaign campaign) {
		this.campaign = campaign;
	}

	public void setChannel(ReferralChannel channel) {
		this.channel = channel;
	}

	public void setPartner(ReferralPartner partner) {
		this.partner = partner;
	}

	public String getCreativeUrl() {
		return creativeUrl;
	}

	public void setCreativeUrl(String creativeUrl) {
		this.creativeUrl = creativeUrl;
	}
	
	
	
}

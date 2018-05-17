package com.freshdirect.fdstore.standingorders;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import com.freshdirect.fdlogistics.model.FDTimeslot;


public class FDStandingOrderInfo implements Serializable  {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -50754633558052125L;
	
	private String soID;
	private String customerId;	
	private Date nextDate;
	private String userId;
	private String companyName;
	private String address;
	private String businessPhone;
	private String cellPhone;
	private Integer frequency;
	private String lastError;
	private String errorHeader;
	private Date startTime;
	private Date endTime;
	private Date failedOn;
	private String paymentMethod;
	private String soName;
	private String zone;
	
	public String getZone() {
		return zone;
	}
	public void setZone(String zone) {
		this.zone = zone;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getBusinessPhone() {
		return businessPhone;
	}
	public void setBusinessPhone(String businessPhone) {
		this.businessPhone = businessPhone;
	}
	public String getCellPhone() {
		return cellPhone;
	}
	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}
	
	public String getSoID() {
		return soID;
	}
	public void setSoID(String soID) {
		this.soID = soID;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public Date getNextDate() {
		return nextDate;
	}
	public void setNextDate(Date nextDate) {
		this.nextDate = nextDate;
	}
	public Integer getFrequency() {
		return frequency;
	}
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}
	public String getLastError() {
		return lastError;
	}
	public void setLastError(String lastError) {
		this.lastError = lastError;
	}
	public String getErrorHeader() {
		return errorHeader;
	}
	public void setErrorHeader(String errorHeader) {
		this.errorHeader = errorHeader;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public String getTimeslotDisplayString(){
		String displayString = null;
		if(null == displayString){
			displayString = FDTimeslot.getDisplayString(true,this.getStartTime(),this.getEndTime());
		}
		return displayString;
	}
	public final static Comparator<FDStandingOrderInfo> COMP_ID = new Comparator<FDStandingOrderInfo>() {
		public int compare(FDStandingOrderInfo c1, FDStandingOrderInfo c2) {
			if(null !=c1.getSoID() && null != c2.getSoID()){
				return c1.getSoID().compareTo(c2.getSoID());
			}else{
				return 0;
			}
		}
	};
	
	public final static Comparator<FDStandingOrderInfo> COMP_USER_ID = new Comparator<FDStandingOrderInfo>() {
		public int compare(FDStandingOrderInfo c1, FDStandingOrderInfo c2) {
			if(null !=c1.getUserId() && null != c2.getUserId()){
				return c1.getUserId().toLowerCase().compareTo(c2.getUserId().toLowerCase());
			}else{
				return 0;
			}
		}
	};
	
	public final static Comparator<FDStandingOrderInfo> COMP_COMPANY = new Comparator<FDStandingOrderInfo>() {
		public int compare(FDStandingOrderInfo c1, FDStandingOrderInfo c2) {
			if(c1.getCompanyName() == null) return 1;
	        if(c2.getCompanyName() == null) return -1;
			return c1.getCompanyName().toLowerCase().compareTo(c2.getCompanyName().toLowerCase());			
		}
	};

	public final static Comparator<FDStandingOrderInfo> COMP_FREQUENCY = new Comparator<FDStandingOrderInfo>() {
		public int compare(FDStandingOrderInfo c1, FDStandingOrderInfo c2) {
			if(null !=c1.getFrequency() && null != c2.getFrequency()){
				return c1.getFrequency().compareTo(c2.getFrequency());
			}else{
				return 0;
			}
		}
	};
	
	public final static Comparator<FDStandingOrderInfo> COMP_TIMESLOT = new Comparator<FDStandingOrderInfo>() {
		public int compare(FDStandingOrderInfo c1, FDStandingOrderInfo c2) {
			if(null !=c1.getTimeslotDisplayString() && null != c2.getTimeslotDisplayString()){
				return c1.getTimeslotDisplayString().toLowerCase().compareTo(c2.getTimeslotDisplayString().toLowerCase());
			}else{
				return 0;
			}
		}
	};
	
	public final static Comparator<FDStandingOrderInfo> COMP_NEXT_DATE = new Comparator<FDStandingOrderInfo>() {
		public int compare(FDStandingOrderInfo c1, FDStandingOrderInfo c2) {
			return c1.getNextDate().compareTo(c2.getNextDate()); 	
		}
	};
	
	public final static Comparator<FDStandingOrderInfo> COMP_WEEKDAY = new Comparator<FDStandingOrderInfo>() {
		public int compare(FDStandingOrderInfo c1, FDStandingOrderInfo c2) {
			Calendar c1c=Calendar.getInstance();
			c1c.setTime(c1.getNextDate());
			Calendar c2c=Calendar.getInstance();
			c2c.setTime(c2.getNextDate());
			
			return c1c.get(Calendar.DAY_OF_WEEK) > c2c.get(Calendar.DAY_OF_WEEK) ? 1 : c1c.get(Calendar.DAY_OF_WEEK) < c2c.get(Calendar.DAY_OF_WEEK) ? -1 : 0;
		}
	};
		
	
	public final static Comparator<FDStandingOrderInfo> COMP_LAST_ERROR = new Comparator<FDStandingOrderInfo>() {
		public int compare(FDStandingOrderInfo c1, FDStandingOrderInfo c2) {
			if(c1.getLastError() == null) return 1;
	        if(c2.getLastError() == null) return -1;
			return c1.getLastError().toLowerCase().compareTo(c2.getLastError().toLowerCase());
			
		}
	};

	public Date getFailedOn() {
		return failedOn;
	}
	public void setFailedOn(Date failedOn) {
		this.failedOn = failedOn;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public String getSoName() {
		return soName;
	}
	public void setSoName(String soName) {
		this.soName = soName;
	}
}

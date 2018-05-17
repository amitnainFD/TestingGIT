package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import com.freshdirect.framework.util.DateUtil;

public class FDComplaintReportCriteria implements Serializable {
	private String startMonth;
	private String startDay;
	private String startYear;
	
	private String endMonth;
	private String endDay;
	private String endYear;
	
	boolean storeCredits = false;
	boolean cashbacks = false;
	
	private String issuedBy;
	private String approvedBy;
	
	public String getStartMonth(){
		return this.startMonth;
	}
	
	public void setStartMonth(String startMonth) {
		this.startMonth = startMonth;
	}
	
	public String getStartDay(){
		return this.startDay;
	}
	
	public void setStartDay(String startDay){
		this.startDay = startDay;
	}
	
	public String getStartYear(){
		return this.startYear;
	}
	
	public void setStartYear(String startYear){
		this.startYear = startYear;
	}
	
	public Date getStartDate(){
		return this.getDate(this.startMonth, this.startDay, this.startYear);
	}
	
	public String getEndMonth(){
		return this.endMonth;
	}
	
	public void setEndMonth(String endMonth){
		this.endMonth = endMonth;
	}
	
	public String getEndDay(){
		return this.endDay;
	}
	
	public void setEndDay(String endDay){
		this.endDay = endDay;
	}
	
	public String getEndYear(){
		return this.endYear;
	}
	
	public void setEndYear(String endYear){
		this.endYear = endYear;
	}
	
	public Date getEndDate() {
		return this.getDate(this.endMonth, this.endDay, this.endYear);
	}
	
	public boolean isStoreCredit(){
		return this.storeCredits;
	}
	
	public void setStoreCredits(boolean storeCredits){
		this.storeCredits = storeCredits;
	}
	
	public boolean isCashbacks(){
		return this.cashbacks;
	}
	
	public void setCashbacks(boolean cashbacks){
		this.cashbacks = cashbacks;
	}
	
	public String getIssuedBy(){
		return this.issuedBy;
	}
	
	public void setIssuedBy(String issuedBy){
		this.issuedBy = issuedBy;
	}
	
	public String getApprovedBy(){
		return this.approvedBy;
	}
	
	public void setApprovedBy(String approvedBy){
		this.approvedBy = approvedBy;
	}
	
	private Date getDate(String month, String day, String year){
		Date date = null;
		if(month != null && !"".equals(month) && day != null && !"".equals(day) && year != null && !"".equals(year)){
			Calendar cal = DateUtil.truncate(Calendar.getInstance());
			cal.set(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
			date = cal.getTime();
		}
		
		return date;
	}
	
	public boolean isBlank(){
		return "".equals(this.startMonth) && "".equals(this.startDay) && "".equals(this.startYear)
			&& "".equals(this.endMonth) && "".equals(this.endDay) && "".equals(this.endYear)
			&& "".equals(this.issuedBy) && "".equals(this.approvedBy); 
		
	}
	
	public String toString() {

		StringBuffer buf = new StringBuffer();
		buf.append("[FDComplaintSearchCriteria Start Month: ").append(this.startMonth);
		buf.append(" Start Day: ").append(this.startDay);
		buf.append(" Start Year: ").append(this.startYear).append("\n");
		buf.append(" End Month: ").append(this.endMonth);
		buf.append(" End Day: ").append(this.endDay);
		buf.append(" End Year: ").append(this.endYear).append("\n");
		buf.append(" StoreCredits: ").append(this.storeCredits);
		buf.append(" Cashbacks: ").append(this.cashbacks);
		buf.append(" Issued By: ").append(this.issuedBy);
		buf.append(" Approved By: ").append(this.approvedBy).append("]");

		return buf.toString();
	}

}

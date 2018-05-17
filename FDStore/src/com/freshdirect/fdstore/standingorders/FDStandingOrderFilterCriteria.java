package com.freshdirect.fdstore.standingorders;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.freshdirect.framework.util.DateUtil;

public class FDStandingOrderFilterCriteria implements Serializable {

	private String id;
	private Integer frequency;
	private String errorType;
	private Integer dayOfWeek;
	private boolean activeOnly = true;
	private Date fromDate;
	private String fromDateStr;
	private Date toDate;
	private String toDateStr;
	
	
	
	public FDStandingOrderFilterCriteria() {
	
	}

	public FDStandingOrderFilterCriteria(Integer frequency, String errorType,
			Integer dayOfWeek) {	
		this.frequency = frequency;
		this.errorType = errorType;
		this.dayOfWeek = dayOfWeek;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getFrequency() {
		return frequency;
	}


	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}


	public String getErrorType() {
		return errorType;
	}


	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}


	public Integer getDayOfWeek() {
		return dayOfWeek;
	}


	public void setDayOfWeek(Integer dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	
	public boolean isEmpty(){
		boolean isEmtpy = true;
		if((null != frequency) 
				|| (null != errorType && !errorType.trim().equalsIgnoreCase(""))
				|| (null != dayOfWeek )||!activeOnly
				|| null != fromDateStr || null !=toDateStr)
				{
			isEmtpy = false;
		}
		return isEmtpy;
	}

	public boolean isActiveOnly() {
		return activeOnly;
	}

	public void setActiveOnly(boolean activeOnly) {
		this.activeOnly = activeOnly;
	}

	public Date getFromDate() {
		return getFormattedDate(fromDateStr, "00:00 AM");
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public String getFromDateStr() {
		return fromDateStr;
	}

	public void setFromDateStr(String dateStr) {
		this.fromDateStr = dateStr;
	}

	private Date getFormattedDate(String dateStr, String time){
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
			Date date = sdf.parse(dateStr+" "+time);
			Calendar cal = DateUtil.toCalendar(date);				
			return cal.getTime();
		} catch (ParseException pe) { }
	
	
		return null;
	}

	public Date getToDate() {
		return getFormattedDate(toDateStr, "11:59 PM");
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public String getToDateStr() {
		return toDateStr;
	}

	public void setToDateStr(String toDateStr) {
		this.toDateStr = toDateStr;
	}
	
	
}

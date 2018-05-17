/*
 * Created on Jun 17, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import com.freshdirect.framework.util.DateUtil;

/**
 * @author rgayle
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LateDlvReportLine implements Serializable{
	private String waveNumber;
	private String truckNumber;
	private String stopSequence;
	private String orderNumber;
	private String firstName;
	private String lastName;
	private String source;
	private boolean chefsTable;
	private boolean undeclared;
	private Date timeCaseOpened;
	private Date startTime;
	private Date endTime;
	public boolean isChefsTable() {
		return chefsTable;
	}

	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public String getSource() {
		return source;
	}

	public String getStopSequence() {
		return stopSequence;
	}

	public String getWaveNumber() {
		return waveNumber;
	}

	public String getTruckNumber() {
		return truckNumber;
	}

	public boolean isUndeclared() {
		return undeclared;
	}

	public Date getTimeCaseOpened() {
		return timeCaseOpened;
	}

	public Date getStartTime() {
		return startTime;
	}
	
	public Date getEndTime() {
		return endTime;
	}

	public String getDisplayableStartTime() {
		int startDlvHour = (DateUtil.toCalendar(startTime)).get(Calendar.HOUR_OF_DAY);
		String rtnString=null;
		if (startDlvHour ==12) {
			rtnString="noon";
		} else if (startDlvHour<12) {
			rtnString=(startDlvHour==0 ? 12 : startDlvHour)+"am";
		} else {
			rtnString=startDlvHour-12+"pm";
		}
		return rtnString;
	}
	
	public String getDisplayableEndTime() {
		int endDlvHour = (DateUtil.toCalendar(endTime)).get(Calendar.HOUR_OF_DAY);
		String rtnString=null;
		if (endDlvHour ==12) {
			rtnString="noon";
		} else if (endDlvHour<12) {
			rtnString=(endDlvHour==0 ? 12 : endDlvHour)+"am";
		} else {
			rtnString=endDlvHour-12+"pm";
		}
		return rtnString;
	}
	
	public void setChefsTable(boolean b) {
		chefsTable = b;
	}

	public void setFirstName(String string) {
		firstName = string;
	}
	public void setLastName(String string) {
		lastName = string;
	}

	public void setOrderNumber(String string) {
		orderNumber = string;
	}

	public void setSource(String string) {
		source = string;
	}

	public void setStopSequence(String string) {
		stopSequence = string;
	}

	public void setTruckNumber(String string) {
		truckNumber = string;
	}

	public void setWaveNumber(String string) {
		waveNumber = string;
	}

	public void setUndeclared(boolean b) {
		undeclared = b;
	}


	public void setTimeCaseOpened(Date tco) {
		timeCaseOpened =tco;
	}
	
	public void setStartTime(Date sTime) {
		startTime =sTime;
	}
	public void setEndTime(Date eTime) {
		endTime =eTime;
	}


}

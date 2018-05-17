package com.freshdirect.fdstore.standingorders;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.freshdirect.framework.core.ModelSupport;

public class FDStandingOrderAltDeliveryDate extends ModelSupport {

	private static final long serialVersionUID = -3087295029694549320L;
	private static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	private static DateFormat timeFormat = new SimpleDateFormat("hh:mm a");
	private static DateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
	
	private Date origDate;
	private Date altDate;
	private String description;
	private Date origStartTime;
	private Date origEndTime;
	private Date altStartTime;
	private Date altEndTime;
	private String origStartTimeStr;
	private String origEndTimeStr;
	private String altStartTimeStr;
	private String altEndTimeStr;
	private String SoId;
	private String actionType;
	private String createdBy;
	private String modifiedBy;
	private Date createdTime;
	private Date modifiedTime;
	

	public DateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	public Date getOrigDate() {
		return origDate;
	}

	public String getOrigDateFormatted() {
		return null !=origDate ? dateFormat.format(origDate) :"";
	}

	public void setOrigDate(Date origDate) {
		this.origDate = origDate;
	}
	
	public Date getAltDate() {
		return altDate;
	}
	
	public String getAltDateFormatted() {
		return null !=altDate ? dateFormat.format(altDate) :"";
	}
	
	public void setAltDate(Date altDate) {
		this.altDate = altDate;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String toString(){
		return String.format("'" + description + "' (original: " + getOrigDateFormatted() +", alternative: " + getAltDateFormatted() +", soId: " + getSoId()+", actionType: " + getActionType()+ ")");
	}

	public Date getOrigStartTime() {
		return origStartTime;
	}

	public String getOrigStartTimeFormatted() {
		return null !=origStartTime ?timeFormat.format(origStartTime):"";
	}
	
	public void setOrigStartTime(Date origStartTime) {
		this.origStartTime = origStartTime;
		this.origStartTimeStr = null !=origStartTime ?timeFormat.format(origStartTime):"";
	}

	public Date getOrigEndTime() {
		return origEndTime;
	}

	public String getOrigEndTimeFormatted() {
		return null !=origEndTime ?timeFormat.format(origEndTime):"";
	}
	
	public void setOrigEndTime(Date origEndTime) {
		this.origEndTime = origEndTime;
		this.origEndTimeStr = null !=origEndTime ?timeFormat.format(origEndTime):"";
	}

	public Date getAltStartTime() {
		return altStartTime;
	}

	public String getAltStartTimeFormatted() {
		return null !=altStartTime ?timeFormat.format(altStartTime):"";
	}
	
	public void setAltStartTime(Date altStartTime) {
		this.altStartTime = altStartTime;
		this.altStartTimeStr = null !=altStartTime ?timeFormat.format(altStartTime):"";
	}

	public Date getAltEndTime() {
		return altEndTime;
	}

	public String getAltEndTimeFormatted() {
		return null !=altEndTime ?timeFormat.format(altEndTime):"";
	}
	
	public void setAltEndTime(Date altEndTime) {
		this.altEndTime = altEndTime;
		this.altEndTimeStr = null !=altEndTime ?timeFormat.format(altEndTime):"";
	}

	public String getSoId() {
		return SoId;
	}

	public void setSoId(String soId) {
		SoId = soId;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	

	/*public String getStartDateFormatted() {
		return null !=startDate ?dateFormat.format(startDate):"";
	}*/
	
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Date getModifiedTime() {
		return modifiedTime;
	}
	
	public String getModifiedTimeStr(){
		return (null !=modifiedTime ?dateTimeFormat.format(modifiedTime):"");
	}

	public void setModifiedTime(Date modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	public DateFormat getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(DateFormat timeFormat) {
		this.timeFormat = timeFormat;
	}

	public String getOrigStartTimeStr() {
		return origStartTimeStr;
	}

	public void setOrigStartTimeStr(String origStartTimeStr) {
		this.origStartTimeStr = origStartTimeStr;
		try {
			this.origStartTime = timeFormat.parse(origStartTimeStr);
		} catch (ParseException e) {
		}
	}

	public String getOrigEndTimeStr() {
		return origEndTimeStr;
	}

	public void setOrigEndTimeStr(String origEndTimeStr) {
		this.origEndTimeStr = origEndTimeStr;
		try {
			this.origEndTime = timeFormat.parse(origEndTimeStr);
		} catch (ParseException e) {
		}
	}

	public String getAltStartTimeStr() {
		return altStartTimeStr;
	}

	public void setAltStartTimeStr(String altStartTimeStr) {
		this.altStartTimeStr = altStartTimeStr;
		try {
			this.altStartTime = timeFormat.parse(altStartTimeStr);
		} catch (ParseException e) {
		}
	}

	public String getAltEndTimeStr() {
		return altEndTimeStr;
	}

	public void setAltEndTimeStr(String altEndTimeStr) {
		this.altEndTimeStr = altEndTimeStr;
		try {
			this.altEndTime = timeFormat.parse(altEndTimeStr);
		} catch (ParseException e) {
		}
	}

	


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 0;
		result = prime * result + ((SoId == null) ? 0 : SoId.hashCode());
		result = prime * result
				+ ((origDate == null) ? 0 : origDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		FDStandingOrderAltDeliveryDate other = (FDStandingOrderAltDeliveryDate) obj;
		if (SoId == null) {
			if (other.SoId != null)
				return false;
		} else if (!SoId.equals(other.SoId))
			return false;
		if (origDate == null) {
			if (other.origDate != null)
				return false;
		} else if (!origDate.equals(other.origDate))
			return false;
		return true;
	}
}

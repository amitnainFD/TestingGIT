package com.freshdirect.fdstore.notification;

import java.util.Date;

import com.freshdirect.framework.core.ModelSupport;

public class FDNotification extends ModelSupport implements Comparable<FDNotification>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5934546406148571887L;
	
	private String message;
	
	private boolean cancelable = true;
	
	private Date validFrom;
	
	private Date validTill;
	
	private boolean important = false;
	

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isCancelable() {
		return cancelable;
	}

	public void setCancelable(boolean cancelable) {
		this.cancelable = cancelable;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidTill() {
		return validTill;
	}

	public void setValidTill(Date validTill) {
		this.validTill = validTill;
	}

	public boolean isImportant() {
		return important;
	}

	public void setImportant(boolean important) {
		this.important = important;
	}

	@Override
	public int compareTo(FDNotification arg0) {
		return arg0.isImportant() ? 1 : this.isImportant() ? -1 : 0;
	}

}

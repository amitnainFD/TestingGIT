package com.freshdirect.fdstore.standingorders.ejb;

public class AuditDataBeanInfo {
	
	private String userId;
	private String type;
	private String comment;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

}

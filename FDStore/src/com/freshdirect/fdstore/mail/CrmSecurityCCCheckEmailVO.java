package com.freshdirect.fdstore.mail;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class CrmSecurityCCCheckEmailVO implements Serializable{
	
	private List<CrmSecurityCCCheckInfo> ccCheckInfo;
	private String forDate;
	private String asOfTime;
	private Integer ccInfoSize;
	private Integer eCheckInfoSize;
	private List<CrmSecurityCCCheckInfo> eCheckInfo;
	private List<CrmSecurityCCCheckInfo> ccInfo;
	
	public List<CrmSecurityCCCheckInfo> getCcCheckInfo() {
		return ccCheckInfo;
	}
	public void setCcCheckInfo(List<CrmSecurityCCCheckInfo> ccCheckInfo) {
		this.ccCheckInfo = ccCheckInfo;
	}
	public String getForDate() {
		return forDate;
	}
	public void setForDate(String forDate) {
		this.forDate = forDate;
	}
	public String getAsOfTime() {
		return asOfTime;
	}
	public void setAsOfTime(String asOfTime) {
		this.asOfTime = asOfTime;
	}	
	
	public int getCcCheckInfoSize(){
		return (null != ccInfoSize && null != eCheckInfoSize? (ccInfoSize+eCheckInfoSize):0);
	}
	public Integer getCcInfoSize() {
		return ccInfoSize;
	}
	public void setCcInfoSize(Integer ccInfoSize) {
		this.ccInfoSize = ccInfoSize;
	}
	public Integer getECheckInfoSize() {
		return eCheckInfoSize;
	}
	public void setECheckInfoSize(Integer checkInfoSize) {
		eCheckInfoSize = checkInfoSize;
	}
	public List<CrmSecurityCCCheckInfo> getECheckInfo() {
		return eCheckInfo;
	}
	public void setECheckInfo(List<CrmSecurityCCCheckInfo> checkInfo) {
		eCheckInfo = checkInfo;
	}
	public List<CrmSecurityCCCheckInfo> getCcInfo() {
		return ccInfo;
	}
	public void setCcInfo(List<CrmSecurityCCCheckInfo> ccInfo) {
		this.ccInfo = ccInfo;
	}
	

}

package com.freshdirect.fdstore.coremetrics.mobileanalytics;

public class CJVFContextHolder {
	
	private String fdUserId;
	private int cjvfStandingOrderFlag = 0;
	
	public CJVFContextHolder(String fdUserId, int cjvfStandingOrderFlag) {
		this.fdUserId = fdUserId;
		this.cjvfStandingOrderFlag = cjvfStandingOrderFlag;
	}
	
	public String getFdUserId() {
		return fdUserId;
	}
	
	public void setFdUserId(String fdUserId) {
		this.fdUserId = fdUserId;
	}
	
	public int getCjvfStandingOrderFlag() {
		return cjvfStandingOrderFlag;
	}
	
	public void setCjvfStandingOrderFlag(int cjvfStandingOrderFlag) {
		this.cjvfStandingOrderFlag = cjvfStandingOrderFlag;
	}
	
	public void thisIsFirstStandingOrder() {
		this.cjvfStandingOrderFlag = 7;
	}
	
	public void thisIsRepeatedStandingOrder() {
		this.cjvfStandingOrderFlag = 3;
	}

	public void thisIsSubsequentTag() {
		this.cjvfStandingOrderFlag = 1;
	}
}

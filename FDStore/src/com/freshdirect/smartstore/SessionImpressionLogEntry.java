package com.freshdirect.smartstore;

import java.io.Serializable;
import java.util.Date;

public class SessionImpressionLogEntry implements Serializable {
	private static final long serialVersionUID = -8122807897703059351L;

	String id;

	String userPrimaryKey;

	String sessionId;

	Date startTime;

	Date endTime;

	String variantId;

	int productImpressions;

	int featureImpressions;
	
	int tabImpressions;
	
	private String zoneId;

	public SessionImpressionLogEntry() {
		super();
		productImpressions = 0;
		featureImpressions = 0;
	}
	
	/**
	 * This constructor should be used when the entry first created by the session
	 * 
	 * @param userPrimaryKey
	 * @param sessionId
	 * @param variantId
	 */
	public SessionImpressionLogEntry(String userPrimaryKey, String sessionId,
			String variantId) {
		this();
		this.userPrimaryKey = userPrimaryKey;
		this.sessionId = sessionId;
		this.variantId = variantId;
	}

	/**
	 * This constructor should be used by the EJB, which loads the entry from
	 * the DB
	 * 
	 * @param id
	 * @param userPrimaryKey
	 * @param sessionId
	 * @param startTime
	 * @param endTime
	 * @param variantId
	 * @param productImpressions
	 * @param featureImpressions
	 */
	public SessionImpressionLogEntry(String id, String userPrimaryKey,
			String sessionId, Date startTime, Date endTime, String variantId,
			int productImpressions, int featureImpressions) {
		super();
		this.id = id;
		this.userPrimaryKey = userPrimaryKey;
		this.sessionId = sessionId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.variantId = variantId;
		this.productImpressions = productImpressions;
		this.featureImpressions = featureImpressions;
	}

	public String getId() {
		return id;
	}

	public String getUserPrimaryKey() {
		return userPrimaryKey;
	}

	public void setUserPrimaryKey(String userPrimaryKey) {
		this.userPrimaryKey = userPrimaryKey;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Date getStartTime() {
		return startTime;
	}


	public Date getEndTime() {
		return endTime;
	}


	public String getVariantId() {
		return variantId;
	}

	public void setVariantId(String variantId) {
		this.variantId = variantId;
	}

	public int getProductImpressions() {
		return productImpressions;
	}

	public int getFeatureImpressions() {
		return featureImpressions;
	}
	
	public int getTabImpressions() {
            return tabImpressions;
        }
	

	public void incrementImpressions(int productCount) {
		this.productImpressions += productCount;
		if (productCount >= 1) {
			this.featureImpressions++;
		}
	}
	
	public void incrementTabImpressions(int tabs) {
	    this.tabImpressions += tabs;
            if (tabs >= 1) {
                this.featureImpressions ++;
            }
	}

    public void setStartEndTime(Date startDate, Date endDate) {
        this.startTime = startDate;
        this.endTime = endDate;
        
    }

	public String getZoneId() {
		return zoneId;
	}

	public void setZoneId(String zoneId) {
		this.zoneId = zoneId;
	}
}

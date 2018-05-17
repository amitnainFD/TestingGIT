package com.freshdirect.fdstore.iplocator;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IpLocatorEventDTO implements Serializable {

	private static final long serialVersionUID = 4734489150750323386L;

	private String id;
	private Long timestamp;
	private String ip;
	private String ipLocZipCode;
	private String ipLocCountry;
	private String ipLocRegion;
	private String ipLocCity;
	private String fdUserId;
	private String fdZipCode;
	private String fdState;
	private String fdCity;
	private String userAgent;
	private Integer uaHashPercent;
	private Integer iplocRolloutPercent;
	
	public String toString(){
		return String.format("id: %s, timestamp: %s, ip: %s, ipLocZipCode: %s, ipLocCountry: %s, ipLocRegion: %s, ipLocCity: %s, fdUserId: %s, fdZipCode: %s, fdState: %s, fdCity: %s, userAgent: %s, uaHashPercent: %d, iplocRolloutPercent: %d",
				id, timestamp==null ? null: new SimpleDateFormat().format(new Date(timestamp)), ip, ipLocZipCode, ipLocCountry, ipLocRegion, ipLocCity, fdUserId, fdZipCode, fdState, fdCity, userAgent, uaHashPercent, iplocRolloutPercent);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIpLocZipCode() {
		return ipLocZipCode;
	}

	public void setIpLocZipCode(String ipLocZipCode) {
		this.ipLocZipCode = ipLocZipCode;
	}

	public String getIpLocCountry() {
		return ipLocCountry;
	}

	public void setIpLocCountry(String ipLocCountry) {
		this.ipLocCountry = ipLocCountry;
	}

	public String getIpLocRegion() {
		return ipLocRegion;
	}

	public void setIpLocRegion(String ipLocRegion) {
		this.ipLocRegion = ipLocRegion;
	}

	public String getIpLocCity() {
		return ipLocCity;
	}

	public void setIpLocCity(String ipLocCity) {
		this.ipLocCity = ipLocCity;
	}

	public String getFdUserId() {
		return fdUserId;
	}

	public void setFdUserId(String fdUserId) {
		this.fdUserId = fdUserId;
	}

	public String getFdZipCode() {
		return fdZipCode;
	}

	public void setFdZipCode(String fdZipCode) {
		this.fdZipCode = fdZipCode;
	}

	public String getFdState() {
		return fdState;
	}

	public void setFdState(String fdState) {
		this.fdState = fdState;
	}

	public String getFdCity() {
		return fdCity;
	}

	public void setFdCity(String fdCity) {
		this.fdCity = fdCity;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public int getUaHashPercent() {
		return uaHashPercent;
	}

	public void setUaHashPercent(Integer uaHashPercent) {
		this.uaHashPercent = uaHashPercent;
	}

	public int getIplocRolloutPercent() {
		return iplocRolloutPercent;
	}

	public void setIplocRolloutPercent(Integer iplocRolloutPercent) {
		this.iplocRolloutPercent = iplocRolloutPercent;
	}

}


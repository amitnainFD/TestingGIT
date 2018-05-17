package com.freshdirect.fdstore.content.productfeed;

import java.io.Serializable;

public class ProductFeedSubscriber implements Serializable {
	
	private String code;
	private String description;
	private ProductFeedSubscriberType type;
	private String url;
	private String userid;
	private String password;
	private String defaultUploadPath;
	
	public ProductFeedSubscriber(String code, String description,
			ProductFeedSubscriberType type, String url, String userid,
			String password, String defaultUploadPath) {
		super();
		this.code = code;
		this.description = description;
		this.type = type;
		this.url = url;
		this.userid = userid;
		this.password = password;
		this.defaultUploadPath = defaultUploadPath;
	}
	
	public String getCode() {
		return code;
	}
	public String getDescription() {
		return description;
	}
	public ProductFeedSubscriberType getType() {
		return type;
	}
	public String getUrl() {
		return url;
	}
	public String getUserid() {
		return userid;
	}
	public String getPassword() {
		return password;
	}

	public String getDefaultUploadPath() {
		return defaultUploadPath;
	}

	@Override
	public String toString() {
		return "ProductFeedSubscriber [code=" + code + ", description="
				+ description + ", type=" + type + ", url=" + url + ", userid="
				+ userid + ", password=" + password + ", defaultUploadPath="
				+ defaultUploadPath + "]";
	}
	
	
}

package com.freshdirect.fdstore.temails;

import java.util.Date;

import com.freshdirect.fdstore.util.EnumSiteFeature;

public class TEmailsSmartEmailInfo {

	private String customerId;
	private EnumSiteFeature siteFeature;
	private String variantId;
	private String productId;
	private int position;
	private String productName;
	private String link;
	private String imagePath;
	private int imageWidth;
	private int imageHeight;
	private String rating;
	private String price;
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getWasPrice() {
		return wasPrice;
	}
	public void setWasPrice(String wasPrice) {
		this.wasPrice = wasPrice;
	}
	public String getTieredPrice() {
		return tieredPrice;
	}
	public void setTieredPrice(String tieredPrice) {
		this.tieredPrice = tieredPrice;
	}
	public String getAboutPrice() {
		return aboutPrice;
	}
	public void setAboutPrice(String aboutPrice) {
		this.aboutPrice = aboutPrice;
	}
	private String wasPrice;
	private String tieredPrice;
	private String aboutPrice;
	private String burst;
	private Date lastModified;
	
	
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getSiteFeature() {
		if(siteFeature==null) return EnumSiteFeature.DYF.getName();
		else return siteFeature.getName();
	}
	public void setSiteFeature(EnumSiteFeature siteFeature) {
		this.siteFeature = siteFeature;
	}
	public String getVariantId() {
		return variantId;
	}
	public void setVariantId(String variantId) {
		this.variantId = variantId;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	public int getImageWidth() {
		return imageWidth;
	}
	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}
	public int getImageHeight() {
		return imageHeight;
	}
	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}
	public String getRating() {
		return rating;
	}
	public void setRating(String rating) {
		this.rating = rating;
	}
	
	public String getBurst() {
		return burst;
	}
	public void setBurst(String burst) {
		this.burst = burst;
	}
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	
}

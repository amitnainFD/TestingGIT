package com.freshdirect.fdstore.content.customerrating;

import java.io.Serializable;
import java.math.BigDecimal;

@Deprecated
public class CustomerReviewsDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 872601294245505330L;
	private String id;
	private String BVReviewId;
	private String productId;
	private String moderationStatus;
	private Long lastModificationTime;
	private String externalId;
	private String displayName;
	private Boolean anonymous;
	private Boolean hyperlinkingEnabled;
	private Boolean ratingsOnly;
	private String title;
	private String reviewText;
	private Integer numComments;
	private String campaignId;
	private BigDecimal rating;
	private Integer ratingRange;
	private Boolean recommended;
	private Integer numFeedbacks;
	private Integer numPositiveFeedbacks;
	private Integer numNegativeFeedbacks;
	private String reviewerLocation;
	private String ipAddress;
	private String displayLocale;
	private Long submissionTime;
	private String badgeName;
	private String badgeContentType;
	private String productReviewsURL;
	private String productReviewsDLURL;
	private Boolean featured;
	private BigDecimal netPromoterScore;
	private String netPromoterComment;
	private String authenticationType;
	private String userEmailAddress;
	private Boolean publishedEmailAlert;
	private Boolean commentedEmailAlert;
	private String originatingDisplayCode;
	private String contentCodes;
	private Long firstPublishTime;
	private Long lastPublishTime;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getModerationStatus() {
		return moderationStatus;
	}
	public void setModerationStatus(String moderationStatus) {
		this.moderationStatus = moderationStatus;
	}
	public Long getLastModificationTime() {
		return lastModificationTime;
	}
	public void setLastModificationTime(Long lastModificationTime) {
		this.lastModificationTime = lastModificationTime;
	}
	public String getExternalId() {
		return externalId;
	}
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public Boolean isAnonymous() {
		return anonymous;
	}
	public void setAnonymous(Boolean anonymous) {
		this.anonymous = anonymous;
	}
	public Boolean isHyperlinkingEnabled() {
		return hyperlinkingEnabled;
	}
	public void setHyperlinkingEnabled(Boolean hyperlinkingEnabled) {
		this.hyperlinkingEnabled = hyperlinkingEnabled;
	}
	public Boolean isRatingsOnly() {
		return ratingsOnly;
	}
	public void setRatingsOnly(Boolean ratingsOnly) {
		this.ratingsOnly = ratingsOnly;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getReviewText() {
		return reviewText;
	}
	public void setReviewText(String reviewText) {
		this.reviewText = reviewText;
	}
	public Integer getNumComments() {
		return numComments;
	}
	public void setNumComments(Integer numComments) {
		this.numComments = numComments;
	}
	public String getCampaignId() {
		return campaignId;
	}
	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}
	public BigDecimal getRating() {
		return rating;
	}
	public void setRating(BigDecimal rating) {
		this.rating = rating;
	}
	public Integer getRatingRange() {
		return ratingRange;
	}
	public void setRatingRange(Integer ratingRange) {
		this.ratingRange = ratingRange;
	}
	public Boolean isRecommended() {
		return recommended;
	}
	public void setRecommended(Boolean recommended) {
		this.recommended = recommended;
	}
	public Integer getNumFeedbacks() {
		return numFeedbacks;
	}
	public void setNumFeedbacks(Integer numFeedbacks) {
		this.numFeedbacks = numFeedbacks;
	}
	public Integer getNumPositiveFeedbacks() {
		return numPositiveFeedbacks;
	}
	public void setNumPositiveFeedbacks(Integer numPositiveFeedbacks) {
		this.numPositiveFeedbacks = numPositiveFeedbacks;
	}
	public Integer getNumNegativeFeedbacks() {
		return numNegativeFeedbacks;
	}
	public void setNumNegativeFeedbacks(Integer numNegativeFeedbacks) {
		this.numNegativeFeedbacks = numNegativeFeedbacks;
	}
	public String getReviewerLocation() {
		return reviewerLocation;
	}
	public void setReviewerLocation(String reviewerLocation) {
		this.reviewerLocation = reviewerLocation;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getDisplayLocale() {
		return displayLocale;
	}
	public void setDisplayLocale(String displayLocale) {
		this.displayLocale = displayLocale;
	}
	public Long getSubmissionTime() {
		return submissionTime;
	}
	public void setSubmissionTime(Long submissionTime) {
		this.submissionTime = submissionTime;
	}
	public String getBadgeName() {
		return badgeName;
	}
	public void setBadgeName(String badgeName) {
		this.badgeName = badgeName;
	}
	public String getBadgeContentType() {
		return badgeContentType;
	}
	public void setBadgeContentType(String badgeContentType) {
		this.badgeContentType = badgeContentType;
	}
	public String getProductReviewsURL() {
		return productReviewsURL;
	}
	public void setProductReviewsURL(String productReviewsURL) {
		this.productReviewsURL = productReviewsURL;
	}
	public String getProductReviewsDLURL() {
		return productReviewsDLURL;
	}
	public void setProductReviewsDLURL(String productReviewsDLURL) {
		this.productReviewsDLURL = productReviewsDLURL;
	}
	public Boolean isFeatured() {
		return featured;
	}
	public void setFeatured(Boolean featured) {
		this.featured = featured;
	}
	public String getAuthenticationType() {
		return authenticationType;
	}
	public void setAuthenticationType(String authenticationType) {
		this.authenticationType = authenticationType;
	}
	public String getUserEmailAddress() {
		return userEmailAddress;
	}
	public void setUserEmailAddress(String userEmailAddress) {
		this.userEmailAddress = userEmailAddress;
	}
	public Boolean isPublishedEmailAlert() {
		return publishedEmailAlert;
	}
	public void setPublishedEmailAlert(Boolean publishedEmailAlert) {
		this.publishedEmailAlert = publishedEmailAlert;
	}
	public Boolean isCommentedEmailAlert() {
		return commentedEmailAlert;
	}
	public void setCommentedEmailAlert(Boolean commentedEmailAlert) {
		this.commentedEmailAlert = commentedEmailAlert;
	}
	public String getOriginatingDisplayCode() {
		return originatingDisplayCode;
	}
	public void setOriginatingDisplayCode(String originatingDisplayCode) {
		this.originatingDisplayCode = originatingDisplayCode;
	}
	public void setFirstPublishTime(Long firstPublishTime) {
		this.firstPublishTime = firstPublishTime;
	}
	public void setLastPublishTime(Long lastPublishTime) {
		this.lastPublishTime = lastPublishTime;
	}
	public String getBVReviewId() {
		return BVReviewId;
	}
	public void setBVReviewId(String bVReviewId) {
		BVReviewId = bVReviewId;
	}
	public BigDecimal getNetPromoterScore() {
		return netPromoterScore;
	}
	public void setNetPromoterScore(BigDecimal netPromoterScore) {
		this.netPromoterScore = netPromoterScore;
	}
	public String getNetPromoterComment() {
		return netPromoterComment;
	}
	public void setNetPromoterComment(String netPromoterComment) {
		this.netPromoterComment = netPromoterComment;
	}
	public String getContentCodes() {
		return contentCodes;
	}
	public void setContentCodes(String contentCodes) {
		this.contentCodes = contentCodes;
	}
	public Long getFirstPublishTime() {
		return firstPublishTime;
	}
	public Long getLastPublishTime() {
		return lastPublishTime;
	}
	
}

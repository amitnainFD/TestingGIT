/*
 * Created on July 22, 2011
 *
 */
package com.freshdirect.fdstore.referral;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.freshdirect.framework.core.ModelSupport;

/**
 * @author jng
 *
 */
public class ReferralPromotionModel extends ModelSupport {

	private static final long serialVersionUID = 1L;
	
	private String referral_prgm_id;
	private Date expiration_date;
	private String give_text;
	private String get_text;
	private String description;
	private String promotion_id;
	private int referral_fee;
	private List<String> prgm_users = new ArrayList<String>();
	private String audience_desc;
	private String shareHeader;
	private String shareText;
	private String getHeader;
	private String giveHeader;
	private String saleId;
	private String customerId;
	private String refCustomerId;
	private String FDCustomerId;
	private String fbFile;
	private String fbHeadline;
	private String fbText;
	private String twitterText;
	private String referralPageText;
	private String referralPageLegal;
	private String inviteEmailSubject;
	private String inviteEmailOfferText;
	private String inviteEmailText;
	private String inviteEmailLegal;
	private String referralCreditEmailSubject;
	private String referralCreditEmailText;
	private String userListFileHolder;
	private String siteAccessImageFile;
	private String advocateEmail;
	private String friendEmail;
	
	public String getFbFile() {
		return fbFile;
	}
	public void setFbFile(String fbFile) {
		this.fbFile = fbFile;
	}
	public String getFbHeadline() {
		return fbHeadline;
	}
	public void setFbHeadline(String fbHeadline) {
		this.fbHeadline = fbHeadline;
	}
	public String getFbText() {
		return fbText;
	}
	public void setFbText(String fbText) {
		this.fbText = fbText;
	}
	public String getTwitterText() {
		return twitterText;
	}
	public void setTwitterText(String twitterText) {
		this.twitterText = twitterText;
	}
	public String getReferralPageText() {
		return referralPageText;
	}
	public void setReferralPageText(String referralPageText) {
		this.referralPageText = referralPageText;
	}
	public String getReferralPageLegal() {
		return referralPageLegal;
	}
	public void setReferralPageLegal(String referralPageLegal) {
		this.referralPageLegal = referralPageLegal;
	}
	public String getInviteEmailSubject() {
		return inviteEmailSubject;
	}
	public void setInviteEmailSubject(String inviteEmailSubject) {
		this.inviteEmailSubject = inviteEmailSubject;
	}
	public String getInviteEmailText() {
		return inviteEmailText;
	}
	public void setInviteEmailText(String inviteEmailText) {
		this.inviteEmailText = inviteEmailText;
	}
	public String getInviteEmailLegal() {
		return inviteEmailLegal;
	}
	public void setInviteEmailLegal(String inviteEmailLegal) {
		this.inviteEmailLegal = inviteEmailLegal;
	}
	public String getReferralCreditEmailSubject() {
		return referralCreditEmailSubject;
	}
	public void setReferralCreditEmailSubject(String referralCreditEmailSubject) {
		this.referralCreditEmailSubject = referralCreditEmailSubject;
	}
	public String getReferralCreditEmailText() {
		return referralCreditEmailText;
	}
	public void setReferralCreditEmailText(String referralCreditEmailText) {
		this.referralCreditEmailText = referralCreditEmailText;
	}
	public String getUserListFileHolder() {
		return userListFileHolder;
	}
	public void setUserListFileHolder(String userListFileHolder) {
		this.userListFileHolder = userListFileHolder;
	}
	public String getReferral_prgm_id() {
		return referral_prgm_id;
	}
	public void setReferral_prgm_id(String referralPrgmId) {
		referral_prgm_id = referralPrgmId;
	}
	public Date getExpiration_date() {
		return expiration_date;
	}
	public void setExpiration_date(Date expirationDate) {
		expiration_date = expirationDate;
	}
	public String getGive_text() {
		return give_text;
	}
	public void setGive_text(String giveText) {
		give_text = giveText;
	}
	public String getGet_text() {
		return get_text;
	}
	public void setGet_text(String getText) {
		get_text = getText;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPromotion_id() {
		return promotion_id;
	}
	public void setPromotion_id(String promotionId) {
		promotion_id = promotionId;
	}
	public int getReferral_fee() {
		return referral_fee;
	}
	public void setReferral_fee(int referralFee) {
		referral_fee = referralFee;
	}
	public List<String> getPrgm_users() {
		return prgm_users;
	}
	public void setPrgm_users(List<String> prgmUsers) {
		prgm_users = prgmUsers;
	}
	
	@Override
	public String toString() {
		return "ReferralPromotionModel [FDCustomerId=" + FDCustomerId
				+ ", audience_desc=" + audience_desc + ", customerId="
				+ customerId + ", description=" + description
				+ ", expiration_date=" + expiration_date + ", fbFile=" + fbFile
				+ ", fbHeadline=" + fbHeadline + ", fbText=" + fbText
				+ ", getHeader=" + getHeader + ", get_text=" + get_text
				+ ", giveHeader=" + giveHeader + ", give_text=" + give_text
				+ ", inviteEmailLegal=" + inviteEmailLegal
				+ ", inviteEmailSubject=" + inviteEmailSubject
				+ ", inviteEmailText=" + inviteEmailText + ", prgm_users="
				+ prgm_users + ", promotion_id=" + promotion_id
				+ ", refCustomerId=" + refCustomerId
				+ ", referralCreditEmailSubject=" + referralCreditEmailSubject
				+ ", referralCreditEmailText=" + referralCreditEmailText
				+ ", referralPageLegal=" + referralPageLegal
				+ ", referralPageText=" + referralPageText + ", referral_fee="
				+ referral_fee + ", referral_prgm_id=" + referral_prgm_id
				+ ", saleId=" + saleId + ", shareHeader=" + shareHeader
				+ ", shareText=" + shareText + ", twitterText=" + twitterText
				+ ", userListFileHolder=" + userListFileHolder + "]";
	}
	public void setAudience_desc(String audience_desc) {
		this.audience_desc = audience_desc;
	}
	public String getAudience_desc() {
		return audience_desc;
	}
	public void setShareHeader(String shareHeader) {
		this.shareHeader = shareHeader;
	}
	public String getShareHeader() {
		return shareHeader;
	}
	public void setShareText(String shareText) {
		this.shareText = shareText;
	}
	public String getShareText() {
		return shareText;
	}
	public void setGetHeader(String getHeader) {
		this.getHeader = getHeader;
	}
	public String getGetHeader() {
		return getHeader;
	}
	public void setGiveHeader(String giveHeader) {
		this.giveHeader = giveHeader;
	}
	public String getGiveHeader() {
		return giveHeader;
	}	
	public void setSaleId(String saleId) {
		this.saleId = saleId;
	}
	public String getSaleId() {
		return saleId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setRefCustomerId(String refCustomerId) {
		this.refCustomerId = refCustomerId;
	}
	public String getRefCustomerId() {
		return refCustomerId;
	}
	public void setFDCustomerId(String fDCustomerId) {
		FDCustomerId = fDCustomerId;
	}
	public String getFDCustomerId() {
		return FDCustomerId;
	}
	public void setSiteAccessImageFile(String siteAccessImageFile) {
		this.siteAccessImageFile = siteAccessImageFile;
	}
	public String getSiteAccessImageFile() {
		return siteAccessImageFile;
	}
	
	public void setInviteEmailOfferText(String inviteEmailOfferText) {
		this.inviteEmailOfferText = inviteEmailOfferText;
	}
	public String getInviteEmailOfferText() {
		return inviteEmailOfferText;
	}
	/**
	 * @return the advocateEmail
	 */
	public String getAdvocateEmail() {
		return advocateEmail;
	}
	/**
	 * @param advocateEmail the advocateEmail to set
	 */
	public void setAdvocateEmail(String advocateEmail) {
		this.advocateEmail = advocateEmail;
	}
	/**
	 * @return the friendEmail
	 */
	public String getFriendEmail() {
		return friendEmail;
	}
	/**
	 * @param friendEmail the friendEmail to set
	 */
	public void setFriendEmail(String friendEmail) {
		this.friendEmail = friendEmail;
	}
	
	
	
}

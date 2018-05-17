package com.freshdirect.fdstore.mail;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.referral.FDInvalidReferralException;
import com.freshdirect.fdstore.referral.FDReferralProgramModel;
import com.freshdirect.framework.util.log.LoggerFactory;

public class TellAFriend implements java.io.Serializable {
	
	protected static Category LOGGER = LoggerFactory.getInstance( TellAFriend.class );
	
	private String friendName;
	private String friendEmail;
	private String emailText;
	private String customerFirstName;
	private String customerLastName;
	private String customerEmail;
	private String referralId;
	private FDReferralProgramModel referralProgram;
	private String server;
		
	private FDIdentity customerIdentity;
	private FDUserI user;
	private String referralProgramId=null;
	
	public TellAFriend() {
		this.emailText = "I thought you might be interested in this...";
	}
	public void setFriendName(String friendName) {	
		this.friendName = friendName;	
	}
	
	public String getFriendName() {
		return this.friendName;
	}
	
	public void setFriendEmail(String friendEmail) {	
		this.friendEmail = friendEmail;		
	}
	
	public String getFriendEmail() {
		return this.friendEmail;
	}	
	
	public void setEmailText(String emailText) {
		this.emailText = emailText;	
	}
	
	public String getEmailText() {
		return this.emailText;
	}

	public void setCustomerIdentity(FDIdentity customerIdentity) {
		this.customerIdentity = customerIdentity;	
	}
	
	public FDIdentity getCustomerIdentity() {
		return this.customerIdentity;
	}

	public void setCustomerFirstName(String customerFirstName) {
		this.customerFirstName = customerFirstName;	
	}
	
	public String getCustomerFirstName() {
		return this.customerFirstName;
	}
	
	public void setCustomerLastName(String customerLastName) {
		this.customerLastName = customerLastName;	
	}
	
	public String getCustomerLastName() {
		return this.customerLastName;
	}
	
	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;	
	}
	
	public String getCustomerEmail() {
		return this.customerEmail;
	}

	public String getReferralId() {
		return this.referralId;
	}
	
	public void setReferralId(String referralId) {
		this.referralId = referralId;	
	}
	
	public FDReferralProgramModel getReferralProgram() {
		return this.referralProgram;
	}
	
	public void setReferralProgram(FDReferralProgramModel referralProgram) {
		this.referralProgram = referralProgram;	
	}

	public void setServer(String server) {	
		this.server = server;	
	}
	
	public String getServer() {
		return this.server;
	}

	/**
	 *  Return the name of the XSL transformation document that
	 *  wil be used to convert the XML format of the message
	 *  to an HTML e-mail.
	 *  
	 *  @return the name of an XSL transformation document
	 */
	public String getXsltPath() {
		return "h_tell_a_friend_3.xsl";
	}
	
	/**
	 *  Tell if this object tells about the specified product or not.
	 *  
	 *  @param productId the id of the product.
	 *  @return true if this object tells about the product with the specified id,
	 *          false otherwise.
	 */
	public boolean isAbout(String productId) {
		return false;
	}

	/**
	 *  Send an e-mail.
	 *  
	 *  @param ctx the servlet context used to fetch external media
	 *  @throws FDResourceException on FD resource issues
	 *  @throws FDInvalidReferralException on referral issues
	 */
	public void send(ServletContext ctx, FDUserI fdUser) throws FDResourceException {
		
		try {
			decorate(ctx);			
			FDCustomerManager.sendTellAFriendEmail(this, fdUser);
		} catch (IOException ie) {
			ie.printStackTrace();
			LOGGER.warn("Could not create a Referral", ie);
		}	
	}
	
	/**
	 *  Generate a previes of the tell-a-friend e-mail.
	 *  
	 *  @param ctx the servlet context used to fetch external media
	 *  @return a preview rendering of the e-mail
	 *  @throws FDResourceException on FD resource issues.
	 */
	public String getPreview(ServletContext ctx) throws FDResourceException {
		String ret = ""; 
		try {
			decorate(ctx);
			ret = FDCustomerManager.makePreviewEmail(this);
		} catch (IOException ie) {
			LOGGER.warn("Product does not have a description", ie);
		}
		
		return ret;
	}

	/**
	 *  Fill in the description field of the object, based on the associated
	 *  product's HTM description media.
	 *   
	 *  @param ctx the servlet context, used to fetch the HTML media
	 *  @throws IOException on I/O errors
	 *  @throws FDResourceException on FD resource errors
	 */
	protected void decorate(ServletContext ctx) throws IOException, FDResourceException {
		// don't do anything by default
	}
	public FDUserI getUser() {
		return user;
	}
	public void setUser(FDUserI user) {
		this.user = user;
	}
	public String getReferralProgramId() {
		return referralProgramId;
	}
	public void setReferralProgramId(String referralProgramId) {
		this.referralProgramId = referralProgramId;
	}
}

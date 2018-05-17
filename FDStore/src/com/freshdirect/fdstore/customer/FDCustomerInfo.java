package com.freshdirect.fdstore.customer;

import com.freshdirect.common.address.PhoneNumber;
import com.freshdirect.fdstore.FDResourceException;


/**
 * 
 * @author knadeem
 */

public class FDCustomerInfo implements java.io.Serializable {
	
	private static final long	serialVersionUID	= 6170229483507780937L;
	
	private int numberOfOrders;
	private String firstName;
	private String lastName;
	private String emailAddress;
	private String altEmailAddress;
	private String depotCode;
	private boolean htmlEmail;
	private double customerCredit;
    private String segmentCode;
    private boolean pickupOnly;
	private boolean corporateUser;
    private boolean eligibleForPromotion;
    private String lastOrderId;
    private PhoneNumber homePhone;
    private PhoneNumber workPhone;
    private PhoneNumber cellPhone;
    private boolean chefsTable;
    private String customerServiceContact;
    private double userGiftCardsBalance;
    private boolean goGreen;
	
	public FDCustomerInfo(String firstName, String lastName){
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	public FDCustomerInfo(String emailAddress){
		this.emailAddress = emailAddress;
	}
	
	public int getNumberOfOrders(){
		return this.numberOfOrders;
	}
	
	public void setNumberOfOrders(int numberOfOrders){
		this.numberOfOrders = numberOfOrders;
	}
	
	public String getFirstName(){
		return this.firstName;
	}
	
	public void setFirstName(String firstName){
		this.firstName = firstName;
	}
	
	public String getLastName(){
		return this.lastName;
	}
	
	public void setLastName(String lastName){
		this.lastName = lastName;
	}
	
	public String getEmailAddress(){
		return this.emailAddress;
	}
	
	public void setEmailAddress(String emailAddress){
		this.emailAddress = emailAddress;
	}
	
	public String getAltEmailAddress(){
		return this.altEmailAddress;
	}
	
	public void setAltEmailAddress(String altEmailAddress){
		this.altEmailAddress = altEmailAddress;
	}
	
	public String getDepotCode(){
		return this.depotCode;
	}
	
	public void setDepotCode(String depotCode){
		this.depotCode = depotCode;
	}
	
	public boolean isHtmlEmail(){
		return this.htmlEmail;
	}
	
	public void setHtmlEmail(boolean htmlEmail){
		this.htmlEmail = htmlEmail;
	}
	
	public double getCustomerCredit(){
		return this.customerCredit;
	}
	
	public void setCustomerCredit(double customerCredit){
		this.customerCredit = customerCredit;
	}
	
    public String getSegmentCode() {
        return this.segmentCode;
    }
    
    public void setSegmentCode(String segmentCode) {
        this.segmentCode = segmentCode;
    }
    
    public boolean isPickupOnly(){
    	return this.pickupOnly;
    }
    
    public void setPickupOnly(boolean pickupOnly) {
    	this.pickupOnly = pickupOnly;
    }
    
	public boolean isCorporateUser(){
		return this.corporateUser;
	}
    
	public void setCorporateUser(boolean corporateUser) {
		this.corporateUser = corporateUser;
	}
    
    public boolean isEligibleForPromotion(){
    	return this.eligibleForPromotion;
    }
    
    public void setEligibleForPromotion(boolean eligibleForPromotion){
    	this.eligibleForPromotion = eligibleForPromotion;
    }
    
    public String getLastOrderId(){
    	return this.lastOrderId;
    }
    
    public void setLastOrderId(String lastOrderId) {
    	this.lastOrderId = lastOrderId;
    }
    
    public PhoneNumber getHomePhone(){
    	return this.homePhone;
    }
    
    public void setHomePhone(PhoneNumber homePhone){
    	this.homePhone = homePhone;
    }
    
    public PhoneNumber getWorkPhone(){
    	return this.workPhone;
    }
    
    public void setWorkPhone(PhoneNumber workPhone){
    	this.workPhone = workPhone;
    }
    
    public PhoneNumber getCellPhone(){
    	return this.cellPhone;
    }
    
    public void setCellPhone(PhoneNumber cellPhone){
    	this.cellPhone = cellPhone;
    }

	public boolean isChefsTable() {
		return this.chefsTable;
	}

	public void setChefsTable(boolean chefsTable) {
		this.chefsTable = chefsTable;
	}

	public String getCustomerServiceContact() {
		return customerServiceContact;
	}

	public void setCustomerServiceContact(String customerServiceContact) {
		this.customerServiceContact = customerServiceContact;
	}

	/**
	 * @return the userGiftCardsBalance
	 */
	public double getUserGiftCardsBalance() {
		return userGiftCardsBalance;
	}

	/**
	 * @param userGiftCardsBalance the userGiftCardsBalance to set
	 */
	public void setUserGiftCardsBalance(double userGiftCardsBalance) {
		this.userGiftCardsBalance = userGiftCardsBalance;
	}

	public boolean isGoGreen() {
		return goGreen;
	}

	public void setGoGreen(boolean goGreen) {
		this.goGreen = goGreen;
	}

	public void getUserInfo(FDUserI customerUser) throws FDResourceException {
		this.setChefsTable(customerUser.isChefsTable());
		this.setCustomerServiceContact(customerUser.getCustomerServiceContact());
	}
}

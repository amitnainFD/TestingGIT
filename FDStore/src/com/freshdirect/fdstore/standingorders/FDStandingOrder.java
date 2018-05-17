package com.freshdirect.fdstore.standingorders;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpPaymentMethodI;
import com.freshdirect.fdlogistics.model.FDReservation;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDAuthenticationException;
import com.freshdirect.fdstore.customer.FDCustomerFactory;
import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDOrderInfoI;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.lists.FDCustomerList;
import com.freshdirect.fdstore.lists.FDListManager;
import com.freshdirect.fdstore.lists.FDStandingOrderList;
import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.xml.ExcludeFromXmlSerializer;

public class FDStandingOrder extends ModelSupport {
	
	private static final long serialVersionUID = 9146272725813248955L;

	String customerId;			// customer ID (erps)
	String customerListId; 		// customer list ID : selected shopping list
	String addressId;			// delivery address ID
	String paymentMethodId; 	// payment method ID
	String customerEmail;
	Date startTime; 			// delivery timeslot - start date
	Date endTime;				// dlv timeslot - end date
	Date nextDeliveryDate;		// next delivery date
	Date previousDeliveryDate;  // previous delivery date (pseudo transient value -- not saved)
	
	int frequency;				// frequency in weeks (chosen by the customer; can be one, two, three, and four weeks)
	
	boolean alcoholAgreement = false;	// agreed to alcohol delivery
	boolean deleted = false;	// deleted flag (false by default)
	
	String lastError = null;	// last exception or invalid condition
	String errorHeader = null;	// detailed error message header
	String errorDetail = null;	// detailed error message detail

	String customerListName;	// Only used when standing order is not yet persisted!
	String zone;
	FDStandingOrderAltDeliveryDate altDeliveryInfo;

	public static final String STANDING_ORDER_DETAIL_PAGE	= "/quickshop/qs_so_details.jsp";
	
	public FDStandingOrder() {
		super();
	}

	public FDStandingOrder(PrimaryKey pk, FDCustomerList list) {
		this();
		
		setPK(pk);
		
		setCustomerId(list.getCustomerPk().getId());
		setCustomerListId(list.getPK().getId());
	}


	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
		customerIdentity = null;
	}
	public String getCustomerListId() {
		return customerListId;
	}
	public void setCustomerListId(String customerListId) {
		this.customerListId = customerListId;
	}
	public String getAddressId() {
		return addressId;
	}
	public void setAddressId(String addressId) {
		this.addressId = addressId;
	}
	public String getPaymentMethodId() {
		return paymentMethodId;
	}
	public void setPaymentMethodId(String paymentMethodId) {
		this.paymentMethodId = paymentMethodId;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public Date getNextDeliveryDate() {
		return nextDeliveryDate;
	}

	public void setNextDeliveryDate(Date nextDeliveryDate) {
		this.nextDeliveryDate = nextDeliveryDate;
	}

	public Date getPreviousDeliveryDate() {
		return previousDeliveryDate;
	}

	public void setPreviousDeliveryDate(Date previousDeliveryDate) {
		this.previousDeliveryDate = previousDeliveryDate;
	}

	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	public boolean isAlcoholAgreement() {
		return alcoholAgreement;
	}
	public void setAlcoholAgreement(boolean alcoholAgreement) {
		this.alcoholAgreement = alcoholAgreement;	
		

	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	public ErrorCode getLastError() {
		if ( lastError == null )
			return null;
		try {
			return ErrorCode.valueOf( lastError );
		} catch ( IllegalArgumentException e ) {
			return ErrorCode.GENERIC;
		} 
	}
	public String getErrorHeader() {
		return errorHeader;
	}
	public String getErrorDetail() {
		return errorDetail;
	}
	public void setLastError( ErrorCode lastErrorCode, String errorHeader, String errorDetail ) {
		this.lastError = lastErrorCode.name();

		this.errorHeader = errorHeader;
		this.errorDetail = errorDetail;
	}
	public void setLastError( String lastErrorCode, String errorHeader, String errorDetail ) {
		this.lastError = lastErrorCode;
		this.errorHeader = errorHeader;
		this.errorDetail = errorDetail;
	}
	public void clearLastError() {
		this.lastError = null;
		this.errorHeader = null;
		this.errorDetail = null;
	}
	public boolean isError() {
		return lastError != null;
	}

	public String getCustomerListName() {
		return customerListName;
	}

	public void setCustomerListName(String customerListName) {
		this.customerListName = customerListName;
	}

	public void setupDelivery(FDReservation r) {
		setStartTime(r.getStartTime());
		setEndTime(r.getEndTime());
		calculateNextDeliveryDate( r.getTimeslot().getDeliveryDate() );
	}

	/**
	 * Sets next delivery date based on baseDate parameter
	 * and frequency
	 * 
	 * @param baseDate
	 */
	public void calculateNextDeliveryDate(Date baseDate) {
		// set next delivery date
		nextDeliveryDate = getSubsequentDeliveryDate(baseDate);
	}

	/**
	 * Utility method that shifts a date with
	 * week frequency
	 * 
	 * @param baseDate
	 * @return date shifted with frequency weeks
	 */
	public Date getSubsequentDeliveryDate(Date baseDate) {
		// calculate next delivery

		Calendar cl = Calendar.getInstance();
		cl.setTime(baseDate);
		
		cl.add(Calendar.DATE, 7*frequency);
		
		cl.set(Calendar.HOUR, 0);
		cl.set(Calendar.MINUTE, 0);

		return cl.getTime();
	}
	
	/**
	 * Skip next delivery and jump to the following date according to the frequency
	 */
	public void skipDeliveryDate() {
		calculateNextDeliveryDate( nextDeliveryDate );
	}
	
	public void recalculateFrequency( int newFreq ) {
		Calendar nextDate = Calendar.getInstance();
		nextDate.setTime( nextDeliveryDate );
		nextDate.add( Calendar.WEEK_OF_YEAR, newFreq - frequency );
		while ( DeliveryInterval.isWithinDeliveryWindow( nextDate.getTime() ) ) {
			nextDate.add( Calendar.WEEK_OF_YEAR, 1 );		
		}
		frequency = newFreq;
		nextDeliveryDate = nextDate.getTime();
	}
	
	
	public static String[] i2s = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};
	

	public String getFrequencyDescription() {
		if (frequency == 1) {
			return "every week";
		}
		return "every " + i2s[frequency] + " weeks";
	}
	

	public static final String DATE_FORMAT = "EEEE, MMMM d.";
	public static final String DATE_FORMAT_SHORT = "MM/dd/yy";
	public static final String DATE_FORMAT_LONG =  "EEEE, MMMM d., yyyy";

	private final DateFormat dateFormatter =  new SimpleDateFormat("EEEE, MMMM d.");
	private final DateFormat dateFormatterShort =  new SimpleDateFormat("MM/dd/yy");
	
	public String getNextDeliveryString() {
		return dateFormatter.format( getNextDeliveryDate() );
	}
	
	public String getNextDeliveryStringShort() {
		return dateFormatterShort.format( getNextDeliveryDate() );
	}

	
	@Override
	public String toString() {
		return "SO["+getId()+", "+customerListName+", "+nextDeliveryDate+"]";
	}

	public static enum ErrorCode { 
		TECHNICAL( "Technical failure.", "Technical failure.",true ), 
		GENERIC( "Generic error.", "Generic error.",true ), 
		
		ADDRESS( "We no longer deliver to the address you set up for this standing order.", "Use the link below to modify this standing order and choose a different address.",true ), 
		PAYMENT( "There was a problem with the payment method you selected.", "Use the link below to modify this standing order and update the payment options.",true ), 
		ALCOHOL( "You must verify your age to receive deliveries containing alcohol.", "Use the link below to modify this standing order and confirm that you are over 21 years of age.",true ), 
		MINORDER( "The order subtotal was below our $50 minimum.", "Please adjust the items or quantities by editing the shopping list for this standing order.",true ), 		
		TIMESLOT_MINORDER( "The order subtotal was below our $x minimum.", "Please adjust the items or quantities by editing the shopping list for this standing order.",true),
		TIMESLOT( "Your selected timeslot was unavailable or sold out.", "Use the link below to modify this standing order and choose a different timeslot.",true ),
		PAYMENT_ADDRESS( "The address you entered does not match the information on file with your card provider.", "Please contact a FreshDirect representative at 9999 for assistance.",true ),
		NO_ADDRESS( "The address you set up for this standing order no longer exists in the system.", "Use the link below to modify this standing order and choose a different address.",true ), 
		CLOSED_DAY( "We do not deliver on closed days.", "We do not deliver on closed days.", false );
		
		private String errorHeader;
		private String errorDetail;
		private boolean sendEmail;
		
		private ErrorCode( String errorHeader, String errorDetail, boolean sendEmail) {
			this.errorHeader = errorHeader;
			this.errorDetail = errorDetail;
			this.sendEmail = sendEmail;
		}
	
		public boolean isTechnical() {
			return this == TECHNICAL || this == GENERIC || this == TIMESLOT || this == CLOSED_DAY;
		}
		
		public String getErrorHeader() {
			return errorHeader;
		}
		
		public boolean isSendEmail(){
			return sendEmail;
		}
		public String getErrorDetail(FDCustomerInfo user) {
			if (this == PAYMENT_ADDRESS)
				return errorDetail.replace("9999", user.getCustomerServiceContact());
			return errorDetail;
		}
		public String getErrorHeader(FDReservation rsv) {
			if (this == TIMESLOT_MINORDER)
				return errorHeader.replace("$x", Double.toString(rsv.getMinOrderAmt()));
			return errorDetail;
		}
	};
	
	
	// more heavyweight getter methods
	
	private FDIdentity customerIdentity = null;
	
	/**
	 * @return FDIdentity of the user of this standing order
	 * @throws FDResourceException
	 */
	public FDIdentity getCustomerIdentity() throws FDResourceException {
		if ( customerIdentity == null )
			customerIdentity = new FDIdentity( customerId, FDCustomerFactory.getFDCustomerIdFromErpId( customerId ) );
		return customerIdentity;
	}
	
	@ExcludeFromXmlSerializer
	public FDUserI getUser() throws FDResourceException, FDAuthenticationException {
		return FDCustomerManager.recognize( getCustomerIdentity() );	
	}	
	
	@ExcludeFromXmlSerializer
	public FDCustomerInfo getUserInfo() throws FDResourceException {
		return FDCustomerManager.getSOCustomerInfo( getCustomerIdentity() );	
	}	
	
	@ExcludeFromXmlSerializer
	public FDCustomerInfo getUserInfoEx() throws FDResourceException {
		return FDCustomerManager.getCustomerInfo( getCustomerIdentity() );	
	}	
	
	/**
	 * @return delivery address of this standing order as ErpAddressModel
	 * @throws FDResourceException
	 */
	public ErpAddressModel getDeliveryAddress() throws FDResourceException {
		return FDCustomerManager.getAddress( getCustomerIdentity(), addressId );		
	}
	/**
	 * @return payment method
	 * @throws FDResourceException
	 */
	public ErpPaymentMethodI getPaymentMethod() throws FDResourceException {
		return FDCustomerManager.getPaymentMethod( getCustomerIdentity(), paymentMethodId );		
	}
	
	/**
	 * @return customer shopping list associated with this standing order
	 * @throws FDResourceException
	 */
	@ExcludeFromXmlSerializer
	public FDStandingOrderList getCustomerList() throws FDResourceException {
		return FDListManager.getStandingOrderList( getCustomerIdentity(), customerListId );
	}
	
	/**
	 * @param user 
	 * @return last placed order by Standing Order which is not expired (?)
	 * @throws FDResourceException
	 * @throws FDAuthenticationException 
	 */
	@ExcludeFromXmlSerializer
	public FDOrderInfoI getLastOrder() throws FDResourceException, FDAuthenticationException {
		return getLastOrder( getUser() );		
	}
	@ExcludeFromXmlSerializer
	public FDOrderInfoI getLastOrder( FDUserI user ) throws FDResourceException {
		return FDStandingOrdersManager.getInstance().getLastOrder( user, this );		
	}

	/**
	 * @param user 
	 * @return all orders based on this standing order of this user
	 * @throws FDAuthenticationException 
	 * @throws FDResourceException 
	 */
	@ExcludeFromXmlSerializer
	public List<FDOrderInfoI> getAllOrders() throws FDResourceException, FDAuthenticationException {
		return getAllOrders( getUser() );
	}
	@ExcludeFromXmlSerializer
	public List<FDOrderInfoI> getAllOrders( FDUserI user ) throws FDResourceException {
		return FDStandingOrdersManager.getInstance().getAllOrders( user, this );		
	}
	@ExcludeFromXmlSerializer
    public List<FDOrderInfoI> getAllUpcomingOrders() throws FDResourceException, FDAuthenticationException {
            return FDStandingOrdersManager.getInstance().getAllUpcomingOrders( getUser(), this );
    }
	
	@ExcludeFromXmlSerializer
	public String getLandingPage() {
		return new StringBuilder().append(STANDING_ORDER_DETAIL_PAGE).append("?ccListId=" + this.getCustomerListId()).toString();
	}
	
	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public FDStandingOrderAltDeliveryDate getAltDeliveryInfo() {
		return altDeliveryInfo;
	}

	public void setAltDeliveryInfo(FDStandingOrderAltDeliveryDate altDeliveryInfo) {
		this.altDeliveryInfo = altDeliveryInfo;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}
}

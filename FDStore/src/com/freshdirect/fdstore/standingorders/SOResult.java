package com.freshdirect.fdstore.standingorders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.standingorders.FDStandingOrder.ErrorCode;

/**
 *	Standing Order background process result utilities
 *
 *	Use the create*() factory methods for creating Result objects in the background process
 */
public class SOResult implements Serializable {

	private static final long	serialVersionUID	= -4890832579467292432L;

	
	// =========================
	// 	public factory methods
	// =========================
	
	/**
	 *	Use it when a null object is received by the background process.
	 *	Should never happen for real. 
	 */
	public static Result createNull() {
		Result r = new Result( Status.SKIPPED );
		return r;
	}
	
	/**
	 *	Skipped Standing Orders result. 
	 */
	public static Result createSkipped( FDStandingOrder so, String skipReason ) {
		return new Result( Status.SKIPPED ).fillSOData( so ).fillInternalMessage( skipReason );
	}
	
	public static Result createSkipped( FDStandingOrder so, FDIdentity cust, FDCustomerInfo custInfo, String skipReason ){
		return createSkipped(so, cust, custInfo, skipReason, null);
	}

	/**
	 *	Skipped Standing Orders result, with extra customer data 
	 */
	public static Result createSkipped( FDStandingOrder so, FDIdentity cust, FDCustomerInfo custInfo, String skipReason,FDStandingOrderAltDeliveryDate altDateInfo) {
		return new Result( Status.SKIPPED ).
				fillSOData( so, altDateInfo ).
				fillCustomerData( cust, custInfo ).
				fillInternalMessage( skipReason );
	}
	
	/**
	 *	 For global technical errors, when even the SO is unavailable.
	 */
	public static Result createTechnicalError( String detail ) {
		return new Result( Status.FAILURE ).
				fillErrorData( ErrorCode.TECHNICAL, ErrorCode.TECHNICAL.getErrorHeader(), detail );
	}
	
	/**
	 *	Technical error for a Standing Order. 
	 */
	public static Result createTechnicalError( FDStandingOrder so, String detail ) {
		return new Result( Status.FAILURE ).
				fillErrorData( ErrorCode.TECHNICAL, ErrorCode.TECHNICAL.getErrorHeader(), detail ).
				fillSOData( so );
	}


	/**
	 *	Technical error for a Standing Order. 
	 */
	public static Result createTechnicalError( FDStandingOrder so, String detail,FDStandingOrderAltDeliveryDate altDateInfo ) {
		return new Result( Status.FAILURE ).
				fillErrorData( ErrorCode.TECHNICAL, ErrorCode.TECHNICAL.getErrorHeader(), detail ).
				fillSOData( so,altDateInfo );
	}
	/**
	 *	User error for a Standing Order. 
	 */
	public static Result createUserError( FDStandingOrder so, FDIdentity cust, FDCustomerInfo custInfo, ErrorCode errCode ) {
		return new Result( Status.FAILURE ).
				fillErrorData( errCode, errCode.getErrorHeader(), errCode.getErrorDetail( custInfo ) ).
				fillSOData( so ).
				fillCustomerData( cust, custInfo );
	}


	/**
	 *	User error for a Standing Order, with custom error header 
	 */
	public static Result createUserError( FDStandingOrder so, FDIdentity cust, FDCustomerInfo custInfo, ErrorCode errCode, String errorHeader ) {
		return new Result( Status.FAILURE ).
				fillErrorData( errCode, errorHeader, errCode.getErrorDetail( custInfo ) ).
				fillSOData( so ).
				fillCustomerData( cust, custInfo );
	}

	/**
	 *	Successful Standing Order. 
	 * @param requestedDate 
	 */
	public static Result createSuccess( FDStandingOrder so, FDIdentity cust, FDCustomerInfo custInfo, boolean hasInvalidItems, List<String> unavItems, String orderId, String internalMessage, boolean errorOccured, Map<FDCartLineI, UnAvailabilityDetails> availabilityDetails, Date requestedDate ) {
		Result result = new Result( Status.SUCCESS ).
				fillSOData( so ).
				fillCustomerData( cust, custInfo ).
				fillSuccessData( hasInvalidItems, unavItems, orderId, internalMessage, availabilityDetails,requestedDate );
		if (errorOccured) {
			result.setErrorOccured();
		}
		return result;
	}
	
	/**
	 *	Cancelled for a week, so skipped to next delivery date- Standing Order. 
	 */
	public static Result createForcedSkipped( FDStandingOrder so, FDIdentity cust, FDCustomerInfo custInfo, String skipReason ) {
		Result result = new Result( Status.FORCED_SKIPPED ).
				fillSOData( so ).
				fillCustomerData( cust, custInfo ).
				fillInternalMessage( skipReason );
		return result;
	}
	
	/**
	 *	SO Result Status enum, represents the status of the result of processing an SO 
	 */
	public static enum Status { SUCCESS, FAILURE, SKIPPED, FORCED_SKIPPED };
	
	
	/**
	 *	SO Result, represents the result of processing an SO, with additional data for the reports 
	 */
	public static class Result implements Serializable {
		
		private static final long	serialVersionUID	= 7481950658518757363L;
		
		// Status field
		private Status				status;
		
		// Error fields
		private ErrorCode			errorCode;
		private String				errorHeader;
		private String				errorDetail;

		// SO basic data
		private String				soId;
		private String				soName;

		// Customer data
		private FDCustomerInfo 		customerInfo;
		private String 				customerId;
		
		// Order ID for successful SOs 
		private String				saleId;
		
		// invalid items flag
		private boolean				hasInvalidItems		= false;

		// list of unavailable items
		private List<String> unavailableItems;
		
		private Map<FDCartLineI, UnAvailabilityDetails> unavailabilityDetails;

		// error mail flags
		private boolean				errorEmailSentToAdmins	= false;
		private boolean				errorEmailSentToCustomer = false;
		
		// additional internal message for cron report.
		private String internalMessage;
		
		protected boolean errorOccured	= false;
		
		private Date deliveryDate;
		private Date deliveryStartTime;
		private Date deliveryEndTime;
		private Date requestedDate;

		
		/**
		 *  private constructor, only for internal use
		 *
		 *  Use the factory methods to create instances
		 * 
		 * @param status
		 */
		private Result( Status status ) {
			this.status = status;
		}

		/**
		 *	Fills in SO basic data, private, only for internal use
		 *
		 *  Use the factory methods to create instances
		 */
		private Result fillSOData( FDStandingOrder so ) {
			this.soId = so.getId();
			this.soName = so.getCustomerListName();
			return this;
		}
		/**
		 *	Fills in SO basic data, private, only for internal use
		 *
		 *  Use the factory methods to create instances
		 */
		private Result fillSOData( FDStandingOrder so,FDStandingOrderAltDeliveryDate altDateInfo ) {
			fillSOData(so);
			if(null != altDateInfo){
				this.deliveryDate = altDateInfo.getAltDate();
				this.deliveryStartTime = altDateInfo.getAltStartTime();
				this.deliveryEndTime = altDateInfo.getAltEndTime();
			}
			return this;
		}

		/**
		 *	Fills in customer data, private, only for internal use
		 *
		 *  Use the factory methods to create instances
		 */
		private Result fillCustomerData( FDIdentity custIdentity, FDCustomerInfo custInfo ) {
			this.customerId = custIdentity.getErpCustomerPK();
			this.customerInfo = custInfo;
			return this;
		}
		
		/**
		 *	Sets error status, and error details, private, only for internal use
		 *
		 *  Use the factory methods to create instances
		 */
		private Result fillErrorData( ErrorCode errCode, String errHeader, String errDetail ) {
			this.status = Status.FAILURE;
			this.errorCode = errCode;
			this.errorHeader = errHeader;
			this.errorDetail = errDetail;
			return this;
		}

		
		/**
		 *	Sets the additional data for successful SOs, private, only for internal use
		 *	
		 *  Use the factory methods to create instances
		 * @param requestedDate 
		 */
		private Result fillSuccessData(boolean hasInvalidItems, List<String> unavItems, String orderId, String internalMessage,  Map<FDCartLineI, UnAvailabilityDetails> unavailabilityDetails,Date requestedDate) {
			this.hasInvalidItems = hasInvalidItems;
			this.unavailableItems = unavItems;
			this.unavailabilityDetails = unavailabilityDetails;
			this.saleId = orderId;
			this.internalMessage = internalMessage;
			this.requestedDate = requestedDate;
			return this;
		}

		
		/**
		 *	sets the internal message, private, only for internal use
		 *
		 *  Use the factory methods to create instances
		 */
		private Result fillInternalMessage( String message ) {
			this.internalMessage = message;
			return this;
		}

		public Date getRequestedDate() {
			return requestedDate;
		}
		
		public boolean isError() {
			return errorCode != null;
		}		
		public boolean isSkipped() {
			return status == Status.SKIPPED;
		}		
		public boolean isTechnicalError() {
			return errorCode != null && errorCode.isTechnical();
		}

		public boolean isSendErrorEmail() {
			return errorCode != null && errorCode.isSendEmail();
		}
		
		public String getSoId() {
			return soId;
		}
		public String getSoName() {
			return soName;
		}		
		public Status getStatus() {
			return status;
		}		
		
		
		public ErrorCode getErrorCode() {
			return errorCode;
		}
		public String getErrorHeader() {
			return errorHeader;
		}
		public String getErrorDetail() {
			return errorDetail;
		}
		
		
		public String getCustId() {
			return customerId;
		}		
		
		public FDCustomerInfo getCustomerInfo() {
			return customerInfo;
		}
		
		public String getSaleId() {
			return saleId;
		}		
		
		public boolean hasInvalidItems() {
			return hasInvalidItems;			
		}		
		
		public List<String> getUnavailableItems() {
			return unavailableItems;
		}
		
		public Map<FDCartLineI, UnAvailabilityDetails> getUnavailabilityDetails() {
			return unavailabilityDetails;
		}

		/**
		 * Flag that signals that an error email was sent to the administrators
		 */
		public boolean isErrorEmailSentToAdmins() {
			return errorEmailSentToAdmins;
		}
		
		/**
		 * Set the flag that signals that an error email was sent to the administrators
		 */
		public void setErrorEmailSentToAdmins() {
			this.errorEmailSentToAdmins = true;
		}
		
		/**
		 * Flag that signals that an error email was sent to the customer
		 */
		public boolean isErrorEmailSentToCustomer() {
			return errorEmailSentToCustomer;
		}
		
		/**
		 * Set the flag that signals that an error email was sent to the customer
		 */
		public void setErrorEmailSentToCustomer() {
			this.errorEmailSentToCustomer = true;
		}
		
		/**
		 *	Internal message or warning for otherwise successful SOs
		 *	Currently it is used to signal that the order subTotal was 
		 *	between the soft and hard limits, nut could be used for any 
		 *	additional messages
		 *
		 * 	Will be added to the cron report.
		 */
		public String getInternalMessage() {
			return internalMessage;
		}
		
		
		@Override
		public String toString() {
			return "SOSRR["+status+", "+errorCode+", "+errorHeader+", "+errorDetail+"]";
		}
		
		public boolean isErrorOccured() {
			return errorOccured;
		}
		public void setErrorOccured() {
			this.errorOccured = true;
		}
	}
	
	/**
	 *	Aggregated result of the background process, 
	 *	contains the list of individual result objects,
	 *	and counters for the three statuses.
	 *
	 *  This will be returned from StandingOrdersServiceSessionBean.placeStandingOrders()
	 *  
	 *  Cron job report is based on this
	 */
	public static class ResultList implements Serializable {
		
		private static final long	serialVersionUID	= -7036748124574006340L;
		
		protected int failedCount	= 0;
		protected int successCount	= 0;
		protected int skippedCount	= 0;
		protected boolean errorOccured	= false;
		
		List<Result> results = new ArrayList<Result>();
		
		public int getTotalCount() {
			return failedCount + successCount + skippedCount;
		}	
		public int getFailedCount() {
			return failedCount;
		}	
		public int getSuccessCount() {
			return successCount;
		}	
		public int getSkippedCount() {
			return skippedCount;
		}
		
		public List<Result> getResultsList() {
			return Collections.unmodifiableList( results );
		}
		
		protected void countSuccess() {
			successCount++;
		}	
		protected void countFailed() {
			failedCount++;
		}
		protected void countSkipped() {
			skippedCount++;
		}
		protected void setErrorOccured(Result r) {
			if (r.isErrorOccured()) errorOccured = true;
		}
		
		public void add( Result r ) {
			if ( r != null ) {
				count( r.status );
				setErrorOccured( r );
				results.add( r );
			}
		}
		
		protected void count( Status s ) {
			switch ( s ) {
				case SUCCESS:
					countSuccess();
					break;
				case FAILURE:
					countFailed();
					break;
				case SKIPPED:
					countSkipped();
					break;
				case FORCED_SKIPPED:
					countSkipped();
					break;
				default:
					break;
			}
		}		
		
		@Override
		public String toString() {
			return "SOSRC["+successCount+" success, "+failedCount+" failed, "+skippedCount+" skipped, "+getTotalCount()+" total]"; 
		}
		public boolean isErrorOccured() {
			return errorOccured;
		}
	}
	
}

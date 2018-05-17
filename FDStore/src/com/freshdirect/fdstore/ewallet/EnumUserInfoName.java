package com.freshdirect.fdstore.ewallet;

import java.io.Serializable;
import java.util.HashMap;

public class EnumUserInfoName implements Serializable {

	private static final long serialVersionUID = 724381415439637795L;

	private final static HashMap<String, EnumUserInfoName> INFO_NAMES = new HashMap<String, EnumUserInfoName>();

	private static int idCounter = 0;

	public final static EnumUserInfoName DLV_NICK_NAME = new EnumUserInfoName("dlvnickname", "Nick Name");
	public final static EnumUserInfoName DLV_FIRST_NAME = new EnumUserInfoName("dlvfirstname", "First Name");
	public final static EnumUserInfoName DLV_LAST_NAME = new EnumUserInfoName("dlvlastname", "Last Name");
	public final static EnumUserInfoName DLV_HOME_PHONE = new EnumUserInfoName("dlvhomephone", "Home Phone");
	public final static EnumUserInfoName DLV_HOME_PHONE_EXT = new EnumUserInfoName("dlvhomephoneext", "Home Phone Ext.");
	public final static EnumUserInfoName DLV_ADDRESS_1 = new EnumUserInfoName("address1", "Street Address");
	public final static EnumUserInfoName DLV_ADDRESS_2 = new EnumUserInfoName("address2", "Address Line 2");
	public final static EnumUserInfoName DLV_APARTMENT = new EnumUserInfoName("apartment", "Apt.");
	public final static EnumUserInfoName DLV_CITY = new EnumUserInfoName("city", "City");
	public final static EnumUserInfoName DLV_STATE = new EnumUserInfoName("state", "State");
	public final static EnumUserInfoName DLV_ZIPCODE = new EnumUserInfoName("zipcode", "Zip Code");
	public final static EnumUserInfoName DLV_COUNTRY = new EnumUserInfoName("country", "Country");
	public final static EnumUserInfoName DLV_DELIVERY_INSTRUCTIONS = new EnumUserInfoName("deliveryInstructions", "Delivery Instructions");
	public final static EnumUserInfoName DLV_ADDRESS_SUGGEST = new EnumUserInfoName("addressSuggest", "Street Address");
	public final static EnumUserInfoName DLV_NOT_IN_ZONE = new EnumUserInfoName("undeliverableAddress", "Undeliverable Address");
	public final static EnumUserInfoName DLV_CANT_GEOCODE = new EnumUserInfoName("cantGeocode", "Can't Geocode");

	// Alternate delivery info stuff
	// > used to determine promo eligibility at some point.. public final static DLV_ALTERNATE_THRESHHOLD = 2;
	public final static EnumUserInfoName DLV_ALTERNATE_DELIVERY = new EnumUserInfoName("alternateDelivery", "Alternate Delivery");
	public final static EnumUserInfoName DLV_ALT_FIRSTNAME = new EnumUserInfoName("alternateFirstName", "First Name");
	public final static EnumUserInfoName DLV_ALT_LASTNAME = new EnumUserInfoName("alternateLastName", "Last Name");
	public final static EnumUserInfoName DLV_ALT_APARTMENT = new EnumUserInfoName("alternateApartment", "Apartment");
	public final static EnumUserInfoName DLV_ALT_PHONE = new EnumUserInfoName("alternatePhone", "Alt Phone");
	public final static EnumUserInfoName DLV_ALT_EXT = new EnumUserInfoName("alternatePhoneExt", "Alt Ext.");

	// Alternate delivery settings constants (DLV_ATERNATE_DELIVERY)
	public final static EnumUserInfoName NO_ALT_DELIVERY = new EnumUserInfoName("none", "None");
	public final static EnumUserInfoName LEAVE_WITH_DOORMAN = new EnumUserInfoName("doorman", "Doorman");
	public final static EnumUserInfoName LEAVE_WITH_NEIGHBOR = new EnumUserInfoName("neighbor", "Neighbor");

	// SIGN IN INFORMATION
	public final static EnumUserInfoName EMAIL = new EnumUserInfoName("email", "E-mail Address");
	public final static EnumUserInfoName REPEAT_EMAIL = new EnumUserInfoName("repeat_email", "Repeat E-mail Address");
	public final static EnumUserInfoName EMAIL_FORMAT = new EnumUserInfoName("email_format", "E-mail Address");
	public final static EnumUserInfoName ALT_EMAIL = new EnumUserInfoName("alt_email", "Alternate E-mail Address");

	public final static EnumUserInfoName PASSWORD = new EnumUserInfoName("password", "Password");
	public final static EnumUserInfoName REPEAT_PASSWORD = new EnumUserInfoName("repeat_password", "Repeat Password");
	public final static EnumUserInfoName PASSWORD_HINT = new EnumUserInfoName("password_hint", "Town of Birth or Mother's Maiden Name");
	public final static EnumUserInfoName CUSTOMER_AGREEMENT = new EnumUserInfoName("terms", "Customer Agreement");

	// CREDIT CARD INFO
	public final static EnumUserInfoName CARD_HOLDER = new EnumUserInfoName("cardHolderName", "Name on Card");
	public final static EnumUserInfoName CARD_BRAND = new EnumUserInfoName("cardBrand", "Card Type");
	public final static EnumUserInfoName CARD_NUMBER = new EnumUserInfoName("cardNum", "Account Number");
	public final static EnumUserInfoName CARD_EXPIRATION = new EnumUserInfoName("expiration", "Expires");
	public final static EnumUserInfoName CARD_CSV = new EnumUserInfoName("csv", "CVV");

	// BILLING ADDRESS
	public final static EnumUserInfoName BIL_ADDRESS_1 = new EnumUserInfoName("bil_address1", "Street Address");
	public final static EnumUserInfoName BIL_ADDRESS_2 = new EnumUserInfoName("bil_address2", "Street Address");
	public final static EnumUserInfoName BIL_APARTMENT = new EnumUserInfoName("bil_apartment", "apt.");
	public final static EnumUserInfoName BIL_CITY = new EnumUserInfoName("bil_city", "City");
	public final static EnumUserInfoName BIL_STATE = new EnumUserInfoName("bil_state", "State");
	public final static EnumUserInfoName BIL_ZIPCODE = new EnumUserInfoName("bil_zipcode", "Zip Code");
	public final static EnumUserInfoName BIL_COUNTRY = new EnumUserInfoName("bil_country", "Country");

	// LOGIN
	public final static EnumUserInfoName USER_ID = new EnumUserInfoName("userid", "E-mail Address");

	// CONTACT FD
	public final static EnumUserInfoName CONTACT_FD_SUBJECT = new EnumUserInfoName("subject", "Subject");
	public final static EnumUserInfoName CONTACT_FD_MESSAGE = new EnumUserInfoName("message", "Your Message");
	public final static EnumUserInfoName CONTACT_FD_EMAIL = new EnumUserInfoName("email", "Email Address");
	public final static EnumUserInfoName CONTACT_FD_FIRST_NAME = new EnumUserInfoName("first_name", "First Name");
	public final static EnumUserInfoName CONTACT_FD_LAST_NAME = new EnumUserInfoName("last_name", "Last Name");
	public final static EnumUserInfoName CONTACT_FD_HOME_PHONE = new EnumUserInfoName("home_phone", "Home Phone");

	// DEPOT
	public final static EnumUserInfoName DLV_WORK_DEPARTMENT = new EnumUserInfoName("workDepartment", "Dept/Division");
	public final static EnumUserInfoName DLV_DEPOT_LOCATION_ID = new EnumUserInfoName("locationId", "Primary Depot Address");
	public final static EnumUserInfoName DLV_DEPOT_CODE = new EnumUserInfoName("depotCode", "Depot");
	public final static EnumUserInfoName DLV_DEPOT_REG_CODE = new EnumUserInfoName("depotAccessCode", "Registration Code");
	public final static EnumUserInfoName DLV_WORK_PHONE = new EnumUserInfoName("busphone", "Work Phone Number");
	public final static EnumUserInfoName DLV_EMPLOYEE_ID = new EnumUserInfoName("employeeId", "Employee ID");

	public final static EnumUserInfoName TECHNICAL_DIFFICULTY = new EnumUserInfoName("technical_difficulty", "Technical Difficulty");

	// These two declaration belong at top with dlv fields but to avoid the confusion in id# continuity, declaring them down here
	public final static EnumUserInfoName DLV_SERVICE_TYPE = new EnumUserInfoName("dlvservicetype", "Service Type");
	public final static EnumUserInfoName DLV_COMPANY_NAME = new EnumUserInfoName("dlvcompanyname", "Company name");

	// ECHECKS
	public final static EnumUserInfoName CARD_NUMBER_VERIFY = new EnumUserInfoName("cardNumVerify", "Verify Account Number");

	public final static EnumUserInfoName DLV_ALT_CONTACT_PHONE = new EnumUserInfoName("altContactPhone", "Alt Contact");
	public final static EnumUserInfoName DLV_ALT_CONTACT_EXT = new EnumUserInfoName("altContactPhoneExt", "Alt Ext");

	// Unattended delivery
	public final static EnumUserInfoName DLV_UNATTENDED_DELIVERY_OPT = new EnumUserInfoName("unattendedDeliveryOpt", "Unattended Delivery Flag");
	public final static EnumUserInfoName DLV_UNATTENDED_DELIVERY_INSTRUCTIONS = new EnumUserInfoName("unattendedDeliveryInstr",
			"Unattended Delivery Instructions");
	public final static EnumUserInfoName DLV_UNATTENDED_CONSENT_SEEN = new EnumUserInfoName("unattendedDeliveryNoticeSeen", "Unattended Delivery Notice Seen");

	public final static EnumUserInfoName DLV_CELL_PHONE = new EnumUserInfoName("dlvcellphone", "Cell Phone");

	// Site Access Update
	public final static EnumUserInfoName DLV_CORP_ZIPCODE = new EnumUserInfoName("corpZipcode", "Corporate Zip Code");

	// Gift Card Purchase
	public final static EnumUserInfoName GC_BUYER_NAME = new EnumUserInfoName("gcBuyerName", "Your Name");
	public final static EnumUserInfoName GC_BUYER_EMAIL = new EnumUserInfoName("gcBuyerEmail", "Your Email");
	public final static EnumUserInfoName GC_RECIPIENT_NAME = new EnumUserInfoName("gcRecipientName", "Recipient\'s Name");
	public final static EnumUserInfoName GC_RECIPIENT_EMAIL = new EnumUserInfoName("gcRecipientEmail", "Recipient\'s Email");
	public final static EnumUserInfoName DLV_METHOD = new EnumUserInfoName("deliveryMethod", "Delivery Method");
	public final static EnumUserInfoName GC_MESSAGE = new EnumUserInfoName("message", "Message");
	public final static EnumUserInfoName GC_QUANTITY = new EnumUserInfoName("quantity", "quantity");

	public final static EnumUserInfoName GC_ACCOUNT_FROM = new EnumUserInfoName("fromGCAccount", "From GC Account");
	public final static EnumUserInfoName GC_ACCOUNT_TO = new EnumUserInfoName("toGCAccount", "To GC Account");
	public final static EnumUserInfoName GC_AMOUNT = new EnumUserInfoName("gcAmount", "GC Amount");
	public final static EnumUserInfoName MOBILE_NUMBER = new EnumUserInfoName("mobile_number", "Mobile Number");
	public final static EnumUserInfoName TEXT_OPTION = new EnumUserInfoName("text_option", "Text Messaging Preferences");
	public final static EnumUserInfoName DISPLAY_NAME = new EnumUserInfoName("displayName", "Display Name");

	public final static EnumUserInfoName					ORDER_NOTICE_EXISTING					= new EnumUserInfoName( "order_notice_existing", "order notice existing" );
	public final static EnumUserInfoName					ORDER_EXCEPTION_EXISTING				= new EnumUserInfoName( "order_exception_existing", "order exception existing" );
	public final static EnumUserInfoName					OFFER_EXISTING   				        = new EnumUserInfoName( "offer_existing", "offer existing" );
	public final static EnumUserInfoName					PARTNER_EXISTING   				        = new EnumUserInfoName( "partner_existing", "partner existing" );

	// Masterpass Required 
	public final static EnumUserInfoName EWALLET_ID = new EnumUserInfoName("eWalletID", "EWallet ID");
	public final static EnumUserInfoName VENDOR_EWALLETID = new EnumUserInfoName("vendorEWalletId", "Vendor EWallet ID");
	public final static EnumUserInfoName EWALLET_TXN_ID = new EnumUserInfoName("eWalletTrxnId", "EWallet Trxn ID");

	
	// Gift Card Donation
	public final static EnumUserInfoName GC_DONOR_ORGANIZATION_NAME = new EnumUserInfoName("gcDonorOrganizationName", "Donor Organization Name");

	private int id;
	private String code;
	private String description;

	private EnumUserInfoName(String code, String desc) {
		this.id = idCounter++;
		this.code = code;
		this.description = desc;
		INFO_NAMES.put(code, this);
	}

	public int getId() {
		return this.id;
	}

	public String getCode() {
		return this.code;
	}

	public String getDescription() {
		return this.description;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof EnumUserInfoName) {
			return this.id == ((EnumUserInfoName) o).id;
		}
		return false;
	}

	public static EnumUserInfoName getUserInfoName(String code) {
		if (code == null) {
			return null;
		}
		return (INFO_NAMES.get(code));
	}
}

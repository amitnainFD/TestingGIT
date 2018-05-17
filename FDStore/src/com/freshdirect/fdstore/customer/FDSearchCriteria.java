package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.freshdirect.common.address.PhoneNumber;
import com.freshdirect.fdstore.FDResourceException;

public class FDSearchCriteria implements Serializable {

	protected String firstName = null;
	protected String lastName = null;
	protected String email = null;
	protected String phone = null;
	protected String customerId = null;

	protected String orderNumber = null;
	protected String depotCode = null;
	protected String gcNumber=null;
	protected String certNumber=null;
	protected String sapId = null;
	
	protected boolean quickSearch;

	public FDSearchCriteria() {
	}
	
	public boolean isQuickSearch(){
		return this.quickSearch;
	}
	
	public void setQuickSearch(boolean quickSearch){
		this.quickSearch = quickSearch;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		if (firstName != null && !"".equals(firstName.trim())) {
			this.firstName = firstName;
		}
	}
	
	public String getCustomerId() {
		return this.customerId;
	}

	public void setCustomerId(String custId) {
		if (custId != null && !"".equals(custId.trim())) {
			this.customerId = custId;
		}
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		if (lastName != null && !"".equals(lastName.trim())) {
			this.lastName = lastName;
		}
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		if (email != null && !"".equals(email.trim())) {
			this.email = email;
		}
	}
	
	public void setGiftCardNumber(String gcNumber){
		this.gcNumber=gcNumber;
	}
		
	
	public String getGiftCardNumber(){
		return this.gcNumber;
	}
	
	public String getCertificateNumber(){
		return this.certNumber;
	}
	
	
	public void setCertificateNumber(String certNum){
		this.certNumber=certNum;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		if (phone != null && !"".equals(phone.trim())) {
			this.phone = normalize(phone);
		}
	}

	public String getOrderNumber() {
		return this.orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		if (orderNumber != null && !"".equals(orderNumber.trim())) {
			this.orderNumber = orderNumber;
		}
	}

	public String getDepotCode() {
		return this.depotCode;
	}

	public void setDepotCode(String depotCode) {
		if (depotCode != null && !"".equals(depotCode.trim())) {
			this.depotCode = depotCode;
		}
	}
	
	public String getSapId() {
		return this.sapId;
	}

	public void setSapId(String sapId) {
		if(sapId != null && !"".equals(sapId.trim())){
		this.sapId = sapId;
		}
	}

	public boolean isBlank() {
		return this.firstName == null
			&& this.lastName == null
			&& this.email == null
			&& this.phone == null
			&& this.orderNumber == null
			&& this.depotCode == null 
			&& this.certNumber == null
			&& this.gcNumber == null
			&& this.customerId == null
			&& this.sapId == null;
	}

	public String getCriteria() throws FDResourceException {
		StringBuffer buf = new StringBuffer();
		for(Iterator i = this.getCriteriaMap().entrySet().iterator(); i.hasNext(); ) {
			Map.Entry e = (Entry) i.next();
			buf.append(" ").append(e.getKey()).append(": ").append(e.getValue());
		}
		
		return buf.toString().trim();
	}
	
	public Map getCriteriaMap() throws FDResourceException {
		Map m = new LinkedHashMap();
		if (this.firstName != null && !"".equals(this.firstName.trim())) {
			m.put("First Name", this.firstName);
		}
		if (this.lastName != null && !"".equals(this.lastName.trim())) {
			m.put("Last Name", this.lastName);
		}
		if (this.email != null && !"".equals(email.trim())) {
			m.put("Email", this.email);
		}
		if (this.phone != null && !"".equals(phone.trim())) {
			m.put("Phone", this.phone);
		}
		if (this.orderNumber != null && !"".equals(this.orderNumber.trim())) {
			m.put("Order Number", this.orderNumber);
		}
		if (this.customerId != null && !"".equals(customerId.trim())) {
			m.put("CustomerId", this.customerId);
		}
		return m;
	}

	public String toString() {

		StringBuffer buf = new StringBuffer();
		buf.append("[FDSearchCriteria First Name: ").append(this.firstName);
		buf.append(" Last Name: ").append(this.lastName);
		buf.append(" Emial: ").append(this.email);
		buf.append(" Phone: ").append(this.phone);
		buf.append(" Order#: ").append(this.orderNumber);
		buf.append(" DepotCode: ").append(this.depotCode).append("]");

		return buf.toString();
	}

	private static String normalize(String string) {
		StringBuffer clean = new StringBuffer();
		if (string == null)
			return "";
		for (int i = 0; i < string.length(); i++) {
			if (Character.isDigit(string.charAt(i)) || string.charAt(i) == '*') {
				clean.append(string.charAt(i));
			}
		}
		return clean.toString();
	}
}

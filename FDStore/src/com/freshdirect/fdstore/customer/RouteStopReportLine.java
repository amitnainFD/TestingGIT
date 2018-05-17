/*
 * Created on Aug 2, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.freshdirect.fdstore.customer;

import java.io.Serializable;

import com.freshdirect.common.address.PhoneNumber;
/**
 * @author jangela
 *
 */
public class RouteStopReportLine implements Serializable {
		private String orderNumber;
		private String firstName;
		private String lastName;
		private String dlvPhone;
		private String dlvPhoneExt;
		private String phoneNumber;
		private String waveNumber;
		private String truckNumber;
		private String stopSequence;
		private String email;
		private String emailFormatType;
		private String customerId;
	
		public String getOrderNumcer() {
			return orderNumber;
		}

		public String getFirstName() {
			return firstName;
		}
	
		public String getLastName() {
			return lastName;
		}

		public String getOrderNumber() {
			return orderNumber;
		}

		public String getDlvPhone() {
			return dlvPhone;
		}
			
		public String getDlvPhoneExt() {
			return dlvPhoneExt;
		}	

		public String getPhoneNumber() {
			return phoneNumber;
		}

		public String getWaveNumber() {
			return waveNumber;
		}

		public String getTruckNumber() {
			return truckNumber;
		}

		public String getStopSequence() {
			return stopSequence;
		}

		public String getEmail() {
			return email;
		}

		public String getEmailFormatType() {
			return emailFormatType;
		}
		
		public void setFirstName(String string) {
			firstName = string;
		}
		
		public void setLastName(String string) {
			lastName = string;
		}
		
		public void setDlvPhone(String string) {
			dlvPhone = string;
		}
			
		public void setDlvPhoneExt(String string) {
			dlvPhoneExt = string;
		}
		
		public void setPhoneNumber(String string, String string2) {
			phoneNumber = (new PhoneNumber(dlvPhone, dlvPhoneExt)).getPhone();
		}

		public void setOrderNumber(String string) {
			orderNumber = string;
		}

		public void setWaveNumber(String string) {
			waveNumber = string;
		}

		public void setTruckNumber(String string) {
			truckNumber = string;
		}
		
		public void setStopSequence(String string) {
			stopSequence = string;
		}

		public void setEmail(String string) {
			email = string;
		}

		public void setEmailFormatType(String string) {
			emailFormatType = string;
		}

		public void setCustomerId(String customerId) {
			this.customerId = customerId;
		}

		public String getCustomerId() {
			return customerId;
		}
}

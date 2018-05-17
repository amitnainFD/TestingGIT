/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.Date;

import com.freshdirect.common.address.ContactAddressModel;
import com.freshdirect.logistics.delivery.model.EnumReservationType;

/**
 * Lightweight information about a Sale.
 *
 * @version $Revision$
 * @author $Author$
 */
public class FDCustomerReservationInfo implements Serializable {
	private String id;
	private Date baseDate;
	private Date cutoffTime;
	private String firstName;
	private String lastName;
	private FDIdentity identity;
	private String email;
	private String phone;
	private String altPhone;
	private String businessPhone;
	private Date startTime;
	private Date endTime;
	private String zone;
	private EnumReservationType type;
	
	private ContactAddressModel address;
		
	public FDCustomerReservationInfo(String id,
									 Date baseDate,
									 Date cutoffTime,
									 String firstName,
									 String lastName, 
									 FDIdentity identity,
									 String email,
									 String phone, 
									 String altPhone,
									 String businessPhone, 
									 Date startTime, 
									 Date endTime, 
									 String zone,
									 EnumReservationType type){
		this.id = id;
		this.baseDate = baseDate;
		this.cutoffTime = cutoffTime;
		this.firstName = firstName;
		this.lastName = lastName;
		this.identity = identity;
		this.email = email;
		this.phone = phone;
		this.altPhone = altPhone;
		this.businessPhone = businessPhone;
		this.startTime = startTime;
		this.endTime = endTime;
		this.zone = zone;
		this.type = type;
		
	}
	
	public FDCustomerReservationInfo(String firstName, String lastName,
			FDIdentity identity, String email, String phone,
			String altPhone, String businessPhone) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.identity = identity;
		this.email = email;
		this.phone = phone;
		this.altPhone = altPhone;
		this.businessPhone = businessPhone;
	}
	public String getAltPhone() {
		return altPhone;
	}
	public Date getBaseDate() {
		return baseDate;
	}
	public String getBusinessPhone() {
		return businessPhone;
	}
	public Date getCutoffTime() {
		return cutoffTime;
	}
	public String getEmail() {
		return email;
	}
	public Date getEndTime() {
		return endTime;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getId() {
		return id;
	}
	public FDIdentity getIdentity() {
		return identity;
	}
	public String getLastName() {
		return lastName;
	}
	public String getPhone() {
		return phone;
	}
	public Date getStartTime() {
		return startTime;
	}
	public String getZone() {
		return zone;
	}
	public EnumReservationType getType() {
		return type;
	}
	
	public ContactAddressModel getAddress() {
		return address;
	}
	public void setAddress(ContactAddressModel address) {
		this.address = address;
	}
	
}
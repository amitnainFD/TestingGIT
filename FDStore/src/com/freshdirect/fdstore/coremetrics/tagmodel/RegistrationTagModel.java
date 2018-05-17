package com.freshdirect.fdstore.coremetrics.tagmodel;

import java.util.List;


public class RegistrationTagModel extends AbstractTagModel  {
	
	private static final long	serialVersionUID	= -5314904240161693135L;
	
	private String registrationId; 
	private String registrantEmail; 
	private String registrantCity;
	private String registrantState;
	private String registrantPostalCode;
	private String registrantCountry;
	public String getRegistrationProfileValue() {
		return registrationProfileValue;
	}

	public void setRegistrationProfileValue(String registrationProfileValue) {
		this.registrationProfileValue = registrationProfileValue;
	}

	private String registrationProfileValue;	
	private String registrationCounty;
	private String erpId;

	public String getErpId() {
		return erpId;
	}

	public void setErpId(String erpId) {
		this.erpId = erpId;
	}

	public String getRegistrationCounty() {
		return registrationCounty;
	}

	public void setRegistrationCounty(String registrationCounty) {
		this.registrationCounty = registrationCounty;
	}

	

	
	
	public String getRegistrationId() {
		return registrationId;
	}
	
	public void setRegistrationId(String registrationId) {
		this.registrationId = registrationId;
	}
	
	public String getRegistrantEmail() {
		return registrantEmail;
	}
	
	public void setRegistrantEmail(String registrantEmail) {
		this.registrantEmail = registrantEmail;
	}
	
	public String getRegistrantCity() {
		return registrantCity;
	}
	
	public void setRegistrantCity(String registrantCity) {
		this.registrantCity = registrantCity;
	}
	
	public String getRegistrantState() {
		return registrantState;
	}
	
	public void setRegistrantState(String registrantState) {
		this.registrantState = registrantState;
	}
	
	public String getRegistrantPostalCode() {
		return registrantPostalCode;
	}
	
	public void setRegistrantPostalCode(String registrantPostalCode) {
		this.registrantPostalCode = registrantPostalCode;
	}
	
	public String getRegistrantCountry() {
		return registrantCountry;
	}

	public void setRegistrantCountry(String registrantCountry) {
		this.registrantCountry = registrantCountry;
	}
	
	@Override
	public String getFunctionName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> toStringList() {
		throw new UnsupportedOperationException();
	} 

}
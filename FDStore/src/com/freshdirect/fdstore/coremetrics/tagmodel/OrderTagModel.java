package com.freshdirect.fdstore.coremetrics.tagmodel;

import java.util.List;

public class OrderTagModel extends AbstractTagModel  {
	
	private static final long	serialVersionUID	= 4832570080647156443L;
	
	private String orderId; 
	private String orderSubtotal; 
	private String orderShipping; 
	private String registrationId; 
	private String registrantCity;
	private String registrantState;
	private String registrantPostalCode;
	
	public String getOrderId() {
		return orderId;
	}
	
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	public String getOrderSubtotal() {
		return orderSubtotal;
	}
	
	public void setOrderSubtotal(String orderSubtotal) {
		this.orderSubtotal = orderSubtotal;
	}
	
	public String getOrderShipping() {
		return orderShipping;
	}
	
	public void setOrderShipping(String orderShipping) {
		this.orderShipping = orderShipping;
	}
	
	public String getRegistrationId() {
		return registrationId;
	}
	
	public void setRegistrationId(String registrationId) {
		this.registrationId = registrationId;
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
	
	@Override
	public String getFunctionName() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public List<String> toStringList() {
		throw new UnsupportedOperationException();
	} 
	
}
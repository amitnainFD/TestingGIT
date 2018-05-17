package com.freshdirect.fdstore.customer;

import java.io.Serializable;

public class FDBrokenAccountInfo implements Serializable {
	private String userId;
	private String erpCustomerPk;
	private String fdCustomerPk;
	private String zipCode;
	private String depotCode;
	

	public FDBrokenAccountInfo(String userId, 
							   String erpCustomerPk, 
							   String fdCustomerPk,
							   String zipCode, 
							   String depotCode) {
		super();
		this.userId = userId;
		this.erpCustomerPk = erpCustomerPk;
		this.fdCustomerPk = fdCustomerPk;
		this.zipCode = zipCode;
		this.depotCode = depotCode;
	}
	
	public String getDepotCode() {
		return depotCode;
	}

	public String getErpCustomerPk() {
		return erpCustomerPk;
	}

	public String getFdCustomerPK() {
		return fdCustomerPk;
	}

	public String getUserId() {
		return userId;
	}

	public String getZipCode() {
		return zipCode;
	}


}

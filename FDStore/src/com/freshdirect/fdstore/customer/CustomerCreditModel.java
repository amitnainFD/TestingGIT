package com.freshdirect.fdstore.customer;

import java.io.Serializable;

public class CustomerCreditModel implements Serializable {
	
	private static final long serialVersionUID = 1L;
	String saleId;
	String customerId;
	double remainingAmout;
	String newCode;
	String remType;
	String firstName;
	String lastName;
	String email;
	double originalAmount;
	double taxAmount;
	double taxRate;
	String dlvPassId;
	String id;
	String orderDate;
	String status;
	String approvedBy;
	String fdCustomerId;
	
	public String getFdCustomerId() {
		return fdCustomerId;
	}
	public void setFdCustomerId(String fdCustomerId) {
		this.fdCustomerId = fdCustomerId;
	}
	public double getRemainingAmout() {
		return remainingAmout;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getApprovedBy() {
		return approvedBy;
	}
	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}
	public String getSaleId() {
		return saleId;
	}
	public void setSaleId(String saleId) {
		this.saleId = saleId;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public double getRemainingAmount() {
		return remainingAmout;
	}
	public void setRemainingAmout(double remainingAmout) {
		this.remainingAmout = remainingAmout;
	}
	public String getNewCode() {
		return newCode;
	}
	public void setNewCode(String newCode) {
		this.newCode = newCode;
	}
	public String getRemType() {
		return remType;
	}
	public void setRemType(String remType) {
		this.remType = remType;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public double getOriginalAmount() {
		return originalAmount;
	}
	public void setOriginalAmount(double originalAmount) {
		this.originalAmount = originalAmount;
	}
	public double getTaxAmount() {
		return taxAmount;
	}
	public void setTaxAmount(double taxAmount) {
		this.taxAmount = taxAmount;
	}
	public double getTaxRate() {
		return taxRate;
	}
	public void setTaxRate(double taxRate) {
		this.taxRate = taxRate;
	}
	public String getDlvPassId() {
		return dlvPassId;
	}
	public void setDlvPassId(String dlvPassId) {
		this.dlvPassId = dlvPassId;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((saleId == null) ? 0 : saleId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomerCreditModel other = (CustomerCreditModel) obj;
		if (saleId == null) {
			if (other.saleId != null)
				return false;
		} else if (!saleId.equals(other.saleId))
			return false;
		return true;
	}
}

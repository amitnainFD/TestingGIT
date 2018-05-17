package com.freshdirect.fdstore.customer;

import java.util.Date;

import com.freshdirect.customer.EnumComplaintLineMethod;
import com.freshdirect.customer.EnumComplaintStatus;
import com.freshdirect.customer.ErpCustomerCreditModel;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.customer.EnumSaleType;

/**@author ekracoff*/
public class FDCustomerCreditModel implements java.io.Serializable {
	private ErpCustomerCreditModel customerCreditModel =  new ErpCustomerCreditModel();
	private String saleId;
	private String action;
	private EnumComplaintStatus status;
	private String issuedBy;
	private String approvedBy;
	private EnumComplaintLineMethod method;
	private EnumSaleType orderType;
	private String refSaleId;
	
	public double getAmount(){
		return customerCreditModel.getAmount();
	}
	
	public void setAmount(double amount){
		customerCreditModel.setAmount(amount);
	}
	
	public double getOriginalAmount(){
		return customerCreditModel.getOriginalAmount();
	}
	
	public void setOriginalAmount(double originalAmount){
		customerCreditModel.setOriginalAmount(originalAmount);
	}
	
	public double getRemainingAmount(){
		return customerCreditModel.getRemainingAmount();
	}
	
	public void setRemainingAmount(double remainingAmount){
		customerCreditModel.setRemainingAmount(remainingAmount);
	}
	
	public String getDepartment(){
		return customerCreditModel.getDepartment();
	}
	
	public void setDepartment(String department){
		customerCreditModel.setDepartment(department);
	}
	
	public Date getCreateDate(){
		return customerCreditModel.getCreateDate();
	}
	
	public void setCreateDate(Date date){
		customerCreditModel.setCreateDate(date);
	}

	public String getAction() {
		return action;
	}

	public String getApprovedBy() {
		return approvedBy;
	}

	public String getIssuedBy() {
		return issuedBy;
	}

	public EnumComplaintStatus getStatus() {
		return status;
	}

	public void setAction(String string) {
		action = string;
	}

	public void setApprovedBy(String string) {
		approvedBy = string;
	}

	public void setIssuedBy(String string) {
		issuedBy = string;
	}

	public void setStatus(EnumComplaintStatus  status) {
		this.status = status;
	}

	public String getSaleId() {
		return saleId;
	}

	public void setSaleId(String string) {
		saleId = string;
	}
	
	public String getComplaintPk(){
		return customerCreditModel.getComplaintPk().getId();
	}
	
	public void setComplaintPk(PrimaryKey complaintPk){
		customerCreditModel.setComplaintPk(complaintPk);
	}

	public EnumComplaintLineMethod getMethod() {
		return method;
	}

	public void setMethod(EnumComplaintLineMethod method) {
		this.method = method;
	}
	public EnumSaleType getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = EnumSaleType.getSaleType(orderType);
	}

	public void setRefSaleId(String refSaleId) {
		this.refSaleId = refSaleId;
	}

	public String getRefSaleId() {
		return refSaleId;
	}

}

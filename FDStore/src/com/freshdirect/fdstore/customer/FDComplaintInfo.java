package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.Date;

import com.freshdirect.customer.EnumComplaintStatus;
import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.customer.EnumSaleType;

public class FDComplaintInfo implements Serializable {
	
	private final String saleId;
	private String complaintId;
	private double orderAmount;
	private Date deliveryDate;
	private EnumSaleStatus saleStatus;
	private String email;
	private String firstName;
	private String lastName;
	private double complaintAmount;
	private String complaintType;
	private String complaintNote;
	
	private String issuedBy;
	private String approvedBy;
	private EnumComplaintStatus complaintStatus;
	private EnumSaleType orderType;
	
	private String eStore;
	private String facility;
	
	public FDComplaintInfo (String saleId){
		this.saleId = saleId;
	}

	public String getSaleId() {
		return this.saleId;
	}
	
	public String getComplaintId() {
		return this.complaintId;
	}

	public void setComplaintId(String complaintId) {
		this.complaintId = complaintId;
	}

	public double getOrderAmount(){
		return this.orderAmount;
	}
	
	public void setOrderAmount(double orderAmount){
		this.orderAmount = orderAmount;
	}
	
	public Date getDeliveryDate(){
		return this.deliveryDate;
	}
	
	public void setDeliveryDate(Date deliveryDate){
		this.deliveryDate = deliveryDate;
	}
	
	public EnumSaleStatus getSaleStatus(){
		return this.saleStatus;
	}
	
	public void setSaleStatus(EnumSaleStatus saleStatus){
		this.saleStatus = saleStatus;
	}
	
	public String getEmail () {
		return this.email;
	}
	
	public void setEmail(String email){
		this.email = email;
	}
	
	public String getFirstName(){
		return this.firstName;
	}
	
	public void setFirstName(String firstName){
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return this.lastName;
	}
	
	public void setLastName(String lastName){
		this.lastName = lastName;
	}
	
	public double getComplaintAmount(){
		return this.complaintAmount;
	}
	
	public void setComplaintAmount(double complaintAmount){
		this.complaintAmount = complaintAmount;
	}
	
	public String getComplaintType(){
		return this.complaintType;
	}
	
	public void setComplaintType(String complaintType){
		this.complaintType = complaintType;
	}
	
	public String getComplaintNote(){
		return this.complaintNote;
	}
	
	public void setComplaintNote(String complaintNote){
		this.complaintNote = complaintNote;
	}
	
	public String getIssuedBy(){
		return this.issuedBy;
	}
	
	public void setIssuedBy(String issuedBy){
		this.issuedBy = issuedBy;
	}
	
	public String getApprovedBy(){
		return this.approvedBy;
	}
	
	public void setApprovedBy(String approvedBy){
		this.approvedBy = approvedBy;
	}
	
	public EnumComplaintStatus getComplaintStatus(){
		return this.complaintStatus;
	}
	
	public void setComplaintStatus(EnumComplaintStatus complaintStatus){
		this.complaintStatus = complaintStatus;
	}

	public EnumSaleType getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = EnumSaleType.getSaleType(orderType);
	}

	public String geteStore() {
		return eStore;
	}

	public void seteStore(String eStore) {
		this.eStore = eStore;
	}

	public String getFacility() {
		return facility;
	}

	public void setFacility(String facility) {
		this.facility = facility;
	}

	
}

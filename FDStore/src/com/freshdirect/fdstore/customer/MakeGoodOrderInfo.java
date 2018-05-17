/*
 * Created on Feb 28, 2005
 *
 */
package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.Date;

import com.freshdirect.customer.EnumSaleStatus;

/**
 * @author jangela
 *
 */
public class MakeGoodOrderInfo implements Serializable {
	private final String saleId;
	private Date deliveryDate;
	private Date orderPlacedDate;
	private EnumSaleStatus saleStatus;
	private double amount;
	private String firstName;
	private String lastName;
	private String route;
	private String stop;
	
	public MakeGoodOrderInfo (String saleId){
		this.saleId = saleId;
	}
	
	public String getSaleId(){
		return this.saleId;
	}
	
	public void setDeliveryDate(Date deliveryDate){
		this.deliveryDate = deliveryDate;
	}
	
	public Date getDeliveryDate(){
		return this.deliveryDate;
	}
	
	public void setOrderPlacedDate(Date orderPlacedDate){
		this.orderPlacedDate = orderPlacedDate;
	}
	
	public Date getOrderPlacedDate(){
		return this.orderPlacedDate;
	}
	
	public void setSaleStatus(EnumSaleStatus saleStatus){
		this.saleStatus = saleStatus;
	}
	
	public EnumSaleStatus getSaleStatus(){
		return this.saleStatus;
	}
	
	public double getAmount(){
		return this.amount;
	}
	
	public void setAmount(double amount){
		this.amount = amount;
	}
	
	public void setFirstName(String firstName){
		this.firstName = firstName;
	}
	
	public String getFirstName(){
		return this.firstName;
	}
	
	public void setLastName(String lastName){
		this.lastName = lastName;
	}
	
	public String getLastName(){
		return this.lastName;
	}
	
	public void setRoute(String route){
		this.route = route;
	}
	
	public String getRoute(){
		return this.route;
	}
	
	public void setStop(String stop){
		this.stop = stop;
	}
	
	public String getStop(){
		return this.stop;
	}
}

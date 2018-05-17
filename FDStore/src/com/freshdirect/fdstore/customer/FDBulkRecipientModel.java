package com.freshdirect.fdstore.customer;

import java.util.ArrayList;
import java.util.List;

import com.freshdirect.framework.util.FormatterUtil;

public class FDBulkRecipientModel extends SavedRecipientModel {

	private String quantity=null;
	private List recipientsIdList=null;
	private double subTotal = 0.0;

	public List getRecipientsIdList() {
		return recipientsIdList;
	}

	public void setRecipientsIdList(List recipientsIdList) {
		this.recipientsIdList = recipientsIdList;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}
	
	public void addRecipientList(SavedRecipientModel m){
		if(recipientsIdList==null){
			recipientsIdList=new ArrayList(); 
		}
		recipientsIdList.add(m);
	}
	
	public double getSubTotal(){
		int quantity=0;
		if(this.quantity!=null || this.quantity.trim().length()>0)
			try {
				quantity=Integer.parseInt(this.quantity);
			} catch (NumberFormatException e) {
				quantity=0;
			}
		subTotal = this.getAmount()*quantity;
		return subTotal;
	}

	public String getFormattedSubtotal() {
		return FormatterUtil.formatToTwoDecimal(this.getSubTotal());
	}
	
}

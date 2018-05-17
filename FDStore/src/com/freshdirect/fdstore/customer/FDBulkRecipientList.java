package com.freshdirect.fdstore.customer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.util.FormatterUtil;
import com.freshdirect.giftcard.EnumGiftCardType;

public class FDBulkRecipientList extends ModelSupport{

	//Holds a list of gift cards in bulk mode
	private List bulkRecipentsList = new ArrayList();
	
	//Holds a list of individual gift card recipients
	private FDRecipientList fdRecipentsList = new FDRecipientList();
	
	public FDBulkRecipientList()
	{
		super();
	}
			
	public FDBulkRecipientList(Collection recipients){
		this.bulkRecipentsList.addAll(recipients);
	}
	
	public List getRecipients() {
		return bulkRecipentsList;
	}

	public void addRecipients(Collection recipents) {
		this.bulkRecipentsList.addAll(recipents);
	}
	
	public void addRecipient(FDBulkRecipientModel rm) {
		this.bulkRecipentsList.add(rm);
	}
	
	public int getRecipientIndex(int randomId) {
		int c = 0;
		for (Iterator i = this.bulkRecipentsList.iterator(); i.hasNext(); c++) {
			if (randomId == ((FDBulkRecipientModel) i.next()).getRandomId()) {
				return c;
			}
		}
		return -1;
	}

	public FDBulkRecipientModel getRecipient(int index) {
		return (FDBulkRecipientModel) this.bulkRecipentsList.get(index);
	}
	
	public FDBulkRecipientModel getRecipientById(String randomId) {
		int idx = -1;
		try{
			idx = this.getRecipientIndex(Integer.parseInt(randomId));
		}catch (NumberFormatException nfe) {
			throw new IllegalArgumentException(nfe);
		}
		return idx == -1 ? null : this.getRecipient(idx);
	}

	public void setRecipient(int index, FDBulkRecipientModel srm) {
		this.bulkRecipentsList.set(index, srm);
	}

	public void removeRecipient(int index) {
		this.bulkRecipentsList.remove(index);
	}

	public boolean removeOrderLineById(int randomId) {
		int idx = this.getRecipientIndex(randomId);
		if (idx == -1) {
			return false;
		}
		this.removeRecipient(idx);
		return true;
	}
	public double getSubtotal() {
		double subtotal = 0;
		for(Iterator it = this.bulkRecipentsList.iterator(); it.hasNext();){
			FDBulkRecipientModel model = (FDBulkRecipientModel)it.next();
			int quantity=0;
			if(model.getQuantity()!=null || model.getQuantity().trim().length()>0)
			    quantity=Integer.parseInt(model.getQuantity());
			subtotal = subtotal+ (model.getAmount()*quantity);
		}
		return subtotal;
	}
	public String getFormattedSubTotal() {
		return FormatterUtil.formatToTwoDecimal(getSubtotal());
	}
	
	public int size(){
		return this.bulkRecipentsList.size();
	
	}
	
	public void constructFDRecipientsList(){
		List list=new ArrayList();
		for(Iterator it = this.bulkRecipentsList.iterator(); it.hasNext();){
			FDBulkRecipientModel model = (FDBulkRecipientModel)it.next();
			int quantity=0;
			if(model.getQuantity()!=null || model.getQuantity().trim().length()>0){
			    quantity=Integer.parseInt(model.getQuantity());			
			    for(int i=0;i<quantity;i++){
			    	SavedRecipientModel srm = new SavedRecipientModel();
			    	srm.setRecipientEmail(model.getRecipientEmail());
			    	srm.setSenderEmail(model.getSenderEmail());
			    	srm.setDeliveryMode(model.getDeliveryMode());
			    	srm.setPersonalMessage(model.getPersonalMessage());
			    	srm.setFdUserId(model.getFdUserId());
			    	srm.setSenderName(model.getSenderName());
			    	srm.setRecipientName(model.getRecipientEmail());
			    	srm.setTemplateId(model.getTemplateId());
			    	srm.setAmount(model.getAmount());
			    	srm.setGiftCardType(EnumGiftCardType.REGULAR_GIFTCARD);
			    	list.add(srm);
			    }
			}   
		}
		//Clear previously added recipients.
		this.fdRecipentsList.clear();
		this.fdRecipentsList.addRecipients(list);
	}

	public FDRecipientList getFDRecipentsList() {
		return fdRecipentsList;
	}
	
}

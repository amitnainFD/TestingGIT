package com.freshdirect.fdstore.lists;

import java.util.Date;

import com.freshdirect.fdstore.customer.SaleStatisticsI;
import com.freshdirect.framework.core.ModelSupport;

public abstract class FDCustomerListItem extends ModelSupport implements SaleStatisticsI {

	private static final long	serialVersionUID	= 5133761393589673937L;

	private int frequency;

	private Date firstPurchase;

	private Date lastPurchase;
	
	private Date deleted;

	/**
	 * @return Returns the firstPurchase.
	 */
	public Date getFirstPurchase() {
		return firstPurchase;
	}

	/**
	 * @return Returns the frequency.
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * @return Returns the lastPurchase.
	 */
	public Date getLastPurchase() {
		return lastPurchase;
	}

	/**
	 * @param firstPurchase
	 *            The firstPurchase to set.
	 */
	public void setFirstPurchase(Date firstPurchase) {
		this.firstPurchase = firstPurchase;
	}

	/**
	 * @param frequency
	 *            The frequency to set.
	 */
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	/**
	 * @param lastPurchase
	 *            The lastPurchase to set.
	 */
	public void setLastPurchase(Date lastPurchase) {
		this.lastPurchase = lastPurchase;
	}

	public void incrementFrequency() {
		this.frequency++;
	}
	
	/**
	 * @param deleted The deleted to set.
	 */
	public void setDeleted(Date deleted) {
		this.deleted = deleted;
	}
	
	public boolean isDeleted(){
		return this.deleted == null ;
	}
	
	/**
	 * @return Returns the deleted.
	 */
	public Date getDeleted() {
		return deleted;
	}
	
}
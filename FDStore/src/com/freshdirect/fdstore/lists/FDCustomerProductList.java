/**
 * @author ekracoff
 * Created on Sep 29, 2004*/

package com.freshdirect.fdstore.lists;

import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDConfiguration;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.customer.FDProductSelectionI;
import com.freshdirect.fdstore.customer.OrderLineUtil;
import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.framework.util.log.LoggerFactory;

public abstract class FDCustomerProductList extends FDCustomerList {
	
	private static final long	serialVersionUID	= -8496978910271535604L;
	
	private final static Category LOGGER = LoggerFactory.getInstance(FDCustomerProductList.class);

	public void mergeSelection(FDProductSelectionI selection, boolean modifying, boolean allowMultipleLinesForSameSkuConfig) {
		try {

			boolean found = false;
			
			if (!allowMultipleLinesForSameSkuConfig) { 
				for (Iterator<FDCustomerListItem> i = this.getLineItems().iterator(); i.hasNext();) {
					FDCustomerProductListLineItem item = (FDCustomerProductListLineItem) i.next();
	
					if (item.getSkuCode().equals(selection.getSkuCode())
						&& OrderLineUtil.isSameConfiguration(selection, item.convertToSelection())) {
	
						if (!modifying) {
							FDCustomerProductListLineItem stat = (FDCustomerProductListLineItem) selection.getStatistics();
							if (stat == null) {
								stat = createListItem(selection);
							}
							item.setFrequency(stat.getFrequency() + item.getFrequency());
							
							Date statLastDate = stat.getLastPurchase() == null ? new Date() : stat.getLastPurchase();
							Date statFirstDate = stat.getFirstPurchase() == null ? new Date() : stat.getFirstPurchase();
							item.setLastPurchase(DateUtil.max(item.getLastPurchase(), statLastDate));
							item.setFirstPurchase(DateUtil.min(item.getFirstPurchase(), statFirstDate));
							item.setDeleted(null);
						}
	
						found = true;
						break;
					}
				}
			}

			if (!found) {
				FDCustomerProductListLineItem stat = (FDCustomerProductListLineItem) selection.getStatistics();
				if (stat == null) {
					stat = createListItem(selection);
				}
				this.getLineItems().add(stat);
				markAsModified();
			}
		} catch (FDSkuNotFoundException e) {
			LOGGER.warn("Found invalid SKU - skipping: " + e.getMessage());
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
	}

	private FDCustomerProductListLineItem createListItem(FDProductSelectionI selection) {		
		FDCustomerProductListLineItem stat = 
			new FDCustomerProductListLineItem(selection.getSkuCode(),
					new FDConfiguration(selection.getConfiguration()),
					selection.getRecipeSourceId());
		stat.setFrequency(1);
		stat.setFirstPurchase(new Date());
		stat.setLastPurchase(new Date());
		
		return stat;
	}

	public void mergeLineItem(FDCustomerProductListLineItem newItem) {
		try {
			FDProductSelectionI sel = newItem.convertToSelection();
			this.mergeSelection(sel, false, false);
			markAsModified();
		} catch (FDSkuNotFoundException e) {
			LOGGER.warn("Found invalid SKU - skipping: " + e.getMessage());
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
	}
	
	
	// This method it brings the list up to date, throwing away 
	//anything that can't be updated
	public void cleanList() throws FDResourceException {
		assert (getLineItems() != null);
		Date modDate = getModificationDate();
		setLineItems(OrderLineUtil.updateCCLItems(getLineItems()));
		// the call to setLineItem will mark the list as modified
		// this is not needed - restore the modification date to the old value
		unmarkAsModified(modDate);
	}
}
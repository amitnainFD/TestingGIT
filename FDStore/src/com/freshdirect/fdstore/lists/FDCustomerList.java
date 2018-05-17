package com.freshdirect.fdstore.lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.ejb.EnumCustomerListType;
import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

public abstract class FDCustomerList extends ModelSupport {
	
	private static final long	serialVersionUID	= -6109376082050638762L;

	protected PrimaryKey customerPk;

	protected String name;
	
	protected Date createDate;
	
	/** The timestamp of the last modification of the list. */
	protected Date modificationDate;
	
	protected String eStoreType;
	
	protected List<FDCustomerListItem> lineItems = new ArrayList<FDCustomerListItem>();

	
	
	private static class CompareByModificationDate implements Comparator<FDCustomerList> {
		
		public int compare( FDCustomerList l1, FDCustomerList l2 ) {

			if ( l1 == null || l1.getModificationDate() == null ) {
				return 1;
			}
			if ( l2 == null || l2.getModificationDate() == null ) {
				return -1;
			}

			return -l1.getModificationDate().compareTo( l2.getModificationDate() );
		}
	}
	
	private static class CompareByName implements Comparator<FDCustomerList> {

		public int compare( FDCustomerList l1, FDCustomerList l2 ) {

			if ( l1 == null || l1.getName() == null ) {
				return -1;
			}
			if ( l2 == null || l2.getName() == null ) {
				return 1;
			}
			// should not return 0, since YoYo and yoyo than would be treated equal
			return l1.getName().compareToIgnoreCase( l2.getName() ) < 0 ? -1 : 1;
		}
	}
	
	private static class CompareByItemCount implements Comparator<FDCustomerList> {

		public int compare( FDCustomerList l1, FDCustomerList l2 ) {
			if ( l1 == null )
				return -1;
			if ( l2 == null )
				return 1;

			if ( l1.equals( l2 ) )
				return 0;
			
			int c1 = l1.getCount();
			int c2 = l2.getCount(); 
			
			return c1 < c2 ? 1 : -1;				
		}
	}

	private static Comparator<FDCustomerList> compareByModificationDate = new CompareByModificationDate();
	private static Comparator<FDCustomerList> compareByName = new CompareByName();
	private static Comparator<FDCustomerList> compareByItemCount = new CompareByItemCount();
	
	public static Comparator<FDCustomerList> getModificationDateComparator() { return compareByModificationDate; }
	public static Comparator<FDCustomerList> getNameComparator() { return compareByName; }
	public static Comparator<FDCustomerList> getItemCountComparator() { return compareByItemCount; }
	

	
	public void setCustomerPk(PrimaryKey customerPk) {
		markAsModified();
		this.customerPk = customerPk;
	}

	public PrimaryKey getCustomerPk() {
		return customerPk;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		markAsModified();
		this.name = name;
	}
	
	/**
	 * @return List<FDCustomerListItem>
	 */
	public List<FDCustomerListItem> getLineItems() {
		return lineItems;
	}

	/**
	 * @param lineItems List of FDCustomerListLineItem
	 * @throws FDResourceException 
	 */
	public void setLineItems(List<FDCustomerListItem> lineItems) {
		markAsModified();
		this.lineItems = lineItems;
	}

	public void addLineItem(FDCustomerListItem item) {
		getLineItems().add(item);
		markAsModified();
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		markAsModified();
		this.createDate = createDate;
	}

	/**
	 *  Get the last modification timestamp for the list.
	 *  
	 *  @return the time of when the list was last modified.
	 */
	public Date getModificationDate() {
		return modificationDate;
	}
	
	/**
	 *  Set the timestamp for the last modification of the list.
	 *  
	 *  @param modificationDate the timestamp of the last modification of the
	 *         list.
	 */
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}
	
	/**
	 *  Return the type of list this implementation handles.
	 *  
	 *  @return the type of list this implementation handles.
	 */
	public abstract EnumCustomerListType getType();
	
	/**
	 *  Mark the list as modified. This will update the last modification date,
	 *  for example.
	 */
	protected void markAsModified() {
		setModificationDate(new Date());
	}
	
	/**
	 *  Reverse a previous marking of this list as modified.
	 *  Resets the last modification date to the supplied date.
	 *  
	 *  @param modDate the old modification date, that was valid before
	 *         the call to markAsModified(). it is the responsibility of the
	 *         caller to store this date if he wants to unmark a list.
	 */
	// FIXME this does not make much sense really ... what was the original intention here? 
	protected void unmarkAsModified(Date modDate) {
		setModificationDate(modDate);
	}
	
	public int getCount() {
		return lineItems == null ? 0 : lineItems.size();
	}
	
	
	public void removeAllLineItems() {
		lineItems.clear();
	}

	
	private String recipeId = null;
	private String recipeName = null;

	public String getRecipeId() {
		return recipeId;
	}
	
	/**
	 * @return the eStoreType
	 */
	public String geteStoreType() {
		return eStoreType;
	}
	/**
	 * @param eStoreType the eStoreType to set
	 */
	public void seteStoreType(String eStoreType) {
		this.eStoreType = eStoreType;
	}
	public void setRecipeId( String recipeId ) {
		this.recipeId = recipeId;
	}
	
	public String getRecipeName() {
		return recipeName;
	}
	
	public void setRecipeName(String recipeName) {
		this.recipeName = recipeName;
	}

}
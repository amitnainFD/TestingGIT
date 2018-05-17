/**
 * @author ekracoff
 * Created on Sep 29, 2004*/

package com.freshdirect.fdstore.lists;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.freshdirect.fdstore.customer.ejb.EnumCustomerListType;
import com.freshdirect.framework.core.PrimaryKey;

public class FDCustomerRecipeList extends FDCustomerList {

	public static final String EVERY_RECIPE_LIST = "Recipes";

	public FDCustomerRecipeList() {
	}
	
	public FDCustomerRecipeList(PrimaryKey pk, PrimaryKey customerPk,
			String name) {
		setPK(pk);
		this.setCustomerPk(customerPk);
		this.setName(name);
		markAsModified();
	}

	public FDCustomerRecipeList(PrimaryKey customerPk, String name) {
		this(null, customerPk, name);
	}

	public void mergeRecipe(String recipeId, boolean modifying) {

		boolean found = false;

		for (Iterator i = this.getLineItems().iterator(); i.hasNext();) {
			FDCustomerRecipeListLineItem item = (FDCustomerRecipeListLineItem) i.next();
			if (item.getRecipeId().equals(recipeId)) {
				if (!modifying) {
					item.setFrequency(item.getFrequency() + 1);
					item.setLastPurchase(new Date());
					item.setDeleted(null);
				}
				found = true;
				break;
			}
		}

		if (!found) {
			markAsModified();
			this.getLineItems().add(createListItem(recipeId));
		}

	}

	private FDCustomerRecipeListLineItem createListItem(String recipeId) {
		FDCustomerRecipeListLineItem stat = new FDCustomerRecipeListLineItem();
		stat.setRecipeId(recipeId);
		stat.setFrequency(1);
		stat.setFirstPurchase(new Date());
		stat.setLastPurchase(new Date());
		return stat;
	}

	public List getAvailableLineItems() {
		List items = new ArrayList(this.getLineItems());
		for (Iterator i = items.iterator(); i.hasNext();) {
			FDCustomerRecipeListLineItem item = (FDCustomerRecipeListLineItem) i.next();
			if (item.getRecipe()==null || !item.getRecipe().isAvailable()) {
				i.remove();
			}
		}
		return items;
	}

	/**
	 *  Return the type of list this implementation handles.
	 *  
	 *  @return the list type corresponding to recipe lists
	 *  @see EnumCustomerListType#RECIPE_LIST
	 */
	public EnumCustomerListType getType() {
		return EnumCustomerListType.RECIPE_LIST; 
	}

}
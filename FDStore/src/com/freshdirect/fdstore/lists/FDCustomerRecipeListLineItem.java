package com.freshdirect.fdstore.lists;

import java.util.Comparator;

import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.Recipe;

public class FDCustomerRecipeListLineItem extends FDCustomerListItem {

	private static final long	serialVersionUID	= -367298453449183662L;

	public final static Comparator<FDCustomerRecipeListLineItem> NAME_COMPARATOR = new Comparator<FDCustomerRecipeListLineItem>() {
		public int compare( FDCustomerRecipeListLineItem l1, FDCustomerRecipeListLineItem l2 ) {
			return l1.getRecipe().getName().compareTo(l2.getRecipe().getName());
		}		
	};
	
	private String recipeId;
	private String recipeName;

	public String getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(String skuCode) {
		this.recipeId = skuCode;
	}

	public String getRecipeName() {
		return recipeName;
	}

	public void setRecipeName(String recipeName) {
		this.recipeName = recipeName;
	}

	public Recipe getRecipe() {
		return (Recipe) ContentFactory.getInstance().getContentNode(recipeId);
	}

}

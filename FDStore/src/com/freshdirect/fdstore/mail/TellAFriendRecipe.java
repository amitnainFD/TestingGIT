package com.freshdirect.fdstore.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.*;

import javax.servlet.ServletContext;

import com.freshdirect.fdstore.*;
import com.freshdirect.fdstore.util.*;
import com.freshdirect.fdstore.content.*;
import com.freshdirect.fdstore.content.view.WebProductRating;

/**
 * Class to take care of the details of sending a recipe recommendation
 * in an e-mail.
 * 
 * @author Akos Maroy
 */

public class TellAFriendRecipe extends TellAFriend {

	/**
	 *  The ID of the recipe.
	 */
	private String recipeId;
	
	/**
	 *  The description of the recipe.
	 */
	private String description;
	
	/**
	 *  Constructor, based on a recipe id.
	 *  
	 *  @param recipeId the ID of the recipe to tell a friend about.
	 */
	public TellAFriendRecipe(String recipeId) {
		super();
		
		this.recipeId = recipeId;
		
		String defaultEmailText = "I thought you might be interested in this recipe that was featured at FreshDirect...";
		
		this.setEmailText(defaultEmailText);
	}

	public void setRecipeId(String recipeId) {
		this.recipeId = recipeId;	
	}
	
	public String getRecipeId() {
		return this.recipeId;
	}
	
	/**
	 *  Tell if this object tells about the specified product or not.
	 *  
	 *  @param productId the id of the product.
	 *  @return true if this object tells about the product with the specified id,
	 *          false otherwise.
	 */
	public boolean isAbout(String productId) {
		return this.recipeId.equalsIgnoreCase(productId);
	}


	public Recipe getRecipe() {
		return (Recipe) ContentFactory.getInstance().getContentNode(recipeId);
	}
	
	public String getProductDescription() {
		return this.description;
	}
	
	public void setProductDescription(String description) {
		this.description = description;
	}
	
	/**
	 *  Fill in the description field of the object, based on the associated
	 *  product's HTM description media.
	 *   
	 *  @param ctx the servlet context, used to fetch the HTML media
	 *  @throws IOException on I/O errors
	 *  @throws FDResourceException on FD resource errors
	 */
	protected void decorate(ServletContext ctx) throws IOException, FDResourceException {
		
		Recipe recipe = getRecipe();
		
		if (recipe != null) {
			Html         html = recipe.getDescription();
			StringBuffer sb   = new StringBuffer();
			
			if (html != null) {
				InputStream is = ctx.getResourceAsStream(html.getPath());
				if (is != null) {
					Reader r   = new InputStreamReader(is);
					char[] buf = new char[1024];
					int    len;
					while ((len = r.read(buf)) != -1) {
						sb.append(buf, 0, len);
					}
					r.close();
				}
			}
			
			setProductDescription(sb.toString());
			
		} else {
			setProductDescription("");
		}
	}
	
	/**
	 *  Return the name of the XSL transformation document that
	 *  wil be used to convert the XML format of the message
	 *  to an HTML e-mail.
	 *  
	 *  @return the name of an XSL transformation document
	 */
	public String getXsltPath() {
		return "h_recipe.xsl";
	}

}

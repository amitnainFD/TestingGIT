/**
 * 
 * ProductRating.java
 * Created Dec 10, 2002
 */
package com.freshdirect.fdstore.content.view;

import java.io.Serializable;

/**
 *
 *  @author knadeem
 */
public class ProductRating implements Serializable {
	private static final long serialVersionUID = 1173007518807218437L;

	private String ratingName;
	private String rating;
	
	public ProductRating(String ratingName, String rating){
		this.ratingName = ratingName;
		this.rating = rating;
	}
	
	public String getRatingName(){
		return this.ratingName;
	}
	
	public void setRatingName(String ratingName){
		this.ratingName = ratingName;
	}
	
	public String getRating(){
		return this.rating;
	}
	
	public void setRating(String rating){
		this.rating = rating;
	}

}

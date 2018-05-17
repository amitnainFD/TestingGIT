/**
 * 
 * WebProductRating.java
 * Created Dec 9, 2002
 */
package com.freshdirect.fdstore.content.view;

/**
 *
 *  @author knadeem
 */
import java.io.Serializable;
import java.util.List;

public class WebProductRating implements Serializable {
	private static final long serialVersionUID = -7393075498094272983L;

	private String ratingLabel;
	private List<ProductRating> ratings;
	private List<ProductRating> textRatings;
	private String compareByLabel;
	private String linkParams;
	
	public WebProductRating(String ratingLabel, List<ProductRating> ratings, List<ProductRating> textRatings, String compareByLabel, String linkParams){
		this.ratingLabel = ratingLabel;
		this.ratings = ratings;
		this.textRatings = textRatings;
		this.compareByLabel = compareByLabel;
		this.linkParams = linkParams;
	}

	public String getRatingLabel(){
		return this.ratingLabel;
	}
	
	public void setRatingLabel(String ratingLabel){
		this.ratingLabel = ratingLabel;
	}
	
	public List<ProductRating> getRatings(){
		return this.ratings;
	}
	
	public void setRatings(List<ProductRating> ratings){
		this.ratings = ratings;
	}
	
	public List<ProductRating> getTextRatings() {
	    return this.textRatings;
	}
        
    public void setTextRatings(List<ProductRating> textRatings) {
        this.textRatings=textRatings;
    }
        
	public String getCompareByLabel(){
		return this.compareByLabel;
	}
	
	public void setCompareByLabel(String compareByLabel){
		this.compareByLabel = compareByLabel;
	}
	
	public String getLinkParams(){
		return this.linkParams;
	}
	
	public void setLinkParams(String linkParams){
		this.linkParams = linkParams;
	}
}

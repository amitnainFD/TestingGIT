package com.freshdirect.fdstore.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;

import com.freshdirect.fdstore.FDCachedFactory;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.ZonePriceListing;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.Html;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.SkuModel;
import com.freshdirect.fdstore.content.view.WebProductRating;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.util.HowToCookItUtil;
import com.freshdirect.fdstore.util.RatingUtil;

public class TellAFriendProduct extends TellAFriend {

	private String productId;
	private String categoryId;
	private String description;
	private final static NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance( Locale.US );
	
	public TellAFriendProduct(String categoryId, String productId) {
		super();
		this.categoryId = categoryId;
		this.productId = productId;
		String defaultEmailText = "I was shopping at FreshDirect and thought you'd be interested in the "+
			(getProduct() != null ? getProduct().getFullName() : "")+"."; 
		this.setEmailText(defaultEmailText);
	}

	public void setProductId(String productId) {
		this.productId = productId;	
	}
	
	public String getProductId() {
		return this.productId;
	}

	/**
	 *  Tell if this object tells about the specified product or not.
	 *  
	 *  @param productId the id of the product.
	 *  @return true if this object tells about the product with the specified id,
	 *          false otherwise.
	 */
	public boolean isAbout(String productId) {
		return this.productId.equalsIgnoreCase(productId);
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;	
	}
	
	public String getCategoryId() {
		return this.categoryId;
	}

	protected ProductModel getProduct() {
		return ContentFactory.getInstance().getProductByName(this.categoryId, this.productId);
	}
	
	public String getProductTitle(){
		ProductModel productNode = getProduct();
		if(productNode == null){
			return "";
		}else{
			return productNode.getFullName();
		}
	}
	
	public String getProductImagePath() {
		ProductModel productNode = getProduct();
		if(productNode == null){
			return "";
		}else{
			return productNode.getDetailImage().getPath();
		}
	}
	
	public String getProductDescription(){
		return this.description;
	}
	
	public void setProductDescription(String description){
		this.description = description;
	}
	
	
	public String getDefaultPrice() throws FDSkuNotFoundException, FDResourceException{
		ProductModel productNode = getProduct();
		if (productNode == null) {
			return "";
		} else {
			return productNode.getPriceCalculator().getDefaultPrice();
		}
	}
	
	public WebProductRating getProductRatings() throws FDResourceException {
		return RatingUtil.getRatings(getProduct());
	}
	
	public List getHowToCookIt() throws FDResourceException {
		return HowToCookItUtil.getHowToCookIt(getProduct());
	}
		
	/**
	 *  Return the name of the XSL transformation document that
	 *  wil be used to convert the XML format of the message
	 *  to an HTML e-mail.
	 *  
	 *  @return the name of an XSL transformation document
	 */
	public String getXsltPath() {
		return "h_tell_a_friend_3.xsl";
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
		
		ProductModel productNode = getProduct();
		
		if (productNode != null) {
			Html         html = productNode.getProductDescription();
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

}

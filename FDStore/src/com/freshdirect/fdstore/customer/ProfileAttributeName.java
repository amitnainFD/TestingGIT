/*
 * Created on Jun 27, 2005
 *
 */
package com.freshdirect.fdstore.customer;

import java.io.Serializable;

/**
 * @author jng
 */
public class ProfileAttributeName implements Serializable {

	private String name;
	private String description;
	private String category;
	private String attributeValueType;
	private boolean isEditable;
	
	public String getName() { return this.name; }
	public void setName(String name) { this.name=name; }
	
	public String getDescription() { return this.description; }
	public void setDescription(String description) { this.description=description; }
	
	public String getCategory() { return this.category; }
	public void setCategory(String category) { this.category=category; }

	public String getAttributeValueType() { return this.attributeValueType; }
	public void setAttributeValueType(String attributeValueType) {this.attributeValueType=attributeValueType; }

	public boolean getIsEditable() { return this.isEditable; }
	public void setIsEditable(boolean isEditable) { this.isEditable=isEditable; }
	
}

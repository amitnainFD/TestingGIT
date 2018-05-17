package com.freshdirect.fdstore.promotion.management;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

public class FDPromotionAttributeParam extends ModelSupport implements Comparable {
	
	private String attributeName;
	private String desiredValue;	
	private String attributeIndex;
	
	public FDPromotionAttributeParam() {
		super();
	}

	public FDPromotionAttributeParam(PrimaryKey pk) {
		this();
		this.setPK(pk);
	}
		
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeName() {
		return this.attributeName;
	}

	public void setDesiredValue(String desiredValue) {
		this.desiredValue = desiredValue;
	}

	public String getDesiredValue() {
		return this.desiredValue;
	}

	public String toString() {
		return "ProfileAttributeParam[ "+this.attributeName+
				" ," + this.desiredValue + " ," + this.attributeIndex +"]";
	}

	public String getAttributeIndex() {
		return attributeIndex;
	}

	public void setAttributeIndex(String attributeIndex) {
		this.attributeIndex = attributeIndex;
	}

	public int compareTo(Object o) {
		 
		int retVal = -1;
		if(o instanceof FDPromotionAttributeParam) {
			FDPromotionAttributeParam tmpInput = (FDPromotionAttributeParam)o;			
			if(tmpInput.getAttributeIndex() != null) {
				try {
					return new Integer(this.getAttributeIndex()).compareTo(new Integer(tmpInput.getAttributeIndex()));
				} catch(NumberFormatException nfe) {
					//Do Nothing Just Ignore Comparison
				}
			}
		}
		return retVal;
	}

}

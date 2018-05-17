package com.freshdirect.fdstore.coremetrics.tagmodel;

import java.util.ArrayList;
import java.util.List;


public class ElementTagModel extends AbstractTagModel  {

	private static final long	serialVersionUID	= 7270886672644021546L;

	private static final String functionName = "cmCreateElementTag";
	
	private String elementId;
	private String elementCategory;
	
	public String getElementId() {
		return elementId;
	}
	
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}
	
	public String getElementCategory() {
		return elementCategory;
	}

	public void setElementCategory(String elementCategory) {
		this.elementCategory = elementCategory;
	}
	
	@Override
	public String getFunctionName() {
		return functionName;
	}

	@Override
	public List<String> toStringList() {
		List<String> elementData = new ArrayList<String>();		
		elementData.add( getFunctionName() );
		elementData.add( getElementId() ); 
		elementData.add( getElementCategory() ); 
		elementData.add( mapToAttrString( getAttributesMaps() ) );
		return elementData;
	} 
	
}
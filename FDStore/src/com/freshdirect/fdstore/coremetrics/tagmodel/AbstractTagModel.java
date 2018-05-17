package com.freshdirect.fdstore.coremetrics.tagmodel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractTagModel implements Serializable {
	
	private static final long	serialVersionUID	= -1513448512448412441L;
	
	private Map<Integer,String> attributesMaps = new HashMap<Integer,String>();

	public Map<Integer,String> getAttributesMaps() {
		return attributesMaps;
	}
	
	public abstract String getFunctionName();	
	public abstract List<String> toStringList();
	
	public static final String ATTR_DELIMITER = "-_-";

	public static String mapToAttrString(Map<Integer,String> attributesMap){
		StringBuilder attrSb = new StringBuilder();
		
		int maxKey = 0; 
		for (Integer key : attributesMap.keySet()){
			if (key > maxKey){
				maxKey = key;
			}
		}
		
		for (int i=1; i<=maxKey; i++){
			String attribute = attributesMap.get(i);
			attrSb.append(attribute==null? "" : attribute);
			attrSb.append(ATTR_DELIMITER);
		}
		
		if (maxKey>0){
			return attrSb.substring(0, attrSb.length() - ATTR_DELIMITER.length());
		} else {
			return attrSb.toString();
		}
		
	}

}
package com.freshdirect.fdstore.referral;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;

public class ReferralSearchCriteria implements Serializable {

	private static final int FETCH_SIZE=FDStoreProperties.getReferralPrgPaginationSize(); 
	private int startIndex;
	private int endIndex;
	private String sortByColumnName;
	private int totalRcdSize;
	
	
	public int getEndIndex() {
		if(totalRcdSize!=0 && (startIndex+FETCH_SIZE)>totalRcdSize){
			return totalRcdSize;
		}
		return startIndex+FETCH_SIZE;
	}
	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}
	public String getSortByColumnName() {
		return sortByColumnName;
	}
	public void setSortByColumnName(String sortByColumnName) {
		this.sortByColumnName = sortByColumnName;
	}
	public int getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
	public boolean isBlank() {
		return this.sortByColumnName == null;
	}
	
	public int getPaginationRcdCapacity()
	{
		return FETCH_SIZE;
	}
	
	public int getPreviousIndex()
	{
		if(this.startIndex==0){
			return this.startIndex; 
		}else{
			return this.startIndex-FETCH_SIZE;
		}
	}

	public String getCriteria() throws FDResourceException {
		StringBuffer buf = new StringBuffer();
		for(Iterator i = this.getCriteriaMap().entrySet().iterator(); i.hasNext(); ) {
			Map.Entry e = (Entry) i.next();
			buf.append(" ").append(e.getKey()).append(": ").append(e.getValue());
		}
		
		return buf.toString().trim();
	}
	
	public Map getCriteriaMap() throws FDResourceException {
		Map m = new LinkedHashMap();

		m.put("Start Index", ""+this.startIndex);
		m.put("Start Index", ""+(this.startIndex+FETCH_SIZE));
		
		if (this.sortByColumnName != null && sortByColumnName.trim().length()>0) {
			m.put("SoryByColumnName", this.sortByColumnName);
		}
		
		return m;
	}

	public String toString() {

		StringBuffer buf = new StringBuffer();
		buf.append("[Start Index: ").append(this.startIndex);
		buf.append("End Index: ").append(this.startIndex+FETCH_SIZE);
		buf.append("SortByColumnName: ").append(this.sortByColumnName).append("]");
		return buf.toString();
	}
	public int getTotalRcdSize() {
		return totalRcdSize;
	}
	public void setTotalRcdSize(int totalRcdSize) {
		this.totalRcdSize = totalRcdSize;
	}

}

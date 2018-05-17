package com.freshdirect.fdstore.promotion.management;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class FDPromoZipRestriction implements java.io.Serializable {

	private Date startDate;
	private List zipCodes;
	private String type;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getZipCodes() {
		String str = "";
		if (this.zipCodes != null && this.zipCodes.size() > 0) {
			Iterator iter = this.zipCodes.iterator();
			while (iter.hasNext()) {
				if (!"".equals(str)) {
					str += ",";
				}
				str += ((String)iter.next()).trim();
			}
		}
		return str; //(!"".equals(str)) ? str : null;
	}
	public List getZipCodeList(){
		return this.zipCodes;
	}
	public void setZipCodes(String zipCodes) {
		this.zipCodes = new ArrayList();
		StringTokenizer st = new StringTokenizer(zipCodes, ",");
		int count = st.countTokens(); 
		for (int i = 0; i < count; i++) {
			String token = st.nextToken().trim();
			this.zipCodes.add(token);
		}
	}
	public void setZipCodes(List zipCodes){
		this.zipCodes = zipCodes;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date zipRestrictionDate) {
		this.startDate = zipRestrictionDate;
	}
	public String toString(){
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		return "<b>Date:</b> "+dateFormatter.format(this.getStartDate()) + "<br> Zipcode: "+this.getZipCodes()+"<br> Restriction Type: "+this.getType()+"<br>";
	}
}

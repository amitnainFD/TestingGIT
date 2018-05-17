package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.freshdirect.fdstore.FDStoreProperties;

public class FDCSContactHoursUtil implements Serializable{

	private static final long serialVersionUID = -8307954581357427455L;
	public static List<FDCSContactHours> getFDCSHours(){
		String days=FDStoreProperties.getCSContactDays();
		String hours=FDStoreProperties.getCSContactHours();
		
		return parseValues(days, hours);
	}
	
	public static List<FDCSContactHours> getFDXCSHours(){
		String days=FDStoreProperties.getCSContactDaysFDX();
		String hours=FDStoreProperties.getCSContactHoursFDX();
		
		return parseValues(days, hours);
	}
	
	private static List<FDCSContactHours> parseValues(String days, String hours) {
		List<FDCSContactHours> list = new ArrayList<FDCSContactHours>();
		if(null != days & null != hours){
			String[] daysArr =days.split(",");
			String[] hoursArr =hours.split(",");
			for (int i = 0; i < hoursArr.length; i++) {
				FDCSContactHours contactHrs = new FDCSContactHours(" "+daysArr[i]+" ",hoursArr[i]);
				list.add(contactHrs);
			}
		}
		return list;
	}
}

package com.freshdirect.fdstore.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.freshdirect.delivery.restriction.AlcoholRestriction;
import com.freshdirect.delivery.restriction.DlvRestrictionsList;
import com.freshdirect.delivery.restriction.EnumDlvRestrictionCriterion;
import com.freshdirect.delivery.restriction.EnumDlvRestrictionReason;
import com.freshdirect.delivery.restriction.RestrictionI;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;

public class RestrictionUtil {
	private final static Comparator<AlcoholRestriction> ACL_RESTRICTION_DATE_COMPARATOR = new Comparator<AlcoholRestriction>() {

		public int compare(AlcoholRestriction r1, AlcoholRestriction r2) {
			if(r1 == null || r1.getDateRange() == null || r2 == null || r2.getDateRange() == null ) return -1;
			return r2.getDateRange().getStartDate().compareTo(r1.getDateRange().getStartDate());
		}
	};

	
	/**
	 * @param state Matching state
	 * @param county Matching county
	 * @param restrictions Source list
	 *
	 * @return Filtered list
	 */
	public static List<RestrictionI> filterAlcoholRestrictionsForStateCounty(String state, String county, List<RestrictionI> restrictions){
		List<RestrictionI> filteredList = new ArrayList<RestrictionI>();
		List<AlcoholRestriction> stateRestrictions = new ArrayList<AlcoholRestriction>();
		List<AlcoholRestriction> countyRestrictions = new ArrayList<AlcoholRestriction>();
		
		for (RestrictionI restriction : restrictions) {
			if (restriction instanceof AlcoholRestriction){
				AlcoholRestriction res = (AlcoholRestriction) restriction;
				if(res.getState() != null && res.getCounty() != null){
					//Restriction defined at county level. 
					if(res.getState().equalsIgnoreCase(state) && res.getCounty().equalsIgnoreCase(county)){
						countyRestrictions.add(res);
					}
				}
				if(res.getState() != null && res.getCounty() == null){
					//Restriction defined at state level.
					if(res.getState().equalsIgnoreCase(state)){
						stateRestrictions.add(res);
					}
				}
			} else {
				// reserve other restriction
				filteredList.add(restriction);
			}
		}
		if(countyRestrictions.size() > 0){
			if(countyRestrictions.size() > 1) //Sort by highest start date.
				Collections.sort(countyRestrictions, ACL_RESTRICTION_DATE_COMPARATOR);
			filteredList.addAll(countyRestrictions);
		}
		else if(stateRestrictions.size() > 0) {
			if(stateRestrictions.size() > 1) //Sort by highest start date.
				Collections.sort(stateRestrictions, ACL_RESTRICTION_DATE_COMPARATOR);
			filteredList.addAll(stateRestrictions);
		}
		
		return filteredList; 
	}
	
	public static boolean isAlcoholRestrictionAvailableForCounty(String county) throws FDResourceException{
		DlvRestrictionsList allRestrictions = FDDeliveryManager.getInstance().getDlvRestrictions();
		Set<EnumDlvRestrictionReason> alcoholReasons = 
			new HashSet<EnumDlvRestrictionReason>(EnumDlvRestrictionReason.getAlcoholEnumList());
		List<RestrictionI> restrictions = allRestrictions.getRestrictions(EnumDlvRestrictionCriterion.DELIVERY, alcoholReasons);
		
		for (RestrictionI restriction : restrictions) {
			if(restriction instanceof AlcoholRestriction){
				AlcoholRestriction res = (AlcoholRestriction) restriction;
				if(res.getCounty() != null && res.getCounty().equalsIgnoreCase(county)){
					//Restriction defined at county level. 
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isAlcoholRestrictionAvailableForState(String state) throws FDResourceException{
		DlvRestrictionsList allRestrictions = FDDeliveryManager.getInstance().getDlvRestrictions();
		Set<EnumDlvRestrictionReason> alcoholReasons = 
			new HashSet<EnumDlvRestrictionReason>(EnumDlvRestrictionReason.getAlcoholEnumList());
		List<RestrictionI> restrictions = allRestrictions.getRestrictions(EnumDlvRestrictionCriterion.DELIVERY, alcoholReasons);
		
		for (RestrictionI restriction : restrictions) {
			if(restriction instanceof AlcoholRestriction){
				AlcoholRestriction res = (AlcoholRestriction) restriction;
				if(res.getState() != null && res.getState().equalsIgnoreCase(state)){
					//Restriction defined at state level. 
					return true;
				}
			}
		}
		return false;
	}
}

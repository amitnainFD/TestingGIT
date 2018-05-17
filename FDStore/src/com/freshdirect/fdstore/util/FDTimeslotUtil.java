package com.freshdirect.fdstore.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.freshdirect.delivery.restriction.DlvRestrictionsList;
import com.freshdirect.delivery.restriction.EnumDlvRestrictionCriterion;
import com.freshdirect.delivery.restriction.EnumDlvRestrictionReason;
import com.freshdirect.delivery.restriction.RestrictionI;
import com.freshdirect.fdlogistics.model.FDZoneCutoffInfo;
import com.freshdirect.fdlogistics.model.FDTimeslot;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.framework.util.DateRange;
import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.framework.util.TimeOfDay;

public class FDTimeslotUtil implements Serializable {
	private static final long	serialVersionUID	= -6937768211070595636L;

	private final static Logger LOGGER = Logger.getLogger(FDTimeslotUtil.class);
	
	private final SortedMap<Date, Map<String, List<FDTimeslot>>> timeslotMap = new TreeMap<Date, Map<String, List<FDTimeslot>>>();
	private final Set<Date> holidaySet = new HashSet<Date>();
	
	//this is map of [date --> media path]
	private final SortedMap<String, String> timeslotSpecialMsgsMap = new TreeMap<String, String>();
	
	public SortedMap<String, String> getTimeslotSpecialMsgsMap() {
		return timeslotSpecialMsgsMap;
	}

	private int responseTime; 
	private boolean advanced;
	private String eventPk;
	
	public FDTimeslotUtil( List<FDTimeslot> timeslots, Date startDate, Date endDate, DlvRestrictionsList restrictions, int responseTime, boolean advanced, String eventPk ) {
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(endDate);
		
		Collections.sort( timeslots );
		
		for ( FDTimeslot timeslot : timeslots ) {
			
			List<FDTimeslot> shiftTimeslotList = null;
			Map<String,List<FDTimeslot>> shiftMap = null;
			
			shiftMap = timeslotMap.get(timeslot.getDeliveryDate());
			if(null == shiftMap){
				shiftMap = new HashMap<String, List<FDTimeslot>>();
			}else{
				shiftTimeslotList = shiftMap.get(timeslot.getTimeslotShift());
			}
			
			if(null == shiftTimeslotList || shiftTimeslotList.isEmpty()){
				shiftTimeslotList = new ArrayList<FDTimeslot>();
			}
			shiftTimeslotList.add(timeslot);
			shiftMap.put(timeslot.getTimeslotShift(), shiftTimeslotList);
			timeslotMap.put(timeslot.getDeliveryDate(), shiftMap);
			
		}
		
		Map<String,List<FDTimeslot>> shiftMap = null;
		while ( startCal.before( endCal ) ) {
			shiftMap = timeslotMap.get( startCal.getTime() );
			if(shiftMap!=null){
				for(List<FDTimeslot> list : shiftMap.values()){
					if ( list == null || restrictions.isRestricted( EnumDlvRestrictionCriterion.DELIVERY, EnumDlvRestrictionReason.CLOSED, startCal.getTime() ) ) {
						//timeslotMap.put( startCal.getTime(), new HashMap<String,List<FDTimeslot>>());
						holidaySet.add(startCal.getTime());
					}	
				}		
			}else{
				timeslotMap.put( startCal.getTime(), new HashMap<String,List<FDTimeslot>>());
				if (restrictions.isRestricted( EnumDlvRestrictionCriterion.DELIVERY, EnumDlvRestrictionReason.CLOSED, startCal.getTime() ) ) {
					holidaySet.add(startCal.getTime());
				}	
			}			
			startCal.add( Calendar.DATE, 1 );
		}	
		this.setAdvanced(advanced);
		this.setResponseTime(responseTime);
		this.setEventPk(eventPk);
		this.parseSpecialMsgsProp();
	}
	
	public boolean hasCapacity() {
		for(Iterator<Map<String, List<FDTimeslot>>> itr = timeslotMap.values().iterator();itr.hasNext();){
			Map<String, List<FDTimeslot>> mapCutOff  = itr.next();
			for ( List<FDTimeslot> timeslots : mapCutOff.values() ) {
				for ( FDTimeslot slot : timeslots ) {
					if ( slot.getTotalAvailable() > 0 ) {
						return true;
					}
				}
			}	
		}		
		return false;
	}
	
	public boolean isKosherSlotAvailable( DlvRestrictionsList restrictions ) {
		for ( Date day : timeslotMap.keySet() ) {
			if ( !restrictions.isRestricted( null, EnumDlvRestrictionReason.KOSHER, day ) ) {
				return false;
			}
		}
		return true;
	}
	
	public Date getStartDate() {
		return timeslotMap.firstKey();
	}
	
	public Date getEndDate() {
		return timeslotMap.lastKey();
	}
	
	public Collection<Date> getDays() {
		return timeslotMap.keySet();
	}

	public Collection<Date> getHolidays() {
		return holidaySet;
	}
	
	/**
	 * Returns list of list of FDTimeslot.
	 * @return list of list of FDTimeslot
	 */	
	public Collection<List<FDTimeslot>> getTimeslots() {
		List<List<FDTimeslot>> timeslots = new ArrayList<List<FDTimeslot>>();
		for(Iterator<Map<String, List<FDTimeslot>>> itr = timeslotMap.values().iterator();itr.hasNext();){
			Map<String, List<FDTimeslot>> tempMap = itr.next();
			timeslots.addAll(tempMap.values());
		}
		return timeslots;
	}
	
	public List<FDTimeslot> getTimeslotsFlat() {
		List<FDTimeslot> slots = new ArrayList<FDTimeslot>();
		for(Iterator<Map<String, List<FDTimeslot>>> itr = timeslotMap.values().iterator();itr.hasNext();){
			Map<String, List<FDTimeslot>> mapShift  = itr.next();
			for ( List<FDTimeslot> list : mapShift.values() ) {
				for ( FDTimeslot slot : list ) {
					slots.add( slot );
				}
			}		
		}		
		return slots;
	}
	
	public List<FDTimeslot> getTimeslotsForDate( Date day ) {
		List<FDTimeslot> slots = new ArrayList<FDTimeslot>();
		Map<String, List<FDTimeslot>> mapShift  = timeslotMap.get( day );
		if (mapShift == null) {
			LOGGER.error("Failed to request timeslots for day " + day);
			return Collections.<FDTimeslot> emptyList();
		}

		for ( List<FDTimeslot> list : mapShift.values()) {
				for ( FDTimeslot slot : list ) {
					slots.add( slot );
				}
		}
		
		if ( slots.size() > 0 ) {
			Collections.sort(slots);
			return slots;
		} else {
			return Collections.<FDTimeslot> emptyList();
		}
	}
	
	public Date getMaxCutoffForDate( String zoneCode, Date day ) {
		Date cutOff = null;
		List<FDZoneCutoffInfo> cutoffInfo = null;
		List<Date> cTimes = new ArrayList<Date>();
		try {
			cutoffInfo = FDDeliveryManager.getInstance().getCutofftimeForZone(zoneCode, day);
			
			for(FDZoneCutoffInfo zoneCutoff : cutoffInfo){
				cTimes.add(zoneCutoff.getCutoffTime().getAsDate());
			}
			if(cTimes.size() > 0){
				Collections.sort(cTimes);
				for (Date _cutoff : cTimes) {
					cutOff = _cutoff;
				}
			}
			if (cutOff != null) {
				Calendar requestedDate = Calendar.getInstance();
				requestedDate.setTime(day);
				requestedDate.add(Calendar.DATE, -1);

				Calendar timeDate = Calendar.getInstance();
				timeDate.setTime(cutOff);
				timeDate.set(Calendar.MONTH, requestedDate.get(Calendar.MONTH));
				timeDate.set(Calendar.DATE, requestedDate.get(Calendar.DATE));
				timeDate.set(Calendar.YEAR, requestedDate.get(Calendar.YEAR));
				cutOff = timeDate.getTime();
			}
		} catch (FDResourceException e) {
			e.printStackTrace();
		}

		return cutOff;
	}
	public RestrictionI getHolidayRestrictionForDate( Date day ) {
		
		Calendar begCal = Calendar.getInstance();
		begCal.setTime(day);
		begCal = DateUtil.truncate(begCal);
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(day); 
		endCal.add(Calendar.DATE, 1);
		endCal = DateUtil.truncate(endCal);
		try {
			DlvRestrictionsList restrictions = FDDeliveryManager.getInstance().getDlvRestrictions();
			List<RestrictionI> r = restrictions.getRestrictions(EnumDlvRestrictionReason.CLOSED, new DateRange(begCal.getTime(),endCal.getTime()));
			if ( r != null ) {
				return r.get(0);
			} else {
				return null;
			}
		} catch (FDResourceException e) {			
			e.printStackTrace();
		}
		return null;
	}
	public FDTimeslot getTimeslot( Date day, Date startTime, Date endTime ) {
		
		Map<String, List<FDTimeslot>> shiftMap  = timeslotMap.get( day );
		for ( List<FDTimeslot> lst : shiftMap.values()) {
			if ( lst != null ) {
				for ( FDTimeslot slot : lst ) {
					if ( slot.isMatching( day, startTime, endTime ) ) {
						return slot;
					}			
				}
			}
		}	
		return null;
	}
	
	public int size() {
		return timeslotMap.size();
	}	
	
	public List<FDTimeslot> getTimeslotsForDayAndShift( Date day, String shift ) {
		
		Map<String,List<FDTimeslot>> tempMap = timeslotMap.get(day);
		if(tempMap.isEmpty()){
			return Collections.<FDTimeslot> emptyList();
		}else{
			List<FDTimeslot> lst = tempMap.get( shift );
			if ( lst != null ) {
				return lst;
			} else {
				return Collections.<FDTimeslot> emptyList();
			}
		}
	}
	
	public int getNumDayShiftTimeslots( Date day, String shift ) {
		
		Map<String,List<FDTimeslot>> tempMap= timeslotMap.get(day);
		if(tempMap.isEmpty()){
			return 0;
		}else{
			List<FDTimeslot> lst = tempMap.get( shift );
			if ( lst != null ) {
				return lst.size();
			} else {
				return 0;
			}
		}
	}

	public int getMaxNumShiftTimeslots(String shift) {
		Collection<Date> days = this.getDays();
		int maxTimeslots = 0; 
		for (Date day : days) {
			List<FDTimeslot> timeslots = this.getTimeslotsForDayAndShift(day, shift);
			if(timeslots.size()>=maxTimeslots)
				maxTimeslots = timeslots.size();					
		}
		
		return maxTimeslots;
	}
	
	public boolean isSelectedTimeslot( Date day, String timeslotId ) {
		
		Map<String, List<FDTimeslot>> mapCutOff  = timeslotMap.get( day );
		for ( List<FDTimeslot> lst : mapCutOff.values()) {
			if ( lst != null ) {
				for ( FDTimeslot slot : lst ) {
					if ( slot.getId().equals(timeslotId) ) {
						return true;
					}			
				}
			}
		}	
		return false;
	}
	
	public int getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(int responseTime) {
		this.responseTime = responseTime;
	}
	
	public void removeTimeslots(Date baseDate)
	{
		timeslotMap.remove(baseDate);
	}

	public boolean isAdvanced() {
		return advanced;
	}

	public void setAdvanced(boolean advanced) {
		this.advanced = advanced;
	}
	
	//parse prop and set to timeslotSpecialMsgsMap
	public void parseSpecialMsgsProp() {
		String propStr = FDStoreProperties.getTSSpecialMessaging();
					
		if (propStr !=null && !"".equals(propStr)) {
			//split in to configs (one of each date)
			for (String configStr : propStr.split(";")) {
				//split in to config sections for a specific date
				String _tmpDate = null;
				String _tmpMedia = null;
				
				for (String configSectionStr : configStr.split(":")) {					
					//split in to date and media section
					String[] configSectionKnownStr = configSectionStr.split("=");
					if (configSectionKnownStr.length == 2) {
						if ("d".equalsIgnoreCase(configSectionKnownStr[0])) {
							_tmpDate = configSectionKnownStr[1];
						} else if ("m".equalsIgnoreCase(configSectionKnownStr[0])) {
							//media
							_tmpMedia = "'"+configSectionKnownStr[1]+"'";
						}
					}
					
				} //end split ":"
				if(_tmpDate != null && _tmpMedia != null ) {
					/* date needs to be quoted for going to json */
					timeslotSpecialMsgsMap.put("'"+_tmpDate+"'", _tmpMedia);
				}
			} //end split ";"
		}
	}

	public String getEventPk() {
		return eventPk;
	}

	public void setEventPk(String eventPk) {
		this.eventPk = eventPk;
	}
}

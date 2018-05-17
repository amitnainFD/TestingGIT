package com.freshdirect.fdstore.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.address.ContactAddressModel;
import com.freshdirect.crm.CrmClick2CallModel;
import com.freshdirect.crm.CrmClick2CallTimeModel;
import com.freshdirect.enums.WeekDay;
import com.freshdirect.fdlogistics.model.FDDeliveryZoneInfo;
import com.freshdirect.fdlogistics.model.FDInvalidAddressException;
import com.freshdirect.fdlogistics.model.FDTimeslot;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.framework.util.DateRange;
import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.logistics.analytics.model.TimeslotEvent;
import com.freshdirect.logistics.delivery.model.TimeslotContext;

public class ClickToCallUtil {

	public static boolean isBusinessHour() {

		Calendar calStart=Calendar.getInstance();
		String hourRange=(String)FDStoreProperties.getCustServHoursRange(calStart.get(Calendar.DAY_OF_WEEK));

		String startHour=hourRange.substring(0,hourRange.indexOf(':'));
		String startMinute=hourRange.substring(hourRange.indexOf(':')+1,hourRange.indexOf('-'));
		String endHour=hourRange.substring(hourRange.indexOf('-')+1,hourRange.lastIndexOf(':'));
		String endMinute=hourRange.substring(hourRange.lastIndexOf(':')+1);
		calStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startHour));
		calStart.set(Calendar.MINUTE, Integer.parseInt(startMinute));
		calStart.set(Calendar.SECOND, 0);

		Calendar calEnd=Calendar.getInstance();
		calEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endHour));
		calEnd.set(Calendar.MINUTE, Integer.parseInt(endMinute));
		calEnd.set(Calendar.SECOND, 0);

		Calendar calNow=Calendar.getInstance();

		if ( FDStoreProperties.getClickToCall() && (calNow.equals(calStart) || calNow.after(calStart) ) &&
				(calNow.equals(calEnd) || calNow.before(calEnd)) ) {
			return true;
		}
		return false;
	}

	private static void print(Calendar now) {

        System.out.println(now.get(Calendar.MONTH)+1+"/"+ now.get(Calendar.DAY_OF_MONTH)+"/"+now.get(Calendar.YEAR)+" "+now.get(Calendar.HOUR_OF_DAY)+":"+now.get(Calendar.MINUTE));

	}

	public static void main (String[] a) {

        System.out.println(ClickToCallUtil.isBusinessHour());

	}
	
	public static boolean evaluateClick2CallInfoDisplay(FDUserI user,AddressModel address) throws FDResourceException{
		CrmClick2CallModel click2CallModel = FDCustomerManager.getClick2CallInfo();
//		FDUserI user = getUser();
		boolean displayClick2CallInfo = false;
		if(null != user && click2CallModel.isStatus() && FDStoreProperties.getClickToCall()){			
			Calendar calendar = Calendar.getInstance();
			Date date = new Date();
			calendar.setTime(date);
			int day = calendar.get(Calendar.DAY_OF_WEEK);
			WeekDay weekDayEnum[] = WeekDay.values();
			CrmClick2CallTimeModel click2CallTime[] = click2CallModel.getDays();
			CrmClick2CallTimeModel click2CallDay = click2CallTime[day-1];
			displayClick2CallInfo = checkDisplayDayAndHrs(user,
					click2CallModel, displayClick2CallInfo, calendar, date,
					click2CallDay,address);
			
		}
		return displayClick2CallInfo;
	}

	private static boolean checkDisplayDayAndHrs(FDUserI user,
			CrmClick2CallModel click2CallModel, boolean displayClick2CallInfo,
			Calendar calendar, Date date, CrmClick2CallTimeModel click2CallDay,AddressModel address)
			throws FDResourceException {
		if(click2CallDay.isShow()){
			String startTime = click2CallDay.getStartTime();
			Integer startHour = Integer.parseInt(startTime);
			String endTime = click2CallDay.getEndTime();
			Integer endHour = Integer.parseInt(endTime);
			Integer currentHour = calendar.get(Calendar.HOUR_OF_DAY);
			if(startHour <= currentHour && (currentHour < endHour || endHour == 0)){
				String[] dlvZones = click2CallModel.getDeliveryZones();
				displayClick2CallInfo = checkDeliveryZones(user,
						click2CallModel, displayClick2CallInfo, date,
						dlvZones,address);
			}
			
		}
		return displayClick2CallInfo;
	}

	private static boolean checkDeliveryZones(FDUserI user,
			CrmClick2CallModel click2CallModel, boolean displayClick2CallInfo,
			Date date, String[] dlvZones,AddressModel address) throws FDResourceException {
		if(null !=dlvZones && dlvZones.length > 0){
			if(null == address){
				address = user.getShoppingCart().getDeliveryAddress();
			}
			try {
				if(null == address){
					String addrId = FDCustomerManager.getDefaultShipToAddressPK(user.getIdentity());
					address = FDCustomerManager.getAddress(user.getIdentity(), addrId);
				}
				if(null != address){
					FDDeliveryZoneInfo dlvZoneInfo = FDDeliveryManager.getInstance().getZoneInfo(address, date, user.getHistoricOrderSize(), user.getRegionSvcType(address.getId()));
					if(null != dlvZoneInfo && null !=dlvZoneInfo.getZoneCode()){
						List dlvZonesList =Arrays.asList(dlvZones);
						if(dlvZonesList.contains(dlvZoneInfo.getZoneCode())){
							//Eligibility Check
							displayClick2CallInfo = checkEligibleCustomers(
									user, click2CallModel,
									displayClick2CallInfo, address);
						}
					}
				}
			} catch (FDInvalidAddressException e) {
//							throw new FDResourceException(e);
			}
		}
		return displayClick2CallInfo;
	}

	private static boolean checkEligibleCustomers(FDUserI user,
			CrmClick2CallModel click2CallModel, boolean displayClick2CallInfo,
			AddressModel address) throws FDResourceException {
		String eligibleCustomers = click2CallModel.getEligibleCustomers();
		if(null != eligibleCustomers){
			String[] eligibleCustomer = eligibleCustomers.split(",");
			if(null != eligibleCustomer && eligibleCustomer.length > 0){
				List elgCustList = Arrays.asList(eligibleCustomer);
				if(elgCustList.contains("everyone")){
					displayClick2CallInfo = true;//checkNextDayTimeSlots(click2CallModel,displayClick2CallInfo, address,user);
					
				}else{
					
					if(elgCustList.contains("ct_dp") || elgCustList.contains("ct_ndp")){
						if(user.isChefsTable()){
							if(elgCustList.contains("ct_dp") && (user.isDlvPassActive()||user.isDlvPassPending()) ){
								displayClick2CallInfo = true;
							}
							if(!displayClick2CallInfo){
							if(elgCustList.contains("ct_ndp") && (!user.isDlvPassActive()&& !user.isDlvPassPending()) ){
								displayClick2CallInfo = true;
							}
							}
						}
					}
					if(!displayClick2CallInfo){
						if(elgCustList.contains("nct_dp") || elgCustList.contains("nct_ndp")){
							if(!user.isChefsTable()){
								if(elgCustList.contains("nct_dp") && (user.isDlvPassActive()||user.isDlvPassPending()) ){
									displayClick2CallInfo = true;
								}
								if(!displayClick2CallInfo){
								if(elgCustList.contains("nct_ndp") && (!user.isDlvPassActive()&& !user.isDlvPassPending()) ){
									displayClick2CallInfo = true;
								}
								}
							}
						}
					}
					/*if(displayClick2CallInfo){
						displayClick2CallInfo = checkNextDayTimeSlots(click2CallModel,false, address,user);
					}*/
					
				}
					
			}
			
		}
		return displayClick2CallInfo;
	}

	private static boolean checkNextDayTimeSlots(
			CrmClick2CallModel click2CallModel, boolean displayClick2CallInfo,
			AddressModel address,FDUserI user) throws FDResourceException {
		if(click2CallModel.isNextDayTimeSlot()){
			if(address instanceof ContactAddressModel){
				Calendar begCal = Calendar.getInstance();
				begCal.add(Calendar.DATE, 1);
				begCal = DateUtil.truncate(begCal);
				Calendar endCal = Calendar.getInstance();
				endCal.add(Calendar.DATE, 2);
				endCal = DateUtil.truncate(endCal);
				TimeslotEvent event = null; 
				FDDeliveryZoneInfo zoneInfo = null;
				try{
					zoneInfo = FDDeliveryManager.getInstance().getZoneInfo(address, begCal.getTime(), 
						user.getHistoricOrderSize(), user.getRegionSvcType(address.getId()));
				}catch(FDInvalidAddressException ie){
					throw new FDResourceException(ie);
				}
				List<DateRange> ranges = new java.util.ArrayList<DateRange>();
				ranges.add(new DateRange(begCal.getTime(), endCal.getTime()));
				
				List<FDTimeslot> timeSlots = FDDeliveryManager.getInstance().getTimeslotsForDateRangeAndZone
						(ranges, event, TimeslotLogic.encodeCustomer((ContactAddressModel)address, user), TimeslotLogic.getDefaultOrderContext(user.getFDCustomer().getErpCustomerPK()), TimeslotContext.CHECK_AVAILABLE_TIMESLOTS)
						.getTimeslotList().get(0).getTimeslots();
				
				if(null == timeSlots || timeSlots.size()==0){
					displayClick2CallInfo = true;
				}else{
					boolean isAvailable = false;
					if(user.isChefsTable()){
					for (FDTimeslot timeslot : timeSlots) {
						if(timeslot.getTotalAvailable()>0){
							isAvailable = true;
							break;
						}
					}
					}else{
						for (FDTimeslot timeslot : timeSlots) {
							if(timeslot.getBaseAvailable()>0){
								isAvailable = true;
								break;
							}
						}
					}
					if(!isAvailable){
						displayClick2CallInfo = true;
					}
				}
			}
		}else{
			displayClick2CallInfo = true;
		}
		return displayClick2CallInfo;
	}
	
	public static boolean isNextDayTimeSlotsCheckRequired() throws FDResourceException{
		CrmClick2CallModel click2CallModel = FDCustomerManager.getClick2CallInfo();		
		return FDStoreProperties.getClickToCall() && click2CallModel.isStatus() && click2CallModel.isNextDayTimeSlot();			
	}
	

}

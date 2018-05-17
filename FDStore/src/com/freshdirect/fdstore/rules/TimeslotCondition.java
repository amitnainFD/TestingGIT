package com.freshdirect.fdstore.rules;

import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.framework.util.MathUtil;
import com.freshdirect.framework.util.TimeOfDay;
import com.freshdirect.rules.ConditionI;
import com.freshdirect.rules.RuleRuntimeI;

public class TimeslotCondition implements ConditionI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private String startTime;
    private String endTime;
    private String day;
   // private Double orderMinimum;
    
    
	public TimeslotCondition() {
	}

	public TimeslotCondition(String day) {
		this.day = day;
	}

	@Override
	public boolean evaluate(Object target, RuleRuntimeI ctx) {
		
		FDRuleContextI context = (FDRuleContextI) target;
		
		if(getStartTimeEx()!=null && 
				(getStartTimeEx().before(context.getTimeslot().getDlvStartTime()) || getStartTimeEx().equals(context.getTimeslot().getDlvStartTime()))
				   && getEndTimeEx()!=null && 
				   (getEndTimeEx().after(context.getTimeslot().getDlvEndTime()) || (getEndTimeEx().equals(context.getTimeslot().getDlvEndTime())))
				   && day!=null && day.equalsIgnoreCase(DateUtil.formatDayOfWeek(context.getTimeslot().getDeliveryDate()))) {
			return true;
		} 
		return false;
		
		
	}

	@Override
	public boolean validate() {

		if(day == null || "".equals(day))
			return false;
		return true;
	}

	public String getStartTime() {
		return startTime;
	}
	
	public TimeOfDay getStartTimeEx() {
		return (startTime!=null)?new TimeOfDay(startTime):null;
	}
	
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}
	public TimeOfDay getEndTimeEx() {
		return (endTime!=null)?new TimeOfDay(endTime):null;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getDay() {		
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}
}
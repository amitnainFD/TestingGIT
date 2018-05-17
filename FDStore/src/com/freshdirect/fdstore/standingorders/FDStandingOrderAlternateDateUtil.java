package com.freshdirect.fdstore.standingorders;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.freshdirect.fdstore.FDResourceException;

public class FDStandingOrderAlternateDateUtil {
	

private static final String ERROR_MSG_INVALID_SO_ID = " is invalid SO Id. Please check and enter the correct SO Id.";
private static final String ERROR_MSG_DIFF_ORIG_DLV_DATE_ALT_DLV_DATE = "Difference between original delivery date and alternate delivery date shouldn't be more than 7 days.";
private static final String ERROR_MSG_ALREADY_STANDING_ORDER_ALT_DATE_FOR_DELIVERY_DATE = " There is already a standing order alternate date setup for the given delivery date: ";
private static final String ERROR_MSG_SO_ID_INTEGER = " SO Id is invalid. It should be an integer.";
private static final String ERROR_MSG_ALTERNATE_DELIVERY_DATE_INVALID = " Alternate delivery date is invalid. It shouldn't be a past date.";
private static final String ERROR_MSG_ALTERNATE_DELIVERY_DATE_EMPTY = " Alternate delivery date can't be empty to change the standing order delivery date.";
private static final String ERROR_MSG_ORIGINAL_DELIVERY_DATE_INVALID = " Original delivery date shouldn't be a past date.";
private static final String ERROR_MSG_ORIGINAL_DELIVERY_DATE_EMPTY = " Original delivery date can't be empty.";
private static final String ERROR_MSG_ROW_NUM = " At row num: ";
private static final String ERROR_MSG_SO_ID = " and SO Id: ";
private static final String STRING_COMMA = ", ";
private static final String STRING_HYPHEN = " - ";

public static String buildResponse(List<String> errors) {
		
		StringBuffer buffer = new StringBuffer();
		
		if(null !=errors && errors.size() > 0) {
			buffer.append("<table  valign=\"top\" width=\"480\" cellpadding=\"0\" cellspacing=\"0\">");
			if(errors.size() > 0) {
				buffer.append("<tr>").append("<th   colspan=\"4\" align=\"center\">").append("<U>Errors</U> ").append("</th>").append("</tr>");
				Iterator<String> itr = errors.iterator();
				buffer.append("<tr>");
				buffer.append("<td  colspan=\"4\">");
				while(itr.hasNext()) {
					String exception = itr.next();
					buffer.append(exception).append("<br/>");
				}				
				buffer.append("<br/></td>");
				buffer.append("</tr>");	
			}			
			
			buffer.append("</table>");	
		}
		
		return buffer.toString();
	}

	public static List<String> validate(FDStandingOrderAltDeliveryDate altDate,List<String> errors,Integer rowNum){
	
		if(null !=altDate){
			Date currentDate = Calendar.getInstance().getTime();
			if(null ==altDate.getOrigDate()){
				addError(errors,ERROR_MSG_ORIGINAL_DELIVERY_DATE_EMPTY,rowNum,null);
			} else if(altDate.getOrigDate().before(currentDate)){
				addError(errors,ERROR_MSG_ORIGINAL_DELIVERY_DATE_INVALID,rowNum,altDate.getOrigDateFormatted());
			}
			if(EnumStandingOrderAlternateDeliveryType.ALTERNATE_DELIVERY.getName().equals(altDate.getActionType()) && null == altDate.getAltDate() ){
				addError(errors,ERROR_MSG_ALTERNATE_DELIVERY_DATE_EMPTY,rowNum,null);				
			} else if(null != altDate.getAltDate()){
				if(altDate.getAltDate().before(currentDate)){
					addError(errors,ERROR_MSG_ALTERNATE_DELIVERY_DATE_INVALID,rowNum,altDate.getAltDateFormatted());	
				} else if(null != altDate.getOrigDate() && (7 < altDate.getAltDate().compareTo(altDate.getOrigDate()) || 7 < altDate.getOrigDate().compareTo(altDate.getAltDate()))){
					addError(errors,ERROR_MSG_DIFF_ORIG_DLV_DATE_ALT_DLV_DATE,rowNum,null);
				}
			}
			if(null != altDate.getSoId() && !"".equals(altDate.getSoId())){ 
				if(!StringUtils.isNumeric(altDate.getSoId())){
					addError(errors,ERROR_MSG_SO_ID_INTEGER,rowNum,altDate.getSoId());
				}else{
					boolean isValid = false;
					try {
						isValid = FDStandingOrdersManager.getInstance().isValidSoId(altDate.getSoId());
					} catch (FDResourceException e) {
						
					}
					if(!isValid){
						addError(errors,ERROR_MSG_INVALID_SO_ID,rowNum,altDate.getSoId());
					}
				}
			}
			if(errors.isEmpty()){
				boolean isDuplicate = false;
				try {
					isDuplicate = FDStandingOrdersManager.getInstance().checkIfAlreadyExists(altDate);
				} catch (FDResourceException e) {
					
				}
				if(isDuplicate){
					if(altDate.getSoId() == null || "".equals(altDate.getSoId())){
						addError(errors,ERROR_MSG_ALREADY_STANDING_ORDER_ALT_DATE_FOR_DELIVERY_DATE,rowNum,altDate.getOrigDateFormatted());						
					}else{
						if(null !=rowNum){
							errors.add(ERROR_MSG_ROW_NUM+rowNum+ERROR_MSG_ALREADY_STANDING_ORDER_ALT_DATE_FOR_DELIVERY_DATE+altDate.getOrigDateFormatted()+ERROR_MSG_SO_ID+altDate.getSoId());
						}else{
							errors.add(ERROR_MSG_ALREADY_STANDING_ORDER_ALT_DATE_FOR_DELIVERY_DATE+altDate.getOrigDateFormatted()+ERROR_MSG_SO_ID+altDate.getSoId());
						}
					}
				}
			}	
		}
		return errors;
	}

	//To check if there are any duplicate rows in the list, uploaded through excel spread sheet.
	public static List<String> validate(List<FDStandingOrderAltDeliveryDate> altDates,List<String> errors){
		if(null != altDates){
			Set<FDStandingOrderAltDeliveryDate> altDatesSet = new HashSet<FDStandingOrderAltDeliveryDate>();
			int i =3;
			for (Iterator<FDStandingOrderAltDeliveryDate> iterator = altDates.iterator(); iterator.hasNext();) {
				FDStandingOrderAltDeliveryDate altDeliveryDate = (FDStandingOrderAltDeliveryDate) iterator.next();
				if(altDatesSet.contains(altDeliveryDate)){
					errors.add(ERROR_MSG_ROW_NUM+i+ERROR_MSG_ALREADY_STANDING_ORDER_ALT_DATE_FOR_DELIVERY_DATE);
				}else{
					altDatesSet.add(altDeliveryDate);
				}
				i++;				
			}
		}
		
		return errors;		
	}
	
	
	private static void addError(List<String> errors, String msg, Integer rowNum, Object value){
		StringBuffer buff = new StringBuffer();
		if(null != rowNum){
			buff.append(ERROR_MSG_ROW_NUM);
			buff.append(rowNum);
			buff.append(STRING_COMMA);		
		}
		if(null !=value){
			buff.append(value);
			buff.append(STRING_HYPHEN);
		}
		buff.append(msg);
		errors.add(buff.toString());
	}
}

package com.freshdirect.fdstore.content.browse.sorter;

import java.util.Comparator;

import org.apache.log4j.Logger;

import com.freshdirect.content.nutrition.ErpNutritionType;
import com.freshdirect.fdstore.FDException;
import com.freshdirect.fdstore.FDNutrition;
import com.freshdirect.fdstore.content.FilteringProductItem;
import com.freshdirect.framework.util.log.LoggerFactory;

public class NutritionComparator implements Comparator<FilteringProductItem> {
	private static final Logger LOGGER = LoggerFactory.getInstance( NutritionComparator.class ); 
	
	private ErpNutritionType.Type erpNutritionTypeType;
	
	public NutritionComparator(ErpNutritionType.Type erpNutritionTypeType){
		this.erpNutritionTypeType = erpNutritionTypeType;
	}
	
	@Override
	public int compare(FilteringProductItem o1, FilteringProductItem o2) {
		int compareValue;
		
		if (erpNutritionTypeType == null){
			LOGGER.error("erpNutritionTypeType is null, returning 0");
			compareValue = 0;

		} else {
			Double p1 = getNutritionValue(o1);
			Double p2 = getNutritionValue(o2);
			
			if (p1==null) {
				if (p2==null) {
					compareValue = 0;
				} else {
					compareValue = 1; //null value goes in the back
				}
			} else {
				if (p2==null) {
					compareValue = -1;  //null value goes in the back
				} else {
					compareValue = p1 > p2 ? 1 : (p1 < p2 ? -1 : 0);
					if (erpNutritionTypeType.isGood()){
						compareValue *= -1;
					}
				}
			}
		}
		return compareValue;
	}

	/** based on ContentNodeComparator.getNutritionValue()**/
	private Double getNutritionValue(FilteringProductItem filteringProductItem){
		Double nutritionValue = null;
		try {            
			FDNutrition servingSizeFdNutrition = filteringProductItem.getFdProduct().getNutritionItemByType(ErpNutritionType.getType(ErpNutritionType.SERVING_SIZE));

			if (servingSizeFdNutrition!=null && servingSizeFdNutrition.getValue() != 0){ //missing serving size will go to the bottom of the list
				FDNutrition fdNutrition = filteringProductItem.getFdProduct().getNutritionItemByType(erpNutritionTypeType);
				
				if (fdNutrition!=null){
					nutritionValue = fdNutrition.getValue() * 1000;
					if (nutritionValue < 0) {
						nutritionValue = 0.001;
					}
				}
			}
		} catch (FDException e) {
        	LOGGER.error(e);
        }
		return nutritionValue;
	}
}

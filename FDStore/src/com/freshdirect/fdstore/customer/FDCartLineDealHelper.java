package com.freshdirect.fdstore.customer;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.content.PriceCalculator;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.ProductReference;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDCartLineDealHelper {

	public enum DealType {
		NONE, REGULAR, TIERED, GROUPED
	}
	
	private static final Logger LOGGER = LoggerFactory.getInstance(FDCartLineDealHelper.class);
	private FDCartLineI cartLine;
	private PriceCalculator priceCalculator;
	
	private int regularDealPercentage;
	private int tieredDealPercentage;
	private int groupedDealPercentage;

	private DealType usedDealType = DealType.NONE;
	private int usedDealPercentage;
	
	
	public FDCartLineDealHelper(FDCartLineI cartLine) {
		this.cartLine = cartLine;
	}
	
	/**
	 * Queries deal percentages and determines used deal type and percentage
	 **/
	public boolean init(){
		try {
				
			ProductReference productRef = cartLine.getProductRef();
			if (productRef==null){
				return false;
			}
			
			ProductModel product = productRef.lookupProductModel();
			priceCalculator = product.getPriceCalculator();
	
			double unitPrice = cartLine.getPrice() / cartLine.getQuantity();
	
			regularDealPercentage = priceCalculator.getDealPercentage();
			tieredDealPercentage = priceCalculator.getTieredDealPercentage();
			groupedDealPercentage = (int) Math.round(getPercentageToWasPrice(priceCalculator.getGroupPrice()));
	
			if (cartLine.getGroupQuantity() == 0) {
	
				if (regularDealPercentage > 0 && tieredDealPercentage <= 0) {
					usedDealType = DealType.REGULAR;
					usedDealPercentage = regularDealPercentage;
	
				} else if (regularDealPercentage <= 0 && tieredDealPercentage > 0) {
					usedDealType = DealType.TIERED;
					usedDealPercentage = tieredDealPercentage;
	
				} else if (regularDealPercentage > 0 && tieredDealPercentage > 0) {
					double usedRealDealPercentage = getPercentageToWasPrice(unitPrice);
	
					if (Math.abs(tieredDealPercentage - usedRealDealPercentage) < Math.abs(regularDealPercentage - usedRealDealPercentage)) {
						usedDealType = DealType.TIERED;
						usedDealPercentage = tieredDealPercentage;
	
					} else {
						usedDealType = DealType.REGULAR;
						usedDealPercentage = regularDealPercentage;
					}
				}
	
			} else {
				usedDealType = DealType.GROUPED;
				groupedDealPercentage = (int) Math.round(getPercentageToWasPrice(unitPrice));
				usedDealPercentage = groupedDealPercentage;
			}
			
			return true;
		
		} catch (NullPointerException e){
			LOGGER.info("FDCartLineDealHelper.init() failed due to NullPointerException: " + e);
			return false;
		}
	}

	public DealType getBestDealType(){
		if (groupedDealPercentage>0 || tieredDealPercentage>0 || groupedDealPercentage>0) {
			return (groupedDealPercentage >= tieredDealPercentage) ? 
					((groupedDealPercentage >= regularDealPercentage) ? DealType.GROUPED : DealType.REGULAR) : 
					((tieredDealPercentage > regularDealPercentage) ? DealType.TIERED : DealType.REGULAR);
		} else {
			return DealType.NONE;
		}
	}
		
		

	public DealType getUsedDealType(){
		return usedDealType;
	}
	
	public int getUsedDealPercentage(){
		return usedDealPercentage;
	}
		
	public int getRegularDealPercentage() {
		return regularDealPercentage;
	}

	public int getTieredDealPercentage() {
		return tieredDealPercentage;
	}

	public int getGroupedDealPercentage() {
		return groupedDealPercentage;
	}

	private double getPercentageToWasPrice(double price) {
		if (price==0){
			return 0;
		
		} else {
			double wasPrice = priceCalculator.getWasPrice();
			return (wasPrice == 0) ? 0 : (1 - price / wasPrice) * 100;
		}
	}
}

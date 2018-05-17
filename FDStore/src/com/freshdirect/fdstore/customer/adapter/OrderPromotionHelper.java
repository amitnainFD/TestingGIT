package com.freshdirect.fdstore.customer.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentNodeModelUtil;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.promotion.EnumDCPDContentType;
import com.freshdirect.framework.util.MathUtil;

public class OrderPromotionHelper {

	/**
	 * @return applicable discount
	 */
	public static double getApplicableSignupAmount(FDCartModel cart, double amount, double maxAmountPerSku) {
		double promotionValue = 0.0;

		Map<String, Double> appliedValue = new HashMap<String, Double>();

		for (final FDCartLineI line : cart.getOrderLines()) {
			if (line.lookupFDProduct().isQualifiedForPromotions()) {
				double applicablePromotion = 0.0;
				if (promotionValue < amount) {
					double lineValue = line.getPrice();
					if (lineValue < maxAmountPerSku) {
						applicablePromotion = lineValue;
					} else {
						applicablePromotion = maxAmountPerSku;
					}
					if ((amount - promotionValue) < applicablePromotion) {
						applicablePromotion = amount - promotionValue;
					}
				}

				//
				// only apply up to maxAmountPerSku
				//
				if (!appliedValue.containsKey(line.getSkuCode())) {
					appliedValue.put(line.getSkuCode(), new Double(applicablePromotion));
				} else {
					double previousAppliedPerSku = ((Double) appliedValue.get(line.getSkuCode())).doubleValue();
					applicablePromotion = Math.min(applicablePromotion, (maxAmountPerSku - previousAppliedPerSku));
					appliedValue.put(line.getSkuCode(), new Double(previousAppliedPerSku + applicablePromotion));
				}

				promotionValue += applicablePromotion;
			}
		}

		return promotionValue;
	}
	/**
	 * 
	 * @param eligibleCartLines
	 * @param percentOff
	 * @return
	 */
	public static double getApplicableCategoryDiscount(List<FDCartLineI> eligibleCartLines, double percentOff) { 
		double promotionValue = 0.0;

		
		for (final FDCartLineI line : eligibleCartLines) {
			//Calculate the line discount value.
			double lineValue = MathUtil.roundDecimal(line.getPrice() * percentOff);
			promotionValue += lineValue;


		}
		return promotionValue;
	}

	
	public static boolean evaluateProductForDCPDPromo(ProductModel model, Set<ContentKey> contentKeys) {
		return evaluateProductForDCPDPromoWithRecCategory(model, contentKeys, false);
	}
	
	public static boolean evaluateProductForDCPDPromoWithRecCategory(ProductModel model, Set<ContentKey> contentKeys, boolean loopEnabled) {
		Set<CategoryModel> virtualCats = null;
		//ProductModel model = cartLine.getProductRef().lookupProduct();
		/*
		 * Load all parents of this cartline product if either eligible
		 * department set or category set is not empty.
		 * 
		 */
		ContentKey cKey = model.getContentKey();
		Set<ContentKey> parentKeys = ContentNodeModelUtil.getAllParentKeys(cKey);
		/*
		 * The reason for adding the productModel's parent node to the parent set
		 * is when a product has its parent category set at runtime.
		 */ 
		//parentKeys.add(model.getParentNode().getContentKey());
		//Handling Products in Eligible Departments/Categories.
		if(CollectionUtils.containsAny(contentKeys, parentKeys)){
			return true;
		}else{			
			//Check for virtual category.
			if(virtualCats == null){
				//Load the first time only within this method.
				virtualCats = ContentNodeModelUtil.findVirtualCategories(contentKeys, loopEnabled);
			}
			//Handling Products in Eligible Virtual Categories.
			if(virtualCats.size() > 0 && ContentNodeModelUtil.isProductInVirtualCategories(virtualCats, model)){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isRecipeEligible(String recipeSourceId , Set<ContentKey> contentKeys) {
		ContentKey rkey = ContentNodeModelUtil.getContentKey(EnumDCPDContentType.RECIPE.getName(), recipeSourceId); 
		if(rkey != null && contentKeys.contains(rkey)){
			//Line item is eligible for the recipe-id-level discount.
			return true;
		}
		return false;
	}
	
	
}

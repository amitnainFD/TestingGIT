package com.freshdirect.fdstore.promotion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.DCPDPromoProductCache;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.adapter.OrderPromotionHelper;

public class CartStrategy extends DCPDLineItemStrategy implements PromotionStrategyI {
	private static final long serialVersionUID = -3120395698927259060L;

	// FIXME: FD only content keys!
	private final static String[] DRY_GOODS = { "gro", "spe" };
	private boolean needDryGoods;	
	private int minSkuQuantity;
	//Will contain only SKU and BRAND types.
	private Map<EnumDCPDContentType, Set<String>> dcpdData = new HashMap<EnumDCPDContentType, Set<String>>();
	private Set<String> combinationSku = new HashSet<String>();
	private Double totalDcpdSubtotal;
	private Double cartDcpdSubtotal = 0.0;
	private FDMinDCPDTotalPromoData minDcpdTotalPromoData = new FDMinDCPDTotalPromoData();
	

	/**
	 * Interface {@link PromotionStrategyI}
	 * Called from {@link CompositeStrategy} and from {@link Promotion}
	 * 
	 * @return PromotionStrategyI value
	 */
	public Double getTotalDcpdSubtotal() {
		return totalDcpdSubtotal;
	}

	public void setTotalDcpdSubtotal(Double totalDcpdSubtotal) {
		this.totalDcpdSubtotal = totalDcpdSubtotal;
	}

	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {
		final Set<ContentKey> contentKeys = getContentKeys();
		
		int qualifiedSku=0;
		int qualifiedCombinationSku = 0;
		int allowORdeny = PromotionStrategyI.RESET;
		FDCartModel cart = context.getShoppingCart();
		int nonQualifiedSku = 0;
		if(null != cart){
			try {
				if (needDryGoods && !cart.hasItemsFrom(DRY_GOODS)) {
					return PromotionStrategyI.DENY;
				}
				if(!contentKeys.isEmpty() || !dcpdData.isEmpty()){
					List<FDCartLineI> orderLines = cart.getOrderLines();
					if(null != orderLines && !orderLines.isEmpty()){
						
						for (final FDCartLineI cartLine : orderLines) {
							if(contentKeys.size() > 0 || null !=dcpdData.get(EnumDCPDContentType.BRAND))
								allowORdeny = evaluate(cartLine, promotionCode, context);
							if(PromotionStrategyI.ALLOW != allowORdeny){
								Set<String> skuSet =dcpdData.get(EnumDCPDContentType.SKU);
								if(null!=skuSet && combinationSku.size()>0)
								skuSet.removeAll(combinationSku);
								if(null != skuSet && skuSet.contains(cartLine.getSkuCode())){
									qualifiedSku += cartLine.getQuantity();
								}else if(null != combinationSku && combinationSku.contains(cartLine.getSkuCode())){
									qualifiedCombinationSku += cartLine.getQuantity();
								}else {
									nonQualifiedSku++;
								}
							}
							else{
								break;
							}
							
							if(combinationSku.size() > 0){
							if(qualifiedSku > 0 && qualifiedCombinationSku > 0 && (qualifiedSku + qualifiedCombinationSku) >= minSkuQuantity) {
								allowORdeny = PromotionStrategyI.ALLOW;
								break;
								}
							}
							else{
								if(qualifiedSku > 0 && qualifiedSku > minSkuQuantity){
									allowORdeny = PromotionStrategyI.ALLOW;
									break;
								}
							}
						}
						if(nonQualifiedSku == orderLines.size() || (qualifiedSku+qualifiedCombinationSku) < minSkuQuantity)
							allowORdeny = PromotionStrategyI.DENY; 
					}else{
						//if the cart is empty, deny the promotion
						allowORdeny = PromotionStrategyI.DENY;
					}
					if(combinationSku.size() > 0 && (qualifiedSku ==0 || qualifiedCombinationSku == 0)){
						allowORdeny = PromotionStrategyI.DENY;
					}
				}
			} catch (FDResourceException e) {
				throw new FDRuntimeException(e);
			}
		}
		if(PromotionStrategyI.RESET==allowORdeny )
			//At this point there are no cart eligiblity set.
			return PromotionStrategyI.ALLOW;
		
		return allowORdeny;
	}
	


	/**
	 * Interface {@link LineItemStrategyI}
	 * Overrides {@link DCPDLineItemStrategy}
	 * 
	 * @return PromotionStrategyI value
	 */
	@Override
	public int evaluate(FDCartLineI lineItem, String promotionCode,
			PromotionContextI context) {
		final Set<ContentKey> contentKeys = getContentKeys();
		boolean eligible = false;
		ProductModel model = lineItem.getProductRef().lookupProductModel();
		String productId = null !=model ?model.getContentKey().getId():"";
		DCPDPromoProductCache dcpdCache = context.getUser().getDCPDPromoProductCache();
		//Check if the line item product is already evaluated.
		if(dcpdCache.isEvaluated(productId, promotionCode)){
			eligible = dcpdCache.isEligible(productId, promotionCode);
		}else{
			if(contentKeys.size() > 0){
				//Check if the line item is eligible for a category or department discount.
				eligible = OrderPromotionHelper.evaluateProductForDCPDPromo(model, contentKeys);
				//Set Eligiblity info to user session.
				dcpdCache.setPromoProductInfo(productId, promotionCode, eligible);
			}
		}
		if(!eligible){
			Set<String> brandSet =dcpdData.get(EnumDCPDContentType.BRAND);
			if(!eligible && brandSet != null && (lineItem.hasBrandName(brandSet))){
				eligible = true;
			}
			
		}
		if(eligible){
			return PromotionStrategyI.ALLOW;
		}		
		return PromotionStrategyI.DENY;
		
	}

	@Override
	public int getPrecedence() {
		return 0;
	}	

	public boolean isNeedDryGoods() {
		return needDryGoods;
	}

	public void setNeedDryGoods(boolean needDryGoods) {
		this.needDryGoods = needDryGoods;
	}	

	public Map<EnumDCPDContentType, Set<String>> getDcpdData() {
		return dcpdData;
	}

	public void setDcpdData(Map<EnumDCPDContentType, Set<String>> dcpdData) {
		this.dcpdData = dcpdData;
	}

	public int getMinSkuQuantity() {
		return minSkuQuantity;
	}

	public void setMinSkuQuantity(int minSkuQuantity) {
		this.minSkuQuantity = minSkuQuantity;
	}

	public Double getCartDcpdSubtotal() {
		return cartDcpdSubtotal;
	}

	public void setCartDcpdSubtotal(Double cartDcpdSubtotal) {
		this.cartDcpdSubtotal = cartDcpdSubtotal;
	}

		
	public Set<String> getCombinationSku() {
		return combinationSku;
	}

	public void setCombinedSku(Set<String> combinationSku) {
		this.combinationSku = combinationSku;
	}

	public FDMinDCPDTotalPromoData getMinDcpdTotalPromoData() {
		return minDcpdTotalPromoData;
	}

	public int evaluate(String promotionCode, PromotionContextI context, boolean dcpdMinSubtotalCheck) {
		cartDcpdSubtotal=0.0;
		final Set<ContentKey> contentKeys = getContentKeys();
		int allowORdeny = PromotionStrategyI.RESET;
		DCPDPromoProductCache dcpdCache = context.getUser().getDCPDPromoProductCache();
		if(!dcpdMinSubtotalCheck){
			return evaluate(promotionCode, context);
		}else{
			allowORdeny = evaluate(promotionCode, context);
			if(PromotionStrategyI.ALLOW == allowORdeny){
				FDCartModel cart = context.getShoppingCart();
				List<FDCartLineI> orderLines = cart.getOrderLines();
				if(null != orderLines && !orderLines.isEmpty()){
				for (Iterator<FDCartLineI> iterator = orderLines.iterator(); iterator.hasNext();) {
					FDCartLineI cartLine = iterator.next();
					if(contentKeys.size() > 0 || null !=dcpdData.get(EnumDCPDContentType.BRAND) || null !=dcpdData.get(EnumDCPDContentType.SKU)){
						int lAllowORdeny = evaluate(cartLine, promotionCode, context);
						if(PromotionStrategyI.ALLOW == lAllowORdeny){
							cartDcpdSubtotal = cartDcpdSubtotal+cartLine.getPrice();
							ProductModel model = cartLine.getProductRef().lookupProductModel();
							String productId = null !=model ?model.getContentKey().getId():"";
							minDcpdTotalPromoData.getDcpdCartLines().add(cartLine);
							/*if(dcpdCache.isEligible(productId, promotionCode)){
								minDcpdTotalPromoModel.setContentKey((ContentKey)contentKeys.toArray()[0]);
							}else{
								minDcpdTotalPromoModel.setBrandNames(getBrands());
							}*/
						}else{
							Set<String> skuSet =dcpdData.get(EnumDCPDContentType.SKU);
							if(null != skuSet && skuSet.contains(cartLine.getSkuCode())){
								cartDcpdSubtotal = cartDcpdSubtotal+cartLine.getPrice();
								minDcpdTotalPromoData.getDcpdCartLines().add(cartLine);
							}
						}
						if(cartDcpdSubtotal >= totalDcpdSubtotal){
							allowORdeny = PromotionStrategyI.ALLOW;
							//cartDcpdOrderLines.clear();//No need to store the matched orderlines if the promotion is applied
							break;
						}
						else{
							allowORdeny = PromotionStrategyI.DENY;
						}
					}
						
					}
				}
				/*if(cartDcpdSubtotal < totalDcpdSubtotal){
					//Set the balance required for messaging
					double balanceRequired = totalDcpdSubtotal - cartDcpdSubtotal;
					//TODO: construct required UI message and set in Context.
					
				}*/
			}
			
		}
		return allowORdeny;
	}
	@Override
	public String toString() {
		return "CartItemStrategy [contentKeys="
				+ rawContentKeys + ", needDryGoods=" + needDryGoods
				+ ", minSkuQuantity=" + minSkuQuantity + ", dcpdData="
				+ dcpdData.toString() + "]";
	}

}

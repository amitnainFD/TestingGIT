package com.freshdirect.fdstore.promotion;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartModel;

public class SampleStrategy implements PromotionStrategyI {

	// FIXME: FD only content IDs
	private final static String[] DRY_GOODS = { "gro", "spe" };

	private Integer orderCount = null;
	private boolean needDryGoods = false;
	private String[] needItemsFrom = null;
	private String excludeSkuPrefix = null;

	private Set<String> needBrands = null;
	private Set<String> excludeBrands = null;

	public String getExcludeSkuPrefix() {
		return excludeSkuPrefix;
	}

	public void setExcludeSkuPrefix(String string) {
		excludeSkuPrefix = string;
	}

	public String[] getNeedItemsFrom() {
		return needItemsFrom;
	}

	public void setNeedItemsFrom(String[] strings) {
		needItemsFrom = strings;
	}

	public Integer getOrderCount() {
		return orderCount;
	}

	public void setOrderCount(Integer integer) {
		orderCount = integer;
	}

	public void setNeedBrands(String[] brands) {
		this.needBrands = new HashSet<String>();
		for (int i = 0; i < brands.length; i++) {
			this.needBrands.add(brands[i]);
		}
	}

	public void setExcludeBrands(String[] brands) {
		this.excludeBrands = new HashSet<String>();
		for (int i = 0; i < brands.length; i++) {
			this.excludeBrands.add(brands[i]);
		}
	}

	public boolean isNeedDryGoods() {
		return needDryGoods;
	}

	public void setNeedDryGoods(boolean b) {
		needDryGoods = b;
	}

	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {

		if (orderCount != null && context.getAdjustedValidOrderCount() != orderCount.intValue()) {
			return DENY;
		}

		FDCartModel cart = context.getShoppingCart();

		try {
			if (needDryGoods && !cart.hasItemsFrom(DRY_GOODS)) {
				return DENY;
			}

			if (needItemsFrom != null && !cart.hasItemsFrom(needItemsFrom)) {
				return DENY;
			}

			if (excludeSkuPrefix != null && cartHasSku(cart, excludeSkuPrefix)) {
				return DENY;
			}

			if (excludeBrands != null && cart.hasBrandName(excludeBrands)) {
				return DENY;
			}

			if (needBrands != null && !cart.hasBrandName(needBrands)) {
				return DENY;
			}

			return ALLOW;

		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
	}

	private boolean cartHasSku(FDCartModel cart, String skuPrefix) {
		for (FDCartLineI line : cart.getOrderLines()) {
			if (line.getSkuCode().startsWith(skuPrefix)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getPrecedence() {
		return 1000;
	}

	public String toString() {
		return "SampleStrategy[...]";
	}
	
	@Override
	public boolean isStoreRequired() {
		return false;
	}
}

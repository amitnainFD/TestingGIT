package com.freshdirect.fdstore.ewallet;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDSalesUnit;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.framework.util.log.LoggerFactory;

public class EwalletUtil {
	
	private final static Category LOGGER = LoggerFactory
			.getInstance(EwalletUtil.class);
	
	/**
	 * @param user
	 * @param ewalletRequestData
	 * @param imageContext
	 */
	public static void prepareShoppingCartItems(final FDUserI user,
			EwalletRequestData ewalletRequestData, String imageContext) {

		FDCartModel fdCart = user.getShoppingCart();

		List<FDCartLineI> fdCartLines = fdCart.getOrderLines();

		double totalPrice = 0;

		// Will store all the Cart Items
		StringBuffer cartItems = new StringBuffer();
		String subTotalTag = "";
		
		for (FDCartLineI cartLine : fdCartLines) {
			
			StringBuffer cartItem = new StringBuffer();
			ProductModel productNode = cartLine.lookupProduct();

			String productDesc = StringEscapeUtils.unescapeXml(cartLine.getDescription());
			productDesc = StringEscapeUtils.escapeXml(productDesc);

			if (productDesc != null && productDesc.length() >= 70) {
				productDesc = productDesc.substring(0, 70);
				productDesc += "...";
			}

        	int semi = productDesc.lastIndexOf(';');
        	int amp = productDesc.lastIndexOf('&');
        	if (semi < amp) {
        		productDesc = productDesc.substring(0, amp);
        		productDesc += "...";
        	}
        	
			productDesc = productDesc + "(" + cartLine.getUnitPrice() + ")";

			String saleUnitCode = cartLine.getConfiguration().getSalesUnit();

			FDSalesUnit[] salesUnits = cartLine.lookupFDProduct()
					.getSalesUnits();

			String saleUnitDescr = "";
			for (FDSalesUnit saleUnit : salesUnits) {
				if (saleUnit.getName().equals(saleUnitCode)) {
					saleUnitDescr = saleUnit.getDescription();
					break;
				}
			}
			String quantity = "0";
			try {
				if (saleUnitCode.equalsIgnoreCase("EA") || saleUnitCode.equalsIgnoreCase("CS") || saleUnitCode.equalsIgnoreCase("BNC")) {
					quantity = ""+ (new Double(cartLine.getQuantity()).longValue());
				} else {
					String qtyStr = "" + cartLine.getQuantity();
					String fractionPart = qtyStr.substring(qtyStr.indexOf(".") + 1);
					if (fractionPart != null && fractionPart.length() > 0) {
						if (Integer.parseInt(fractionPart) > 0) {
//							productDesc = productDesc + " (Qty: " + qtyStr+ ")"; // Causing issue in PROD because of 100 max Limit of Product Description from MP
							quantity = "0";
						} else {
							if (saleUnitDescr != null&& saleUnitDescr.length() > 0) {
//								productDesc = productDesc + " (Qty: "+ saleUnitDescr + ")"; // Causing issue in PROD because of 100 max Limit of Product Description from MP
								quantity = "0";
							} else {
								quantity = ""+ (new Double(cartLine.getQuantity()).longValue());
							}
						}
					}
				}
			} catch (Exception e) {
				LOGGER.error("Error while create Shopping Cart for EWallet service provider",e);
			}

			cartItem.append("<ShoppingCartItem>");
			cartItem.append("<Description>"+ productDesc+ "</Description>");
			cartItem.append("<Quantity>" + quantity + "</Quantity>");
			cartItem.append("<Value>"
					+ (new Double(cartLine.getPrice() * 100)).longValue()
					+ "</Value>");
			cartItem.append("<ImageURL>" + imageContext
					+ StringEscapeUtils.escapeXml(productNode.getProdImage().getPathWithPublishId())
					+ "</ImageURL>");
			cartItem.append("</ShoppingCartItem>");
			cartItems.append(cartItem);
			totalPrice += cartLine.getPrice();
		}
		subTotalTag = "<Subtotal>" + new Double(totalPrice * 100).longValue()
				+ "</Subtotal>";
		ewalletRequestData.setShoppingCartItems(subTotalTag
				+ cartItems.toString());
	}
}

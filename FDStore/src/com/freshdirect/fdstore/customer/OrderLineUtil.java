package com.freshdirect.fdstore.customer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.pricing.ZoneInfo;
import com.freshdirect.fdstore.FDCachedFactory;
import com.freshdirect.fdstore.FDConfigurableI;
import com.freshdirect.fdstore.FDProduct;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSalesUnit;
import com.freshdirect.fdstore.FDSku;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.FDVariation;
import com.freshdirect.fdstore.FDVariationOption;
import com.freshdirect.fdstore.content.ComponentGroupModel;
import com.freshdirect.fdstore.content.ConfiguredProduct;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.DepartmentModel;
import com.freshdirect.fdstore.content.DomainValue;
import com.freshdirect.fdstore.content.EnumProductLayout;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.ProductReference;
import com.freshdirect.fdstore.content.ProductReferenceImpl;
import com.freshdirect.fdstore.content.Recipe;
import com.freshdirect.fdstore.content.SkuModel;
import com.freshdirect.fdstore.lists.FDCustomerListItem;
import com.freshdirect.fdstore.lists.FDCustomerProductList;
import com.freshdirect.fdstore.lists.FDCustomerProductListLineItem;

public class OrderLineUtil {
	
    private OrderLineUtil() {
	}
	

	/**
	 * CCL
	 * brings a list of old items up to date, throwing away anything that can't be updated
	 */
	public static List<FDCustomerListItem> updateCCLItems(List<FDCustomerListItem> listItems) throws FDResourceException {
		assert (listItems != null);
		
		List<FDCustomerListItem> validItems = new ArrayList<FDCustomerListItem>(listItems.size());

		for (Iterator<FDCustomerListItem> i = listItems.iterator(); i.hasNext();) {
			FDCustomerProductListLineItem pl = (FDCustomerProductListLineItem)i.next();
			FDProductSelectionI ps;
			try {
				ps = pl.convertToSelection();
			} catch (FDSkuNotFoundException e1) {
	            //LOGGER.warn("No sku found for " + pl);
				continue;
			} catch (FDResourceException e1) {
				//LOGGER.warn("Resource exception (" + pl + ')');
				continue;
			} 
			try {
				OrderLineUtil.cleanup(ps);
			} catch (FDInvalidConfigurationException.Unavailable e) {
				//LOGGER.warn("Unavailable item found: " + e.getMessage() + '(' + pl + ')');
				// OK, let it go
			} catch (FDInvalidConfigurationException.InvalidSalesUnit e) {
				//LOGGER.warn("Configuration exception: " + e.getMessage() + '(' + pl + ')');
				// OK, let it go
			} catch (FDInvalidConfigurationException.MissingVariation e) {
				//LOGGER.warn("Missing variation exception: " + e.getMessage() + '(' + pl + ')');
				// OK, let it go
			} catch (FDInvalidConfigurationException.InvalidOption e) {
				//LOGGER.warn("Invalid option exeption: " + e.getMessage() + '(' + pl + ')');
				// OK, let it go
			} catch (FDInvalidConfigurationException e) {	
				//LOGGER.warn("Configuration exception: " + e.getMessage() + '(' + pl + ')');
				continue;
			}
			validItems.add(pl);
		}
		return validItems;
	}


	/**
	 * brings a list of old items up to date, throwing away anything that can't be updated
	 */
	public static List<FDCartLineI> update(List<FDCartLineI> productSelections, boolean removeUnavailable) throws FDResourceException {

		List<FDCartLineI> validSelections = new ArrayList<FDCartLineI>(productSelections.size());

		for ( FDCartLineI ps : productSelections ) {
			try {
				OrderLineUtil.cleanup(ps);
				validSelections.add(ps);
			} catch (FDInvalidConfigurationException.Unavailable ue) {
			    if (removeUnavailable) continue;
			    validSelections.add(ps);
			} catch (FDInvalidConfigurationException e) {				
				continue;
			}
		}

		return validSelections;
	}
	
	public static List<FDCartLineI> cleanAndRemoveDuplicateProductSelections(List<FDCartLineI> productSelections, boolean removeUnavailable) throws FDResourceException {
		List<FDCartLineI> cleanList = update(productSelections,removeUnavailable);
		return removeDuplicateProductSelections(cleanList);
	}
	
	//APPDEV4057
	public static List<FDCartLineI> cleanProductSelections(List<FDCartLineI> productSelections, boolean removeUnavailable) throws FDResourceException {
		return update(productSelections,removeUnavailable);
	}
	

	public static List<FDCartLineI> removeDuplicateProductSelections(List<FDCartLineI> productSelections) throws FDResourceException {
		List<FDCartLineI> cleanList = new ArrayList<FDCartLineI>(productSelections);
		for (int i = 0; i < cleanList.size() - 1; i++) {
			if (OrderLineUtil.isSameConfiguration(cleanList.get(i), cleanList.get(i + 1))) {
				cleanList.remove(i + 1);
				i--;
			}
		}
		return cleanList;
	}

	public static List<FDProductSelectionI> getValidProductSelectionsFromCCLItems(List<FDCustomerListItem> cclItems) throws FDResourceException {
		List<FDProductSelectionI> productSelections = new ArrayList<FDProductSelectionI>();
		for (Iterator<FDCustomerListItem> it = cclItems.iterator(); it.hasNext();) {
			FDCustomerProductListLineItem pl = (FDCustomerProductListLineItem)it.next(); 
			try {
				FDProductSelectionI item = pl.convertToSelection();
				productSelections.add( item );
			} catch (FDSkuNotFoundException e) {
				// Invalid selections are omitted
				continue;
			} catch (FDResourceException e) {
				throw e;
			}
		}
		return productSelections;
	}

	public static boolean isValidCustomerList(List<FDCustomerListItem> cclItems) throws FDResourceException {
		for ( FDCustomerListItem item : cclItems ) {
			if ( item instanceof FDCustomerProductListLineItem ) {
				FDCustomerProductListLineItem pl = (FDCustomerProductListLineItem)item; 
				try {
					pl.convertToSelection();
				} catch (FDSkuNotFoundException e) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
	
	public static void describe(FDProductSelectionI theProduct) throws FDInvalidConfigurationException {
		ProductModel prodNode = theProduct.lookupProduct();
		if (prodNode == null) {
			throw new FDInvalidConfigurationException.Unavailable("Can't find product in store for selection " + theProduct.getProductRef());
		}
		
		theProduct.setDescription(prodNode.getFullName());
		
		String recipeId = theProduct.getRecipeSourceId();
		Recipe recipe = recipeId==null ? null : (Recipe) ContentFactory.getInstance().getContentNode(recipeId);
		String externalGroup = theProduct.getExternalGroup();

		if (externalGroup!=null){
			theProduct.setDepartmentDesc(createExternalDescription(externalGroup, theProduct.getExternalSource()));
		} else if (recipe!=null) {
			theProduct.setDepartmentDesc("Recipe: " + recipe.getName());
		} else {
			DepartmentModel dept = prodNode.getDepartment();
			theProduct.setDepartmentDesc(dept.getFullName());
		}
		
		try {
			theProduct.setConfigurationDesc(createConfigurationDescription(theProduct));
		} catch (FDSkuNotFoundException e) {
			throw new FDInvalidConfigurationException.Unavailable(e,"Sku not found");
		} catch (FDResourceException e) {
		    throw new FDInvalidConfigurationException.Unavailable(e,"Material not found");
        }

	}

	public static String createExternalDescription(String externalGroup, String externalSource){
		if (externalSource!=null && !"".equals(externalSource)){
			return externalGroup +  " | " + externalSource;
		} else {
			return externalGroup;
		}
	}
	
	private static String createConfigurationDescription(FDProductSelectionI theProduct) throws FDSkuNotFoundException, FDResourceException {

		FDProduct product = theProduct.lookupFDProduct();
		ProductModel prodNode = theProduct.lookupProduct();
		SkuModel skuNode = prodNode.getSku(theProduct.getSkuCode());

		StringBuffer confDescr = new StringBuffer();

		//
		// add sales unit description
		//
		if (product == null) {
			throw new FDSkuNotFoundException("No current product found for " + theProduct.getSku());
		}
		FDSalesUnit unit = product.getSalesUnit(theProduct.getConfiguration().getSalesUnit());
		String salesUnitDescr = unit.getDescription();

		// clean sales unit description
		if (salesUnitDescr != null) {
			if (salesUnitDescr.indexOf("(") > -1) {
				salesUnitDescr = salesUnitDescr.substring(0, salesUnitDescr.indexOf("("));
			}
			salesUnitDescr = salesUnitDescr.trim();
			// empty descriptions, "nm" and "ea" should be ignored
			if ((!"".equals(salesUnitDescr))
				&& (!"nm".equalsIgnoreCase(salesUnitDescr))
				&& (!"ea".equalsIgnoreCase(salesUnitDescr))) {
				if (!prodNode.getSellBySalesunit().equals("SALES_UNIT")) {
					confDescr.append(salesUnitDescr);
				} else if ((prodNode.getPrimarySkus().size() == 1)
					&& (prodNode.getVariationMatrix().isEmpty())
					&& (product.getSalesUnits().length == 1)
					&& (product.getSalesUnits()[0].getName().equalsIgnoreCase("EA"))) {
					confDescr.append(salesUnitDescr);
				}
			}
		}

		//
		// add description of sku variations
		//
		List<DomainValue> varMatr = skuNode.getVariationMatrix();
		for ( DomainValue dv : varMatr ) {
			if (dv == null)
				continue;
			if (confDescr.length() > 0)
				confDescr.append(", ");
			confDescr.append(dv.getLabel().trim());
		}
		List<DomainValue> varOpts = skuNode.getVariationOptions();
		for ( DomainValue dv : varOpts ) {
			//
			// only add this to the description if it can be used to uniquely describe the selected sku
			//
			boolean useThis = false;
			List<SkuModel> skus = prodNode.getSkus();
			for (ListIterator<SkuModel> li = skus.listIterator(); li.hasNext();) {
				SkuModel s = li.next();
				if (s.isUnavailable()) {
					li.remove();
				}
			}
			if (skus.size() == 1) {
				useThis = false;
			} else {
				for ( SkuModel testSku : skus ) {
					List<DomainValue> variationOptions = testSku.getVariationOptions();
					for ( DomainValue testDv : variationOptions ) {
						if (dv.getDomain().equals(testDv.getDomain()) && !dv.equals(testDv)) {
							useThis = true;
						}
					}
				}
			}
			if (useThis) {
				if (confDescr.length() > 0)
					confDescr.append(", ");
				confDescr.append(dv.getLabel().trim());
			}
		}

        //
        // add variation options
        //
        if (EnumProductLayout.HOLIDAY_MEAL_BUNDLE_PRODUCT.equals(prodNode.getSpecialLayout())) {
            List<ComponentGroupModel> componentGroups = prodNode.getComponentGroups();
            if (componentGroups != null) {
                for (ComponentGroupModel componentGroup : componentGroups) {
                    if (componentGroup.getOptionalProducts().isEmpty()) {
                        Map<String, FDVariationOption[]> variationOptions = new HashMap<String, FDVariationOption[]>();
                        for (FDVariation variation : product.getVariations()) {
                            variationOptions.put(variation.getName(), variation.getVariationOptions());
                        }
                        for (String characteristicName : componentGroup.getCharacteristicNames()) {
                            if (variationOptions.containsKey(characteristicName)) {
                                for (FDVariationOption variationOption : variationOptions.get(characteristicName)) {
                                    ProductModel sideBoxProductModel = ContentFactory.getInstance().getProduct(variationOption.getSkuCode());
                                    List<ProductModel> includeSideBoxProductModels = sideBoxProductModel.getIncludeProducts();
                                    if (includeSideBoxProductModels.isEmpty()) {
                                        appendVariationOptionDescriptionToConfigurationDescription(confDescr, sideBoxProductModel.getFullName());
                                    } else {
                                        for (ProductModel includeSideBoxProductModel : includeSideBoxProductModels) {
                                            appendVariationOptionDescriptionToConfigurationDescription(confDescr, includeSideBoxProductModel.getFullName());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for (FDVariation variation : product.getVariations()) {
                String optionName = theProduct.getConfiguration().getOptions().get(variation.getName());
                if (optionName == null)
                    continue;

                for (FDVariationOption option : variation.getVariationOptions()) {
                    if (option.getName().equals(optionName)) {
                        appendVariationOptionDescriptionToConfigurationDescription(confDescr, option.getDescription());
                    }
                }
            }
        }

        return confDescr.toString();
    }

    private static void appendVariationOptionDescriptionToConfigurationDescription(StringBuffer configurationDescription, String optionDescription) {
        if ((!"None".equalsIgnoreCase(optionDescription)) && (!"nm".equalsIgnoreCase(optionDescription)) && (!"".equalsIgnoreCase(optionDescription))) {
            if (configurationDescription.length() > 0)
                configurationDescription.append(", ");
            if (optionDescription.indexOf("(") > -1) {
                configurationDescription.append(optionDescription.substring(0, optionDescription.indexOf("(")).trim());
            } else {
                configurationDescription.append(optionDescription.trim());
            }
        }
    }

	public static boolean isSameConfiguration(FDProductSelectionI cartLine1, FDProductSelectionI cartLine2)
		throws FDResourceException {

		//LOGGER.debug(cartLine1.getSkuCode() +" vs "+cartLine2.getSkuCode());
		String skuCode = cartLine1.getSkuCode();
		if (!skuCode.equals(cartLine2.getSkuCode())) {
			return false;
		}

		ProductModel productNode;
		try {
			productNode = ContentFactory.getInstance().getProduct(skuCode);
		} catch (FDSkuNotFoundException ex) {
			throw new FDResourceException(ex);
		}
		boolean soldBySalesUnit = "SALES_UNIT".equals(productNode.getSellBySalesunit());

		if (!soldBySalesUnit && !cartLine1.getSalesUnit().equals(cartLine2.getSalesUnit())) {
			return false;
		}

		Map<String,String> config1 = cartLine1.getOptions();
		Map<String,String> config2 = cartLine2.getOptions();
		for ( Map.Entry<String,String> e : config1.entrySet() ) {
			String c1 = e.getKey();
			String cv2 = config2.get(c1);

			if (cv2 == null || !cv2.equals(e.getValue())) {
				return false;
			}
		}
		return true;
	}

	public static boolean isSameConfiguration(ConfiguredProduct cartLine1, FDProductSelectionI cartLine2)
	throws FDResourceException {

		//LOGGER.debug(cartLine1.getSkuCode() +" vs "+cartLine2.getSkuCode());
		String skuCode = cartLine1.getSkuCode();
		if (!skuCode.equals(cartLine2.getSkuCode())) {
			return false;
		}
	
		ProductModel productNode;
		try {
			productNode = ContentFactory.getInstance().getProduct(skuCode);
		} catch (FDSkuNotFoundException ex) {
			throw new FDResourceException(ex);
		}
		boolean soldBySalesUnit = "SALES_UNIT".equals(productNode.getSellBySalesunit());
	
		if (!soldBySalesUnit && !cartLine1.getSalesUnit().equals(cartLine2.getSalesUnit())) {
			return false;
		}
	
		Map<String,String> config1 = cartLine1.getOptions();
		Map<String,String> config2 = cartLine2.getOptions();
		for ( Map.Entry<String,String> e : config1.entrySet() ) {
			String c1 = e.getKey();
			String cv2 = config2.get(c1);
	
			if (cv2 == null || !cv2.equals(e.getValue())) {
				return false;
			}
		}	
		return true;
	}
	
	/**
	 * Bring productselection up-to-date (sku version).
	 *
	 * @throws FDInvalidConfigurationException if the configuration is no longer valid
	 */
	public static void cleanup(FDProductSelectionI prodSel) throws FDResourceException, FDInvalidConfigurationException {

		ProductModel prodNode = null;
		ProductReference ref = prodSel.getProductRef();

		// lookup product in non-CRM mode
		if (ref != null && !ProductReferenceImpl.NULL_REF.equals(ref)) {
			prodNode = ref.lookupProductModel();
			if (prodNode == null) {
				throw new FDInvalidConfigurationException(
						"Product is missing : "
								+ ref.getCategoryId() + '/'
								+ ref.getProductId()
								+ ", sku :" + prodSel.getSkuCode());
			}
		}

		// find most recent fd product based on sku
		FDProductInfo productInfo;
		try {
			productInfo = FDCachedFactory.getProductInfo(prodSel.getSkuCode());
			prodSel.setSku(new FDSku(productInfo.getSkuCode(), productInfo.getVersion()));
		} catch (FDSkuNotFoundException e) {
			throw new FDInvalidConfigurationException(e);
		}


        ZoneInfo zone=prodSel.getUserContext().getPricingContext().getZoneInfo();
		if (!productInfo.isAvailable(zone.getSalesOrg(), zone.getDistributionChanel())) {
			
			// throw new
			// FDInvalidConfigurationException.Unavailable("Product "+prodNode.getFullName()+"["
			// +prodSel.getSkuCode()+"] is no longer available");
			// APPDEV-3050
			throw new FDInvalidConfigurationException.Unavailable(
				prodSel.getSkuCode()
							+ " is discontinued or is no longer available.");
		}

		FDProduct product;
		try {
			product = FDCachedFactory.getProduct(productInfo);
		} catch (FDSkuNotFoundException e) {
			throw new FDInvalidConfigurationException(e);
		}

		ensureValidConfiguration(product, prodSel);

		if (prodNode != null && prodNode.hasComponentGroups()) {
			boolean charAvail = prodNode.isCharacteristicsComponentsAvailable(prodSel);
			//LOGGER.warn("OrderLineUtil.cleanup() "+prodNode +" -> "+charAvail);
			if (!charAvail) {
				throw new FDInvalidConfigurationException("This configuration is not available");
			}
		}

		prodSel.refreshConfiguration();
	}

	private static void ensureValidConfiguration(FDProduct product, FDConfigurableI config) throws FDInvalidConfigurationException {

		// validate salesunit
		boolean salesUnitValid = product.getSalesUnit(config.getSalesUnit()) != null;
		if (!salesUnitValid) {
			throw new FDInvalidConfigurationException.InvalidSalesUnit("Invalid salesUnit found " + config.getSalesUnit());
		}

		// validate options
		Map<String,String> conf = config.getOptions();
		Set<String> currOptions = new HashSet<String>();
		FDVariation[] variations = product.getVariations();
		for (int vi = 0; vi < variations.length; vi++) {
			FDVariation var = variations[vi];
			String varOpt = conf.get(var.getName());
			currOptions.add(var.getName());
			if (varOpt == null) {
				throw new FDInvalidConfigurationException.MissingVariation("Missing variation found " + var.getName());
			}
			FDVariationOption[] options = var.getVariationOptions();
			boolean optValid = false;
			for (int voi = 0; voi < options.length; voi++) {
				if (options[voi].getName().equals(varOpt)) {
					optValid = true;
					break;
				}
			}
			
			if (!optValid) {
				throw new FDInvalidConfigurationException.InvalidOption("Invalid option " + varOpt + " found for variation " + var.getName());
			}
		}
		
		if(!currOptions.containsAll(conf.keySet())){
			throw new FDInvalidConfigurationException.InvalidOption("Missing Options");
		}
	}


	public static void cleanProductLists(List<? extends FDCustomerProductList> lists) throws FDResourceException {
		assert (lists != null);
		for ( FDCustomerProductList pl : lists ) {
			pl.cleanList();
		}	
	}

	

	public static Set<String> collectDepartmentNames(Collection<FDCartLineI> orderlines) {
		if (orderlines == null || orderlines.size() == 0)
			return Collections.emptySet();
		
		Set<String> depts = new HashSet<String>();
		
		for (FDCartLineI orderLine : orderlines ) {
			depts.add(orderLine.getDepartmentDesc());
		}
		
		return depts;
	}
	public static boolean isInvalid(FDCartLineI prodSel,UserContext userCtx)  {
		
				ProductModel prodNode;
		        prodNode = prodSel.getProductRef().lookupProductModel();
		        if (prodNode == null) {
		            /*throw new FDInvalidConfigurationException("Product is missing : " + prodSel.getProductRef().getCategoryId() + '/'
		                    + prodSel.getProductRef().getProductId() + ", sku :" + prodSel.getSkuCode());
		                    */
		        	return true;
		        }
		        ZoneInfo zone=userCtx.getPricingContext().getZoneInfo();
		        // find most recent fd product based on sku
		        FDProductInfo productInfo;
				try {
					productInfo = FDCachedFactory.getProductInfo(prodSel.getSkuCode());
					
				} catch (FDSkuNotFoundException e) {
					//throw new FDInvalidConfigurationException(e);
					return true;
				} catch (FDResourceException e) {
					return true;
				}
		
				if (!productInfo.isAvailable(zone.getSalesOrg(),zone.getDistributionChanel())) {
					
					/*throw new FDInvalidConfigurationException.Unavailable(
							prodNode.getFullName()
									+ " is discontinued or is no longer available.");
									*/
					return true;
				}
		
		return false;
	}
	
	public static List<FDCartLineI> getInvalidLines(List<FDCartLineI> productSelections, UserContext userCtx) {
			List<FDCartLineI> invalidSelections = new ArrayList<FDCartLineI>(productSelections.size());

		for ( FDCartLineI ps : productSelections ) {
			
				if(OrderLineUtil.isInvalid(ps,userCtx)) {
					invalidSelections.add(ps);
				}
		}

		return invalidSelections;
	}
}

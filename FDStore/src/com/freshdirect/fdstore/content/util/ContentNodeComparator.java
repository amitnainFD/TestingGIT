package com.freshdirect.fdstore.content.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.content.nutrition.EnumKosherSymbolValue;
import com.freshdirect.content.nutrition.ErpNutritionType;
import com.freshdirect.fdstore.FDKosherInfo;
import com.freshdirect.fdstore.FDNutrition;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.DepartmentModel;
import com.freshdirect.fdstore.content.DomainValue;
import com.freshdirect.fdstore.content.PrioritizedI;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.SkuModel;
import com.freshdirect.fdstore.content.sort.PopularityComparator;
import com.freshdirect.fdstore.content.sort.SaleComparator;
import com.freshdirect.framework.util.log.LoggerFactory;

/**@author ekracoff on Feb 13, 2004*/
public class ContentNodeComparator implements Comparator<ContentNodeModel> {
	
	private static Logger LOGGER = LoggerFactory.getInstance(ContentNodeComparator.class);

	List<SortStrategyElement> strategy;
	
	private Comparator<ContentNodeModel> popularityComparator = null;
	private Comparator<ContentNodeModel> saleComparator = null;

	public ContentNodeComparator(List<SortStrategyElement> strategy) {
		this.strategy = strategy;
		
		for ( SortStrategyElement e : strategy ) {
			
			if ( e.getSortType() == SortStrategyElement.PRODUCTS_BY_POPULARITY && popularityComparator == null ) {
				popularityComparator = new PopularityComparator();
			}
			
			if ( e.getSortType() == SortStrategyElement.PRODUCTS_BY_SALE && saleComparator == null ) {
				saleComparator = new SaleComparator();
			}
			
		}
	}

	public int compare(ContentNodeModel node1, ContentNodeModel node2) {
		int rslt = 0;
		for (Iterator<SortStrategyElement> i = strategy.iterator(); i.hasNext() && rslt == 0;) {
			SortStrategyElement strategyElement = i.next();
			boolean descending = strategyElement.sortDescending();

			rslt = compareByStrategy(node1, node2, strategyElement);

			if (descending)
				rslt = -rslt;
		}

		// If all sort strategies fail to produce match, do final compare against contentName
		if(rslt == 0) {
			return node1.getContentName().compareToIgnoreCase(node2.getContentName());
		}
		return rslt;
	}

	private int compareByStrategy(ContentNodeModel node1, ContentNodeModel node2, SortStrategyElement strategyElement) {
		boolean descending = strategyElement.sortDescending();

		switch (strategyElement.getSortType()) {
			case SortStrategyElement.PRODUCTS_BY_NUTRITION :
				return compareByNutrition(node1, node2, strategyElement.getSecondayAttrib(), descending);

			case SortStrategyElement.PRODUCTS_BY_KOSHER :
				return getKosherPriority(node1) - getKosherPriority(node2);

			case SortStrategyElement.PRODUCTS_BY_PRICE :
				return (int) ((getPrice(node1) - getPrice(node2)) * 100);

			case SortStrategyElement.PRODUCTS_BY_PRIORITY :
				if (node1 instanceof SkuModel && !(node2 instanceof SkuModel)) return 1;
				if (!(node1 instanceof SkuModel) && node2 instanceof SkuModel) return -1;
				if (node1 instanceof SkuModel && node2 instanceof SkuModel)  return 0;

				PrioritizedI p1 = (PrioritizedI) node1;
				PrioritizedI p2 = (PrioritizedI) node2;
				return p1.getPriority() - p2.getPriority();

			case SortStrategyElement.PRODUCTS_BY_NAME :
				return compareByName(node1, node2, strategyElement.getSecondayAttrib());

			case SortStrategyElement.PRODUCTS_BY_DOMAIN_RATING :
			    return compareByMultiDomainValue(node1, node2, strategyElement.getMultiAttribName(), descending);
//				return compareByAttribute(
//					node1,
//					node2,
//					strategyElement.getSecondayAttrib(),
//					strategyElement.getMultiAttribName(),
//					descending);

			case SortStrategyElement.PRODUCTS_BY_WINE_COUNTRY : 
			    return compareByWineCountry((ProductModel) node1, (ProductModel) node2, descending);
			case SortStrategyElement.GROUP_BY_CATEGORY_PRIORITY :
				return groupByCategory(node1, node2, SortStrategyElement.GROUP_BY_CATEGORY_PRIORITY);

			case SortStrategyElement.GROUP_BY_CATEGORY_NAME :
				return groupByCategory(node1, node2, SortStrategyElement.GROUP_BY_CATEGORY_NAME);

			case SortStrategyElement.GROUP_BY_AVAILABILITY :
				return compareByAvailability(node1, node2);

			case SortStrategyElement.PRODUCTS_BY_WINE_ATTRIBUTE :
				return compareByWineAttribute(
						node1,
						node2,
						strategyElement.getSecondayAttrib(),
						strategyElement.getMultiAttribName(),
						descending);
			case SortStrategyElement.PRODUCTS_BY_RATING :
				return compareByProductRating(node1, node2, descending);
			case SortStrategyElement.PRODUCTS_BY_SEAFOOD_SUSTAINABILITY :
				return compareByProductSeafoodSustainabilityRating(node1, node2, descending);
				
			case SortStrategyElement.PRODUCTS_BY_POPULARITY :
				return compareByPopularity(node1, node2, descending);
				
			case SortStrategyElement.PRODUCTS_BY_SALE :
				return compareBySale(node1, node2, descending);
				
			default :
				throw new IllegalArgumentException("Unknown sort type " + strategyElement.getSortType());
		}

	}

	private int compareBySale( ContentNodeModel node1, ContentNodeModel node2, boolean descending ) {
		return descending ? saleComparator.compare( node2, node1 ) : saleComparator.compare( node1, node2 );
	}

	private int compareByPopularity( ContentNodeModel node1, ContentNodeModel node2, boolean descending ) {
		return descending ? popularityComparator.compare( node2, node1 ) : popularityComparator.compare( node1, node2 );
	}

	private int compareByWineCountry(ProductModel node1, ProductModel node2, boolean descending) {
            ContentKey k1 = node1.getWineCountryKey();
            ContentKey k2 = node2.getWineCountryKey();
            if (k1 == null && k2 == null) {
                return 0;
            }
            if (k1 == null) {
                return descending ? -1 : 1;
            }

            if (k2 == null) {
                return descending ? 1 : -1;
            }
            return k1.getId().compareTo(k2.getId());
	}
	

    private int compareByAvailability(ContentNodeModel node1, ContentNodeModel node2) {
		// always put unavailable items to the bottom when not sorting by name
		boolean unav1 = node1 instanceof ProductModel ? ((ProductModel) node1).isUnavailable() 
				: node1 instanceof SkuModel ? ((SkuModel)node1).isUnavailable() : false;
		boolean unav2 = node2 instanceof ProductModel ? ((ProductModel) node2).isUnavailable() 
				: node2 instanceof SkuModel ? ((SkuModel)node2).isUnavailable(): false;
		if (unav1 && !unav2) {
			return 1;
		} else if (!unav1 && unav2) {
			return -1;
		}
		return 0;
	}

	private int compareByNutrition(ContentNodeModel node1, ContentNodeModel node2, String nutriName, boolean naLast) {

		if (node1 instanceof CategoryModel && !(node2 instanceof CategoryModel))
			return 1;
		if (!(node1 instanceof CategoryModel) && node2 instanceof CategoryModel)
			return -1;

		double v1 = getNutritionValue(node1, nutriName);
		double v2 = getNutritionValue(node2, nutriName);

		if (v1 == v2 || (Double.isNaN(v1) && Double.isNaN(v2))) {
			return 0;
		}
		if (Double.isNaN(v1)) {
			return naLast ? -1 : 1;
		}
		if (Double.isNaN(v2)) {
			return naLast ? 1 : -1;
		}

		if (v1 > v2) {
			return 1;
		}
		if (v1 < v2) {
			return -1;
		}
		return 0;
	}

	/** @return Double. if not applicable */
	private double getNutritionValue(ContentNodeModel node, String nutriName) {
		try {
			SkuModel skuModel = null; 
			if (node instanceof CategoryModel) {
				return Double.NaN;
			}

			if (node instanceof ProductModel) {
				skuModel = ((ProductModel)node).getDefaultSku();
			} else if (node instanceof SkuModel){
				skuModel = (SkuModel) node;
			}
			
			if (skuModel == null) {
				return Double.NaN;
			}

			List<FDNutrition> nList = skuModel.getProduct().getNutrition();

			boolean foundNutrition = false;
			double servingSize = -1;
			double nutValue = Double.NaN; //in case nutrition value is not found
			for (Iterator<FDNutrition> iNut = nList.iterator(); iNut.hasNext() && (servingSize == 0 || !foundNutrition);) {
				FDNutrition fdNut = (FDNutrition) iNut.next();
				if (fdNut.getName().equals(ErpNutritionType.getType("SERVING_SIZE").getDisplayName())) {
					servingSize = fdNut.getValue();
					//in case we are actually sorting by serving_size
					if (nutriName.equals(ErpNutritionType.getType("SERVING_SIZE").getDisplayName())) {
						nutValue = fdNut.getValue();
						foundNutrition = true;
					}
				} else if (fdNut.getName().equalsIgnoreCase(nutriName)) {
					nutValue = fdNut.getValue() * 1000;
					if (nutValue < 0)
						nutValue = 0.001;
					foundNutrition = true;
				}
			}

			//force items where serving size = 0 to the bottom
			if (servingSize == 0)
				nutValue = Double.NaN;

			return nutValue;

		} catch (FDResourceException ex) {
			LOGGER.warn("Catching FDResourceException after calling getKosherPriority method in ItemGrabber");
			throw new RuntimeException("Catching FDResourceException after calling getKosherPriority method in ContentNodeComparator");
		} catch (FDSkuNotFoundException fsku) {
			LOGGER.error("Catching FDSkuNotFoundException after calling getKosherPriority method in ItemGrabber");
			return Double.NaN; //	throw new RuntimeException("Catching FDSkuNotFoundException after calling getKosherPriority method in ContentNodeComparator");
		}

	}

	private double getPrice(ContentNodeModel node) {
		try {
			if (node instanceof CategoryModel) {
				return Double.MAX_VALUE;
			}
			SkuModel skuModel = null;
			if (node instanceof ProductModel) {
				skuModel = ((ProductModel)node). getDefaultSku();
			} else {
				skuModel = (SkuModel)node;
			}
			if (skuModel == null) {
				return Double.MAX_VALUE;
			}

			return skuModel.getProductInfo().getZonePriceInfo(skuModel.getPricingContext().getZoneInfo()).getDefaultPrice();

		} catch (FDResourceException ex) {
			LOGGER.warn("Catching FDResourceException after calling getPrice method in ItemGrabber");
			throw new RuntimeException("Catching FDResourceException after calling getPrice method in ContentNodeComparator");
		} catch (FDSkuNotFoundException fsku) {
			LOGGER.error("Catching FDSkuNotFoundException after calling getPrice method in ItemGrabber");
			return Double.MAX_VALUE;
			//throw new RuntimeException("Catching FDSkuNotFoundException after calling getPrice method in ContentNodeComparator");
		}
	}

	private int getKosherPriority(ContentNodeModel node) {
		try {
			SkuModel skuModel = null;
			
			if (!node.getContentType().equals(ContentNodeModel.TYPE_PRODUCT) 
				 && !node.getContentType().equals(ContentNodeModel.TYPE_SKU) ) {
				return Integer.MAX_VALUE;
			}
			if (node instanceof ProductModel){
				ProductModel pm = (ProductModel) node;
				if (pm.isUnavailable()) {
					return Integer.MAX_VALUE;
				}
				skuModel = pm.getDefaultSku();
			} else if (node instanceof SkuModel) {
				skuModel = (SkuModel)node;
			}

			if (skuModel == null) {
				return Integer.MAX_VALUE;
			}
			
			String plantID=ContentFactory.getInstance().getCurrentUserContext().getFulfillmentContext().getPlantId();
			FDKosherInfo kosherInfo = skuModel.getProduct().getKosherInfo(plantID);
			
			if (kosherInfo == null || !kosherInfo.hasKosherSymbol() || kosherInfo.getKosherSymbol().equals(EnumKosherSymbolValue.NONE)) {
				return Integer.MAX_VALUE;
			}

			return kosherInfo.getKosherSymbol().getPriority();
		} catch (FDResourceException ex) {
			LOGGER.warn("Catching FDResourceException after calling getKosherPriority method in ItemGrabber");
			throw new RuntimeException("Catching FDResourceException after calling getKosherPriority method in ContentNodeComparator");
		} catch (FDSkuNotFoundException fsku) {
			LOGGER.error("Catching FDSkuNotFoundException after calling getKosherPriority method in ItemGrabber");
			return Integer.MAX_VALUE;
			//throw new RuntimeException("Catching FDSkuNotFoundException after calling getKosherPriority method in ContentNodeComparator");
		}
	}

	private int compareByName(ContentNodeModel node1, ContentNodeModel node2, String sortingName) {
		String name1;
		String name2;

		if (sortingName == null)
			sortingName = SortStrategyElement.SORTNAME_FULL;

		if (SortStrategyElement.SORTNAME_GLANCE.equals(sortingName)) {
			name1 = node1.getGlanceName();
			name2 = node2.getGlanceName();
		} else if (SortStrategyElement.SORTNAME_NAV.equals(sortingName)) {
			name1 = node1.getNavName();
			name2 = node2.getNavName();
		} else if (SortStrategyElement.SORTNAME_FULL.equals(sortingName)) {
			name1 = node1.getFullName();
			name2 = node2.getFullName();
		} else {
			throw new IllegalStateException("Unknown sortName type " + sortingName);
		}
		//since Categories may not have glance or nav names, set them to the fullname
		if (name1 == null)
			name1 = node1.getFullName();
		if (name2 == null)
			name2 = node2.getFullName();

		if(name1==null) name1 = "";
		if(name2==null) name2 = "";
		
		return name1.compareToIgnoreCase(name2);
	}

//	private int compareByAttribute(
//		ContentNodeModel node1,
//		ContentNodeModel node2,
//		String attribute,
//		String multiAttribName,
//		boolean descending) {
//		Attribute attrib1 = node1.getAttribute(attribute);
//		Attribute attrib2 = node2.getAttribute(attribute);
//
//		if (attrib1 == null && attrib2 == null)
//			return 0;
//
//		if (attrib1 == null)
//			return descending ? -1 : 1;
//
//		if (attrib2 == null)
//			return descending ? 1 : -1;
//
//		EnumAttributeType type = attrib1.getType();
//
//		if (attrib1.equals(attrib2))
//			return 0;
//
//		if (EnumAttributeType.STRING.equals(type))
//			return ((String) attrib1.getValue()).compareTo((String) attrib2.getValue());
//
//		if (EnumAttributeType.INTEGER.equals(type))
//			return ((Integer) attrib1.getValue()).compareTo((Integer) attrib2.getValue());
//
//		if (EnumAttributeType.DOUBLE.equals(type))
//			return ((Double) attrib1.getValue()).compareTo((Double) attrib2.getValue());
//
//		if (attrib1 instanceof MultiAttribute) {
//			if (multiAttribName == null)
//				throw new IllegalArgumentException("Unable to sort by MultiAttribute without the name of the attribute your searching for");
//			return compareMultiAttributes((MultiAttribute) attrib1, (MultiAttribute) attrib2, multiAttribName, descending);
//		}
//
//		if (EnumAttributeType.DOMAINVALUEREF.equals(type)) {
//		    return ((DomainValue) attrib1.getValue()).getContentKey().getId().compareTo(
//		            ((DomainValue) attrib2.getValue()).getContentKey().getId());
//		} else {
//			throw new IllegalArgumentException("Unable to sort by attribute of type " + attrib1.getType().getName());
//		}
//	}
	
	
	private int compareByMultiDomainValue(ContentNodeModel node1, ContentNodeModel node2, String multiAttribName, boolean descending) {
            List<DomainValue> values1;
			if (node1 instanceof ProductModel)
				values1 = ((ProductModel) node1).getRating();
			else
				values1 = Collections.emptyList();
            List<DomainValue> values2;
			if (node2 instanceof ProductModel)
				values2 = ((ProductModel) node2).getRating();
			else
				values2 = Collections.emptyList();
            
            String value1 = getDomainValue(values1, multiAttribName);
            String value2 = getDomainValue(values2, multiAttribName);
            
            if (value1 == null && value2 == null) {
                return 0;
            }

            if (value1 == null) {
                return descending ? -1 : 1;
            }

            if (value2 == null) {
                return descending ? 1 : -1;
            }
            try {
                return Integer.parseInt(value1) - Integer.parseInt(value2);
            } catch (NumberFormatException e) {
                return value1.compareTo(value2);
            }
	}

	private int compareByWineAttribute(
			ContentNodeModel node1,
			ContentNodeModel node2,
			String attributeName,
			String multiAttribName,
			boolean descending) {
			Object attrib1 = getWineAttribute(node1, attributeName);
			Object attrib2 = getWineAttribute(node2, attributeName);
			
			if (attrib1 == null && attrib2 == null)
				return 0;

			if (attrib1 == null)
				return descending ? -1 : 1;

			if (attrib2 == null)
				return descending ? 1 : -1;

			if (attrib1.equals(attrib2))
				return 0;

			if (attrib1 instanceof Integer)
				return ((Integer) attrib1).compareTo((Integer) attrib2);
			else
				return (attrib1.toString()).compareTo(attrib2.toString());

		}
	
	private Integer getProductRating(ContentNodeModel node) {
		if(!(node instanceof ProductModel))
			return null;
		ProductModel pm = (ProductModel) node;
		try {
			int rating = Integer.parseInt(pm.getProductRating());
			return new Integer(rating);
		}catch(Exception exp){
			return null;
		}
	}
	
	private Integer getSustainabilityRating(ContentNodeModel node) {
		if(!(node instanceof ProductModel))
			return null;
		ProductModel pm = (ProductModel) node;
		try {
			int rating = Integer.parseInt(pm.getSustainabilityRating());
			return new Integer(rating);
		}catch(Exception exp){
			return null;
		}
	}
	
	private int compareByProductRating( ContentNodeModel node1, ContentNodeModel node2, boolean descending ) {
		
		Integer attrib1 = getProductRating(node1);
		Integer attrib2 = getProductRating(node2);
		
		if (attrib1 == null && attrib2 == null)
			return 0;

		if (attrib1 == null)
			return descending ? -1 : 1;

		if (attrib2 == null)
			return  descending ? 1 : -1;

		if (attrib1.equals(attrib2))
			return 0;

		return attrib1.compareTo(attrib2);
	}
	
	private int compareByProductSeafoodSustainabilityRating( ContentNodeModel node1, ContentNodeModel node2, boolean descending ) {
		
		Integer attrib1 = getSustainabilityRating(node1);
		Integer attrib2 = getSustainabilityRating(node2);
		
		if (attrib1 == null && attrib2 == null)
			return 0;

		if (attrib1 == null)
			return descending ? -1 : 1;

		if (attrib2 == null)
			return  descending ? 1 : -1;

		if (attrib1.equals(attrib2))
			return 0;

		return attrib1.compareTo(attrib2);
	}
	
	private Object getWineAttribute(ContentNodeModel node, String attributeName) {
		if(!(node instanceof ProductModel))
			return null;
		ProductModel pm = (ProductModel) node;	
		Object attrValue = null;			
		if(EnumWineSortType.RATING.equals(EnumWineSortType.getWineSortType(attributeName))){
			List<DomainValue> ratingValues = pm.getWineRating1();
			if(ratingValues != null && ratingValues.size() > 0){
				DomainValue dv = (DomainValue) ratingValues.get(0);
				try {
					attrValue = new Integer(dv.getValue());
				}catch(NumberFormatException ne){
					attrValue = null;
				}
			}
		}
		if(EnumWineSortType.REGION.equals(EnumWineSortType.getWineSortType(attributeName))) {
			List<DomainValue> regionValues = pm.getNewWineRegion();
			if(regionValues != null && regionValues.size() > 0){
				DomainValue dv = (DomainValue) regionValues.get(0);
				attrValue = dv.getValue();
			}
		}
		if(EnumWineSortType.VARIETY.equals(EnumWineSortType.getWineSortType(attributeName))) {
			List<DomainValue> varietalValues = pm.getWineVarietal();
			if(varietalValues != null && varietalValues.size() > 0){
				DomainValue dv = (DomainValue) varietalValues.get(0);
				attrValue = dv.getValue();
			}
		}
		if(EnumWineSortType.VINTAGE.equals(EnumWineSortType.getWineSortType(attributeName))){
			List<DomainValue> vintageValues = pm.getWineVintage();
			if(vintageValues != null && vintageValues.size() > 0){
				DomainValue dv = (DomainValue) vintageValues.get(0);
				try {
					attrValue = new Integer(dv.getValue());
				}catch(NumberFormatException ne){
					attrValue = null;
				}
			}
		}
		return attrValue;
	}
	
	private int groupByCategory(ContentNodeModel node1, ContentNodeModel node2, int sortType) {
		boolean isProduct1 = node1 instanceof ProductModel || node1 instanceof SkuModel || (node1 instanceof CategoryModel && ((CategoryModel)node1).getTreatAsProduct());
		boolean isProduct2 = node2 instanceof ProductModel || node2 instanceof SkuModel || (node2 instanceof CategoryModel && ((CategoryModel)node2).getTreatAsProduct());

		List<ContentNodeModel> parentList1 = new ArrayList<ContentNodeModel>();
		List<ContentNodeModel> parentList2 = new ArrayList<ContentNodeModel>();

		fillParentList(parentList1, node1);
		fillParentList(parentList2, node2);

		int len = Math.min(parentList1.size(), parentList2.size());
		//we will traverse the list backwards.		
		for (int i = 1; i <= len; i++) {
			CategoryModel cn1 = (CategoryModel) parentList1.get(parentList1.size() - i);
			CategoryModel cn2 = (CategoryModel) parentList2.get(parentList2.size() - i);
			int r = compareFolders(cn1, cn2, sortType);
			if (r != 0) {
				return r;
			}

		}

		if (isProduct1 && !isProduct2 && node1.getParentNode() == node2) {
			return 1;
		}

		if (!isProduct1 && isProduct2 && node1 == node2.getParentNode()) {
			return -1;
		}

		int rslt = parentList1.size() - parentList2.size();
		return rslt;
	}

	private int compareFolders(CategoryModel cn1, CategoryModel cn2, int sortType) {
		if (cn1 == cn2) {
			return 0;
		}
		// if sorting by priority then
		int r = 0;
		if (sortType == SortStrategyElement.GROUP_BY_CATEGORY_PRIORITY) {
			r = cn1.getPriority() - cn2.getPriority();
		}
		//if previous tests yield equality, then check the names.
		if (r == 0) {
			r = cn1.getFullName().compareToIgnoreCase(cn2.getFullName());
		}
		return r;
	}

	private void fillParentList(List<ContentNodeModel> parentList, ContentNodeModel node) {
		parentList.clear();
		ContentNodeModel theNode = (node instanceof SkuModel ? node.getParentNode() : node);
		if (theNode instanceof CategoryModel && !((CategoryModel)theNode).getTreatAsProduct()) {
			//since the node is a category that is not being treated as a product, then place it into the list 
			parentList.add(theNode);
		}
		while (theNode.getParentNode() != null && !(theNode.getParentNode() instanceof DepartmentModel)) {
			theNode = theNode.getParentNode();
			parentList.add(theNode);
		}
	}

        private String getDomainValue(List<DomainValue> values, String attribName) {
            for (DomainValue dv : values) {
                if (attribName.equalsIgnoreCase(dv.getDomainContentKey().getId())) {
                    return dv.getContentKey().getId();
                }
            }
            return null;
        }
	
//	private int compareMultiAttributes(
//		MultiAttribute attrib1,
//		MultiAttribute attrib2,
//		String multiAttribName,
//		boolean descending) {
//		String value1 = getMultiAttributeValue(attrib1, multiAttribName);
//		String value2 = getMultiAttributeValue(attrib2, multiAttribName);
//
//		if (value1 == null && value2 == null)
//			return 0;
//
//		if (value1 == null)
//			return descending ? -1 : 1;
//
//		if (value2 == null)
//			return descending ? 1 : -1;
//
//		try {
//			return Integer.parseInt(value1) - Integer.parseInt(value2);
//		} catch (NumberFormatException e) {
//			return value1.compareTo(value2);
//		}
//	}

//	private String getMultiAttributeValue(MultiAttribute attrib, String attribName) {
//		for (Iterator i = attrib.getValues().iterator(); i.hasNext();) {
//			Object obj = i.next();
//
//			if (obj instanceof DomainValue) {
//			    DomainValue dv = (DomainValue) obj;
//			    if (attribName.equalsIgnoreCase(dv.getDomainContentKey().getId())) {
//			        return dv.getContentKey().getId();
//			    }
//			}
//		}
//
//		return null;
//	}
//	

}

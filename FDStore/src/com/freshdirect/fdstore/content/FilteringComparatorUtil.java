package com.freshdirect.fdstore.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.util.SmartSearchUtils;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.ecoupon.EnumCouponOfferType;
import com.freshdirect.fdstore.ecoupon.FDCouponFactory;
import com.freshdirect.fdstore.ecoupon.model.FDCouponInfo;
import com.freshdirect.fdstore.pricing.ProductModelPricingAdapter;
import com.freshdirect.fdstore.util.FilteringNavigator;
import com.freshdirect.fdstore.util.NewProductsGrouping;
import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.fdstore.ScoreProvider;
import com.freshdirect.smartstore.sorting.ScriptedContentNodeComparator;

public class FilteringComparatorUtil {
	private static final Logger LOGGER = LoggerFactory.getInstance(FilteringComparatorUtil.class);
	
	public static Comparator<FilteringSortingItem<ProductModel>> createProductComparator(List<FilteringSortingItem<ProductModel>> products,
			String userId, PricingContext pricingContext, String suggestedTerm, FilteringNavigator nav, boolean showGrouped) {
		
		SearchSortType sortBy = (SearchSortType)nav.getSortBy();
		boolean ascending = nav.isSortOrderingAscending();
		
		ComparatorChain<FilteringSortingItem<ProductModel>> comparator;
		switch (sortBy) {
			case DEFAULT:
				return null;
			case BY_NAME:
				comparator = ComparatorChain.create(FilteringSortingItem.wrap(ProductModel.FULL_NAME_PRODUCT_COMPARATOR));
				if (!ascending)
					comparator = ComparatorChain.reverseOrder(comparator);
				break;
			case BY_PRICE:
				comparator = ComparatorChain.create(FilteringSortingItem.wrap(ProductModel.GENERIC_PRICE_COMPARATOR));
				if (!ascending)
					comparator = ComparatorChain.reverseOrder(comparator);
				comparator.chain(FilteringSortingItem.wrap(ProductModel.FULL_NAME_PRODUCT_COMPARATOR));
				break;
			case BY_POPULARITY:
				comparator = ComparatorChain.create(FilteringSortingItem.wrap(ScriptedContentNodeComparator.createGlobalComparator(userId, pricingContext)));
				if (!ascending)
					comparator = ComparatorChain.reverseOrder(comparator);
				comparator.chain(FilteringSortingItem.wrap(ProductModel.FULL_NAME_PRODUCT_COMPARATOR));
				break;
			case BY_SALE:
				SmartSearchUtils.collectSaleInfo(products, pricingContext);
				comparator = ComparatorChain.create(new SortValueComparator<ProductModel>(EnumSortingValue.DEAL));
				if (!ascending)
					comparator = ComparatorChain.reverseOrder(comparator);
				comparator.chain(FilteringSortingItem.wrap(ProductModel.FULL_NAME_PRODUCT_COMPARATOR));
				break;
			case BY_RELEVANCY:
				// if there's only one DYM then we display products for that DYM
				// but for those products we have to use the suggested term to produce the following scores
				SmartSearchUtils.collectOriginalTermInfo(products, suggestedTerm);
				SmartSearchUtils.collectRelevancyCategoryScores(products, suggestedTerm);
				SmartSearchUtils.collectTermScores(products, suggestedTerm);
				comparator = ComparatorChain.create(new SortValueComparator<ProductModel>(EnumSortingValue.PHRASE));
				comparator.chain(FilteringSortingItem.wrap(ScriptedContentNodeComparator.createUserComparator(userId, pricingContext)));
				comparator.chain(new SortIntValueComparator<ProductModel>(EnumSortingValue.ORIGINAL_TERM));
				comparator.chain(new SortValueComparator<ProductModel>(EnumSortingValue.CATEGORY_RELEVANCY));
				comparator.chain(new SortLongValueComparator<ProductModel>(EnumSortingValue.TERM_SCORE));
				comparator.chain(FilteringSortingItem.wrap(ScriptedContentNodeComparator.createGlobalComparator(userId, pricingContext)));
				if (!ascending)
					comparator = ComparatorChain.reverseOrder(comparator);
				comparator.chain(FilteringSortingItem.wrap(ProductModel.FULL_NAME_PRODUCT_COMPARATOR));
				break;
			case BY_START_DATE:
				comparator = ComparatorChain.create(FilteringSortingItem.wrap(COUPON_START_DATE_COMPARATOR));
				if (!ascending)
					comparator = ComparatorChain.reverseOrder(comparator);
				comparator.chain(FilteringSortingItem.wrap(ProductModel.FULL_NAME_PRODUCT_COMPARATOR));
				break;
			case BY_EXPIRATION_DATE:
				comparator = ComparatorChain.create(FilteringSortingItem.wrap(COUPON_EXP_DATE_COMPARATOR));
				comparator.chain(FilteringSortingItem.wrap(ProductModel.FULL_NAME_PRODUCT_COMPARATOR));
				if (!ascending)
					comparator = ComparatorChain.reverseOrder(comparator);
				break;
				
			case BY_PERC_DISCOUNT:
				comparator = ComparatorChain.create(FilteringSortingItem.wrap(COUPON_PERCENT_OFF_COMPARATOR));
				if (!ascending)
					comparator = ComparatorChain.reverseOrder(comparator);
				comparator.chain(FilteringSortingItem.wrap(ProductModel.FULL_NAME_PRODUCT_COMPARATOR));
				break;
			case BY_DOLLAR_DISCOUNT:
				comparator = ComparatorChain.create(FilteringSortingItem.wrap(COUPON_DOLLAR_OFF_COMPARATOR));
				if (!ascending)
					comparator = ComparatorChain.reverseOrder(comparator);
				comparator.chain(FilteringSortingItem.wrap(ProductModel.FULL_NAME_PRODUCT_COMPARATOR));
				break;
			case BY_PRIORITY:
				comparator = ComparatorChain.create(FilteringSortingItem.wrap(COUPON_POPULARITY_COMPARATOR));
				if (!ascending)
					comparator = ComparatorChain.reverseOrder(comparator);
				comparator.chain(FilteringSortingItem.wrap(ProductModel.FULL_NAME_PRODUCT_COMPARATOR));
				break;
			case BY_RECENCY:
			default:
				if (showGrouped)
					comparator = ComparatorChain.create(new NewProductsGrouping(!nav.isSortOrderingAscending()).getTimeRangeComparator());
				else
					comparator = ComparatorChain.create(new SortValueComparator<ProductModel>(EnumSortingValue.NEWNESS));
				if (!ascending)
					comparator = ComparatorChain.reverseOrder(comparator);
				if(showGrouped)
					comparator.chain(FilteringSortingItem.wrap(ProductModel.DEPTFULL_COMPARATOR)).chain(FilteringSortingItem.wrap(ProductModel.FULL_NAME_PRODUCT_COMPARATOR));

		}
		SmartSearchUtils.collectAvailabilityInfo(products, pricingContext);
		comparator.prepend(new SortValueComparator<ProductModel>(EnumSortingValue.AVAILABILITY));
		return comparator;
	}
	
	public static List<FilteringSortingItem<ProductModel>> reOrganizeFavourites(List<FilteringSortingItem<ProductModel>> products, String userId, PricingContext pricingContext) {
		//Collecting favourites
		ComparatorChain<FilteringSortingItem<ProductModel>> comparator = ComparatorChain.create(FilteringSortingItem.wrap(ScriptedContentNodeComparator.createUserComparator(userId, pricingContext)));
		SmartSearchUtils.collectAvailabilityInfo(products, pricingContext);
		comparator.prepend(new SortValueComparator<ProductModel>(EnumSortingValue.AVAILABILITY));
		Collections.sort(products, comparator);
		List<FilteringSortingItem<ProductModel>> favourites = new ArrayList<FilteringSortingItem<ProductModel>>();
		for (FilteringSortingItem<ProductModel> product : products) {
			if (ScoreProvider.getInstance().isUserHasScore(userId, product.getNode().getContentKey()) && product.getModel().isFullyAvailable()) {
				favourites.add(product);
			}
		}

		//Sorting favorites according to user relevance
		/*comparator = ComparatorChain.create(new SortValueComparator<ProductModel>(EnumSortingValue.CATEGORY_RELEVANCY));
		comparator.chain(new SortLongValueComparator<ProductModel>(EnumSortingValue.TERM_SCORE));
		SmartSearchUtils.collectAvailabilityInfo(products, pricingContext);
		comparator.prepend(new SortValueComparator<ProductModel>(EnumSortingValue.AVAILABILITY));
		Collections.sort(favourites, comparator);*/
		ComparatorChain<FilteringSortingItem<ProductModel>> comparatorForFavorites = ComparatorChain.create(FilteringSortingItem.wrap(ScriptedContentNodeComparator.createUserComparator(userId, pricingContext)));
		comparator.chain(new SortValueComparator<ProductModel>(EnumSortingValue.CATEGORY_RELEVANCY));
		comparatorForFavorites.chain(new SortLongValueComparator<ProductModel>(EnumSortingValue.TERM_SCORE));
		SmartSearchUtils.collectAvailabilityInfo(products, pricingContext);
		comparatorForFavorites.prepend(new SortValueComparator<ProductModel>(EnumSortingValue.AVAILABILITY));
		Collections.sort(favourites, comparatorForFavorites);
		

		//Reordering favourites in the product list
		for (int index = 0; index < Math.min(FDStoreProperties.getSearchPageTopFavouritesNumber(), favourites.size()); index ++) {
			products.remove(favourites.get(index));
		}
		
		comparator = ComparatorChain.create(new SortValueComparator<ProductModel>(EnumSortingValue.PHRASE));
		comparator.chain(new SortIntValueComparator<ProductModel>(EnumSortingValue.ORIGINAL_TERM));
		comparator.chain(new SortValueComparator<ProductModel>(EnumSortingValue.CATEGORY_RELEVANCY));
		comparator.chain(new SortLongValueComparator<ProductModel>(EnumSortingValue.TERM_SCORE));
		comparator.chain(FilteringSortingItem.wrap(ScriptedContentNodeComparator.createGlobalComparator(userId, pricingContext)));
		comparator.chain(FilteringSortingItem.wrap(ProductModel.FULL_NAME_PRODUCT_COMPARATOR));
		SmartSearchUtils.collectAvailabilityInfo(products, pricingContext);
		comparator.prepend(new SortValueComparator<ProductModel>(EnumSortingValue.AVAILABILITY));

		Collections.sort(products, comparator);
		for (int index = Math.min(FDStoreProperties.getSearchPageTopFavouritesNumber(), favourites.size()) - 1; index >= 0 ; index --) {
			products.add(0,favourites.get(index));
		}
		
		return products;
	}

	
	public static Comparator<FilteringSortingItem<Recipe>> RECIPE_SORT_BY_NAME_ASC = new Comparator<FilteringSortingItem<Recipe>>() {
		@Override
		public int compare(FilteringSortingItem<Recipe> arg0, FilteringSortingItem<Recipe> arg1) {
			return arg0.getModel().getName().compareTo(arg1.getModel().getName());
		}
	};
	
	public static Comparator<FilteringSortingItem<Recipe>> RECIPE_SORT_BY_NAME_DESC = new Comparator<FilteringSortingItem<Recipe>>() {
		@Override
		public int compare(FilteringSortingItem<Recipe> arg0, FilteringSortingItem<Recipe> arg1) {
			return arg1.getModel().getName().compareTo(arg0.getModel().getName());
		}
	};
	
	
	public final static Comparator<ProductModel> COUPON_EXP_DATE_COMPARATOR = new Comparator<ProductModel>() {

		public int compare(ProductModel p1, ProductModel p2) {
			try {
				FDCouponInfo cp1 = FDCouponFactory.getInstance().getCouponByUpc(p1.getDefaultSku().getProductInfo().getUpc());
				FDCouponInfo cp2 = FDCouponFactory.getInstance().getCouponByUpc(p2.getDefaultSku().getProductInfo().getUpc());
				if(null != cp1 && null !=cp2){
					Date d1=cp1.getExpirationDate();
					Date d2=cp2.getExpirationDate();
					return d1.compareTo(d2);
				}
			} catch(Exception e) {
				
			}
			return 0;	
		}
	};
	
	public final static Comparator<ProductModel> COUPON_START_DATE_COMPARATOR = new Comparator<ProductModel>() {

		public int compare(ProductModel p1, ProductModel p2) {		
			try {
				FDCouponInfo cp1 = FDCouponFactory.getInstance().getCouponByUpc(p1.getDefaultSku().getProductInfo().getUpc());
				FDCouponInfo cp2 = FDCouponFactory.getInstance().getCouponByUpc(p2.getDefaultSku().getProductInfo().getUpc());
				if(null != cp1 && null !=cp2){
					Date d1=DateUtil.parseMDY(cp1.getStartDate());
					Date d2=DateUtil.parseMDY(cp2.getStartDate());
					return d1.compareTo(d2);
				}
			} catch(Exception e) {
				
			}
			return 0;			
		}
	};
	
	public final static Comparator<ProductModel> COUPON_DOLLAR_OFF_COMPARATOR = new Comparator<ProductModel>() {

		public int compare(ProductModel p2, ProductModel p1) {	
			try {
				FDCouponInfo cp1 = FDCouponFactory.getInstance().getCouponByUpc(p1.getDefaultSku().getProductInfo().getUpc());
				FDCouponInfo cp2 = FDCouponFactory.getInstance().getCouponByUpc(p2.getDefaultSku().getProductInfo().getUpc());
				if(null != cp1 && null !=cp2){
					Double disc1=0.0;
					Double disc2=0.0;
					if(EnumCouponOfferType.DOLLAR_OFF.equals(cp1.getOfferType())){
						disc1=Double.parseDouble(cp1.getValue());
					}else{
						if(EnumCouponOfferType.PERCENT_OFF.equals(cp1.getOfferType()) && p1 instanceof ProductModelPricingAdapter){
							ProductModelPricingAdapter pricing1 = (ProductModelPricingAdapter)p1;
							disc1=Double.parseDouble(cp1.getValue());
							//Calculate the dollar discount based on the % discount value and the price
							double price =pricing1.getPriceCalculator().getDefaultPriceValue();
							disc1=(price*disc1)/100;
						}
					}
					
					if(EnumCouponOfferType.DOLLAR_OFF.equals(cp2.getOfferType())){
						disc2=Double.parseDouble(cp2.getValue());	
					}else{
						if(EnumCouponOfferType.PERCENT_OFF.equals(cp2.getOfferType()) && p2 instanceof ProductModelPricingAdapter){
							ProductModelPricingAdapter pricing2 = (ProductModelPricingAdapter)p2;
							disc2=Double.parseDouble(cp2.getValue());
							//Calculate the dollar discount based on the % discount value and the price
							double price =pricing2.getPriceCalculator().getDefaultPriceValue();
							disc2=(price*disc2)/100;
						}
					}
					
					return disc1.compareTo(disc2);
				}
			} catch (Exception e) {
			}
			return 0;
			
		}
	};
	
	public final static Comparator<ProductModel> COUPON_PERCENT_OFF_COMPARATOR = new Comparator<ProductModel>() {

		public int compare(ProductModel p2, ProductModel p1) {			
			try {
				FDCouponInfo cp1 = FDCouponFactory.getInstance().getCouponByUpc(p1.getDefaultSku().getProductInfo().getUpc());
				FDCouponInfo cp2 = FDCouponFactory.getInstance().getCouponByUpc(p2.getDefaultSku().getProductInfo().getUpc());
				if(null != cp1 && null !=cp2){
					Double disc1=0.0;
					Double disc2=0.0;
					if(EnumCouponOfferType.PERCENT_OFF.equals(cp1.getOfferType())){
						disc1=Double.parseDouble(cp1.getValue());
					}else{
						if(EnumCouponOfferType.DOLLAR_OFF.equals(cp1.getOfferType()) && p1 instanceof ProductModelPricingAdapter){
							ProductModelPricingAdapter pricing1 = (ProductModelPricingAdapter)p1;
							disc1=Double.parseDouble(cp1.getValue());
							double price =pricing1.getPriceCalculator().getDefaultPriceValue();
							//Calculate the % discount based on the dollar discount value and the price
							disc1= price-disc1> 0?((disc1)/price)*100:100;
							disc1=Math.abs(disc1);
						}
					}
					
					if(EnumCouponOfferType.PERCENT_OFF.equals(cp2.getOfferType())){
						disc2=Double.parseDouble(cp2.getValue());	
					}else{
						if(EnumCouponOfferType.DOLLAR_OFF.equals(cp2.getOfferType()) && p2 instanceof ProductModelPricingAdapter){
							ProductModelPricingAdapter pricing2 = (ProductModelPricingAdapter)p2;
							disc2=Double.parseDouble(cp2.getValue());
							double price =pricing2.getPriceCalculator().getDefaultPriceValue();
							//Calculate the % discount based on the dollar discount value and the price
							disc2= price-disc2> 0?((disc2)/price)*100:100;
							disc2=Math.abs(disc2);
						}
					}
					
					return disc1.compareTo(disc2);
				}
				
			} catch (Exception e) {
			}
			return 0;
			
		}
	};
	
	public final static Comparator<ProductModel> COUPON_POPULARITY_COMPARATOR = new Comparator<ProductModel>() {

		public int compare(ProductModel p1, ProductModel p2) {			
			try {
				FDCouponInfo cp1 = FDCouponFactory.getInstance().getCouponByUpc(p1.getDefaultSku().getProductInfo().getUpc());
				FDCouponInfo cp2 = FDCouponFactory.getInstance().getCouponByUpc(p2.getDefaultSku().getProductInfo().getUpc());
				if(null != cp1 && null !=cp2){
					if(cp2.getOfferPriority()!=null && cp1.getOfferPriority()!=null){
						return Integer.valueOf(cp1.getOfferPriority()).compareTo(Integer.valueOf(cp2.getOfferPriority()));
					}else if(cp1.getOfferPriority()!=null){
						return 1;
					}
				}
			} catch (Exception e) {
			}
			return 0;			
		}
	};
	
	
	

	public static void logSortResult(List<FilteringSortingItem<ProductModel>> products, FDUserI user){
		try {
			FDIdentity identity = user.getIdentity();
			ScriptedContentNodeComparator c = ScriptedContentNodeComparator.createGlobalComparator(identity == null ? null : identity.getErpCustomerPK(), user.getPricingContext());
			StringBuilder sb = new StringBuilder("\n");
			
			for (FilteringSortingItem<ProductModel> s : products){
				if (s==null || s==null){
					continue;
				}
				sb.append(s.getSortingValue(EnumSortingValue.PHRASE));
				sb.append("\t");
				sb.append(s.getSortingValue(EnumSortingValue.ORIGINAL_TERM));
				sb.append("\t");
				sb.append(s.getSortingValue(EnumSortingValue.CATEGORY_RELEVANCY));
				sb.append("\t");
				sb.append(s.getSortingValue(EnumSortingValue.TERM_SCORE));
				sb.append("\t");
				sb.append(c.getScore(s.getModel()));
				sb.append("\t");
				sb.append(s.getModel().getFullName());
				sb.append("\t");
				sb.append(s.getModel().getPrimaryHome().getDepartment().getFullName());
				sb.append("\n");
			}
			LOGGER.debug(sb);
		
		} catch (Exception e){
			LOGGER.debug("Log failed", e);
		}
	}
}


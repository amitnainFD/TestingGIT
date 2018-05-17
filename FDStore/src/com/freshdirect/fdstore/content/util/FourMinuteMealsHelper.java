package com.freshdirect.fdstore.content.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.ContentType;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.content.nutrition.EnumClaimValue;
import com.freshdirect.fdstore.FDNutrition;
import com.freshdirect.fdstore.FDProduct;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.BrandModel;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.DepartmentModel;
import com.freshdirect.fdstore.content.Domain;
import com.freshdirect.fdstore.content.DomainValue;
import com.freshdirect.fdstore.content.Html;
import com.freshdirect.fdstore.content.MediaI;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.SkuModel;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.pricing.ProductModelPricingAdapter;
import com.freshdirect.fdstore.pricing.ProductPricingFactory;
import com.freshdirect.fdstore.util.DYFUtil;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.sorting.ScriptedContentNodeComparator;


public class FourMinuteMealsHelper {
	
	private static final Logger LOGGER = LoggerFactory.getInstance( FourMinuteMealsHelper.class );
	
	// === Constant ContentKey-s ===
	public static final ContentKey departmentKey = new ContentKey( ContentType.get( "Department" ), "fdi" );
	public static final ContentKey sidesInASnapKey = new ContentKey( ContentType.get( "Category" ), "hmr_fresh_sias" );
	public static final ContentKey editorialKey = new ContentKey( ContentType.get( "Category" ), "4mm-editorial" );
	public static final ContentKey filterPageKey = new ContentKey( ContentType.get( "Category" ), "4mm-view-all" );
	public static final ContentKey aboutPageKey = new ContentKey( ContentType.get( "Category" ), "4mm-about" );
	public static final ContentKey ingredientdomainKey = new ContentKey( ContentType.get( "Domain" ), "4mm-ingredients" );	
	public static final ContentKey sideTypeDomainKey = new ContentKey( ContentType.get( "Domain" ), "4mm-sides" );
	public static final ContentKey extraVeggieSideDVKey = new ContentKey( ContentType.get( "DomainValue" ), "4mm-sides-vegetable" );
	public static final ContentKey extraStarchSideDVKey = new ContentKey( ContentType.get( "DomainValue" ), "4mm-sides-starch" );
			
	// === Base url for all/filter page ===
	public static final String allPageBaseUrl = "/4mm/all.jsp?deptId=fdi&trk=4mm-filter";
	
	// === Filter page sorting modes ===
	public static final String sortBrand = "brand";
	public static final String sortPopularity = "pop";
	public static final String sortPriceAsc = "priceasc";
	public static final String sortPriceDesc = "pricedesc";

	public static final String sortParam = "sortby";

	public static final SortInfo[] sortInfos = {	
		new SortInfo(sortBrand,"Brand"),
	 	new SortInfo(sortPopularity,"Popularity"),
	 	new SortInfo(sortPriceAsc,"Price (low to high)"),
	 	new SortInfo(sortPriceDesc,"Price (high to low)")
	};

	// === Filtering url parameter names ===
	public static final String filterParamRestaurant = "r";
	public static final String filterParamNutrition = "n";
	public static final String filterParamIngredients = "i";
	public static final String filterParamPrice = "p";
	public static final String filterParamCalories = "c";
	
	// === Constant price and nutrition infos ===
	
	private static final List<String> defaultPriceFilterIds = Arrays.asList( "LT6", "LT8", "LT10", "LT13", "LT15", "LT20", "LT100" );
	private static final double[] priceLimits = { 6.0, 8.0, 10.0, 13.0, 15.0, 20.0, 100.0 };

	private static final List<String> defaultCaloriesFilterIds = Arrays.asList( "LT200C", "LT300C", "LT400C", "LT500C" );
	private static final double[] caloriesLimits = {200.0, 300.0, 400.0, 500.0 };

	private static final List<EnumClaimValue> nutritionClaims = Arrays.asList( 
			EnumClaimValue.NUTRITION_4MM_LOWCALORIE, 
			EnumClaimValue.NUTRITION_4MM_VEGETARIAN, 
			EnumClaimValue.NUTRITION_4MM_2VEGETABLE, 
			EnumClaimValue.NUTRITION_4MM_WHOLEGRAINS, 
			EnumClaimValue.NUTRITION_4MM_HIGHFIBER, 
			EnumClaimValue.NUTRITION_4MM_SODIUM 
	);
	private static List<String> nutritionFilterIds = Arrays.asList( 
			EnumClaimValue.NUTRITION_4MM_LOWCALORIE.getCode(), 
			EnumClaimValue.NUTRITION_4MM_VEGETARIAN.getCode(), 
			EnumClaimValue.NUTRITION_4MM_2VEGETABLE.getCode(), 
			EnumClaimValue.NUTRITION_4MM_WHOLEGRAINS.getCode(), 
			EnumClaimValue.NUTRITION_4MM_HIGHFIBER.getCode(), 
			EnumClaimValue.NUTRITION_4MM_SODIUM.getCode() 
	);

	
	// convert minutes to milliseconds
	private static final long initFrequency = 1000 * 60 * FDStoreProperties.get4mmRefreshInterval(); 		
	private static long lastInitTimeStamp = 0;

	/**
	 * Internal class containing all the cached indexes for fast filtering.
	 * Only the init method should write these when refreshing.
	 * Read-only for all other methods.  
	 */
	private static class FilterCache {
		
		private DepartmentModel department = null;
		private CategoryModel aboutPage = null;
		private CategoryModel filterPage = null;	
		private CategoryModel editorial = null;
		private List<CategoryModel> restaurants = null;		
		
		private Domain ingredientsDomain = null;
		private List<DomainValue> ingredientsDomainValues = null;
		
		private List<String> restaurantFilterIds;
		private List<String> ingredientsFilterIds;			
		
		private Map<String, FilterInfo> filterInfos;

		private Map<String, FilterInfo> restaurantCounts;
		private int totalCount = 0;

		private Map<String, List<ProductModel>> restaurantProducts;
		private Map<String, List<ProductModel>> nutritionProducts;	
		private Map<String, List<ProductModel>> ingredientProducts;

		private List<ProductModel> allProducts;
		
		private List<ProductModel> sideProducts;
		private List<ProductModel> realSiasProducts;		
		
		private List<MediaI> restaurantMedia;
		
		private MediaI mealHeaderMedia = null;
		private MediaI sideHeaderMedia = null;		
		
		private DomainValue extraVeggieSideDV;
		private DomainValue extraStarchSideDV;
		private List<ProductModel> extraVegetableSides;
		private List<ProductModel> extraStarchSides;		
		private CategoryModel sideVeggieCategory;
		private CategoryModel sideStarchCategory;
		
		private List<ProductModel> siasVegetableSides;
		private List<ProductModel> siasStarchSides;		
		
		private MediaI siasVegetableMedia;
		private MediaI siasStarchMedia;

		private List<String> priceFilterIds;
		private List<String> caloriesFilterIds;
		
		private Map<ProductModel, Double> calories;
	}
	
	// private cache instance
	private static FilterCache cache;
	
	/**
	 *	Simple and fast price comparator. Compares the default prices. 
	 */
	private static class PriceComparator implements Comparator<ProductModelPricingAdapter> {
		
	    final boolean inverse;
	    
	    public PriceComparator(boolean inverse) {
	        this.inverse = inverse;
	    }
	    
        public int compare(ProductModelPricingAdapter model1, ProductModelPricingAdapter model2) {
        	
            double price1 = model1.getPriceCalculator().getDefaultPriceValue();
            double price2 = model2.getPriceCalculator().getDefaultPriceValue();

            int result = Double.compare(price1, price2);
            if (result == 0)
            	result = model1.getFullName().compareTo(model2.getFullName());
            return inverse ? -result : result;
        }
    }
	
	// Price comparator instances 
    public final static Comparator<ProductModelPricingAdapter> PRICE_COMPARATOR_ASC = new PriceComparator(false);
    public final static Comparator<ProductModelPricingAdapter> PRICE_COMPARATOR_DESC = new PriceComparator(true);

	

	// === Public getters ===
	
	public static DepartmentModel get4mmDepartment() {
		initLazy();
		return cache.department;
	}	
	public static CategoryModel getAboutPage() {
		initLazy();
		return cache.aboutPage;
	}
	public static CategoryModel getFilterPage() {
		initLazy();
		return cache.filterPage;
	}
	
	public static List<CategoryModel> getRestaurants() {
		initLazy();
		return Collections.unmodifiableList( cache.restaurants );
	}	
	public static Domain getIngredientsDomain() {
		initLazy();
		return cache.ingredientsDomain;
	}	
	public static List<DomainValue> getIngredientsDomainValues() {
		initLazy();
		return Collections.unmodifiableList( cache.ingredientsDomainValues );
	}	
	public static List<EnumClaimValue> getNutritionClaims() {
		return Collections.unmodifiableList( nutritionClaims );
	}
	
	public static List<String> getRestaurantFilterIds() {
		initLazy();
		return Collections.unmodifiableList( cache.restaurantFilterIds );
	}	
	public static List<String> getNutritionFilterIds() {
		return Collections.unmodifiableList( nutritionFilterIds );
	}	
	public static List<String> getIngredientsFilterIds() {
		initLazy();
		return Collections.unmodifiableList( cache.ingredientsFilterIds );
	}
	public static List<String> getPriceFilterIds() {
		initLazy();
		return Collections.unmodifiableList( cache.priceFilterIds );
	}
	public static List<String> getCaloriesFilterIds() {
		initLazy();
		return Collections.unmodifiableList( cache.caloriesFilterIds );
	}
	
	public static Map<String, FilterInfo> getFilterInfos() {
		initLazy();
		return Collections.unmodifiableMap( cache.filterInfos );
	}	

	public static Map<String, FilterInfo> getRestaurantCounts() {
		initLazy();
		return Collections.unmodifiableMap( cache.restaurantCounts );
	}

	public static int getTotalCount() {
		initLazy();
		return cache.totalCount;
	}
	
	public static boolean isSidesInASnapCategory( ContentNodeModel cat ) {
		return cat.getContentKey().equals( sidesInASnapKey );
	}

	public static CategoryModel get4mmEditorialRecommender() {
		initLazy();
		return cache.editorial;
	}	

	
	/**
	 *	Small utility class for filter page sort modes, contains an id and a label
	 */
	public static class SortInfo {
		
		private String id;
		private String label;
		
		public SortInfo(String id,String label) {
			this.id=id;
			this.label=label;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
	}

	
	/**
	 *	Small utility class for filtering, contains a label(String) and a count(int) 
	 */
	public static class FilterInfo {	
		
		public FilterInfo( String label, int count ) {
			this.label = label;
			this.count = count;
		}
		
		public FilterInfo( FilterInfo copy ) {
			this.label = copy.label;
			this.count = copy.count;
		}
		
		private String label;
		private int count;
		
		public String getLabel() {
			return label;
		}
		
		public void setLabel( String label ) {
			this.label = label;
		}
		
		public int getCount() {
			return count;
		}
		
		public void setCount( int count ) {
			this.count = count;
		}
		
		@Override
		public String toString() {			
			return "Filterinfo[" + label + "=" + count + "]";
		}
	}

	/**
	 * 	Utility class for filtering, contains all the results of a filtering operation:
	 * 	- filter parameters used for filtering
	 *  - updated FilterInfos representing the current state
	 *  - the resulting filtered collection of items 
	 */
	public static class FilterResult {
		
		private Map<String,List<String>> filterParams;
		private Map<String,FilterInfo> filterInfos;
		
		private List<List<? extends ProductModel>> multiList;
		private List<MediaI> mediaList;
		
		public Map<String, List<String>> getFilterParams() {
			return filterParams;
		}
		
		public void setFilterParams( Map<String, List<String>> filterParams ) {
			this.filterParams = filterParams;
		}
		
		public Map<String, FilterInfo> getFilterInfos() {
			return filterInfos;
		}
		
		public void setFilterInfos( Map<String, FilterInfo> filterInfos ) {
			this.filterInfos = filterInfos;
		}
		
		public int getResultSize() {
			if ( multiList == null )
				return 0;
			
			int size = 0;
			for ( List<? extends ProductModel> l : multiList ) {
				size += l.size();
			}
			return size;
		}
		
		public int getSectionCount() {
			return multiList.size();
		}
		
		public List<List<? extends ProductModel>> getMultiList() {
			return multiList;
		}
		
		public void setSingleList( List<? extends ProductModel> singleList ) {
			this.multiList = new ArrayList<List<? extends ProductModel>>();
			this.multiList.add( singleList );
			this.mediaList = new ArrayList<MediaI>();
			this.mediaList.add( null );
		}
		
		public void setMultiList( List<List<? extends ProductModel>> multiList ) {
			this.multiList = multiList;
		}
		
		public List<MediaI> getMediaList() {
			return mediaList;
		}
		
		public void setMediaList( List<MediaI> mediaList ) {
			this.mediaList = mediaList;
		}
	}

	
	// ======================
	// initialization methods
	// ======================	
	
	
	// First time init.
	static { 
		try {
			initLazy(); 
		} catch (Exception e) {
			lastInitTimeStamp = 0;
			LOGGER.error( "Four-minute-meals helper cache initialization failed. Please check CMS data.", e );
		}
	}
	

	public static synchronized void initLazy() {
		
		// check if we have initialized ever
		if ( lastInitTimeStamp == 0 ) {
			LOGGER.info( "Starting first time initialization." );
			try {
				init(); 
			} catch (Exception e) {
				lastInitTimeStamp = 0;
				LOGGER.error( "Four-minute-meals helper cache initialization failed. Nothing will work. Please check CMS data.", e );
			}
			return;
		}
		
		// check timestamp if we need to reinitialize
		long now = System.currentTimeMillis();		
		LOGGER.debug( "Last refresh was " + (now-lastInitTimeStamp)/1000.0f + " seconds ago." );
		if ( now - lastInitTimeStamp > initFrequency ) {
			LOGGER.info( "Starting reinitialization." );
			try {
				init();
			} catch (Exception e) {
				LOGGER.error( "Four-minute-meals helper cache reloading failed. Cache contents were not updated. Please check CMS data.", e );
			}
		}
	}
	
	@SuppressWarnings( "unchecked" )
	private static synchronized void init() {
		
		LOGGER.info( "Building 4mm helper cache." );
		long startTime = System.currentTimeMillis();
		
		// === Basic initializing ===
		FilterCache newCache = new FilterCache();
		
		// get 4mm department
		newCache.department = (DepartmentModel)ContentFactory.getInstance().getContentNodeByKey( departmentKey );
		// 4mm editorial recommender
		newCache.editorial = (CategoryModel)ContentFactory.getInstance().getContentNodeByKey( editorialKey );
		// 4mm pages
		newCache.aboutPage = (CategoryModel) ContentFactory.getInstance().getContentNodeByKey( aboutPageKey ); 
		newCache.filterPage = (CategoryModel) ContentFactory.getInstance().getContentNodeByKey( filterPageKey );

		// section header media
		if ( newCache.filterPage != null ) {
			List<? extends MediaI> sectionHeaders = newCache.filterPage.getTopMedia();
			if ( sectionHeaders != null && sectionHeaders.size() == 2 ) {
				newCache.mealHeaderMedia = sectionHeaders.get( 0 );
				newCache.sideHeaderMedia = sectionHeaders.get( 1 );
			}
		}
		
		// filtering 
		newCache.allProducts = new ArrayList<ProductModel>();
		newCache.sideProducts = new ArrayList<ProductModel>();
		newCache.realSiasProducts = new ArrayList<ProductModel>();
		newCache.restaurantProducts = new HashMap<String, List<ProductModel>>();
		newCache.restaurantFilterIds = new ArrayList<String>();
		newCache.filterInfos = new HashMap<String, FilterInfo>();
		newCache.restaurantCounts = new HashMap<String, FilterInfo>();		
		newCache.restaurantMedia = new ArrayList<MediaI>();
		
		newCache.extraVegetableSides = new ArrayList<ProductModel>();
		newCache.extraStarchSides = new ArrayList<ProductModel>();
		newCache.siasVegetableSides = new ArrayList<ProductModel>(); 
		newCache.siasStarchSides = new ArrayList<ProductModel>(); 
		
		// === Restaurants initializing ===

		// grab the restaurants
		try {
			ItemGrabber deptGrabber = new ItemGrabber();
			deptGrabber.setDepth( 0 );
			deptGrabber.setRootNode( newCache.department );
			newCache.restaurants = (List<CategoryModel>)(List<?>)deptGrabber.grabTheItems();
		} catch (FDResourceException e) {
			LOGGER.error( "Failed to get 4mm department children categories", e );
		} catch ( FDSkuNotFoundException e ) {
			LOGGER.error( "Failed to get 4mm department children categories", e );
		}		 
		
		// grab all the items, assort items by restaurant, filter out entrees
		newCache.totalCount = 0;
		if ( newCache.restaurants != null ) {
			for ( CategoryModel restaurant : newCache.restaurants ) {
				String id = restaurant.getContentKey().getId();
				newCache.restaurantFilterIds.add( id );
				
				boolean isSidesInASnap = isSidesInASnapCategory( restaurant );
				
				// grab the contents
				ItemGrabber prodsGrabber = new ItemGrabber();
				prodsGrabber.setDepth( 2 );
				prodsGrabber.setIgnoreShowChildren( true );
				prodsGrabber.setRootNode( restaurant );
				prodsGrabber.setReturnInvisibleProducts( false );
				prodsGrabber.setFilterDiscontinued( true );
				prodsGrabber.setReturnHiddenFolders( true );
				
				List <ContentNodeModel> nodes = null;
				try {
					nodes = prodsGrabber.grabTheItems();					
				} catch ( FDResourceException e ) {
					LOGGER.error( "ItemGrabber failed.", e );
				} catch ( FDSkuNotFoundException e ) {
					LOGGER.error( "ItemGrabber failed.", e );
				}
				List<ProductModel> prods = new ArrayList<ProductModel>();
				CategoryModel currentCategory = null;
				int catCounter = 0;
				boolean isCatHidden = false;
				if ( nodes != null ) {
					for ( ContentNodeModel node : nodes ) {
						// only first two subcategories are counted, not the entrees
						if ( node instanceof ProductModel && catCounter <= 2 ) {
							if ( isCatHidden )
								continue;
							
							// get the real product node without pricing context if it has one
							ProductModel prod = 
								node instanceof ProductModelPricingAdapter ? 
								((ProductModelPricingAdapter)node).getRealProduct() : 
								(ProductModel)node;
								
							// skip if not available
							if ( !prod.isFullyAvailable() || prod.isTempUnavailable() ) {
								continue;
							}
							
							prods.add( prod );
							newCache.allProducts.add( prod );

							
							// fetch sides in a snap products and media
							if ( isSidesInASnap ) {
								if ( currentCategory.equals( newCache.sideVeggieCategory ) ) {
									newCache.siasVegetableSides.add( prod );
									List<Html> media = currentCategory.getTopMedia();
									if ( media != null && !media.isEmpty() ) {
										if (media.size() > 1)
											newCache.siasVegetableMedia = media.get( 1 );
										else
											newCache.siasVegetableMedia = media.get( 0 );											
									} else {
										newCache.siasVegetableMedia = null;
									}
								} else if ( currentCategory.equals( newCache.sideStarchCategory ) ) {
									newCache.siasStarchSides.add( prod );
									List<Html> media = currentCategory.getTopMedia();
									if ( media != null && !media.isEmpty() ) {
										if (media.size() > 1)
											newCache.siasStarchMedia = media.get( 1 );
										else
											newCache.siasStarchMedia = media.get( 0 );
									} else {
										newCache.siasStarchMedia = null;
									}
								}  
							}
									
							// second subcategory is sides, all subcategory of sides.ias. are sides 
							if ( catCounter == 2 || isSidesInASnap ) {
								newCache.sideProducts.add( prod );								
							}
							//collect real sides in a snap to avoid duplicates later 
							if ( isSidesInASnap ) {
								newCache.realSiasProducts.add( prod );
							}
							
						} else if ( node instanceof CategoryModel ) {
							catCounter++;
							currentCategory = (CategoryModel)node;
							
							isCatHidden = !currentCategory.getShowSelf();
							// fetch sides in a snap subcategories
							if ( isSidesInASnap ) {
								if ( catCounter == 1 ) {
									newCache.sideVeggieCategory = (CategoryModel)node;
								} else if ( catCounter == 2 ) {
									newCache.sideStarchCategory = (CategoryModel)node;
								}
							}

						}
					}
				}
				int count = prods.size();
				newCache.restaurantProducts.put( id, prods );			
				newCache.filterInfos.put( id, new FilterInfo( restaurant.getFullName(), count ) );
				newCache.restaurantCounts.put( id, new FilterInfo( restaurant.getAltText(), count ) );
				newCache.totalCount += count;

				// restaurant brand header media
				List<BrandModel> rBrands = restaurant.getFeaturedBrands();
				if ( rBrands != null && rBrands.size() > 0 ) {
					MediaI sectionHeader = rBrands.get( 0 ).getLogoLarge();
					newCache.restaurantMedia.add( sectionHeader );
				} else {
					newCache.restaurantMedia.add( null );
				}
			}
		}
		
		// === Nutrition and Ingredients initializing ===
		
		// basic nutrition initializing
		newCache.nutritionProducts = new HashMap<String, List<ProductModel>>();	
		
		for ( String claimId : nutritionFilterIds ) {
			newCache.nutritionProducts.put( claimId, new ArrayList<ProductModel>() );
		}
				
		// basic ingredients initializing
		newCache.ingredientsFilterIds = new ArrayList<String>();
		newCache.ingredientProducts = new HashMap<String, List<ProductModel>>();	
		
		newCache.ingredientsDomain = (Domain)ContentFactory.getInstance().getContentNodeByKey( ingredientdomainKey );
		newCache.ingredientsDomainValues = newCache.ingredientsDomain.getDomainValues();

		for ( DomainValue dv : newCache.ingredientsDomainValues ) {
			String id = dv.getID();
			newCache.ingredientsFilterIds.add( id );
			newCache.ingredientProducts.put( id, new ArrayList<ProductModel>() );
		}

		// domain for side type vegetable/starch
		newCache.extraVeggieSideDV = (DomainValue)ContentFactory.getInstance().getContentNodeByKey( extraVeggieSideDVKey );
		newCache.extraStarchSideDV = (DomainValue)ContentFactory.getInstance().getContentNodeByKey( extraStarchSideDVKey );
		
		newCache.calories = new TreeMap<ProductModel, Double>(ProductModel.DEPTFULL_COMPARATOR);
		
		// assort products		
		for ( ProductModel prod : newCache.allProducts ) {
			
			// assort by nutrition
			SkuModel sku = prod.getDefaultSku();
			if ( sku != null ) {
				FDProduct fdprod;
				try {
					fdprod = sku.getProduct();
					
					putCaloriesValue(prod, fdprod, newCache);
					
					List<EnumClaimValue> claims = fdprod.getClaims();
					if ( claims != null ) {
						for ( EnumClaimValue claim : claims ) {
							int nuNdx = nutritionClaims.indexOf( claim );
							if ( nuNdx != -1 ) {
								EnumClaimValue c = nutritionClaims.get( nuNdx );
								newCache.nutritionProducts.get( c.getCode() ).add( prod );
							}
						}
					}
				} catch ( FDResourceException e ) {
					LOGGER.warn( "Error getting FDProduct from Sku", e );
				} catch ( FDSkuNotFoundException e ) {
					LOGGER.warn( "Error getting FDProduct from Sku", e );
				}
			}
			
			// assort by ingredients
			List<DomainValue> ingredients = prod.getRating();				
			for ( DomainValue dv : ingredients ) {
				Domain d = dv.getDomain();
				// ingredients
				if ( newCache.ingredientsDomain.equals( d ) ) {
					newCache.ingredientProducts.get( dv.getContentKey().getId() ).add( prod );
				}
				if ( !newCache.realSiasProducts.contains(prod) ) {
					// sides in a snap extra items
					if ( newCache.extraVeggieSideDV.equals( dv ) ) {
						newCache.extraVegetableSides.add( prod );
					} else if ( newCache.extraStarchSideDV.equals( dv ) ) {
						newCache.extraStarchSides.add( prod );
					}
				}
			}	
		}
		
		// === Price initializing ===
				
		// scan prices for max price filter		
		List<ProductModelPricingAdapter> pricedProducts = new ArrayList<ProductModelPricingAdapter>( newCache.allProducts.size() );
		for ( ProductModel p : newCache.allProducts ) {
			pricedProducts.add( ProductPricingFactory.getInstance().getPricingAdapter( p, PricingContext.DEFAULT ) );
		}		
		double maxPrice = Collections.max( pricedProducts, PRICE_COMPARATOR_ASC ).getPriceCalculator().getDefaultPriceValue();

		
		// init price filter infos
		newCache.priceFilterIds = new ArrayList<String>( defaultPriceFilterIds.size() );		
		int i = 0;
		for ( String prId : defaultPriceFilterIds ) {
			if ( priceLimits[i] < maxPrice ) {
				newCache.priceFilterIds.add( prId );
				newCache.filterInfos.put( prId, new FilterInfo( "Under $" + (int)priceLimits[i++], 1 ) );
			}
		}
		// add one extra price filter which is over the max price (explicitly requested to work this way)
		if ( i < defaultPriceFilterIds.size() ) {
			String prId = defaultPriceFilterIds.get( i );
			newCache.priceFilterIds.add( prId );
			newCache.filterInfos.put( prId, new FilterInfo( "Under $" + (int)priceLimits[i], 1 ) );
		}
		
		// init calories filter infos
		newCache.caloriesFilterIds = new ArrayList<String>( defaultCaloriesFilterIds.size() );		
		i = 0;
		for ( String prId : defaultCaloriesFilterIds ) {
			newCache.caloriesFilterIds.add( prId );
			newCache.filterInfos.put( prId, new FilterInfo( (int)caloriesLimits[i++] + " cal or less", 1 ) );
		}

		// count items for nutrition
		for ( EnumClaimValue claim : nutritionClaims ) {
			String id = claim.getCode();
			int count = newCache.nutritionProducts.get( id ).size();
			newCache.filterInfos.put( id, new FilterInfo( claim.getName() , count ) );
		}
		
		// count items for ingredients
		for ( DomainValue dv : newCache.ingredientsDomainValues ) {
			String id = dv.getContentKey().getId();
			int count = newCache.ingredientProducts.get( id ).size();
			newCache.filterInfos.put( id, new FilterInfo( dv.getLabel(), count ) );
		}
		
		// change the cache to the updated one
		cache = newCache;
		
		// update timestamp of last init
		long endTime = System.currentTimeMillis();
		
		lastInitTimeStamp = endTime;
		LOGGER.info( "4mm helper cache built successfully. Took " + (endTime-startTime)/1000.0f + " seconds." );
	}

	/**
	 * 	Filters 4mm products according to the url parameters contained in the pagecontext.
	 *  Parses the url parameters, filters the items and creates FilterInfos for the filter page.
	 * 
	 * @param pageContext	jsp page context
	 * @param pricingContext ?
	 * @return FilterResult containing the filtered items, the parsed filter parameters, and new FilterInfos to display.
	 */
	public static FilterResult filterItems( PageContext pageContext, PricingContext pricingContext, FDUserI customer ) {
		
		// init if needed
		initLazy();
		
		LOGGER.debug(  "4mm filtering started." );
		long startTime = System.currentTimeMillis();
//		long time = startTime;

		boolean filterForSides = false;
		
		FilterResult result = new FilterResult();
		
		Map<String,List<String>> filters = parseUrlParams( pageContext );
		result.setFilterParams( filters );
		
		List<String> restaurantFilters = filters.get( filterParamRestaurant );
		List<String> nutritionFilters = filters.get( filterParamNutrition );
		List<String> ingredientsFilters = filters.get( filterParamIngredients );
		List<String> priceFilters = filters.get( filterParamPrice );
		List<String> caloriesFilters = filters.get( filterParamCalories );
		
		// do the filtering
		List<ProductModel> workSet = new ArrayList<ProductModel>();
		if ( restaurantFilters != null && restaurantFilters.size() > 0 ) {
			for ( String fRes : restaurantFilters ) {
				
				workSet.addAll( cache.restaurantProducts.get( fRes ) );
				
				// Add the extra sides 
				if ( restaurantFilters.get( 0 ).equals( sidesInASnapKey.getId() ) ) {
					workSet.addAll( cache.extraVegetableSides );
					workSet.addAll( cache.extraStarchSides );
					filterForSides = true;
				}
			}
		} else {
			workSet.addAll( cache.allProducts );
		}
		
		if ( nutritionFilters != null && nutritionFilters.size() > 0 ) {
			// 'OR'ing selected sets - form union of filtered sets
			Set<ProductModel> nutSet = new HashSet<ProductModel>();
			for ( String fNut : nutritionFilters ) {
				nutSet.addAll( cache.nutritionProducts.get( fNut ) );
			}

			// intersect work set with the union
			workSet.retainAll(nutSet);
		}

		if ( ingredientsFilters != null && ingredientsFilters.size() > 0 ) {
			for ( String fIng : ingredientsFilters ) {
				workSet.retainAll( cache.ingredientProducts.get( fIng ) );
			}
		}

//		LOGGER.debug( "Phase1[base filters] took " + (System.currentTimeMillis()-time)/1000.0f + " seconds." );
//		time = System.currentTimeMillis();
		
		if( caloriesFilters != null && caloriesFilters.size() > 0) {
			// take the first one
			int ndx = cache.caloriesFilterIds.indexOf( caloriesFilters.get( 0 ) );
			double calLimit = 9999.0;
			if ( ndx != -1 )
				calLimit = caloriesLimits[ndx];
			
			for ( int i = workSet.size() - 1; i >= 0; i-- ) {
				ProductModel prod = workSet.get( i );
				Double cal = cache.calories.get(prod);
				if ( cal > calLimit ) {
					workSet.remove( i );
				}
			}
		}
		
		// create product pricing adapters from actual pricing context
		List<ProductModelPricingAdapter> pricedWorkSet = new ArrayList<ProductModelPricingAdapter>( workSet.size() );
		for ( ProductModel prod : workSet ) {
			pricedWorkSet.add( ProductPricingFactory.getInstance().getPricingAdapter( prod , pricingContext ) );
		}

//		LOGGER.debug( "Phase2[pricing adapters] took " + (System.currentTimeMillis()-time)/1000.0f + " seconds." );
//		time = System.currentTimeMillis();


		// filter by price
		if ( priceFilters != null && priceFilters.size() > 0 ) {
			// take the first one
			int ndx = cache.priceFilterIds.indexOf( priceFilters.get( 0 ) );
			double priceLimit = 999.0;
			if ( ndx != -1 )
				priceLimit = priceLimits[ndx];
			
			// remove items from the end of list first
			for ( int i = workSet.size() - 1; i >= 0; i-- ) {
				ProductModelPricingAdapter prod = pricedWorkSet.get( i );
				double price = prod.getPriceCalculator().getDefaultPriceValue();
				if ( price >= priceLimit ) {
					// price exceeds limit, remove it
					pricedWorkSet.remove( i );
					workSet.remove( i );
				}
			}
		}
		
//		LOGGER.debug( "Phase3[price filter] took " + (System.currentTimeMillis()-time)/1000.0f + " seconds." );
//		time = System.currentTimeMillis();
		
		// filtering done, workset contains the result
		
		
		// ====================
		// Update filter infos
		// ====================
		Map<String, FilterInfo> infos = new HashMap<String, FilterInfo>();
		
		if ( filterForSides ) {			
			for ( String rId : cache.restaurantFilterIds ) {
				FilterInfo info = new FilterInfo( cache.filterInfos.get( rId ) );
				info.setCount( rId.equals( sidesInASnapKey.getId() ) ? 1 : 0 );
				infos.put( rId, info );			
			}			
		} else {
			for ( String rId : cache.restaurantFilterIds ) {
				FilterInfo info = new FilterInfo( cache.filterInfos.get( rId ) );
				info.setCount( Collections.disjoint( workSet, cache.restaurantProducts.get( rId ) ) ? 0 : 1 );
				infos.put( rId, info );			
			}
		}

		for ( String nId : nutritionFilterIds ) {
			FilterInfo info = new FilterInfo( cache.filterInfos.get( nId ) );
			info.setCount( Collections.disjoint( workSet, cache.nutritionProducts.get( nId ) ) ? 0 : 1 );
			infos.put( nId, info );
		}

		for ( String iId : cache.ingredientsFilterIds ) {
			FilterInfo info = new FilterInfo( cache.filterInfos.get( iId ) );
			info.setCount( Collections.disjoint( workSet, cache.ingredientProducts.get( iId ) ) ? 0 : 1 );
			infos.put( iId, info );
		}

//		LOGGER.debug( "Phase4[base filter infos] took " + (System.currentTimeMillis()-time)/1000.0f + " seconds." );
//		time = System.currentTimeMillis();

		if ( priceFilters != null && priceFilters.size() > 0 ) {
			String priceFilter = priceFilters.get( 0 );
			for ( String pId : cache.priceFilterIds ) {
				FilterInfo info = new FilterInfo( cache.filterInfos.get( pId ) );
				if ( pId.equals( priceFilter ) ) {
					info.setCount( 1 );
				} else {
					info.setCount( 0 );
				}
				infos.put( pId, info );
			}
		} else {
			double min = Collections.min( pricedWorkSet, PRICE_COMPARATOR_ASC ).getPriceCalculator().getDefaultPriceValue();
			for ( String pId : cache.priceFilterIds ) {
				FilterInfo info = new FilterInfo( cache.filterInfos.get( pId ) );
				double limit = priceLimits[ cache.priceFilterIds.indexOf( pId ) ];
				if ( limit < min ) {
					info.setCount( 0 );
				} else {
					info.setCount( 1 );
				}				
				infos.put( pId, info );
			}			
		}
		
		
		if ( caloriesFilters != null && caloriesFilters.size() > 0 ) {
			//If one of the calories selectors is selected, other ones should be disabled.
			String filter = caloriesFilters.get( 0 );
			for ( String pId : cache.caloriesFilterIds ) {
				FilterInfo info = new FilterInfo( cache.filterInfos.get( pId ) );
				if ( pId.equals( filter ) ) {
					info.setCount( 1 );
				} else {
					info.setCount( 0 );
				}
				infos.put( pId, info );
			}
		} else {
			double min = 999.9d;
			for(ProductModel prod : workSet) {
				min = Math.min(min, cache.calories.get(prod));
			}
			for ( String pId : cache.caloriesFilterIds ) {
				FilterInfo info = new FilterInfo( cache.filterInfos.get( pId ) );
				double limit = caloriesLimits[ cache.caloriesFilterIds.indexOf( pId ) ];
				if ( limit < min ) {
					info.setCount( 0 );
				} else {
					info.setCount( 1 );
				}				
				infos.put( pId, info );
			}
		}

		result.setFilterInfos( infos );
		
//		LOGGER.debug( "Phase5[price filter infos] took " + (System.currentTimeMillis()-time)/1000.0f + " seconds." );
//		time = System.currentTimeMillis();

		
		
		// ===============
		// Sort the items
		// ===============
		
		String sortMode = parseSortParam( pageContext );
		
		if ( sortBrand.equals( sortMode ) ) {
			// already sorted by brand, need to do multiple sections
			
			List<List<? extends ProductModel>> separatedList = new ArrayList<List<? extends ProductModel>>();
			List<MediaI> mediaList = new ArrayList<MediaI>();
			
			int i = 0;
			for ( CategoryModel restaurant : cache.restaurants ) {
				
				// sides in a snap extra tricks
				boolean isSias = isSidesInASnapCategory( restaurant );				
				
				if ( isSias ) {
					// special handling for sides in a snap
					
					List<ProductModel> tempList = new ArrayList<ProductModel>();
					
					// do it twice, once for vegetable sides
					boolean found = false;
					for ( ProductModel prod : pricedWorkSet ) {
						if ( cache.siasVegetableSides.contains( prod ) || filterForSides && cache.extraVegetableSides.contains( prod ) ) {
							tempList.add( prod );
							found = true;
						}
					}
					if ( found ) {
						mediaList.add( cache.siasVegetableMedia );
						separatedList.add( tempList );
					}

					// do it twice, second for starch sides
					tempList = new ArrayList<ProductModel>();
					found = false;
					for ( ProductModel prod : pricedWorkSet ) {
						if ( cache.siasStarchSides.contains( prod ) || filterForSides && cache.extraStarchSides.contains( prod ) ) {
							tempList.add( prod );
							found = true;
						}
					}
					if ( found ) {
						mediaList.add( cache.siasStarchMedia );
						separatedList.add( tempList );
					}
					
					// increment counter once
					i++;
					
				} else if ( !filterForSides ) {
					// regular processing
					List<ProductModel> restaurantList = new ArrayList<ProductModel>();
					boolean found = false;
					for ( ProductModel prod : pricedWorkSet ) {
						if ( cache.restaurantProducts.get( restaurant.getContentKey().getId() ).contains( prod ) ) {
							restaurantList.add( prod );
							found = true;
						}
					}
					if ( found ) {
						mediaList.add( cache.restaurantMedia.get( i ) );
						separatedList.add( restaurantList );
					}
					i++;
				}
			}
			
			// Move favorites to top of every brand-list
			if ( customer != null ) {
				ListIterator<List<? extends ProductModel>> iter1 = separatedList.listIterator();
				while ( iter1.hasNext() ) {					
					List<? extends ProductModel> prodL = iter1.next();
					List<ProductModel> favList = new ArrayList<ProductModel>();
					boolean foundFav = false;
					ListIterator<? extends ProductModel> iter2 = prodL.listIterator();
					while ( iter2.hasNext() ) {
						ProductModel prod = iter2.next();
						if ( DYFUtil.isFavorite(prod, customer) ) {
							favList.add( prod );
							iter2.remove();
							foundFav = true;
						}
					}
					if ( foundFav ) {
						List<ProductModel> newList = new ArrayList<ProductModel>();
						newList.addAll( favList );
						newList.addAll( prodL );
						iter1.set( newList );
					}
				}
			}
			
			result.setMediaList( mediaList );
			result.setMultiList( separatedList );
			
			
		} else if ( sortPopularity.equals( sortMode ) ) {
			Comparator<ProductModel> popComp = ScriptedContentNodeComparator.createShortTermPopularityComparator(null, pricingContext);
			Collections.sort( pricedWorkSet, popComp );
			
			result.setSingleList( pricedWorkSet );
			
		} else if ( sortPriceAsc.equals( sortMode ) || sortPriceDesc.equals( sortMode ) ) {
			Comparator<ProductModelPricingAdapter> prComp = sortPriceAsc.equals( sortMode ) ? PRICE_COMPARATOR_ASC : PRICE_COMPARATOR_DESC;			
			Collections.sort( pricedWorkSet, prComp );
			
			// 2 sections: Meals/Sides						
			List<List<? extends ProductModel>> separatedList = separateMealsAndSides( pricedWorkSet );
			
			List<MediaI> mediaList = new ArrayList<MediaI>(2);
			mediaList.add( cache.mealHeaderMedia );
			mediaList.add( cache.sideHeaderMedia );
			
			result.setMediaList( mediaList );
			result.setMultiList( separatedList );
		} 
		
//		LOGGER.debug( "Phase6[sorting by "+sortMode+"] took " + (System.currentTimeMillis()-time)/1000.0f + " seconds." );
//		time = System.currentTimeMillis();

			
		LOGGER.debug( "4mm filtering done. Returning " + result.getResultSize() + " items. Took " + (System.currentTimeMillis()-startTime)/1000.0f + " seconds." );

		return result;
	}
	
	
	private static List<List<? extends ProductModel>> separateMealsAndSides( List<? extends ProductModel> list ) {
		List<ProductModel> meals = new ArrayList<ProductModel>();
		List<ProductModel> sides = new ArrayList<ProductModel>();
		List<List<? extends ProductModel>> multiList = new ArrayList<List<? extends ProductModel>>( 2 );
		
		for ( ProductModel prod : list ) {
			if ( cache.sideProducts.contains( prod ) ) {
				sides.add( prod );
			} else {
				// if not side should be meal?
				meals.add( prod );
			}
		}
		
		multiList.add( meals );
		multiList.add( sides );		
		return multiList;
	}
	

	
	/**
	 *  Parses the filtering mode out of an 4mm/all.jsp kind url.
	 * 
	 * @param pageContext 	jsp page context
	 * @return the sort mode
	 */
	public static String parseSortParam( PageContext pageContext ) {
		String sortMode = pageContext.getRequest().getParameter( sortParam );
		if ( sortMode == null ) {
			sortMode = sortBrand;
		}
		return sortMode;
	}
	
	/**
	 * Parses filtering parameters out of an 4mm/all.jsp kind url.
	 * 
	 * @param pageContext	jsp page context
	 * @return	map of filtering parameters assorted by kind of filtering
	 */
	public static Map<String,List<String>> parseUrlParams( PageContext pageContext ) {
		Map<String,List<String>> paramMap = new HashMap<String, List<String>>(4);
		paramMap.put( filterParamRestaurant, getSelectedIds( filterParamRestaurant, pageContext ) );
		paramMap.put( filterParamNutrition, getSelectedIds( filterParamNutrition, pageContext ) );
		paramMap.put( filterParamIngredients, getSelectedIds( filterParamIngredients, pageContext ) );
		paramMap.put( filterParamPrice, getSelectedIds( filterParamPrice, pageContext ) );
		paramMap.put( filterParamCalories, getSelectedIds( filterParamCalories, pageContext ) );
		return paramMap;
	}
	
	private static List<String> getSelectedIds( String paramName, PageContext pageContext) {
		List<String> selectedIds=null;
		@SuppressWarnings( "unchecked" )
		Map<String,String[]> urlParams = pageContext.getRequest().getParameterMap();
		String[] params = urlParams.get( paramName );
		if(params!=null) {
			selectedIds = Arrays.asList(params);			
		}		
		return selectedIds;
	}
	
	/**
	 * Puts calories value into Map for further use.
	 */
	private static void putCaloriesValue(ProductModel prod, FDProduct fdprod, FilterCache newCache) {
		for(FDNutrition nutr : fdprod.getNutrition()) {
			if("Calories".equals(nutr.getName())) {
				double cal = nutr.getValue();
				newCache.calories.put(prod, cal);
//				System.out.println(prod.getFullName() + " " + cal + " " +  nutr.getUnitOfMeasure());
				break;
			}
		}
	}
	
	public static Double getCaloriesForProductModel(ProductModel pm) {
		return cache.calories.get(pm);
	}
	
}

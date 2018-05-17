package com.freshdirect.smartstore.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.freshdirect.fdstore.content.EnumBurstType;
import com.freshdirect.fdstore.content.RecommenderStrategy;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.framework.util.StringUtil;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.ConfigurationException;
import com.freshdirect.smartstore.ConfigurationStatus;
import com.freshdirect.smartstore.EnumConfigurationState;
import com.freshdirect.smartstore.RecommendationService;
import com.freshdirect.smartstore.RecommendationServiceConfig;
import com.freshdirect.smartstore.RecommendationServiceType;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.dsl.CompileException;
import com.freshdirect.smartstore.impl.AllProductInCategoryRecommendationService;
import com.freshdirect.smartstore.impl.BrandUniquenessSorter;
import com.freshdirect.smartstore.impl.COSFilter;
import com.freshdirect.smartstore.impl.CandidateProductRecommendationService;
import com.freshdirect.smartstore.impl.ClassicYMALRecommendationService;
import com.freshdirect.smartstore.impl.FavoritesRecommendationService;
import com.freshdirect.smartstore.impl.FeaturedItemsRecommendationService;
import com.freshdirect.smartstore.impl.ManualOverrideRecommendationService;
import com.freshdirect.smartstore.impl.MostFrequentlyBoughtDyfVariant;
import com.freshdirect.smartstore.impl.NullRecommendationService;
import com.freshdirect.smartstore.impl.RandomDyfVariant;
import com.freshdirect.smartstore.impl.ScriptedRecommendationService;
import com.freshdirect.smartstore.impl.SmartSavingRecommendationService;
import com.freshdirect.smartstore.impl.SmartYMALRecommendationService;
import com.freshdirect.smartstore.impl.YmalYfRecommendationService;
import com.freshdirect.smartstore.impl.YourFavoritesInCategoryRecommendationService;
import com.freshdirect.smartstore.sampling.ComplicatedImpressionSampler;
import com.freshdirect.smartstore.sampling.ConfiguredImpressionSampler;
import com.freshdirect.smartstore.sampling.ConsiderationLimit;
import com.freshdirect.smartstore.sampling.ImpressionSampler;
import com.freshdirect.smartstore.sampling.ListSampler;
import com.freshdirect.smartstore.sampling.SimpleLimit;

public class RecommendationServiceFactory {
	
	private static final Logger LOGGER = LoggerFactory.getInstance(RecommendationServiceFactory.class);

	public static final String CKEY_SAMPLING_STRATEGY = "sampling_strat";
	public static final String CKEY_TOP_PERC = "top_perc";
	public static final String CKEY_TOP_N = "top_n";
	public static final String CKEY_EXPONENT = "exponent";

	public static final String CKEY_PREZ_DESC = "prez_desc";
	public static final String CKEY_PREZ_TITLE = "prez_title";
	public static final String CKEY_PREZ_FOOTER = "prez_footer";
	public static final String CKEY_FI_LABEL = "fi_label";
	
	public static final String CKEY_SHOW_TEMP_UNAVAILABLE = "show_temp_unavailable";
	public static final String CKEY_BRAND_UNIQ_SORT = "brand_uniq_sort";

	public static final String CKEY_CAT_AGGR = "cat_aggr";
	public static final String CKEY_INCLUDE_CART_ITEMS = "include_cart_items";

	@Deprecated
	public static final String CKEY_SMART_SAVE = "smart_saving";
	public static final String CKEY_COS_FILTER = "cos_filter";
	public static final String CKEY_BRAND_SORTER = "brand_uniq_sort";
	public static final String CKEY_FAVORITE_LIST_ID = "favorite_list_id";

	public static final String CKEY_GENERATOR = "generator";
	public static final String CKEY_SCORING = "scoring";

	// APPREQ-734
	public static final String CKEY_USE_ALTS = "use_alternatives";
	
	// APPREQ-735 - list of burst enums separated by comma
	public static final String CKEY_HIDE_BURSTS = "hide_bursts";
	
	// APPDEV-2320 - determine cart'n'tabs look
	// valid values: "tabs" - tabbed look (default) / "flat" - flattened look
	public static final String CKEY_TABS_LOOK = "tabs_ui_look";
	
	
	public static final Map<String,String> CONFIG_LABELS = new HashMap<String,String>();

	static {
		CONFIG_LABELS.put(CKEY_SAMPLING_STRATEGY, "Sampling Strategy");
		CONFIG_LABELS.put(CKEY_TOP_PERC, "Top %");
		CONFIG_LABELS.put(CKEY_TOP_N, "Top N");
		CONFIG_LABELS.put(CKEY_EXPONENT, "Exponent");
		CONFIG_LABELS.put(CKEY_PREZ_DESC, "Presentation Description");
		CONFIG_LABELS.put(CKEY_PREZ_TITLE, "Presentation Title");
		CONFIG_LABELS.put(CKEY_PREZ_FOOTER, "Presentation Footer");
		CONFIG_LABELS.put(CKEY_FI_LABEL, "Featured Items Label");
		CONFIG_LABELS.put(CKEY_CAT_AGGR, "Category Aggregation");
		CONFIG_LABELS.put(CKEY_INCLUDE_CART_ITEMS, "Include Cart Items");
		CONFIG_LABELS.put(CKEY_SMART_SAVE, "Smart Savings");
		CONFIG_LABELS.put(CKEY_COS_FILTER, "COS Filter");
		CONFIG_LABELS.put(CKEY_BRAND_SORTER, "Brand Uniqueness Sorter");
		CONFIG_LABELS.put(CKEY_FAVORITE_LIST_ID, "Favorite List Id");
		CONFIG_LABELS.put(CKEY_GENERATOR, "Generator Function");
		CONFIG_LABELS.put(CKEY_SCORING, "Scoring Function");
		CONFIG_LABELS.put(CKEY_USE_ALTS, "Use Alternative Products");
		CONFIG_LABELS.put(CKEY_HIDE_BURSTS, "Hide Bursts");
		CONFIG_LABELS.put(CKEY_SHOW_TEMP_UNAVAILABLE, "Show Temporary Unavailable Products");
		CONFIG_LABELS.put(CKEY_TABS_LOOK, "Tab Look & Feel");
	}

	public static final int DEFAULT_TOP_N = 20;
	public static final double DEFAULT_TOP_P = 20.0;
	public static final String DEFAULT_SAMPLING_STRATEGY = "deterministic";
	public static final double DEFAULT_EXPONENT = 0.66;

	public static final String DEFAULT_FI_LABEL = "Our Favorites";

	public static final boolean DEFAULT_CAT_AGGR = false;
	public static final boolean DEFAULT_INCLUDE_CART_ITEMS = false;
	public static final boolean DEFAULT_SMART_SAVE = false;
	public static final String DEFAULT_COS_FILTER = null;
	public static final boolean DEFAULT_BRAND_SORTER = false;
	public static final String DEFAULT_FAVORITE_LIST_ID = "fd_favorites";
	
	public static final String DEFAULT_TABS_LOOKNFEEL = "tabs";

	public static final boolean DEFAULT_USE_ALTS = true;
	public static final Map<EnumSiteFeature,Set<EnumBurstType>> DEF_HBSETS = new HashMap<EnumSiteFeature, Set<EnumBurstType>>();

	static {
		Set<EnumBurstType> hb = new HashSet<EnumBurstType>(1);
		hb.add(EnumBurstType.YOUR_FAVE);
		DEF_HBSETS.put(EnumSiteFeature.DYF, hb);
	}
	
	// Valid sampler names
	public static final String SAMPLERS[] = { "deterministic", "uniform",
			"linear", "quadratic", "cubic", "harmonic", "sqrt", "power",
			"complicated" };

	/**
	 * Configure a service for the variant.
	 * 
	 * Returns the appropriate {@link RecommendationService} configured or
	 * throws a runtime exception.
	 * 
	 * @param variant
	 *            variant
	 * @throws CompileException
	 */
	public static RecommendationService configure(Variant variant) {
		RecommendationService service;
		RecommendationServiceConfig serviceConfig = variant.getServiceConfig();
		RecommendationServiceType serviceType = serviceConfig.getType();
		boolean includeCartItems = DEFAULT_INCLUDE_CART_ITEMS;
		boolean smartSave = DEFAULT_SMART_SAVE;
		String cosFilter = DEFAULT_COS_FILTER;
		boolean brandUniqueness = DEFAULT_BRAND_SORTER;
		
		SortedMap<String,ConfigurationStatus> statuses = serviceConfig.getConfigStatus();
		if (statuses == null)
			serviceConfig.setConfigStatus(statuses = new TreeMap<String, ConfigurationStatus>());

		if (!RecommendationServiceType.TAB_STRATEGY.equals(serviceType)) {
			// if smart saving used, we will return items from the cart.
			includeCartItems = extractIncludeCartItems(serviceConfig, statuses,
					smartSave);
			cosFilter = extractCosFilter(serviceConfig, statuses);
			brandUniqueness = extractBrandUniquenessSorter(serviceConfig,
					statuses);
			extractCartPresentation(serviceConfig, statuses);
			
			Set<EnumBurstType> hb = extractHideBursts(serviceConfig, statuses, variant.getSiteFeature());
			variant.setHideBursts(hb);
		} else {
			if (EnumSiteFeature.CART_N_TABS.equals( variant.getSiteFeature() ) &&
					(variant.getServiceConfig() != null && RecommendationServiceType.TAB_STRATEGY.equals(variant.getServiceConfig().getType()) ) ) {
				// APPDEV-2320
				String cValue = serviceConfig.get(CKEY_TABS_LOOK);

				if (cValue == null) {
					// default look
					statuses.put(CKEY_TABS_LOOK, new ConfigurationStatus(
							CKEY_TABS_LOOK, null,
							EnumConfigurationState.UNCONFIGURED_OK,
							"Default 'tabs' value applied."));
					
					variant.setDefaultTabLook(true); // set default look
				} else if ( DEFAULT_TABS_LOOKNFEEL.equalsIgnoreCase( cValue ) ) {
					statuses.put(CKEY_TABS_LOOK, new ConfigurationStatus(
							CKEY_TABS_LOOK, cValue,
							EnumConfigurationState.CONFIGURED_OK ));

					variant.setDefaultTabLook(true); // set default look
				} else if ("flat".equalsIgnoreCase( cValue )) {
					statuses.put(CKEY_TABS_LOOK, new ConfigurationStatus(
							CKEY_TABS_LOOK, cValue,
							EnumConfigurationState.CONFIGURED_OK ));

					variant.setDefaultTabLook(false); // set new look
				} else {
					statuses.put(CKEY_TABS_LOOK, new ConfigurationStatus(
							CKEY_TABS_LOOK, cValue,
							EnumConfigurationState.CONFIGURED_WRONG ));
				}
			}
		}

		if (EnumSiteFeature.FEATURED_ITEMS.equals(variant.getSiteFeature())) {
			extractFeaturedPresentation(serviceConfig, statuses);
		}

		String favoriteListId = extractFavoriteListId(serviceConfig, statuses);

		ImpressionSampler sampler = null;
		if (!RecommendationServiceType.NIL.equals(serviceType)
				&& !RecommendationServiceType.TAB_STRATEGY.equals(serviceType)
				&& !RecommendationServiceType.SMART_YMAL.equals(serviceType)
				&& !RecommendationServiceType.YMAL_YF.equals(serviceType)) {
			try {
				sampler = configureSampler(serviceConfig, statuses);
			} catch (ConfigurationException e) {
				LOGGER.error("unable to configure sampler", e);
				sampler = null;
			}
		}

		String generator = null;
		String scoring = null;
		if (RecommendationServiceType.SCRIPTED.equals(serviceType)) {
			generator = serviceConfig.get(CKEY_GENERATOR);
			if (generator != null)
				statuses.put(CKEY_GENERATOR, new ConfigurationStatus(
						CKEY_GENERATOR, generator,
						EnumConfigurationState.CONFIGURED_OK));
			else
				statuses.put(CKEY_GENERATOR, new ConfigurationStatus(
						CKEY_GENERATOR, null,
						EnumConfigurationState.UNCONFIGURED_WRONG,
						"Mandatory parameter!"));
			scoring = serviceConfig.get(CKEY_SCORING);
			if (scoring != null)
				statuses.put(CKEY_SCORING, new ConfigurationStatus(
						CKEY_SCORING, scoring,
						EnumConfigurationState.CONFIGURED_OK));
			else
				statuses
						.put(
								CKEY_SCORING,
								new ConfigurationStatus(
										CKEY_SCORING,
										null,
										EnumConfigurationState.UNCONFIGURED_DEFAULT)
										.setWarning("You might have forgotten to set this value."));
		}

		Set<String> unused = new HashSet<String>(serviceConfig.keys());
		unused.removeAll(statuses.keySet());
		Iterator<String> it = unused.iterator();
		while (it.hasNext()) {
			String param = it.next();
			statuses.put(param, new ConfigurationStatus(param, serviceConfig
					.get(param), EnumConfigurationState.CONFIGURED_UNUSED)
					.setWarning("Unused parameter. Maybe mistyped?!?"));
		}

		if (!RecommendationServiceType.NIL.equals(serviceType)
				&& !RecommendationServiceType.TAB_STRATEGY.equals(serviceType)
				&& !RecommendationServiceType.SMART_YMAL.equals(serviceType)
				&& !RecommendationServiceType.YMAL_YF.equals(serviceType)
				&& sampler == null)
			return new NullRecommendationService(variant);

		// If composite
		/* if (RecommendationServiceType.FREQUENTLY_BOUGHT_DYF.equals(serviceType)) {
			service = new MostFrequentlyBoughtDyfVariant(variant, sampler, includeCartItems);
		} else if (RecommendationServiceType.RANDOM_DYF.equals(serviceType)) {
			service = new RandomDyfVariant(variant, sampler, includeCartItems);
		} else */
		if (RecommendationServiceType.NIL.equals(serviceType) || RecommendationServiceType.TAB_STRATEGY.equals(serviceType)) {
			service = new NullRecommendationService(variant);
		} else if (RecommendationServiceType.FEATURED_ITEMS.equals(serviceType)) {
			service = new FeaturedItemsRecommendationService(variant, sampler, includeCartItems);
		} else if (RecommendationServiceType.ALL_PRODUCT_IN_CATEGORY.equals(serviceType)) {
			service = new AllProductInCategoryRecommendationService(variant, sampler, includeCartItems);
		} else if (RecommendationServiceType.FAVORITES.equals(serviceType)) {
			service = new FavoritesRecommendationService(variant, sampler, includeCartItems, favoriteListId);
		} else if (RecommendationServiceType.CANDIDATE_LIST.equals(serviceType)) {
			service = new CandidateProductRecommendationService(variant, sampler, includeCartItems);
		} else if (RecommendationServiceType.YOUR_FAVORITES_IN_FEATURED_ITEMS.equals(serviceType)) {
			service = new YourFavoritesInCategoryRecommendationService(variant, sampler, includeCartItems);
		} else if (RecommendationServiceType.MANUAL_OVERRIDE.equals(serviceType)) {
			service = new ManualOverrideRecommendationService(variant, sampler, includeCartItems);
		} else if (RecommendationServiceType.SCRIPTED.equals(serviceType)) {
			try {
				service = new ScriptedRecommendationService(variant, sampler, includeCartItems, generator, scoring);
			} catch (CompileException e) {
				LOGGER.error("cannot instantiate script recommender - compile error (fall back to NIL): " + variant.getId(), e);
				return new NullRecommendationService(variant);
			} catch (IllegalArgumentException e) {
				LOGGER.error("cannot instantiate script recommender - generator null (fall back to NIL): " + variant.getId(), e);
				return new NullRecommendationService(variant);
			} catch (NullPointerException e) {
				LOGGER.error("cannot instantiate script recommender - generator null (fall back to NIL): " + variant.getId(), e);
				return new NullRecommendationService(variant);
			}
		} else if (RecommendationServiceType.CLASSIC_YMAL.equals(serviceType)) {
			service = new ClassicYMALRecommendationService(variant, sampler, includeCartItems);
		} else if (RecommendationServiceType.SMART_YMAL.equals(serviceType)) {
			service = new SmartYMALRecommendationService(variant, includeCartItems);
		} else if (RecommendationServiceType.YMAL_YF.equals(serviceType)) {
			try {
				service = new YmalYfRecommendationService(variant, includeCartItems);
			} catch (CompileException e) {
				LOGGER.error("cannot instantiate script recommender (fall back to NIL): " + variant.getId(), e);
				return new NullRecommendationService(variant);
			}
		} else {
			service = new NullRecommendationService(variant);
		}

		if (smartSave)
			service = new SmartSavingRecommendationService(service);

		if (brandUniqueness)
			service = new BrandUniquenessSorter(service);

		if (cosFilter != null)
			service = new COSFilter(service, cosFilter);

		return service;
	}

	protected static boolean extractCategoryAggregation(
			RecommendationServiceConfig config, Map<String,ConfigurationStatus> statuses) {
		boolean catAggr = DEFAULT_CAT_AGGR;
		String catAggrStr = config.get(CKEY_CAT_AGGR);
		if (catAggrStr != null) {
			if (catAggrStr.equalsIgnoreCase("yes")
					|| catAggrStr.equalsIgnoreCase("true")
					|| catAggrStr.equals("1")) {
				catAggr = true;
				statuses.put(CKEY_CAT_AGGR, new ConfigurationStatus(
						CKEY_CAT_AGGR, catAggrStr, Boolean.toString(catAggr),
						EnumConfigurationState.CONFIGURED_OK));
			} else
				statuses.put(CKEY_CAT_AGGR, new ConfigurationStatus(
						CKEY_CAT_AGGR, catAggrStr, Boolean
								.toString(DEFAULT_CAT_AGGR),
						EnumConfigurationState.CONFIGURED_WRONG_DEFAULT,
						"Unrecognized value defaulting to "
								+ Boolean.toString(DEFAULT_CAT_AGGR)));
		} else
			statuses.put(CKEY_CAT_AGGR, new ConfigurationStatus(CKEY_CAT_AGGR,
					null, Boolean.toString(DEFAULT_CAT_AGGR),
					EnumConfigurationState.UNCONFIGURED_DEFAULT));
		return catAggr;
	}

	protected static boolean extractIncludeCartItems(
			RecommendationServiceConfig config, Map<String,ConfigurationStatus> statuses, boolean smartSave) {
		boolean includeCartItems = DEFAULT_INCLUDE_CART_ITEMS;
		String iciStr = config.get(CKEY_INCLUDE_CART_ITEMS);
		if (smartSave) {
			includeCartItems = true;
			if (iciStr != null)
				statuses
						.put(
								CKEY_INCLUDE_CART_ITEMS,
								new ConfigurationStatus(
										CKEY_INCLUDE_CART_ITEMS,
										iciStr,
										Boolean.toString(includeCartItems),
										EnumConfigurationState.CONFIGURED_OVERRIDDEN)
										.setWarning("Variant belongs to Smart Savings enabled Site Feature therefore automatically turned on"));
			else
				statuses
						.put(
								CKEY_INCLUDE_CART_ITEMS,
								new ConfigurationStatus(
										CKEY_INCLUDE_CART_ITEMS,
										null,
										Boolean.toString(includeCartItems),
										EnumConfigurationState.UNCONFIGURED_OVERRIDDEN)
										.setWarning("Variant belongs to Smart Savings enabled Site Feature therefore automatically turned on"));
			return includeCartItems;
		}
		if (iciStr != null) {
			if (iciStr.equalsIgnoreCase("yes")
					|| iciStr.equalsIgnoreCase("true") || iciStr.equals("1")) {
				includeCartItems = true;
				statuses.put(CKEY_INCLUDE_CART_ITEMS, new ConfigurationStatus(
						CKEY_INCLUDE_CART_ITEMS, iciStr, Boolean
								.toString(includeCartItems),
						EnumConfigurationState.CONFIGURED_OK));
			} else
				statuses
						.put(
								CKEY_INCLUDE_CART_ITEMS,
								new ConfigurationStatus(
										CKEY_INCLUDE_CART_ITEMS,
										iciStr,
										Boolean
												.toString(DEFAULT_INCLUDE_CART_ITEMS),
										EnumConfigurationState.CONFIGURED_WRONG_DEFAULT,
										"Unrecognized value defaulting to "
												+ Boolean
														.toString(DEFAULT_INCLUDE_CART_ITEMS)));
		} else
			statuses.put(CKEY_INCLUDE_CART_ITEMS, new ConfigurationStatus(
					CKEY_INCLUDE_CART_ITEMS, null, Boolean
							.toString(DEFAULT_INCLUDE_CART_ITEMS),
					EnumConfigurationState.UNCONFIGURED_DEFAULT));
		return includeCartItems;
	}

	
	/**
	 * SmartSavings feature is no longer supported
	 * 
	 * @param config
	 * @param statuses
	 * @param siteFeature
	 * @return
	 */
	@Deprecated
	protected static boolean extractSmartSave(RecommendationServiceConfig config,
			Map<String,ConfigurationStatus> statuses, EnumSiteFeature siteFeature) {
		boolean smartSave = DEFAULT_SMART_SAVE;
		String smartSaveStr = config.get(CKEY_SMART_SAVE);
		if (siteFeature.isSmartSavings()) {
			smartSave = true;
			if (smartSaveStr != null)
				statuses
						.put(
								CKEY_SMART_SAVE,
								new ConfigurationStatus(
										CKEY_SMART_SAVE,
										smartSaveStr,
										Boolean.toString(smartSave),
										EnumConfigurationState.CONFIGURED_OVERRIDDEN)
										.setWarning("Variant belongs to Smart Savings enabled Site Feature therefore automatically turned on"));
			else
				statuses
						.put(
								CKEY_SMART_SAVE,
								new ConfigurationStatus(
										CKEY_SMART_SAVE,
										null,
										Boolean.toString(smartSave),
										EnumConfigurationState.UNCONFIGURED_OVERRIDDEN)
										.setWarning("Variant belongs to Smart Savings enabled Site Feature therefore automatically turned on"));
			return smartSave;
		}
		if (smartSaveStr != null) {
			if (smartSaveStr.equalsIgnoreCase("yes")
					|| smartSaveStr.equalsIgnoreCase("true")
					|| smartSaveStr.equals("1")) {
				smartSave = true;
				statuses.put(CKEY_SMART_SAVE, new ConfigurationStatus(
						CKEY_SMART_SAVE, smartSaveStr, Boolean
								.toString(smartSave),
						EnumConfigurationState.CONFIGURED_OK));
			} else
				statuses.put(CKEY_SMART_SAVE, new ConfigurationStatus(
						CKEY_SMART_SAVE, smartSaveStr, Boolean
								.toString(DEFAULT_SMART_SAVE),
						EnumConfigurationState.CONFIGURED_WRONG_DEFAULT,
						"Unrecognized value defaulting to "
								+ Boolean.toString(DEFAULT_SMART_SAVE)));
		} else
			statuses.put(CKEY_SMART_SAVE, new ConfigurationStatus(
					CKEY_SMART_SAVE, null,
					Boolean.toString(DEFAULT_SMART_SAVE),
					EnumConfigurationState.UNCONFIGURED_DEFAULT));
		return smartSave;
	}

	protected static String extractCosFilter(RecommendationServiceConfig config,
			Map<String,ConfigurationStatus> statuses) {
		String cosFilter = DEFAULT_COS_FILTER;
		String cosFilterStr = config.get(CKEY_COS_FILTER);
		if (cosFilterStr != null) {
			if (cosFilterStr.equalsIgnoreCase("corporate")
					|| cosFilterStr.equalsIgnoreCase("residential")
					|| cosFilterStr.equalsIgnoreCase("home")) {
				cosFilter = cosFilterStr;
				statuses.put(CKEY_COS_FILTER, new ConfigurationStatus(
						CKEY_COS_FILTER, cosFilter.toUpperCase(),
						EnumConfigurationState.CONFIGURED_OK));
			} else
				statuses.put(CKEY_COS_FILTER, new ConfigurationStatus(
						CKEY_COS_FILTER, cosFilterStr, null,
						EnumConfigurationState.CONFIGURED_WRONG_DEFAULT,
						"Unrecognized value defaulting to "
								+ DEFAULT_COS_FILTER));
		} else
			statuses.put(CKEY_COS_FILTER, new ConfigurationStatus(
					CKEY_COS_FILTER, null,
					EnumConfigurationState.UNCONFIGURED_DEFAULT));
		return cosFilter;
	}

	protected static boolean extractBrandUniquenessSorter( RecommendationServiceConfig config, Map<String, ConfigurationStatus> statuses ) {
		boolean brandUniqueness = DEFAULT_BRAND_SORTER;
		String brandUniquenessStr = config.get( CKEY_BRAND_SORTER );
		if ( brandUniquenessStr != null ) {
			if ( brandUniquenessStr.equalsIgnoreCase( "yes" ) || brandUniquenessStr.equalsIgnoreCase( "true" ) || brandUniquenessStr.equals( "1" ) ) {
				brandUniqueness = true;
				statuses.put( CKEY_BRAND_SORTER, 
					new ConfigurationStatus( CKEY_BRAND_SORTER, brandUniquenessStr, Boolean.toString( brandUniqueness ), 
					EnumConfigurationState.CONFIGURED_OK ) );
			} else if ( brandUniquenessStr.equalsIgnoreCase( "no" ) || brandUniquenessStr.equalsIgnoreCase( "false" ) || brandUniquenessStr.equals( "0" ) ) {
				brandUniqueness = false;
				statuses.put( CKEY_BRAND_SORTER, 
					new ConfigurationStatus( CKEY_BRAND_SORTER, brandUniquenessStr, Boolean.toString( brandUniqueness ), 
					EnumConfigurationState.CONFIGURED_OK ) );
			} else {
				statuses.put( CKEY_BRAND_SORTER, 
					new ConfigurationStatus( CKEY_BRAND_SORTER, brandUniquenessStr, Boolean.toString( DEFAULT_BRAND_SORTER ), 
					EnumConfigurationState.CONFIGURED_WRONG_DEFAULT, 
					"Unrecognized value defaulting to " + Boolean.toString( DEFAULT_BRAND_SORTER ) ) );
			}
		} else
			statuses.put( CKEY_BRAND_SORTER, new ConfigurationStatus( CKEY_BRAND_SORTER, null, Boolean.toString( DEFAULT_BRAND_SORTER ), EnumConfigurationState.UNCONFIGURED_DEFAULT ) );
		return brandUniqueness;
	}

	protected static void extractCartPresentation(
			RecommendationServiceConfig config, Map<String,ConfigurationStatus> statuses) {
		ConfigurationStatus status;
		if (config.get(CKEY_PREZ_TITLE) != null)
			status = new ConfigurationStatus(CKEY_PREZ_TITLE, config
					.get(CKEY_PREZ_TITLE), EnumConfigurationState.CONFIGURED_OK);
		else
			status = new ConfigurationStatus(CKEY_PREZ_TITLE, null,
					EnumConfigurationState.UNCONFIGURED_OK)
					.setWarning("Missing value may cause visual issues.");

		statuses.put(CKEY_PREZ_TITLE, status);
		if (config.get(CKEY_PREZ_DESC) != null)
			status = new ConfigurationStatus(CKEY_PREZ_DESC, config
					.get(CKEY_PREZ_DESC), EnumConfigurationState.CONFIGURED_OK);
		else
			status = new ConfigurationStatus(CKEY_PREZ_DESC, null,
					EnumConfigurationState.UNCONFIGURED_OK)
					.setWarning("Missing value may cause visual issues.");
		statuses.put(CKEY_PREZ_DESC, status);

		if (config.get(CKEY_PREZ_FOOTER) != null)
			statuses.put(CKEY_PREZ_FOOTER, new ConfigurationStatus(
					CKEY_PREZ_FOOTER, config.get(CKEY_PREZ_FOOTER),
					EnumConfigurationState.CONFIGURED_OK));
	}

	protected static void extractFeaturedPresentation(
			RecommendationServiceConfig config, Map<String,ConfigurationStatus> statuses) {
		if (config.get(CKEY_FI_LABEL) != null) {
			statuses.put(CKEY_FI_LABEL, new ConfigurationStatus(CKEY_FI_LABEL,
					config.get(CKEY_FI_LABEL),
					EnumConfigurationState.CONFIGURED_OK));
		} else {
			statuses.put(CKEY_FI_LABEL, new ConfigurationStatus(CKEY_FI_LABEL,
					null, DEFAULT_FI_LABEL,
					EnumConfigurationState.UNCONFIGURED_DEFAULT)
					.setWarning("Using default but may be inappropriate."));
			config.set(CKEY_FI_LABEL, DEFAULT_FI_LABEL);
		}
	}

	protected static String extractFavoriteListId(
			RecommendationServiceConfig config, Map<String,ConfigurationStatus> statuses) {
		String favoriteListId = DEFAULT_FAVORITE_LIST_ID;
		if (RecommendationServiceType.FAVORITES.equals(config.getType())) {
			ConfigurationStatus status;
			if (config.get(CKEY_FAVORITE_LIST_ID) != null) {
				favoriteListId = config.get(CKEY_FAVORITE_LIST_ID);
				status = new ConfigurationStatus(CKEY_FAVORITE_LIST_ID,
						favoriteListId, EnumConfigurationState.CONFIGURED_OK);
			} else
				status = new ConfigurationStatus(CKEY_FAVORITE_LIST_ID,
						favoriteListId,
						EnumConfigurationState.UNCONFIGURED_DEFAULT);
			statuses.put(CKEY_FAVORITE_LIST_ID, status);
		}
		return favoriteListId;
	}

	protected static boolean extractUseAlternatives(RecommendationServiceConfig config, Map<String,ConfigurationStatus> statuses) {
		boolean alts = DEFAULT_USE_ALTS; // default value
		
		ConfigurationStatus status;

		String value = config.get(CKEY_USE_ALTS);
		if (value != null) {
			if ("true".equalsIgnoreCase(value)) {
				alts = true;
				status = new ConfigurationStatus(CKEY_USE_ALTS, value, EnumConfigurationState.CONFIGURED_DEFAULT);
			} else if ("false".equalsIgnoreCase(value)) {
				alts = false;
				status = new ConfigurationStatus(CKEY_USE_ALTS, value, EnumConfigurationState.CONFIGURED_OVERRIDDEN);
			} else {
				status = new ConfigurationStatus(CKEY_USE_ALTS, value, EnumConfigurationState.CONFIGURED_WRONG);
			}
		} else {
			status = new ConfigurationStatus(CKEY_USE_ALTS, Boolean.toString(alts), EnumConfigurationState.UNCONFIGURED_DEFAULT);
		}
		
		// record configuration status
		statuses.put(CKEY_USE_ALTS, status);
		
		
		return alts;
	}


	protected static Set<EnumBurstType> extractHideBursts(RecommendationServiceConfig config, Map<String,ConfigurationStatus> statuses, EnumSiteFeature siteFeature) {
		Set<EnumBurstType> options = new HashSet<EnumBurstType>();

		ConfigurationStatus status;


		// default value
		Set<EnumBurstType> defval = null;
		if (DEF_HBSETS.keySet().contains(siteFeature))
			defval = DEF_HBSETS.get(siteFeature);
		
		
		String value = config.get(CKEY_HIDE_BURSTS);
		if (value != null) {
			Set<String> badVals = new HashSet<String>();
			for (String s : value.split(",")) {
				try {
					EnumBurstType b = EnumBurstType.valueOf(s.trim());
					options.add(b);
				} catch(IllegalArgumentException exc) {
					LOGGER.error("Invalid burst type <" + s + "> in hide_burst of variant " + config.getVariantId());
					badVals.add(s);
				}
			}
			

			
			if ( (defval == null && options.size() == 0) || (defval != null && defval.equals(options))) {
				// configured = default
				String newval = defval != null ? StringUtil.join(defval, ",") : "";
				
				status = new ConfigurationStatus(CKEY_HIDE_BURSTS, newval, EnumConfigurationState.CONFIGURED_DEFAULT);
			} else if (badVals.size() > 0) {
				// found bad burst names in config
				status = new ConfigurationStatus(CKEY_HIDE_BURSTS, value, EnumConfigurationState.CONFIGURED_WRONG);
			} else {
				// good!
				status = new ConfigurationStatus(CKEY_HIDE_BURSTS, value, EnumConfigurationState.CONFIGURED_OVERRIDDEN);
			}
		} else {
			if (defval != null)
				options = defval;
			String newval = defval != null ? StringUtil.join(defval, ",") : "";
			status = new ConfigurationStatus(CKEY_HIDE_BURSTS, newval, EnumConfigurationState.UNCONFIGURED_DEFAULT);
		}
		
		// record configuration status
		statuses.put(CKEY_HIDE_BURSTS, status);

		return options;
	}
	
	public static ImpressionSampler configureSampler(
			RecommendationServiceConfig config, Map<String,ConfigurationStatus> statuses) {
		ImpressionSampler sampler;
		Random R = new Random();

		// log configuration
		LOGGER.debug("configuration=" + config.toString());

		int topN = extractTopN(config, statuses);
		double topP = extractTopPercentage(config, statuses);

		final ConsiderationLimit cl = new SimpleLimit(topP, topN);

		String samplingStrategy = extractSamplingStrategy(config, statuses);
		double exponent = extractExponent(config, statuses, samplingStrategy);

		boolean catAggr = extractCategoryAggregation(config, statuses);
		
		boolean useAlternatives = extractUseAlternatives(config, statuses);
		
		if ("deterministic".equals(samplingStrategy)) {
			sampler = new ConfiguredImpressionSampler(cl, catAggr, useAlternatives,
					ListSampler.ZERO);
		} else if ("uniform".equals(samplingStrategy)) {
			sampler = new ConfiguredImpressionSampler(cl, catAggr, useAlternatives,
					new ListSampler.Uniform(R));
		} else if ("linear".equals(samplingStrategy)) {
			sampler = new ConfiguredImpressionSampler(cl, catAggr, useAlternatives,
					new ListSampler.Linear(R));
		} else if ("quadratic".equals(samplingStrategy)) {
			sampler = new ConfiguredImpressionSampler(cl, catAggr, useAlternatives,
					new ListSampler.Quadratic(R));
		} else if ("cubic".equals(samplingStrategy)) {
			sampler = new ConfiguredImpressionSampler(cl, catAggr, useAlternatives,
					new ListSampler.Cubic(R));
		} else if ("harmonic".equals(samplingStrategy)) {
			sampler = new ConfiguredImpressionSampler(cl, catAggr, useAlternatives,
					new ListSampler.Harmonic(R));
		} else if ("sqrt".equals(samplingStrategy)) {
			sampler = new ConfiguredImpressionSampler(cl, catAggr, useAlternatives,
					new ListSampler.SquareRootCDF(R));
		} else if ("power".equals(samplingStrategy)) {
			sampler = new ConfiguredImpressionSampler(cl, catAggr, useAlternatives,
					new ListSampler.PowerCDF(R, exponent));
		} else if ("complicated".equals(samplingStrategy)) {
			sampler = new ComplicatedImpressionSampler(cl, catAggr, useAlternatives);
		} else {
			LOGGER.warn("Invalid strategy: " + samplingStrategy);
			ConfigurationStatus status = statuses.get(CKEY_SAMPLING_STRATEGY);
			throw new ConfigurationException(CKEY_SAMPLING_STRATEGY, status.getState());
		}

		// log sampler
		LOGGER.debug("Configured sampler: " + sampler);

		return sampler;
	}

	protected static int extractTopN(RecommendationServiceConfig config,
			Map<String,ConfigurationStatus> statuses) {
		int topN = DEFAULT_TOP_N;
		ConfigurationStatus status;
		try {
			topN = Integer.parseInt(config.get(CKEY_TOP_N, Integer
					.toString(DEFAULT_TOP_N)));
			if (config.get(CKEY_TOP_N) != null) {
				if (topN == DEFAULT_TOP_N)
					status = new ConfigurationStatus(CKEY_TOP_N, Integer
							.toString(DEFAULT_TOP_N),
							EnumConfigurationState.CONFIGURED_DEFAULT);
				else
					status = new ConfigurationStatus(CKEY_TOP_N, Integer
							.toString(topN),
							EnumConfigurationState.CONFIGURED_OK);
			} else {
				status = new ConfigurationStatus(CKEY_TOP_N, null, Integer
						.toString(DEFAULT_TOP_N),
						EnumConfigurationState.UNCONFIGURED_DEFAULT);
			}
		} catch (NumberFormatException e) {
			status = new ConfigurationStatus(CKEY_TOP_N,
					config.get(CKEY_TOP_N), Integer.toString(DEFAULT_TOP_N),
					EnumConfigurationState.CONFIGURED_WRONG_DEFAULT)
					.setWarning("Integer cannot be parsed, using default value");
		}
		statuses.put(CKEY_TOP_N, status);
		LOGGER.debug("  TOP N: " + topN);
		return topN;
	}

	protected static double extractTopPercentage(RecommendationServiceConfig config,
			Map<String, ConfigurationStatus> statuses) {
		double topP;
		ConfigurationStatus status;
		if (config.get(CKEY_TOP_PERC) != null) {
			if (RecommendationServiceType.ALL_PRODUCT_IN_CATEGORY.equals(config.getType())
					|| RecommendationServiceType.CLASSIC_YMAL.equals(config.getType())) {
				topP = 100.0;
				status = new ConfigurationStatus(CKEY_TOP_PERC, Double.toString(topP),
						EnumConfigurationState.CONFIGURED_OVERRIDDEN);
			} else {
				try {
					topP = Double.parseDouble(config.get(CKEY_TOP_PERC,
							Double.toString(DEFAULT_TOP_P)));
					if (topP == DEFAULT_TOP_P)
						status = new ConfigurationStatus(CKEY_TOP_PERC,
								Double.toString(DEFAULT_TOP_P),
								EnumConfigurationState.CONFIGURED_DEFAULT);
					else
						status = new ConfigurationStatus(CKEY_TOP_PERC,
								Double.toString(topP),
								EnumConfigurationState.CONFIGURED_OK);
				} catch (NumberFormatException e) {
					topP = DEFAULT_TOP_P;
					status = new ConfigurationStatus(CKEY_TOP_PERC, config.get(CKEY_TOP_PERC),
							Double.toString(DEFAULT_TOP_P),
							EnumConfigurationState.CONFIGURED_WRONG_DEFAULT)
							.setWarning("Float cannot be parsed, using default value");
				}
			}
		} else {
			if (RecommendationServiceType.ALL_PRODUCT_IN_CATEGORY.equals(config.getType())
					|| RecommendationServiceType.CLASSIC_YMAL.equals(config.getType())) {
				topP = 100.0;
				status = new ConfigurationStatus(CKEY_TOP_PERC, Double.toString(topP),
						EnumConfigurationState.UNCONFIGURED_OVERRIDDEN);
			} else {
				topP = DEFAULT_TOP_P;
				status = new ConfigurationStatus(CKEY_TOP_PERC,
						Double.toString(DEFAULT_TOP_P),
						EnumConfigurationState.UNCONFIGURED_DEFAULT);
			}
		}
		statuses.put(CKEY_TOP_PERC, status);
		LOGGER.debug("  TOP %: " + topP);
		return topP;
	}

	protected static String extractSamplingStrategy(
			RecommendationServiceConfig config, Map<String,ConfigurationStatus> statuses) {
		ConfigurationStatus status;
		String samplingStrategy = config.get(CKEY_SAMPLING_STRATEGY);
		if (samplingStrategy == null) {
			/* if (RecommendationServiceType.RANDOM_DYF.equals(config.getType())) {
				samplingStrategy = "uniform";
				status = new ConfigurationStatus(CKEY_SAMPLING_STRATEGY, null,
						samplingStrategy,
						EnumConfigurationState.UNCONFIGURED_OVERRIDDEN);
			} else */ if (RecommendationServiceType.FAVORITES.equals(config.getType())
					|| RecommendationServiceType.CLASSIC_YMAL.equals(config.getType())
					|| RecommendationServiceType.ALL_PRODUCT_IN_CATEGORY.equals(
							config.getType())) {
				samplingStrategy = "deterministic";
				status = new ConfigurationStatus(CKEY_SAMPLING_STRATEGY, null,
						samplingStrategy,
						EnumConfigurationState.UNCONFIGURED_OVERRIDDEN);
			} else {
				samplingStrategy = DEFAULT_SAMPLING_STRATEGY;
				status = new ConfigurationStatus(CKEY_SAMPLING_STRATEGY, null,
						samplingStrategy,
						EnumConfigurationState.UNCONFIGURED_DEFAULT);
			}
		} else {
			samplingStrategy = samplingStrategy.toLowerCase();
			/* if (RecommendationServiceType.RANDOM_DYF.equals(config.getType())) {
				status = new ConfigurationStatus(CKEY_SAMPLING_STRATEGY,
						samplingStrategy, "uniform",
						EnumConfigurationState.CONFIGURED_OVERRIDDEN);
				samplingStrategy = "uniform";
			} else */ if (RecommendationServiceType.FAVORITES.equals(config.getType())
					|| RecommendationServiceType.CLASSIC_YMAL.equals(config.getType())
					|| RecommendationServiceType.ALL_PRODUCT_IN_CATEGORY.equals(
							config.getType())) {
				status = new ConfigurationStatus(CKEY_SAMPLING_STRATEGY,
						samplingStrategy, "deterministic",
						EnumConfigurationState.CONFIGURED_OVERRIDDEN);
				samplingStrategy = "deterministic";
			} else {
				boolean found = false;
				for (int i = 0; i < SAMPLERS.length; i++)
					if (samplingStrategy.equals(SAMPLERS[i])) {
						found = true;
						break;
					}
				if (found)
					status = new ConfigurationStatus(CKEY_SAMPLING_STRATEGY,
							samplingStrategy,
							EnumConfigurationState.CONFIGURED_OK);
				else
					status = new ConfigurationStatus(CKEY_SAMPLING_STRATEGY,
							samplingStrategy,
							EnumConfigurationState.CONFIGURED_WRONG,
							"Possible values are: "
									+ ArrayUtils.toString(SAMPLERS));
			}
		}
		statuses.put(CKEY_SAMPLING_STRATEGY, status);
		LOGGER.debug("  Sampling Strategy: " + samplingStrategy);
		return samplingStrategy;
	}

	protected static double extractExponent(RecommendationServiceConfig config,
			Map<String,ConfigurationStatus> statuses, String samplingStrategy) {
		String exponentStr = config.get(CKEY_EXPONENT);
		double exponent = DEFAULT_EXPONENT;
		ConfigurationStatus status = null;
		if ("power".equals(samplingStrategy)) {
			if (exponentStr != null)
				try {
					exponent = Double.parseDouble(exponentStr);
					if (exponent == DEFAULT_EXPONENT)
						status = new ConfigurationStatus(CKEY_EXPONENT, Double
								.toString(exponent),
								EnumConfigurationState.CONFIGURED_DEFAULT);
					else
						status = new ConfigurationStatus(CKEY_EXPONENT, Double
								.toString(exponent),
								EnumConfigurationState.CONFIGURED_OK);
				} catch (NumberFormatException e) {
					status = new ConfigurationStatus(CKEY_EXPONENT, config
							.get(CKEY_EXPONENT), Double
							.toString(DEFAULT_EXPONENT),
							EnumConfigurationState.CONFIGURED_WRONG_DEFAULT)
							.setWarning("Float cannot be parsed, using default value");
				}
			else
				status = new ConfigurationStatus(CKEY_EXPONENT, null, Double
						.toString(DEFAULT_EXPONENT),
						EnumConfigurationState.UNCONFIGURED_DEFAULT);

			LOGGER.debug("  Exponent: " + exponent);
		} else {
			if (exponentStr != null)
				status = new ConfigurationStatus(CKEY_EXPONENT, exponentStr,
						EnumConfigurationState.CONFIGURED_UNUSED);
		}
		if (status != null)
			statuses.put(CKEY_EXPONENT, status);
		return exponent;
	}

	public static RecommendationServiceConfig createServiceConfig( RecommenderStrategy strat ) {
		RecommendationServiceConfig config = new RecommendationServiceConfig(
				"cms_" + strat.getContentName(), RecommendationServiceType.SCRIPTED);
		
		config.set( CKEY_SAMPLING_STRATEGY, strat.getSampling() );
		config.set( CKEY_TOP_N, Integer.toString( strat.getTopN() ) );
		config.set( CKEY_TOP_PERC, Double.toString( strat.getTopPercent() ) );
		config.set( CKEY_EXPONENT, Double.toString( strat.getExponent() ) );
		config.set( CKEY_SHOW_TEMP_UNAVAILABLE, Boolean.toString( strat.isShowTemporaryUnavailable() ) );
		config.set( CKEY_BRAND_UNIQ_SORT, Boolean.toString( strat.isBrandUniqSort() ) );
		config.set( CKEY_GENERATOR, strat.getGenerator() );
		config.set( CKEY_SCORING, strat.getScoring() );
		config.set( CKEY_USE_ALTS, Boolean.FALSE.toString() );
		return config;
	}
}

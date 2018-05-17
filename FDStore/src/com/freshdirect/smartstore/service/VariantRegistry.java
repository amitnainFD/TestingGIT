package com.freshdirect.smartstore.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.customer.ejb.FDServiceLocator;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.RecommendationService;
import com.freshdirect.smartstore.RecommendationServiceConfig;
import com.freshdirect.smartstore.RecommendationServiceType;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.ejb.SmartStoreServiceConfigurationSB;
import com.freshdirect.smartstore.external.scarab.ScarabInfrastructure;
import com.freshdirect.smartstore.fdstore.FactorRequirer;
import com.freshdirect.smartstore.fdstore.ScoreProvider;
import com.freshdirect.smartstore.impl.NullRecommendationService;

final public class VariantRegistry {
	
	private static final Logger LOGGER = LoggerFactory.getInstance(VariantRegistry.class);
	private static VariantRegistry instance;

	private Map<String, Variant> variantMap;
	private Map<EnumSiteFeature, Map<String, Variant>> siteFeatureMap;

	private Variant nilVariant;
	
	
	private VariantRegistry() {
	}

	public static synchronized VariantRegistry getInstance() {
		if (instance == null) {
			instance = new VariantRegistry();
		}
		return instance;
	}

	public synchronized Variant getService(String variantId) {
		if (variantMap == null)
			load();
		if (variantMap == null)
			throw new FDRuntimeException("failed to load variant map, see log for details");
		return variantMap.get(variantId);
	}

	/**
	 * THIS IS A MOCK METHOD, STRICTLY FOR TESTING PURPOSES !!!
	 * @param variant
	 */
	public synchronized void addService(Variant variant) {
		if (variantMap == null) {
			variantMap = new HashMap<String, Variant>();
			siteFeatureMap = new HashMap<EnumSiteFeature, Map<String,Variant>>();
		}
		variantMap.put(variant.getId(), variant);
		if (!siteFeatureMap.containsKey(variant.getSiteFeature()))
			siteFeatureMap.put(variant.getSiteFeature(), new HashMap<String, Variant>());
		siteFeatureMap.get(variant.getSiteFeature()).put(variant.getId(), variant);
	}
	
	public synchronized Map<String, Variant> getServices(
			EnumSiteFeature siteFeature) {
		if (variantMap == null)
			load();
		if (variantMap == null)
			throw new FDRuntimeException("failed to load variant map, see log for details");
		return siteFeatureMap.get(siteFeature);
	}

	private SmartStoreServiceConfigurationSB getServiceConfiguration() {
	    return FDServiceLocator.getInstance().getSmartStoreServiceConfiguration();
	}

	private void load() {
		try {
			ScarabInfrastructure.reload();
			EnumSiteFeature.refresh();
			SmartStoreServiceConfigurationSB sb;

			sb = getServiceConfiguration();
			Collection<Variant> variants = new ArrayList<Variant>();
			for (EnumSiteFeature feature : EnumSiteFeature.getSmartStoreEnumList())
				variants.addAll(sb.getVariants(feature));

			LOGGER.info("loading variants:" + variants);

			Map<String, Variant> variantMapTmp = new HashMap<String, Variant>(
					variants.size());
			Map<EnumSiteFeature, Map<String, Variant>> siteFeatureMapTmp = 
					new HashMap<EnumSiteFeature, Map<String,Variant>>();
			
			siteFeatureMapTmp.put(EnumSiteFeature.NIL, new HashMap<String, Variant>());
			for (EnumSiteFeature feature : EnumSiteFeature.getSmartStoreEnumList())
				siteFeatureMapTmp.put(feature, new HashMap<String, Variant>());

			Set<String> factors = new HashSet<String>();

			if (nilVariant == null) {
				final Variant _nilVariant = new Variant(
					Variant.NIL_ID,
					EnumSiteFeature.NIL,
					new RecommendationServiceConfig(Variant.NIL_ID, RecommendationServiceType.NIL)
				);

				NullRecommendationService nilService = new NullRecommendationService( _nilVariant );
				_nilVariant.setRecommender( nilService );

				nilVariant = _nilVariant;
				
			}
	        // register NIL variant in variant and site feature map
			variantMapTmp.put(nilVariant.getId(), nilVariant);
			siteFeatureMapTmp.get(nilVariant.getSiteFeature()).put(nilVariant.getId(), nilVariant);
			
			for (Variant variant : variants) {
				try {
					RecommendationService rs = RecommendationServiceFactory
							.configure(variant);
					if (rs instanceof FactorRequirer) {
						((FactorRequirer) rs).collectFactors(factors);
					}

					variant.setRecommender(rs);
					variantMapTmp.put(variant.getId(), variant);
					siteFeatureMapTmp.get(variant.getSiteFeature()).put(variant.getId(), variant);
				} catch (Exception e) {
					LOGGER.error("failed to configure variant '"
							+ variant.getId()
							+ "' -- variants are not (re)loaded", e);
					throw new FDRuntimeException(e,
							"failed to configure variant '" + variant.getId()
									+ "' -- variants are not (re)loaded");
				}
			}

			LOGGER.info("needed factors :" + factors);
			ScoreProvider.getInstance().acquireFactors(factors);
			LOGGER.info("configured variants:" + variantMapTmp.keySet());

			variantMap = variantMapTmp;
			siteFeatureMap = siteFeatureMapTmp;
		} catch (Exception e) {
			LOGGER.error("failed to (re)load variants", e);
		}
	}

	public synchronized void reload() {
		load();
	}
}

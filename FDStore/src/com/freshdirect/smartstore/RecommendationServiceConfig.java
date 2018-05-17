package com.freshdirect.smartstore;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.smartstore.service.RecommendationServiceFactory;


/**
 * Recommendation service configuration.
 * 
 * @author istvan
 * 
 */
public class RecommendationServiceConfig implements Serializable {
	// auto generated serial version id
	private static final long serialVersionUID = -5942360122361860134L;

	public static boolean isPresentationIncluded(Variant variant) {
		return EnumSiteFeature.DYF.equals(variant.getSiteFeature())
				|| EnumSiteFeature.FAVORITES.equals(variant.getSiteFeature());
	}
	
	/** Id of the variant. */
	protected String variantId;

	/** Service type. */
	protected RecommendationServiceType type;

	protected Map<String,String> params;
	
	protected SortedMap<String,ConfigurationStatus> configStatus;

	/**
	 * Constructor.
	 * 
	 * @param name
	 */
	public RecommendationServiceConfig(String variantId,
			RecommendationServiceType type) {
		this.variantId = variantId;
		this.type = type;
	}

	/**
	 * Get the hash code.
	 * 
	 * @see #equals(Object)
	 * @return {@link #getVariantId()}.{@link String#hashCode()}
	 */
	public int hashCode() {
		return getVariantId().hashCode();
	}

	/**
	 * Equality. Two configurations are the same if they have the same name.
	 * Configurations have id's in the corresponding database table.
	 * 
	 * @return if the two configs have the same name
	 */
	public boolean equals(Object o) {
		if (!(o instanceof RecommendationServiceConfig))
			return false;
		return ((RecommendationServiceConfig) o).getVariantId().equals(
				getVariantId());
	}

	/**
	 * Get config name.
	 * 
	 * @return name
	 */
	public String getVariantId() {
		return variantId;
	}

	/**
	 * Get config type.
	 * 
	 * @return config type
	 */
	public RecommendationServiceType getType() {
		return type;
	}

	/**
	 * RecommendationServiceConfig As string.
	 * 
	 * @return String representation
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();

		buf.append("{variant:" + variantId + ", ");
		buf.append("type:" + (type == null ? "" : type.getName()) + ", ");
		buf.append("config:{");

		if (params != null) {
			for (Iterator<String> it = params.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				buf.append(key + ":" + params.get(key));
				if (it.hasNext())
					buf.append(", ");
			}
		}
		buf.append("}}");

		return buf.toString();
	}

	public String get(String key) {
		return (params != null ? params.get(key) : null);
	}

	public String get(String key, String defaultValue) {
		String value = (params != null ? params.get(key) : defaultValue);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	public String getFILabel() {
		return get(RecommendationServiceFactory.CKEY_FI_LABEL,
				RecommendationServiceFactory.DEFAULT_FI_LABEL);
	}

	public String getPresentationTitle() {
		return get(RecommendationServiceFactory.CKEY_PREZ_TITLE);
	}

	public String getPresentationDescription() {
		return get(RecommendationServiceFactory.CKEY_PREZ_DESC);
	}

	public String getPresentationFooter() {
		return get(RecommendationServiceFactory.CKEY_PREZ_FOOTER);
	}
	
	public boolean isShowTempUnavailable() {
	    return "true".equals(get(RecommendationServiceFactory.CKEY_SHOW_TEMP_UNAVAILABLE));
	}
	
	public boolean isBrandUniqSort() {
	    return "true".equals(get(RecommendationServiceFactory.CKEY_BRAND_UNIQ_SORT));
	}
	
	public RecommendationServiceConfig set(String key, String value) {
		if (params == null) {
			params = new HashMap<String,String>();
		}
		params.put(key, value);
		return this;
	}

	public Set<String> keys() {
		return params != null ? params.keySet() : Collections.<String>emptySet();
	}

	public SortedMap<String,ConfigurationStatus> getConfigStatus() {
		return configStatus;
	}

	public void setConfigStatus(SortedMap<String,ConfigurationStatus> configStatus) {
		this.configStatus = configStatus;
	}
}

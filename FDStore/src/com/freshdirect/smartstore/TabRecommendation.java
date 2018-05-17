package com.freshdirect.smartstore;

import java.util.Collections;
import java.util.List;

import com.freshdirect.fdstore.util.EnumSiteFeature;

public class TabRecommendation {
	
    public static final String PIP_DEFAULT_DESC = "These are some of the items we recommend you:";

    final List<Variant> variants;
    final Variant tabVariant;
    String parentImpressionId;
    String[] featureImpId;
    int selected;

    public TabRecommendation(Variant tabVariant, List<Variant> variants) {
        this.tabVariant = tabVariant;
        this.variants = variants;
        this.featureImpId = new String[variants.size()];
    }

    public int size() {
        return variants.size();
    }
    
    public void setFeatureImpressionId(int pos, String featureImpId) {
        this.featureImpId[pos] = featureImpId;
    }
    
    public String getFeatureImpressionId(int pos) {
        return featureImpId[pos];
    }

    public Variant get(int index) {
        return variants.get(index);
    }

    public String getTabTitle(int index) {
        Variant variant = get(index);

        String prezTitle = variant.getServiceConfig().getPresentationTitle();
        if (prezTitle == null) {
            EnumSiteFeature siteFeature = variant.getSiteFeature();
            prezTitle = siteFeature.getPresentationTitle();
            if (prezTitle == null)
                prezTitle = siteFeature.getTitle();
            if (prezTitle == null)
                prezTitle = siteFeature.getName();
        }
        if (!variant.isSmartSavings() || prezTitle.toLowerCase().startsWith("save on "))
        	return prezTitle;
        
		return "Save on " + prezTitle;
    }

    public String getTabDescription(int index) {
        Variant variant = get(index);
        String varPrezDescription = variant.getServiceConfig().getPresentationDescription();
        if (varPrezDescription != null) {
            return varPrezDescription;
        }

        return PIP_DEFAULT_DESC;
    }
    
    public String getTabFooter(int index) {
        Variant variant = get(index);
        return variant.getServiceConfig().getPresentationFooter();
    }
    
    public Variant getTabVariant() {
        return tabVariant;
    }
    
    public String getParentImpressionId() {
        return parentImpressionId;
    }
    
    public void setParentImpressionId(String parentImpressionId) {
        this.parentImpressionId = parentImpressionId;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }
    
    public int getSelected() {
        return selected;
    }

    public int getTabIndex(String tabId) {
        for (int i = 0; i <variants.size(); i++) {
            Variant v = variants.get(i);
            if (v.getId().equals(tabId)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Returns the list of all variants.
     * @return
     */
    public List<Variant> getVariants() {
    	return Collections.unmodifiableList(this.variants);
    }
}

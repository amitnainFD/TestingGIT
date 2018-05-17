package com.freshdirect.fdstore.rollout;

public enum EnumRolloutFeature {
    pdplayout2014("pdplayout", "2014"), pplayout2014("pplayout", "2014"), leftnav2014("leftnav", "2014"), searchredesign2014("searchredesign", "2014"), leftnavtut2014(
            "leftnavtut", "2014"), browseflyoutrecommenders("browseflyout", "recommenders"), // fly-out recommended products on "browse" pages (transactionalPopup)
    quickshop2_2("quickshop", "2_2"), quickshop2_0(quickshop2_2, "quickshop", "2_0"), akamaiimageconvertor("akamaiimageconvertor", "2015"), checkout2_0("checkout", "2_0"), checkout1_0(
            checkout2_0, "checkout", "1_0"), gridlayoutcolumn4_0("gridlayoutcolumn", "4_0"), gridlayoutcolumn5_0(gridlayoutcolumn4_0,
            "gridlayoutcolumn", "5_0");

    private final EnumRolloutFeature child;
    private final String cookieName;
    private final String cookieVersion;

    private EnumRolloutFeature(String cookieName, String cookieVersion) {
        this(null, cookieName, cookieVersion);
    }

    private EnumRolloutFeature(EnumRolloutFeature child, String cookieName, String cookieVersion) {
        this.child = child;
        this.cookieName = cookieName;
        this.cookieVersion = cookieVersion;
    }

    /**
     * @return the parent
     */
    public EnumRolloutFeature getChild() {
        return child;
    }

    /**
     * @return the cookieName
     */
    public String getCookieName() {
        return cookieName;
    }

    /**
     * @return the cookieVersion
     */
    public String getCookieVersion() {
        return cookieVersion;
    }

    public boolean featureIsChild(EnumRolloutFeature childCandidate) {
        boolean result = false;
        EnumRolloutFeature feature = this.getChild();
        while (result == false && feature != null) {
            result = feature == childCandidate;
            feature = feature.getChild();
        }
        return result;
    }
}

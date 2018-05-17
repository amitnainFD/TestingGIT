package com.freshdirect.smartstore.fdstore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.ZonePriceListing;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ContentNodeModelReference;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.ProductReference;
import com.freshdirect.fdstore.content.ProductReferenceImpl;
import com.freshdirect.fdstore.content.YmalSource;
import com.freshdirect.fdstore.pricing.ProductPricingFactory;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.VariantReference;
import com.freshdirect.smartstore.impl.AbstractRecommendationService;

/**
 * A list of recommended contents tagged with a variant.
 * 
 * This objects is serializable, so 
 * 
 * @author istvan
 *
 */
public class Recommendations implements Serializable {
	public static final int MAX_PRODS = 5;
	
	private static final long serialVersionUID = 8230385944777453868L;
	private VariantReference variant;
	
	/**
	 * List of all recommended products
	 */
	private transient List<ProductModel> products;
    private List<ProductReference> productReferences;

	private Map<ContentKey,String> impressionIds;
	private Map<String,String> prd2recommender;
	private Map<String,String> prd2recommenderStrat;
    private Map<String,List<ContentKey>> previousRecommendations;
	private String requestId;

	ContentNodeModelReference<CategoryModel> category;
	ContentNodeModelReference<ContentNodeModel> currentNode;
	ContentNodeModelReference<YmalSource> ymalSource;
	
	private boolean isRefreshable = true;
	
	@Deprecated
	private boolean isSmartSavings = false;

	// products window
	int	offset	= 0;
	int	wsize	= MAX_PRODS;
	
	// array of logged products
	boolean logged[];
	
	/**
	 * Constructor.
	 * @param variant 
	 * @param contentKeys List<{@link ProductModel}>
	 */
	public Recommendations(Variant variant, List<ContentNodeModel> contentNodes, boolean isRefreshable, boolean isSmartSavings, int wsize, PricingContext pricingCtx) {
		this.variant = new VariantReference(variant);
		this.productReferences = new ArrayList<ProductReference>(contentNodes.size());
		this.products = new ArrayList<ProductModel>(contentNodes.size());
		for (ContentNodeModel m : contentNodes) {
			//Convert to ProductModelPricingAdapter for Zone Pricing
		    ProductModel p = ProductPricingFactory.getInstance().getPricingAdapter((ProductModel)m,pricingCtx);
		    this.products.add(p);
		    this.productReferences.add(new ProductReferenceImpl(p));
		}

		this.wsize = wsize;

		final int S = getNumberOfPages();
		logged = new boolean[S];
		for (int i=0; i<S; i++)
			logged[i] = false;


		this.isRefreshable = isRefreshable;
		this.isSmartSavings = false;

		if (AbstractRecommendationService.RECOMMENDER_SERVICE_AUDIT.get() != null) {
			prd2recommender = AbstractRecommendationService.RECOMMENDER_SERVICE_AUDIT.get();	        
	        AbstractRecommendationService.RECOMMENDER_SERVICE_AUDIT.set(null);
		} else {
			prd2recommender = Collections.emptyMap();
		}

		if (AbstractRecommendationService.RECOMMENDER_STRATEGY_SERVICE_AUDIT.get() != null) {
			prd2recommenderStrat = AbstractRecommendationService.RECOMMENDER_STRATEGY_SERVICE_AUDIT.get();
	        AbstractRecommendationService.RECOMMENDER_STRATEGY_SERVICE_AUDIT.set(null);
		} else {
			prd2recommenderStrat = Collections.emptyMap();			
		}
	}

	public Recommendations(Variant variant, List<ContentNodeModel> contentNodes) {
		this(variant, contentNodes, true, false, MAX_PRODS, new PricingContext(ZonePriceListing.DEFAULT_ZONE_INFO));
	}
	
	/**
	 * This constructor is called from FDStoreRecommender
	 * 
	 * @param variant
	 * @param products
	 * @param sessionInput
	 * @param isRefreshable
	 * @param isSmartSavings
	 */
	public Recommendations(Variant variant, List<ContentNodeModel> products, SessionInput sessionInput,
			boolean isRefreshable, boolean isSmartSavings) {
		this(variant, products, isRefreshable, isSmartSavings, sessionInput != null ? sessionInput.getWindowSize(MAX_PRODS) : MAX_PRODS,
				sessionInput != null && sessionInput.getPricingContext() != null ? sessionInput.getPricingContext() : new PricingContext(ZonePriceListing.DEFAULT_ZONE_INFO));
		if (sessionInput != null) {
		    this.previousRecommendations = sessionInput.getPreviousRecommendations();
		    this.category = new ContentNodeModelReference<CategoryModel> (sessionInput.getCategory());
            this.currentNode = new ContentNodeModelReference<ContentNodeModel> (sessionInput.getCurrentNode());
		    this.ymalSource = new ContentNodeModelReference<YmalSource> (sessionInput.getYmalSource());
		}
	}

	/**
	 * Get recommended products.
	 * @return List<{@link ProductModel}>
	 */
    public List<ProductModel> getProducts() {
        if (productReferences.isEmpty()) {
            return Collections.emptyList();
        }

        if (offset < 0 || offset * wsize >= productReferences.size())
            throw new IndexOutOfBoundsException();
        final int p = offset * wsize;
        // DEBUG System.err.println("pos: " + p + " num: " + Math.min(wsize, products.size()-p) + " / max products: " + products.size());
        return getAllProducts().subList(p, Math.min(p + wsize, productReferences.size()));
    }	
	
	public List<ProductModel> getAllProducts() {
	    if (products == null) {
	        products = new ArrayList<ProductModel>();
	        for (ProductReference ref : productReferences) {
	            products.add(ref.lookupProductModel());
	        }
	    }
	    return products;
	}

	/**
	 * Get variant.
	 * @return variant
	 */
	public Variant getVariant() {
		return variant.get();
	}
	

	
    public void addImpressionIds(Map<ContentKey, String> impressionIds) {
        if (this.impressionIds == null) {
            this.impressionIds = impressionIds;
        } else {
            this.impressionIds.putAll(impressionIds);
        }
    }
	
	String getImpressionId(ContentKey key) {
	    Object obj =  impressionIds!=null ? impressionIds.get(key) : null;
	    if (obj instanceof String) {
	        return ((String)obj);
	    }
	    return null;
	}

	public String getImpressionId(ProductModel model) {
		return model != null ? getImpressionId(model.getSourceProduct().getContentKey()) : null;
	}
	
    public boolean isRefreshable() {
    	return this.isRefreshable;
    }

    public boolean isSmartSavings() {
		return isSmartSavings;
	}


    
    /* PAGING MODULE */

    public void pageForward() {
    	if ( (offset+1)*wsize < products.size() ) {
    		offset++;
    	}
    }

    public void pageBackward() {
    	if ( offset > 0 )
    		offset--;
    }

    public boolean isFirstPage() {
    	return offset == 0;
    }
    
    public boolean isLastPage() {
    	return offset == getNumberOfPages()-1;
    }

    public int getOffset() {
    	return this.offset;
    }

    public void setOffset(int newOffset) throws IndexOutOfBoundsException {
    	if (newOffset < 0 || (newOffset*wsize)>= products.size())
    		throw new IndexOutOfBoundsException();
    	this.offset = newOffset;
    }

    public int getNumberOfPages() {
    	return (int) Math.ceil( ( (double) products.size() )/wsize );
    }


    /**
     * Tells if the current product subset logged and sets to true.
     * @return
     */
    public boolean isLogged() {
    	final boolean x = logged[offset];
    	logged[offset] = true;
    	return x;
    }
    
    public Map<String, List<ContentKey>> getPreviousRecommendations() {
        return previousRecommendations;
    }
    
    public CategoryModel getCategory() {
        return category != null ? category.get() : null;
    }
    
    public ContentNodeModel getCurrentNode() {
        return currentNode != null ? currentNode.get() : null;
    }
    
    public YmalSource getYmalSource() {
        return ymalSource != null ? ymalSource.get() : null;
    }
    
    
    public String getRecommenderIdForProduct(String productId) {
    	return prd2recommender.get(productId);
    }
    
    public String getRecommenderStrategyIdForProduct(String productId) {
    	return prd2recommenderStrat.get(productId);
    }
    
    public String getRequestId() {
		return requestId;
	}
    
    public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append( "Recommendations[" );
    	sb.append( "requestId=" );
    	sb.append( requestId );
    	sb.append( ", # of products=" );
    	sb.append( products.size() );
    	sb.append( "]" );
    	return sb.toString();
    }
}

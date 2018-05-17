package com.freshdirect.fdstore.content.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.FDCachedFactory;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ContentUtil;
import com.freshdirect.fdstore.content.DepartmentModel;
import com.freshdirect.fdstore.content.EnumShowChildrenType;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.SkuModel;
import com.freshdirect.fdstore.pricing.ProductPricingFactory;
import com.freshdirect.fdstore.pricing.SkuModelPricingAdapter;
import com.freshdirect.framework.util.log.LoggerFactory;


public class ItemGrabber {

	private static final long	serialVersionUID	= -8310578679108946007L;

	@SuppressWarnings( "unused" )
	private static Logger LOGGER = LoggerFactory.getInstance( ItemGrabber.class );
	
	private boolean ignoreShowChildren = false;
	private ContentNodeModel rootNode = null;
	private boolean returnHiddenFolders = false;
	private boolean returnSecondaryFolders = false;
	private boolean filterDiscontinued = true;
	private boolean filterUnavailable = true;
    private boolean ignoreDuplicateProducts=false;
    private boolean returnSkus=false;
	private boolean returnInvisibleProducts = false;  // return prods that are flagged as invisible
	private int depth = 1;
	private PricingContext pricingCtx = PricingContext.DEFAULT;
		
	
	private HashSet<String> noDupeProds = new HashSet<String>();
	private List<ContentNodeModel> workSet = new ArrayList<ContentNodeModel>();
	private List<String> skuList = new ArrayList<String>();

	public void setReturnHiddenFolders(boolean returnHiddenFlag) {
		this.returnHiddenFolders = returnHiddenFlag;
	}

	public void setIgnoreShowChildren(boolean ignoreFlag) {
		this.ignoreShowChildren = ignoreFlag;
	}

    public void setIgnoreDuplicateProducts(boolean ignoreFlag) {
        this.ignoreDuplicateProducts = ignoreFlag;
        
    }
    public void setWorkSet(List<ContentNodeModel> workSet) {
    	this.workSet=workSet;
    }    
    
    public void setReturnSkus(boolean flag) {
        this.returnSkus = flag;
    }    
    
	public void setRootNode(ContentNodeModel category) {
		this.rootNode = category;
	}
		
	public void setDepth(int depth){
		this.depth = depth;
	}

	public void setReturnSecondaryFolders(boolean rsf) {
		this.returnSecondaryFolders = rsf;
	}
		
	public void setReturnInvisibleProducts(boolean setting) {
		this.returnInvisibleProducts = setting;
	}

	public void setFilterDiscontinued(boolean filterDiscontinued) {
		this.filterDiscontinued = filterDiscontinued;
	}

	public void setFilterUnavailable(boolean filterUnavailable) {
		this.filterUnavailable = filterUnavailable;
	}

	public void setPricingCtx( PricingContext pricingCtx ) {
		this.pricingCtx = pricingCtx;
	}

	public List<ContentNodeModel> grabTheItems() throws FDSkuNotFoundException, FDResourceException {
		// we do not grab things if depth is set to negative
		// in this case we let the layout manager decided what to grab
		if (depth < 0)
			return workSet;

		if (rootNode == null) {
			return workSet;
		}
		
		getProdsAndFolders(rootNode, this.depth, 0, pricingCtx );
		filterDiscItems();
		
		return workSet;
	} 


	/**
	 * @return true if at least one product/category added to work list
	 * also if a duplicate product would have been added, it returns true.
	 */
	private boolean getProdsAndFolders(ContentNodeModel currentNode, int desiredDepth, int currentDepth, PricingContext pricingCtx) throws FDResourceException, FDSkuNotFoundException {
   
		if (currentNode instanceof ProductModel) {
			return false;
		}
	
		boolean rtnValue = false;  

		List<CategoryModel> subFolders = null;

		if (currentNode instanceof CategoryModel) {
			CategoryModel currentCat = (CategoryModel)currentNode;
			Collection<ProductModel> products = currentCat.getProducts();
			boolean isDDPPCat = (null != currentCat.getProductPromotionType() && ContentFactory.getInstance().isEligibleForDDPP());
	        
			for ( ProductModel product : products) {  // get prods from current folder

				// are we returning invisible products 
				if (!returnInvisibleProducts && product.isInvisible()) {
					continue;
				}
				
				//isFiltered Origin : [APPDEV-2857] Blocking Alcohol for customers outside of Alcohol Delivery Area
				if(!ContentUtil.isAvailableByContext(product)) {
					continue;
				}
					
				if ( (!ignoreDuplicateProducts && this.noDupeProds.add(product.getContentName())==false) || product.isHidden()) {
					rtnValue = true;
					continue;  
				}
				
				if (filterDiscontinued || filterUnavailable) {
					this.skuList.addAll( product.getSkuCodes() );
				}
				if (this.returnSkus) {
					for ( SkuModel sku : product.getSkus() ) {
						if(isDDPPCat){
							if(!sku.isUnavailable())
								this.workSet.add(new SkuModelPricingAdapter(sku, pricingCtx));
						}else{
							if (!sku.isDiscontinued()){
								//Convert to SkuModelPricingAdapter for Zone Pricing
								this.workSet.add(new SkuModelPricingAdapter(sku, pricingCtx));
							}
						}
					}
				} else {
					//Convert to ProductModelPricingAdapter for Zone Pricing
					if(!isDDPPCat || !product.getDefaultSku().isUnavailable()){
						this.workSet.add(ProductPricingFactory.getInstance().getPricingAdapter(product ,pricingCtx) );
					}
				}
				rtnValue=true;
			}
			if(!isDDPPCat){
				subFolders = currentCat.getSubcategories();
			}else{
				return rtnValue;
			}

		} else if ( currentNode instanceof DepartmentModel ) {
			// it must be a department
			subFolders = ((DepartmentModel)currentNode).getCategories();				   
		
		} else {
			// some other type of node, should not happen for real.
			return false;
		}

		//
		// Now get the products for the subfolders that have their Show_Folder=true and Show_children=always
		//

		for ( CategoryModel subFolder : subFolders ) {

			if (subFolder.isHidden()) {
				// skip hidden
				continue;
			}
			if (!returnSecondaryFolders && subFolder.isSecondaryCategory()) {
				continue;
			}
			if (subFolder.getShowSelf() || returnHiddenFolders) {
				this.workSet.add(subFolder);
				rtnValue = true;
			}

			if ( (!subFolder.getTreatAsProduct() && depth>0) && 
			     ( (EnumShowChildrenType.ALWAYS.equals(subFolder.getShowChildren()) ||
			        (ignoreShowChildren && !EnumShowChildrenType.NEVER.equals(subFolder.getShowChildren()))		//get prods if show_children, regardless of show folder setting
			       ) 
			     )
			   ) {	
				  
				int wrkSetSize1 = this.workSet.size();
					
				// recurse and process this folder
				if (desiredDepth >= (currentDepth+1)) {
					boolean keepLastCategory = this.getProdsAndFolders( subFolder, desiredDepth, currentDepth+1,pricingCtx );
					if (!keepLastCategory && this.workSet.size()==wrkSetSize1 && wrkSetSize1 !=0 && (subFolder.getShowSelf() || returnHiddenFolders)) {
						this.workSet.remove(wrkSetSize1-1);
				   }
				}
			}
		}
		return rtnValue;
	}
	

        private void filterDiscItems() throws FDResourceException {

            if ((this.filterDiscontinued || this.filterUnavailable) && this.skuList.size() > 0) {
		// make sure FDProductInfos are cached
		FDCachedFactory.getProductInfos( (String[])this.skuList.toArray( new String[0] ) );

                // remove discontinued products from workSet
                for (ListIterator<ContentNodeModel> i = this.workSet.listIterator(); i.hasNext();) {
                    ContentNodeModel node = i.next();
                    if (node instanceof ProductModel) {
                    	ProductModel product = (ProductModel) node;
                    	if (filterDiscontinued && product.isDiscontinued()) {
                    		i.remove();
                    		continue;
                    	}
                    	if (filterUnavailable && product.isUnavailable()) {
                    		i.remove();
                    		continue;
                    	}
                    }
                }
            }
        }

}

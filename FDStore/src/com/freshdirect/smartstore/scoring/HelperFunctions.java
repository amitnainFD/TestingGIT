package com.freshdirect.smartstore.scoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.fdstore.FDContentTypes;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.content.BrandModel;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.DepartmentModel;
import com.freshdirect.fdstore.content.FavoriteList;
import com.freshdirect.fdstore.content.ProductContainer;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.Recommender;
import com.freshdirect.fdstore.content.RecommenderStrategy;
import com.freshdirect.fdstore.content.SkuModel;
import com.freshdirect.fdstore.content.StoreModel;
import com.freshdirect.fdstore.content.customerrating.CustomerRatingsContext;
import com.freshdirect.fdstore.content.customerrating.CustomerRatingsDTO;
import com.freshdirect.smartstore.RecommendationService;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.external.ExternalRecommender;
import com.freshdirect.smartstore.external.ExternalRecommenderCommunicationException;
import com.freshdirect.smartstore.external.ExternalRecommenderRegistry;
import com.freshdirect.smartstore.external.ExternalRecommenderRequest;
import com.freshdirect.smartstore.external.ExternalRecommenderType;
import com.freshdirect.smartstore.external.NoSuchExternalRecommenderException;
import com.freshdirect.smartstore.external.RecommendationItem;
import com.freshdirect.smartstore.fdstore.DatabaseScoreFactorProvider;
import com.freshdirect.smartstore.fdstore.SmartStoreUtil;
import com.freshdirect.smartstore.filter.ContentFilter;
import com.freshdirect.smartstore.filter.FilterFactory;
import com.freshdirect.smartstore.impl.SmartYMALRecommendationService;
import com.freshdirect.smartstore.sampling.RankedContent;
import com.freshdirect.smartstore.sampling.RankedContent.Single;
import com.freshdirect.smartstore.service.CmsRecommenderRegistry;

/**
 * This class contains functions which used by the generated code.
 * @author zsombor
 *
 */
public class HelperFunctions {
	private static final Logger LOGGER = Logger.getLogger(HelperFunctions.class.getCanonicalName());
	
    private HelperFunctions() {}    
    
    /**
     * used by the generated code
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static final int between(double value, double min, double max) {
        return (min<=value && value<= max ? 1 : 0);
    }

    /**
     * used by the generated code
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static final int between(int value, int min, int max) {
        return (min<=value && value<= max ? 1 : 0);
    }

    /**
     * This method called from the generated code! Modify the signature with extreme care.
     * @param params the list of parameters, it can contains strings, nodes, and lists
     * @return
     */
    @SuppressWarnings( "unchecked" )
	public static final List<ProductModel> recursiveNodes(Object[] params) {
        List<ProductModel> result = new ArrayList<ProductModel>();
        for (int i=0;i<params.length;i++) {
            Object obj = params[i];
            if (obj instanceof ContentNodeModel) {
                result.addAll(recursiveNodes((ContentNodeModel) obj));
            } else if (obj instanceof String) {
                result.addAll(recursiveNodes(lookup((String) obj)));
            } else if (obj instanceof List<?>) {
                result.addAll(recursiveNodes((List<ContentNodeModel>) obj));
            }
        }

		return result;
    }

    /**
     * 
     * @param model List<ContentNodeModel>
     * @return
     */
    public static final List<ProductModel> recursiveNodes(List<ContentNodeModel> list) {
        List<ProductModel> result = new ArrayList<ProductModel>();
        for (int i=0;i<list.size();i++) {
            result.addAll(recursiveNodes(list.get(i)));
        }
        return result;
    }

    /**
     * Return a parent category, or null if there is no category in the hierarchy.
     * @param model
     * @return
     */
	public static final ContentNodeModel getParentCategory( ContentNodeModel model ) {
		if ( model != null ) {
			ContentNodeModel parentNode = model.getParentNode();
			if ( parentNode != null ) {
				if ( parentNode instanceof CategoryModel ) {
					return parentNode;
				}
				return getParentCategory( parentNode );
			}
		}
		return null;
	}

    /**
     * Return a parent category, or null if there is no category in the hierarchy.
     * @param model
     * @return
     */
	public static final ContentNodeModel getParentDepartment( ContentNodeModel model ) {
		if ( model != null ) {
			ContentNodeModel parentNode = model.getParentNode();
			if ( parentNode != null ) {
				if ( parentNode instanceof DepartmentModel ) {
					return parentNode;
				}
				return getParentDepartment( parentNode );
			}
		}
		return null;
	}    
    
    /**
     * Return the top level category from the hierarchy. Top level category means, category which parent isn't a category.
     * @param model
     * @return
     */
	public static final ContentNodeModel getToplevelCategory( ContentNodeModel model ) {
		if ( model != null ) {
			ContentNodeModel parentNode = model.getParentNode();
			if ( parentNode != null && parentNode instanceof CategoryModel ) {
				return getToplevelCategory( parentNode );
			}
		}
		return model;
	}

    /**
     * Return a list of nodes, under the given one.
     * @param model
     * @return
     */
    public static final List<ProductModel> recursiveNodes(ContentNodeModel model) {
        List<ProductModel> result = new ArrayList<ProductModel>();
        collect(model, result);
        return result;
    }

    /**
     * Collect nodes into the result list starting from the first parameter.
     * 
     * @param model
     * @param result
     */
    private static void collect(ContentNodeModel model, Collection<ProductModel> result) {
        if (model instanceof ProductModel) {
            result.add(SmartStoreUtil.addConfiguredProductToCache((ProductModel) model));
        } else if (model instanceof CategoryModel) {
            CategoryModel cat = (CategoryModel) model;
            for (Iterator<ProductModel> iter = cat.getStaticProducts().iterator(); iter.hasNext();) {
                result.add(iter.next());
            }
            for (Iterator<CategoryModel> iter = cat.getSubcategories().iterator(); iter.hasNext();) {
                collect( iter.next(), result);
            }
        } else if (model instanceof FavoriteList) {
            FavoriteList fl = (FavoriteList) model;
            for (Iterator<ProductModel> iter = fl.getFavoriteItems().iterator(); iter.hasNext();) {
                collect( iter.next(), result); 
            }
        } else if (model instanceof DepartmentModel) {
            DepartmentModel dep = (DepartmentModel) model;
            for (Iterator<CategoryModel> iter = dep.getCategories().iterator(); iter.hasNext();) {
                collect( iter.next(), result);
            }
        } else if (model instanceof StoreModel) {
            StoreModel store = (StoreModel) model;
            for (Iterator<DepartmentModel> iter = store.getDepartments().iterator(); iter.hasNext();) {
                collect( iter.next(), result);
            }
        }
    }

    /**
     * Return a list of nodes, under the given one, except the specified values from the exceptionObject.
     * @param model node
     * @param exceptionObject can be string, ContentNodeModel or Collection 
     * @return
     */
    public static final List<ProductModel> recursiveNodesExcept(ContentNodeModel model,Object exceptionObject) {
        return collectExcept(model, collectIds(exceptionObject));
    }
    
    /**
     * Return a list of nodes, under the given one, except the specified values from the exceptionIds collection.
     * @param model
     * @param exceptionObject can be an array of strings, ContentNodeModel or Collection. 
     * @return
     */
    public static final List<ProductModel> recursiveNodesExcept(ContentNodeModel model,Object[] exceptionObject) {
        return collectExcept(model, collectIds(exceptionObject));
    }

    /**
     * Return a list of nodes, under the given ones, except the specified values from the exceptionIds collection.
     * @param models a collections of nodes
     * @param exceptionObject can be string, ContentNodeModel or Collection 
     * @return
     */
    public static final List<ProductModel> recursiveNodesExcept(Collection<ContentNodeModel> models,Object exceptionObject) {
        return collectExcept(models, collectIds(exceptionObject));
    }

    /**
     * Return a list of nodes, under the given one, except the specified values from the exceptionIds collection.
     * @param models a collections of nodes
     * @param exceptionObject can be an array of strings, ContentNodeModel or Collection. 
     * @return
     */
    public static final List<ProductModel> recursiveNodesExcept(Collection<ContentNodeModel> models,Object[] exceptionObject) {
        return collectExcept(models, collectIds(exceptionObject));
    }

    private static Set<String> collectIds(Object exceptionObject) {
        Set<String> exceptionIds = new HashSet<String>(); 
        collectIds(exceptionIds, exceptionObject);
        return exceptionIds;
    }

    private static Set<String> collectIds(Object[] exceptionObject) {
        Set<String> exceptionIds = new HashSet<String>(); 
        for (int i=0;i<exceptionObject.length;i++) {
            Object obj = exceptionObject[i];
            collectIds(exceptionIds, obj);
        }
        return exceptionIds;
    }

    private static List<ProductModel> collectExcept(ContentNodeModel model, Set<String> exceptionIds) {
        Collection<ProductModel> result = new HashSet<ProductModel>();
        collectExcept(model, result, exceptionIds);
        return new ArrayList<ProductModel>(result);
    }

    private static List<ProductModel> collectExcept(Collection<ContentNodeModel> models, Set<String> exceptionIds) {
        Collection<ProductModel> result = new HashSet<ProductModel>();
        for (Iterator<ContentNodeModel> iter= models.iterator(); iter.hasNext();) {
            ContentNodeModel model = iter.next();
            collectExcept(model, result, exceptionIds);
        }
        return new ArrayList<ProductModel>(result);
    }

    private static void collectIds(Set<String> exceptionIds, Object obj) {
        if (obj instanceof String) {
            exceptionIds.add((String) obj); 
        } else if (obj instanceof ContentNodeModel) {
            exceptionIds.add(((ContentNodeModel)obj).getContentKey().getId());
        } else if (obj instanceof Collection) {
            Collection<?> col = (Collection<?>) obj;
            for (Iterator<?> iter= col.iterator();iter.hasNext();) {
                collectIds(exceptionIds, iter.next());
            }
        }
    }
    
    /**
     * Collect nodes into the result list starting from the first parameter, except under the specified nodes.
     * 
     * @param model
     * @param result
     * @param exceptionIds collection of strings, representing the content keys.
     */
    private static void collectExcept(ContentNodeModel model, Collection<ProductModel> result, Collection<String> exceptionIds) {
        if (exceptionIds.contains(model.getContentKey().getId())) {
            return;
        }
        if (model instanceof ProductModel) {
            result.add(SmartStoreUtil.addConfiguredProductToCache((ProductModel) model));
        } else if (model instanceof CategoryModel) {
            CategoryModel cat = (CategoryModel) model;
            for (Iterator<ProductModel> iter = cat.getProducts().iterator(); iter.hasNext();) {
                collectExcept(iter.next(), result, exceptionIds);
            }
            for (Iterator<CategoryModel> iter = cat.getSubcategories().iterator(); iter.hasNext();) {
                collectExcept( iter.next(), result, exceptionIds);
            }
        } else if (model instanceof FavoriteList) {
            FavoriteList fl = (FavoriteList) model;
            for (Iterator<ProductModel> iter = fl.getFavoriteItems().iterator(); iter.hasNext();) {
                collectExcept( iter.next(), result, exceptionIds); 
            }
        } else if (model instanceof DepartmentModel) {
            DepartmentModel dep = (DepartmentModel) model;
            for (Iterator<CategoryModel> iter = dep.getCategories().iterator(); iter.hasNext();) {
                collectExcept( iter.next(), result, exceptionIds);
            }
        } else if (model instanceof StoreModel) {
            StoreModel store = (StoreModel) model;
            for (Iterator<DepartmentModel> iter = store.getDepartments().iterator(); iter.hasNext();) {
                collectExcept( iter.next(), result, exceptionIds);
            }
        }
    }
    
    
    

    public static ContentNodeModel lookup(String id) {
        return ContentFactory.getInstance().getContentNode(id);
    }

    @SuppressWarnings( "unchecked" )
	public static List<ContentNodeModel> toList(Object[] params) {
        List<ContentNodeModel> result = new ArrayList<ContentNodeModel>();
        for (int i=0;i<params.length;i++) {
            Object obj = params[i];
            if (obj instanceof ContentNodeModel) {
                result.add((ContentNodeModel) obj);
            } else if (obj instanceof String) {
                result.add(lookup((String) obj));
            } else if (obj instanceof List) {
                result.addAll((List<ContentNodeModel>) obj);
            }
        }
        return result;
    }
    
    /**
     * used by 'MatchBrandFilter'
     * 
     * @param models
     * @return
     */
    public static ContentNodeModel findBrand(List<ContentNodeModel> models) {
        for (ContentNodeModel m : models) {
            if (m instanceof BrandModel) {
                return m;
            }
        }
        return null;
    }
    
    /**
     * used by 'MatchBrandFilter'
     * 
     * @param model
     * @param filterBrand
     * @return
     */
    public static boolean matchBrand(ContentNodeModel model, ContentNodeModel filterBrand) {
        if (model instanceof ProductModel) {
            List<BrandModel> brands = ((ProductModel) model).getBrands();
            for (BrandModel brand : brands) {
                if (brand.equals(filterBrand)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static List<ContentNodeModel> toList(ContentNodeModel model) {
        ArrayList<ContentNodeModel> a = new ArrayList<ContentNodeModel>();
        a.add(model);
        return a;
    }

    public static List<ContentNodeModel> toList(String id) {
        ArrayList<ContentNodeModel> a = new ArrayList<ContentNodeModel>();
        a.add(lookup(id));
        return a;
    }
    
    public static List<ContentNodeModel> toList(Collection<ContentKey> keys) {
    	List<ContentNodeModel> nodes = new ArrayList<ContentNodeModel>(keys.size());
    	for (ContentKey key : keys) {
    		ContentNodeModel node = ContentFactory.getInstance().getContentNodeByKey(key);
    		if (node != null)
    			nodes.add(node);
    	}
    	return nodes;
    }

    /**
     * Return the part of the cache key for the current node.
     * @param input
     * @return
     */
    public static String getCurrentNodeCacheKey(SessionInput input) {
        return (input!=null && input.getCurrentNode()!=null) ? input.getCurrentNode().getContentKey().getId() : "<null>";
    }
    
    public static String getExplicitListCacheKey(SessionInput input) {
        if (input!=null && input.getExplicitList()!=null) {
            StringBuffer buf = new StringBuffer("[");
            boolean first = true;
            for (Iterator<? extends ContentNodeModel> iter=input.getExplicitList().iterator();iter.hasNext();) {
                ContentNodeModel model = iter.next();
                if (!first) {
                    buf.append(',');
                } else {
                    first = false;
                }
                buf.append(model.getContentKey().getId());
            }
            buf.append(']');
            return buf.toString();
        }
        return "<empty-list>";
    }
    
    /**
     * Return a list of product recommendation for a given product by a
     * recommender vendor.
     * 
     * @param recommender
     * @param model
     * @return List<ContentNodeModel>
     */
	@Deprecated
    public static List<ContentNodeModel> getProductRecommendationFromVendor(String recommender, ContentNodeModel model) {
        return SmartStoreUtil.toContentNodesFromKeys(DatabaseScoreFactorProvider.getInstance().getProductRecommendations(recommender, model.getContentKey()));
    }

	@Deprecated
    public static List<ContentNodeModel> getUserRecommendationFromVendor(String recommender, String erpCustomerId) {
        return SmartStoreUtil.toContentNodesFromKeys(DatabaseScoreFactorProvider.getInstance().getPersonalRecommendations(recommender, erpCustomerId));
    }

    /**
     * Return a list of featured items.
     * 
     * @param model
     * @return
     */
    public static List<ProductModel> getFeaturedItems(ContentNodeModel model) {
        if(model instanceof ProductContainer) {
            return ((ProductContainer) model).getFeaturedProducts();
        } else if (model instanceof ProductModel) {
            return ((CategoryModel) HelperFunctions.getToplevelCategory(model)).getFeaturedProducts();
        }
        return Collections.emptyList();
    }
    
    public static List<ProductModel> getCandidateLists(ContentNodeModel model) {
		List<ProductModel> nodes = new ArrayList<ProductModel>();
		if (model instanceof CategoryModel) {
			CategoryModel category = (CategoryModel) model;
			List<ContentNodeModel> candidateList = category.getCandidateList();
			if (candidateList != null && candidateList.size() > 0) {
				nodes.addAll(recursiveNodes(candidateList));
			} else {
				nodes.addAll(recursiveNodes(model));
			}
		}
		return nodes;
	}

	public static List<ProductModel> getManuallyOverriddenSlots( ContentNodeModel model, SessionInput input, DataAccess dataAccess ) {
		List<ProductModel> nodes = new ArrayList<ProductModel>();
		if ( model instanceof CategoryModel ) {
			CategoryModel category = (CategoryModel)model;
			int slots = category.getManualSelectionSlots();
			if ( slots > 0 ) {
				ContentFilter filter = FilterFactory.getInstance().createFilter( input.getExclusions(), input.isUseAlternatives(), input.isShowTemporaryUnavailable() );
				List<ProductModel> fprods = category.getFeaturedProducts();
				Random rnd = new Random();

				while ( nodes.size() < slots && fprods.size() > 0 ) {
					int pos = input.isNoShuffle() ? 0 : rnd.nextInt( fprods.size() );
					Object product = fprods.remove( pos );

					ProductModel pm = (ProductModel)product;
					// it does all checks against cart include, displaying, uniqueness, etc.
					final ProductModel filteredModel = filter.filter( pm );
					if ( filteredModel != null ) {
						nodes.add( filteredModel );
						slots--;
					}
				}
				
				input.traceContentNodes("FeaturedProducts", nodes);
			}
		}
		return nodes;
	}

	public static List<ProductModel> getManuallyOverriddenSlotsP( ContentNodeModel model, SessionInput input, DataAccess dataAccess ) {
		if ( model instanceof CategoryModel ) {
			CategoryModel category = (CategoryModel)model;
			int slots = category.getManualSelectionSlots();
			if ( slots > 0 ) {
				List<ProductModel> fprods = category.getFeaturedProducts();
				Random rnd = new Random();

				while ( slots > 0 && fprods.size() > 0 ) {
					int pos = input.isNoShuffle() ? 0 : rnd.nextInt( fprods.size() );
					Object product = fprods.remove( pos );

					ProductModel pm = (ProductModel)product;
					// it does all checks against cart include, displaying, uniqueness, etc.
					if ( dataAccess.addPrioritizedNode( pm ) ) {
						slots--;
						input.traceContentNode("FeaturedItems", (ContentNodeModel) product);
					}
					// we do not return prioritized nodes
				}
			}
		}
		// return an empty list on purpose, items were added to prioritized list, they are not returned here
		return new ArrayList<ProductModel>();
	}
    
    public static List<ProductModel> getTopN(List<ContentNodeModel> nodes, String factorName, int n, SessionInput input, final DataAccess dataAccess) {
        String userId = input.getCustomerId();
        PricingContext pricingCtx = input.getPricingContext();
        String[] variables = { factorName };
        OrderingFunction of = new OrderingFunction();
        for (ContentNodeModel model : nodes) {
            of.addScore(model, dataAccess.getVariables(userId, pricingCtx, model, variables));
        }
        List<ProductModel> results = new ArrayList<ProductModel>(n);
        List<Single> ranked = of.getRankedContents();
        int topN = Math.min(n, ranked.size());
        ContentFilter filter = FilterFactory.getInstance().createFilter(input.getExclusions(), input.isUseAlternatives(), input.isShowTemporaryUnavailable());
        Iterator<Single> it = ranked.iterator();
        while (topN > 0 && it.hasNext()) {
            RankedContent.Single rc = it.next();
            ContentNodeModel node = rc.getModel();
            if (node instanceof ProductModel) {
                ProductModel filteredModel = filter.filter((ProductModel) node); 
                if (filteredModel != null) {
                    results.add(filteredModel);
                    topN--;
                }
            }
        }
        return results;
    }    
    
    public static boolean matchSkuPrefix(ContentNodeModel model, String[] prefixes) {
        if (model instanceof ProductModel) {
            ProductModel pm = (ProductModel) model;
            List<SkuModel> skus = pm.getSkus();
            for (SkuModel sku : skus) {
                for (String prefix : prefixes) {
                    if (sku.getSkuCode().startsWith(prefix)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * @param model
     * @param key
     * @return the model, if it's the same as the key refers , or look up the model from the ContentFactory. 
     */
    public static ContentNodeModel getContentNodeModelOrLookup(ContentKey key, ContentNodeModel model) {
        ContentNodeModel result = key.equals(model.getContentKey()) ? model : ContentFactory.getInstance().getContentNodeByKey(key);
        return result;
    }
    
    /**
     * Add the node to the prioritized sets, and returns 0, because this function is used as a filter
     * and 0 means that the node should be removed from the active list.
     * 
     * @param da the data access object
     * @param model
     * @return 0
     */
    public static int addPrioritizedNode(DataAccess da, ContentNodeModel model) {
        da.addPrioritizedNode(model);
        return 0;
    }

    /**
     * Adds node to the posterior nodes list and indicates removal from the active list
     * by returning 0 value.
     * 
     * @param da the data access object
     * @param model
     * @return 0 (gets item removed from active nodes list)
     */
    public static int addDeprioritizedNode(DataAccess da, ContentNodeModel model) {
        da.addPosteriorNode(model);
        return 0;
    }

    public static List<ContentNodeModel> getPersonalizedExternalRecommendations(String providerName, SessionInput input) {
    	try {
			ExternalRecommender recommender = ExternalRecommenderRegistry.getInstance(providerName, ExternalRecommenderType.PERSONALIZED);
			List<RecommendationItem> items = recommender.recommendItems(new ExternalRecommenderRequest(input.getCustomerId()));
			List<ContentNodeModel> nodes = new ArrayList<ContentNodeModel>(items.size());
			for (RecommendationItem item : items) {
				ContentNodeModel node = ContentFactory.getInstance().getContentNode(item.getId());
				if (node != null)
					nodes.add(node);
			}
			
			// APPDEV-1633 trace nodes
			input.traceContentNodes(providerName, nodes);
			
			return nodes;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchExternalRecommenderException e) {
			e.printStackTrace();
		} catch (ExternalRecommenderCommunicationException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return new ArrayList<ContentNodeModel>();
    }

    public static List<ContentNodeModel> getRelatedExternalRecommendations(List<ContentNodeModel> paramNodes, String providerName, SessionInput input) {
    	try {
			ExternalRecommender recommender = ExternalRecommenderRegistry.getInstance(providerName, ExternalRecommenderType.RELATED);
			List<RecommendationItem> requestItems = new ArrayList<RecommendationItem>();
			for (ContentNodeModel node : paramNodes) {
				requestItems.add(new RecommendationItem(node.getContentKey().getId()));
			}
			List<RecommendationItem> items = recommender.recommendItems(new ExternalRecommenderRequest(requestItems));
			List<ContentNodeModel> nodes = new ArrayList<ContentNodeModel>(items.size());
			for (RecommendationItem item : items) {
				ContentNodeModel node = ContentFactory.getInstance().getContentNode(item.getId());
				if (node != null)
					nodes.add(node);
			}

			// APPDEV-1633 trace nodes
			input.traceContentNodes(providerName, nodes);

			return nodes;
		} catch (IllegalArgumentException e) {
		} catch (NoSuchExternalRecommenderException e) {
		} catch (ExternalRecommenderCommunicationException e) {
		} catch (NullPointerException e) {
		}
		return new ArrayList<ContentNodeModel>();
    }
    
    
    

    /**
     * Produce Smart YMAL recommendation based on session input
     * 
     * @param input
     * @return
     */
    public static List<ContentNodeModel> getSmartYMALRecommendation(SessionInput input) {
    	return SmartYMALRecommendationService.recommendYmalItems(input);
    }


    /**
     * APPDEV-1633 Trace nodes
     * @param input
     * @param dsName
     * @param nodes
     * @return
     */
    public static List<ContentNodeModel> trace(SessionInput input, String dsName, List<ContentNodeModel> nodes) {
    	if (input.isTraceMode()) {
    		input.traceContentNodes(dsName, nodes);
    	}
    	return nodes;
    }



    /**
     * Return all products that have customer rating
     * @return
     */
    public static List<ContentNodeModel> getCustomerRatedProducts() {
    	List<ContentNodeModel> prds = new ArrayList<ContentNodeModel>();
    	for (CustomerRatingsDTO dto : CustomerRatingsContext.getInstance().getCustomerRatings().values()) {
    		final String productId = dto.getProductId();
    		
    		ContentNodeModel node = ContentFactory.getInstance().getContentNode(FDContentTypes.PRODUCT, productId);
    		if (node != null)
    			prds.add(node);
    	}

    	return prds;
    }


   
    /**
     * Pull recommended items from a CMS Recommender
     * This method feeds data for 'SmartCategory' expression
     */
	public static List<? extends ContentNodeModel> getSmartCategoryRecommendation(
			SessionInput input) {
		if (input == null) {
			return Collections.emptyList();
		}

		Recommender rec = lookupCMSRecommenderInSessionInput(input);
		if (rec == null) {
			LOGGER.error("No recommender attached to target category !");
			return Collections.emptyList();
		}
		LOGGER.debug(".. Recommender: " + rec.getContentName());

		
		if (input.getCmsRecommenderKeys().contains(rec.getContentKey())) {
			LOGGER.warn("CMS Recommender call loop detected, abort!");
			return Collections.emptyList();
		}



		// prepare new context, build a new session input
		List<ContentNodeModel> scope = rec.getScope();
		if (scope == null || scope.size() == 0) {
			LOGGER.error("Recommender scope is not set!");
			return Collections.emptyList();
		}

		SessionInput subInput = cloneInput(input);
		// override explicit list
		subInput.setExplicitList(scope);
		// add recommender to call list
		subInput.getCmsRecommenderKeys().add(rec.getContentKey());



		// Fetch recommended items
		RecommendationService svc = CmsRecommenderRegistry.getInstance()
				.getService(rec.getStrategy().getContentName());

		return svc.recommendNodes(subInput);
	}




	private static Recommender lookupCMSRecommenderInSessionInput(SessionInput input) {
		ContentNodeModel target = null;

		final List<? extends ContentNodeModel> explicitList = input
				.getExplicitList();
		
		// Method #1 - Lookup explicit list
		if (explicitList != null && explicitList.size() > 0) {
			target = explicitList.get(0);
			LOGGER.debug("Picked first node from explicitList");
		} else {
			// Method #2 - See current node
			ContentNodeModel node = input.getCurrentNode();
			if (!(node instanceof CategoryModel)) {
				LOGGER.warn("current node is not a category!");
				LOGGER.warn(".. node = " + node);

				return null;
			}
			LOGGER.debug("Picked currentNode (fallback)");
			target = node;
		}

		// Grab CMS Recommender from target category
		CategoryModel cat = (CategoryModel) target;
		LOGGER.debug(".. Category: " + cat.getContentName());

		final Recommender rec = cat.getRecommender();
		if (rec == null) {
			LOGGER.warn("No recommender attached to category !");
		}

		return rec;
	}

	
    /**
     * Cloned session input for subsequent recommenders
     * 
     * @param input
     * @param expList
     * @return
     */
    private static SessionInput cloneInput(SessionInput input) {
		SessionInput smartInput = new SessionInput.Builder()
		.setCustomerId(input.getCustomerId())
		.setServiceType(input.getCustomerServiceType())
		.setPricingContext(input.getPricingContext())
		.setFulfillmentContext(input.getFulfillmentContext())
		.setCurrentNode(input.getCurrentNode())
		.setNoShuffle(input.isNoShuffle())
		.setTraceMode(input.isTraceMode())
		.setDataSourceMap(input.getDataSourcesMap())
		.setCmsRecommenderKeys(input.getCmsRecommenderKeys())
		.build();

		return smartInput;
    }
}

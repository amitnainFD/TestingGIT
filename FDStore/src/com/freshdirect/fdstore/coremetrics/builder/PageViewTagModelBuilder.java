package com.freshdirect.fdstore.coremetrics.builder;

import com.freshdirect.cms.ContentType;
import com.freshdirect.cms.fdstore.FDContentTypes;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.DepartmentModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.WineFilterValue;
import com.freshdirect.fdstore.coremetrics.CmContext;
import com.freshdirect.fdstore.coremetrics.tagmodel.PageViewTagModel;

public class PageViewTagModelBuilder  {
	

    /** enum for all category ids not listed in FDStoreProperties - used in CDF generation too */
	public enum CustomCategory {
        SEARCH, SO_TEMPLATE, ACCOUNT, BUYING_GUIDES, CART, ERROR, HOMEPAGE, INVITE, POPUPS, RECIPE, NEW_PRODUCTS_DEPARTMENT, ABOUT, DDPP, ECOUPON, CHECKOUT
	}
	
	private static final String INDEX_FILE = "index.jsp";
	private static final int INDEX_FILE_SUFFIX_LENGTH = INDEX_FILE.length();
    private static final String HOLIDAY_MEAL_BUNDLE_DIRECTORY_PATH_NAME = "hmb";

	private PageViewTagInput input;
	
	private Integer searchResultsSize;
	private String searchTerm;
	private String suggestedTerm;
	private Integer recipeSearchResultsSize;
	private ProductModel productModel;
	private ContentNodeModel currentFolder;
	private String recipeSource;
	private WineFilterValue wineFilterValue;
	private boolean wineFilterValueSet;
	private PageViewTagModel tagModel = new PageViewTagModel();
	
	private CmContext context = CmContext.getContext();
	
	public PageViewTagModel buildTagModel() throws SkipTagException {
		if (input == null) {
			throw new SkipTagException("Failed to build CM PageViewTag",
					new NullPointerException(
							"No input specified, abort now!"));
		}
		
		
		identifyPageAndCategoryId();
		identifyAttributes();
		return tagModel;
	}
	
	private void identifyPageAndCategoryId() throws SkipTagException {
		if (input.uri == null) {
			// CRASH NOW!
			throw new SkipTagException("Failed to build CM PageViewTag",
					new NullPointerException(
							"Missing / NULL 'uri' property"));
		}



		if (PageViewTagInput.SRC_JSON == input.source) {
			// SPECIAL TREATMENT FOR AJAX REQUESTS
			
			if ("SEARCH".equalsIgnoreCase(input.page)) {
				processSearchAttributes();
				tagModel.setPageId("search");
				tagModel.setCategoryId(CustomCategory.SEARCH.toString());
				decoratePageIdWithCatId(tagModel);
			
			} else if (input.uri.contains("filter")) {
				// "/api/filter" branch:
				findCurrentFolder( input.id );
				processDeptOrCat();
			}
			
			return;
		}


		// Go the standard way
		
		//uri always starts with a slash	
		String uriAfterSlash = input.uri.substring(1);
		int slashAfterDirNamePos = uriAfterSlash.indexOf("/");

		//uri has a directory name
		if (slashAfterDirNamePos>-1){ 
			String dirName = uriAfterSlash.substring(0, slashAfterDirNamePos);
			if (FDStoreProperties.getCoremetricsCatIdDirs().contains(dirName)){
				tagModel.setCategoryId(dirName);
				tagModel.setPageId(uriAfterSlash.substring(slashAfterDirNamePos+1));
				
				if ("wine".equals(tagModel.getCategoryId()) && "filter.jsp".equals(tagModel.getPageId())) {
					processWineFilter();
				}
				
				processHelpDir();
				decoratePageIdWithCatId(tagModel);

			} else if ("expressco".equals(dirName)){
				tagModel.setCategoryId(CustomCategory.CHECKOUT.toString());
				tagModel.setPageId(uriAfterSlash.substring(slashAfterDirNamePos+1));
				decoratePageIdWithCatId(tagModel);
            } else if (HOLIDAY_MEAL_BUNDLE_DIRECTORY_PATH_NAME.equals(dirName)) {
                findCurrentFolder(input.id);
                processDeptOrCat();
            }

		//uri has only a file name	
		} else {
			String fileName = TagModelUtil.dropExtension(uriAfterSlash);
			
			if ("search".equalsIgnoreCase(fileName) || "srch".equalsIgnoreCase(fileName) && (input.page == null || "SEARCH".equalsIgnoreCase(input.page))){
				
				if ("pres_picks".equalsIgnoreCase(input.page) && input.ppParentType != null) {
					// DDPP / President's Picks case
					// rule: category ID := embodying content ID
					findCurrentFolder( input.ppParentId );
					processDeptOrCat();

					tagModel.setPageId("search");
				} else {
					processSearchAttributes();
					tagModel.setPageId("search");
					tagModel.setCategoryId(CustomCategory.SEARCH.toString());
					decoratePageIdWithCatId(tagModel);
				}

			} else if ("department".equals(fileName) || "department_cohort_match".equals(fileName) || "category".equals(fileName) || "newsletter".equals(fileName) || "whatsgood".equals(fileName)  || "ddpp".equals(fileName) /* || "browse".equals(fileName) */){
				processDeptOrCat();
			} else if ("browse".equals(fileName) || "browse_special".equals(fileName) || uriAfterSlash.contains("srch.jsp") && "PRES_PICKS".equalsIgnoreCase(input.page)) {
				findCurrentFolder( input.id );
				processDeptOrCat();
			} else if ("product".equals(fileName) || "pdp".equals(fileName)) {
				processProduct();
			}
		} 
		
		//could not identify category from uri yet, try custom categorization rules 
		if (tagModel.getCategoryId()==null) {
			if (uriAfterSlash.contains("popup.jsp") || uriAfterSlash.contains("pop.jsp")){
				tagModel.setCategoryId(CustomCategory.POPUPS.toString());

			//recipe begin
			} else if (uriAfterSlash.contains("recipe.jsp")){
				tagModel.setCategoryId(CustomCategory.RECIPE.toString());

			} else if (uriAfterSlash.contains("recipe_print.jsp")){
				tagModel.setCategoryId(CustomCategory.RECIPE.toString());
				tagModel.setPageId("PRINT");

			} else if (uriAfterSlash.contains("recipe_search.jsp")){
				processSearchAttributes();
				tagModel.setCategoryId(CustomCategory.RECIPE.toString());
				tagModel.setPageId("SEARCH");
			
			} else if (uriAfterSlash.contains("recipe_dept.jsp") || uriAfterSlash.contains("recipe_cat.jsp") || uriAfterSlash.contains("recipe_subcat.jsp")){
				if (currentFolder==null) {
					throw new SkipTagException("currentFolder is null");
				} else {				
					tagModel.setCategoryId(CustomCategory.RECIPE.toString());
					tagModel.setPageId(currentFolder.getFullName());
				}

			} else if (uriAfterSlash.contains("recipe_source.jsp")){
				if (recipeSource==null) {
					throw new SkipTagException("recipeSource is null");
				} else {
					tagModel.setCategoryId(CustomCategory.RECIPE.toString());
					tagModel.setPageId(recipeSource);
				}
			//recipe end	
				
			} else if (uriAfterSlash.contains("invite")){
				tagModel.setCategoryId(CustomCategory.INVITE.toString());
				tagModel.setPageId("PERSONAL");

			} else if (uriAfterSlash.contains(INDEX_FILE)){
				tagModel.setCategoryId(CustomCategory.HOMEPAGE.toString());

				int uriPathLen = uriAfterSlash.length() - INDEX_FILE_SUFFIX_LENGTH - 1; //remove slash as well
				if (uriPathLen > 0){
					tagModel.setPageId(uriAfterSlash.substring(0, uriPathLen)); //use path without file name as page name
				}

			} else if (uriAfterSlash.contains("error.jsp") || uriAfterSlash.contains("unsupported.jsp")){
				tagModel.setCategoryId(CustomCategory.ERROR.toString());
			
			} else if (uriAfterSlash.contains("cart.jsp") || uriAfterSlash.contains("confirm.jsp") || uriAfterSlash.contains("cart_confirm_pdp.jsp") || uriAfterSlash.contains("quickbuy")
					|| uriAfterSlash.contains("shop5.jsp") || uriAfterSlash.contains("product_modify.jsp")){
				tagModel.setCategoryId(CustomCategory.CART.toString());
				
			} else if (uriAfterSlash.contains("cheese/101_") || uriAfterSlash.contains("coffee/coffee_")
					|| uriAfterSlash.contains("producers_map.jsp") || uriAfterSlash.contains("peakproduce.jsp")
					|| uriAfterSlash.contains("rating_ranking.jsp") || uriAfterSlash.contains("seasonal_guide.jsp")
					|| uriAfterSlash.contains("nutrition_info.jsp")){
				tagModel.setCategoryId(CustomCategory.BUYING_GUIDES.toString());
				
			//account begin
			} else if (uriAfterSlash.contains("main/account_details.jsp")){
				tagModel.setCategoryId(CustomCategory.ACCOUNT.toString());
				tagModel.setPageId("MAIN");

			} else if (uriAfterSlash.contains("logout.jsp")){
				tagModel.setCategoryId(CustomCategory.ACCOUNT.toString());
				tagModel.setPageId("LOG OUT");
			//account end
				
			} else if (uriAfterSlash.contains("newproducts.jsp") || uriAfterSlash.contains("srch.jsp") && "NEWPRODUCTS".equalsIgnoreCase(input.page)){
				tagModel.setCategoryId(CustomCategory.NEW_PRODUCTS_DEPARTMENT.toString());
				tagModel.setPageId("");

			} else if (uriAfterSlash.contains("ecoupon.jsp") || uriAfterSlash.contains("srch.jsp") && "ECOUPON".equalsIgnoreCase(input.page)){
				tagModel.setCategoryId(CustomCategory.ECOUPON.toString());
				tagModel.setPageId("ecoupons");

			} else if (uriAfterSlash.contains("cos.jsp")){
				tagModel.setCategoryId(CustomCategory.HOMEPAGE.toString());
				tagModel.setPageId("cos.jsp");

			} else if (uriAfterSlash.contains("welcome.jsp")){
				tagModel.setCategoryId(CustomCategory.ABOUT.toString());
				tagModel.setPageId("welcome.jsp");
			}
			
			//if found category based on rules defined above
			if (tagModel.getCategoryId()!=null) {
				//fill page id if still empty
				if (tagModel.getPageId()==null){
					tagModel.setPageId(uriAfterSlash);
				}
				
				decoratePageIdWithCatId(tagModel);
			}
		}
		
		//could not identify category from uri, fallback to other category
		if (tagModel.getCategoryId()==null) {
			tagModel.setPageId(uriAfterSlash);
			decoratePageIdWithCatId(tagModel);
			setPrefixedCategoryId(FDStoreProperties.getCoremetricsCatIdOtherPage());
		} else {
			setPrefixedCategoryId(tagModel.getCategoryId());
		}
	}

	
	private void setPrefixedCategoryId(final String categoryId) {
		tagModel.setCategoryId( context.prefixedCategoryId(categoryId) );
	}

	/**
	 * Find the current container content node (LeftNav)
	 */
	private static final ContentType CONTAINER_TYPES[] = { FDContentTypes.DEPARTMENT, FDContentTypes.CATEGORY, FDContentTypes.SUPER_DEPARTMENT };
	private void findCurrentFolder(final String contentId) {
		final ContentFactory f = ContentFactory.getInstance();  

		if (contentId != null) {
			for (ContentType t : CONTAINER_TYPES) {
				currentFolder = f.getContentNode( t, contentId);
				if (currentFolder != null) {
					return;
				}
			}
		}

		// We are sitting in a deep hole wondering where the Content ID was gone.
		// No wayout. Shoot the horses. Hope is lost to make it to the destination.
	}

	public static void decoratePageIdWithCatId(PageViewTagModel tagModel){
		tagModel.setPageId(tagModel.getCategoryId().replace("_", " ").toUpperCase() + TagModelUtil.PAGE_ID_DELIMITER + tagModel.getPageId());
	}

	private void processHelpDir() throws SkipTagException{
		if ("help".equals(tagModel.getCategoryId())){
			String pageParam = input.page;
			if (pageParam != null){
				tagModel.setPageId(pageParam);

			} else if(tagModel.getPageId().contains("faq_search")){
				processSearchAttributes();
				tagModel.setPageId("faq_search");
			}
		}
	}

	private void processDeptOrCat() throws SkipTagException{
		if (currentFolder==null){
			throw new SkipTagException("currentFolder is null");
		} else {
			StringBuilder sb = new StringBuilder();
			getDeptOrCatPageId(currentFolder, sb);
			tagModel.setPageId(sb.toString());
			tagModel.setCategoryId(currentFolder.getContentKey().getId());
		}
	}
	
	private void getDeptOrCatPageId(ContentNodeModel curFolder, StringBuilder sb){
		if (curFolder != null){
			if (curFolder instanceof DepartmentModel){
				sb.append(curFolder.getFullName());
	
			} else {
				getDeptOrCatPageId(curFolder.getParentNode(), sb);
				sb.append(TagModelUtil.PAGE_ID_DELIMITER).append(curFolder.getFullName());
			}
		}
	}

	private void processProduct() throws SkipTagException{
		if (productModel==null){
			throw new SkipTagException("productModel is null");
		} else {
			tagModel.setPageId(TagModelUtil.getPageIdFromProductModel(productModel));
			tagModel.setCategoryId(productModel.getParentId());
		}
	}

	
	/**
	 * Page view tags only fire for search pages if the searchResultsSize and searchTerm is set. 
	 * Therefore the page view tag placed at the top of all pages (which doesn't have these pieces of information yet) won't fire. 
	 * But the additional page view tags placed in the search and faq_search pages will.
	 * @throws CmTagMissingAttributesException 
	 */
	private void processSearchAttributes() throws SkipTagException{
		if (searchResultsSize == null || searchTerm == null){
			throw new SkipTagException("searchResultsSize or searchTerm is null");
		
		} else {
			tagModel.setSearchTerm(searchTerm);
			tagModel.setSearchResults(searchResultsSize.toString());
		}
	}
	
	private void processWineFilter() throws SkipTagException{
		if (wineFilterValueSet){
			if (wineFilterValue != null){
				tagModel.setPageId(wineFilterValue.getDomainName());
			}
		} else {
			throw new SkipTagException("wineFilterValue is not set");
		}
	}
	
	private void identifyAttributes(){
		if (suggestedTerm != null) {
			tagModel.getAttributesMaps().put(1, suggestedTerm);
		}
		if (recipeSearchResultsSize != null) {
			tagModel.getAttributesMaps().put(2, recipeSearchResultsSize.toString());
		}

		//Additional Coremetrics attributes [APPDEV-3073]
		int currentAttributeIndex = 3;
		//Up to 4 items maximum
		for (ContentNodeModel contentNode : TagModelUtil.getPageLocationSubset(ContentFactory.getInstance().getContentNode(tagModel.getCategoryId()))) {
			tagModel.getAttributesMaps().put(currentAttributeIndex++, contentNode.getContentName());
		}
		
		if (tagModel.getAttributesMaps().get(3) == null) {
			tagModel.getAttributesMaps().put(3, tagModel.getPageId());
		}
		tagModel.getAttributesMaps().put(7, tagModel.getPageId());
		
	}

	/**
	 * Sets context object (mandatory)
	 * @param input
	 */
	public void setInput(PageViewTagInput input) {
		this.input = input;
	}
	
	public void setSearchResultsSize(Integer searchResultsSize) {
		this.searchResultsSize = searchResultsSize;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public void setSuggestedTerm(String suggestedTerm) {
		this.suggestedTerm = suggestedTerm;
	}

	public void setRecipeSearchResultsSize(Integer recipeSearchResultsSize) {
		this.recipeSearchResultsSize = recipeSearchResultsSize;
	}

	public void setProductModel(ProductModel productModel) {
		this.productModel = productModel;
	}

	public void setCurrentFolder(ContentNodeModel currentFolder) {
		this.currentFolder = currentFolder;
	}
	
	public void setRecipeSource(String recipeSource) {
		this.recipeSource = recipeSource;
	}

	public void setWineFilterValue(WineFilterValue wineFilterValue) {
		this.wineFilterValue = wineFilterValue;
		this.wineFilterValueSet = true; 
	}
}
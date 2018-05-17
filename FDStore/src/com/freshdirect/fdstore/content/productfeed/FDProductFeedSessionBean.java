package com.freshdirect.fdstore.content.productfeed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ejb.EJBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Category;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.application.CmsManager;
import com.freshdirect.cms.fdstore.FDContentTypes;
import com.freshdirect.cms.fdstore.PreviewLinkProvider;
import com.freshdirect.common.pricing.CharacteristicValuePrice;
import com.freshdirect.common.pricing.MaterialPrice;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.common.pricing.ZoneInfo;
import com.freshdirect.content.attributes.EnumAttributeName;
import com.freshdirect.content.attributes.EnumAttributeType;
import com.freshdirect.erp.model.ErpInventoryModel;
import com.freshdirect.fdstore.FDCachedFactory;
import com.freshdirect.fdstore.FDGroup;
import com.freshdirect.fdstore.FDGroupNotFoundException;
import com.freshdirect.fdstore.FDNutrition;
import com.freshdirect.fdstore.FDProduct;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSalesUnit;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.FDVariation;
import com.freshdirect.fdstore.FDVariationOption;
import com.freshdirect.fdstore.GroupScalePricing;
import com.freshdirect.fdstore.ZonePriceListing;
import com.freshdirect.fdstore.ZonePriceModel;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.productfeed.Attributes.ProdAttribute;
import com.freshdirect.fdstore.content.productfeed.Configurations.Configuration;
import com.freshdirect.fdstore.content.productfeed.Configurations.Configuration.VariationOption;
import com.freshdirect.fdstore.content.productfeed.GroupPrices.GroupPrice;
import com.freshdirect.fdstore.content.productfeed.Images.Image;
import com.freshdirect.fdstore.content.productfeed.Inventories.Inventory;
import com.freshdirect.fdstore.content.productfeed.NutritionInfo.Nutrition;
import com.freshdirect.fdstore.content.productfeed.Prices.Price;
import com.freshdirect.fdstore.content.productfeed.Products.Product;
import com.freshdirect.fdstore.content.productfeed.Ratings.Rating;
import com.freshdirect.fdstore.content.productfeed.SaleUnits.SaleUnit;
import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;


public class FDProductFeedSessionBean extends SessionBeanSupport {
    
	private static final long	serialVersionUID	= 270497771665382812L;
	
	private static Category LOGGER = LoggerFactory.getInstance( FDProductFeedSessionBean.class );
	
	private static final String RATING = "RATING";
	private static final String SUSTAINABILITY_RATING = "SUSTAINABILITY_RATING";
	private static final String PRODUCT_CATEGORY_IMAGE = "PRODUCT_CATEGORY_IMAGE";
	private static final String PRODUCT_FEATURE_IMAGE = "PRODUCT_FEATURE_IMAGE";
	private static final String PRODUCT_DETAIL_IMAGE = "PRODUCT_DETAIL_IMAGE";
	private static final String PRODUCT_ZOOM_IMAGE = "PRODUCT_ZOOM_IMAGE";
	private static final String PRODUCT_IMAGE = "PRODUCT_IMAGE";
	private static final String URL_DOMAIN ="https://www.freshdirect.com";
		    
    /** Constructor
     */
    public FDProductFeedSessionBean() {
        super();
    }
    
    /**
     * Template method that returns the cache key to use for caching resources.
     *
     * @return the bean's home interface name
     */
    protected String getResourceCacheKey() {
        return "com.freshdirect.content.productfeed.ejb.FDProductFeedHome";
    }
    
    /** Remove method for container to call
     * @throws EJBException throws EJBException if there is any problem.
     */
    public void ejbRemove() {
    }
    
    public boolean uploadProductFeed() throws FDResourceException, RemoteException {
    	
    	try {

			LOGGER.info("Inside uploadProductFeed()..");
			Products xmlProducts = new Products();
			JAXBContext jaxbCtx = JAXBContext.newInstance(Products.class);
			populateProducts(xmlProducts);								
			uploadProductFeedFile(xmlProducts, jaxbCtx);
			LOGGER.info("Available products fetched & uploaded: "+xmlProducts.getProduct().size());

		} catch (Exception e) {
			LOGGER.error("Exception :"+e.getMessage());
			throw new FDResourceException(e);
		}
    	return true;
    }
    
    
    private void populateProducts(Products xmlProducts)
			throws FDResourceException {
		Set<ContentKey> skuContentKeys = CmsManager.getInstance().getContentKeysByType(FDContentTypes.SKU);
		if(null !=skuContentKeys){
			LOGGER.info("Skus in CMS: "+skuContentKeys.size());
			for (Iterator<ContentKey> i = skuContentKeys.iterator(); i.hasNext();) {
				ContentKey key = (ContentKey) i.next();
				String skucode = key.getId();
//			    if(skucode.equals("VAR3770250") || skucode.equals("MEA1075690")|| skucode.equals("DAI0069651") || skucode.equals("MEA1075865")) {
					ProductModel productModel =null;
					FDProductInfo fdProductInfo = null;
					FDProduct fdProduct = null;
					try {
						productModel =ContentFactory.getInstance().getProduct(skucode);
						if(null != productModel && !productModel.isOrphan() && !"Archive".equalsIgnoreCase(productModel.getDepartment().getContentName())){
							fdProductInfo = FDCachedFactory.getProductInfo(skucode);
							fdProduct = FDCachedFactory.getProduct(fdProductInfo);		
							
						}
					} catch (FDSkuNotFoundException e) {
						//Ignore
					}
					if(null != fdProductInfo && null != fdProduct){						
										
						Product product = new Product();			
						xmlProducts.getProduct().add(product);
						
						populateProductBasicInfo(productModel, fdProductInfo,fdProduct, product);
						
						populateAttributes(fdProductInfo, fdProduct, product, productModel);
						
						populatePricingInfo(fdProduct,  product);		
										
						populateGroupScalePriceInfo(skucode,  product);		
											
						populateSalesUnitInfo(fdProduct,product);
											
						populateNutritionInfo(fdProduct, product);									
						
						populateConfigurationsInfo(fdProduct, product,productModel.getUserContext().getPricingContext());										
						
						populateRatingInfo(fdProductInfo,  product,"1000");//::FDX:: What plant to consider?
											
						populateInventoryInfo(fdProductInfo,  product);
						
						populateImages(productModel, product);	
					}
//				}
			}
		}
	}

	private void uploadProductFeedFile(Products xmlProducts,
			JAXBContext jaxbCtx) throws FDResourceException {
		
		File rawProductFile = null;
		String zipFileName = null;
		try {
			if(!xmlProducts.getProduct().isEmpty()) {
				FileInputStream in = null;
				ZipOutputStream zos = null;
				try {
					byte[] buffer = new byte[1024];
					Marshaller mar =jaxbCtx.createMarshaller();
					mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
					
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
					String fileName = "FDProductFeed_" + df.format(new Date());
					
					//Serialize to a Xml file!
					rawProductFile = new File(fileName + ".xml");
					StreamResult result = new StreamResult(rawProductFile);				
					mar.marshal(xmlProducts, result);
					
					//Zip it!
					zipFileName = fileName + ".zip";
					FileOutputStream fos = new FileOutputStream(zipFileName);
					zos = new ZipOutputStream(fos);
					ZipEntry ze = new ZipEntry(rawProductFile.getName());
					zos.putNextEntry(ze);
					
					int len;
					in = new FileInputStream(rawProductFile);
					while ((len = in.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				} catch (Exception e) {
					 throw new FDResourceException("feed serialization failed: " + e.getMessage(), e);
				} finally {
					try {
						if (in != null) {
							in.close();
						}
						if (zos != null) {
							zos.closeEntry();
							zos.close();
						}
					} catch (Exception e2) {
						// Ignore Me
					}
				}
	
				
				
				//Upload it to subscribers!
				Connection conn = null;
				try {
					conn = getConnection();
					List<ProductFeedSubscriber> productFeedSubscribers = ProductFeedDAO.getAllProductFeedSubscribers(conn);	
					
					if(productFeedSubscribers != null) {
						for(ProductFeedSubscriber subscriber : productFeedSubscribers) {
							if(ProductFeedSubscriberType.FTP.equals(subscriber.getType())) {
								FeedUploader.uploadToFtp(subscriber.getUrl(), subscriber.getUserid(), subscriber.getPassword()
																, subscriber.getDefaultUploadPath(), zipFileName);
							} else if(ProductFeedSubscriberType.SFTP.equals(subscriber.getType())) {
								FeedUploader.uploadToSftp(subscriber.getUrl(), subscriber.getUserid(), subscriber.getPassword()
																, subscriber.getDefaultUploadPath(), zipFileName);
							} else if (ProductFeedSubscriberType.S3.equals(subscriber.getType())) {
								FeedUploader.uploadToS3(subscriber.getUserid(), subscriber.getPassword()
															, subscriber.getUrl(), zipFileName);
							} else {
								LOGGER.warn( "unKnown product feed subscriber:" + subscriber);
							}
						}
					}
				} catch (SQLException e) {
					LOGGER.error( "FeedSubscriber DaO CONNECTION failed with SQLException: ", e );
					throw new FDResourceException("FeedSubscriber DAO CONNECTION failed with SQLException: " + e.getMessage(), e);
				} finally {
					if ( conn != null ) {
						try { 
							conn.close();
						} catch (SQLException e) {
							LOGGER.error( "connection close failed with SQLException: ", e );
						}
					}
				}				
	
			} else {
				throw new FDResourceException("No feed file generated please check the log for error");
			}
		} finally {
			//Cleanup!
			if(rawProductFile != null) {
				LOGGER.info("raw product file about to be deleted :"+rawProductFile.getAbsolutePath());
				rawProductFile.delete();
			}
			if(zipFileName != null) {
				//Remove the generated product feed zip file
				File dfile = new File(zipFileName);
				LOGGER.info("zipped product file about to be deleted :"+dfile.getAbsolutePath());
				dfile.delete();
			}			
		}		
	}

	private void populateProductBasicInfo(ProductModel productModel,
			FDProductInfo fdProductInfo, FDProduct fdProduct, Product product) {
		
		ZoneInfo zone=productModel.getUserContext().getPricingContext().getZoneInfo();
		product.setSkuCode(fdProduct.getSkuCode());
		product.setUpc(fdProductInfo.getUpc());
		product.setMaterialNum(fdProduct.getMaterial().getMaterialNumber());
		product.setProdId(productModel.getContentName());
		product.setProdName(productModel.getFullName());
		product.setProdUrl(URL_DOMAIN+PreviewLinkProvider.getLink(productModel.getContentKey()));
		populateParentInfo(productModel, product);
		product.setDeptId(productModel.getDepartment().getContentName());
		product.setProdStatus(null !=fdProductInfo.getAvailabilityStatus(zone.getSalesOrg(),zone.getDistributionChanel())?fdProductInfo.getAvailabilityStatus(zone.getSalesOrg(),zone.getDistributionChanel()).getStatusCode():"");
		product.setMinQuantity(""+productModel.getQuantityMinimum());
		product.setMaxQuantity(""+productModel.getQuantityMaximum());
		product.setQtyIncrement(""+productModel.getQuantityIncrement());
	}

	private void populateParentInfo(ProductModel productModel, Product product) {
		product.setCatId(productModel.getParentId());
		product.setParentCatId(productModel.getParentId());
		product.setRootCatId(productModel.getParentId());
		product.setSubCatId(productModel.getParentId());
		if(null !=productModel.getCategory()){
			ContentNodeModel contentNode =ContentFactory.getInstance().getContentNode(productModel.getCategory().getParentId());
			if(null !=contentNode && FDContentTypes.CATEGORY.equals(contentNode.getContentKey().getType())){
				product.setParentCatId(contentNode.getContentName());
				contentNode =ContentFactory.getInstance().getContentNode(contentNode.getParentId());
				if(null !=contentNode && FDContentTypes.CATEGORY.equals(contentNode.getContentKey().getType())){
					product.setRootCatId(contentNode.getContentName());
				}else {
					product.setRootCatId(productModel.getCategory().getParentId());
				}
			}
		}
	}

	private void populateImages(ProductModel productModel,
			Product product) {
		
		Images images = new Images();
		product.setImages(images);								
		Image image = null;
		if(null !=productModel.getProdImage()){
			image = new Image();
			image.setImgType(PRODUCT_IMAGE);
			image.setImgUrl(URL_DOMAIN+productModel.getProdImage().getPath());
			images.getImage().add(image);
		}
		if(null !=productModel.getZoomImage()){
			image = new Image();
			image.setImgType(PRODUCT_ZOOM_IMAGE);
			image.setImgUrl(URL_DOMAIN+productModel.getZoomImage().getPath());
			images.getImage().add(image);
		}
		if(null !=productModel.getDetailImage()){
			image = new Image();
			image.setImgType(PRODUCT_DETAIL_IMAGE);
			image.setImgUrl(URL_DOMAIN+productModel.getDetailImage().getPath());
			images.getImage().add(image);
		}
		if(null !=productModel.getFeatureImage()){
			image = new Image();
			image.setImgType(PRODUCT_FEATURE_IMAGE);
			image.setImgUrl(URL_DOMAIN+productModel.getFeatureImage().getPath());
			images.getImage().add(image);
		}
		if(null !=productModel.getCategoryImage()){
			image = new Image();					
			image.setImgType(PRODUCT_CATEGORY_IMAGE);
			image.setImgUrl(URL_DOMAIN+productModel.getCategoryImage().getPath());
			images.getImage().add(image);
		}
	}

	private void populateAttributes(FDProductInfo fdProductInfo, FDProduct fdProduct, Product product,ProductModel productModel) {
		
		Attributes attributes = new Attributes();
		product.setAttributes(attributes);		
		
		for (Iterator iterator = EnumAttributeName.iterator(); iterator.hasNext();) {			
			ProdAttribute attribute = new ProdAttribute();			
			attributes.getProdAttribute().add(attribute);
			EnumAttributeName enumAttr = (EnumAttributeName) iterator.next();
			attribute.setAttrName(enumAttr.getName());	
			attribute.setAttrValue("");
			Object attrValue = null;
			if(EnumAttributeType.BOOLEAN.equals(enumAttr.getType())){
				attrValue = fdProduct.getMaterial().getAttributeBoolean(enumAttr);
			}else if(EnumAttributeType.INTEGER.equals(enumAttr.getType())){
				attrValue = fdProduct.getMaterial().getAttributeInt(enumAttr);
			}else{
				attrValue = fdProduct.getMaterial().getAttribute(enumAttr);
			}
			if(null != attrValue){
				if(attrValue instanceof Boolean){
					attribute.setAttrValue(attrValue.toString());
				}else{
					attribute.setAttrValue(""+attrValue);
				}
			}
			
			
		}		
	}

	private void populateConfigurationsInfo(
			FDProduct fdProduct, Product product,PricingContext pCtx) {
		
		Configurations configurations = new Configurations();
		product.setConfigurations(configurations);			
		
		FDVariation[] fdVariations=fdProduct.getVariations();
		if(null !=fdVariations){
			for (int j = 0; j < fdVariations.length; j++) {
				FDVariation fdVariation = fdVariations[j];
				Configuration configuration = new Configuration();			
				configurations.getConfiguration().add(configuration);
				configuration.setCharName(fdVariation.getName());
				FDVariationOption[] options =fdVariation.getVariationOptions();
				if(null !=options){
					for (int k = 0; k < options.length; k++) {
						FDVariationOption fdVariationOption = options[k];
						CharacteristicValuePrice[] cvPrices =fdProduct.getPricing().getCharacteristicValuePrices(pCtx);
						VariationOption variationOption = new VariationOption();
						double price = 0.0;
						String pricingUnit = "NA";
						if(null != cvPrices){
							for (int l = 0; l < cvPrices.length; l++) {
								CharacteristicValuePrice characteristicValuePrice = cvPrices[l];
								if(characteristicValuePrice.getCharValueName().equalsIgnoreCase(fdVariationOption.getName())){
									price = characteristicValuePrice.getPrice();
									pricingUnit = characteristicValuePrice.getPricingUnit();
									break;
								}
								
							}
						}
															
						variationOption.setCharValueName(fdVariationOption.getName());								
						variationOption.setCharValueDesc(fdVariationOption.getDescription());						
						variationOption.setPrice(BigDecimal.valueOf(price));
						variationOption.setPricingUnit(pricingUnit);
						
						configuration.getVariationOption().add(variationOption);
						
					}
				}				
			}
		}
	}

	private void populateInventoryInfo(FDProductInfo fdProductInfo,
			 Product product) {
				
		ErpInventoryModel inventoryModel = fdProductInfo.getInventory();
		if(null !=inventoryModel){
			Inventories inventories = new Inventories();
			product.setInventories(inventories);			
			Inventory inventory = new Inventory();			
			inventories.getInventory().add(inventory);
			Calendar cal = Calendar.getInstance();
			cal.setTime(inventoryModel.getInventoryStartDate());
			inventory.setStartDate(cal);		
			inventory.setQuantity(""+inventoryModel.getEntries().get(0).getQuantity());
		}
	}

	private void populateRatingInfo(FDProductInfo fdProductInfo,
			 Product product,String plantID) {
		
		Ratings ratings = new Ratings();
		product.setRatings(ratings);
		Rating rating = null;
		if(null !=fdProductInfo.getSustainabilityRating(plantID)){
			rating = new Rating();			
			ratings.getRating().add(rating);
					
			rating.setRatingType(SUSTAINABILITY_RATING);			
			rating.setDesc(fdProductInfo.getSustainabilityRating(plantID).getShortDescription());
			rating.setRatingType(fdProductInfo.getSustainabilityRating(plantID).getStatusCode());
		}
		if(null !=fdProductInfo.getRating(plantID)){
			rating = new Rating();			
			ratings.getRating().add(rating);
			
			rating.setRatingType(RATING);			
			rating.setDesc(fdProductInfo.getRating(plantID).getShortDescription());
			rating.setRatingType(fdProductInfo.getRating(plantID).getStatusCode());
		}
	}

	private void populateGroupScalePriceInfo(String skucode,
			 Product product) throws FDResourceException {
		
		GroupScalePricing gsp = null;
		GroupPrices groupPrices;
		try {
               FDGroup fdGroup =FDCachedFactory.getProductInfo(skucode).getGroup(ZonePriceListing.DEFAULT_SALES_ORG,ZonePriceListing.DEFAULT_DIST_CHANNEL);			
			gsp = null !=fdGroup?FDCachedFactory.getGrpInfo(fdGroup):null;
		} catch (FDSkuNotFoundException e) {
			// DO Nothing
		} catch (FDGroupNotFoundException e) {
			// DO Nothing
		}
		if(null != gsp){
			groupPrices = new GroupPrices();
			product.setGroupPrices(groupPrices);
			GroupPrice groupPrice = new GroupPrice();			
			groupPrices.getGroupPrice().add(groupPrice);
			
			groupPrice.setZoneCode(ZonePriceListing.DEFAULT_ZONE_INFO.getPricingZoneId());//::FDX::
			groupPrice.setGroupMaterials(gsp.getMatList().toString());
			if(null !=gsp.getGrpZonePrice(ZonePriceListing.DEFAULT_ZONE_INFO)){//::FDX::
				groupPrice.setUnitPrice(BigDecimal.valueOf(gsp.getGrpZonePrice(ZonePriceListing.DEFAULT_ZONE_INFO).getMaxUnitPrice()));//::FDX::
				groupPrice.setUnitWeight(gsp.getGrpZonePrice(ZonePriceListing.DEFAULT_ZONE_INFO).getMaterialPrices()[0].getPricingUnit());//::FDX::
				groupPrice.setGroupQuantity(""+gsp.getGrpZonePrice(ZonePriceListing.DEFAULT_ZONE_INFO).getMaterialPrices()[0].getScaleLowerBound());//::FDX::
			}
			groupPrice.setGroupId(gsp.getGroupId());
			groupPrice.setGroupDesc(gsp.getLongDesc());
		}
	}

	private void populatePricingInfo(FDProduct fdProduct, 
			Product product) {
		Prices prices = new Prices();
		product.setPrices(prices);
		
		ZonePriceModel zpModel = fdProduct.getPricing().getZonePrice(ZonePriceListing.DEFAULT_ZONE_INFO);
		MaterialPrice[] materialPrices =zpModel.getMaterialPrices();
		for (MaterialPrice materialPrice : materialPrices) {
			Price price = new Price();			
			prices.getPrice().add(price);
			
			price.setZoneCode(ZonePriceListing.MASTER_DEFAULT_ZONE);					
			price.setUnitPrice(BigDecimal.valueOf(materialPrice.getPrice()));
			price.setUnitDescription(materialPrice.getPricingUnit());
			price.setUnitWeight(materialPrice.getPricingUnit());
			
			if(materialPrice.getPromoPrice() > 0.0){
				price.setSalePrice(BigDecimal.valueOf(materialPrice.getPromoPrice()));
			}
			
			price.setScaleQuantity(""+materialPrice.getScaleLowerBound());
			
		}
	}

	private void populateSalesUnitInfo(FDProduct fdProduct,
			 Product product) {
		FDSalesUnit[] salesUnits = fdProduct.getSalesUnits();
		SaleUnits saleUnits = new SaleUnits();
		product.setSaleUnits(saleUnits);		
		
		for (FDSalesUnit fdSalesUnit : salesUnits) {
			SaleUnit saleUnit = new SaleUnit();			
			saleUnits.getSaleUnit().add(saleUnit);
			
			saleUnit.setName(fdSalesUnit.getName());			
			saleUnit.setBaseUnit(fdSalesUnit.getBaseUnit());
			saleUnit.setDescription(fdSalesUnit.getDescription());
			if("LB".equalsIgnoreCase(fdSalesUnit.getBaseUnit()) && !"LB".equalsIgnoreCase(fdSalesUnit.getName()) ){
				saleUnit.setEstimatedWeight(""+(fdSalesUnit.getNumerator()/(double)fdSalesUnit.getDenominator()));
			}
		}
	}

	private void populateNutritionInfo(FDProduct fdProduct,
			 Product product) {
		NutritionInfo nutritionInfo = new NutritionInfo();
		product.setNutritionInfo(nutritionInfo);			
		List<FDNutrition> nutritionList = fdProduct.getNutrition();
		if(null != nutritionList)
		for (Iterator iterator = nutritionList.iterator(); iterator
				.hasNext();) {
			FDNutrition fdNutrition = (FDNutrition) iterator.next();
			Nutrition nutrition = new Nutrition();			
			nutritionInfo.getNutrition().add(nutrition);
			nutrition.setNutritionType(fdNutrition.getName());
			nutrition.setUom(fdNutrition.getUnitOfMeasure());
			nutrition.setValue(BigDecimal.valueOf(fdNutrition.getValue()));
			
		}
	}   
}

package com.freshdirect.fdstore.content.customerrating;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.application.CmsManager;
import com.freshdirect.cms.fdstore.FDContentTypes;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.BrandModel;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.DepartmentModel;
import com.freshdirect.fdstore.content.ProductContainer;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.StoreModel;
import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.framework.util.RuntimeServiceUtil;
import com.freshdirect.framework.util.log.LoggerFactory;

@Deprecated
public class UploadFeedProcessTask {
	

	private static final String ID_RESTRICTIONS = "[^A-Za-z0-9_]";

	private static final Logger LOGGER = LoggerFactory.getInstance(UploadFeedProcessTask.class);
	
	private List<ProductContainer> categoryStore = new ArrayList<ProductContainer>();
	private List<ProductModel> productStore = new ArrayList<ProductModel>();
	private Set<BrandModel> brandStore = new HashSet<BrandModel>();
	
	private static final String BASE_URL = "http://www.freshdirect.com";
	private static final String CAT_URL = "/category.jsp?catId=";
	private static final String PROD_URL = "/product.jsp?catId=";
	private static final String PROD_PARAM_URL = "&productId=";
	
	private final String UPLOAD_FEED_FILE_NAME = "freshDirectData.xml";
	private String uploadFeedFilePath;
	
	public BazaarvoiceFeedProcessResult process(){
		
		try {
			saveUploadFeedFile(createDocument());
			uploadFile();
		} catch (FDResourceException e) {
			LOGGER.error("Bazaarvoice feed creation failed!",e);
			return new BazaarvoiceFeedProcessResult(false, e.getMessage());
		}
		
		LOGGER.info("Feed creation complete.");
		return new BazaarvoiceFeedProcessResult(true, null);
	}
	
	private Document createDocument() throws FDResourceException{
		
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder=null;
		Document doc = null;
		
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			doc = documentBuilder.newDocument();
			doc.setXmlStandalone(true);
		} catch (ParserConfigurationException e) {
			throw new FDResourceException("Feed document creation failed!", e);
		}
		
		StoreModel store = ContentFactory.getInstance().getStore();
		for (DepartmentModel dept : store.getDepartments()) {
			collectAllCategories(dept);
		}
		collectAllProductsAndBrands();
		
		Element root = decorateDocumentWithRoot(doc);
		decorateDocumentWithBrands(doc, root);
		decorateDocumentWithCategories(doc, root, store);
		decorateDocumentWithProducts(doc, root);
		
		return doc;
	}
	
	private Element decorateDocumentWithRoot(Document doc){
		Element root = doc.createElement("Feed");
		doc.appendChild(root);
		Date now = new Date();
		root.setAttribute("name", "FreshDirect");
		root.setAttribute("extractDate", DateUtil.format(now)+"T"+DateUtil.formatSimpleTime(now));
		root.setAttribute("incremental", "false");
		root.setAttribute("xmlns", "http://www.bazaarvoice.com/xs/PRR/ProductFeed/5.0");
		root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.setAttribute("xsi:schemaLocation", "http://www.bazaarvoice.com/xs/PRR/ProductFeed/5.0 http://www.bazaarvoice.com/xs/PRR/ProductFeed/5.0");
		return root;
	}
	
	private void decorateDocumentWithCategories(Document doc, Element root, StoreModel store){
		
		LOGGER.info("Decorate document with categories.");
		
		Element catRootElement = doc.createElement("Categories");
		root.appendChild(catRootElement);
		
		for(ProductContainer pc : categoryStore){
			
			Element catElement = doc.createElement("Category");
			catRootElement.appendChild(catElement);
			
			Element extId = doc.createElement("ExternalId");
			extId.appendChild(doc.createTextNode(pc.getContentKey().getId()));
			catElement.appendChild(extId);
			
			if(!"FreshDirect".equals(pc.getParentId())){
				Element parentExtId = doc.createElement("ParentExternalId");
				parentExtId.appendChild(doc.createTextNode(pc.getParentId()));
				catElement.appendChild(parentExtId);				
			}
			
			Element name = doc.createElement("Name");
			name.appendChild(doc.createTextNode(pc.getFullName()==null ? "" : pc.getFullName()));
			catElement.appendChild(name);
			
			Element catPageUrl = doc.createElement("CategoryPageUrl");
			catPageUrl.appendChild(doc.createTextNode(BASE_URL+CAT_URL+pc.getContentKey().getId()));
			catElement.appendChild(catPageUrl);
		}
		
		LOGGER.info("Categories done.");
	}
	
	private void decorateDocumentWithProducts(Document doc, Element root){
		
		LOGGER.info("Decorate document with products.");
		
		Element productRoot = doc.createElement("Products");
		root.appendChild(productRoot);
		
		for(ProductModel product : productStore){
			
			Element productElement = doc.createElement("Product");
			productRoot.appendChild(productElement);
			
			Element extId = doc.createElement("ExternalId");
			extId.appendChild(doc.createTextNode(product.getContentKey().getId()));
			productElement.appendChild(extId);
			
			Element catExtId = doc.createElement("CategoryExternalId");
			catExtId.appendChild(doc.createTextNode(product.getPrimaryHome().getContentKey().getId()));
			productElement.appendChild(catExtId);
			
			if(product.getBrands()!=null && product.getBrands().size()>0){
				Element brandExtId = doc.createElement("BrandExternalId");
				brandExtId.appendChild(doc.createTextNode(product.getBrands().get(0).getContentKey().getId().replaceAll(ID_RESTRICTIONS, "")));
				productElement.appendChild(brandExtId);
			}
			
			Element name = doc.createElement("Name");
			name.appendChild(doc.createTextNode(product.getFullName()));
			productElement.appendChild(name);
			
			Element description = doc.createElement("Description");
			description.appendChild(doc.createTextNode(""));
			productElement.appendChild(description);
			
			Element productPageUrl = doc.createElement("ProductPageUrl");
			productPageUrl.appendChild(doc.createTextNode(BASE_URL+PROD_URL+product.getPrimaryHome().getContentKey().getId()+PROD_PARAM_URL+product.getContentKey().getId()));
			productElement.appendChild(productPageUrl);
			
			Element productImageUrl = doc.createElement("ImageUrl");
			productImageUrl.appendChild(doc.createTextNode(BASE_URL+product.getProdImage().getPath()));
			productElement.appendChild(productImageUrl);
			
		}
		
		LOGGER.info("Products done.");
	}
	
	private void decorateDocumentWithBrands(Document doc, Element root){
		
		LOGGER.info("Decorate document with brands.");
		
		Element brandRoot = doc.createElement("Brands");
		root.appendChild(brandRoot);
		
		for(BrandModel brand : brandStore){
			
			Element brandElement = doc.createElement("Brand");
			brandRoot.appendChild(brandElement);
			
			Element extId = doc.createElement("ExternalId");
			extId.appendChild(doc.createTextNode(brand.getContentKey().getId().replaceAll(ID_RESTRICTIONS, "")));
			brandElement.appendChild(extId);
			
			Element name = doc.createElement("Name");
			name.appendChild(doc.createTextNode(brand.getFullName()));
			brandElement.appendChild(name);
		}
		
		LOGGER.info("Brands done.");
	}
	
	private void collectAllProductsAndBrands(){
		
		Collection<ContentKey> products = CmsManager.getInstance().getContentKeysByType(FDContentTypes.PRODUCT);
		for (ContentKey product : products){
			ProductModel productModel = (ProductModel) ContentFactory.getInstance().getContentNodeByKey(product);
			
			if(!productModel.isOrphan() && !productModel.isHidden() && !productModel.isInvisible()){
				productStore.add(productModel);
				if(productModel.getBrands()!=null && productModel.getBrands().size()>0){
					brandStore.add(productModel.getBrands().get(0));
				}
			}
		}
		LOGGER.info("Collected " + productStore.size() + " products.");
	}
	
	private void collectAllCategories(ProductContainer cat) {

		if (!cat.isHidden()) {
			categoryStore.add(cat);
			
			if (cat.getSubcategories() != null) {
				for (ProductContainer con : cat.getSubcategories()) {
					collectAllCategories(con);
				}
			}
		}
		LOGGER.debug("Collected " + categoryStore.size() + " categories.");
	}
	
	
	private void saveUploadFeedFile(Document doc) throws FDResourceException {
		String rootDirectory = RuntimeServiceUtil.getInstance().getRootDirectory();
		uploadFeedFilePath = rootDirectory + File.separator + UPLOAD_FEED_FILE_NAME;
		LOGGER.info("saving Bazaarvoice feed file to " + uploadFeedFilePath);
		
		try {	     
			
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			
			//set properties
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, new StreamResult(new File(uploadFeedFilePath)));
			
		} catch (TransformerException e) {
			throw new FDResourceException("Failed to transform document to file!", e);
		}
	}
	
	private void uploadFile() throws FDResourceException {
		
		String ftpUrl = FDStoreProperties.getBazaarvoiceFtpUrl();
		String ftpUser = FDStoreProperties.getBazaarvoiceFtpUsername();
		
		LOGGER.info("uploading Bazaarvoice feed file to " + ftpUser +"@"+ ftpUrl);
		FTPClient client = new FTPClient();
		client.setDefaultTimeout(600000);
		client.setDataTimeout(600000);
		
        FileInputStream fis = null;
        
        try {
            client.connect(ftpUrl);
            
            if (!client.login(ftpUser, FDStoreProperties.getBazaarvoiceFtpPassword())) {
            	throw new FDResourceException("ftp login failed"); 
            }
    		client.enterLocalPassiveMode();
    		client.changeWorkingDirectory("import-inbox");

            fis = new FileInputStream(uploadFeedFilePath);
            if (!client.storeFile(UPLOAD_FEED_FILE_NAME, fis)) {
            	throw new FDResourceException("ftp file store failed");
            }
            
            client.logout();

        } catch (IOException e) {
            throw new FDResourceException("uploadFile", e);
            
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                client.disconnect();
            } catch (IOException e) {
            }
        }
    }

}

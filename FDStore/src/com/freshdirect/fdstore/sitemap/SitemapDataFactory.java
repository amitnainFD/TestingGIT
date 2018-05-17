package com.freshdirect.fdstore.sitemap;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Category;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.ContentType;
import com.freshdirect.cms.application.CmsManager;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.DepartmentModel;
import com.freshdirect.fdstore.content.ProductContainer;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.SuperDepartmentModel;
import com.freshdirect.framework.util.log.LoggerFactory;


public class SitemapDataFactory {
	private static final Category LOGGER = LoggerFactory.getInstance(SitemapDataFactory.class);
	private static SitemapData cache;
	
	public static synchronized SitemapData create(){
		
		if (FDStoreProperties.isSiteMapEnabled()){
			if (cache == null){
				cache = processStore();
			}
			return cache;

		} else {
			cache = null;
			return null;
		}
		
	}
	
	private static SitemapData processStore(){
		LOGGER.info("Load start");
		Date startTime = new Date();
		
		boolean isQuick = false;
		SitemapData rootData = new SitemapData();

		Set<DepartmentModel> processedDepartments = new HashSet<DepartmentModel>();

		for (ContentKey superDeptKey : CmsManager.getInstance().getContentKeysByType(ContentType.get("SuperDepartment"))) {
			ContentNodeModel superDeptNode = ContentFactory.getInstance().getContentNodeByKey(superDeptKey);
			
			if (superDeptNode instanceof SuperDepartmentModel) {
				SuperDepartmentModel superDept = (SuperDepartmentModel) superDeptNode;
				SitemapData superDeptData = new SitemapData();
				rootData.children.add(superDeptData);
				superDeptData.id	= superDept.getContentName();
				superDeptData.name	= superDept.getFullName();
				
				for (DepartmentModel dept : superDept.getDepartments()){
					if (processedDepartments.add(dept)){
//						out.println(" "+dept.getFullName()+"...");
//						response.flushBuffer();
						SitemapData deptData = processProductContainer(superDeptData, dept, isQuick);
						superDeptData.countAll 				+= deptData.countAll;
						superDeptData.countAvailable 		+= deptData.countAvailable;
						superDeptData.countTempUnavailable 	+= deptData.countTempUnavailable;
						superDeptData.countDiscontinued 	+= deptData.countDiscontinued;
					}
				}
			}
		}

		SitemapData emptySuperDeptData = new SitemapData();
		rootData.children.add(emptySuperDeptData);
		emptySuperDeptData.id	= "none";
		emptySuperDeptData.name	= "No Super Department";

		//remaining departments
		for (DepartmentModel dept : ContentFactory.getInstance().getStore().getDepartments()) {
			if (!processedDepartments.contains(dept)) {
//				out.println(" "+dept.getFullName()+"...");
//				response.flushBuffer();
				SitemapData deptData = processProductContainer(emptySuperDeptData, dept, isQuick);
				emptySuperDeptData.countAll 			+= deptData.countAll;
				emptySuperDeptData.countAvailable 		+= deptData.countAvailable;
				emptySuperDeptData.countTempUnavailable += deptData.countTempUnavailable;
				emptySuperDeptData.countDiscontinued 	+= deptData.countDiscontinued;
			}
		}
		
		long loadTimeDiffSec = (new Date().getTime() - startTime.getTime()) / 1000l;
		
		LOGGER.info("Load finished, it took " + loadTimeDiffSec + " seconds" );
		return rootData;
	}
	
	private static SitemapData processProductContainer(SitemapData parentData, ProductContainer container, Boolean isQuick) {
		SitemapData SitemapData = new SitemapData();
		parentData.children.add(SitemapData);
		
		SitemapData.id = container.getContentName();
		SitemapData.name = container.getFullName();
		
		if (container instanceof CategoryModel){
			for (ProductModel prod : ((CategoryModel) container).getProducts()){
	      if (isQuick) {
	        SitemapData.countAvailable++;
	      } else {
	        if (prod.isDiscontinued()){
	          SitemapData.countDiscontinued++;
	        } else if (prod.isTempUnavailable()){
	          SitemapData.countTempUnavailable++;
	        } else {
	          SitemapData.countAvailable++;
	        }
	      }
				SitemapData.countAll++;
			}
		}
		
		for (CategoryModel subCat : container.getSubcategories()){
			SitemapData childData = processProductContainer(SitemapData, subCat, isQuick);
			SitemapData.countAll 				+= childData.countAll;
			SitemapData.countAvailable 		+= childData.countAvailable;
			SitemapData.countTempUnavailable 	+= childData.countTempUnavailable;
			SitemapData.countDiscontinued 		+= childData.countDiscontinued;
		}
		return SitemapData;
	}
	

}
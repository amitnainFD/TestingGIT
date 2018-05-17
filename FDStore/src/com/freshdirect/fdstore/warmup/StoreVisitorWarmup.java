package com.freshdirect.fdstore.warmup;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.DepartmentModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.StoreModel;
import com.freshdirect.framework.util.log.LoggerFactory;

import java.util.Collections;

public class StoreVisitorWarmup extends Warmup {

	private static final int MAX_THREADS = 4;

	private static Category LOGGER = LoggerFactory.getInstance(StoreVisitorWarmup.class);

	private final boolean excerciseGetters;
	
	private final boolean validateParent;

	private List depts;
		
	private List currentCats;
	
	public StoreVisitorWarmup() {
		this(true, true);
	}

	public StoreVisitorWarmup(boolean excerciseGetters, boolean validateParent) {
		super();
		this.excerciseGetters = excerciseGetters;
		this.validateParent = validateParent;
	}

	public void warmup() {
		LOGGER.info("Starting warmup process...");
		// super.warmup();
		LOGGER.info("Start visiting the store...");
		startStoreVisitorWarmup();
		LOGGER.info("Finished visiting the store...");
	}

	
	private void startStoreVisitorWarmup() {
		Thread t = new Thread() {
			public void run() {
				try {
					visitStore(contentFactory.getStore());
				} catch (FDResourceException e) {
					LOGGER.warn("FDResourceException in StoreVisitorWarmup: " + e.getMessage());
					e.printStackTrace();
				}
			}
		};
		t.start();
	}

	private void visitStore(StoreModel store) throws FDResourceException {
		depts = Collections.synchronizedList(store.getDepartments());
		
		for (int i = 0; i < MAX_THREADS; i++) {
			Thread t = new StoreVisitorThread(i);
			t.start();
		}		
	}


	private void excerciseGetters(Object node) {
		if (!excerciseGetters) {
			return;
		}
		Class nodeClass = node.getClass();
		Method[] methods = nodeClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			String name = m.getName();
			if ((name.startsWith("get") || name.startsWith("is")) && m.getParameterTypes().length == 0) {
				//LOGGER.debug("Calling "+m.getName());
				try {
					m.invoke(node, null);
				} catch (Exception e) {
					//LOGGER.debug("Exception during call "+m.getName()+"(): "+e.getMessage());					
				}				
			}
		}
	}
	
	private void validateParent(ContentNodeModel parent, ContentNodeModel child) {
		if (!validateParent) {
			return;
		}
		if (child.getParentNode() != parent) {
			LOGGER.warn("Expected parent " + parent.getPath() + " for "
					+ child.getPath() + ", found "
					+ child.getParentNode().getPath());
		}
	}
	
	private class StoreVisitorThread extends Thread {
		private int id;

		public StoreVisitorThread(int id) {
			this.id = id;
		}

		public void run() {
			while (true) {
				CategoryModel cat = null;
				synchronized (depts) {
					while (currentCats == null || currentCats.isEmpty()) {
						if (depts.isEmpty()) {
							LOGGER.info("Completed departments"); 
							return; 
						}
						DepartmentModel dept = (DepartmentModel) depts.remove(0);
						LOGGER.info("Thread "+id+": Start crawling department: "+dept.getContentName() +", "+depts.size()+" left.");
						excerciseGetters(dept);
						currentCats = Collections.synchronizedList(new LinkedList(dept.getCategories()));						
					}
					
					cat = (CategoryModel) currentCats.remove(0);
					// TODO validateParent
				}
				try {
					LOGGER.info("Thread "+id+": Visiting category: "+cat.getContentName());
					visitCategory(cat);
				} catch (FDResourceException e) {
					LOGGER.warn("Thread "+id+": FDResourceException while visiting "+cat);
				}				
			}
		}

		private void visitCategory(CategoryModel cat) throws FDResourceException {
			LOGGER.debug("Thread "+id+": Visiting "+cat.getContentName());
			excerciseGetters(cat);
			List prods = cat.getProducts();
			for (Iterator i = prods.iterator(); i.hasNext();) {
				ProductModel prod = (ProductModel) i.next();
				//LOGGER.debug("Thread "+id+": Visiting "+prod.getContentName());
				excerciseGetters(prod);
				validateParent(cat, prod);
			}
			
			List subCats = cat.getSubcategories();
			for (Iterator i = subCats.iterator(); i.hasNext();) {
				CategoryModel subCat = (CategoryModel) i.next();
				this.visitCategory(subCat);
				validateParent(cat, subCat);
			}
		}

	}
	
}

/*
 * $Workfile:FDCartModel.java$
 *
 * $Date:8/23/2003 7:26:19 PM$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.content.ProductModel;

/**
 * QuickCart class.
 *
 * @version	$Revision:$
 * @author	 $Author:$
 */
@Deprecated
public class QuickCart implements FDProductCollectionI, Serializable {

	private final List<FDProductSelectionI> orderLines = new ArrayList<FDProductSelectionI>();
	private String orderId = null;
	private Date deliveryDate = null;
	private String productType="";
	private String name = null;
	private UserContext userCtx=null;
	
	public static final String PRODUCT_TYPE_CCL="CCL";
	public static final String PRODUCT_TYPE_PRD="PRODUCT";
	public static final String PRODUCT_TYPE_STARTER_LIST="STARTER_LIST";
	public static final String PRODUCT_TYPE_SO="SO";
	

	public QuickCart() {
		super();
	}
	
	public void setName(String name) {
	   this.name = name;
	}
	
	public String getName() { return name; }
	

	public void setOrderId(String oid) {
		this.orderId = oid;
	}

	public String getOrderId() {
		return this.orderId;
	}

	public boolean isEveryItemEverOrdered() {
		return "every".equalsIgnoreCase(this.orderId);
	}

	public void setDeliveryDate(Date deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public Date getDeliveryDate() {
		return this.deliveryDate;
	}

	public void addProduct(FDProductSelectionI orderLine) {
		if(getUserContext()!=null) orderLine.setUserContext(userCtx);
		
		this.orderLines.add(orderLine);
		//this.reSort();
	}

	public void addProducts(Collection<? extends FDProductSelectionI> cartLines) {
		this.orderLines.addAll(cartLines);
	}

	public int numberOfProducts() {
		return this.orderLines.size();
	}

	public int numberOfProducts(String deptId) {
		return this.getProducts(deptId).size();
	}

	public FDProductSelectionI getProduct(int index) {
		return (FDProductSelectionI) this.orderLines.get(index);
	}

	public FDProductSelectionI getProduct(int index, String deptId) {
		return (FDProductSelectionI) this.getProducts(deptId).get(index);
	}

	public void setProducts(List<? extends FDProductSelectionI> lines) {
		this.orderLines.clear();
		for ( FDProductSelectionI product : lines ) {
			if ( getUserContext() != null )
				product.setUserContext(getUserContext());
			this.orderLines.add( product );
		}
		this.sort( PRODUCT_COMPARATOR );
	}

	public void sort(Comparator<FDProductSelectionI> comparator) {
		Collections.sort(orderLines, comparator);
	}

	public void setProduct(int index, FDProductSelectionI orderLine) {
		throw new UnsupportedOperationException("QuickCart.setProduct(int,FDProductSelectionI)");
	}

	public void zeroAllQuantities() {
		for (Iterator<FDProductSelectionI> i = this.orderLines.iterator(); i.hasNext();) {
			FDProductSelectionI product = (FDProductSelectionI) i.next();
			if (product.isSoldBySalesUnits()) {
				product.setSalesUnit("");
			} else {
				product.setQuantity(0.0);
			}
		}
	}

	public void removeProduct(int index) {
		this.orderLines.remove(index);
	}

	public void clearProducts() {
		this.orderLines.clear();
	}

	// List<FDProductSelectionI>
	public List<FDProductSelectionI> getProducts() {
		return Collections.unmodifiableList(this.orderLines);
	}

	public int getAvailableProductCnt(){
		int productCnt = 0;
		for (FDProductSelectionI orderLine: orderLines) {
			ProductModel productNode = orderLine.lookupProduct();
			if(!((productNode==null || productNode.getSku(orderLine.getSkuCode()).isUnavailable() || orderLine.isInvalidConfig()))) {
					productCnt++;
			}
		}
		return productCnt;
	}
	
	// List<FDProductSelectionI>
	public List<FDProductSelectionI> getProducts(String deptId) {
		List<FDProductSelectionI> deptProducts = new ArrayList<FDProductSelectionI>();
		for (Iterator<FDProductSelectionI> i = this.orderLines.iterator(); i.hasNext();) {
			FDProductSelectionI productSelection = (FDProductSelectionI) i.next();
			ProductModel product = productSelection.lookupProduct();
			if (product!=null && product.getDepartment()!=null && product.getDepartment().getContentName().equalsIgnoreCase(deptId)) {
				deptProducts.add(productSelection);
			}
		}
		return Collections.unmodifiableList(deptProducts);
	}

	private final static Comparator<FDProductSelectionI> PRODUCT_COMPARATOR = new Comparator<FDProductSelectionI>() {
		public int compare(FDProductSelectionI product1, FDProductSelectionI product2) {
			///order by Department and then by product.
		
			int retValue = product1.getDepartmentDesc().compareTo(product2.getDepartmentDesc());
			if (retValue == 0) {
				retValue = product1.getDescription().compareTo(product2.getDescription());
				if (retValue == 0) {
					retValue = product1.getConfigurationDesc().compareTo(product2.getConfigurationDesc());
					if (retValue == 0) { //dept * desc * configDesc matches, check quantity
						if (product1.getQuantity() <= product2.getQuantity()) {
							retValue = -1;
						} else {
							retValue = 1;
						}
					}
				}
			}
			return retValue;
		}
	};

	public String getProductType() {
		return productType;
	}

	public boolean isEmpty() { return orderLines.size() == 0; }

	public void setProductType(String productType) {
		this.productType = productType;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("QuickCart:type=").append(productType);
		if (name != null) sb.append(",name=").append(name);
		if (orderId != null) sb.append(",id=").append(orderId);
		sb.append(",itemCount=").append(orderLines.size());
		return sb.toString();
	}

	public UserContext getUserContext() {
		return userCtx;
	}

	public void setUserContext(UserContext userCtx) {
		this.userCtx = userCtx;
	}

}

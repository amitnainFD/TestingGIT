package com.freshdirect.fdstore.customer;

import java.util.Collection;
import java.util.List;


public interface FDProductCollectionI {
	
	public void addProduct(FDProductSelectionI Product);
	public void addProducts(Collection<? extends FDProductSelectionI> cartLines);
	public int numberOfProducts();
	public FDProductSelectionI getProduct(int index);
	public void setProducts(List<? extends FDProductSelectionI> lines);
	public void setProduct(int index, FDProductSelectionI Product);
	public void removeProduct(int index);
	public void clearProducts();
	public List<FDProductSelectionI> getProducts();
	
}
package com.freshdirect.fdstore.standingorders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UnavDetailsReportingBean implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	List<InventoryMapInfoBean> discProductInfoBeanList = new ArrayList<InventoryMapInfoBean>();
	List<InventoryMapInfoBean> restrictedProductInfoBeanList = new ArrayList<InventoryMapInfoBean>();
	List<InventoryMapInfoBean> atpFailureProductInfoBeanList = new ArrayList<InventoryMapInfoBean>();
	List<InventoryMapInfoBean> unavProductInfoBeanList = new ArrayList<InventoryMapInfoBean>();
	
	
	public List<InventoryMapInfoBean> getDiscProductInfoBeanList() {
		return discProductInfoBeanList;
	}
	public List<InventoryMapInfoBean> getRestrictedProductInfoBeanList() {
		return restrictedProductInfoBeanList;
	}
	public List<InventoryMapInfoBean> getAtpFailureProductInfoBeanList() {
		return atpFailureProductInfoBeanList;
	}
	public void setDiscProductInfoBeanList(
			List<InventoryMapInfoBean> discProductInfoBeanList) {
		this.discProductInfoBeanList = discProductInfoBeanList;
	}
	public void setRestrictedProductInfoBeanList(
			List<InventoryMapInfoBean> restrictedProductInfoBeanList) {
		this.restrictedProductInfoBeanList = restrictedProductInfoBeanList;
	}
	public void setAtpFailureProductInfoBeanList(
			List<InventoryMapInfoBean> atpFailureProductInfoBeanList) {
		this.atpFailureProductInfoBeanList = atpFailureProductInfoBeanList;
	}
	public List<InventoryMapInfoBean> getUnavProductInfoBeanList() {
		return unavProductInfoBeanList;
	}
	
	public void setUnavProductInfoBeanList(
			List<InventoryMapInfoBean> unavProductInfoBeanList) {
		this.unavProductInfoBeanList = unavProductInfoBeanList;
	}
	

}

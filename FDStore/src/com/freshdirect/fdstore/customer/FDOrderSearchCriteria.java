package com.freshdirect.fdstore.customer;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.freshdirect.fdlogistics.model.FDDeliveryDepotLocationModel;
import com.freshdirect.fdlogistics.model.FDDeliveryDepotModel;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.logistics.controller.data.PickupData;
import com.freshdirect.logistics.controller.data.PickupLocationData;

public class FDOrderSearchCriteria extends FDSearchCriteria {

	private final Set depotLocationIds = new HashSet();
	
	private String[] status = null;
	
	private Date date = null;
	
	private boolean corporate = false;
	private boolean chefsTable = false;
	
	public boolean isCorporate() {
		return corporate;
	}

	public void setCorporate(boolean corporate) {
		this.corporate = corporate;
	}
	
	public boolean isChefsTable() {
		return chefsTable;
	}
	
	public void setChefsTable(boolean chefsTable) {
		this.chefsTable = chefsTable;
	}
	
	public String getDepotLocationId() {
		return this.depotLocationIds.size() == 1 ? (String) this.depotLocationIds.iterator().next() : null;
	}

	public void setDepotLocationId(String depotLocationId) {
		this.depotLocationIds.clear();
		if (depotLocationId != null && !"".equals(depotLocationId.trim())) {
			this.depotLocationIds.add(depotLocationId);
		}
	}

	public Set getDepotLocationIds() {
		return this.depotLocationIds;
	}

	public void setDepotLocationIds(Set s) {
		this.depotLocationIds.clear();
		this.depotLocationIds.addAll(s);
	}


	
	public void setStatus(String[] status) {
		if(status != null){
			this.status = status;
		}
	}
	
	public String[] getStatus(){
		return this.status;
	}
	
	public Date getDeliveryDate() {
		return this.date;
	}
	
	public void setDeliveryDate(Date date){
		this.date = date;
	}
	
	public String getCriteria() throws FDResourceException {
		StringBuffer buf = new StringBuffer(super.getCriteria());
		
		for(Iterator i = this.getCriteriaMap().entrySet().iterator(); i.hasNext(); ) {
			Map.Entry e = (Entry) i.next();
			buf.append(" ").append(e.getKey()).append(": ").append(e.getValue());
		}
		
		return buf.toString().trim();
	}

	public Map getCriteriaMap() throws FDResourceException {
		Map m = super.getCriteriaMap();
		if (this.getDeliveryDate() != null && !"".equals(this.getDeliveryDate())) {
			Calendar dlvCalendar = Calendar.getInstance();
			dlvCalendar.setTime(this.date);
			m.put("Date", dlvCalendar.get(12)+"."+dlvCalendar.get(Calendar.DATE)+"."+dlvCalendar.get(Calendar.YEAR));
		}
		
		if(this.chefsTable){
			m.put("Chefs Table", "true");
		}
		
		if(this.corporate){
			m.put("Corporate", "true");
		}
		
		if (this.depotLocationIds != null && depotLocationIds.size() == 1) {
			List<FDDeliveryDepotModel> depots = FDDeliveryManager.getInstance().getPickupDepots();
			for (FDDeliveryDepotModel pickup: depots) {
	            for (FDDeliveryDepotLocationModel location: pickup.getLocations()) {
	                if (location.getPK()!=null && this.getDepotLocationId().equals(location.getPK().getId())) {
	                    m.put("Depot", location.getFacility());
	                }
	            }
	        }
	    }
		
		return m;
	}
	
	public boolean isBlank() {
		return super.isBlank() && this.depotLocationIds.isEmpty() && this.date == null && this.status == null;
	}

	public String toString() {

		StringBuffer buf = new StringBuffer();
		buf.append("[FDSearchCriteria First Name: ").append(this.firstName);
		buf.append(" Last Name: ").append(this.lastName);
		buf.append(" Email: ").append(this.email);
		buf.append(" Phone: ").append(this.phone);
		buf.append(" Order#: ").append(this.orderNumber);
		buf.append(" DepotCode: ").append(this.depotCode);
		buf.append(" DepotLocationId: ").append(this.depotLocationIds);
		buf.append(" Day: ").append(this.date);
		buf.append(" Chefs Table: ").append(chefsTable).append("]");

		return buf.toString();
	}


}

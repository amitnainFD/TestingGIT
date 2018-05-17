package com.freshdirect.fdstore.customer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.customer.EnumSaleType;
import com.freshdirect.customer.ErpOrderHistory;
import com.freshdirect.customer.ErpSaleInfo;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.customer.adapter.FDOrderInfoAdapter;

public class FDOrderHistory extends ErpOrderHistory {

	private static final long	serialVersionUID	= -4179034756408883935L;
	
	private final List<FDOrderInfoI> fdOrderInfos;

	public FDOrderHistory(Collection<ErpSaleInfo> erpSaleInfos) {
		super(erpSaleInfos);
		this.fdOrderInfos = new ArrayList<FDOrderInfoI>(erpSaleInfos.size());
		for ( ErpSaleInfo esi : erpSaleInfos ) {
			fdOrderInfos.add( new FDOrderInfoAdapter( esi ) );
		}

	}

	/** @return Collection of FDOrderInfoI */
	public Collection<FDOrderInfoI> getFDOrderInfos() {
		return fdOrderInfos;
	}

	public Collection<FDOrderInfoI> getFDOrderInfos(EnumSaleType saleType,EnumEStoreId eStore) {
		
		if(eStore==null)
			return getFDOrderInfos(saleType);

		List<FDOrderInfoI> l = new ArrayList<FDOrderInfoI>();
		for (Iterator<FDOrderInfoI> i = this.fdOrderInfos.iterator(); i.hasNext();) {
			FDOrderInfoI o = i.next();
			if(saleType.equals(o.getSaleType())&& eStore.equals(o.getEStoreId())) {
				l.add(o);
			}
		}
		return l;
	}
	
	public Collection<FDOrderInfoI> getFDOrderInfos(EnumSaleType saleType) {

		List<FDOrderInfoI> l = new ArrayList<FDOrderInfoI>();
		for (Iterator<FDOrderInfoI> i = this.fdOrderInfos.iterator(); i.hasNext();) {
			FDOrderInfoI o = i.next();
			if(saleType.equals(o.getSaleType())) {
				l.add(o);
			}
		}
		return l;
	}
	
	public FDOrderInfoI getFDOrderInfo(String orderId) {

		List<FDOrderInfoI> l = new ArrayList<FDOrderInfoI>();
		for (Iterator<FDOrderInfoI> i = this.fdOrderInfos.iterator(); i.hasNext();) {
			FDOrderInfoI o = i.next();
			if(orderId.equals(o.getErpSalesId())) {
				return o;
			}
		}
		return null;
	}
	
	/**
	 * @return Collection of FDOrderInfoI where status allows creating make-good
	 */
	public Collection<FDOrderInfoI> getMakeGoodReferenceInfos() {
		List<FDOrderInfoI> l = new ArrayList<FDOrderInfoI>();
		for (Iterator<FDOrderInfoI> i = this.fdOrderInfos.iterator(); i.hasNext();) {
			FDOrderInfoI o = i.next();
			EnumSaleStatus s = o.getOrderStatus();
			if (EnumSaleStatus.NEW.equals(s)
				|| EnumSaleStatus.SUBMITTED.equals(s)
				|| EnumSaleStatus.CANCELED.equals(s)
				|| EnumSaleStatus.NOT_SUBMITTED.equals(s)) {
				continue;
			}
			l.add(o);	
		}
		return l;
	}
	
	/**
	 * This method returns a list of orders that used the given delivery pass.
	 * @ return Collection of FDOrderInfoI.
	 */
	public Collection<FDOrderInfoI> getDlvPassOrderInfos(String dlvPassId) {
		List<FDOrderInfoI> l = new ArrayList<FDOrderInfoI>();
		for (Iterator<FDOrderInfoI> i = this.fdOrderInfos.iterator(); i.hasNext();) {
			FDOrderInfoI o = i.next();
			if (o.getDlvPassId() != null && o.getDlvPassId().equals(dlvPassId)) {
				//This Order used the delivery pass. So add it to the list.
				l.add(o);				
			}
		}
		return l;
	}
	
	/**
	 * Returns a list of orders created from a specified Standing Order (or all Standing Order if orderId is null) and will be delivered in delivery window
	 * @return Collection of FDOrderInfoI.
	 */
	public List<FDOrderInfoI> getStandingOrderInstances(String soId) {
		
		Calendar now=Calendar.getInstance();
		
		List<FDOrderInfoI> result = new ArrayList<FDOrderInfoI>(fdOrderInfos.size());
		
		for(FDOrderInfoI order : this.fdOrderInfos) {
			
			Calendar rDate = Calendar.getInstance();
			rDate.setTime(order.getRequestedDate());
			
			if(soId!=null && soId.equals(order.getStandingOrderId()) && rDate.after(now) && !order.getOrderStatus().isCanceled()) {
				result.add(order);
			}else if(soId==null && order.getStandingOrderId() != null  && rDate.after(now) && !order.getOrderStatus().isCanceled()) {
				result.add(order);
			}
		}	
		return result;
	}
		
	
	public Collection<FDOrderInfoI> getFDOrderInfos(EnumEStoreId eStore) {
		
		if(eStore==null)
			return this.fdOrderInfos;

		List<FDOrderInfoI> l = new ArrayList<FDOrderInfoI>(fdOrderInfos.size());
		for(FDOrderInfoI order : this.fdOrderInfos) {
			
			if(eStore.equals(order.getEStoreId())) {
				l.add(order);
			}
		}
		return l;
	}
}
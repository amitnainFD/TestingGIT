package com.freshdirect.fdstore.oas.ejb;

import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ejb.EJBException;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.apache.log4j.Category;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.application.CmsManager;
import com.freshdirect.cms.fdstore.FDContentTypes;
import com.freshdirect.common.pricing.MaterialPrice;
import com.freshdirect.common.pricing.ZonePromoDiscount;
import com.freshdirect.common.pricing.util.GroupScaleUtil;
import com.freshdirect.fdstore.FDCachedFactory;
import com.freshdirect.fdstore.FDGroup;
import com.freshdirect.fdstore.FDGroupNotFoundException;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.GroupScalePricing;
import com.freshdirect.fdstore.GrpZonePriceListing;
import com.freshdirect.fdstore.GrpZonePriceModel;
import com.freshdirect.fdstore.ZonePriceListing;
import com.freshdirect.fdstore.ZonePriceModel;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.SkuModel;
import com.freshdirect.fdstore.grp.FDGrpInfoManager;
import com.freshdirect.framework.core.GatewaySessionBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;

/**@author ekracoff on May 25, 2004*/
public class AdServerGatewaySessionBean extends GatewaySessionBeanSupport {

	private static Category LOGGER = LoggerFactory.getInstance(AdServerGatewaySessionBean.class);
	protected final static DecimalFormat PRICE_FORMATTER = new DecimalFormat("0.00");
	private HashSet<AdServerRow> results;
	
	public void run() throws RemoteException{
		results = new HashSet<AdServerRow>();

		Set productKeys = CmsManager.getInstance().getContentKeysByType(FDContentTypes.PRODUCT);
		for (Iterator i = productKeys.iterator(); i.hasNext(); ){
			ContentKey k = (ContentKey)i.next();
			ProductModel pm = (ProductModel) ContentFactory.getInstance().getContentNode(k.getId());
			this.visit(pm);
		}
		if(FDStoreProperties.isGroupScaleEnabled()){
			try {
				//Pass Group scale Info if available. Group id along with pricing information to the ad server.
				Collection<FDGroup> grpInfoList=FDGrpInfoManager.loadAllGrpInfoMaster();
				for (Iterator<FDGroup> i = grpInfoList.iterator(); i.hasNext(); ){
					FDGroup group = (FDGroup)i.next();
					try {
						GroupScalePricing gsPricing = FDCachedFactory.getGrpInfo(group);
						this.visitGroupScale(gsPricing);
					}catch(FDGroupNotFoundException fe) {
						// keep going if this happens
					}
				}
			}catch(FDResourceException fe){
				throw new RuntimeException(fe);
			}
		}
		
		LOGGER.info("Enqueueing results");
		enqueue(results);
		LOGGER.info("Enqueueing finished");
	}

	/*
	 * For each product get all the pricing tiers
	 * off the default sku and send to OAS
	 */
	public void visit(ProductModel prod) {
		SkuModel sku = prod.getDefaultSku();
		//if default sku is null we dont care about pricing
		if(sku == null){
			results.add(new AdServerRow(prod.getContentName(), false, null, null, null));
		} else {
			evaluatePrices(prod, sku);
		}

		
	}

	private void evaluatePrices(ProductModel prod, SkuModel sku) {
		try {
			//@TODO Start pushing prices for every available zone. Temporarily passing master default prices alone.
//			MaterialPrice[] prices = sku.getProduct().getPricing().getZonePrice(ZonePriceListing.MASTER_DEFAULT_ZONE).getMaterialPrices();
//			for (int i=0; i<prices.length; i++) {
//				MaterialPrice mp = prices[i];
//				String price = mp.getScaleLowerBound() + "@" + mp.getPrice();
//				results.add(new AdServerRow(prod.getContentName(), !prod.isUnavailable(), price));
//			}
			ZonePriceListing zonePriceList = sku.getProduct().getPricing().getZonePriceList();
			if(null != zonePriceList){ 
				Collection zonePrices = zonePriceList.getZonePrices();
				if(zonePrices!= null && zonePrices.size() > 0){
					for (Iterator iterator = zonePrices.iterator(); iterator.hasNext();) {
						ZonePriceModel zonePriceModel = (ZonePriceModel) iterator.next();
						MaterialPrice[] prices = sku.getProduct().getPricing().getZonePrice(zonePriceModel.getPricingZone()).getMaterialPrices();						
						for (int i=0; i<prices.length; i++) {
							MaterialPrice mp = prices[i];
							String price = mp.getScaleLowerBound() + "@" + mp.getPrice();
							if(!FDStoreProperties.isZonePricingAdEnabled()){
								if("M".equalsIgnoreCase(getZoneType(zonePriceModel.getPricingZone().getPricingZoneId())))//::FDX::
								results.add(new AdServerRow(prod.getContentName(), !prod.isUnavailable(), price, zonePriceModel.getPricingZone(), getZoneType(zonePriceModel.getPricingZone().getPricingZoneId())));
							}else{
								results.add(new AdServerRow(prod.getContentName(), !prod.isUnavailable(), price, zonePriceModel.getPricingZone(), getZoneType(zonePriceModel.getPricingZone().getPricingZoneId())));
							}
						}
					}
				}
			
			}
		} catch (FDResourceException e) {
			throw new RuntimeException(e);
		} catch (FDSkuNotFoundException e) {
			//keep going if this happens
			e.printStackTrace();
		}
	}
	/*
	 * For each group get all the pricing tiers
	 * and send to OAS
	 */
	public void visitGroupScale(GroupScalePricing gsPricing) {
		//if default sku is null we dont care about pricing
		if( gsPricing.getGrpZonePriceList() == null){
			results.add(new AdServerRow(gsPricing.getGroupId(), false, null, null, null));
		} else {
			evaluateGroupScalePrices(gsPricing);
		}

		
	}

	private void evaluateGroupScalePrices(GroupScalePricing gsPricing) {
		try {
			GrpZonePriceListing gsZonePriceList = gsPricing.getGrpZonePriceList();
			if(null != gsZonePriceList){ 
				Collection<GrpZonePriceModel> zonePrices = gsZonePriceList.getGrpZonePrices();
				if(zonePrices!= null && zonePrices.size() > 0){
					for (Iterator<GrpZonePriceModel> iterator = zonePrices.iterator(); iterator.hasNext();) {
						GrpZonePriceModel grpPriceModel = (GrpZonePriceModel) iterator.next();
						MaterialPrice[] prices =grpPriceModel.getMaterialPrices();
						for (int i=0; i<prices.length; i++) {
							MaterialPrice mp = prices[i];
							if(!GroupScaleUtil.isGroupPriceValid(gsPricing, grpPriceModel.getSapZone(), mp)){//::FDX::
								//Group Price is invalid. so ignore
								continue;
							}
							String price = mp.getScaleLowerBound() + "@" + PRICE_FORMATTER.format(mp.getPrice());
							if(!FDStoreProperties.isZonePricingAdEnabled()){
								if("M".equalsIgnoreCase(getZoneType(grpPriceModel.getSapZone().getPricingZoneId())))
								results.add(new AdServerRow(gsPricing.getGroupId(), gsPricing.isActive(), price, grpPriceModel.getSapZone(), getZoneType(grpPriceModel.getSapZone().getPricingZoneId())));//::FDX::
							}else{
								results.add(new AdServerRow(gsPricing.getGroupId(), gsPricing.isActive(), price, grpPriceModel.getSapZone(), getZoneType(grpPriceModel.getSapZone().getPricingZoneId())));//::FDX::
							}
						}
					}
				}
			}
		}catch (FDResourceException e) {
			throw new RuntimeException(e);
		}  
	}

	private String getZoneType(String sapZoneId) {
		String zoneType = null;
		if(null != sapZoneId){
			if(ZonePriceListing.MASTER_DEFAULT_ZONE.equalsIgnoreCase(sapZoneId)){
				zoneType="M";
			}else if(ZonePriceListing.RESIDENTIAL_DEFAULT_ZONE.equalsIgnoreCase(sapZoneId)||ZonePriceListing.CORPORATE_DEFAULT_ZONE.equalsIgnoreCase(sapZoneId)){
				zoneType="S";
			}else{
				zoneType="C";
			}
		}
		return zoneType;
	}

	private void enqueue(HashSet results) {

		try {
			ObjectMessage rowMsg = qsession.createObjectMessage();
			rowMsg.setObject(results);

			this.qsender.send(rowMsg);
			LOGGER.info("Queued row with set of " + results.size() + " products");

		} catch (JMSException ex) {
			LOGGER.warn("Error enqueueing Capture Message", ex);
			throw new EJBException(ex);
		} catch (Throwable t) {
			LOGGER.warn("Unexpected exception queueing row", t);
			throw new EJBException(t.getMessage());
		}
	}

}

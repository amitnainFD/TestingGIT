package com.freshdirect.fdstore.promotion.pxp;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Category;

import com.freshdirect.common.customer.EnumCardType;
import com.freshdirect.crm.CrmAgentModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.promotion.EnumDCPDContentType;
import com.freshdirect.fdstore.promotion.EnumPromoChangeType;
import com.freshdirect.fdstore.promotion.EnumPromotionSection;
import com.freshdirect.fdstore.promotion.EnumPromotionStatus;
import com.freshdirect.fdstore.promotion.management.FDPromoChangeDetailModel;
import com.freshdirect.fdstore.promotion.management.FDPromoChangeModel;
import com.freshdirect.fdstore.promotion.management.FDPromoContentModel;
import com.freshdirect.fdstore.promotion.management.FDPromoCustStrategyModel;
import com.freshdirect.fdstore.promotion.management.FDPromoDlvTimeSlotModel;
import com.freshdirect.fdstore.promotion.management.FDPromoDlvZoneStrategyModel;
import com.freshdirect.fdstore.promotion.management.FDPromoPaymentStrategyModel;
import com.freshdirect.fdstore.promotion.management.FDPromoZipRestriction;
import com.freshdirect.fdstore.promotion.management.FDPromotionAttributeParam;
import com.freshdirect.fdstore.promotion.management.FDPromotionNewManager;
import com.freshdirect.fdstore.promotion.management.FDPromotionNewModel;
import com.freshdirect.fdstore.promotion.management.WSAdminInfo;
import com.freshdirect.fdstore.promotion.management.WSPromotionInfo;
import com.freshdirect.fdstore.util.json.FDPromotionJSONSerializer;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.metaparadigm.jsonrpc.JSONSerializer;
import com.metaparadigm.jsonrpc.MarshallException;
import com.metaparadigm.jsonrpc.UnmarshallException;

/**
 * 
 * @author segabor
 *
 */
public class PromoPublisher {
	private static Category		LOGGER				= LoggerFactory.getInstance( PromoPublisher.class );

	
	private List<FDPromotionNewModel> promoList;
	private CrmAgentModel agent;

	/**
	 * Result of publish per promotion. Each couple is a Code to TRUE/FALSE mapping.
	 */
	private Map<String,Boolean> lastResult = null;
	
	public void setPromoList(List<FDPromotionNewModel> promoList) {
		this.promoList = new ArrayList<FDPromotionNewModel>();
		if (promoList == null)
			return;

		for (FDPromotionNewModel promo : promoList) {
			// -- remove change logs
			//promo.setAuditChanges(Collections.<FDPromoChangeModel>emptyList());
			promo.clearAuditChanges();
			// -- remove assigned customer IDs
			promo.setAssignedCustomerUserIds("");
			
			this.promoList.add(promo);
		}
	}

	public void setAgent(CrmAgentModel agent) {
		this.agent = agent;
	}
	

	/**
	 * Returns the result of last publish
	 * @return
	 */
	public Map<String, Boolean> getLastResult() {
		return lastResult;
	}
	
	public boolean doPublish() {
		// and log this event
		Map<String, EnumPromotionStatus> preStatuses = new HashMap<String, EnumPromotionStatus>();
		// Map<String, EnumPromotionStatus> postStatuses = new HashMap<String, EnumPromotionStatus>();
		// Map<String, String> changeIDs  = new HashMap<String, String>();

		// store pre-statuses
		for (FDPromotionNewModel promo : promoList) {
			preStatuses.put(promo.getPromotionCode(), promo.getStatus());
		}
		
		lastResult = null;
		
		// check parameters
		if (promoList == null || promoList.size() == 0) {
			LOGGER.error("Null or empty promo list");
			return false;
		}

		if (agent == null) {
			LOGGER.error("Missing agent parameter");
			return false;
		}

		
		// prepare serializer
		JSONSerializer ser = new JSONSerializer();
		try {
			ser.registerDefaultSerializers();
			ser.registerSerializer(FDPromotionJSONSerializer.getInstance());
		} catch (Exception e) {
			LOGGER.error("Failed to register serializer modules", e);
			return false;
		}


		// Serialize promos
		String payload;
		try {
			payload = ser.toJSON(promoList);
		} catch (MarshallException e) {
			LOGGER.error("Failed to serialize promo list", e);
			return false;
		}
		
		final String promoPublishURL = FDStoreProperties.getPromoPublishURL();
		if (promoPublishURL == null) {
			LOGGER.error("Promotion Publish URL is not set in fdstore.properties!");
			return false;
		}
		
		// setup http client
		HttpClient client = new HttpClient();

		PostMethod meth = new PostMethod(promoPublishURL);
		meth.addRequestHeader("User-Agent", "PromoPublish/1.0");

		NameValuePair[] pairs = new NameValuePair[2];
		
		pairs[0] = new NameValuePair();
		pairs[0].setName("payload");
		pairs[0].setValue(payload);
		
		pairs[1] = new NameValuePair();
		pairs[1].setName("agent");
		pairs[1].setValue(agent.getUserId());

		meth.setRequestBody(pairs);
		
		Date tStart = null, tEnd = null;

		// send content and wait for response
		try {

			tStart = new Date();
			int status = client.executeMethod(meth);
			tEnd = new Date();
			LOGGER.debug("Publish operation finished with status " + status);
			
			
			if (status != HttpStatus.SC_OK) {
				LOGGER.error("Publish failed with status code " + status);
				return false;
			}
			
			
			// process answer?
			
			String respBody = new String(meth.getResponseBody());
			Object resp = ser.fromJSON(respBody);
			LOGGER.debug("Response: " + respBody);
			if (resp instanceof Map<?, ?>) {
				lastResult = (Map<String,Boolean>) resp;
			}
		} catch (HttpException e) {
			LOGGER.error("Failed to publish content", e);
			return false;
		} catch (IOException e) {
			LOGGER.error("Failed to publish content", e);
			return false;
		} catch (UnmarshallException e) {
			LOGGER.error("Failed to publish content", e);
			return false;
		}

		
		if (lastResult != null) {
			// run post-op on successfully published promotions
			final Collection<String> codes = lastResult.keySet(); 
			
			// codes of successfully published / cancelled promotions
			final Set<String> goodCodes = new HashSet<String>();
			for (String code : codes) {
				if (lastResult.get(code))
					goodCodes.add(code);
			}
			
			if (goodCodes.size() > 0) {
				try {
					FDPromotionNewManager.fixPromoStatusAfterPublish(goodCodes);
				} catch (FDResourceException e) {
					LOGGER.error("Failed to adjust status after publish of promotions ", e);
				}
			}

			// log publish result
			try {
				FDPromotionNewManager.logPublishEvent(agent, tStart, tEnd, promoPublishURL, lastResult, preStatuses, null, null);
			} catch (FDResourceException e) {
				LOGGER.error("Failed log publish event", e);
			}

			// also log publish event in activity log
			final Date NOW = new Date();
			for (String code : goodCodes) {
				FDPromotionNewModel promotion =null;
				try {
					promotion = FDPromotionNewManager.loadPromotion(code);//FDPromotionNewModelFactory.getInstance().getPromotion(code);
				} catch (FDResourceException e1) {
					LOGGER.error("Failed to get promotion:"+code+", after publish of promotions ", e1);
				}
				if (promotion != null) {
					FDPromoChangeModel changeModel = new FDPromoChangeModel();
					changeModel.setChangeDetails( new ArrayList<FDPromoChangeDetailModel>() );
					
					changeModel.setActionDate(NOW);
					changeModel.setActionType(EnumPromoChangeType.PUBLISH);
					changeModel.setUserId(agent.getUserId());

					promotion.addAuditChange(changeModel);
					
					// this event type has no particular details
					changeModel.setChangeDetails(new ArrayList<FDPromoChangeDetailModel>());
					
					try {
						FDPromotionNewManager.storeChangeLogEntries(promotion.getPK().getId(), promotion.getAuditChanges());
					} catch (FDResourceException e) {
						LOGGER.error("Failed to add publish entry to agent " + agent.getUserId() + "'s activity log", e);
					}
				} else {
					LOGGER.error("Unable to load missing promotion " + code);
				}
			}
			
			// return 'false' response at first negative result
			for (Boolean b : lastResult.values()) {
				if (!b)
					return false;
			}
		}

		return true;
	}
	
	public List getAllActiveWSPromotions() {
				
		// prepare serializer
		JSONSerializer ser = new JSONSerializer();
		try {
			ser.registerDefaultSerializers();
			ser.registerSerializer(FDPromotionJSONSerializer.getInstance());
		} catch (Exception e) {
			LOGGER.error("getAllActiveWSPromotions Failed to register serializer modules", e);
			return null;
		}

		final String promoPublishURL = FDStoreProperties.getPromoPublishURL();
		if (promoPublishURL == null) {
			LOGGER.error("Promotion getAllActiveWSPromotions URL is not set in fdstore.properties!");
			return null;
		}
		
		// setup http client
		HttpClient client = new HttpClient();

		GetMethod meth = new GetMethod(promoPublishURL+"?action=getWSPromosForAutoCancel");
		
		try {

			int status = client.executeMethod(meth);
			LOGGER.debug("getAllActiveWSPromotions operation finished with status " + status);
			
			if (status != HttpStatus.SC_OK) {
				LOGGER.error("Publish failed with status code " + status);
				return null;
			}						

			String respBody = new String(meth.getResponseBody());
			Object resp = ser.fromJSON(respBody);
			LOGGER.debug("getAllActiveWSPromotions: " + respBody);
			if (resp instanceof List) {
				return (List) resp;
			}
		} catch (HttpException e) {
			LOGGER.error("Failed to getAllActiveWSPromotions content", e);
			return null;
		} catch (IOException e) {
			LOGGER.error("Failed to getAllActiveWSPromotions content", e);
			return null;
		} catch (UnmarshallException e) {
			LOGGER.error("Failed to getAllActiveWSPromotions content", e);
			return null;
		}

		return null;
	}

	public List<WSAdminInfo> getWSAdminInfo() {
		
		// prepare serializer
		JSONSerializer ser = new JSONSerializer();
		try {
			ser.registerDefaultSerializers();
			ser.registerSerializer(FDPromotionJSONSerializer.getInstance());
		} catch (Exception e) {
			LOGGER.error("getWSAdminInfo Failed to register serializer modules", e);
			return null;
		}

		final String promoPublishURL = FDStoreProperties.getPromoPublishURL();
		if (promoPublishURL == null) {
			LOGGER.error("Promotion getWSAdminInfo URL is not set in fdstore.properties!");
			return null;
		}
		
		// setup http client
		HttpClient client = new HttpClient();

		GetMethod meth = new GetMethod(promoPublishURL+"?action=getWSAdminInfo");
		
		try {

			int status = client.executeMethod(meth);
			LOGGER.debug("getAllActiveWSPromotions operation finished with status " + status);
			
			if (status != HttpStatus.SC_OK) {
				LOGGER.error("Publish failed with status code " + status);
				return null;
			}						

			String respBody = new String(meth.getResponseBody());
			Object resp = ser.fromJSON(respBody);
			LOGGER.debug("getAllActiveWSPromotions: " + respBody);
			if (resp instanceof List) {
				return (List) resp;
			}
		} catch (HttpException e) {
			LOGGER.error("Failed to getAllActiveWSPromotions content", e);
			return null;
		} catch (IOException e) {
			LOGGER.error("Failed to getAllActiveWSPromotions content", e);
			return null;
		} catch (UnmarshallException e) {
			LOGGER.error("Failed to getAllActiveWSPromotions content", e);
			return null;
		}

		return null;
	}

	public static void main(String[] args) throws ParseException {
		PromoPublisher p = new PromoPublisher();
		System.out.println("Result >>>>"+p.getAllActiveWSPromotions());
	}
	
	/**
	 * For test purposes!
	 * 
	 * @param args
	 * @throws ParseException
	 */
	public static void mainEx(String[] args) throws ParseException {
		FDPromoContentModel ctnt;
		FDPromoPaymentStrategyModel paymentStrategy;
		FDPromotionNewModel promoNew;

		DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
		final Date aDate = df.parse("1973.01.31");
		
		// FDPromoContentModel class 
		ctnt = new FDPromoContentModel();
		
		ctnt.setContentId("sample_id");
		ctnt.setContentType(EnumDCPDContentType.BRAND);
		ctnt.setPK(new PrimaryKey("sample_pk"));
		ctnt.setPromotionId("promo123");

		paymentStrategy = new FDPromoPaymentStrategyModel();
		paymentStrategy.setPromotionId("Promo123");

		paymentStrategy.setOrderTypeHome(true);
		paymentStrategy.setOrderTypePickup(false);
		paymentStrategy.setOrderTypePickup(false);
		
		paymentStrategy.setPaymentType(new EnumCardType[]{EnumCardType.AMEX, EnumCardType.GCP});
		paymentStrategy.setPriorEcheckUse("tejbetok");
		
		// FDPromotionNewModel class
		promoNew = new FDPromotionNewModel();
		
		Set<String> assignedCustomerUserIds = new HashSet<String>();
		assignedCustomerUserIds.add("user1");
		assignedCustomerUserIds.add("user2");
		assignedCustomerUserIds.add("user3");
		promoNew.setAssignedCustomerUserIds(assignedCustomerUserIds);
		
		TreeMap<Date,FDPromoZipRestriction> zrt = new TreeMap<Date,FDPromoZipRestriction>();
		for(int j=0; j<3; j++) {
			final Date zrDate = df.parse("2010.01."+j);

			FDPromoZipRestriction zr = new FDPromoZipRestriction();
			zr.setStartDate(aDate);
			zr.setType("type"+j);
			zr.setZipCodes("11101,11102,11103");
			zrt.put(zrDate,zr);
		}
		promoNew.setZipRestrictions(zrt);
		
		List<FDPromotionAttributeParam> attrList = new ArrayList<FDPromotionAttributeParam>();
		for (int j=0; j<3; j++) {
			FDPromotionAttributeParam attr = new FDPromotionAttributeParam();
			attr.setPK(new PrimaryKey("12300"+j));
			attr.setAttributeName("attr"+j);
			attr.setAttributeIndex("ix"+j);
			attr.setDesiredValue("val"+j);
			attrList.add(attr);
		}
		promoNew.setAttributeList(attrList);
		
		promoNew.setPromotionCode("promo123");
		promoNew.setName("PROMO123");
		promoNew.setDescription("Desc");
		promoNew.setRedemptionCode("RDC123");
		promoNew.setStartDate(aDate);
		promoNew.setStartYear("1973");
		promoNew.setStartMonth("1");
		promoNew.setStartDay("31");
		promoNew.setExpirationDate(aDate);
		promoNew.setExpirationYear("1973");
		promoNew.setExpirationMonth("1");
		promoNew.setExpirationDay("31");
		promoNew.setRollingExpirationDays(3);
		promoNew.setMaxUsage("3");
		promoNew.setPromotionType("PT1");
		promoNew.setMinSubtotal("10");
		promoNew.setMaxAmount("2");
		promoNew.setPercentOff("5");
		promoNew.setWaiveChargeType("dontknow");
		promoNew.setStatus(EnumPromotionStatus.CANCELLED);
		promoNew.setOfferDesc("offer desc 123");
		promoNew.setAudienceDesc("valami");
		promoNew.setTerms("nothing");
		promoNew.setRedeemCount(1);
		promoNew.setSkuQuantity(10);
		promoNew.setPerishable(false);
		promoNew.setTmpAssignedCustomerUserIds("111,112,113");
		
		List<FDPromoContentModel> dcpdData = new ArrayList<FDPromoContentModel>();
		{
			dcpdData.add(ctnt);
		}
		promoNew.setDcpdData(dcpdData);
		
		List<FDPromoContentModel> cartStrategies = new ArrayList<FDPromoContentModel>();
		{
			dcpdData.add(ctnt);
		}
		promoNew.setCartStrategies(cartStrategies);

		List<FDPromoCustStrategyModel> custStrategies = new ArrayList<FDPromoCustStrategyModel>();
		{
			FDPromoCustStrategyModel custStrategy2 = new FDPromoCustStrategyModel();
			custStrategy2.setPromotionId("promo123");
			custStrategy2.setCohorts(new String[]{"C1","C2","C3"});
			custStrategy2.setDpStatus("dontknow");
			custStrategy2.setDpExpStart(aDate);
			custStrategy2.setDpExpEnd(aDate);
			custStrategies.add(custStrategy2);
		}
		promoNew.setCustStrategies(custStrategies);
		
		List<FDPromoPaymentStrategyModel> dlvPaymentStrategies = new ArrayList<FDPromoPaymentStrategyModel>();
		{
			FDPromoPaymentStrategyModel strategy = new FDPromoPaymentStrategyModel();
			strategy.setPromotionId("promo123");
			strategy.setOrderTypeHome(true);
			strategy.setOrderTypePickup(false);
			strategy.setOrderTypeCorporate(false);
			strategy.setPriorEcheckUse("nope");
			
			dlvPaymentStrategies.add(strategy);
		}
		promoNew.setPaymentStrategies(dlvPaymentStrategies);
		
		
		List<FDPromoDlvZoneStrategyModel> dlvZoneStrategies = new ArrayList<FDPromoDlvZoneStrategyModel>();
		{
			FDPromoDlvZoneStrategyModel zoneStrat = new FDPromoDlvZoneStrategyModel();
			zoneStrat.setPromotionId("promo123");
			zoneStrat.setDlvDays("Mon,Tue,Wed");
			zoneStrat.setDlvZones(new String[]{"zone1", "zone2", "zone3"});
			
			List<FDPromoDlvTimeSlotModel> dlvTimeSlots = new ArrayList<FDPromoDlvTimeSlotModel>();
			FDPromoDlvTimeSlotModel dlvTimeSlot = new FDPromoDlvTimeSlotModel();
			{
				dlvTimeSlot.setDayId(0);
				dlvTimeSlot.setDlvTimeStart("8:00");
				dlvTimeSlot.setDlvTimeEnd("10:00");
				dlvTimeSlot.setPromoDlvZoneId("<nil>");
			}
			dlvTimeSlots.add(dlvTimeSlot);

			zoneStrat.setDlvTimeSlots(dlvTimeSlots);
		}
		promoNew.setDlvZoneStrategies(dlvZoneStrategies);
		
		List<FDPromoChangeModel> auditChanges = new ArrayList<FDPromoChangeModel>();
		{
			FDPromoChangeModel audit = new FDPromoChangeModel();
			audit.setPromotionId("promo123");
			audit.setUserId("fob@fob.com");
			audit.setActionDate(aDate);
			audit.setActionType(EnumPromoChangeType.APPROVE);
			
			FDPromoChangeDetailModel detail = new FDPromoChangeDetailModel();
			detail.setPromoChangeId("pchg1");
			detail.setChangeSectionId(EnumPromotionSection.BASIC_INFO);


			detail.setChangeFieldOldValue("old");
			detail.setChangeFieldNewValue("new");
			
			List<FDPromoChangeDetailModel> changeDetails = new ArrayList<FDPromoChangeDetailModel>();
			changeDetails.add(detail);
			audit.setChangeDetails(changeDetails);
		}
		promoNew.setAuditChanges(auditChanges);
		
		promoNew.setNeedDryGoods(false);
		promoNew.setNeedCustomerList(false);

		List<FDPromotionNewModel> list = new ArrayList<FDPromotionNewModel>();
		list.add(promoNew);

		CrmAgentModel agent = new CrmAgentModel();
		agent.setUserId("bela");
		
		PromoPublisher p = new PromoPublisher();
		p.setAgent(agent);
		p.setPromoList(list);
		
		p.doPublish();
	}
}

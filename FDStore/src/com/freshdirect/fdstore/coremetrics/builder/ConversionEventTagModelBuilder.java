package com.freshdirect.fdstore.coremetrics.builder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.fdstore.coremetrics.CmContext;
import com.freshdirect.fdstore.coremetrics.tagmodel.ConversionEventTagModel;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDModifyCartModel;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.customer.adapter.FDOrderAdapter;
import com.freshdirect.framework.util.log.LoggerFactory;

public class ConversionEventTagModelBuilder  {
	private static final Logger LOGGER = LoggerFactory.getInstance(ConversionEventTagModelBuilder.class);
	
	public static String CAT_REGISTRATION = "registration";
	public static String CAT_FAQ_HELP = "faq_help";
	public static String EVENT_ORDER_ADDED = "items_added";
	public static String EVENT_ORDER_DELETED = "items_removed";
	public static String EVENT_ORDER_MODIFIED = "items_modified";
	public static String EVENT_TIME_SLOT_CHANGED = "time_slot_changed";
	public static String CAT_ORDER_MODIFIED = "order_modified";
	public static String CAT_LOGIN = "login";
	public static String EVENT_LOGIN = "login";
	public static String EVENT_BECAME_A_CUSTOMER = "became_a_customer";
	public static String EVENT_SO_HELP = "so_help";
	public static String EVENT_EMAIL = "email";
	public static String ACTION_START = "1";
	public static String ACTION_END = "2";
	public static String DEFAULT_POINTS = "1";

	private ConversionEventTagModel tagModel = new ConversionEventTagModel();
	
	private String eventId;
	private String categoryId;
	private boolean firstPhase;
	private String zipCode;
	private FDUserI user;
	private String url;
	private String subject;
	private String paramName;
	private String paramValue;
	
	private CmContext context = CmContext.getContext();
	
	public ConversionEventTagModel buildTagModel() throws SkipTagException{
		
		setDefaultModelAttributes();
		
		if (EVENT_BECAME_A_CUSTOMER.equalsIgnoreCase(eventId)){
			processBecameACustomer();

		} else if (EVENT_SO_HELP.equalsIgnoreCase(eventId)){
			processSoHelp();

		} else if (EVENT_EMAIL.equalsIgnoreCase(eventId)){
			processEmail();

		} else if (CAT_REGISTRATION.equalsIgnoreCase(categoryId)){
			processRegistration();
		
		} else if (CAT_LOGIN.equalsIgnoreCase(categoryId)){
			processLogin();
		}
		
		// prefix event category
		tagModel.setEventCategoryId( context.prefixedCategoryId( tagModel.getEventCategoryId() ) );
		
		return tagModel;
	}
	
	public List<ConversionEventTagModel> buildOrderModifiedModels(FDCartModel cartS){
		if(!(cartS instanceof FDModifyCartModel)){
			LOGGER.error("buildOrderModifiedModels failed because cartS is null");
			return null;
		}
		
		FDModifyCartModel cart = (FDModifyCartModel) cartS;
		
		ConversionEventTagModel model = new ConversionEventTagModel();
		model.setEventCategoryId(CAT_ORDER_MODIFIED);
		model.setActionType(ACTION_END);
		
		FDOrderAdapter originalCart = cart.getOriginalOrder(); 
		boolean added = false;
		boolean deleted = false;
		double addedCounter = 0d;
		double deletedCounter = 0d;
		
		/** determine whether orderline is added, removed or both */
		for(FDCartLineI cartLine : cart.getOrderLines()){
			
			boolean found = false;
			for(FDCartLineI origCartLine : originalCart.getOrderLines()){
				if(origCartLine.getCartlineId().equals(cartLine.getCartlineId())){
					
					found = true;
					
					if(origCartLine.getQuantity()>cartLine.getQuantity()){
						
						deleted = true;
						deletedCounter+=origCartLine.getQuantity()-cartLine.getQuantity();
					}
				}
				
			}
			if(!found){
				added = true;
				addedCounter+=cartLine.getQuantity();
			}			
		}
		
		/** we still have to check if a line is completely deleted */
		for(FDCartLineI origCartLine : originalCart.getOrderLines()){
			
			boolean found = false;
			
			for(FDCartLineI cartLine : cart.getOrderLines()){
				
				if(origCartLine.getCartlineId().equals(cartLine.getCartlineId())){
					found = true;
				}
			}
			
			if(!found){
				deleted=true;
				deletedCounter+=origCartLine.getQuantity();
			}
		}
		
		double priceDistance = cart.getSubTotal()-originalCart.getSubTotal();
		DecimalFormat dF = new DecimalFormat("#.##");
		model.setPoints(dF.format(priceDistance));
		
		if(added && deleted){
			
			model.setEventId(EVENT_ORDER_MODIFIED);
			double sumUnitDistance=addedCounter-deletedCounter;
			model.getAttributesMaps().put(1, sumUnitDistance+"");
			
		}else if(added){
			
			model.setEventId(EVENT_ORDER_ADDED);
			model.getAttributesMaps().put(1, addedCounter+"");
			
		}else if(deleted){
			
			model.setEventId(EVENT_ORDER_DELETED);
			model.getAttributesMaps().put(1, deletedCounter+"");
			
		}
		
		List<ConversionEventTagModel> models = new ArrayList<ConversionEventTagModel>();
		models.add(model);
		
		if(!cart.getDeliveryReservation().getStartTime().equals(originalCart.getDeliveryReservation().getStartTime())){
			ConversionEventTagModel tsModel = new ConversionEventTagModel();
			tsModel.setActionType(ACTION_END);
			tsModel.setEventCategoryId( context.prefixedCategoryId( CAT_ORDER_MODIFIED ));
			tsModel.setEventId(EVENT_TIME_SLOT_CHANGED);
			tsModel.setPoints("1");
			models.add(tsModel);
		}
		
		return models;
	}
	
	public static void completeOrderModifiedModel(ConversionEventTagModel model, FDOrderI order){
		String eventId = model.getEventId();
		
		if(EVENT_ORDER_ADDED.equals(eventId) || EVENT_ORDER_DELETED.equals(eventId) || EVENT_ORDER_MODIFIED.equals(eventId)){
			model.getAttributesMaps().put(2, TagModelUtil.getCmOrderId(order));
		}	
	}
	
	private void setDefaultModelAttributes(){
		tagModel.setEventId(eventId);
		tagModel.setEventCategoryId(categoryId);
		tagModel.setActionType(firstPhase ? ACTION_START : ACTION_END);
		tagModel.setPoints(DEFAULT_POINTS);
	}

	private void processRegistration(){
		tagModel.getAttributesMaps().put(4, zipCode);
	}

	private void processBecameACustomer() throws SkipTagException{
		
		if(TagModelUtil.getOrderCount(user) == 1){
			
			ErpAddressModel erpAddressModel = TagModelUtil.getDefaultShipToErpAddressModel(user);
			if(erpAddressModel != null){
				zipCode = erpAddressModel.getZipCode();
			}
			
			tagModel.setEventCategoryId(CAT_REGISTRATION);
			processRegistration();
		
		} else {
			throw new SkipTagException("became_a_customer: Not after first order creation");
		}
	}
	
	private void processLogin() throws SkipTagException{
		Map<Integer, String> attributesMap = tagModel.getAttributesMaps();
		
		attributesMap.put(3, user.getPrimaryKey());
		attributesMap.put(7, Integer.toString(TagModelUtil.getOrderCount(user)));
	}

	private void processSoHelp() {
		tagModel.setEventCategoryId(CAT_FAQ_HELP);
		tagModel.getAttributesMaps().put(6, url);
	}

	private void processEmail() {
		tagModel.setEventCategoryId(CAT_FAQ_HELP);
		tagModel.getAttributesMaps().put(5, subject);
	}
	
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public void setFirstPhase(boolean firstPhase) {
		this.firstPhase = firstPhase;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public void setUser(FDUserI user) {
		this.user = user;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setParamName(String paramName) {
		// TODO Auto-generated method stub
		this.paramName = paramName;
		
	}
	public void setParamValue(String paramValue) {
		// TODO Auto-generated method stub
		this.paramValue = paramValue;
		
	}
}
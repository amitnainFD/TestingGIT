package com.freshdirect.fdstore.coremetrics.builder;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;

import org.apache.log4j.Logger;

import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpCustomerModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.coremetrics.tagmodel.RegistrationTagModel;
import com.freshdirect.fdstore.customer.FDCustomerFactory;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.customer.ejb.FDCustomerManagerHome;
import com.freshdirect.fdstore.customer.ejb.FDCustomerManagerSB;
import com.freshdirect.fdstore.customer.ejb.FDServiceLocator;
import com.freshdirect.framework.util.log.LoggerFactory;

public class RegistrationTagModelBuilder  {
	
	private static final Logger LOGGER = LoggerFactory.getInstance(RegistrationTagModelBuilder.class);
	
	private FDUserI user;
	private static RegistrationTagModel tagModel = new RegistrationTagModel();
	private static FDCustomerManagerHome managerHome = null;
	private static FDServiceLocator LOCATOR = FDServiceLocator.getInstance();
	private AddressModel addressModel;
	private String location;
	private String origZipCode;
	private String email;	
    private String registrationProfileValue;
    private String registrationProfileCounty;
    String profileValue=null;
    String county=null;
	public RegistrationTagModel buildTagModel() throws SkipTagException{	
		lookupManagerHome();
		//if no address is passed explicitly, try to find out if user has a defaultShipToAddress (for existing users)
		if (addressModel == null) {
			identifyDefaultShipToAddress();
		}
		
		//try to get address info of newly registered COS user
		if (addressModel == null) {
			identifyFirstShipToAddress();
		}
		
		//address info of newly registered residential user (fall back if no defaultShipToAddress or COS address exists)
		if (addressModel == null){
			//user.getAddress() doesn't work because original zip code is lost in lite reg
			addressModel = new AddressModel(null, null, null, null, origZipCode);
		}
		
		if (addressModel != null){
			tagModel.setRegistrantCity(addressModel.getCity());
			tagModel.setRegistrantState(addressModel.getState());
			tagModel.setRegistrantPostalCode(addressModel.getZipCode());
			tagModel.setRegistrantCountry(addressModel.getCountry());
		}
		
		tagModel.setRegistrationId(user.getIdentity().getFDCustomerPK());
		tagModel.setErpId(user.getIdentity().getErpCustomerPK());
		tagModel.setRegistrantEmail(email==null ? user.getUserId(): email); 		
		LOGGER.info( "REgID # # #: "+tagModel.getRegistrationId());           
		LOGGER.info( "ERPID: "+tagModel.getErpId()); 	
				try {				
					registrationProfileValue = getProfileValue(tagModel.getRegistrationId());
					LOGGER.info( "registrantProfielvalue ====: "+registrationProfileValue);					
					tagModel.setRegistrationProfileValue(registrationProfileValue);					
				} catch (FDResourceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try{								
					registrationProfileCounty = getProfileCounty(tagModel.getErpId());
					LOGGER.info( "registrationProfileCounty ====: "+registrationProfileCounty);					
					tagModel.setRegistrationCounty(registrationProfileCounty);
				  }catch (FDResourceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			
			LOGGER.info( "Registrationprofielvaue # # #: "+tagModel.getRegistrationProfileValue()); 
		
	
	
		identifyAttributes();
		return tagModel;
	}
	
	public String getProfileValue(String customerId)throws FDResourceException{
		if (managerHome==null) {
			lookupManagerHome();
		}
		
		try {
			FDCustomerManagerSB sb = managerHome.create();
			profileValue = sb.getCustomersProfileValue(tagModel.getRegistrationId());
		
		}catch (RemoteException e) {
			invalidateManagerHome();
			throw new FDResourceException(e, "Error creating session bean");
		} catch (CreateException e) {
			invalidateManagerHome();
			throw new FDResourceException(e, "Error creating session bean");
		}
		return profileValue;
	}
	
	public String getProfileCounty(String customerId)throws FDResourceException{
		if (managerHome==null) {
			lookupManagerHome();
		}
		
		try {
			FDCustomerManagerSB sb = managerHome.create();
			county = sb.getCustomersCounty(customerId);
		
		}catch (RemoteException e) {
			invalidateManagerHome();
			throw new FDResourceException(e, "Error creating session bean");
		} catch (CreateException e) {
			invalidateManagerHome();
			throw new FDResourceException(e, "Error creating session bean");
		}
		return county;
	}
	

	public void identifyDefaultShipToAddress() throws SkipTagException{
		ErpAddressModel erpAddressModel = TagModelUtil.getDefaultShipToErpAddressModel(user);
		
		if (erpAddressModel != null){
			addressModel = new AddressModel(erpAddressModel);
		}
	}
	
	public void identifyFirstShipToAddress() throws SkipTagException{
		try {
			ErpCustomerModel custModel = FDCustomerFactory.getErpCustomer(user.getIdentity().getErpCustomerPK());
			List<ErpAddressModel> addresses = custModel.getShipToAddresses();
			if (addresses != null && addresses.size()>0){
				addressModel = new AddressModel(addresses.get(0));
			}
			
		} catch (FDResourceException e) {
			LOGGER.error(e);
			throw new SkipTagException("FDResourceException occured", e);
		}
	}
	
	public void identifyAttributes() throws SkipTagException{
		Map<Integer, String> attributesMap = tagModel.getAttributesMaps();

		EnumServiceType serviceType = user.getSelectedServiceType();
		if (serviceType != null){
			attributesMap.put(1, serviceType.toString());
		}
		
		if (location != null){
			attributesMap.put(2, location);
		}
		
		attributesMap.put(3, Integer.toString(TagModelUtil.getOrderCount(user)));
		attributesMap.put(4, user.getCohortName());
		//for enhancement 4125 marketingPromotion used as a constant value		
		attributesMap.put(5, tagModel.getRegistrationProfileValue());
		attributesMap.put(10, tagModel.getRegistrationCounty());
	}
	
	public void setUser(FDUserI user) {
		this.user = user;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}

	public void setOrigZipCode(String origZipCode) {
		this.origZipCode = origZipCode;
	}
	
	public void setAddress(AddressModel addressModel){
		this.addressModel = addressModel;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	private static void lookupManagerHome() {
		if (managerHome != null) {
			return;
		}
		managerHome = LOCATOR.getFDCustomerManagerHome();
	}
	private static void invalidateManagerHome() {
		managerHome = null;
	}
	
}
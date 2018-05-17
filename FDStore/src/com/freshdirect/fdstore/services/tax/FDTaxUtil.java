package com.freshdirect.fdstore.services.tax;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshdirect.common.pricing.EnumTaxationType;
import com.freshdirect.common.pricing.MunicipalityInfo;
import com.freshdirect.common.pricing.MunicipalityInfoWrapper;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpOrderLineModel;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDCartI;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.framework.util.MathUtil;

/*
 * @author Nakkeeran Annamalai
 */
public class FDTaxUtil {
	public static final String AVALARA_TAX = "AVALARA_TAX";
	public static final String FD_TRADITIONAL_TAX = "FD_TRADITIONAL_TAX";
	
	private static final Logger LOGGER = Logger.getLogger(FDTaxUtil.class);
	
	public static<S,T> T execute(String url, S request, Class<T> responseClass){
		final String baseUrl = FDStoreProperties.getAvalaraBaseURL();
		final String accountNumber = FDStoreProperties.getAvalaraAccountNumber();
		final String licenseKey = FDStoreProperties.getAvalaraLicenseKey();
		final PostMethod postMethod = new PostMethod(baseUrl + url);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		final String encoded = "Basic " + new String(Base64.encodeBase64((accountNumber+":"+licenseKey).getBytes())); //Create auth content
		postMethod.addRequestHeader("Authorization",encoded);
		postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		mapper.setSerializationInclusion(Include.NON_NULL);  //Tells the serializer to only include those parameters that are not null
		T response = null;
		final HttpClient client = new HttpClient();
		client.setTimeout(6000);
		try{
			if(request != null){
				final String content = mapper.writeValueAsString(request);
				postMethod.setRequestBody(content);
			}
			client.executeMethod(postMethod);
			response = mapper.readValue(postMethod.getResponseBodyAsStream(), responseClass);
		} catch (JsonProcessingException e1) {
			LOGGER.error(e1);
		} catch(IOException e){
			LOGGER.error(e);
		}
		return response;
	}
	
	public static double getTaxValue(ErpOrderLineModel orderLine) {
		return getTaxValue(orderLine.getActualPrice(), orderLine.getDiscountAmount(), null!=orderLine.getCouponDiscount()?orderLine.getCouponDiscount().getDiscountAmt():0.0, orderLine.getTaxRate(), orderLine.getTaxationType());		
	}
	
	public static double getTaxValue(double originalPrice, double promotionalDiscount, double couponsDiscount, double taxRate, EnumTaxationType taxationType) {
		double taxValue=0.0;
		double taxablePrice =0.0;
		if(null == taxationType || null == EnumTaxationType.getEnum(taxationType.getName())){
			taxationType = EnumTaxationType.TAX_AFTER_ALL_DISCOUNTS;
		}
		
		if(EnumTaxationType.TAX_AFTER_ALL_DISCOUNTS.equals(taxationType)){
			taxablePrice = originalPrice-promotionalDiscount-couponsDiscount;
		}else if(EnumTaxationType.TAX_AFTER_INTERNAL_DISCOUNTS.equals(taxationType)){
			taxablePrice = originalPrice-promotionalDiscount;
		}
		
		taxValue = MathUtil.roundDecimal(taxablePrice*taxRate);
		return taxValue > 0 ? taxValue :0;
	}
	
	public static MunicipalityInfo getMunicipalityInformation(FDCartI cart) throws FDResourceException{
		FDDeliveryManager fdMan = FDDeliveryManager.getInstance();
		MunicipalityInfoWrapper miw = fdMan.getMunicipalityInfos();
		MunicipalityInfo mi = null;
		ErpAddressModel deliveryAddress = cart.getDeliveryAddress();
		String zipcode = deliveryAddress.getZipCode();
		if(deliveryAddress.getState() != null){	
			String county = fdMan.getCounty(deliveryAddress);
			mi = miw.getMunicipalityInfo(deliveryAddress.getState(), county, deliveryAddress.getCity());
		} else {
			mi = miw.getMunicipalityInfo(fdMan.lookupStateByZip(zipcode), fdMan.lookupCountyByZip(zipcode), null);
		}
		return mi;
	}

	public static MunicipalityInfo getMunicipalityInformation(
			String zipcode) throws FDResourceException{
		FDDeliveryManager fdMan = FDDeliveryManager.getInstance();
		MunicipalityInfoWrapper miw = fdMan.getMunicipalityInfos();
		MunicipalityInfo mi = null;
		return mi = miw.getMunicipalityInfo(fdMan.lookupStateByZip(zipcode), fdMan.lookupCountyByZip(zipcode), null);
	}
}
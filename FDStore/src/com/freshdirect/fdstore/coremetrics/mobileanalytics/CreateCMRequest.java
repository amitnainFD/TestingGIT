package com.freshdirect.fdstore.coremetrics.mobileanalytics;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.coremetrics.builder.OrderTagModelBuilder;
import com.freshdirect.fdstore.coremetrics.builder.Shop5TagModelBuilder;
import com.freshdirect.fdstore.coremetrics.builder.Shop9TagModelBuilder;
import com.freshdirect.fdstore.coremetrics.builder.SkipTagException;
import com.freshdirect.fdstore.coremetrics.tagmodel.OrderTagModel;
import com.freshdirect.fdstore.coremetrics.tagmodel.ShopTagModel;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.framework.util.log.LoggerFactory;

public class CreateCMRequest {

	private static final Logger LOGGER = LoggerFactory.getInstance(CreateCMRequest.class);
	public static final int GENERAL_ERROR = 999; // Error code representing at least three consecutive errors
	
	private List<ShopTagModel> tagModels = null;
	private StringBuilder cmUrlStr = new StringBuilder("");
	private String constQuery = "";
	private CJVFContextHolder cjvfContextHolder;
	public static int consecutiveTimeout = 0;
	
	public CreateCMRequest(String fdUserId, CJVFContextHolder cjvfContextHolder) throws FDResourceException {
		this.cjvfContextHolder = cjvfContextHolder;

		cmUrlStr.append("http://").append(FDStoreProperties.getCoremetricsDataCollectionDomain()).append("/eluminate");
		cmUrlStr.append("?").append("cjen=1&vn2=mobile&vn1=4.1.1&ec=UTF-8&ul=");
		try {
			cmUrlStr.append("&cjuid=").append(URLEncoder.encode(CookieProvider.generateUniqueCJUID(fdUserId), "UTF-8"));
			cmUrlStr.append("&cjsid=").append(URLEncoder.encode(CookieProvider.generateUniqueCJSID(), "UTF-8"));
			cmUrlStr.append("&ci=").append(URLEncoder.encode(FDStoreProperties.getCoremetricsClientId(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e);
			throw new FDResourceException(e);
		}
		constQuery = cmUrlStr.toString();
	}
	
	
	
	/**
	 * Composing final URL and sending HTTP GET Request
	 * 
	 * @param customQueryPart
	 * @return httpResponseCode
	 * @throws UnsupportedEncodingException
	 */
	private int sendHttpRequest(String customQueryPart) throws UnsupportedEncodingException {
		
		HttpURLConnection conn = null;
		int responseCode = 0; // Error code representing Java Exception, not a valid HTTP response code
		try {
			StringBuilder cmUrlStr = new StringBuilder(constQuery);
			cmUrlStr.append("&st=").append(URLEncoder.encode(Long.toString(System.currentTimeMillis()), "UTF-8"));
			cmUrlStr.append("&cjvf=").append(URLEncoder.encode(Integer.toString(cjvfContextHolder.getCjvfStandingOrderFlag()), "UTF-8"));
			cmUrlStr.append(customQueryPart);
			
			if (consecutiveTimeout < 3) {
				URL cmUrl = new URL(cmUrlStr.toString());
				
				conn = (HttpURLConnection) cmUrl.openConnection();
			  conn.setRequestMethod("GET");
			  
			  /* Sending out the first HTTP request means the followings could be only subsequent tag related. */
			  this.cjvfContextHolder.thisIsSubsequentTag();
			  
			  responseCode = conn.getResponseCode();
			  if (responseCode >= 400) {
			      consecutiveTimeout ++;
			  }
			} else {
			  return GENERAL_ERROR;
			}
		} catch (Exception e) {
			consecutiveTimeout ++;
			LOGGER.error(e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return responseCode;
	}
	

	/**********************************************************************/
	/* Composing custom query parts for different tags based on tagmodels */
	/**********************************************************************/
	
	private String composePageViewQuery(String pageId, String categoryId) throws UnsupportedEncodingException {
		
		String pageViewQueryPart = "&tid=1&pi=%s&cg=%s";
		pageViewQueryPart = String.format(pageViewQueryPart, URLEncoder.encode(pageId, "UTF-8"), URLEncoder.encode(categoryId, "UTF-8"));
		return pageViewQueryPart;
	}
	
	private String composeShop5Query(ShopTagModel shop5TagModel) throws UnsupportedEncodingException {
		
		String shop5QueryPart = "&tid=4&at=5&pr=%s&pm=%s&qt=%s&bp=%s&cg=%s&att=%s&s_a1=%s&s_a2=%s&s_a3=%s&s_a4=%s&s_a5=%s&s_a6=%s&s_a7=%s";
		shop5QueryPart = String.format(shop5QueryPart, 
										URLEncoder.encode(shop5TagModel.getProductId(), "UTF-8"), 
										URLEncoder.encode(shop5TagModel.getProductName(), "UTF-8"),
										URLEncoder.encode(shop5TagModel.getQuantity(), "UTF-8"),
										URLEncoder.encode(shop5TagModel.getUnitPrice(), "UTF-8"),
										URLEncoder.encode(shop5TagModel.getCategoryId(), "UTF-8"),
										URLEncoder.encode(isNull(shop5TagModel.getAttributesMaps().get(1)), "UTF-8"),
										URLEncoder.encode(isNull(shop5TagModel.getAttributesMaps().get(2)), "UTF-8"),
										URLEncoder.encode(isNull(shop5TagModel.getAttributesMaps().get(3)), "UTF-8"),
										URLEncoder.encode(isNull(shop5TagModel.getAttributesMaps().get(4)), "UTF-8"),
										URLEncoder.encode(isNull(shop5TagModel.getAttributesMaps().get(5)), "UTF-8"),
										URLEncoder.encode(isNull(shop5TagModel.getAttributesMaps().get(6)), "UTF-8"),
										URLEncoder.encode(isNull(shop5TagModel.getAttributesMaps().get(7)), "UTF-8"));
		return shop5QueryPart;
	}

	private String composeShop9Query(ShopTagModel shop9TagModel) throws UnsupportedEncodingException {
		
		String shop9QueryPart = "&tid=4&at=9&pr=%s&pm=%s&qt=%s&bp=%s&cg=%s&on=%s&tr=%s&cd=%s&s_a1=%s&s_a2=%s&s_a3=%s&s_a4=%s&s_a5=%s&s_a6=%s&s_a7=%s";
		shop9QueryPart = String.format(shop9QueryPart, 
										URLEncoder.encode(shop9TagModel.getProductId(), "UTF-8"), 
										URLEncoder.encode(shop9TagModel.getProductName(), "UTF-8"),
										URLEncoder.encode(shop9TagModel.getQuantity(), "UTF-8"),
										URLEncoder.encode(shop9TagModel.getUnitPrice(), "UTF-8"),
										URLEncoder.encode(shop9TagModel.getCategoryId(), "UTF-8"),
										URLEncoder.encode(shop9TagModel.getOrderId(), "UTF-8"),
										URLEncoder.encode(shop9TagModel.getOrderSubtotal(), "UTF-8"),
										URLEncoder.encode(shop9TagModel.getRegistrationId(), "UTF-8"),
										URLEncoder.encode(isNull(shop9TagModel.getAttributesMaps().get(1)), "UTF-8"),
										URLEncoder.encode(isNull(shop9TagModel.getAttributesMaps().get(2)), "UTF-8"),
										URLEncoder.encode(isNull(shop9TagModel.getAttributesMaps().get(3)), "UTF-8"),
										URLEncoder.encode(isNull(shop9TagModel.getAttributesMaps().get(4)), "UTF-8"),
										URLEncoder.encode(isNull(shop9TagModel.getAttributesMaps().get(5)), "UTF-8"),
										URLEncoder.encode(isNull(shop9TagModel.getAttributesMaps().get(6)), "UTF-8"),
										URLEncoder.encode(isNull(shop9TagModel.getAttributesMaps().get(7)), "UTF-8"));

		return shop9QueryPart;
	}

	private String composeOrderQuery(OrderTagModel orderTagModel) throws UnsupportedEncodingException {
		
		String orderQueryPart = "&tid=3&on=%s&tr=%s&sg=%s&cd=%s&ct=%s&sa=%s&zp=%s&o_a1=%s&o_a2=%s&o_a3=%s&o_a4=%s&o_a5=%s&o_a6=%s&o_a7=%s&o_a8=%s&o_a9=%s";
		orderQueryPart = String.format(orderQueryPart, 
										URLEncoder.encode(orderTagModel.getOrderId(), "UTF-8"),
										URLEncoder.encode(orderTagModel.getOrderSubtotal(), "UTF-8"),
										URLEncoder.encode(orderTagModel.getOrderShipping(), "UTF-8"),
										URLEncoder.encode(orderTagModel.getRegistrationId(), "UTF-8"),
										URLEncoder.encode(orderTagModel.getRegistrantCity(), "UTF-8"),
										URLEncoder.encode(orderTagModel.getRegistrantState(), "UTF-8"),
										URLEncoder.encode(orderTagModel.getRegistrantPostalCode(), "UTF-8"),
										URLEncoder.encode(isNull(orderTagModel.getAttributesMaps().get(1)), "UTF-8"),
										URLEncoder.encode(isNull(orderTagModel.getAttributesMaps().get(2)), "UTF-8"),
										URLEncoder.encode(isNull(orderTagModel.getAttributesMaps().get(3)), "UTF-8"),
										URLEncoder.encode(isNull(orderTagModel.getAttributesMaps().get(4)), "UTF-8"),
										URLEncoder.encode(isNull(orderTagModel.getAttributesMaps().get(5)), "UTF-8"),
										URLEncoder.encode(isNull(orderTagModel.getAttributesMaps().get(6)), "UTF-8"),
										URLEncoder.encode(isNull(orderTagModel.getAttributesMaps().get(7)), "UTF-8"),
										URLEncoder.encode(isNull(orderTagModel.getAttributesMaps().get(8)), "UTF-8"),
										URLEncoder.encode(isNull(orderTagModel.getAttributesMaps().get(9)), "UTF-8"));

		return orderQueryPart;
	}

	/*************************************************************************/
	/* Public methods for sending http requests based on standing order data */
	/*************************************************************************/

	public int sendPageViewTag(String pageId, String categoryId) throws FDResourceException {
		
		String pageViewQueryPart;
		int httpResponseCode;
		
		try {
			pageViewQueryPart = composePageViewQuery(pageId, categoryId);
			httpResponseCode = sendHttpRequest(pageViewQueryPart);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e);
			throw new FDResourceException(e);
		}
		return httpResponseCode;
		
	}

	public int[] sendShop5Tags(FDCartModel cart) throws FDResourceException {
		
		int httpResponseCodes [] = null;
  		int counter = 0;
		
  		Shop5TagModelBuilder tagModelBuilder = new Shop5TagModelBuilder();
  		tagModelBuilder.setCart(cart);
  		tagModelBuilder.setStandingOrder(true);
		
  		try {
		
  			tagModels = tagModelBuilder.buildTagModels();
			httpResponseCodes = new int[tagModels.size()];
			
			for(ShopTagModel shop5TagModel : tagModels) {
				String shop5QueryPart = composeShop5Query(shop5TagModel);
				httpResponseCodes[counter++] = sendHttpRequest(shop5QueryPart);
				
			}
		} catch (SkipTagException e) {
			LOGGER.error(e);
			throw new FDResourceException(e);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e);
			throw new FDResourceException(e);
		}
		return httpResponseCodes;
		
	}
	
	public int[] sendShop9Tags(FDCartModel cart, FDOrderI order, FDUserI user) throws FDResourceException {
		
		int httpResponseCodes [] = null;
  		int counter = 0;
		
  		Shop9TagModelBuilder tagModelBuilder = new Shop9TagModelBuilder();
  		tagModelBuilder.setCart(cart);
  		tagModelBuilder.setOrder(order);
  		tagModelBuilder.setUser(user);  	
  		tagModelBuilder.setStandingOrder(true);
		
  		try {
		
  			tagModelBuilder.createTagModelPrototypesFromCart();
  			tagModels = tagModelBuilder.buildTagModels();
			httpResponseCodes = new int[tagModels.size()];
			
			for(ShopTagModel shop9TagModel : tagModels) {
				String shop9QueryPart = composeShop9Query(shop9TagModel);
				httpResponseCodes[counter++] = sendHttpRequest(shop9QueryPart);
				
			}
		} catch (SkipTagException e) {
			LOGGER.error(e);
			throw new FDResourceException(e);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e);
			throw new FDResourceException(e);
		}
		return httpResponseCodes;
		
	}

	public int sendOrderTag(FDOrderI order, FDUserI user) throws FDResourceException {
		
		int httpResponseCode;
		
  		OrderTagModelBuilder tagModelBuilder = new OrderTagModelBuilder(order, user);
		OrderTagModel orderTagModel = tagModelBuilder.buildTagModel();
		
		String orderQueryPart;
		try {
			orderQueryPart = composeOrderQuery(orderTagModel);
			httpResponseCode = sendHttpRequest(orderQueryPart);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e);
			throw new FDResourceException(e);
		}
				
		return httpResponseCode;
		
	}
	
	private String isNull(String attribute) {
		if (attribute == null) {
			attribute = "";
		}
		return attribute.toString();
	}
}

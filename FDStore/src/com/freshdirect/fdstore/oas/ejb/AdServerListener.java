package com.freshdirect.fdstore.oas.ejb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.framework.core.MessageDrivenBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;

/**@author ekracoff on Jul 20, 2004*/
public class AdServerListener extends MessageDrivenBeanSupport{

	private final static Category LOGGER = LoggerFactory.getInstance(AdServerListener.class);

	private final static int TIMEOUT = 30000;

	public void onMessage(Message msg) {
		if (!(msg instanceof ObjectMessage)) {
			LOGGER.error("Not an ObjectMessage, consuming message");
			// silently consume it, no point in throwing it back to the queue
			return;
		}

		Set results = null;
		try {

			ObjectMessage oasMsg = (ObjectMessage) msg;
			results = (Set) oasMsg.getObject();

		} catch (JMSException ex) {
			LOGGER.error("JMSException occured while reading command, throwing RuntimeException", ex);
			throw new RuntimeException("JMSException occured while reading command: " + ex.getMessage());
		}

		try {
			sendOASRow(results, FDStoreProperties.getAdServerUpdatesURL());
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		LOGGER.debug("message sent");

	}

	protected void sendOASRow(Set results, String url) throws HttpException, IOException {
		HttpClient httpClient = new HttpClient();
		httpClient.setHttpConnectionFactoryTimeout(TIMEOUT);
		httpClient.setTimeout(TIMEOUT);
		httpClient.setConnectionTimeout(TIMEOUT);

		PostMethod post =
			new PostMethod(url);
		List nameValues = new ArrayList(results.size() * 3);
		for(Iterator i = results.iterator(); i.hasNext();){
			AdServerRow row = (AdServerRow)i.next();	
			nameValues.add(new NameValuePair("product_id", row.getProductId()));
			nameValues.add(new NameValuePair("price", row.getPrice()));
			nameValues.add(new NameValuePair("available", row.isAvailable()?"Yes":"No"));
			nameValues.add(new NameValuePair("zoneId", row.getZone().getPricingZoneId()));
			nameValues.add(new NameValuePair("zoneType",row.getZoneType()));
		};
		nameValues.add(new NameValuePair("zonePricingEnabled",String.valueOf(FDStoreProperties.isZonePricingAdEnabled())));
		NameValuePair[] data = (NameValuePair[]) nameValues.toArray(new NameValuePair[nameValues.size()]);

		post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		post.setRequestBody(data);
		post.setUseExpectHeader(false);

		int responseCode = httpClient.executeMethod(post);
		LOGGER.debug("HTTP response code: " + responseCode);
		LOGGER.debug("OAS Response: " + post.getResponseBodyAsString());

		if (responseCode != 200) {
			throw new HttpException("HTTP response not 200 OK");
		}

		post.releaseConnection();
	}
}

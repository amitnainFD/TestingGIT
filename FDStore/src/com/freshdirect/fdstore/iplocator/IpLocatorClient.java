package com.freshdirect.fdstore.iplocator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.framework.util.StringUtil;
import com.freshdirect.framework.util.log.LoggerFactory;

public class IpLocatorClient {

	private static final Category LOGGER = LoggerFactory.getInstance(IpLocatorClient.class);
	
	private IpLocatorClient(){
	}
	
	public static synchronized IpLocatorClient getInstance(){
		return new IpLocatorClient();
	}
	
	public IpLocatorData getData(String ip) throws IpLocatorException{
		String rawResponse = doRequest(ip);
		Document doc = parseResponse(rawResponse);
		return extractData(doc);
	}
	
	private String doRequest(String ip) throws IpLocatorException{
		String urlStr = FDStoreProperties.getIpLocatorUrl() + "?id=" + FDStoreProperties.getIpLocatorClientId() + "&ip=" + StringUtil.encodeUrl(ip);
		LOGGER.debug("IP Locator URL: " + urlStr);
		URL url;
		try {
			url = new URL(null, urlStr, new sun.net.www.protocol.https.Handler());
		} catch (MalformedURLException e) {
			LOGGER.error(e);
			throw new IpLocatorException(e);
		}

		BufferedReader in = null; 
		try {
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(FDStoreProperties.getIpLocatorTimeout());
			conn.setReadTimeout(FDStoreProperties.getIpLocatorTimeout());
		    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
		    StringBuilder responseSb = new StringBuilder();
			String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	        	responseSb.append(inputLine);
	        }

	        String response = responseSb.toString();
	        LOGGER.debug(response);
	        return response;

		} catch (IOException e) {
			LOGGER.error(e);
			throw new IpLocatorException(e);

		} finally {
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
					LOGGER.error(e);
				}
			}
		}
	}
	
	private Document parseResponse(String rawResponse) throws IpLocatorException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db=null;
		Document doc=null;
		
		try {
			db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(rawResponse));
			doc = db.parse(is);
			if(doc!=null) {
				doc.getDocumentElement().normalize();
			}
			return doc;

		} catch (SAXException e) {
			LOGGER.error(e);
			throw new IpLocatorException(e);
		} catch (IOException e) {
			LOGGER.error(e);
			throw new IpLocatorException(e);
		} catch (ParserConfigurationException e) {
			LOGGER.error(e);
			throw new IpLocatorException(e);
		}
	}

	private IpLocatorData extractData(Document doc) throws IpLocatorException{
		try {
			String results = doc.getElementsByTagName("Results").item(0).getChildNodes().item(0).getNodeValue();
			if (results!=null && !"".equals(results.trim())){
				String errorMsg = "Results field contains error code: " + results;
				LOGGER.error(errorMsg);
				throw new IpLocatorException(errorMsg);
			}
			
			Element recordElement = (Element)doc.getElementsByTagName("Record").item(0);
			Element ipElement = (Element)recordElement.getElementsByTagName("IP").item(0);

			IpLocatorData ipLocatorData = new IpLocatorData();
			ipLocatorData.setZipCode(ipElement.getElementsByTagName("Zip").item(0).getTextContent());
			ipLocatorData.setRegion(ipElement.getElementsByTagName("Region").item(0).getTextContent());
			
			Element cityElement = (Element)ipElement.getElementsByTagName("City").item(0);
			ipLocatorData.setCity(cityElement.getElementsByTagName("Name").item(0).getTextContent());

			Element countryElement = (Element)ipElement.getElementsByTagName("Country").item(0);
			ipLocatorData.setCountryCode(countryElement.getElementsByTagName("Abbreviation").item(0).getTextContent());

			LOGGER.debug(ipLocatorData);
			return ipLocatorData;

		} catch (NullPointerException e){
			LOGGER.error(e);
			throw new IpLocatorException(e);
		} catch (ClassCastException e){
			LOGGER.error(e);
			throw new IpLocatorException(e);
		}
	}
	
}

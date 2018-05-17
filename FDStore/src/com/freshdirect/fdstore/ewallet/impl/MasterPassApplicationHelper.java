/**
 * 
 */
package com.freshdirect.fdstore.ewallet.impl;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.mastercard.mcwallet.sdk.MasterPassServiceRuntimeException;

/**
 * @author Aniwesh 
 *
 */
public class MasterPassApplicationHelper {
	/**
	 * Method to indent and format XML strings to be displayed.
	 * 
	 * @param input
	 * @param indent
	 * 
	 * @return Formatted XML string
	 */
	private static String prettyFormat(String input, String indent) {
	    try {
	    	//
	    	if (input == null || input.equals("")) { 
	    		return input;
	    	}
	    	input = input.replace(">  <", "><");
	    	if (input.contains("<html>") ) {
	    		return input;
	    	}
	        Source xmlInput = new StreamSource(new StringReader(input));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        
	        Transformer transformer = transformerFactory.newTransformer();
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",indent);
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        transformer.transform(xmlInput, xmlOutput);
	        
	        return xmlOutput.getWriter().toString();
	    } catch (Exception e) {
	    	throw new MasterPassServiceRuntimeException(e);
	    }
	}
	
	public static String prettyFormat(String input) {
	    return prettyFormat(input,"4");
	}
	
	/**
	 * Converts a MerchantTransactions to a String containing all the data in the class in XML format
	 * 
	 * @param merchantTransactions
	 * 
	 * @return Marshaled string containing the data stored in merchantTransactions in an XML format
	 * 
	 * @throws JAXBException
	 */
	public static String printXML(Object xmlClass) {

			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(xmlClass.getClass());
				StringWriter st = new StringWriter();
				jaxbContext.createMarshaller().marshal(xmlClass, st);
				String xml = st.toString();
				return xml;
				
			} catch (JAXBException e) {
				throw new MasterPassServiceRuntimeException(e);
			}
	}
	
	/**
	 * Method to escape any HTML tags for displaying the XML in a web page.
	 * This method is for displaying the data only.
	 * 
	 * @param t
	 * 
	 * @return String with the escaped XML
	 */
	public static String xmlEscapeText(String t) {

		if(t!= null){
		StringBuilder sb = new StringBuilder();
		   for(int i = 0; i < t.length(); i++){
		      char c = t.charAt(i);
		      switch(c){
		      case '<': sb.append("&lt;"); break;
		      case '>': sb.append("&gt;"); break;
		      case '\"': sb.append("&quot;"); break;
		      case '&': sb.append("&amp;"); break;
		      case '\'': sb.append("&apos;"); break;
		      default:
//		         if(c>0x7e) {
//		            sb.append("&#"+((int)c)+";");
//		         }else
		            sb.append(c);
		      }
		   }
		   return sb.toString();
		   }
		return "";
	}
	
	
	  static Map<String, Integer> e2i = new HashMap<String, Integer>();

	  static Map<Integer, String> i2e = new HashMap<Integer, String>();
	  // html entity list
	  private static Object[][] entities = { { "quot", new Integer(34) }, // " - double-quote
	      { "copy", new Integer(169) }, 
	      { "reg", new Integer(174) }, 
	      { "Agrave", new Integer(192) }, 
	      { "Aacute", new Integer(193) }, 
	      { "Acirc", new Integer(194) }, 
	      { "Atilde", new Integer(195) }, 
	      { "Auml", new Integer(196) }, 
	      { "Aring", new Integer(197) }, 
	      { "AElig", new Integer(198) }, 
	      { "Ccedil", new Integer(199) }, 
	      { "Egrave", new Integer(200) }, 
	      { "Eacute", new Integer(201) }, 
	      { "Ecirc", new Integer(202) }, 
	      { "Euml", new Integer(203) }, 
	      { "Igrave", new Integer(204) }, 
	      { "Iacute", new Integer(205) }, 
	      { "Icirc", new Integer(206) }, 
	      { "Iuml", new Integer(207) }, 
	      { "ETH", new Integer(208) }, 
	      { "Ntilde", new Integer(209) }, 
	      { "Ograve", new Integer(210) }, 
	      { "Oacute", new Integer(211) }, 
	      { "Ocirc", new Integer(212) }, 
	      { "Otilde", new Integer(213) }, 
	      { "Ouml", new Integer(214) }, 
	      { "Oslash", new Integer(216) }, 
	      { "Ugrave", new Integer(217) }, 
	      { "Uacute", new Integer(218) }, 
	      { "Ucirc", new Integer(219) }, 
	      { "Uuml", new Integer(220) }, 
	      { "Yacute", new Integer(221) }, 
	      { "THORN", new Integer(222) }, 
	      { "szlig", new Integer(223) }, 
	      { "agrave", new Integer(224) }, 
	      { "aacute", new Integer(225) }, 
	      { "acirc", new Integer(226) }, 
	      { "atilde", new Integer(227) }, 
	      { "auml", new Integer(228) }, 
	      { "aring", new Integer(229) }, 
	      { "aelig", new Integer(230) }, 
	      { "ccedil", new Integer(231) }, 
	      { "egrave", new Integer(232) }, 
	      { "eacute", new Integer(233) }, 
	      { "ecirc", new Integer(234) }, 
	      { "euml", new Integer(235) }, 
	      { "igrave", new Integer(236) }, 
	      { "iacute", new Integer(237) }, 
	      { "icirc", new Integer(238) }, 
	      { "iuml", new Integer(239) }, 
	      { "igrave", new Integer(236) }, 
	      { "iacute", new Integer(237) }, 
	      { "icirc", new Integer(238) }, 
	      { "iuml", new Integer(239) }, 
	      { "eth", new Integer(240) }, 
	      { "ntilde", new Integer(241) }, 
	      { "ograve", new Integer(242) }, 
	      { "oacute", new Integer(243) }, 
	      { "ocirc", new Integer(244) }, 
	      { "otilde", new Integer(245) }, 
	      { "ouml", new Integer(246) }, 
	      { "oslash", new Integer(248) }, 
	      { "ugrave", new Integer(249) }, 
	      { "uacute", new Integer(250) }, 
	      { "ucirc", new Integer(251) }, 
	      { "uuml", new Integer(252) }, 
	      { "yacute", new Integer(253) }, 
	      { "thorn", new Integer(254) }, 
	      { "yuml", new Integer(255) }, 
	      { "euro", new Integer(8364) },
	  };
	
	  static {
	    for (int i = 0; i < entities.length; i++)
	        e2i.put((String) entities[i][0], (Integer) entities[i][1]);
	    for (int i = 0; i < entities.length; i++)
	        i2e.put((Integer) entities[i][1], (String) entities[i][0]);
	  }
	public static String replaceSpecialCharsWithBlanks(String s1) {
	    StringBuffer buf = new StringBuffer();

	    int i;

	    for (i = 0; i < s1.length(); ++i)
	    {

	      char ch = s1.charAt(i);

	      if (ch == '&')
	      {
	        int semi = s1.indexOf(';', i + 1);
	        if (semi == -1)
	        {
	          buf.append(ch);
	          continue;
	        }
	        String entity = s1.substring(i + 1, semi);
	        Integer iso;
	        if (entity.charAt(0) == '#')
	        {
	          iso = new Integer(entity.substring(1));
	        }
	        else
	        {
	          iso = e2i.get(entity);
	        }
	        if (iso == null)
	        {
	          buf.append("&" + entity + ";");
	        }
	        else
	        {
	          //Just for info that empty char is introduced.
	          buf.append("");
	        }
	        i = semi;
	      }
	      else
	      {
	        buf.append(ch);
	      }
	    }

	    return buf.toString();
	}
	
	/**
	 * Method to format the error messages that will be displayed in the test app.
	 * This method is for displaying only.
	 * 
	 * @param errorMessage
	 * 
	 * @return
	 */
	public static String formatErrorMessage(String errorMessage) {
		if (errorMessage.contains("<Errors>")) {
			return xmlEscapeText(prettyFormat(errorMessage));
		}
		else {
			return errorMessage;
		}
	}
	/**
	 * This method is used only to make the shipping image URL dynamic when changing the app URL and context.
	 * This method is not intented to be used in a production application or environment.
	 * 
	 * @param  shoppingCartRequest
	 * @param  data
	 * 
	 * @return String with the replaced image URLs
	 */
//	public static String xmlReplaceImageUrl(String shoppingCartRequest,MasterpassData data) {
//		return shoppingCartRequest.replaceAll("http://ech-0a9d8167.corp.mastercard.test:8080/SampleApp",data.getAppBaseUrl() + data.getContextPath() );
//	}
	
	public static String xmlReplaceImageUrl(String shoppingCartRequest,MasterpassData data) {
		return shoppingCartRequest.replaceAll("http://ech-0a9d8167.corp.mastercard.test:8080/SampleApp",data.getAppBaseUrl() + data.getContextPath() );
	}
}

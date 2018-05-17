package com.freshdirect.fdstore.mail;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.freshdirect.ErpServicesProperties;
import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.framework.mail.EmailSupport;
import com.freshdirect.framework.mail.XMLEmailI;

public class FDInfoEmail extends EmailSupport implements XMLEmailI {
	private static final long serialVersionUID = -4523407316264096202L;

	private final FDCustomerInfo customer;

	private String htmlXsl;
	private String textXsl;
	
	private boolean htmlEmail = true;
	private String fromEmail;
	
	public FDInfoEmail(FDCustomerInfo customer) {
		super();
		this.customer = customer;

		this.htmlEmail = customer.isHtmlEmail();
	}

	/**
	 * @see com.freshdirect.framework.mail.AbstractEmail#getRecipient()
	 */
	public String getRecipient() {
		return this.customer.getEmailAddress();
	}

	/**
	 * @see com.freshdirect.framework.mail.XMLEmail#isHtmlEmail()
	 */
	@Override
	public boolean isHtmlEmail() {
		return this.htmlEmail;
	}

	/**
	 * Enables manual override of HTML mode. Used only for test purposes.
	 * @param htmlEmail
	 */
	public void setHtmlEmail(boolean htmlEmail) {
		this.htmlEmail = htmlEmail;
	}

	public void setXslPath(String htmlXsl, String textXsl) {
		this.htmlXsl = ErpServicesProperties.getMailerXslHome() + htmlXsl;
		this.textXsl = ErpServicesProperties.getMailerXslHome() + textXsl;
	}

	/**
	 * @see com.freshdirect.framework.mail.XMLEmail#getXslPath()
	 */
	@Override
	public String getXslPath() {
		return isHtmlEmail() ? this.htmlXsl : this.textXsl;
	}

	/**
	 * @see com.freshdirect.framework.mail.XMLEmail#getXML()
	 */
	@Override
	public final String getXML() {
		FDXMLSerializer s = new FDXMLSerializer();
		Map map = new HashMap();
		this.decorateMap(map);
		//String xmlString = s.serialize("fdemail", map); 
		//System.out.print(xmlString);
		return s.serialize("fdemail", map);
	}

	protected void decorateMap(Map map) {
		map.put("customer", this.customer);
		map.put("fromEmail", this.getFromEmail()); 
		map.put("curYear", Calendar.getInstance().get(Calendar.YEAR)); //used in footer
	}
	
	public String getFromEmail() {
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

}

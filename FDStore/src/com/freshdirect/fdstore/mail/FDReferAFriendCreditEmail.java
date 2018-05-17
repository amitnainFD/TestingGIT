package com.freshdirect.fdstore.mail;

import java.util.HashMap;
import java.util.Map;

import com.freshdirect.ErpServicesProperties;
import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.framework.mail.EmailSupport;
import com.freshdirect.framework.mail.XMLEmailI;

public class FDReferAFriendCreditEmail extends EmailSupport implements XMLEmailI {

	private static final long serialVersionUID = 1L;
	private String name;
	private String userMessage;

	public FDReferAFriendCreditEmail(String name, String message) {			
		this.name = name;
		this.userMessage = message;
	}

	/**
	 * @see com.freshdirect.fdstore.mail.FDInfoEmail#decorateMap(java.util.Map)
	 */
	protected void decorateMap(Map map) {
		map.put("name", this.name);
		map.put("userMessage", userMessage);
	}

	@Override
	public final String getXML() {
		FDXMLSerializer s = new FDXMLSerializer();
		Map map = new HashMap();
		this.decorateMap(map);
		return s.serialize("fdemail", map);
	}		

	@Override
	public String getXslPath() {
		return ErpServicesProperties.getMailerXslHome() + "h_referral_credit_earned.xsl";
	}

	@Override
	public boolean isHtmlEmail() {			
		return true;
	}

}

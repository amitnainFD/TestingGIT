package com.freshdirect.fdstore.mail;

import java.util.HashMap;
import java.util.Map;

import com.freshdirect.ErpServicesProperties;
import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.framework.mail.EmailSupport;
import com.freshdirect.framework.mail.XMLEmailI;

public class FDReferAFriendInvEmail extends EmailSupport implements XMLEmailI {

	private static final long serialVersionUID = 1L;
	private String name;
	private String userMessage;
	private String systemMessage;
	private String legal;
	private String refLink;
	private String offerText;

	public FDReferAFriendInvEmail(String name, String userMessage, String systemMessage, String legal, String refLink, String offerText) {			
		this.name = name;
		this.userMessage = userMessage;
		this.systemMessage = systemMessage;
		this.legal = legal;
		this.refLink = refLink;
		this.offerText = offerText;
	}

	/**
	 * @see com.freshdirect.fdstore.mail.FDInfoEmail#decorateMap(java.util.Map)
	 */
	protected void decorateMap(Map map) {
		map.put("name", this.name);
		map.put("userMessage", userMessage);
		map.put("systemMessage", systemMessage);
		map.put("legal", legal);
		map.put("refLink", refLink);
		map.put("offerText", offerText);
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
		return ErpServicesProperties.getMailerXslHome() + "x_refer_a_friend_invite.xsl";
	}

	@Override
	public boolean isHtmlEmail() {			
		return true;
	}

}

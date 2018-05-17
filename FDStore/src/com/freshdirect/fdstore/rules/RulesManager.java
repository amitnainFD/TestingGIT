package com.freshdirect.fdstore.rules;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.rules.ejb.RulesManagerHome;
import com.freshdirect.fdstore.rules.ejb.RulesManagerSB;
import com.freshdirect.framework.core.ServiceLocator;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.rules.AbstractRuleStore;
import com.freshdirect.rules.ClassDescriptor;
import com.freshdirect.rules.Rule;
import com.freshdirect.rules.RulesConfig;
import com.freshdirect.rules.RulesRuntimeException;
import com.thoughtworks.xstream.XStream;

public class RulesManager extends AbstractRuleStore {
	
	private final ServiceLocator serviceLocator;
	private static final Category LOGGER = LoggerFactory.getInstance(RulesManager.class);
	private final RulesConfig config;
	private final XStream xstream = new XStream();

	
	/**
	 * private constructor to ensure single instance of FDDeliveryManager
	 */
	public RulesManager(String subsystem, List configurations) throws NamingException {
		this.serviceLocator = new ServiceLocator(FDStoreProperties.getInitialContext());
		this.config = this.findConfig(configurations, subsystem);
		this.configureXStream();
	}

	private void configureXStream() {
		List l = new ArrayList();
		l.addAll(config.getConditionTypes());
		l.addAll(config.getOutcomeTypes());
		for (Iterator i = l.iterator(); i.hasNext();) {
			ClassDescriptor desc = (ClassDescriptor) i.next();
			xstream.alias(desc.getXmlTag(), desc.getTargetClass());
		}
	}

	public RulesManagerHome getRulesManagerHome() {
		try {
			return (RulesManagerHome) serviceLocator.getRemoteHome(FDStoreProperties.getRulesManagerHome());
		} catch (NamingException ne) {
			throw new EJBException(ne);
		}
	}

	@Override
	public String getSubsystem() {
		return this.config.getSubsystem();
	}

	@Override
	public Map<String, Rule> getRules() {
		try{
			RulesManagerSB sb = getRulesManagerHome().create();
			Map<String, Rule> rules = sb.getRules(getSubsystem());
			for(Rule r:rules.values()){
				createXMLObject(r);
			}
			return rules;
		} catch (FDResourceException e) {
			throw new RulesRuntimeException(e, "Cannot talk to the SessionBean");
		} catch (CreateException e) {
			throw new RulesRuntimeException(e, "Cannot create SessionBean");
		} catch (RemoteException e) {
			throw new RulesRuntimeException(e, "Cannot talk to the SessionBean");
		}
	}
	

	private void createXMLObject(Rule r) {
		
		List l = new ArrayList();

		try {
			ObjectInputStream in = xstream.createObjectInputStream(new StringReader(r.getConditionStr()));
			while (true) {
				try {
					l.add(in.readObject());
				} catch (EOFException e) {
					break;
				}
			}

		} catch (ClassNotFoundException e) {
			throw new RulesRuntimeException("Bad data in Conditions: " + r.getConditionStr());
		} catch (IOException e) {
			throw new RulesRuntimeException("Bad data in Conditions: " + r.getConditionStr());
		}

		if (!"".equals(r.getOutcomeStr())) {
			Object o = xstream.fromXML(r.getOutcomeStr());
			r.setOutcome(o);
		}

		r.setConditions(l);

	}

	@Override
	public Rule getRule(String ruleId) {
		try{
			RulesManagerSB sb = getRulesManagerHome().create();
			Rule r = sb.getRule(ruleId);
			createXMLObject(r);
			return r;
		} catch (FDResourceException e) {
			throw new RulesRuntimeException(e, "Cannot talk to the SessionBean");
		} catch (CreateException e) {
			throw new RulesRuntimeException(e, "Cannot create SessionBean");
		} catch (RemoteException e) {
			throw new RulesRuntimeException(e, "Cannot talk to the SessionBean");
		}
	}

	@Override
	public void storeRule(Rule rule) {
		try{
			RulesManagerSB sb = getRulesManagerHome().create();
			rule.setConditionStr(getConditionsXML(rule.getConditions()).trim());
			rule.setOutcomeStr(xstream.toXML(rule.getOutcome()).trim());
			
			sb.storeRule(rule);
		} catch (FDResourceException e) {
			throw new RulesRuntimeException(e, "Cannot talk to the SessionBean");
		} catch (CreateException e) {
			throw new RulesRuntimeException(e, "Cannot create SessionBean");
		} catch (RemoteException e) {
			throw new RulesRuntimeException(e, "Cannot talk to the SessionBean");
		}
	}

	@Override
	public void deleteRule(String ruleId) {
		try{
			RulesManagerSB sb = getRulesManagerHome().create();
			sb.deleteRule(ruleId);
		} catch (FDResourceException e) {
			throw new RulesRuntimeException(e, "Cannot talk to the SessionBean");
		} catch (CreateException e) {
			throw new RulesRuntimeException(e, "Cannot create SessionBean");
		} catch (RemoteException e) {
			throw new RulesRuntimeException(e, "Cannot talk to the SessionBean");
		}
	}
	private String getConditionsXML(List conditions) {
		return xstream.toXML(conditions).replaceAll("\\<list>|</list>|\n|\t", "");
	}


	
	}

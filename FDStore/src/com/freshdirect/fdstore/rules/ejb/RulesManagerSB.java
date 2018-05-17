package com.freshdirect.fdstore.rules.ejb;

import java.rmi.RemoteException;
import java.util.Map;

import javax.ejb.EJBObject;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.rules.Rule;
import com.freshdirect.rules.RulesConfig;

public interface RulesManagerSB extends EJBObject {

	Map<String, Rule> getRules(String subsystem) throws FDResourceException, RemoteException;

	Rule getRule(String ruleId) throws FDResourceException,RemoteException;

	void deleteRule(String ruleId) throws FDResourceException,RemoteException;

	void storeRule(Rule rule) throws FDResourceException,RemoteException;

}

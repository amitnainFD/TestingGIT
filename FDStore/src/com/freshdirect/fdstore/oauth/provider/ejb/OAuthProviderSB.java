package com.freshdirect.fdstore.oauth.provider.ejb;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;

/**
 * @author Tamas Gelesz
 */
public interface OAuthProviderSB extends EJBObject {

	void deleteOldAccessors() throws OAuthException, RemoteException;
	
	OAuthConsumer getConsumer(OAuthMessage requestMessage) throws IOException, OAuthProblemException, RemoteException;

	OAuthAccessor getAccessor(OAuthMessage requestMessage) throws IOException, OAuthException, RemoteException;

	OAuthAccessor markAsAuthorized(OAuthAccessor accessor, String userId) throws OAuthException, RemoteException;

	OAuthAccessor generateRequestToken(OAuthAccessor accessor) throws OAuthException, RemoteException;

	OAuthAccessor generateAccessToken(OAuthAccessor accessor) throws OAuthException, RemoteException;

	String getProviderUrl() throws RemoteException;
	
	void validateMessage(OAuthMessage message, OAuthAccessor accessor) throws OAuthException, IOException, URISyntaxException, RemoteException;
}

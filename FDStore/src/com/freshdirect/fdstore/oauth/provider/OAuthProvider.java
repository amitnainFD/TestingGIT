package com.freshdirect.fdstore.oauth.provider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.server.OAuthServlet;

import com.freshdirect.fdstore.customer.ejb.FDServiceLocator;
import com.freshdirect.fdstore.oauth.provider.ejb.OAuthProviderHome;
import com.freshdirect.fdstore.oauth.provider.ejb.OAuthProviderSB;

/**
 * @author Tamas Gelesz
 */
public class OAuthProvider {

	private static FDServiceLocator LOCATOR = FDServiceLocator.getInstance();
	
	private static OAuthProviderHome getOAuthProviderHome() {
		try {
			return (OAuthProviderHome) LOCATOR.getRemoteHome("freshdirect.oauth.OAuthProvider");
		} catch (NamingException e) {
			throw new EJBException(e);
		}
	}

    public static synchronized OAuthConsumer getConsumer(OAuthMessage requestMessage) throws IOException, OAuthProblemException {
    	OAuthProviderHome home = getOAuthProviderHome();
		try {
			OAuthProviderSB oAuthProviderSB = home.create();
			return oAuthProviderSB.getConsumer(requestMessage);
		} catch (RemoteException e) {
			throw new EJBException(e);
		} catch (CreateException e) {
			throw new EJBException(e);
		}
    }
    
    public static synchronized OAuthAccessor getAccessor(OAuthMessage requestMessage) throws IOException, OAuthException {
    	OAuthProviderHome home = getOAuthProviderHome();
		try {
			OAuthProviderSB oAuthProviderSB = home.create();
			return oAuthProviderSB.getAccessor(requestMessage);
		} catch (RemoteException e) {
			throw new EJBException(e);
		} catch (CreateException e) {
			throw new EJBException(e);
		}
    }

    public static synchronized OAuthAccessor markAsAuthorized(OAuthAccessor accessor, String userId) throws OAuthException {
    	OAuthProviderHome home = getOAuthProviderHome();
		try {
			OAuthProviderSB oAuthProviderSB = home.create();
			return oAuthProviderSB.markAsAuthorized(accessor, userId);
		} catch (RemoteException e) {
			throw new EJBException(e);
		} catch (CreateException e) {
			throw new EJBException(e);
		}
    }

    public static synchronized OAuthAccessor generateRequestToken(OAuthAccessor accessor) throws OAuthException {
    	OAuthProviderHome home = getOAuthProviderHome();
		try {
			OAuthProviderSB oAuthProviderSB = home.create();
			return oAuthProviderSB.generateRequestToken(accessor);
		} catch (RemoteException e) {
			throw new EJBException(e);
		} catch (CreateException e) {
			throw new EJBException(e);
		}
	}

    public static synchronized OAuthAccessor generateAccessToken(OAuthAccessor accessor) throws OAuthException {
    	OAuthProviderHome home = getOAuthProviderHome();
		try {
			OAuthProviderSB oAuthProviderSB = home.create();
			return oAuthProviderSB.generateAccessToken(accessor);
		} catch (RemoteException e) {
			throw new EJBException(e);
		} catch (CreateException e) {
			throw new EJBException(e);
		}
    }

    public static void handleException(Exception e, HttpServletRequest request, HttpServletResponse response, boolean sendBody) throws IOException, ServletException {
        String realm = (request.isSecure())?"https://":"http://";
        realm += request.getLocalName();
        OAuthServlet.handleException(response, e, realm, sendBody); 
    }

    public static String getProviderUrl(){
    	OAuthProviderHome home = getOAuthProviderHome();
		try {
			OAuthProviderSB oAuthProviderSB = home.create();
			return oAuthProviderSB.getProviderUrl();
		} catch (RemoteException e) {
			throw new EJBException(e);
		} catch (CreateException e) {
			throw new EJBException(e);
		}
    }

    public static void validateMessage(OAuthMessage message, OAuthAccessor accessor) throws OAuthException, IOException, URISyntaxException{
    	OAuthProviderHome home = getOAuthProviderHome();
		try {
			OAuthProviderSB oAuthProviderSB = home.create();
			oAuthProviderSB.validateMessage(message, accessor);
		} catch (RemoteException e) {
			throw new EJBException(e);
		} catch (CreateException e) {
			throw new EJBException(e);
		}
    }

    public static void deleteOldAccessors() throws OAuthException{
    	OAuthProviderHome home = getOAuthProviderHome();
		try {
			OAuthProviderSB oAuthProviderSB = home.create();
			oAuthProviderSB.deleteOldAccessors();
		} catch (RemoteException e) {
			throw new EJBException(e);
		} catch (CreateException e) {
			throw new EJBException(e);
		}
    }
}

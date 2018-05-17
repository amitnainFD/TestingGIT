package com.freshdirect.fdstore.customer.accounts.external;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.CreateException;

import org.apache.log4j.Category;

import com.freshdirect.customer.EnumExternalLoginSource;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.ejb.FDServiceLocator;
import com.freshdirect.framework.util.log.LoggerFactory;

public class ExternalAccountManager{
	
	private static Category LOGGER = LoggerFactory.getInstance(ExternalAccountManager.class);
	
	private static ExternalAccountManagerHome externalLoginManagerHome = null;
	
	private static FDServiceLocator LOCATOR = FDServiceLocator.getInstance();
	
	public static String getUserIdForUserToken(String userToken)  throws FDResourceException {
		lookupManagerHome();
		String userId="";
		try {
			ExternalAccountManagerSB sb = externalLoginManagerHome.create();
			userId = sb.getUserIdForUserToken(userToken);
			LOGGER.info("USER ID for token: "+userToken+" is:"+userId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		return userId;

	}
	
	public static boolean isUserEmailAlreadyExist(String email) throws FDResourceException {
		lookupManagerHome();
		try {
			ExternalAccountManagerSB sb = externalLoginManagerHome.create();
			return sb.isUserEmailAlreadyExist(email);
			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static int isUserEmailAlreadyExist(String email, String provider) throws FDResourceException {
		lookupManagerHome();
		try {
			ExternalAccountManagerSB sb = externalLoginManagerHome.create();
			return sb.isUserEmailAlreadyExist(email, provider);
			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}	
	
	
	public static List<String> getConnectedProvidersByUserId(String userId, EnumExternalLoginSource source) throws FDResourceException {
		lookupManagerHome();
		try {
			ExternalAccountManagerSB sb = externalLoginManagerHome.create();
			return sb.getConnectedProvidersByUserId(userId, source);
			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static List<String> getConnectedProvidersByUserId(String userId) throws FDResourceException {
		lookupManagerHome();
		try {
			ExternalAccountManagerSB sb = externalLoginManagerHome.create();
			return sb.getConnectedProvidersByUserId(userId);
			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static boolean isExternalLoginOnlyUser(String userId, EnumExternalLoginSource source) throws FDResourceException
	{
		lookupManagerHome();
		try {
			ExternalAccountManagerSB sb = externalLoginManagerHome.create();
			return sb.isExternalLoginOnlyUser(userId, source);
			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	
	}
	
	
	
	public static void linkUserTokenToUserId(String customerId, String userId,String userToken, String identityToken, String provider, String displayName, String preferredUserName, String email, String emailVerified) throws FDResourceException
	{
		lookupManagerHome();
		try {
			ExternalAccountManagerSB sb = externalLoginManagerHome.create();
			sb.linkUserTokenToUserId(customerId, userId, userToken, identityToken, provider, displayName, preferredUserName, email, emailVerified);
			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	
	private static void invalidateManagerHome() {
		externalLoginManagerHome = null;
	}

	private static void lookupManagerHome() {
		if (externalLoginManagerHome != null) {
			return;
		}
		externalLoginManagerHome = LOCATOR.getExternalLoginManagerHome();
	}

	public static void unlinkExternalAccountWithUser(String email, String userToken, String provider) throws FDResourceException {
		lookupManagerHome();
		try {
			ExternalAccountManagerSB sb = externalLoginManagerHome.create();
			sb.unlinkExternalAccountWithUser(email, userToken, provider);
			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		
	}
	
	public static void unlinkExternalAccountWithUser(String email, String provider) throws FDResourceException {
		lookupManagerHome();
		try {
			ExternalAccountManagerSB sb = externalLoginManagerHome.create();
			sb.unlinkExternalAccountWithUser(email, provider);
			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		
	}
	
	
	public static boolean isSocialLoginOnlyUser(String customer_id) throws FDResourceException {
		lookupManagerHome();
		try {
			ExternalAccountManagerSB sb = externalLoginManagerHome.create();
			return sb.isSocialLoginOnlyUser(customer_id);
			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}	

}

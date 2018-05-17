package com.freshdirect.fdstore.customer.accounts.external;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.List;

import javax.ejb.EJBObject;

import com.freshdirect.customer.EnumExternalLoginSource;

public interface ExternalAccountManagerSB extends EJBObject  {
	
	public String getUserIdForUserToken(String userToken) throws RemoteException;
	
	public boolean isUserEmailAlreadyExist(String email) throws RemoteException;
	
	public int isUserEmailAlreadyExist(String email, String provider) throws RemoteException;
	
	public void linkUserTokenToUserId(String customerId, String userId,String userToken, String identityToken, String provider, String displayName, String preferredUserName, String email, String emailVerified) throws RemoteException;

	public List<String> getConnectedProvidersByUserId(String userId, EnumExternalLoginSource source) throws RemoteException;
	
	public boolean isExternalLoginOnlyUser(String userId, EnumExternalLoginSource source) throws RemoteException;

	public void unlinkExternalAccountWithUser(String email, String userToken, String provider) throws RemoteException;
	
	public void unlinkExternalAccountWithUser(String email, String provider) throws RemoteException;
	
	public boolean isSocialLoginOnlyUser(String customer_id) throws RemoteException; 

	public List<String> getConnectedProvidersByUserId(String userId) throws RemoteException;
	
}

package com.freshdirect.fdstore.survey.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface FDSurveyHome extends EJBHome {
	
	public FDSurveySB create() throws CreateException, RemoteException;

}


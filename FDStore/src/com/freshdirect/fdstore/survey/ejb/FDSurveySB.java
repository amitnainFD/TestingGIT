package com.freshdirect.fdstore.survey.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.survey.FDSurvey;
import com.freshdirect.fdstore.survey.FDSurveyResponse;
import com.freshdirect.fdstore.survey.SurveyKey;

public interface FDSurveySB extends EJBObject {
	public FDSurvey getSurvey(SurveyKey key) throws RemoteException;
	public FDSurveyResponse getCustomerProfile(FDIdentity identity, EnumServiceType serviceType) throws RemoteException;
	public FDSurveyResponse getSurveyResponse(FDIdentity identity, SurveyKey key) throws RemoteException;
	public void storeSurvey(FDSurveyResponse survey) throws FDResourceException,RemoteException;
}




package com.freshdirect.fdstore.survey;

import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDUserI;

/**
 * Caching proxy for FDFactory.
 * 
 * @version $Revision$
 * @author $Author$
 */
public class FDSurveyCachedFactory {

    /**
     * Get current survey information object for surveyId.
     * 
     * @param surveyId
     *            surveyId
     * 
     * @return FDSurvey object
     * @throws FDResourceException
     * 
     */
    public static FDSurvey getSurvey(EnumSurveyType surveyType, EnumServiceType userType) throws FDResourceException {
        return FDSurveyFactory.getInstance().getSurvey(surveyType, userType);        
    }
    
    public static FDSurvey getSurvey(EnumSurveyType surveyType, FDUserI user) throws FDResourceException {
        return FDSurveyFactory.getInstance().getSurvey(surveyType, user);        
    }
    

}

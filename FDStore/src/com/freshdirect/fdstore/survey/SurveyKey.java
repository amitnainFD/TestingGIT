/**
 * 
 */
package com.freshdirect.fdstore.survey;

import java.io.Serializable;

import com.freshdirect.common.customer.EnumServiceType;

public class SurveyKey implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    EnumSurveyType surveyType;
    EnumServiceType userType;

    public SurveyKey(EnumSurveyType surveyType, EnumServiceType userType) {
        super();
        this.surveyType = surveyType;
        this.userType = userType;
    }

    public SurveyKey(String surveyType, EnumServiceType userType) {
        super();
        this.surveyType = EnumSurveyType.getEnum(surveyType);
        this.userType = userType;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SurveyKey) {
            SurveyKey sk = (SurveyKey) obj;
            return surveyType == sk.surveyType && userType == sk.userType;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return surveyType.hashCode() << (userType != null ? 4 ^ userType.hashCode() : 0); 
    }

    public EnumSurveyType getSurveyType() {
        return surveyType;
    }

    public EnumServiceType getUserType() {
        return userType;
    }

    @Override
    public String toString() {
        return "SurveyKey[" + surveyType + ',' + userType + ']';
    }

	public void setUserType(EnumServiceType userType) {
		this.userType = userType;
	}

}
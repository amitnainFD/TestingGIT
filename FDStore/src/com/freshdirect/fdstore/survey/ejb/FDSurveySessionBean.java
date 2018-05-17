package com.freshdirect.fdstore.survey.ejb;


import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.FinderException;

import org.apache.log4j.Logger;

import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.ejb.FDSessionBeanSupport;
import com.freshdirect.fdstore.survey.EnumSurveyType;
import com.freshdirect.fdstore.survey.FDSurvey;
import com.freshdirect.fdstore.survey.FDSurveyConstants;
import com.freshdirect.fdstore.survey.FDSurveyQuestion;
import com.freshdirect.fdstore.survey.FDSurveyResponse;
import com.freshdirect.fdstore.survey.SurveyKey;
import com.freshdirect.framework.util.log.LoggerFactory;





public class FDSurveySessionBean extends FDSessionBeanSupport {

	private static final Logger LOGGER = LoggerFactory
			.getInstance(FDSurveySessionBean.class);
	
	public FDSurveySessionBean() {
		super();
	}

	/**
	 * Template method that returns the cache key to use for caching resources.
	 * 
	 * @return the bean's home interface name
	 */
	protected String getResourceCacheKey() {
		return "com.freshdirect.fdstore.survey.ejb.FDSurveyHome";
	}


	
	public FDSurvey getSurvey(SurveyKey key) {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDSurveyDAO.loadSurvey(conn, key);
		} catch (SQLException e) {
			LOGGER.warn("SQLException while loading survey : "+key, e);
			throw new EJBException(e);
		} catch (Exception exp) {
			LOGGER.warn("Unknown error while loading survey : "+key, exp);
			throw new EJBException(exp);
		} finally {
		    close(conn);
		}
	}

	public FDSurveyResponse getCustomerProfile(FDIdentity identity, EnumServiceType serviceType) {
		Connection conn = null;
		try {
		
			conn = getConnection();
			List<String> surveys= FDSurveyDAO.getCustomerProfileSurveys(conn, identity, serviceType);
			if(surveys==null||surveys.size()==0 )
				return null;
			else if(surveys.size()==1) {
				return FDSurveyDAO.getResponse(conn, identity, new SurveyKey(surveys.get(0), serviceType));
			} else {
				FDSurveyResponse surveyResponse=FDSurveyDAO.getResponse(conn, identity, new SurveyKey(surveys.get(0), serviceType));
				FDSurveyResponse additionalResponse=null;
				for(int i=1;i<surveys.size();i++) {
					additionalResponse=FDSurveyDAO.getResponse(conn, identity, new SurveyKey(surveys.get(i), serviceType));
					if(additionalResponse!=null) {
						if(surveyResponse==null) {
							surveyResponse=additionalResponse;
						}
						Map additionalAnswers=additionalResponse.getAnswers();
						Iterator it=additionalAnswers.keySet().iterator();
						String key="";
						while(it.hasNext()) {
							key=it.next().toString();
							if(surveyResponse.getAnswer(key)==null) {
								surveyResponse.addAnswer(key, additionalResponse.getAnswer(key));
							}
						}
					}
				}
				return surveyResponse;
			}
		} catch (SQLException e) {
			LOGGER.warn("SQLException while loading survey response : "+FDSurveyConstants.CUSTOMER_PROFILE_SURVEY, e);
			throw new EJBException(e);
		} catch (Exception exp) {
			LOGGER.warn("Unknown error while loading survey response : "+FDSurveyConstants.CUSTOMER_PROFILE_SURVEY, exp);
			throw new EJBException(exp);
		} finally {
                    close(conn);
		}
	}

	public FDSurveyResponse getSurveyResponse(FDIdentity identity, SurveyKey key) {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDSurveyDAO.getResponse(conn, identity, key);
		} catch (SQLException e) {
			LOGGER.warn("SQLException while loading survey response: "+key, e);
			throw new EJBException(e);
		} catch (Exception exp) {
			LOGGER.warn("Unknown error while loading survey response: "+key, exp);
			throw new EJBException(exp);
		} finally {
                    close(conn);
		}
	}

        public void storeSurvey(FDSurveyResponse survey) throws FDResourceException {
            Connection conn = null;
            try {
                    conn = this.getConnection();
                    // customer profile survey is a special case, it contains multiple survey, so we have to separate  
                    if (survey.getKey().getSurveyType() == EnumSurveyType.CUSTOMER_PROFILE_SURVEY) {
                        List<String> surveys = FDSurveyDAO.getCustomerProfileSurveys(conn, survey.getIdentity(), survey.getKey().getUserType());
                        if (surveys.size()>=2) {
                            separateSurveyAnswers(conn, surveys, survey);
                            surveyStored(survey);
                            return;
                        }
                    } 
                    
                    String id = this.getNextId(conn, "CUST");
                    FDSurveyDAO.storeSurvey(conn, id, survey);
                    
                    surveyStored(survey);
            } catch (SQLException se) {
                    throw new FDResourceException(se, "Could not store survey");
            } catch (FinderException fe) {
                    throw new FDResourceException(fe, "Could not update customer profile");
            } catch (RemoteException re) {
                    throw new FDResourceException(re, "Having problem talking to FDCustomerEntityBean");
            } finally {
                close(conn);
            }
    }

        private void separateSurveyAnswers(Connection conn, List<String> surveys, FDSurveyResponse survey) throws SQLException {
            for (String s : surveys) {
                EnumSurveyType surveyType = EnumSurveyType.getEnum(s);
                
                FDSurvey surveyDef = FDSurveyDAO.loadSurvey(conn, new SurveyKey(surveyType, survey.getKey().getUserType()));
                
                FDSurveyResponse newSurvey = new FDSurveyResponse(survey.getIdentity(), surveyDef.getKey());
                for (FDSurveyQuestion e : surveyDef.getQuestions()) {
                    String[] answer = survey.getAnswer(e.getName());
                    if (answer != null && answer.length > 0) {
                        newSurvey.addAnswer(e.getName(), survey.getAnswer(e.getName()));
                    }
                }
                String id = this.getNextId(conn, "CUST");
                FDSurveyDAO.storeSurvey(conn, id, newSurvey);
            }
        }

        private void surveyStored(FDSurveyResponse survey) throws FinderException, RemoteException, FDResourceException {
            if ("Signup_survey".equals(survey.getName())) {
                getFDCustomerManager().setProfileAttribute(survey.getIdentity(), "signup_survey", "YES", null);
            } else if ("Signup_survey_v2".equals(survey.getName())) {
                getFDCustomerManager().setProfileAttribute(survey.getIdentity(), "signup_survey_v2", "FILL", null);
            } else if ("fourth_order_survey".equals(survey.getName())) {
                getFDCustomerManager().setProfileAttribute(survey.getIdentity(), "fourth_order_survey", "FILL", null);
            } else if ("Second Order Survey".equals(survey.getName())) {
                getFDCustomerManager().setProfileAttribute(survey.getIdentity(), "second_order_survey", "FILL", null);
            }
        }

}

package com.freshdirect.fdstore.survey.ejb;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;

import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.survey.EnumFormDisplayType;
import com.freshdirect.fdstore.survey.EnumSurveyType;
import com.freshdirect.fdstore.survey.EnumViewDisplayType;
import com.freshdirect.fdstore.survey.FDSurvey;
import com.freshdirect.fdstore.survey.FDSurveyAnswer;
import com.freshdirect.fdstore.survey.FDSurveyQuestion;
import com.freshdirect.fdstore.survey.FDSurveyResponse;
import com.freshdirect.fdstore.survey.SurveyKey;
import com.freshdirect.framework.util.StringUtil;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * @author gopal
 *
 */
public class FDSurveyDAO {
	private static final Category LOGGER = LoggerFactory.getInstance(FDSurveyDAO.class);
    

	private static final String LOAD_SURVEYS="SELECT NAME, descr FROM cust.SURVEY_DEF";
	
	private static final String LOAD_QUESTIONS_BY_SURVEY="SELECT sq.* FROM cust.SURVEY_DEF sd, cust.SURVEY_SETUP ss,cust.SURVEY_QUESTION sq WHERE sd.NAME=? AND ss.QUESTION=sq.ID ORDER  BY ss.SEQUENCE ASC";
	
	private static final String LOAD_ANSWERS_BY_QUESTION="SELECT sa.* FROM cust.SURVEY_QUESTION sq,cust.SURVEY_SETUP ss, cust.SURVEY_QA qa, cust.SURVEY_ANSWER sa WHERE sq.NAME=? AND ss.QUESTION=sq.ID AND ss.QUESTION=qa.QUESTION AND qa.ANSWER=sa.ID";
	
	private static final String LOAD_ACTIVE_SURVEY="SELECT sd.NAME,sd.IS_ORDER_SURVEY,sd.MIN_COVERAGE as Min_Coverage, "+
	" sq.NAME AS Question_Name, sq.DESCRIPTION AS Question_Descr,sq.SHORT_DESCR as Question_Short_Descr, sq.IS_REQUIRED as Required, sq.IS_MULTISELECT as Multiselect, sq.IS_OPENENDED as OpenEnded, sq.IS_RATING as Rating, sq.SHOW_OPTIONAL_TEXT as ShowOptional,sq.IS_PULLDOWN as Pulldown, sq.FORM_DISPLAY_TYPE as FormDisplayType,sq.VIEW_DISPLAY_TYPE as ViewDisplayType, "+
	" sa.NAME AS Answer_Name, sa.DESCRIPTION AS Answer_Description, sqa.ANSWER_GROUP as Answer_Group FROM "+
	" cust.SURVEY_DEF sd, cust.SURVEY_QUESTION sq, cust.SURVEY_ANSWER sa, cust.SURVEY_SETUP ss, cust.SURVEY_QA sqa WHERE "+
    " sd.NAME=? AND sd.SERVICE_TYPE = ? AND sd.ID=ss.SURVEY AND ss.QUESTION=sq.ID AND ss.ACTIVE='Y' AND sq.ID=sqa.QUESTION AND sqa.ANSWER=sa.ID ORDER BY sd.NAME, ss.SEQUENCE, sqa.SEQUENCE ";
	
    public static FDSurvey loadSurvey(Connection conn, SurveyKey key) throws SQLException {
        FDSurvey survey = loadSurvey(conn, key.getSurveyType(), key.getUserType().name(), key);
        if (survey == null) {
            survey = loadSurvey(conn, key.getSurveyType(), "ANY", key);
        }
        return survey;
    }	

    private static FDSurvey loadSurvey(Connection conn, EnumSurveyType type, String serviceType, SurveyKey key) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        FDSurvey survey = null;
        try {
            ps = conn.prepareStatement(LOAD_ACTIVE_SURVEY);
            ps.setString(1, type.getLabel());
            ps.setString(2, serviceType);
            rs = ps.executeQuery();
            survey = getSurvey(rs, key);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }

        return survey;
    }	
	
	/*private static final String GET_SURVEY_RESPONSE="SELECT sd.SURVEY_ID, sd.QUESTION, sd.ANSWER FROM cust.SURVEYDATA sd "+
	" WHERE survey_id IN "+
	" (SELECT ID FROM cust.SURVEY WHERE survey_name='Customer Profile Survey' AND customer_id=(SELECT ID FROM cust.FDCUSTOMER WHERE erp_customer_id=?) "+
	" AND create_date=(SELECT MAX(create_date) FROM cust.SURVEY WHERE survey_name=? AND customer_id=(SELECT ID FROM cust.FDCUSTOMER WHERE erp_customer_id=?))) ";
	*/
	
	
	/*private static final String GET_SURVEY_RESPONSE="SELECT sd.SURVEY_ID, sd.QUESTION, sd.ANSWER,t.SEQUENCE FROM cust.SURVEYDATA sd, "+
	" (SELECT sq.NAME AS question, SEQUENCE FROM cust.SURVEY_SETUP ss, cust.SURVEY_QUESTION sq WHERE SURVEY=(SELECT ID FROM cust.SURVEY_DEF WHERE NAME=?) AND ss.QUESTION=sq.ID) t "+
	" WHERE sd.survey_id IN "+
	" (SELECT ID FROM cust.SURVEY WHERE survey_name=? AND customer_id=(SELECT ID FROM cust.FDCUSTOMER WHERE erp_customer_id=?) "+
	" AND create_date=(SELECT MAX(create_date) FROM cust.SURVEY WHERE survey_name=? AND customer_id=(SELECT ID FROM cust.FDCUSTOMER WHERE erp_customer_id=?))) "+
	" AND sd.QUESTION=t.question ORDER BY t.SEQUENCE ";*/
	
	private static final String GET_SURVEY_RESPONSE=" SELECT sd.SURVEY_ID, sd.QUESTION, sd.ANSWER,QuestionNumber,AnswerNumber FROM cust.SURVEYDATA sd, "+ 
	 " (SELECT ss.SEQUENCE AS QuestionNumber, sq.NAME AS question,sqa.SEQUENCE AS AnswerNumber,sa.name as AnswerName  FROM "+
     " cust.SURVEY_SETUP ss, cust.SURVEY_QUESTION sq,cust.SURVEY_ANSWER sa,cust.SURVEY_QA sqa WHERE "+ 
     " ss.SURVEY=(SELECT ID FROM cust.SURVEY_DEF WHERE NAME=? AND SERVICE_TYPE = ?) AND "+
     " ss.QUESTION=sq.ID AND ss.QUESTION=sqa.QUESTION AND sqa.ANSWER=sa.ID) t "+ 
	 " WHERE sd.survey_id IN (SELECT ID FROM cust.SURVEY WHERE survey_name=? AND "+ 
	 " customer_id=(SELECT ID FROM cust.FDCUSTOMER WHERE erp_customer_id=?) "+ 
	 " AND create_date=(SELECT MAX(create_date) FROM cust.SURVEY WHERE survey_name=? AND service_type = ? AND customer_id=(SELECT ID FROM cust.FDCUSTOMER WHERE erp_customer_id=?))) "+ 
	 " AND sd.QUESTION=t.question(+) AND sd.ANSWER=t.AnswerName(+)	 ORDER BY QuestionNumber,AnswerNumber";
	public static FDSurveyResponse getResponse(Connection conn,FDIdentity identity, SurveyKey key) throws SQLException {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		FDSurveyResponse surveyResponse =null;
		try {			
			ps = conn.prepareStatement(GET_SURVEY_RESPONSE);
			ps.setString(1, key.getSurveyType().getLabel());
			ps.setString(2, key.getUserType().name());
			ps.setString(3, key.getSurveyType().getLabel());
			ps.setString(4, identity.getErpCustomerPK());
			ps.setString(5, key.getSurveyType().getLabel());
                        ps.setString(6, key.getUserType().name());
			ps.setString(7, identity.getErpCustomerPK());
			rs = ps.executeQuery();
			surveyResponse = getSurveyResponse(identity,key,rs);						
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		
		return surveyResponse;
	}

	private static final String GET_CUSTOMER_PROFILE_SURVEYS="SELECT s.ID,s.SURVEY_NAME,s.CREATE_DATE FROM cust.SURVEY s, "+
	"(SELECT ID,NAME FROM cust.SURVEY_DEF WHERE is_customer_profile_survey='Y' AND service_type = ?) cps "+
	" WHERE s.CUSTOMER_ID=(SELECT ID FROM cust.FDCUSTOMER WHERE erp_customer_id=?) "+
	" AND s.SURVEY_NAME=cps.NAME AND s.CREATE_DATE=(SELECT MAX(create_date) FROM cust.SURVEY WHERE CUSTOMER_ID=s.customer_id AND survey_name=s.survey_name)"+
	" ORDER BY s.create_date DESC ";
    
        public static List<String> getCustomerProfileSurveys(Connection conn, FDIdentity identity, EnumServiceType serviceType) throws SQLException {
    
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = conn.prepareStatement(GET_CUSTOMER_PROFILE_SURVEYS);
                ps.setString(1, serviceType.name());
                ps.setString(2, identity.getErpCustomerPK());
                
                rs = ps.executeQuery();
                List<String> surveys = new ArrayList<String>();
                while (rs.next()) {
                    surveys.add(rs.getString("SURVEY_NAME"));
                }
                return surveys;
            } finally {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
            }
        }

	private static FDSurvey getSurvey(ResultSet rs, SurveyKey key) throws SQLException {
		
		FDSurvey survey=null;
		FDSurveyQuestion activeQuestion=null;
		String oldQuestion="";
		String question="";
		while(rs.next()) {
			if (survey==null) {
				
				survey=new FDSurvey(key,getBoolean(rs.getString(2)), rs.getInt("Min_Coverage"));
				//System.out.println("Min Coverage :"+rs.getInt("Min_Coverage"));
			}
			question=rs.getString("Question_Name");
			if(!"".equals(oldQuestion)&& !question.equals(oldQuestion)) {
				//survey.addQuestion(activeQuestion);
				activeQuestion=null;
			}
			if(activeQuestion==null) {
				//activeQuestion=new FDSurveyQuestion(question, rs.getString("Question_Descr"), getBoolean(rs.getString("Required")), getBoolean(rs.getString("Multiselect")), getBoolean(rs.getString("OpenEnded")), getBoolean(rs.getString("Rating")),getBoolean(rs.getString("ShowOptional")), false);
				EnumFormDisplayType formDisplayType = EnumFormDisplayType.getEnum(rs.getString("FormDisplayType"));
				EnumViewDisplayType viewDisplayType = EnumViewDisplayType.getEnum(rs.getString("ViewDisplayType"));
				activeQuestion=new FDSurveyQuestion(question, rs.getString("Question_Descr"),rs.getString("Question_Short_Descr"), getBoolean(rs.getString("Required")), getBoolean(rs.getString("Multiselect")), getBoolean(rs.getString("OpenEnded")), getBoolean(rs.getString("Rating")),getBoolean(rs.getString("Pulldown")), false,formDisplayType,viewDisplayType);
				activeQuestion.addAnswer(new FDSurveyAnswer(rs.getString("Answer_Name"), rs.getString("Answer_Description"),rs.getString("Answer_Group")));
				survey.addQuestion(activeQuestion);
				
			} else {
				activeQuestion.addAnswer(new FDSurveyAnswer(rs.getString("Answer_Name"), rs.getString("Answer_Description"),rs.getString("Answer_Group")));
			}
			oldQuestion=question;
		}
		return survey;		
	}
	
private static FDSurveyResponse getSurveyResponse(FDIdentity identity,SurveyKey survey,ResultSet rs) throws SQLException {
		
		FDSurveyResponse surveyResponse=null;
		Map qa=new HashMap();
		String question="";
		List answers=null;
		while(rs.next()) {
			if (surveyResponse==null) {
				surveyResponse=new FDSurveyResponse(identity,survey);
			}
			question=rs.getString(2);
			if(qa.containsKey(question)) {
				answers=(List)qa.get(question);
			} else {
				answers=new ArrayList();
			}
			answers.add(rs.getString(3));
			qa.put(question, answers);

		}
		Iterator it=qa.keySet().iterator();
		while(it.hasNext()) {
			question=it.next().toString();
			String[] _answers=getAnswers((List)qa.get(question));
			surveyResponse.addAnswer(question, _answers);
		}
		return surveyResponse;		
	}

	private static String[] getAnswers(List _answers) {
		String[] answers=new String[_answers.size()];
		for(int i=0;i<_answers.size();i++) {
			answers[i]=(String)_answers.get(i);
		}
		
		return answers;
}


	private static boolean getBoolean(String data) {
		boolean value=false;
		if(!StringUtil.isEmpty(data) && "Y".equalsIgnoreCase(data)) {
			value=true;
		}
		return value;
	}
	


        public static void storeSurvey(Connection conn, String id, FDSurveyResponse survey) throws SQLException {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO CUST.SURVEY(ID,CUSTOMER_ID,SURVEY_NAME,SALE_ID,CREATE_DATE, SERVICE_TYPE) VALUES(?, ?, ?, ?, SYSDATE, ?)");
                ps.setString(1, id);
                
                if (survey.getIdentity() == null) {
                        ps.setNull(2, Types.VARCHAR);
                } else {
                        ps.setString(2, survey.getIdentity().getFDCustomerPK());
                }
                
                ps.setString(3, survey.getName());
                
                if (survey.getSalePk() == null) {
                        ps.setNull(4, Types.VARCHAR);
                } else {
                        ps.setString(4, survey.getSalePk().getId());
                }
                ps.setString(5, survey.getKey().getUserType().name());
                
                ps.executeUpdate();
                ps.close();

                ps = conn.prepareStatement("INSERT INTO CUST.SURVEYDATA (SURVEY_ID, QUESTION, ANSWER) VALUES (?, ?, ?)");
                for (Iterator i = survey.getAnswers().entrySet().iterator(); i.hasNext();) {
                        Map.Entry entry = (Map.Entry) i.next();
                        String question = (String) entry.getKey();
                        String[] values = (String[]) entry.getValue();

                        for (int j = 0; j < values.length; j++) {
                                String answer = values[j];
                                ps.setString(1, id);
                                ps.setString(2, question);
                                ps.setString(3, answer);
                                ps.addBatch();
                        }
                }
                ps.executeBatch();
                ps.close();
        }

  
}




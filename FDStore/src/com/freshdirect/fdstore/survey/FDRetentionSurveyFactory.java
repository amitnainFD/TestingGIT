package com.freshdirect.fdstore.survey;

import java.util.HashMap;
import java.util.Map;

import com.freshdirect.crm.CrmCaseSubject;

public class FDRetentionSurveyFactory {
	
	private final static FDRetentionSurveyFactory INSTANCE = new FDRetentionSurveyFactory();

	private final Map surveys = new HashMap();
	
	public final static String SURVEY_SUCCESS = "SUCCESS";
	
	public final static String SURVEY_FAILURE = "FAILURE";
	
	private final static String SUM_2TO5G2 = "Please call this customer to find out why they were unhappy with 1st order.  (customer respondent 'unhappy' in survey). Remind them they have 25% off their 2nd order. Use code FRESH25";
	private final static String SUM_2TO5G3 = "Please call this customer to find out why they were unhappy with 1st order (customer respondent 'unhappy' in survey). Please encourage them to try FreshDirect again."; 
	private final static String SUM_2TO5G4 = "Please call this customer to find out why they were unhappy with 1st order (customer respondent 'unhappy' in survey). Please encourage them to try FreshDirect again."; 
		
	private final static String NOT_2TO5G2 = "This customer is entitled to 25% off their 2nd order.  Remind them to use the code FRESH25 on their 2nd order as it will expire. Code is only good for three weeks after 1st order delivery date.";
	private final static String NOT_2TO5G3 = "This customer did NOT receive a welcome brochure but will be receiving an offer via email for their 2nd order.";
	private final static String NOT_2TO5G4 = "This customer is in our control group for the 2-5 retention plan and will NOT be receiving any additional offers. Please encourage them to try FreshDirect again.";
	
	private FDRetentionSurveyFactory() {
	}

	public static FDRetentionSurveyFactory getInstance() {
		return INSTANCE;
	}

	public FDRetentionSurvey getSurvey(String surveyName) {
		return (FDRetentionSurvey) this.surveys.get(surveyName);
	}

	private void addSurvey(FDRetentionSurvey survey) {
		this.surveys.put(survey.getName(), survey);
	}

	static {
				
		//Yes, No Response for Survey
		Map responseProfileMap15Survey = new HashMap();
		responseProfileMap15Survey.put("0","no");
		responseProfileMap15Survey.put("1","yes");
		
		//Landing Pages Map
		Map mediaLinkMap15Survey = new HashMap();
		mediaLinkMap15Survey.put("1_SUCCESS","/media/editorial/site_pages/1_5_plan/survey_result_offer.html");
		mediaLinkMap15Survey.put("1_SUCCESS_2to5G4","/media/editorial/site_pages/1_5_plan/survey_result.html");
		mediaLinkMap15Survey.put("1_FAILURE","/media/editorial/site_pages/1_5_plan/survey_repeat.html");
		
		mediaLinkMap15Survey.put("0_SUCCESS","http://linescale.com/survey/freshdirect6346");//Test Link: http://linescale.com/perl/ls/run/s014-6346.pl?prod=testdata 
		mediaLinkMap15Survey.put("0_FAILURE","/media/editorial/site_pages/1_5_plan/survey_repeat.html");
		
		// Case Creation Attributes
		Map caseInfoMap15Survey = new HashMap();				
		caseInfoMap15Survey.put("0_2to5G2",new String[]{CrmCaseSubject.CODE_15RETENTION,SUM_2TO5G2,NOT_2TO5G2});
		caseInfoMap15Survey.put("0_2to5G3",new String[]{CrmCaseSubject.CODE_15RETENTION,SUM_2TO5G3,NOT_2TO5G3});
		caseInfoMap15Survey.put("0_2to5G4",new String[]{CrmCaseSubject.CODE_15RETENTION,SUM_2TO5G4,NOT_2TO5G4});
		
		
		FDRetentionSurveyFactory.getInstance().addSurvey(new FDRetentionSurvey
													("RETSUR01","RetentionProgram", "RetentionAux",
																responseProfileMap15Survey,mediaLinkMap15Survey, caseInfoMap15Survey));
		
	}

}

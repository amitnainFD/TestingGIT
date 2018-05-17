package com.freshdirect.fdstore.survey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * @author ekracoff
 */
public class FDSurveyQuestion implements java.io.Serializable {
	private final String name;
	private final String description;
	private final String shortDescr;
	private List<FDSurveyAnswer> answers = new ArrayList<FDSurveyAnswer>();
	private final boolean multiselect;
	private final boolean required;
	private final boolean openEnded;
	private final boolean rating;
	private final boolean subQuestion;
	private boolean showOptionalText;
	private final boolean pulldown;
	private EnumFormDisplayType formDisplayType;
	private EnumViewDisplayType viewDisplayType;
	
	public FDSurveyQuestion(String name, String description, boolean required, boolean multiselect, boolean openEnded, boolean rating, boolean subQuestion){
		this.name = name;
		this.description = description;
		this.shortDescr=description;
		this.multiselect = multiselect;
		this.required = required;
		this.openEnded = openEnded;
		this.rating = rating;
		this.subQuestion = subQuestion;
		this.showOptionalText = !required;
		this.pulldown=false;
		this.formDisplayType=EnumFormDisplayType.SINGLE_ANS_PER_ROW;
		this.viewDisplayType=EnumViewDisplayType.SINGLE_ANS_PER_ROW;
	}
	
	public FDSurveyQuestion(String name, String description, boolean required, boolean multiselect, boolean openEnded, boolean rating){
		this(name, description, required, multiselect, openEnded, rating, false);
	}
	
	public FDSurveyQuestion(String name, String description, boolean required, boolean multiselect, boolean openEnded){
		this(name, description, required, multiselect, openEnded, false, false);
	}
	
	public FDSurveyQuestion(String name, String description, boolean required, boolean multiselect){
		this(name, description, required, multiselect, false, false, false);
	}

	public FDSurveyQuestion(String name, String description,String shortDescr, boolean required, boolean multiselect, boolean openEnded, boolean rating,boolean pulldown, boolean subQuestion,EnumFormDisplayType formDisplayType,EnumViewDisplayType viewDisplayType) {
		this.name = name;
		this.description = description;
		this.shortDescr=shortDescr;
		this.multiselect = multiselect;
		this.required = required;
		this.openEnded = openEnded;
		this.rating = rating;
		this.subQuestion = subQuestion;
		this.showOptionalText = !required;;
		this.pulldown=pulldown;
		this.formDisplayType=formDisplayType;
		this.viewDisplayType=viewDisplayType;
	}

	public void addAnswer(FDSurveyAnswer answer){
		answers.add(answer);
	}

	public List<FDSurveyAnswer> getAnswers() {
		return Collections.unmodifiableList(answers);
	}
	
	public void setAnswers(List<FDSurveyAnswer> answers){
		this.answers = answers;
	}
	
	public void setShowOptionalText(boolean showOptionalText) {
		this.showOptionalText = showOptionalText;
	}
	
	public boolean isShowOptionalText() {
		return showOptionalText;
	}
	
//	public FDSurveyAnswer createAnswer(String name, String description){
//		FDSurveyAnswer answer = new FDSurveyAnswer(name, description);
//		this.addAnswer(answer);
//		return answer;
//	}

	public String getDescription() {
		return description;
	}

	public boolean isMultiselect() {
		return multiselect;
	}

	public boolean isRating() {
		return rating;
	}
	
	public boolean isSubQuestion() {
		return subQuestion;
	}
	
	public String getName() {
		return name;
	}

	public boolean isRequired() {
		return required;
	}
	
	public boolean isOpenEnded(){
		return openEnded;
	}
	
	public void sortAnswers(Comparator<FDSurveyAnswer> c){
		Collections.sort(answers, c);
	}
	
	public void sortAnswers(){
		this.sortAnswers(ANSWER_COMAPARATOR);
	}
	
        public boolean isValidAnswer(String[] answer) {
    
            if (this.required && answer.length == 0) {
                return false;
            }
            if (!this.multiselect && answer.length > 1) {
                return false;
            }
    
            // check to see if answer is an expected answer
            if (!this.openEnded) {
                HashSet<String> names = new HashSet<String>();
                for (FDSurveyAnswer i : this.answers) {
                    names.add(i.getName());
                }
    
                for (int i = 0; i < answer.length; i++) {
                    if (!names.remove(answer[i])) {
                        return false;
                    }
                }
            }
    
            return true;
    
        }
	
	public final static Comparator<FDSurveyAnswer> ANSWER_COMAPARATOR = new Comparator<FDSurveyAnswer>() {
		public int compare(FDSurveyAnswer obj1, FDSurveyAnswer obj2) {
			return obj1.getDescription().compareTo(obj2.getDescription());
		}
	};

	public boolean isPulldown() {
		return pulldown;
	}

	public EnumFormDisplayType getFormDisplayType() {
		return formDisplayType;
	}
	
	public EnumViewDisplayType getViewDisplayType() {
		return viewDisplayType;
	}
	
        public List<String> getAnswerGroups() {
            List<String> answerGroup = new ArrayList<String>();
            List<FDSurveyAnswer> answers = getAnswers();
            for (FDSurveyAnswer answer : answers) {
                if (answer.getGroup() != null && !"".equals(answer.getGroup()) && !answerGroup.contains(answer.getGroup())) {
                    answerGroup.add(answer.getGroup());
                }
            }
            return answerGroup;
        }
	
        public List<FDSurveyAnswer> getAnswersByGroup(String answerGroup) {
            if (answerGroup == null || "".equals(answerGroup))
                return new ArrayList<FDSurveyAnswer> ();
            List<FDSurveyAnswer> answers = getAnswers();
            List<FDSurveyAnswer> filteredList = new ArrayList<FDSurveyAnswer>(2);
            for (FDSurveyAnswer answer : answers) {
                if (answerGroup.equals(answer.getGroup())) {
                    filteredList.add(answer);
                }
            }
            return filteredList;
        }
	
	public String getShortDescr() {
		return (shortDescr==null ||"".equals(shortDescr))? description:shortDescr;
	}
	
    String toSqlInserts() {
        String surveyQuestion = "insert into CUST.SURVEY_QUESTION "
                + "(ID,NAME,DESCRIPTION,IS_MULTISELECT,IS_OPENENDED,IS_RATING,SHOW_OPTIONAL_TEXT,IS_REQUIRED,IS_PULLDOWN,SHORT_DESCR,FORM_DISPLAY_TYPE,VIEW_DISPLAY_TYPE,DATE_MODIFIED) " 
                + "values (" 
                + toMaxSelect("SURVEY_QUESTION")
                + ','
                + escape(name)
                + ','
                + escape(description)
                + ','
                + bool(multiselect)
                + ','
                + bool(openEnded)
                + ','
                + bool(rating)
                + ','
                + bool(showOptionalText)
                + ','
                + bool(required)
                + ','
                + bool(pulldown)
                + ','
                + escape(shortDescr)
                + ','
                + (formDisplayType != null ? formDisplayType.getName() : "null")
                + ','
                + (viewDisplayType != null ? viewDisplayType.getName() : "null")
                + ",SYSDATE);";

        String sqId = toSelectSqlId();

        StringBuilder s = new StringBuilder(surveyQuestion);

        int sequence = 1;
        for (FDSurveyAnswer answer : answers) {

            String surveyAnswer = "insert into CUST.SURVEY_ANSWER (ID,NAME,DESCRIPTION,DATE_MODIFIED) values ("+toMaxSelect("SURVEY_ANSWER")+"," + escape(answer.getName()) + ','
                    + escape(answer.getDescription()) + ",SYSDATE);";
            String saId = "(select id from CUST.SURVEY_ANSWER where NAME = " + escape(answer.getName()) + ")";
            String surveyQA = "insert into CUST.SURVEY_QA (ID,QUESTION, ANSWER, SEQUENCE, ANSWER_GROUP, DATE_MODIFIED) values (" + toMaxSelect("SURVEY_QA")+','+sqId + ',' + saId + ','
                    + sequence + ',' + escape(answer.getGroup()) + ", SYSDATE);";

            s.append("\n").append(surveyAnswer).append("\n").append(surveyQA);
            sequence ++;
        }
        return s.toString();
    }

    String toSelectSqlId() {
        return "(select id from CUST.SURVEY_QUESTION q where q.name = " + escape(name) + ")";
    }
    
    String toMaxSelect(String name) {
        return "(select max(to_number(id))+1 from CUST."+name+")";
    }

    private static String escape(String name2) {
        return "'" + name2 + '\'';
    }

    private static String bool(boolean b) {
        return b ? "'Y'" : "'N'";
    }	

    public static FDSurveyQuestion radio(String name, String description) {
        return new FDSurveyQuestion(name, description, "", false, false, false,false,false, false,EnumFormDisplayType.SINGLE_ANS_PER_ROW, null);
    }
    
    public static FDSurveyQuestion pulldown(String name, String description) {
        return new FDSurveyQuestion(name, description, "", false, false, false,false,true, false,EnumFormDisplayType.SINGLE_ANS_PER_ROW, null);
    }

    public static FDSurveyQuestion multi(String name, String description, EnumFormDisplayType type) {
        return new FDSurveyQuestion(name, description, "", false, true, false, false, false, false, type, null);
    }



    
}

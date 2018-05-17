package com.freshdirect.fdstore.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.framework.core.PrimaryKey;

public class FDSurveyResponse implements Serializable {

	private final FDIdentity identity;
	private final SurveyKey key;
	private final Map answers = new HashMap();
	private final PrimaryKey salePk;

	public FDSurveyResponse(FDIdentity identity, SurveyKey key) {
		this(identity, key, null);
	}

	public FDSurveyResponse(FDIdentity identity, SurveyKey key, PrimaryKey salePk) {
		this.identity = identity;
		this.key = key;
		this.salePk = salePk;
	}

	public FDIdentity getIdentity() {
		return identity;
	}

        public SurveyKey getKey() {
            return key;
    }
	
	public String getName() {
		return key.getSurveyType().getLabel();
	}

	public PrimaryKey getSalePk() {
		return salePk;
	}

	public void addAnswer(String question, String answer) {
		this.addAnswer(question, new String[] { answer });
	}

	public void addAnswer(String question, String[] answer) {
		this.answers.put(question, answer);
	}
	
	public void addAnswerEx(String question,String[] answer) {
		
		if(this.answers.containsKey(question)) {
			List temp=asList((String[])answers.get(question));
			for(int i=0;i<answer.length;i++)
				temp.add(answer[i]);
			this.answers.put(question, asStringArray(temp));
		}
		else {
			addAnswer(question,answer);
		}
	}
	
	private static List asList(String[] data) {
		List _data=new ArrayList(data.length);
		for(int i=0;i<data.length;i++)
			_data.add(data[i]);
		
		return _data;
	}
	
	private static String[] asStringArray(List data) {
		String[] _data=new String[data.size()];
		for(int i=0;i<data.size();i++)
			_data[i]=data.get(i).toString();
		return _data;
	}

	/** @param map Map of String -> String[] */
	public void setAnswers(Map map) {
		this.answers.clear();
		this.answers.putAll(map);
	}

	/** @return map of String -> String[] */
	public Map<String, String[]> getAnswers() {
		return Collections.unmodifiableMap(answers);
	}
	
	public String[] getAnswer(String question) {
		
		if(this.answers.containsKey(question)) {
			return (String[]) this.answers.get(question);
			
		}
		return null;
	}
	
	public List getAnswerAsList(String question) {
		
		String[] answer=getAnswer(question);
		if(answer==null)
			return new ArrayList();
		else {
			List answerList=new ArrayList(answer.length);
			for(int i=0;i<answer.length;i++) {
				if(answer[i]!=null && !"".equals(answer[i]))
				answerList.add(answer[i]);
			}
		    return answerList;
		}
		
	}

}

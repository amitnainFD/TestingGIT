/*Created on Nov 20, 2003*/
package com.freshdirect.fdstore.survey;


public class FDSurveyAnswer implements java.io.Serializable {
	private final String name;
	private final String description;
	private final String group;
	
	
	public FDSurveyAnswer(String name, String description){
		this.name = name;
		this.description = description;
		group="";
		
	}
	public FDSurveyAnswer(String name, String description, String group){
		this.name = name;
		this.description = description;
		this.group=group;
		
	}
	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public String getGroup() {
		return group;
	}
}
package com.freshdirect.fdstore.survey;

import java.util.Map;

public class FDRetentionSurvey {
	
	private final String name;
	
	private final String checkProfileName;
	
	private final String profileName;
		
	private final Map responseProfileMap;
	
	private final Map mediaLink;
	
	private final Map caseInfo;
	
	
	public FDRetentionSurvey(String name,String checkProfileName, String profileName
				, Map responseProfileMap, Map mediaLink, Map caseInfo) {
		
		this.name = name;
		this.checkProfileName = checkProfileName;
		this.profileName = profileName;
		this.responseProfileMap = responseProfileMap;
		this.mediaLink = mediaLink;
		this.caseInfo = caseInfo;
	}


	public Map getMediaLink() {
		return mediaLink;
	}

	public String getName() {
		return name;
	}

	public String getProfileName() {
		return profileName;
	}

	public Map getResponseProfileMap() {
		return responseProfileMap;
	}
	
	public boolean isValidResponse(String response) {
		
		return responseProfileMap != null && responseProfileMap.containsKey(response);
	}
	
	public String getProfileValueForResponse(String response) {
		
		return responseProfileMap != null ? (String)responseProfileMap.get(response) : null;
	}
	
	public String getMediaLink(String responseKey) {
				
		return mediaLink != null ? (String)mediaLink.get(responseKey) : null;
	}
	
	public String[] getCaseInfo(String responseKey) {
		
		return mediaLink != null ? (String[])caseInfo.get(responseKey) : null;
	}


	public String getCheckProfileName() {
		return checkProfileName;
	}

}

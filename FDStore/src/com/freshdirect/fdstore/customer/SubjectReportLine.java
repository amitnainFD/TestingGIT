package com.freshdirect.fdstore.customer;

import java.io.Serializable;

public class SubjectReportLine implements Serializable {

	private String queueName;
	private String subject;
	private int caseCount;

	public SubjectReportLine (String queueName, String subject, int caseCount) {
		this.queueName = queueName;
		this.subject = subject;
		this.caseCount = caseCount;
	}

	public int getCaseCount() {
		return caseCount;
	}

	public String getQueueName() {
		return queueName;
	}

	public String getSubject() {
		return subject;
	}

}

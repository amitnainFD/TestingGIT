package com.freshdirect.fdstore.services.tax.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommonResponse {
	public static class Message {
		
		@JsonProperty("Summary")
		public String summary;
		
		@JsonProperty("Details")
		public String details;
		
		@JsonProperty("RefersTo")
		public String refersTo;
		
		@JsonProperty("Severity")
		public SeverityLevel severity;
		
		@JsonProperty("Source")
		public String source;

		public String getSummary() {
			return summary;
		}

		public String getDetails() {
			return details;
		}

		public String getRefersTo() {
			return refersTo;
		}

		public SeverityLevel getSeverity() {
			return severity;
		}

		public String getSource() {
			return source;
		}

		public void setSummary(String summary) {
			this.summary = summary;
		}

		public void setDetails(String details) {
			this.details = details;
		}

		public void setRefersTo(String refersTo) {
			this.refersTo = refersTo;
		}

		public void setSeverity(SeverityLevel severity) {
			this.severity = severity;
		}

		public void setSource(String source) {
			this.source = source;
		}

	}

	public enum SeverityLevel {
		Success, Warning, Error, Exception;
	}
}
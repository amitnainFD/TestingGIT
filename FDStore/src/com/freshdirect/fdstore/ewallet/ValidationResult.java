package com.freshdirect.fdstore.ewallet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ValidationResult implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5493721707011107135L;
	private String fdform;
	private List<ValidationError> errors = new ArrayList<ValidationError>();

	public String getFdform() {
		return fdform;
	}

	public void setFdform(String fdform) {
		this.fdform = fdform;
	}

	public List<ValidationError> getErrors() {
		return errors;
	}

	public void setErrors(List<ValidationError> errors) {
		this.errors = errors;
	}

}

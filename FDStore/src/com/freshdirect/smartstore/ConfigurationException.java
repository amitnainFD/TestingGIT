package com.freshdirect.smartstore;

public class ConfigurationException extends RuntimeException {
	private static final long serialVersionUID = -40625046299344024L;

	private String parameter;
	
	private EnumConfigurationState state;
	
	public ConfigurationException(String parameter, EnumConfigurationState state) {
		super("wrong parameter '"	+ parameter + "' (" + state	+ ")");
		this.parameter = parameter;
		this.state = state;
	}

	public String getParameter() {
		return parameter;
	}

	public EnumConfigurationState getState() {
		return state;
	}
}

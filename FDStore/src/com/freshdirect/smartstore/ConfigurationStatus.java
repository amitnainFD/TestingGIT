package com.freshdirect.smartstore;

import java.io.Serializable;

/**
 * 
 * @author csongor
 */
public class ConfigurationStatus implements Comparable<ConfigurationStatus>, Serializable {
	
	private static final long serialVersionUID = 5347738460088039431L;

	String name;
	
	String loadedValue;
	
	String appliedValue;
	
	EnumConfigurationState state;
	
	String message;
	
	public ConfigurationStatus(String name, String value,
			EnumConfigurationState state) {
		this(name, value, state, null);
	}

	public ConfigurationStatus(String name, String value,
			EnumConfigurationState state, String message) {
		this(name, value, value, state, message);
	}

	public ConfigurationStatus(String name, String loadedValue,
			String appliedValue, EnumConfigurationState state) {
		this(name, loadedValue, appliedValue, state, null);
	}

	public ConfigurationStatus(String name, String loadedValue,
			String appliedValue, EnumConfigurationState state, String message) {
		super();
		this.name = name;
		this.loadedValue = loadedValue;
		this.appliedValue = appliedValue;
		this.state = state;
		this.message = message;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	public int compareTo(ConfigurationStatus status) {
		return this.name.compareTo(status.name);
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigurationStatus other = (ConfigurationStatus) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public String getLoadedValue() {
		return loadedValue;
	}

	public String getAppliedValue() {
		return appliedValue;
	}

	public EnumConfigurationState getState() {
		return state;
	}

	public String getMessage() {
		return message;
	}
	
	public String getError() {
		if (message == null || message.startsWith("WARNING: "))
			return null;
		
		return message;
	}
	
	public ConfigurationStatus setError(String error) {
		this.message = error;
		return this;
	}
	
	public String getWarning() {
		if (message != null && message.startsWith("WARNING: "))
			return message.substring(9);
		
		return null;
	}

	public ConfigurationStatus setWarning(String warning) {
		this.message = "WARNING: " + warning;
		return this;
	}
	
	public boolean isValueSame() {
		if (appliedValue == null) {
			return loadedValue == null;
		}
		if (loadedValue == null)
			return true; // FIXME ? appliedValue not null and loadedValue is null - why we return true for isValueSame when they are definitely not equal? (seems like it should be false...)
		
		return appliedValue.equals(loadedValue);
	}

	public boolean isConfigured() {
		return state.isConfigured();
	}

	public boolean isDefault() {
		return state.isDefault();
	}

	public boolean isOverridden() {
		return state.isOverridden();
	}

	public boolean isValid() {
		return state.isValid();
	}
}
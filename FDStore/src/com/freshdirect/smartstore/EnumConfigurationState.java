package com.freshdirect.smartstore;

import org.apache.commons.lang.enums.Enum;

/**
 * Represents the state of a recomennder's configuration value
 *  
 * @author csongor
 */
public class EnumConfigurationState extends Enum {
	private static final long serialVersionUID = 1200755760532000751L;
	
	public final static EnumConfigurationState CONFIGURED_OK =
			new EnumConfigurationState("CONFIGURED_OK");
	public final static EnumConfigurationState CONFIGURED_DEFAULT =
			new EnumConfigurationState("CONFIGURED_DEFAULT");
	public final static EnumConfigurationState CONFIGURED_OVERRIDDEN =
			new EnumConfigurationState("CONFIGURED_OVERRIDDEN");
	public final static EnumConfigurationState CONFIGURED_WRONG =
			new EnumConfigurationState("CONFIGURED_WRONG", false);
	public final static EnumConfigurationState CONFIGURED_WRONG_DEFAULT =
		new EnumConfigurationState("CONFIGURED_WRONG_DEFAULT");
	public final static EnumConfigurationState CONFIGURED_UNUSED =
			new EnumConfigurationState("CONFIGURED_UNUSED");
	public final static EnumConfigurationState UNCONFIGURED_OK =
		new EnumConfigurationState("UNCONFIGURED_OK", true, false);
	public final static EnumConfigurationState UNCONFIGURED_DEFAULT =
			new EnumConfigurationState("UNCONFIGURED_DEFAULT", true, false);
	public final static EnumConfigurationState UNCONFIGURED_OVERRIDDEN =
			new EnumConfigurationState("UNCONFIGURED_OVERRIDDEN", true, false);
	public final static EnumConfigurationState UNCONFIGURED_WRONG =
			new EnumConfigurationState("UNCONFIGURED_WRONG", false, false);
	
	private boolean valid;
	
	private boolean configured;
	
	protected EnumConfigurationState(String name) {
		super(name);
		valid = true;
		configured = true;
	}

	public EnumConfigurationState(String name, boolean valid) {
		super(name);
		this.valid = valid;
	}

	public EnumConfigurationState(String name, boolean valid,
			boolean configured) {
		super(name);
		this.valid = valid;
		this.configured = configured;
	}

	public boolean isValid() {
		return valid;
	}

	public boolean isConfigured() {
		return configured;
	}
	
	public boolean isOverridden() {
		return getName().endsWith("OVERRIDDEN");
	}

	public boolean isDefault() {
		return getName().endsWith("DEFAULT");
	}
}

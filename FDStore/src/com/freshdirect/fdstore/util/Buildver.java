package com.freshdirect.fdstore.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.framework.util.log.LoggerFactory;

public class Buildver {
	private static final Logger LOGGER = LoggerFactory.getInstance(Buildver.class);

	private static Buildver INSTANCE = null;

	public synchronized static Buildver getInstance() {
		if (INSTANCE == null)
			INSTANCE = new Buildver();
		return INSTANCE;
	}

	public synchronized static void mockInstance(Buildver instance) {
		INSTANCE = instance;
	}

	private String buildver = null;

	private Boolean developerMode = null;

	protected Buildver() {
	}

	/**
	 * Determines the build version from the Build-Id in the Manifest of the
	 * deployed EARs. If this process fails then it generates a random
	 * {@link UUID}.
	 * 
	 * @return the determined / generated build version
	 */
	public String getBuildver() {
		if (buildver == null) {
			String version = null;
			try {
				LOGGER.info("trying to deterimine build version using compile time information");
				Class<?> clazz = Class.forName("com.freshdirect.fdstore.util.BuildverCompileTime");
				Method method = clazz.getMethod("getBuildver");
				Object instance = clazz.newInstance();
				Object retValue = method.invoke(instance);
				if (retValue != null) {
					version = retValue.toString();
					LOGGER.info("build version identified using compile time information");
				}
			} catch (Exception e) {
				LOGGER.error("error while identifying build version from compile time information", e);
			}
			if (version == null) {
				try {
					InitialContext ctx;
					LOGGER.info("trying to deterimine build version based on deployment information");
					ctx = new InitialContext();
					MBeanServer connection = (MBeanServer) ctx.lookup("java:comp/env/jmx/runtime");
					ObjectName rts = new ObjectName(
							"com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean");
					ObjectName dc = (ObjectName) connection.getAttribute(rts, "DomainConfiguration");
					ObjectName[] ads = (ObjectName[]) connection.getAttribute(dc, "AppDeployments");

					for (ObjectName ad : ads) {
						String sourcePath = (String) connection.getAttribute(ad, "AbsoluteSourcePath");
						if (sourcePath.toLowerCase().endsWith(".ear")) {
							LOGGER.info("found EAR at " + sourcePath + " as a good candidate");
							try {
								JarFile jarFile = new JarFile(sourcePath);
								Manifest mf = jarFile.getManifest();
								Attributes.Name key = new Attributes.Name("Build-Id");
								if (mf.getMainAttributes().keySet().contains(key)) {
									version = mf.getMainAttributes().getValue(key);
									break;
								} else {
									LOGGER.warn("EAR " + sourcePath + " has missing Build-Id in Manifest, skipping");
								}
							} catch (IOException e) {
							}
						} else {
							LOGGER.info("skipping deployment unit: " + sourcePath + " (not an EAR)");
						}
					}
				} catch (Exception e) {
					LOGGER.error("error while identifying build version via MBeans", e);
				}
			}
			if (version != null) {
				buildver = version;
				LOGGER.info("build version identified: " + buildver);
			} else {
				buildver = UUID.randomUUID().toString();
				LOGGER.warn("could not identify build version, using random UUID instead: " + buildver);
			}
		}
		return buildver;
	}

	/**
	 * Determines whether the current WebLogic domain is in developer mode (not
	 * in production mode).
	 * 
	 * @return if the current WebLogic domain is in developer mode
	 */
	public boolean isDeveloperMode() {
		if (developerMode == null) {
			InitialContext ctx;
			try {
				LOGGER.info("trying to deterimine developer mode based on deployment information");
				ctx = new InitialContext();
				MBeanServer connection = (MBeanServer) ctx.lookup("java:comp/env/jmx/runtime");
				ObjectName rts = new ObjectName(
						"com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean");
				ObjectName dc = (ObjectName) connection.getAttribute(rts, "DomainConfiguration");
				Boolean prodMode = (Boolean) connection.getAttribute(dc, "ProductionModeEnabled");
				if (prodMode != null) {
					developerMode = !prodMode;
					LOGGER.info("determined developer mode from WebLogic domain configuration: " + developerMode);
				}
			} catch (NamingException e) {
				LOGGER.error("error while identifying build version", e);
			} catch (MalformedObjectNameException e) {
				LOGGER.error("error while identifying build version", e);
			} catch (NullPointerException e) {
				LOGGER.error("error while identifying build version", e);
			} catch (AttributeNotFoundException e) {
				LOGGER.error("error while identifying build version", e);
			} catch (InstanceNotFoundException e) {
				LOGGER.error("error while identifying build version", e);
			} catch (MBeanException e) {
				LOGGER.error("error while identifying build version", e);
			} catch (ReflectionException e) {
				LOGGER.error("error while identifying build version", e);
			}
			if (developerMode == null) {
				developerMode = false;
				LOGGER.warn("unable to determine developer mode from WebLogic domain configuration, using default value: false");
			}
		}
		return developerMode;
	}

}

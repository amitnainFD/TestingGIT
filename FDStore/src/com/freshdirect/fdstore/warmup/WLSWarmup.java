/*
 * $Workfile:$
 *
 * $Date:$
 *
 * Copyright (c) 2004 FreshDirect
 *
 */

package com.freshdirect.fdstore.warmup;

import java.util.logging.Level;
import java.util.logging.Logger;

import weblogic.application.ApplicationLifecycleEvent;
import weblogic.application.ApplicationLifecycleListener;
import weblogic.logging.LoggingHelper;

import com.freshdirect.fdstore.FDStoreProperties;

/**
 * WLSWarmup
 *
 * @version    $Revision:$
 * @author     $Author:$
 */
public class WLSWarmup extends ApplicationLifecycleListener {

	public void postStart(ApplicationLifecycleEvent evt) {
		Logger logger = LoggingHelper.getServerLogger();

		if (FDStoreProperties.performStorePreLoad()) {
			Class warmupClass = Warmup.class;
			String className = FDStoreProperties.getWarmupClass();
			if (className != null) {
				try {
					warmupClass = Class.forName(className);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Could not find Warmup class "+ className +" fallback to default.", e);
				}
			}
			try {
				logger.info("initiating warmup using class '" + warmupClass.getName() + "'");
				Warmup warmup = (Warmup) warmupClass.newInstance();
				warmup.warmup();
				logger.info("warmup completed");
			} catch (Exception e) {
				logger.log(Level.SEVERE, "error during warmup", e);
				throw new RuntimeException(e);
			}
		}
	}

}

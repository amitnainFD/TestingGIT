package com.freshdirect.smartstore.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.dsl.CompileException;
import com.freshdirect.smartstore.fdstore.ScoreProvider;
import com.freshdirect.smartstore.impl.GlobalCompiler;
import com.freshdirect.smartstore.scoring.ScoringAlgorithm;

public class SearchScoringRegistry {

    private static final Logger LOGGER = LoggerFactory.getInstance(SearchScoringRegistry.class);

    private static SearchScoringRegistry instance;
    
    protected ScoringAlgorithm globalScoring;
    
    protected ScoringAlgorithm personalScoring;
    
    protected ScoringAlgorithm shortTermPopularityScoring;

	private static String SHORT_TERM_POPULARITY_DEFAULT = "Popularity8W_Discretized;QualityRating_Discretized2";

    
    public synchronized static SearchScoringRegistry getInstance() {
        if (instance == null) {
            instance = new SearchScoringRegistry();
        }
        return instance;
    }
    
    public synchronized ScoringAlgorithm getGlobalScoringAlgorithm() {
        if (globalScoring == null) {
            String global = FDStoreProperties.getGlobalPopularityScoring();
            if (global == null || global.trim().length() == 0) {
                global = ScoreProvider.GLOBAL_POPULARITY;
            }
            setGlobalScoringAlgorithm(global);
            ScoreProvider.getInstance().acquireFactors(new ArrayList<String>(Arrays.asList(globalScoring.getVariableNames())));
        }
        return globalScoring;
    }

    public synchronized ScoringAlgorithm getShortTermPopularityScoringAlgorithm() {
    	if (shortTermPopularityScoring == null) {
			String algorithm = FDStoreProperties.getShortTermPopularityScoring();
			if (algorithm == null || algorithm.length() == 0)
				algorithm = SHORT_TERM_POPULARITY_DEFAULT;
    		try {
				shortTermPopularityScoring = GlobalCompiler.getInstance().createScoringAlgorithm(algorithm);
			} catch (CompileException e) {
	            LOGGER.error("Creating scoring algorithm  : " + algorithm, e);
	            throw new FDRuntimeException(e);
			}
    		ScoreProvider.getInstance().acquireFactors(new ArrayList<String>(Arrays.asList(shortTermPopularityScoring.getVariableNames())));
    	}
        return shortTermPopularityScoring;
    }
    
    public synchronized void setGlobalScoringAlgorithm(String global) {
        try {
            globalScoring = GlobalCompiler.getInstance().createScoringAlgorithm(global);
        } catch (CompileException e) {
            LOGGER.error("Creating scoring algorithm  : " + global, e);
            throw new FDRuntimeException(e);
        }
    }

    public synchronized ScoringAlgorithm getUserScoringAlgorithm() {
        if (personalScoring == null) {
            String user = FDStoreProperties.getUserPopularityScoring();
            if (user == null || user.trim().length() == 0) {
                user = ScoreProvider.USER_FREQUENCY + ',' + ScoreProvider.GLOBAL_POPULARITY;
            }
            setUserScoringAlgorithm(user);
            ScoreProvider.getInstance().acquireFactors(new ArrayList<String>(Arrays.asList(personalScoring.getVariableNames())));
        }
        return personalScoring;
    }

    public synchronized void setUserScoringAlgorithm(String user) {
        try {
            personalScoring = GlobalCompiler.getInstance().createScoringAlgorithm(user);
        } catch (CompileException e) {
            LOGGER.error("Creating scoring algorithm  : " + user, e);
            throw new FDRuntimeException(e);
        }
    }
    
    
    
    public void load() {
        Set<String> factors = new HashSet<String>();
        factors.addAll(Arrays.asList(getGlobalScoringAlgorithm().getVariableNames()));
        factors.addAll(Arrays.asList(getUserScoringAlgorithm().getVariableNames()));
        ScoreProvider.getInstance().acquireFactors(factors);
    }

}

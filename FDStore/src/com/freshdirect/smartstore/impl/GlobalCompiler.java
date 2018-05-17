package com.freshdirect.smartstore.impl;

import java.util.Iterator;
import java.util.Set;

import com.freshdirect.smartstore.dsl.CompileException;
import com.freshdirect.smartstore.dsl.Expression;
import com.freshdirect.smartstore.fdstore.ScoreProvider;
import com.freshdirect.smartstore.scoring.DataGenerator;
import com.freshdirect.smartstore.scoring.DataGeneratorCompiler;
import com.freshdirect.smartstore.scoring.ScoringAlgorithm;
import com.freshdirect.smartstore.scoring.ScoringAlgorithmCompiler;

/**
 * This class
 * 
 * @author zsombor
 * 
 */
public class GlobalCompiler {

    static GlobalCompiler instance;
    
    DataGeneratorCompiler    compiler;
    static int               index = 0;
    ScoringAlgorithmCompiler saCompiler;

    public DataGeneratorCompiler getDataGeneratorCompiler() {
        if (compiler == null) {
            compiler = new DataGeneratorCompiler(ScoreProvider.ZONE_DEPENDENT_FACTORS_ARRAY);
        }
        return compiler;
    }

    public ScoringAlgorithmCompiler getScoringAlgorithmCompiler() {
        if (saCompiler == null) {
            saCompiler = new ScoringAlgorithmCompiler();
        }
        return saCompiler;
    }

    public void setCompiler(DataGeneratorCompiler compiler) {
        this.compiler = compiler;
    }

    public void setScoringAlgorithmCompiler(ScoringAlgorithmCompiler saCompiler) {
        this.saCompiler = saCompiler;
    }

    public synchronized DataGenerator createDataGenerator(String expression) throws CompileException {
        return getDataGeneratorCompiler().createDataGenerator("DataGenerator" + index++, expression);
    }
    
    public synchronized ScoringAlgorithm createScoringAlgorithm(String expression) throws CompileException {
        return getScoringAlgorithmCompiler().createScoringAlgorithm("ScoringAlgorithm"+(index++), expression);
    }

    public synchronized static GlobalCompiler getInstance() {
        if (instance == null) {
            instance = new GlobalCompiler();
            instance.loadFactorNames();
        }
        return instance;
    }

    /**
     * Load factor names from the ScoreProvider.
     * 
     */
    private void loadFactorNames() {
        DataGeneratorCompiler dg = getDataGeneratorCompiler();

        String[] datasourceNames = ScoreProvider.getInstance().getDatasourceNames();
        for (int i=0;i<datasourceNames.length;i++) {
            dg.addVariable(datasourceNames[i], Expression.RET_SET);
        }
        dg.setGlobalVariables(ScoreProvider.getInstance().getNonPersonalizedFactors());
        
        ScoringAlgorithmCompiler sc = getScoringAlgorithmCompiler();

        Set factors = ScoreProvider.getInstance().getAvailableFactors();
        
        for (Iterator iter = factors.iterator();iter.hasNext();) {
            String name = (String) iter.next();
            dg.addVariable(name, Expression.RET_FLOAT);
            
            sc.addVariable(name, Expression.RET_FLOAT);
        }
    }
    
    /**
     * DO NOT CALL DIRECTLY !!! ONLY FOR TESTING !!!
     * 
     * @param name
     * @param type
     */
    public void addVariable(String name, int type) {
        switch (type) {
            case Expression.RET_INT :
            case Expression.RET_FLOAT:
                getScoringAlgorithmCompiler().addVariable(name, type);
            case Expression.RET_SET :
                getDataGeneratorCompiler().addVariable(name, type);
            default :
                
        }
    }

    public static void setInstance(GlobalCompiler gc) {
        instance = gc;
    }

}

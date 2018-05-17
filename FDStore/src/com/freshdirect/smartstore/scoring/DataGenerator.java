package com.freshdirect.smartstore.scoring;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.smartstore.SessionInput;

public class DataGenerator {

    Set<String> factors;

    public List<? extends ContentNodeModel> generate(SessionInput sessionInput, DataAccess input) {
        return Collections.<ContentNodeModel>emptyList();
    }

    public void setFactors(Set<String> factors) {
        this.factors = factors;
    }

    public Set<String> getFactors() {
        return factors;
    }
    
    public String getGeneratedCode() {
        return "";
    }
}

package com.freshdirect.smartstore.scoring;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.smartstore.sampling.RankedContent;

public class OrderingFunction {
    // if multiple number is the returning statement, we can't interpret as a probability.
    TreeSet<Score> scores = new TreeSet<Score>();

    public void addScore(ContentNodeModel contentNode, double[] score) {
        Score sc = new Score(contentNode, score);
        scores.add(sc);
    }
    
    public List<RankedContent.Single> getRankedContents() {
        int max = scores.size();
        List<RankedContent.Single> rankedContents = new ArrayList<RankedContent.Single>(max);
        int i = 0;
        for (Score sc : scores) {
            rankedContents.add(new RankedContent.Single(max + 1 - i, sc.getNode()));
            i++;
        }
        return rankedContents;
    }
    
    public String getInitializerCode() {
        return "return new "+this.getClass().getName()+" ();"; 
    }
}

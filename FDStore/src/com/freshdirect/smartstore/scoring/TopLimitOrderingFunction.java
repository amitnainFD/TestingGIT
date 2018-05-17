package com.freshdirect.smartstore.scoring;

import java.util.ArrayList;
import java.util.List;

import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.smartstore.sampling.RankedContent;

/**
 * Implement sorting functionality which support zeroing out all non-maximum values.
 * It is used in the following scoring function: 'Recency:top,GlobalPopularity' which causes the top recent product sorted to the first position, 
 * and the rest sorted by GlobalPopularity.
 * 
 * @author zsombor
 *
 */
public class TopLimitOrderingFunction extends OrderingFunction {
    
    int position;
    ContentNodeModel maximum;
    double maxValue = Double.NEGATIVE_INFINITY;
    List<Score> nodes = new ArrayList<Score>();
    
    public TopLimitOrderingFunction() {
        position  = 0;
    }
    
    public TopLimitOrderingFunction(int pos) {
        this.position = pos;
    }
    
    public void addScore(ContentNodeModel contentNode, double[] score) {
        nodes.add(new Score(contentNode, score));
        if (score[position]>maxValue) {
            maximum = contentNode;
            maxValue = score[position];
        }
    }
    
    public List<RankedContent.Single> getRankedContents() {
        for (Score sc : this.nodes) {
            if (sc.node!=maximum) {
                sc.values[position] = 0;
            }
            scores.add(sc);
        }
        return super.getRankedContents();
    }
    
    @Override
    public String getInitializerCode() {
        return "return new " + this.getClass().getName() + " (" + position + ");";
    }

}

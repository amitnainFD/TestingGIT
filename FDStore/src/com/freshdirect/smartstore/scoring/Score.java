package com.freshdirect.smartstore.scoring;

import com.freshdirect.fdstore.content.ContentNodeModel;

public class Score implements Comparable {
    ContentNodeModel node;
    double[]         values;

    public Score(int size) {
        this.values = new double[size];
    }

    public Score(ContentNodeModel node, double[] values) {
        this.node = node;
        this.values = values;
    }

    public void set(int pos, double value) {
        values[pos] = value;
    }

    public void set(int pos, int value) {
        values[pos] = value;
    }

    public double get(int pos) {
        return values[pos];
    }

    public int size() {
        return values.length;
    }

    public ContentNodeModel getNode() {
        return node;
    }

    public void setNode(ContentNodeModel node) {
        this.node = node;
    }
    
    public double[] getScores() {
    	return values;
    }

    public int compareTo(Object o) {
        Score sc = (Score) o;
        for (int i = 0; i < values.length; i++) {
            if (!(values[i] == sc.values[i])) {
                if (values[i] > sc.values[i]) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
        return node.getContentKey().getId().compareTo(sc.node.getContentKey().getId());
    }

    /**
     * Stupid HACK.<br>
     * Someone implemented the content id comparison into the default {@link Score#compareTo(Object)} method which is VERY WRONG !!!
     * @param o
     * @return
     */
    public int compareTo2(Object o) {
        Score sc = (Score) o;
        for (int i = 0; i < values.length; i++) {
            if (!(values[i] == sc.values[i])) {
                if (values[i] > sc.values[i]) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
        return 0;
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append( "Score[" );
    	sb.append( node.getContentKey().getEncoded() );
    	sb.append( "=" );
    	for ( double d : values ) {
        	sb.append( d );    		
        	sb.append( "," );    		
    	}
    	sb.append( "]" );
    	return sb.toString();
    }
}

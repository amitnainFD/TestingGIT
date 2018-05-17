package com.freshdirect.smartstore.scoring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.smartstore.sampling.RankedContent.Single;

public class TopNLimitFunction extends OrderingFunction {

    static class Limit implements Comparator<Score> {
        int position;
        int maxElement;

        TreeSet<Score> elements;

        public Limit(int position, int maxElement) {
            this.position = position;
            this.maxElement = maxElement;
            this.elements = new TreeSet<Score>(this);
        }

        @Override
        public int compare(Score o1, Score o2) {
            if (!(o1.values[position] == o2.values[position])) {
                if (o1.values[position] > o2.values[position]) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return o1.node.getContentKey().getId().compareTo(o2.node.getContentKey().getId());
        }

        public void add(Score s) {
            elements.add(s);
        }

        public void calculateFinalScores() {
            int i = 0;
            for (Score s : elements) {
                i++;
                if (i > maxElement) {
                    // null out the other elements
                    s.values[position] = 0;
                }
            }
        }
    }

    List<Limit> limits = new ArrayList<Limit>();
    List<Score> notOrderedScores = new ArrayList<Score>();

    public TopNLimitFunction() {
    }

    @Override
    public String getInitializerCode() {
        StringBuilder b = new StringBuilder();
        b.append(this.getClass().getName()).append(" t = new ").append(this.getClass().getName()).append("();\n");
        for (Limit l : limits) {
            b.append("t.addTopN(").append(l.position).append(",").append(l.maxElement).append(");\n");
        }
        b.append(" return t;\n");
        return b.toString();
    }
    
    public void addTopN(int position, int maximum) {
        limits.add(new Limit(position, maximum));
    }

    @Override
    public void addScore(ContentNodeModel contentNode, double[] score) {
        Score s = new Score(contentNode, score);
        notOrderedScores.add(s);
        for (Limit l : limits) {
            l.add(s);
        }
    }


    @Override
    public List<Single> getRankedContents() {
        for (Limit l : limits) {
            l.calculateFinalScores();
        }
        scores = new TreeSet<Score>();
        scores.addAll(notOrderedScores);
        return super.getRankedContents();
    }
}

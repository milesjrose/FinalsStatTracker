package uk.ac.ed.inf.model;

public class BestMatch {
    private String label;
    private double score;

    public BestMatch(String label, double score) {
        this.label = label;
        this.score = score;
    }

    public String getLabel() {
        return label;
    }

    public double getScore() {
        return score;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setScore(double score) {
        this.score = score;
    }
}

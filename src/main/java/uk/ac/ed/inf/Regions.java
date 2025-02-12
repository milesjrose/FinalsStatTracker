package uk.ac.ed.inf;

import java.awt.Rectangle;

public class Regions {
    public final Rectangle p1;
    public final Rectangle s1;
    public final Rectangle p2;
    public final Rectangle s2;
    public final Rectangle p3;
    public final Rectangle s3;

    /**
     * Constructor to initialize all six OCR regions.
     *
     * @param p1 First player’s stats region
     * @param s1 First player’s score region
     * @param p2 Second player’s stats region
     * @param s2 Second player’s score region
     * @param p3 Third player’s stats region
     * @param s3 Third player’s score region
     */
    public Regions(Rectangle p1, Rectangle s1, Rectangle p2, Rectangle s2, Rectangle p3, Rectangle s3) {
        this.p1 = p1;
        this.s1 = s1;
        this.p2 = p2;
        this.s2 = s2;
        this.p3 = p3;
        this.s3 = s3;
    }

    /**
     * Factory method to create default OCR regions.
     * Adjust these values based on your screen resolution and UI.
     */
    public static Regions getDefaultRegions() {
        return new Regions(
                new Rectangle(100, 200, 400, 50),  // P1 stats
                new Rectangle(500, 200, 100, 50),  // S1 score
                new Rectangle(100, 300, 400, 50),  // P2 stats
                new Rectangle(500, 300, 100, 50),  // S2 score
                new Rectangle(100, 400, 400, 50),  // P3 stats
                new Rectangle(500, 400, 100, 50)   // S3 score
        );
    }

    public Rectangle[] getRegions(){
        return new Rectangle[]{p1, s1, p2, s2, p3, s3};
    }

    @Override
    public String toString() {
        return "Regions{" +
                "p1=" + p1 + ", s1=" + s1 +
                ", p2=" + p2 + ", s2=" + s2 +
                ", p3=" + p3 + ", s3=" + s3 +
                '}';
    }
}

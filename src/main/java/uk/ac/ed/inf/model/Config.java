package uk.ac.ed.inf.model;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;


public class Config {
    public static final int STAT_W = 405-325;
    public static final int STAT_H = 280+15;
    public static final int NAME_W = 260;
    public static final int NAME_H = 50;
    public static final int SUMMARY_W = 240;
    public static final int SUMMARY_H = 60;
    public static final int NUM_STATS = 7;

    private static final Point[] stats = {
        new Point(625+325+5, 960-10),
        new Point(1080+325, 960-10),
        new Point(1530+325+5, 960-10),
    };

    private static final Point[] names = {
        new Point(700, 290),
        new Point(1150, 290),
        new Point(1600, 290),
    };

    private static final Point summary = new Point(155, 40);

    public static final Rectangle statBox(int player, int index){
        // Get player stat box, divide height by 7, multiply by index, add to y of player stat box
        return new Rectangle(stats[player-1].x, stats[player-1].y + (index * STAT_H / NUM_STATS), STAT_W, STAT_H / NUM_STATS);
    }

    public static final Rectangle nameBox(int player){
        return new Rectangle(names[player-1].x, names[player-1].y, NAME_W, NAME_H);
    }

    public static final Rectangle summaryBox(){
        return new Rectangle(summary.x, summary.y, SUMMARY_W, SUMMARY_H);
    }

    public static final List<Rectangle> allRegions(){
        List<Rectangle> boxes = new ArrayList<>();
        // player names
        boxes.add(nameBox(1));
        boxes.add(nameBox(2));
        boxes.add(nameBox(3));
        // summary
        boxes.add(summaryBox());
        // For each player
        for (int n = 1; n <= 3; n++){
            // For each stat
            //int offset = n-1*NUM_STATS;
            for (int i = 0; i < NUM_STATS; i++){
                boxes.add(statBox(n, i));
            }
        }
        return boxes;
    }
}

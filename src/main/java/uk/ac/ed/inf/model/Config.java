package uk.ac.ed.inf.model;
import java.awt.Rectangle;


public class Config {
    public static Regions getRegions(){
        return new Regions(new Rectangle(700, 290, 260, 50),
        new Rectangle(625, 960, 415, 290),
        new Rectangle(1150, 290, 260, 50),
        new Rectangle(1080, 960, 415, 290),
        new Rectangle(1600, 290, 260, 50),
        new Rectangle(1530, 960, 415, 290));
    }
}

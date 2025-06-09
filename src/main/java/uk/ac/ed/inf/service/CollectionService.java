package uk.ac.ed.inf.service;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import uk.ac.ed.inf.checks.EndGameCheck;
import uk.ac.ed.inf.model.Config;
import uk.ac.ed.inf.model.PlayerStats;
import uk.ac.ed.inf.textProc.ImageProc;
import uk.ac.ed.inf.utility.Screenshot;


public class CollectionService {

    public static BufferedImage screenshot() {
        System.out.println("Capturing screenshot...");
        BufferedImage screenshot = Screenshot.getScreenshot();
        return screenshot;
    }

    public static boolean isEndGame(BufferedImage screenshot){
        return EndGameCheck.check(screenshot);
    }

    public static PlayerStats parse(BufferedImage screenshot, int player){
        System.out.println("Parsing stats...");
        // Get regions
        Rectangle nameBox = Config.nameBox(player);
        Rectangle[] statBoxes = new Rectangle[Config.NUM_STATS];
        for (int i = 0; i < Config.NUM_STATS; i++){
            statBoxes[i] = Config.statBox(player, i);
        }
        // Get name
        String name = ImageProc.getText(ImageProc.crop(screenshot, nameBox));
        //get stats
        int[] stats = new int[Config.NUM_STATS];
        for (int i = 0; i < Config.NUM_STATS; i++){
            stats[i] = ImageProc.getInt(ImageProc.crop(screenshot, statBoxes[i]));
        }
        return new PlayerStats(name, stats);
    }
}

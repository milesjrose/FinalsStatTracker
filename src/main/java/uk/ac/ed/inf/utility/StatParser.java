package uk.ac.ed.inf.utility;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import uk.ac.ed.inf.model.PlayerStats;
import uk.ac.ed.inf.model.Config;
import uk.ac.ed.inf.textProc.ImageProc;
import uk.ac.ed.inf.database.PlayerStatsDAO;
import java.sql.SQLException;
import java.util.List;

public class StatParser {

    public static PlayerStats parseStats(BufferedImage screenshot, int player){
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

    public static void saveStats(PlayerStats stats){
        try {
            if (PlayerStatsDAO.getPlayerStats("hash", String.valueOf(stats.hash)) == null){ // Check if the stats already exist
                PlayerStatsDAO.addPlayerStats(stats);
                System.out.println("Player stats saved for: " + stats.name);
            }
            else{
                System.out.println("Player stats already exists for: " + stats.name);
            }

        } catch (SQLException e) {
            System.err.println("Error saving player stats: " + e.getMessage());
            // Handle the error appropriately
        }
    }

    public static PlayerStats loadStats(String hash){
        try {
            PlayerStats stats = PlayerStatsDAO.getPlayerStats("hash", hash);
            System.out.println("Player stats loaded for: " + stats.name);
            return stats;
        } catch (SQLException e) {
            System.err.println("Error loading player stats: " + e.getMessage());
            // Handle the error appropriately
        }
        return null;
    }

    public static void delStats(String hash){
        try{
            PlayerStatsDAO.deletePlayerStats(hash);
            System.out.println("Stats deleted for: " + hash);
        } catch (SQLException e){
            System.out.println("Error deleting stats: " + e.getMessage());
        }
    }

    public static boolean checkHash(String hash){
        try{
            return PlayerStatsDAO.getPlayerStats("hash", hash) != null;
        } catch (SQLException e){
            System.out.println("Error checking hash: " + e.getMessage());
        }
        return false;
    }

    public static List<PlayerStats> loadAll(){
        try{
            List<PlayerStats> stats = PlayerStatsDAO.getAllPlayerStats();
            for (PlayerStats stat : stats){
                System.out.println("Stats loaded: " + stat.toString());
            }
            return stats;
        } catch (SQLException e){
            System.out.println("Error loading all stats: " + e.getMessage());
        }
        return null;
    }
}

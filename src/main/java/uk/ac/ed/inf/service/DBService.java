package uk.ac.ed.inf.service;

import java.sql.SQLException;
import java.util.List;

import uk.ac.ed.inf.database.PlayerStatsDAO;
import uk.ac.ed.inf.model.PlayerStats;

public class DBService {

    public static void save(PlayerStats stats){
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

    public static PlayerStats load(String hash){
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

    public static void del(String hash){
        try{
            PlayerStatsDAO.deletePlayerStats(hash);
            System.out.println("Stats deleted for: " + hash);
        } catch (SQLException e){
            System.out.println("Error deleting stats: " + e.getMessage());
        }
    }

    public static void delAll(){
        try {
            List<PlayerStats> stats = PlayerStatsDAO.getAllPlayerStats();
            for (PlayerStats stat : stats){
                del(String.valueOf(stat.hash));
            }
        } catch (SQLException e) {
            System.out.println("Error deleting all stats: " + e.getMessage());
        }
    }

    public static boolean check(String hash){
        try{
            return PlayerStatsDAO.getPlayerStats("hash", hash) != null;
        } catch (SQLException e){
            System.out.println("Error checking hash: " + e.getMessage());
        }
        return false;
    }
}

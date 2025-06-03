package uk.ac.ed.inf.model;

import java.lang.reflect.Field;

public class PlayerStats {
    public String name;
    public int combat;
    public int objective;
    public int support;
    public int elims;
    public int assists;
    public int deaths;
    public int revives;

    public PlayerStats(String nameString, int[] statInts){
        this.name = nameString;
        if (statInts.length != Config.NUM_STATS){
            System.out.println("Invalid stats (len = " + statInts.length + "):");
            for (int stat : statInts){
                System.out.println(stat);
            }
            statInts = new int[Config.NUM_STATS];
            for (int i = 0; i < Config.NUM_STATS; i++) {
                statInts[i] = 0;
            }
        }
        
        try {
            Field[] fields = PlayerStats.class.getDeclaredFields();
            // Skip the first field (name) since it's already set
            for (int i = 1; i <= Config.NUM_STATS; i++) {
                fields[i].setAccessible(true);
                fields[i].set(this, statInts[i-1]);
            }
        } catch (IllegalAccessException e) {
            System.out.println("Error setting stats: " + e.getMessage());
        }
    }

    @Override
    public String toString(){
        return "Name: " + name + "\n" +
               "Combat: " + combat + "\n" +
               "Objective: " + objective + "\n" +
               "Support: " + support + "\n" +
               "Elims: " + elims + "\n" +
               "Assists: " + assists + "\n" +
               "Deaths: " + deaths + "\n" +
               "Revives: " + revives + "\n" + "\n";
    }
}
